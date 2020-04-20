/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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

import org.datagear.util.IOUtil;
import org.datagear.util.JdbcSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Derby数据库SQL客户端。
 * 
 * @author datagear@163.com
 *
 */
public class DerbySqlClient extends JdbcSupport
{
	public DerbySqlClient()
	{
		super();
	}

	public void run() throws Exception
	{
		ClassPathXmlApplicationContext applicationContext = null;
		Connection cn = null;

		try
		{
			applicationContext = new ClassPathXmlApplicationContext(
					"classpath:org/datagear/web/datagear-propertyConfigurer.xml",
					"classpath:org/datagear/web/datagear-dataSource.xml");

			DataSource dataSource = applicationContext.getBean(DataSource.class);
			cn = dataSource.getConnection();

			println("*****************************************");
			println("Derby SQL client");
			println("URL :" + JdbcUtil.getURLIfSupports(cn));
			println("*****************************************");
			println();
			println("Print SQL for executing single sql");
			println("Print @<sql-file-path> for executing sql scripts");
			println("Print [exist] for exist");

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
}
