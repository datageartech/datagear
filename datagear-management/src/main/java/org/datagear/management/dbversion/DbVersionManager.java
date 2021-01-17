package org.datagear.management.dbversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	public static final String SQL_SCRIPT_RESOURCE = "org/datagear/management/ddl/datagear.sql";

	/** 数据库SQL文件中版本号注释开头标识 */
	public static final String VERSION_LINE_PREFIX = "--version[";

	/** 数据库SQL文件中版本号注释结尾标识 */
	public static final String VERSION_LINE_SUFFIX = "]";

	public static final String DEFAULT_VERSION_TABLE_NAME = "DATAGEAR_VERSION";

	private String sqlScriptLocation = SQL_SCRIPT_RESOURCE;

	private String sqlScriptEncoding = ENCODING_UTF8;

	private String versionTableName = DEFAULT_VERSION_TABLE_NAME;

	private DataSource dataSource;

	public DbVersionManager()
	{
		super();
	}

	public DbVersionManager(DataSource dataSource)
	{
		super();
		this.dataSource = dataSource;
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

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	/**
	 * 获取当前版本。
	 * 
	 * @return
	 * @throws DbVersionManagerException
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
	 * 升级至最新版本。
	 * 
	 * @return
	 * @throws DbVersionManagerException
	 */
	public Version upgrade() throws DbVersionManagerException
	{
		Connection cn = null;
		try
		{
			cn = this.dataSource.getConnection();

			return upgrade(cn);
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
	 * 执行升级。
	 * 
	 * @param cn
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	protected Version upgrade(Connection cn) throws SQLException, IOException
	{
		Version current = null;

		try
		{
			current = getCurrentVersion(cn);
		}
		catch (SQLException e)
		{
			current = Version.ZERO_VERSION;

			if (LOGGER.isWarnEnabled())
				LOGGER.warn("The database may not be initialized, the current version will be set to [" + current
						+ "] for full upgrade", e);
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Start upgrade database version from [" + current + "] to the latest");

		Version target = updateSchema(cn, current);
		target = Version.valueOf(Global.VERSION);

		if (target.isHigherThan(current))
			updateVersion(cn, target);

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Finish upgrade database version from [" + current + "] to the latest verion [" + target + "]");

		return target;
	}

	/**
	 * 获取当前版本。
	 * 
	 * @param cn
	 * @return
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
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	protected Version updateSchema(Connection cn, Version from) throws SQLException, IOException
	{
		Version target = null;

		List<VersionContent> versionContents = resolveUpgradeSqlVersionContents(from);

		if (versionContents == null || versionContents.isEmpty())
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("No upgrade sqls for verion [" + from + "], it is already the latest");

			target = from;
		}
		else
		{
			for (VersionContent versionContent : versionContents)
			{
				target = versionContent.getVersion();

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Got upgrade sqls for verion from [" + from + "] to [" + target + "] (line "
							+ versionContent.getVersionStartLine() + " - " + versionContent.getVersionEndLine()
							+ ") in [" + this.sqlScriptLocation + "]");

				executeSqls(cn, versionContent.getContents());
			}
		}

		return target;
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
					LOGGER.info("Start executing sql : " + sql);

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
	 * @param current
	 * @return
	 * @throws IOException
	 */
	protected List<VersionContent> resolveUpgradeSqlVersionContents(Version current) throws IOException
	{
		BufferedReader reader = null;

		try
		{
			reader = getSqlScriptBufferedReader();

			return resolveVersionContents(reader, current, null, false, true);
		}
		finally
		{
			IOUtil.close(reader);
		}
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
		int start = line.indexOf(VERSION_LINE_PREFIX);

		if (start < 0)
			throw new IllegalArgumentException("[" + line + "] is not version line");

		start = start + VERSION_LINE_PREFIX.length();
		int end = line.indexOf(VERSION_LINE_SUFFIX, start);

		String version = line.substring(start, end);

		return Version.valueOf(version);
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

	/**
	 * 获取SQL脚本输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected BufferedReader getSqlScriptBufferedReader() throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				DbVersionManager.class.getClassLoader().getResourceAsStream(this.sqlScriptLocation),
				this.sqlScriptEncoding));

		return reader;
	}
}
