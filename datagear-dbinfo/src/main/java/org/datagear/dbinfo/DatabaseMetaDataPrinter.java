/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * {@linkplain DatabaseMetaData}信息打印类。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseMetaDataPrinter extends AbstractDevotedDatabaseInfoResolver
{
	protected static final List<ActionInfo> ACTION_INFOS = new ArrayList<ActionInfo>();

	public static void registerActionInfo(String actionMethodName, String... actionMethodArgNames)
	{
		ActionInfo actionInfo = new ActionInfo(actionMethodName, actionMethodArgNames);

		ACTION_INFOS.add(actionInfo);
	}

	static
	{
		registerActionInfo("print_getCatalog");
		registerActionInfo("print_getSchema");
		registerActionInfo("print_getCatalogs");
		registerActionInfo("print_getSchemas");
		registerActionInfo("print_getUserName");
		registerActionInfo("print_getTableTypes");
		registerActionInfo("print_getTables", "catalog", "schema");
		registerActionInfo("print_getPrimaryKeys", "catalog", "schema", "table");
		registerActionInfo("print_getColumn", "catalog", "schema", "table");
		registerActionInfo("print_getExportedKeys", "catalog", "schema", "table");
		registerActionInfo("print_getImportedKeys", "catalog", "schema", "table");
		registerActionInfo("print_getUniqueKeys", "catalog", "schema", "table");
	}

	private String driverClassName;

	private String url;

	private String user;

	private String password;

	public DatabaseMetaDataPrinter()
	{
		super();
	}

	public DatabaseMetaDataPrinter(String driverClassName, String url, String user, String password)
	{
		super();
		this.driverClassName = driverClassName;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public String getDriverClassName()
	{
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName)
	{
		this.driverClassName = driverClassName;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public boolean supports(Connection cn)
	{
		return true;
	}

	public void print_getCatalog() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			printlnData("getCatalog", cn.getCatalog());
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getSchema() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			String schema = getSchema(cn, cn.getMetaData());
			printlnData("getSchema", schema);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getCatalogs() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getCatalogs();

			printlnData("getCatalogs", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getSchemas() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getSchemas();

			printlnData("getSchemas", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getUserName() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			printlnData("getUserName", metaData.getUserName());
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getTableTypes() throws Exception
	{
		Connection cn = getConnection();

		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getTableTypes();

			printlnData("getTableTypes", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getTables(String catalog, String schema) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getTables(catalog, schema, "%", new String[] { "TABLE" });

			printlnData("getTables", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getPrimaryKeys(String catalog, String schema, String table) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getPrimaryKeys(catalog, schema, table);

			printlnData("getPrimaryKeys", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getColumn(String catalog, String schema, String table) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getColumns(catalog, schema, table, "%");

			printlnData("getColumns", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getExportedKeys(String catalog, String schema, String table) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getExportedKeys(catalog, schema, table);

			printlnData("getExportedKeys", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getImportedKeys(String catalog, String schema, String table) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getImportedKeys(catalog, schema, table);

			printlnData("getImportedKeys", rs);
		}
		finally
		{
			cn.close();
		}
	}

	public void print_getUniqueKeys(String catalog, String schema, String table) throws Exception
	{
		Connection cn = getConnection();

		try
		{
			catalog = getCatalog(cn, catalog);
			schema = getSchema(cn, schema);

			DatabaseMetaData metaData = cn.getMetaData();

			ResultSet rs = metaData.getIndexInfo(catalog, schema, table, true, false);

			printlnData("getUniqueKeys", rs);
		}
		finally
		{
			cn.close();
		}
	}

	protected String getCatalog(Connection cn, String catalog) throws Exception
	{
		if (catalog == null || catalog.isEmpty())
			catalog = cn.getCatalog();

		return catalog;
	}

	protected String getSchema(Connection cn, String schema) throws Exception
	{
		if (schema == null || schema.isEmpty())
			schema = getSchema(cn, cn.getMetaData());

		return schema;
	}

	protected Connection getConnection() throws Exception
	{
		return DriverManager.getConnection(this.url, this.user, this.password);
	}

	protected void printlnData(String label, Object data) throws Exception
	{
		println("");
		println("========================================");
		println("==" + label);
		println("========================================");

		if (data instanceof ResultSet)
			printlnResultSet(label, (ResultSet) data);
		else
			println(data);

		println("========================================");
		println("==" + label);
		println("========================================");
	}

	/**
	 * 打印{@linkplain ResultSet}。
	 * 
	 * @param label
	 * @param rs
	 * @throws SQLException
	 */
	protected void printlnResultSet(String label, ResultSet rs) throws SQLException
	{
		ResultSetMetaData rsMeta = rs.getMetaData();
		int colCount = rsMeta.getColumnCount();

		for (int i = 1; i <= colCount; i++)
		{
			if (i > 1)
				print(", ");

			print(rsMeta.getColumnLabel(i));
		}
		println("");
		println("------------------------------");

		int count = 0;
		while (rs.next())
		{
			for (int i = 1; i <= colCount; i++)
			{
				if (i > 1)
					print(", ");

				print(rs.getObject(i));
			}
			println("");

			count++;
		}
		println("------------------------------");
		println("Total : " + count);
	}

	protected static void println(Object o)
	{
		System.out.println((o == null ? "null" : o.toString()));
	}

	protected static void print(Object o)
	{
		System.out.print((o == null ? "null" : o.toString()));
	}

	public static void main(String[] args) throws Exception
	{
		String driverClassName = null;
		String url = null;
		String user = null;
		String password = null;

		Scanner scanner = new Scanner(System.in);

		while (driverClassName == null)
		{
			println("Please input the jdbc driver class name :");
			driverClassName = scanner.nextLine();

			try
			{
				Class.forName(driverClassName);
			}
			catch (Exception e)
			{
				println(e);
				driverClassName = null;
			}
		}

		println("Please input the jdbc connection url :");
		url = scanner.nextLine();

		println("Please input the jdbc connection user :");
		user = scanner.nextLine();

		println("Please input the jdbc connection password :");
		password = scanner.nextLine();

		DatabaseMetaDataPrinter printer = new DatabaseMetaDataPrinter(driverClassName, url, user, password);

		String actionIndexStr = null;

		while (!"exit".equalsIgnoreCase(actionIndexStr))
		{
			println("");
			println("=======================");
			for (int i = 0; i < ACTION_INFOS.size(); i++)
			{
				ActionInfo actionInfo = ACTION_INFOS.get(i);

				println(i + " : " + actionInfo.getActionMethodName());
			}
			println("exit : exit");
			println("=======================");

			println("");
			println("Please input the action index:");
			actionIndexStr = scanner.nextLine();

			int actionIndex = -1;

			try
			{
				actionIndex = Integer.parseInt(actionIndexStr);
			}
			catch (Exception e)
			{
				actionIndex = -1;
			}

			if (actionIndex < 0 || actionIndex >= ACTION_INFOS.size())
				continue;

			ActionInfo actionInfo = ACTION_INFOS.get(actionIndex);
			String[] actionMethodArgNames = actionInfo.getActionMethodArgNames();

			String[] methodArgs = new String[actionMethodArgNames.length];

			for (int i = 0; i < actionMethodArgNames.length; i++)
			{
				println("Please input [" + actionInfo.getActionMethodName() + "] method arg [" + actionMethodArgNames[i]
						+ "] value ([null] for null value) :");
				methodArgs[i] = scanner.nextLine();

				if (methodArgs[i].equalsIgnoreCase("null"))
					methodArgs[i] = null;
			}

			try
			{
				Method method = getMethodByName(actionInfo.getActionMethodName());
				method.invoke(printer, (Object[]) methodArgs);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	protected static Method getMethodByName(String methodName)
	{
		Method[] methods = DatabaseMetaDataPrinter.class.getMethods();

		for (Method method : methods)
		{
			if (method.getName().equals(methodName))
				return method;
		}

		throw new IllegalArgumentException(
				"No method named [" + methodName + "] in class [" + DatabaseMetaDataPrinter.class.getName() + "]");
	}

	protected static class ActionInfo
	{
		private String actionMethodName;

		private String[] actionMethodArgNames;

		public ActionInfo()
		{
			super();
		}

		public ActionInfo(String actionMethodName, String[] actionMethodArgNames)
		{
			super();
			this.actionMethodName = actionMethodName;
			this.actionMethodArgNames = actionMethodArgNames;
		}

		public String getActionMethodName()
		{
			return actionMethodName;
		}

		public void setActionMethodName(String actionMethodName)
		{
			this.actionMethodName = actionMethodName;
		}

		public String[] getActionMethodArgNames()
		{
			return actionMethodArgNames;
		}

		public void setActionMethodArgNames(String[] actionMethodArgNames)
		{
			this.actionMethodArgNames = actionMethodArgNames;
		}
	}
}
