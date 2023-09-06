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

package org.datagear.meta.resolver;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.DefaultConnectionSource;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.connection.DriverEntityManagerException;
import org.datagear.connection.DriverLibraryInfo;
import org.datagear.connection.PathDriverFactory;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * {@linkplain DatabaseMetaData}信息打印类。
 * 
 * @author datagear@163.com
 *
 */
public class DatabaseMetaDataPrinter extends AbstractDevotedDBMetaResolver
{
	protected static final List<ActionInfo> ACTION_INFOS = new ArrayList<>();

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

	private ConnectionSource connectionSource;

	private String url;

	private String user;

	private String password;

	public DatabaseMetaDataPrinter()
	{
		super();
	}

	public DatabaseMetaDataPrinter(ConnectionSource connectionSource, String url, String user, String password)
	{
		super();
		this.connectionSource = connectionSource;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
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

			String[] tableTypes = getTableTypes(cn, metaData);

			ResultSet rs = getTableResulSet(cn, metaData, catalog, schema, null, tableTypes);

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

			ResultSet rs = getPrimaryKeyResulSet(cn, metaData, catalog, schema, table);

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

			ResultSet rs = getColumnResulSet(cn, metaData, catalog, schema, table);

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

			ResultSet rs = getImportKeyResulSet(cn, metaData, catalog, schema, table);

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

			ResultSet rs = getUniqueKeyResulSet(cn, metaData, catalog, schema, table);

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
			schema = cn.getSchema();

		return schema;
	}

	protected Connection getConnection() throws Exception
	{
		ConnectionOption connectionOption = ConnectionOption.valueOf(this.url, this.user, this.password);
		return this.connectionSource.getConnection(connectionOption);
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

			print(getColumnName(rsMeta, i));
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
		File driverPath = null;
		String driverClassName = null;
		String url = null;
		String user = null;
		String password = null;

		Scanner scanner = new Scanner(System.in);

		while (driverPath == null || !driverPath.exists())
		{
			println("Please input the driver path :");
			String path = scanner.nextLine();
			driverPath = FileUtil.getFile(path);
		}

		println("Please input the driver class name :");
		driverClassName = scanner.nextLine();

		println("Please input the jdbc connection url :");
		url = scanner.nextLine();

		println("Please input the jdbc connection user :");
		user = scanner.nextLine();

		println("Please input the jdbc connection password :");
		password = scanner.nextLine();

		DriverEntity driverEntity = new DriverEntity("driverEntity", driverClassName);
		SingleDriverEntityManager driverEntityManager = new SingleDriverEntityManager(driverPath, driverEntity);
		ConnectionSource connectionSource = new DefaultConnectionSource(driverEntityManager);

		DatabaseMetaDataPrinter printer = new DatabaseMetaDataPrinter(connectionSource, url, user, password);

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

		println("Bye!");

		IOUtil.close(scanner);
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
				"No method named [" + methodName + "] in class [" + DatabaseMetaData.class.getName() + "]");
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

	protected static class SingleDriverEntityManager implements DriverEntityManager
	{
		private File driverPath;

		private DriverEntity driverEntity;

		private PathDriverFactory _pathDriverFactory;

		public SingleDriverEntityManager()
		{
			super();
		}

		public SingleDriverEntityManager(File driverPath, DriverEntity driverEntity)
		{
			super();
			this.driverPath = driverPath;
			this.driverEntity = driverEntity;
			this._pathDriverFactory = new PathDriverFactory(this.driverPath);
			this._pathDriverFactory.init();
		}

		public File getDriverPath()
		{
			return driverPath;
		}

		public void setDriverPath(File driverPath)
		{
			this.driverPath = driverPath;
			this._pathDriverFactory = new PathDriverFactory(this.driverPath);
			this._pathDriverFactory.init();
		}

		public DriverEntity getDriverEntity()
		{
			return driverEntity;
		}

		public void setDriverEntity(DriverEntity driverEntity)
		{
			this.driverEntity = driverEntity;
		}

		@Override
		public void add(DriverEntity... driverEntities) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean[] update(DriverEntity... driverEntities) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public DriverEntity get(String id) throws DriverEntityManagerException
		{
			if (!this.driverEntity.getId().equals(id))
				throw new IllegalArgumentException();

			return this.driverEntity;
		}

		@Override
		public void delete(String... ids) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DriverEntity> getAll() throws DriverEntityManagerException
		{
			return Arrays.asList(this.driverEntity);
		}

		@Override
		public long getLastModified() throws DriverEntityManagerException
		{
			return 0;
		}

		@Override
		public long getLastModified(DriverEntity driverEntity) throws DriverEntityManagerException
		{
			return 0;
		}

		@Override
		public void addDriverLibrary(DriverEntity driverEntity, String libraryName, InputStream in)
				throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean[] deleteDriverLibrary(DriverEntity driverEntity, String... libraryName)
				throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean deleteDriverLibrary(DriverEntity driverEntity) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream getDriverLibrary(DriverEntity driverEntity, String libraryName)
				throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void readDriverLibrary(DriverEntity driverEntity, String libraryName, OutputStream out)
				throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DriverLibraryInfo> getDriverLibraryInfos(DriverEntity driverEntity)
				throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Driver getDriver(DriverEntity driverEntity) throws DriverEntityManagerException
		{
			return this._pathDriverFactory.getDriver(driverEntity.getDriverClassName());
		}

		@Override
		public void release(DriverEntity driverEntity) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void releaseAll()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void exportToZip(ZipOutputStream out, String... ids) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();

		}

		@Override
		public void importFromZip(ZipInputStream in, String... ids) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DriverEntity> readDriverEntitiesFromZip(ZipInputStream in) throws DriverEntityManagerException
		{
			throw new UnsupportedOperationException();
		}
	}
}
