/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.web.config.PropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Derby数据库SQL客户端。
 * 
 * @author datagear@163.com
 *
 */
public class DerbySqlClient extends JdbcSupport
{
	private static final String COMMAND_TABLES = "/t";

	private static final String COMMAND_COLUMNS = "/c ";

	public DerbySqlClient()
	{
		super();
	}

	public void run() throws Exception
	{
		AnnotationConfigApplicationContext applicationContext = null;
		Connection cn = null;

		try
		{
			applicationContext = new AnnotationConfigApplicationContext(PropertiesConfig.class,
					DerbySqlClientDataSourceConfig.class);

			DataSource dataSource = applicationContext.getBean(DataSource.class);
			cn = dataSource.getConnection();

			println("*****************************************");
			println("Derby SQL client");
			println("URL :" + JdbcUtil.getURLIfSupports(cn));
			println("*****************************************");
			println();
			println("*****************************************");
			println("Print [SQL] for executing single sql, example:");
			println("SELECT * FROM DATAGEAR_VERSION");
			println();
			println("Print [@<sql-file-path>] for executing UTF-8 sql scripts, example:");
			println("@/home/tmp.sql");
			println();
			println("Print [" + COMMAND_TABLES + "] for listing tables, example:");
			println(COMMAND_TABLES);
			println();
			println("Print [" + COMMAND_COLUMNS + "<table-name>] for listing table columns, example:");
			println(COMMAND_COLUMNS + "DATAGEAR_VERSION");
			println();
			println("Print [exit] for exit, example:");
			println("exit");
			println("*****************************************");

			Scanner scanner = new Scanner(System.in);

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().trim();

				if (input.isEmpty())
					;
				else if ("exit".equalsIgnoreCase(input))
				{
					println("Bye!");
					scanner.close();
					System.exit(0);
				}
				else if (input.equals(COMMAND_TABLES))
				{
					executeSql(cn, "SELECT * FROM SYS.SYSTABLES");
				}
				else if (input.startsWith(COMMAND_COLUMNS))
				{
					String tableName = input.substring(COMMAND_COLUMNS.length());

					String sql = "SELECT C.* FROM SYS.SYSCOLUMNS C, SYS.SYSTABLES T "
							+ " WHERE C.REFERENCEID = T.TABLEID AND T.TABLENAME='" + tableName
							+ "' ORDER BY C.COLUMNNUMBER ASC";

					executeSql(cn, sql);
				}
				else if (input.startsWith("@"))
				{
					String filePath = input.substring(1);

					println("start execute sql scripts in file :" + filePath);

					Reader reader = null;

					try
					{
						reader = IOUtil.getReader(new File(filePath), "UTF-8");
						SqlScriptParser sqlScriptParser = new SqlScriptParser(reader);
						SqlStatement sql = null;

						while ((sql = sqlScriptParser.parseNext()) != null)
						{
							println(sql.getSql());
							executeSql(cn, sql.getSql());
						}
					}
					catch (Throwable t)
					{
						t.printStackTrace();
					}
					finally
					{
						IOUtil.close(reader);
					}

					println("finish execute sql scripts in file :" + filePath);
				}
				else
				{
					executeSql(cn, input);
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(applicationContext);
		}
	}

	protected void executeSql(Connection cn, String sql)
	{
		if (sql.endsWith(";"))
			sql = sql.substring(0, sql.length() - 1);

		Statement st = null;

		try
		{
			st = cn.createStatement();

			boolean query = st.execute(sql);

			println("-----------------------------------------");
			if (!query)
			{
				println(st.getUpdateCount() + " records affected.");
			}
			else
			{
				ResultSet rs = st.getResultSet();

				ResultSetMetaData metaData = rs.getMetaData();
				int columnCount = metaData.getColumnCount();

				for (int i = 1; i <= columnCount; i++)
				{
					String columnLabel = getColumnName(metaData, i);

					if (i > 1)
						print(", ");

					print(columnLabel);
				}

				println();

				while (rs.next())
				{
					for (int i = 1; i <= columnCount; i++)
					{
						Object cv = rs.getObject(i);

						if (i > 1)
							print(", ");

						print(cv);
					}

					println();
				}
			}
			println("-----------------------------------------");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}
	}

	protected void print(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.print(str);
	}

	protected void println(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.println(str);
	}

	protected void println()
	{
		System.out.println();
	}

	public static void main(String[] args) throws Exception
	{
		new DerbySqlClient().run();
	}

	@Configuration
	public static class DerbySqlClientDataSourceConfig
	{
		private Environment environment;

		@Autowired
		public DerbySqlClientDataSourceConfig(Environment environment)
		{
			this.environment = environment;
		}

		public Environment getEnvironment()
		{
			return environment;
		}

		public void setEnvironment(Environment environment)
		{
			this.environment = environment;
		}

		@Bean
		public DataSource dataSource()
		{
			BasicDataSource bean = new BasicDataSource();

			bean.setDriverClassName(this.environment.getProperty("datasource.driverClassName"));
			bean.setUrl(this.environment.getProperty("datasource.url"));
			bean.setUsername(this.environment.getProperty("datasource.username"));
			bean.setPassword(this.environment.getProperty("datasource.password"));

			return bean;
		}
	}
}
