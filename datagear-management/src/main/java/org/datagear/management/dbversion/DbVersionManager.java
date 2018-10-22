package org.datagear.management.dbversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.datagear.connection.JdbcUtil;
import org.datagear.management.Version;
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
public class DbVersionManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DbVersionManager.class);

	/** 脚本资源文件路径 */
	public static final String SQL_SCRIPT_RESOURCE = "org/datagear/management/ddl/datagear.sql";

	/** 脚本资源文件编码 */
	public static final String DEFAULT_SQL_SCRIPT_ENCODING = "UTF-8";

	/** 数据库SQL文件中版本号注释开头标识 */
	public static final String DEFAULT_VERSION_LINE_PREFIX = "--version[";

	/** 数据库SQL文件中版本号注释结尾标识 */
	public static final String DEFAULT_VERSION_LINE_SUFFIX = "]";

	public static final String DEFAULT_VERSION_TABLE_NAME = "DATAGEAR_VERSION";

	private String sqlScriptLocation = SQL_SCRIPT_RESOURCE;

	private String sqlScriptEncoding = DEFAULT_SQL_SCRIPT_ENCODING;

	private String versionLinePrefix = DEFAULT_VERSION_LINE_PREFIX;

	private String versionLineSuffix = DEFAULT_VERSION_LINE_SUFFIX;

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

	public String getVersionLinePrefix()
	{
		return versionLinePrefix;
	}

	public void setVersionLinePrefix(String versionLinePrefix)
	{
		this.versionLinePrefix = versionLinePrefix;
	}

	public String getVersionLineSuffix()
	{
		return versionLineSuffix;
	}

	public void setVersionLineSuffix(String versionLineSuffix)
	{
		this.versionLineSuffix = versionLineSuffix;
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

		UpgradeSqls upgradeSqls = extractUpgradeSqls(from);

		if (upgradeSqls == null)
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("No upgrade sqls for verion [" + from + "], it is already the latest");

			target = from;
		}
		else
		{
			target = upgradeSqls.getTargetVersion();

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Got upgrade sqls for verion from [" + from + "] to [" + target + "], line from ["
						+ upgradeSqls.getStartLine() + "] in [" + this.sqlScriptLocation + "]");

			executeSqls(cn, upgradeSqls.getSqls());
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
	 * 提取当前版本的升级SQL脚本。
	 * <p>
	 * 如果已是最新版本，返回{@code null}。
	 * </p>
	 * 
	 * @param current
	 * @return
	 */
	protected UpgradeSqls extractUpgradeSqls(Version current) throws IOException
	{
		List<String> sqls = new ArrayList<String>();

		Version target = null;

		BufferedReader reader = getSqlScriptBufferedReader();

		int readStart = -1;

		int lineNumber = 1;

		try
		{
			boolean canWrite = (current == null || Version.ZERO_VERSION.equals(current));
			if (canWrite)
				readStart = lineNumber;

			StringBuilder sql = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (canWrite)
				{
					String trimLine = line.trim();

					if (isVersionLine(trimLine))
					{
						target = extractVersion(line);
					}
					else if (isCommentLine(trimLine))
					{
						// 忽略注释行
					}
					else if (isEmptyLine(trimLine))
					{
						// 空行作为语句分隔符
						String trimSql = sql.toString().trim();

						if (!trimSql.isEmpty())
							sqls.add(postProcessSql(trimSql));

						sql.delete(0, sql.length());
					}
					else
					{
						if (sql.length() > 0)
							sql.append("\r\n");

						sql.append(line);
					}
				}
				else
				{
					if (isVersionLine(line))
					{
						Version myVersion = extractVersion(line);

						if (myVersion.isHigherThan(current))
						{
							canWrite = true;
							readStart = lineNumber;
						}
					}
				}

				lineNumber++;
			}

			String trimSql = sql.toString().trim();

			if (!trimSql.isEmpty())
				sqls.add(postProcessSql(trimSql));
		}
		finally
		{
			reader.close();
		}

		if (target == null)
			return null;
		else
			return new UpgradeSqls(sqls, target, readStart);
	}

	/**
	 * 后置处理SQL语句。
	 * 
	 * @param trimSql
	 * @return
	 */
	protected String postProcessSql(String trimSql)
	{
		if (trimSql.endsWith(";"))
			trimSql = trimSql.substring(0, trimSql.length() - 1);

		return trimSql;
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

	/**
	 * 是否空行。
	 * 
	 * @param trimLine
	 * @return
	 */
	protected boolean isEmptyLine(String trimLine)
	{
		return trimLine.isEmpty();
	}

	/**
	 * 是否是注释行。
	 * 
	 * @param trimLine
	 * @return
	 */
	protected boolean isCommentLine(String trimLine)
	{
		return trimLine.startsWith("--");
	}

	/**
	 * 判断给定行是否是版本标识行。
	 * 
	 * @param trimLine
	 * @return
	 */
	protected boolean isVersionLine(String trimLine)
	{
		return trimLine.startsWith(this.versionLinePrefix);
	}

	/**
	 * 从字符串中解析版本号。
	 * 
	 * @param line
	 * @return
	 */
	protected Version extractVersion(String line)
	{
		int start = line.indexOf(this.versionLinePrefix);

		if (start < 0)
			throw new IllegalArgumentException("[" + line + "] is not version line");

		start = start + this.versionLinePrefix.length();
		int end = line.indexOf(this.versionLineSuffix, start);

		String version = line.substring(start, end);

		return Version.valueOf(version);
	}

	/**
	 * 版本升级SQL脚本。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class UpgradeSqls
	{
		/** 脚本列表 */
		private List<String> sqls;

		/** 脚本的目标版本 */
		private Version targetVersion;

		/** 起始行 */
		private int startLine;

		public UpgradeSqls()
		{
			super();
		}

		public UpgradeSqls(List<String> sqls, Version targetVersion)
		{
			super();
			this.sqls = sqls;
			this.targetVersion = targetVersion;
		}

		public UpgradeSqls(List<String> sqls, Version targetVersion, int startLine)
		{
			super();
			this.sqls = sqls;
			this.targetVersion = targetVersion;
			this.startLine = startLine;
		}

		public List<String> getSqls()
		{
			return sqls;
		}

		public void setSqls(List<String> sqls)
		{
			this.sqls = sqls;
		}

		public Version getTargetVersion()
		{
			return targetVersion;
		}

		public void setTargetVersion(Version targetVersion)
		{
			this.targetVersion = targetVersion;
		}

		public int getStartLine()
		{
			return startLine;
		}

		public void setStartLine(int startLine)
		{
			this.startLine = startLine;
		}
	}
}
