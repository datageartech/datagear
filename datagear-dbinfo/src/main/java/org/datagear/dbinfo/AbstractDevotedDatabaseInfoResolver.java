/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象{@linkplain DevotedDatabaseInfoResolver}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDevotedDatabaseInfoResolver implements DevotedDatabaseInfoResolver
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDevotedDatabaseInfoResolver.class);

	protected static final String[] TABLE_TABLE_TYPES = new String[] { TableInfoResultSetSpec.TABLE_TYPE_TABLE,
			TableInfoResultSetSpec.TABLE_TYPE_VIEW };

	public AbstractDevotedDatabaseInfoResolver()
	{
		super();
	}

	@Override
	public DatabaseInfo getDatabaseInfo(Connection cn) throws DatabaseInfoResolverException
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			DatabaseInfo databaseInfo = new DatabaseInfo();

			databaseInfo.setCatalog(cn.getCatalog());
			databaseInfo.setSchema(getSchema(cn, metaData));

			databaseInfo.setUrl(metaData.getURL());
			databaseInfo.setUser(metaData.getUserName());
			databaseInfo.setProductName(metaData.getDatabaseProductName());
			databaseInfo.setProductVersion(metaData.getDatabaseProductVersion());
			databaseInfo.setDriverName(metaData.getDriverName());
			databaseInfo.setDriverVersion(metaData.getDriverVersion());

			return databaseInfo;
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	@Override
	public EntireTableInfo getEntireTableInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		EntireTableInfo entireTableInfo = new EntireTableInfo();

		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		TableInfo tableInfo = getTableInfo(cn, metaData, schema, tableName);
		ColumnInfo[] columnInfos = getColumnInfos(cn, metaData, schema, tableName);
		String[] primaryKeyColumnNames = getPrimaryKeyColumnNames(cn, metaData, schema, tableName);
		String[][] uniqueKeyColumnNames = getUniqueKeyColumnNames(cn, metaData, schema, tableName);
		ImportedKeyInfo[] importedKeyInfos = getImportedKeyInfos(cn, metaData, schema, tableName);
		ExportedKeyInfo[] exportedKeyInfos = getExportedKeyInfos(cn, metaData, schema, tableName);

		entireTableInfo.setTableInfo(tableInfo);
		entireTableInfo.setColumnInfos(columnInfos);
		entireTableInfo.setPrimaryKeyColumnNames(primaryKeyColumnNames);
		entireTableInfo.setUniqueKeyColumnNames(uniqueKeyColumnNames);
		entireTableInfo.setImportedKeyInfos(importedKeyInfos);
		entireTableInfo.setExportedKeyInfos(exportedKeyInfos);

		return entireTableInfo;
	}

	@Override
	public TableInfo[] getTableInfos(Connection cn) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getTableInfos(cn, metaData, schema);
	}

	@Override
	public TableInfo getRandomTableInfo(Connection cn) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getRandomTableInfo(cn, metaData, schema);
	}

	@Override
	public TableInfo getTableInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getTableInfo(cn, metaData, schema, tableName);
	}

	@Override
	public ColumnInfo[] getColumnInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getColumnInfos(cn, metaData, schema, tableName);
	}

	@Override
	public ColumnInfo getRandomColumnInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getRandomColumnInfo(cn, metaData, schema, tableName);
	}

	@Override
	public String[] getPrimaryKeyColumnNames(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getPrimaryKeyColumnNames(cn, metaData, schema, tableName);
	}

	@Override
	public String[][] getUniqueKeyColumnNames(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getUniqueKeyColumnNames(cn, metaData, schema, tableName);
	}

	@Override
	public ImportedKeyInfo[] getImportedKeyInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getImportedKeyInfos(cn, metaData, schema, tableName);
	}

	@Override
	public ExportedKeyInfo[] getExportedKeyInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getExportedKeyInfos(cn, metaData, schema, tableName);
	}

	protected TableInfo[] getTableInfos(Connection cn, DatabaseMetaData metaData, String schema)
			throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = this.getTableInfoResulSet(cn, metaData, schema, null, getTableTypes());

			List<TableInfo> tableInfos = getTableInfoResultSetSpec().read(rs);

			TableInfo[] array = tableInfos.toArray(new TableInfo[tableInfos.size()]);

			postProcessTableInfo(array);

			return array;
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected TableInfo getRandomTableInfo(Connection cn, DatabaseMetaData metaData, String schema)
			throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = this.getTableInfoResulSet(cn, metaData, schema, null, TABLE_TABLE_TYPES);

			List<TableInfo> tableInfos = getTableInfoResultSetSpec().read(rs, 1, 1);

			TableInfo[] array = tableInfos.toArray(new TableInfo[tableInfos.size()]);

			postProcessTableInfo(array);

			return (array.length > 0 ? array[0] : null);
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected TableInfo getTableInfo(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getTableInfoResulSet(cn, metaData, schema, tableName, getTableTypes());

			List<TableInfo> tableInfos = getTableInfoResultSetSpec().read(rs);

			if (tableInfos == null || tableInfos.isEmpty())
				throw new TableNotExistsException(tableName, "Table [" + tableName + "] is not exists");

			TableInfo[] array = tableInfos.toArray(new TableInfo[tableInfos.size()]);

			postProcessTableInfo(array);

			return array[0];
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected ColumnInfo[] getColumnInfos(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getColumnInfoResulSet(cn, metaData, schema, tableName);

			List<ColumnInfo> columnInfos = getColumnInfoResultSetSpec().read(rs);

			ColumnInfo[] array = columnInfos.toArray(new ColumnInfo[columnInfos.size()]);

			postProcessColumnInfo(array);

			return array;
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected ColumnInfo getRandomColumnInfo(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getColumnInfoResulSet(cn, metaData, schema, tableName);

			List<ColumnInfo> columnInfos = getColumnInfoResultSetSpec().read(rs, 1, 1);

			ColumnInfo[] array = columnInfos.toArray(new ColumnInfo[columnInfos.size()]);

			postProcessColumnInfo(array);

			return (array.length > 0 ? array[0] : null);
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	@Override
	public ColumnInfo[] getColumnInfos(Connection cn, ResultSetMetaData resultSetMetaData)
			throws DatabaseInfoResolverException
	{
		try
		{
			int columnCount = resultSetMetaData.getColumnCount();

			ColumnInfo[] columnInfos = new ColumnInfo[columnCount];

			for (int i = 1; i <= columnCount; i++)
			{
				ColumnInfo columnInfo = new ColumnInfo();

				String columnName = resultSetMetaData.getColumnLabel(i);
				if (columnName == null || columnName.isEmpty())
					columnName = resultSetMetaData.getColumnName(i);
				columnInfo.setName(columnName);
				columnInfo.setType(resultSetMetaData.getColumnType(i));
				columnInfo.setTypeName(resultSetMetaData.getColumnTypeName(i));
				columnInfo.setSize(resultSetMetaData.getPrecision(i));
				columnInfo.setDecimalDigits(resultSetMetaData.getScale(i));
				columnInfo.setNullable(
						ColumnInfoResultSetSpec.NULLABLE_CONVERTER.convert(resultSetMetaData.isNullable(i)));
				columnInfo.setAutoincrement(resultSetMetaData.isAutoIncrement(i));

				columnInfos[i - 1] = columnInfo;
			}

			return columnInfos;
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	protected String[] getPrimaryKeyColumnNames(Connection cn, DatabaseMetaData metaData, String schema,
			String tableName) throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getPrimaryKeyResulSet(cn, metaData, schema, tableName);

			List<PrimaryKeyInfo> primaryKeyInfos = getPrimaryKeyInfoResultSetSpec().read(rs);

			String[] names = new String[primaryKeyInfos.size()];

			for (int i = 0, len = primaryKeyInfos.size(); i < len; i++)
			{
				names[i] = primaryKeyInfos.get(i).getColumnName();
			}

			return names;
		}
		// 当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常，为了简化处理逻辑，这里直接压制
		catch (SQLNonTransientException e)
		{
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("An String[0] object will be returned for an SQLNonTransientException", e);

			return new String[0];
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected String[][] getUniqueKeyColumnNames(Connection cn, DatabaseMetaData metaData, String schema,
			String tableName) throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		List<String> uniqueKeyNames = new ArrayList<String>();
		List<List<String>> uniqueKeyColumnNames = new ArrayList<List<String>>();

		try
		{
			rs = getUniqueKeyResulSet(cn, metaData, schema, tableName);

			List<UniqueKeyInfo> uniqueKeyInfos = getUniqueKeyInfoResultSetSpec().read(rs);

			for (UniqueKeyInfo uniqueKeyInfo : uniqueKeyInfos)
			{
				String uniqueKeyName = uniqueKeyInfo.getKeyName();
				String columnName = uniqueKeyInfo.getColumnName();

				List<String> uniqueKeyColumnName = null;

				int myIndex = uniqueKeyNames.indexOf(uniqueKeyName);
				if (myIndex < 0)
				{
					uniqueKeyNames.add(uniqueKeyName);

					uniqueKeyColumnName = new ArrayList<String>();
					uniqueKeyColumnNames.add(uniqueKeyColumnName);
				}
				else
					uniqueKeyColumnName = uniqueKeyColumnNames.get(myIndex);

				uniqueKeyColumnName.add(columnName);
			}
		}
		// 当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常，为了简化处理逻辑，这里直接压制
		catch (SQLNonTransientException e)
		{
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("An String[0][0] object will be returned for an SQLNonTransientException", e);

			return new String[0][0];
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}

		int size = uniqueKeyColumnNames.size();

		String[][] uniqueKeyColumnNamesAry = new String[size][];

		for (int i = 0; i < size; i++)
		{
			List<String> myUniqueKeyColumnNames = uniqueKeyColumnNames.get(i);
			uniqueKeyColumnNamesAry[i] = myUniqueKeyColumnNames.toArray(new String[myUniqueKeyColumnNames.size()]);
		}

		return uniqueKeyColumnNamesAry;
	}

	protected ImportedKeyInfo[] getImportedKeyInfos(Connection cn, DatabaseMetaData metaData, String schema,
			String tableName) throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getImportedKeyResulSet(cn, metaData, schema, tableName);

			List<ImportedKeyInfo> importedKeyInfos = getImportedKeyInfoResultSetSpec().read(rs);

			ImportedKeyInfo[] array = importedKeyInfos.toArray(new ImportedKeyInfo[importedKeyInfos.size()]);

			postProcessImportedKeyInfo(array);

			return array;
		}
		// 当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常，为了简化处理逻辑，这里直接压制
		catch (SQLNonTransientException e)
		{
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("An " + ImportedKeyInfo.class.getSimpleName()
						+ "[0] object will be returned for an SQLNonTransientException", e);

			return new ImportedKeyInfo[0];
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected ExportedKeyInfo[] getExportedKeyInfos(Connection cn, DatabaseMetaData metaData, String schema,
			String tableName) throws DatabaseInfoResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getExportedKeyResulSet(cn, metaData, schema, tableName);

			List<ExportedKeyInfo> exportedKeyInfos = getExportedKeyInfoResultSetSpec().read(rs);

			ExportedKeyInfo[] array = exportedKeyInfos.toArray(new ExportedKeyInfo[exportedKeyInfos.size()]);

			postProcessExportedKeyInfo(array);

			return array;
		}
		// 当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常，为了简化处理逻辑，这里直接压制
		catch (SQLNonTransientException e)
		{
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("An " + ExportedKeyInfo.class.getSimpleName()
						+ "[0] object will be returned for an SQLNonTransientException", e);

			return new ExportedKeyInfo[0];
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	/**
	 * 构建{@linkplain TableInfo}结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param schema
	 * @param tableNamePattern
	 * @param tableTypes
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getTableInfoResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableNamePattern, String[] tableTypes) throws SQLException
	{
		if (tableNamePattern == null || tableNamePattern.isEmpty())
			tableNamePattern = "%";

		return databaseMetaData.getTables(cn.getCatalog(), schema, tableNamePattern, tableTypes);
	}

	/**
	 * 构建{@linkplain ColumnInfo}结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getColumnInfoResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getColumns(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName, "%");
	}

	/**
	 * 构建主键结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getPrimaryKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getPrimaryKeys(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName);
	}

	/**
	 * 构建唯一键结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getUniqueKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getIndexInfo(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName, true, false);
	}

	/**
	 * 构建{@linkplain ImportedKeyInfo}结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getImportedKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getImportedKeys(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName);
	}

	/**
	 * 构建{@linkplain ExportedKeyInfo}结果集。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	protected ResultSet getExportedKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getExportedKeys(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName);
	}

	/**
	 * 获取{@linkplain TableInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<TableInfo> getTableInfoResultSetSpec()
	{
		return new TableInfoResultSetSpec();
	}

	/**
	 * 获取{@linkplain ColumnInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<ColumnInfo> getColumnInfoResultSetSpec()
	{
		return new ColumnInfoResultSetSpec();
	}

	/**
	 * 获取{@linkplain PrimaryKeyInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<PrimaryKeyInfo> getPrimaryKeyInfoResultSetSpec()
	{
		return new PrimaryKeyInfoResultSetSpec();
	}

	/**
	 * 获取{@linkplain UniqueKeyInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<UniqueKeyInfo> getUniqueKeyInfoResultSetSpec()
	{
		return new UniqueKeyInfoResultSetSpec();
	}

	/**
	 * 获取{@linkplain ImportedKeyInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<ImportedKeyInfo> getImportedKeyInfoResultSetSpec()
	{
		return new ImportedKeyInfoResultSetSpec();
	}

	/**
	 * 获取{@linkplain ExportedKeyInfo}的{@linkplain ResultSetSpec}。
	 * 
	 * @return
	 */
	protected ResultSetSpec<ExportedKeyInfo> getExportedKeyInfoResultSetSpec()
	{
		return new ExportedKeyInfoResultSetSpec();
	}

	/**
	 * 获取表类型数组。
	 * 
	 * @return
	 */
	protected String[] getTableTypes()
	{
		return TABLE_TABLE_TYPES;
	}

	/**
	 * 后置处理{@link TableInfo}。
	 * <p>
	 * 子类可以重写此方法对{@linkplain TableInfo}进行特殊后置处理。
	 * </p>
	 * 
	 * @param tableInfo
	 */
	protected void postProcessTableInfo(TableInfo[] tableInfos)
	{
	}

	/**
	 * 后置处理{@link ColumnInfo}。
	 * <p>
	 * 子类可以重写此方法对{@linkplain ColumnInfo}进行特殊后置处理。
	 * </p>
	 * 
	 * @param columnInfos
	 */
	protected void postProcessColumnInfo(ColumnInfo[] columnInfos)
	{
	}

	/**
	 * 后置处理{@link ImportedKeyInfo}。
	 * <p>
	 * 子类可以重写此方法对{@linkplain ImportedKeyInfo}进行特殊后置处理。
	 * </p>
	 * 
	 * @param importedKeyInfos
	 */
	protected void postProcessImportedKeyInfo(ImportedKeyInfo[] importedKeyInfos)
	{
	}

	/**
	 * 后置处理{@link ExportedKeyInfo}。
	 * <p>
	 * 子类可以重写此方法对{@linkplain ExportedKeyInfo}进行特殊后置处理。
	 * </p>
	 * 
	 * @param exportedKeyInfos
	 */
	protected void postProcessExportedKeyInfo(ExportedKeyInfo[] exportedKeyInfos)
	{
	}

	/**
	 * 获取{@linkplain DatabaseMetaData}。
	 * 
	 * @param cn
	 * @return
	 * @throws DatabaseInfoResolverException
	 */
	protected DatabaseMetaData getDatabaseMetaData(Connection cn) throws DatabaseInfoResolverException
	{
		try
		{
			return cn.getMetaData();
		}
		catch (SQLException e)
		{
			throw new DatabaseInfoResolverException(e);
		}
	}

	/**
	 * 获取指定连接的schema。
	 * 
	 * @param cn
	 * @param databaseMetaData
	 * @return
	 * @throws DatabaseInfoResolverException
	 */
	protected String getSchema(Connection cn, DatabaseMetaData databaseMetaData) throws DatabaseInfoResolverException
	{
		// 在JDBC4.0（JDK1.6）中需要将其设置为null，才符合DatabaseMetaData.getTables(...)等接口的参数要求
		String schema = null;

		// JDBC4.1（JDK1.7）才有Connection.getSchema()接口，为了兼容性，本应用采用了JDBC4.0（JDK1.6）
		// 这里为了能支持JDBC4.1的的驱动，先采用反射方式获取
		if (GET_SCHEMA_METHOD_JDBC41 != null)
		{
			try
			{
				schema = (String) GET_SCHEMA_METHOD_JDBC41.invoke(cn, new Object[0]);
			}
			catch (Exception e)
			{
			}
		}

		// 测试使用连接用户名是否可行
		if (schema == null)
		{
			ResultSet rs = null;

			try
			{
				String dbUserName = databaseMetaData.getUserName();
				rs = getTableInfoResulSet(cn, databaseMetaData, dbUserName, null, getTableTypes());

				if (rs.next())
					schema = dbUserName;
			}
			catch (SQLException e)
			{
				throw new DatabaseInfoResolverException(e);
			}
			finally
			{
				JdbcUtil.closeResultSet(rs);
			}
		}

		return schema;
	}

	protected static Method GET_SCHEMA_METHOD_JDBC41 = null;
	static
	{
		Method[] methods = Connection.class.getMethods();

		for (Method method : methods)
		{
			if ("getSchema".equals(method.getName()))
			{
				Class<?>[] parameterTypes = method.getParameterTypes();

				if (parameterTypes == null || parameterTypes.length == 0)
				{
					GET_SCHEMA_METHOD_JDBC41 = method;
					break;
				}
			}
		}
	}

}
