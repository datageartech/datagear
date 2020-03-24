/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.DataType;
import org.datagear.meta.Database;
import org.datagear.meta.ImportKey;
import org.datagear.meta.PrimaryKey;
import org.datagear.meta.SearchableType;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;
import org.datagear.meta.TableType;
import org.datagear.meta.UniqueKey;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象{@linkplain DevotedDBMetaResolver}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDevotedDBMetaResolver implements DevotedDBMetaResolver
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDevotedDBMetaResolver.class);

	public static final String TABLE_TYPE_TABLE = "TABLE";

	public static final String TABLE_TYPE_VIEW = "VIEW";

	protected static final String[] TABLE_TYPES = { TABLE_TYPE_TABLE, TABLE_TYPE_VIEW };

	protected static final String[] EMPTY_STRING_ARRAY = new String[0];

	public AbstractDevotedDBMetaResolver()
	{
		super();
	}

	@Override
	public Database getDatabase(Connection cn) throws DBMetaResolverException
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();

			Database databaseInfo = new Database();

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
			throw new DBMetaResolverException(e);
		}
	}

	@Override
	public List<SimpleTable> getSimpleTables(Connection cn) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getSimpleTables(cn, metaData, schema, null);
	}

	@Override
	public SimpleTable getRandomSimpleTable(Connection cn) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getRandomSimpleTable(cn, metaData, schema);
	}

	@Override
	public Table getTable(Connection cn, String tableName) throws DBMetaResolverException
	{
		@JDBCCompatiblity("如果cn为readonly，某些驱动程序的DatabaseMetaData.isReadOnly()也将为true（比如：Postgresql JDBC 42.2.5），"
				+ "这会导致解析Table.readonly不正确，因此这里设为false，以保证解析正确")
		boolean readonly = JdbcUtil.isReadonlyIfSupports(cn, true);
		if (readonly)
			JdbcUtil.setReadonlyIfSupports(cn, false);

		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getTable(cn, metaData, schema, tableName);
	}

	@Override
	public Column[] getColumns(Connection cn, String tableName) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getColumns(cn, metaData, schema, tableName, null);
	}

	@Override
	public Column getRandomColumn(Connection cn, String tableName) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		Column[] columns = getColumns(cn, metaData, schema, tableName, 1);

		return (columns == null || columns.length < 1 ? null : columns[0]);
	}

	@Override
	public Column[] getColumns(Connection cn, ResultSetMetaData resultSetMetaData) throws DBMetaResolverException
	{
		try
		{
			int columnCount = resultSetMetaData.getColumnCount();

			Column[] columnInfos = new Column[columnCount];

			for (int i = 1; i <= columnCount; i++)
			{
				Column column = new Column();

				String columnName = resultSetMetaData.getColumnLabel(i);
				if (columnName == null || columnName.isEmpty())
					columnName = resultSetMetaData.getColumnName(i);

				column.setName(columnName);
				column.setType(resultSetMetaData.getColumnType(i));
				column.setTypeName(resultSetMetaData.getColumnTypeName(i));
				column.setSize(resultSetMetaData.getPrecision(i));
				column.setDecimalDigits(resultSetMetaData.getScale(i));
				column.setNullable(DatabaseMetaData.columnNoNulls != resultSetMetaData.isNullable(i));
				column.setAutoincrement(resultSetMetaData.isAutoIncrement(i));

				columnInfos[i - 1] = column;
			}

			return columnInfos;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
	}

	@Override
	public PrimaryKey getPrimaryKey(Connection cn, String tableName) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		return getPrimaryKey(cn, metaData, schema, tableName);
	}

	@Override
	public List<DataType> getDataTypes(Connection cn) throws DBMetaResolverException
	{
		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		return getDataTypes(cn, metaData);
	}

	@Override
	public List<String[]> getImportTables(Connection cn, String... tableNames)
	{
		List<String[]> importTabless = new ArrayList<>(tableNames.length);

		DatabaseMetaData metaData = getDatabaseMetaData(cn);
		String schema = getSchema(cn, metaData);

		for (int i = 0; i < tableNames.length; i++)
		{
			String[] importTables = null;

			if (StringUtil.isEmpty(tableNames[i]))
				importTables = EMPTY_STRING_ARRAY;
			else
			{
				// 处理重复表
				for (int k = 0; i < i; k++)
				{
					if (tableNames[k].equals(tableNames[i]))
					{
						importTables = importTabless.get(k);
						break;
					}
				}

				if (importTables == null)
				{
					ImportKey[] importKeys = getImportKeys(cn, metaData, schema, tableNames[i]);

					if (importKeys == null || importKeys.length == 0)
						importTables = EMPTY_STRING_ARRAY;
					else
					{
						List<String> importedTableList = new ArrayList<>(2);

						for (int j = 0; j < importKeys.length; j++)
						{
							String primaryTable = importKeys[j].getPrimaryTableName();

							if (!importedTableList.contains(primaryTable))
								importedTableList.add(primaryTable);
						}

						importTables = new String[importedTableList.size()];
						importedTableList.toArray(importTables);
					}
				}
			}

			importTabless.add(importTables);
		}

		return importTabless;
	}

	/**
	 * @param cn
	 * @param metaData
	 * @param schema
	 * @param tableNamePattern
	 *            允许为{@code null}
	 * @return
	 * @throws DBMetaResolverException
	 */
	protected List<SimpleTable> getSimpleTables(Connection cn, DatabaseMetaData metaData, String schema,
			String tableNamePattern) throws DBMetaResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getTableResulSet(cn, metaData, schema, tableNamePattern, TABLE_TYPES);

			List<SimpleTable> simpleTables = new ArrayList<>();

			while (rs.next())
			{
				SimpleTable simpleTable = readSimpleTable(cn, metaData, schema, rs);

				if (simpleTable != null)
				{
					simpleTable = postProcessSimpleTable(cn, metaData, schema, simpleTable);
					simpleTables.add(simpleTable);
				}
			}

			return simpleTables;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	/**
	 * @return 返回{@code null}表示未读取到
	 */
	protected SimpleTable readSimpleTable(Connection cn, DatabaseMetaData metaData, String schema, ResultSet rs)
	{
		try
		{
			String name = rs.getString("TABLE_NAME");
			TableType type = toTableType(rs.getString("TABLE_TYPE"));

			if (StringUtil.isEmpty(name) || StringUtil.isEmpty(type))
			{
				LOGGER.warn("Invalid table row : name={}, type={}", name, type);
				return null;
			}

			SimpleTable simpleTable = new SimpleTable(name, type);
			simpleTable.setComment(rs.getString("REMARKS"));

			return simpleTable;
		}
		catch (SQLException e)
		{
			return null;
		}
	}

	protected TableType toTableType(String type)
	{
		if (TABLE_TYPE_TABLE.equalsIgnoreCase(type))
			return TableType.TABLE;
		else if (TABLE_TYPE_VIEW.equalsIgnoreCase(type))
			return TableType.VIEW;
		else
			return null;
	}

	protected SimpleTable postProcessSimpleTable(Connection cn, DatabaseMetaData metaData, String schema,
			SimpleTable simpleTable) throws SQLException
	{
		return simpleTable;
	}

	protected List<DataType> getDataTypes(Connection cn, DatabaseMetaData metaData) throws DBMetaResolverException
	{
		List<DataType> dataTypes = new ArrayList<>();

		ResultSet rs = null;

		try
		{
			rs = getDataTypeResultSet(cn, metaData);

			while (rs.next())
			{
				DataType dataType = readDataType(rs);

				if (dataType != null)
					dataTypes.add(dataType);
			}

			return dataTypes;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected Table getTable(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DBMetaResolverException
	{
		boolean readonly = resolveTableReadonly(cn);

		List<SimpleTable> simpleTables = getSimpleTables(cn, metaData, schema, tableName);

		if (simpleTables == null || simpleTables.isEmpty())
			throw new TableNotFoundException(tableName);

		SimpleTable simpleTable = simpleTables.get(0);

		Table table = new Table();
		table.setName(simpleTable.getName());
		table.setType(simpleTable.getType());
		table.setComment(simpleTable.getComment());
		table.setColumns(getColumns(cn, metaData, schema, tableName, null));
		table.setPrimaryKey(getPrimaryKey(cn, metaData, schema, tableName));
		table.setUniqueKeys(getUniqueKeys(cn, metaData, schema, tableName));
		table.setImportKeys(getImportKeys(cn, metaData, schema, tableName));
		table.setReadonly(readonly);

		table = postProcessTable(cn, metaData, schema, table);

		return table;
	}

	protected boolean resolveTableReadonly(Connection cn)
	{
		@JDBCCompatiblity("如果cn为readonly，某些驱动程序的DatabaseMetaData.isReadOnly()也将为true（比如：Postgresql JDBC 42.2.5），"
				+ "这会导致解析Table.readonly不正确，因此这里尝试设为false，来测试数据库是否为只读")
		boolean cnReadonly = JdbcUtil.isReadonlyIfSupports(cn, true);
		if (cnReadonly)
			JdbcUtil.setReadonlyIfSupports(cn, false);

		try
		{
			// 数据库是否只读
			boolean dbReadonly = cn.getMetaData().isReadOnly();
			return dbReadonly;
		}
		catch(SQLException e)
		{
			return false;
		}
	}

	protected Table postProcessTable(Connection cn, DatabaseMetaData metaData, String schema, Table table)
	{
		return table;
	}

	protected ResultSet getTableResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableNamePattern, String[] tableTypes) throws SQLException
	{
		if (tableNamePattern == null || tableNamePattern.isEmpty())
			tableNamePattern = "%";

		return databaseMetaData.getTables(cn.getCatalog(), schema, tableNamePattern, tableTypes);
	}

	/**
	 * 
	 * @param cn
	 * @param metaData
	 * @param schema
	 * @param tableName
	 * @param count
	 *            为{@code null}获取全部，否则获取指定数目
	 * @return
	 * @throws DBMetaResolverException
	 */
	protected Column[] getColumns(Connection cn, DatabaseMetaData metaData, String schema, String tableName,
			Integer count) throws DBMetaResolverException
	{
		ResultSet rs = null;

		try
		{
			rs = getColumnResulSet(cn, metaData, schema, tableName);

			List<Column> columns = new ArrayList<>();

			while (rs.next())
			{
				Column column = readColumn(cn, metaData, schema, tableName, rs);
				column = postProcessColumn(cn, metaData, schema, tableName, column);
				addColumn(columns, column);

				if (count != null && columns.size() == count)
					break;
			}

			sortColumns(columns);

			return columns.toArray(new Column[columns.size()]);
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	/**
	 * @return 返回{@code null}表示未读取到
	 */
	protected Column readColumn(Connection cn, DatabaseMetaData metaData, String schema, String tableName, ResultSet rs)
	{
		try
		{
			String name = rs.getString("COLUMN_NAME");

			if (StringUtil.isEmpty(name))
			{
				LOGGER.warn("Invalid column row : name={}", name);
				return null;
			}

			Column column = new Column(name, rs.getInt("DATA_TYPE"));

			column.setTypeName(rs.getString("TYPE_NAME"));
			column.setSize(rs.getInt("COLUMN_SIZE"));
			column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
			column.setNullable(DatabaseMetaData.columnNoNulls != rs.getInt("NULLABLE"));
			column.setComment(rs.getString("REMARKS"));
			column.setDefaultValue(rs.getString("COLUMN_DEF"));
			column.setAutoincrement("yes".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")));
			resolveSortable(cn, metaData, schema, tableName, column);
			resolveSearchableType(cn, metaData, schema, tableName, column);

			resolveDefaultValue(column);

			return column;
		}
		catch (SQLException e)
		{
			return null;
		}
	}

	protected void resolveDefaultValue(Column column)
	{
		if (!column.hasDefaultValue())
			return;

		String value = column.getDefaultValue();
		int len = value.length();

		// 移除开头和结尾的引号
		if (len >= 2 && ((value.charAt(0) == '\'' && value.charAt(len - 1) == '\'')
				|| (value.charAt(0) == '"' && value.charAt(len - 1) == '"')))
		{
			value = (len == 2 ? "" : value.substring(1, value.length() - 1));
			column.setDefaultValue(value);
		}

		if (StringUtil.isEmpty(value))
			return;

	}

	@JDBCCompatiblity("很多驱动程序的值为SearchableType.ALL但实际并不支持LIKE语法（比如：PostgreSQL JDBC 42.2.5），"
			+ "这里为了兼容，不采用数据库级的SearchableType逻辑")
	protected void resolveSearchableType(Connection cn, DatabaseMetaData metaData, String schema, String tableName,
			Column column)
	{
		SearchableType searchableType = null;

		int sqlType = column.getType();

		if (Types.CHAR == sqlType || Types.VARCHAR == sqlType || Types.NCHAR == sqlType || Types.NVARCHAR == sqlType)
			searchableType = SearchableType.ALL;

		column.setSearchableType(searchableType);
	}

	@JDBCCompatiblity("某些驱动程序对有些类型不支持排序（比如Oracle对于BLOB类型）")
	protected void resolveSortable(Connection cn, DatabaseMetaData metaData, String schema, String tableName,
			Column column)
	{
		int sqlType = column.getType();

		boolean sortable = (Types.BIGINT == sqlType || Types.BIT == sqlType || Types.BOOLEAN == sqlType
				|| Types.CHAR == sqlType || Types.DATE == sqlType || Types.DECIMAL == sqlType || Types.DOUBLE == sqlType
				|| Types.FLOAT == sqlType || Types.INTEGER == sqlType || Types.NCHAR == sqlType
				|| Types.NUMERIC == sqlType || Types.NVARCHAR == sqlType || Types.REAL == sqlType
				|| Types.SMALLINT == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType
				|| Types.TINYINT == sqlType || Types.VARCHAR == sqlType);

		column.setSortable(sortable);
	}

	protected Column postProcessColumn(Connection cn, DatabaseMetaData metaData, String schema, String tableName,
			Column column) throws SQLException
	{
		return column;
	}

	protected ResultSet getColumnResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getColumns(cn.getCatalog(), schema, tableName, "%");
	}

	/**
	 * 
	 * @param cn
	 * @param metaData
	 * @param schema
	 * @param tableName
	 * @return 返回{@code null}表示无主键。
	 * @throws DBMetaResolverException
	 */
	protected PrimaryKey getPrimaryKey(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DBMetaResolverException
	{
		PrimaryKey primaryKey = null;

		ResultSet rs = null;
		try
		{
			rs = getPrimaryKeyResulSet(cn, metaData, schema, tableName);

			List<String> columnNames = new ArrayList<>();
			String keyName = null;

			while (rs.next())
			{
				String columnName = rs.getString("COLUMN_NAME");
				if (StringUtil.isEmpty(keyName))
					keyName = rs.getString("PK_NAME");

				addName(columnNames, columnName);
			}

			if (!columnNames.isEmpty())
			{
				primaryKey = new PrimaryKey(columnNames.toArray(new String[columnNames.size()]));
				primaryKey.setKeyName(keyName);
			}

			return primaryKey;
		}
		catch (SQLNonTransientException e)
		{
			@JDBCCompatiblity("当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常")
			SQLNonTransientException e1 = e;

			LOGGER.warn("return null primary key object for exception", e1);

			return null;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	/**
	 * @param cn
	 * @param metaData
	 * @param schema
	 * @param tableName
	 * @return 返回{@code null}表示无唯一键
	 * @throws DBMetaResolverException
	 */
	protected UniqueKey[] getUniqueKeys(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DBMetaResolverException
	{
		UniqueKey[] uniqueKeys = null;

		ResultSet rs = null;

		List<String> keyNames = new ArrayList<>();
		List<List<String>> keyColumnNamess = new ArrayList<>();

		try
		{
			rs = getUniqueKeyResulSet(cn, metaData, schema, tableName);

			while (rs.next())
			{
				String keyName = rs.getString("INDEX_NAME");
				if (keyName == null)
					keyName = "";
				String columnName = rs.getString("COLUMN_NAME");

				int myIndex = keyNames.indexOf(keyName);
				List<String> keyColumnNames = null;

				if (myIndex < 0)
				{
					keyNames.add(keyName);
					keyColumnNames = new ArrayList<>();
					keyColumnNamess.add(keyColumnNames);
				}
				else
					keyColumnNames = keyColumnNamess.get(myIndex);

				addName(keyColumnNames, columnName);
			}
		}
		catch (SQLNonTransientException e)
		{
			@JDBCCompatiblity("当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常")
			SQLNonTransientException e1 = e;

			LOGGER.warn("return null primary key object for exception", e1);

			return null;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}

		if (!keyNames.isEmpty())
		{
			uniqueKeys = new UniqueKey[keyNames.size()];

			for (int i = 0; i < uniqueKeys.length; i++)
			{
				List<String> keyColumnNames = keyColumnNamess.get(i);
				uniqueKeys[i] = new UniqueKey(keyColumnNames.toArray(new String[keyColumnNames.size()]));
				uniqueKeys[i].setKeyName(keyNames.get(i));
			}
		}

		return uniqueKeys;
	}

	protected ImportKey[] getImportKeys(Connection cn, DatabaseMetaData metaData, String schema, String tableName)
			throws DBMetaResolverException
	{
		ImportKey[] importKeys = null;

		ResultSet rs = null;

		List<String> keyNames = new ArrayList<>();
		List<List<String>> columnNamess = new ArrayList<>();
		List<String> primaryTableNames = new ArrayList<>();
		List<List<String>> primaryColumnNamess = new ArrayList<>();

		try
		{
			rs = getImportKeyResulSet(cn, metaData, schema, tableName);

			while (rs.next())
			{
				String keyName = rs.getString("FK_NAME");
				if (keyName == null)
					keyName = "";

				String columnName = rs.getString("FKCOLUMN_NAME");
				String primaryColumnName = rs.getString("PKCOLUMN_NAME");

				int myIndex = keyNames.indexOf(keyName);
				List<String> columnNames = null;
				List<String> primaryColumnNames = null;

				if (myIndex < 0)
				{
					keyNames.add(keyName);
					primaryTableNames.add(rs.getString("PKTABLE_NAME"));

					columnNames = new ArrayList<>();
					columnNamess.add(columnNames);

					primaryColumnNames = new ArrayList<>();
					primaryColumnNamess.add(primaryColumnNames);
				}
				else
				{
					columnNames = columnNamess.get(myIndex);
					primaryColumnNames = primaryColumnNamess.get(myIndex);
				}

				addName(columnNames, columnName);
				addName(primaryColumnNames, primaryColumnName);
			}
		}
		catch (SQLNonTransientException e)
		{
			@JDBCCompatiblity("当tableName是视图时，某些驱动（比如Oracle）可能会抛出SQLSyntaxErrorException异常")
			SQLNonTransientException e1 = e;

			LOGGER.warn("return null import key object for exception", e1);

			return null;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}

		if (!keyNames.isEmpty())
		{
			importKeys = new ImportKey[keyNames.size()];

			for (int i = 0; i < importKeys.length; i++)
			{
				ImportKey importKey = new ImportKey();

				List<String> columnNames = columnNamess.get(i);
				List<String> primaryColumnNames = primaryColumnNamess.get(i);

				importKey.setColumnNames(columnNames.toArray(new String[columnNames.size()]));
				importKey.setPrimaryTableName(primaryTableNames.get(i));
				importKey.setPrimaryColumnNames(primaryColumnNames.toArray(new String[primaryColumnNames.size()]));
				importKey.setKeyName(keyNames.get(i));

				importKeys[i] = importKey;
			}
		}

		return importKeys;
	}

	@JDBCCompatiblity("避免某些驱动程序的结果集出现非法或者重复项")
	protected boolean addName(List<String> names, String name)
	{
		if (StringUtil.isEmpty(name) || names.indexOf(name) > -1)
			return false;

		names.add(name);
		return true;
	}

	@JDBCCompatiblity("避免某些驱动程序的结果集出现非法或者重复项")
	protected boolean addColumn(List<Column> columns, Column column)
	{
		if (column == null || containsColumn(columns, column.getName()))
			return false;

		columns.add(column);
		return true;
	}

	protected boolean containsColumn(List<Column> columns, String name)
	{
		for (Column column : columns)
		{
			if (column.getName().equals(name))
				return true;
		}

		return false;
	}

	protected void sortColumns(List<Column> columns)
	{
		Collections.sort(columns, COLUMN_SORT_COMPARATOR);
	}

	protected DataType readDataType(ResultSet rs)
	{
		try
		{
			String name = rs.getString("TYPE_NAME");
			if (StringUtil.isEmpty(name))
			{
				LOGGER.warn("Invalid data type row : name={}", name);
				return null;
			}

			DataType column = new DataType(name, rs.getInt("DATA_TYPE"));

			return column;
		}
		catch (SQLException e)
		{
			return null;
		}
	}

	protected ResultSet getPrimaryKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getPrimaryKeys(cn.getCatalog(), schema, tableName);
	}

	protected ResultSet getUniqueKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getIndexInfo(cn.getCatalog(), getSchema(cn, databaseMetaData), tableName, true, false);
	}

	protected ResultSet getImportKeyResulSet(Connection cn, DatabaseMetaData databaseMetaData, String schema,
			String tableName) throws SQLException
	{
		return databaseMetaData.getImportedKeys(cn.getCatalog(), schema, tableName);
	}

	protected ResultSet getDataTypeResultSet(Connection cn, DatabaseMetaData databaseMetaData) throws SQLException
	{
		return databaseMetaData.getTypeInfo();
	}

	protected SimpleTable getRandomSimpleTable(Connection cn, DatabaseMetaData metaData, String schema)
			throws DBMetaResolverException
	{
		SimpleTable simpleTable = null;

		ResultSet rs = null;

		try
		{
			rs = getTableResulSet(cn, metaData, schema, null, TABLE_TYPES);

			while (rs.next())
			{
				simpleTable = readSimpleTable(cn, metaData, schema, rs);

				if (simpleTable != null)
					return simpleTable;
			}

			return null;
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
		finally
		{
			JdbcUtil.closeResultSet(rs);
		}
	}

	protected DatabaseMetaData getDatabaseMetaData(Connection cn) throws DBMetaResolverException
	{
		try
		{
			return cn.getMetaData();
		}
		catch (SQLException e)
		{
			throw new DBMetaResolverException(e);
		}
	}

	protected String getSchema(Connection cn, DatabaseMetaData databaseMetaData) throws DBMetaResolverException
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
				rs = getTableResulSet(cn, databaseMetaData, dbUserName, null, TABLE_TYPES);

				if (rs.next())
					schema = dbUserName;
			}
			catch (SQLException e)
			{
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

	protected static final Comparator<Column> COLUMN_SORT_COMPARATOR = new Comparator<Column>()
	{
		@Override
		public int compare(Column o1, Column o2)
		{
			Integer p1 = o1.getPosition();
			return p1.compareTo(o2.getPosition());
		}
	};
}
