/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.dbversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.datagear.util.Global;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.version.AbstractVersionContentReader;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * 数据库版本管理器。
 * <p>
 * 此类用于读取SQL脚本文件，执行数据库版本升级。
 * </p>
 * <p>
 * 它对SQL脚本文件格式有如下规范：
 * </p>
 * <ul>
 * <li>以“--”开头的行表示注释行，将被忽略执行；</li>
 * <li>空行用于分隔SQL语句；</li>
 * <li>“--version[1.0.0]...”是版本行，用于标识后续的SQL版本，直到下一个版本行或者文件末尾；</li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class DbVersionManager extends AbstractVersionContentReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DbVersionManager.class);

	/** 脚本资源文件路径 */
	public static final String DEFAULT_SQL_SCRIPT_LOCATION = ResourceLoader.CLASSPATH_URL_PREFIX
			+ "org/datagear/management/ddl/datagear.sql";

	/** 数据库SQL文件中版本号注释开头标识 */
	public static final String VERSION_LINE_PREFIX = "--version[";

	/** 数据库SQL文件中版本号注释结尾标识 */
	public static final String VERSION_LINE_SUFFIX = "]";

	/**
	 * 低于此版本将不支持自动升级。
	 * 在3.0.0版本，整理了历届版本的SQL变更记录，合并为2.13.0版本SQL（详见：org/datagear/management/ddl/datagear.sql），
	 * 因此，低于2.13.0版本的程序，必须先下载2.13.0版程序，使数据库自动升级至2.13.0版本，然后再下载高于2.13.0版本的程序，才能正确自动升级。
	 */
	public static final Version UPGRADE_UNCOMPATIBLE_VERSION_LOWER_THAN = Version.valueOf("2.13.0");

	public static final String DEFAULT_VERSION_TABLE_NAME = "DATAGEAR_VERSION";

	private DataSource dataSource;

	private ResourceLoader resourceLoader;

	private String sqlScriptLocation = DEFAULT_SQL_SCRIPT_LOCATION;

	private String sqlScriptEncoding = ENCODING_UTF8;

	private String versionTableName = DEFAULT_VERSION_TABLE_NAME;

	private volatile SqlVersionContents sqlVersionContents = null;

	public DbVersionManager()
	{
		super();
	}

	public DbVersionManager(DataSource dataSource, ResourceLoader resourceLoader)
	{
		super();
		this.dataSource = dataSource;
		this.resourceLoader = resourceLoader;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public ResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	public String getSqlScriptLocation()
	{
		return sqlScriptLocation;
	}

	public void setSqlScriptLocation(String sqlScriptLocation)
	{
		this.sqlScriptLocation = sqlScriptLocation;
	}

	public String getSqlScriptEncoding()
	{
		return sqlScriptEncoding;
	}

	public void setSqlScriptEncoding(String sqlScriptEncoding)
	{
		this.sqlScriptEncoding = sqlScriptEncoding;
	}

	public String getVersionTableName()
	{
		return versionTableName;
	}

	public void setVersionTableName(String versionTableName)
	{
		this.versionTableName = versionTableName;
	}

	/**
	 * 获取当前版本。
	 * 
	 * @return 当没有定义时返回{@code null}
	 * @throws DbVersionManagerException
	 *             当获取异常时（通常是版本表不存在）
	 */
	public Version getCurrentVersion() throws DbVersionManagerException
	{
		Connection cn = null;
		try
		{
			cn = this.dataSource.getConnection();
			return getCurrentVersion(cn);
		}
		catch (SQLException e)
		{
			throw new DbVersionManagerException(e);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	/**
	 * 获取当前版本。
	 * <p>
	 * 当底层出现SQL异常时（通常是版本表不存在，数据库没有初始化），此方法会返回{@linkplain Version#ZERO_VERSION}。
	 * </p>
	 * 
	 * @return 当没有定义时返回{@code null}
	 */
	public Version getCurrentVersionSafe()
	{
		Connection cn = null;

		try
		{
			return getCurrentVersionSafe(cn);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	/**
	 * 升级至最新版本。
	 * 
	 * @throws DbVersionManagerException
	 */
	public void upgrade() throws DbVersionManagerException
	{
		upgrade(Global.VERSION);
	}

	/**
	 * 升级至指定版本。
	 * 
	 * @param to
	 *            目标版本
	 * @throws DbVersionManagerException
	 */
	public void upgrade(String to) throws DbVersionManagerException
	{
		Version toVersion = Version.valueOf(to);
		upgrade(toVersion);
	}

	/**
	 * 升级至指定版本。
	 * 
	 * @param to
	 *            目标版本
	 * @throws DbVersionManagerException
	 */
	public void upgrade(Version to) throws DbVersionManagerException
	{
		Connection cn = null;
		try
		{
			cn = this.dataSource.getConnection();

			Version current = getCurrentVersionSafe(cn);

			if (current == null)
				throw new DbVersionManagerException("No version found in table : " + this.versionTableName);

			upgrade(cn, current, to);
		}
		catch (SQLException e)
		{
			throw new DbVersionManagerException(e);
		}
		catch (IOException e)
		{
			throw new DbVersionManagerException(e);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	/**
	 * 获取从{@code from}升级至{@code to}版本需执行的SQL。
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws DbVersionManagerException
	 */
	public List<VersionContent> getUpgradeSqls(Version from, Version to) throws DbVersionManagerException
	{
		try
		{
			return resolveUpgradeSqlVersionContents(from, to);
		}
		catch (IOException e)
		{
			throw new DbVersionManagerException(e);
		}
	}

	/**
	 * 安全获取当前版本号。
	 * <p>
	 * 当底层出现SQL异常时（通常是版本表不存在，数据库没有初始化），此方法会返回{@linkplain Version#ZERO_VERSION}。
	 * </p>
	 * 
	 * @param cn
	 * @return 当版本表里没有版本号数据时返回{@code null}
	 */
	protected Version getCurrentVersionSafe(Connection cn)
	{
		Version current = null;

		try
		{
			current = getCurrentVersion(cn);
		}
		catch(SQLException e)
		{
			current = Version.ZERO_VERSION;

			if (LOGGER.isWarnEnabled())
				LOGGER.warn("The database may not be initialized, version [" + current + "] is returned", e);
		}

		return current;
	}

	/**
	 * 执行升级。
	 * 
	 * @param cn
	 * @param from
	 * @param to
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void upgrade(Connection cn, Version from, Version to) throws SQLException, IOException
	{
		// 自动升级不兼容的版本
		if (from.isHigherThan(Version.ZERO_VERSION) && from.isLowerThan(UPGRADE_UNCOMPATIBLE_VERSION_LOWER_THAN))
		{
			throw new DbVersionManagerException("Upgrade lower than " + Global.PRODUCT_NAME_EN + "-"
					+ UPGRADE_UNCOMPATIBLE_VERSION_LOWER_THAN.toString() + " NOT support, you MUST run "
					+ Global.PRODUCT_NAME_EN + "-" + UPGRADE_UNCOMPATIBLE_VERSION_LOWER_THAN.toString()
					+ " for upgrading version to " + UPGRADE_UNCOMPATIBLE_VERSION_LOWER_THAN.toString()
					+ " first, then shutdown it, then run " + Global.PRODUCT_NAME_EN + "-"
					+ to);
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Start upgrade database version from [" + from + "] to [" + to + "]");

		if (to.isHigherThan(from))
		{
			updateSchema(cn, from, to);
			updateVersion(cn, to);
		}
		else
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Upgrade database version from [" + from + "] to [" + to + "] is ignored, [" + to
						+ "] is not higher than [" + from + "]");
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Finish upgrade database version from [" + from + "] to [" + to + "]");
	}

	/**
	 * 获取当前版本。
	 * 
	 * @param cn
	 * @return 当版本表中没有记录时会返回{@code null}
	 * @throws SQLException
	 *             当执行查询版本SQL出现异常时，比如：版本表不存在（通常是数据库没有初始化）
	 */
	protected Version getCurrentVersion(Connection cn) throws SQLException
	{
		Version version = null;

		Statement st = cn.createStatement();

		try
		{
			try
			{
				version = getCurrentVersionForNewTable(cn, st);
			}
			catch (SQLException e)
			{
				version = getCurrentVersionForOldTable(cn, st);
			}
		}
		finally
		{
			// 需在这里释放st，不然如果更新脚本里有版本号表的DDL语句，可能会导致锁表无法执行更新脚本
			JdbcUtil.closeStatement(st);
		}

		return version;
	}

	/**
	 * 获取当前版本。
	 * <p>
	 * 此方法用于从新结构（{@code 2.2.0及以上版本}）的{@linkplain #versionTableName}中读取版本号。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	protected Version getCurrentVersionForNewTable(Connection cn, Statement st) throws SQLException
	{
		Version version = null;

		ResultSet rs = st.executeQuery("SELECT VERSION_VALUE FROM " + this.versionTableName);

		if (rs.next())
		{
			version = Version.valueOf(rs.getString(1));
		}

		return version;
	}

	/**
	 * 获取当前版本。
	 * <p>
	 * 此方法用于从旧结构（{@code 2.2.0以下版本}）的{@linkplain #versionTableName}中读取版本号。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @return
	 * @throws SQLException
	 */
	protected Version getCurrentVersionForOldTable(Connection cn, Statement st) throws SQLException
	{
		Version version = null;

		ResultSet rs = st.executeQuery(
				"SELECT VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD FROM " + this.versionTableName);

		if (rs.next())
		{
			String major = rs.getString(1);
			String minor = rs.getString(2);
			String revision = rs.getString(3);
			String build = rs.getString(4);

			version = new Version(major, minor, revision, build);
		}

		return version;
	}

	/**
	 * 更新数据库中的版本号。
	 * 
	 * @param cn
	 * @param version
	 * @throws SQLException
	 */
	protected void updateVersion(Connection cn, Version version) throws SQLException
	{
		try
		{
			updateVersionForNewTable(cn, version);
		}
		catch (SQLException e)
		{
			updateVersionForOldTable(cn, version);
		}
	}

	/**
	 * 更新数据库中的版本号。
	 * <p>
	 * 此方法用于更新新结构（{@code 2.2.0及以上版本}）的{@linkplain #versionTableName}中的版本号。
	 * </p>
	 * 
	 * @param cn
	 * @param version
	 * @throws SQLException
	 */
	protected void updateVersionForNewTable(Connection cn, Version version) throws SQLException
	{
		PreparedStatement st = cn.prepareStatement("UPDATE " + this.versionTableName + " SET VERSION_VALUE = ?");

		st.setString(1, Version.stringOf(version));

		int count = st.executeUpdate();

		if (count == 0)
		{
			st = cn.prepareStatement("INSERT INTO " + this.versionTableName + " (VERSION_VALUE) VALUES(?)");

			st.setString(1, Version.stringOf(version));

			st.executeUpdate();
		}
	}

	/**
	 * 更新数据库中的版本号。
	 * <p>
	 * 此方法用于更新旧结构（{@code 2.2.0以下版本}）的{@linkplain #versionTableName}中的版本号。
	 * </p>
	 * 
	 * @param cn
	 * @param version
	 * @throws SQLException
	 */
	protected void updateVersionForOldTable(Connection cn, Version version) throws SQLException
	{
		PreparedStatement st = cn.prepareStatement("UPDATE " + this.versionTableName
				+ " SET VERSION_MAJOR = ?, VERSION_MINOR = ?, VERSION_REVISION = ?, VERSION_BUILD = ?");

		st.setString(1, version.getMajor());
		st.setString(2, version.getMinor());
		st.setString(3, version.getRevision());
		st.setString(4, version.getBuild());

		int count = st.executeUpdate();

		if (count == 0)
		{
			st = cn.prepareStatement("INSERT INTO " + this.versionTableName
					+ " (VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION, VERSION_BUILD) VALUES(?, ?, ?, ?)");

			st.setString(1, version.getMajor());
			st.setString(2, version.getMinor());
			st.setString(3, version.getRevision());
			st.setString(4, version.getBuild());

			st.executeUpdate();
		}
	}

	/**
	 * 更新模式脚本。
	 * 
	 * @param cn
	 * @param from
	 * @param to   目标版本
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void updateSchema(Connection cn, Version from, Version to) throws SQLException, IOException
	{
		List<VersionContent> versionContents = resolveUpgradeSqlVersionContents(from, to);

		if (versionContents == null || versionContents.isEmpty())
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("No upgrade sqls from version [" + from + "] to [" + to + "]");
		}
		else
		{
			for (VersionContent versionContent : versionContents)
			{
				Version myVersion = versionContent.getVersion();

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Got upgrade sqls for version from [" + from + "] to [" + myVersion + "] (line "
							+ versionContent.getVersionStartLine() + " - " + versionContent.getVersionEndLine()
							+ ") in [" + this.sqlScriptLocation + "]");

				executeSqls(cn, versionContent.getContents());
			}
		}
	}

	/**
	 * 执行SQL脚本列表。
	 * 
	 * @param cn
	 * @param sqls
	 * @throws SQLException
	 */
	protected void executeSqls(Connection cn, List<String> sqls) throws SQLException
	{
		Statement st = null;

		try
		{
			st = cn.createStatement();

			for (String sql : sqls)
			{
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Start execute sql : " + sql);

				st.execute(sql);
			}
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}
	}

	/**
	 * 解析升级SQL脚本内容。
	 * 
	 * @param from 起始版本（不包含）
	 * @param to   目标版本（包含）
	 * @return
	 * @throws IOException
	 */
	protected List<VersionContent> resolveUpgradeSqlVersionContents(Version from, Version to) throws IOException
	{
		List<VersionContent> myVersionContents = new ArrayList<VersionContent>();

		List<VersionContent> allVersionContents = resolveAllSqlVersionContents();
		for (VersionContent vc : allVersionContents)
		{
			Version myVersion = vc.getVersion();
			
			if (myVersion.isHigherThan(from) && (myVersion.equals(to) || myVersion.isLowerThan(to)))
			{
				myVersionContents.add(vc);
			}
		}

		return myVersionContents;
	}

	/**
	 * 解析所有版本SQL脚本。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected List<VersionContent> resolveAllSqlVersionContents() throws IOException
	{
		Resource resource = getSqlScriptResource();
		long lastModified = resource.lastModified();

		if (this.sqlVersionContents == null || this.sqlVersionContents.getLastModified() < 0
				|| this.sqlVersionContents.getLastModified() != lastModified)
		{
			BufferedReader reader = null;

			try
			{
				reader = IOUtil.getReader(resource.getInputStream(), this.sqlScriptEncoding);
				List<VersionContent> svcs = resolveVersionContents(reader, null, null, false, true);

				this.sqlVersionContents = new SqlVersionContents(svcs, lastModified);
			}
			finally
			{
				IOUtil.close(reader);
			}
		}

		return this.sqlVersionContents.getVersionContents();
	}

	/**
	 * 获取SQL脚本资源。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected Resource getSqlScriptResource() throws IOException
	{
		return this.resourceLoader.getResource(this.sqlScriptLocation);
	}

	@Override
	protected void handleVersionContentLine(VersionContent versionContent, List<String> contents, StringBuilder cache,
			String line)
	{
		line = line.trim();

		// 空行作为SQL语句的分隔符
		if (line.isEmpty())
		{
			String sql = cache.toString().trim();

			if (!sql.isEmpty())
			{
				contents.add(deleteTailSemicolon(sql));

				cache.delete(0, cache.length());
			}
		}
		else
		{
			if (cache.length() > 0)
				cache.append(LINE_SEPARATOR);

			cache.append(line);
		}
	}

	@Override
	protected void finishVersionContent(VersionContent versionContent, List<String> contents, StringBuilder cache)
	{
		if (cache.length() > 0)
		{
			String sql = cache.toString().trim();

			if (!sql.isEmpty())
			{
				contents.add(deleteTailSemicolon(sql));
			}
		}
	}

	@Override
	protected boolean isVersionLine(String line)
	{
		return line.startsWith(VERSION_LINE_PREFIX);
	}

	@Override
	protected Version resolveVersion(String line)
	{
		return resolveVersion(line, VERSION_LINE_PREFIX, VERSION_LINE_SUFFIX);
	}

	/**
	 * 删除SQL语句末尾的分号。
	 * 
	 * @param sql
	 * @return
	 */
	protected String deleteTailSemicolon(String sql)
	{
		if (sql.endsWith(";"))
			sql = sql.substring(0, sql.length() - 1);

		return sql;
	}

	protected static class SqlVersionContents
	{
		private final List<VersionContent> versionContents;

		private final long lastModified;

		public SqlVersionContents(List<VersionContent> versionContents, long lastModified)
		{
			super();
			this.versionContents = Collections.unmodifiableList(versionContents);
			this.lastModified = lastModified;
		}

		public List<VersionContent> getVersionContents()
		{
			return versionContents;
		}

		public long getLastModified()
		{
			return lastModified;
		}
	}
}
