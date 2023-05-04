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

package org.datagear.web.util;

import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.web.config.ApplicationPropertiesConfigSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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

	private static final String COMMAND_DATAS = "/d ";

	private static final String COMMAND_TABLES_UPPER = "/T";

	private static final String COMMAND_COLUMNS_UPPER = "/C ";

	private static final String COMMAND_DATAS_UPPER = "/D ";

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
			applicationContext = new AnnotationConfigApplicationContext(DerbySqlClientPropertiesConfig.class,
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
			println(COMMAND_TABLES_UPPER);
			println();
			println("Print [" + COMMAND_COLUMNS + "<table-name>] for listing table columns, example:");
			println(COMMAND_COLUMNS + "DATAGEAR_VERSION");
			println(COMMAND_COLUMNS_UPPER + "DATAGEAR_VERSION");
			println();
			println("Print [" + COMMAND_DATAS + "<table-name>] for listing table data, example:");
			println(COMMAND_DATAS + "DATAGEAR_VERSION");
			println(COMMAND_DATAS_UPPER + "DATAGEAR_VERSION");
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
				else if (input.equals(COMMAND_TABLES) || input.equals(COMMAND_TABLES_UPPER))
				{
					executeSql(cn, "SELECT * FROM SYS.SYSTABLES");
				}
				else if (input.startsWith(COMMAND_COLUMNS) || input.startsWith(COMMAND_COLUMNS_UPPER))
				{
					String tableName = input.substring(COMMAND_COLUMNS.length()).trim();

					String sql = "SELECT C.* FROM SYS.SYSCOLUMNS C, SYS.SYSTABLES T "
							+ " WHERE C.REFERENCEID = T.TABLEID AND T.TABLENAME='" + tableName
							+ "' ORDER BY C.COLUMNNUMBER ASC";

					executeSql(cn, sql);
				}
				else if (input.startsWith(COMMAND_DATAS) || input.startsWith(COMMAND_DATAS_UPPER))
				{
					String tableName = input.substring(COMMAND_DATAS.length()).trim();

					String sql = "SELECT * FROM " + tableName;

					executeSql(cn, sql);
				}
				else if (input.startsWith("@"))
				{
					String filePath = input.substring(1);

					println("start execute sql scripts in file :" + filePath);

					Reader reader = null;

					try
					{
						reader = IOUtil.getReader(FileUtil.getFile(filePath), IOUtil.CHARSET_UTF_8);
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
	@PropertySource(value = ApplicationPropertiesConfigSupport.PROPERTY_SOURCE_PATH, encoding = IOUtil.CHARSET_UTF_8)
	public static class DerbySqlClientPropertiesConfig extends ApplicationPropertiesConfigSupport
	{
		public DerbySqlClientPropertiesConfig()
		{
			super();
		}
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
