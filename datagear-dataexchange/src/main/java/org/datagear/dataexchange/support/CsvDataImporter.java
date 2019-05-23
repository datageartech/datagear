/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataImportException;
import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dataexchange.ImportResult;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV {@linkplain DevotedDataImporter}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImporter extends AbstractDevotedDataImporter<CsvImport>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public CsvDataImporter()
	{
		super();
	}

	public CsvDataImporter(DatabaseInfoResolver databaseInfoResolver)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	@Override
	public ImportResult impt(CsvImport impt) throws DataImportException
	{
		ImportResult importResult = new ImportResult();

		long startTime = System.currentTimeMillis();

		CSVParser csvParser = buildCSVParser(impt);
		SetParameterContext setParameterContext = buildSetParameterContext(impt);

		Connection cn = impt.getConnection();

		ColumnInfo[] columnInfos = null;
		PreparedStatement st = null;

		try
		{
			for (CSVRecord csvRecord : csvParser)
			{
				if (columnInfos == null)
				{
					columnInfos = resolveColumnInfos(impt, csvRecord);
					String sql = buildPreparedSql(cn, impt.getTable(), columnInfos);
					st = cn.prepareStatement(sql);
				}
				else
				{
					String[] recordValues = resolveCSVRecordValues(impt, csvRecord);
					setPreparedStatementParameters(impt, st, columnInfos, recordValues, setParameterContext);

					st.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			throw new DataImportException(e);
		}
		finally
		{
			JdbcUtil.closeStatement(st);
		}

		importResult.setDuration(System.currentTimeMillis() - startTime);

		return importResult;
	}

	/**
	 * 构建{@linkplain DataFormatContext}。
	 * 
	 * @param impt
	 * @return
	 */
	protected SetParameterContext buildSetParameterContext(AbstractTextImport impt)
	{
		return new SetParameterContext(impt.getDataFormat());
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数。
	 * 
	 * @param impt
	 * @param st
	 * @param parameterColumnInfos
	 * @param parameterValues
	 * @param setParameterContext
	 * @throws SQLException
	 */
	protected void setPreparedStatementParameters(AbstractTextImport impt, PreparedStatement st,
			ColumnInfo[] parameterColumnInfos, String[] parameterValues, SetParameterContext setParameterContext)
			throws SQLException
	{
		for (int i = 0; i < parameterColumnInfos.length; i++)
		{
			ColumnInfo columnInfo = parameterColumnInfos[i];
			String rawValue = (parameterValues == null || parameterValues.length - 1 < i ? null : parameterValues[i]);

			setPreparedStatementParameter(impt, st, i + 1, columnInfo, rawValue, setParameterContext);
		}
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param impt
	 * @param st
	 * @param parameterIndex
	 * @param parameterColumnInfo
	 * @param parameterValue
	 * @param setParameterContext
	 * @throws SQLException
	 */
	protected void setPreparedStatementParameter(AbstractTextImport impt, PreparedStatement st, int parameterIndex,
			ColumnInfo parameterColumnInfo, String parameterValue, SetParameterContext setParameterContext)
			throws SQLException, ParseException
	{
		int sqlType = parameterColumnInfo.getType();

		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		Connection cn = impt.getConnection();
		NumberFormat numberFormat = setParameterContext.getNumberFormatter();

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:

				st.setString(parameterIndex, parameterValue);
				break;

			case Types.NUMERIC:
			case Types.DECIMAL:

				BigDecimal bdv = new BigDecimal(parameterValue);
				st.setBigDecimal(parameterIndex, bdv);
				break;

			case Types.BIT:
			case Types.BOOLEAN:

				boolean bv = ("true".equalsIgnoreCase(parameterValue) || "1".equals(parameterValue)
						|| "on".equalsIgnoreCase(parameterValue));
				st.setBoolean(parameterIndex, bv);
				break;

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:

				numberFormat.setParseIntegerOnly(true);
				int iv = numberFormat.parse(parameterValue).intValue();
				st.setInt(parameterIndex, iv);
				break;

			case Types.BIGINT:

				numberFormat.setParseIntegerOnly(true);
				long lv = numberFormat.parse(parameterValue).longValue();
				st.setLong(parameterIndex, lv);
				break;

			case Types.REAL:

				numberFormat.setParseIntegerOnly(false);
				float fv = numberFormat.parse(parameterValue).floatValue();
				st.setFloat(parameterIndex, fv);
				break;

			case Types.FLOAT:
			case Types.DOUBLE:

				numberFormat.setParseIntegerOnly(false);
				double dv = numberFormat.parse(parameterValue).doubleValue();
				st.setDouble(parameterIndex, dv);
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:

				// TODO
				break;

			case Types.DATE:

				// TODO
				break;

			case Types.TIME:

				// TODO
				break;

			case Types.TIMESTAMP:

				// TODO
				break;

			case Types.CLOB:

				Clob clob = cn.createClob();
				clob.setString(1, parameterValue);
				st.setClob(parameterIndex, clob);
				break;

			case Types.BLOB:

				// TODO
				break;

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:

				st.setNString(parameterIndex, parameterValue);
				break;

			case Types.NCLOB:

				NClob nclob = cn.createNClob();
				nclob.setString(1, parameterValue);
				st.setNClob(parameterIndex, nclob);
				break;

			case Types.SQLXML:

				SQLXML sqlxml = cn.createSQLXML();
				sqlxml.setString(parameterValue);
				st.setSQLXML(parameterIndex, sqlxml);
				break;

			default:

				// 不支持的类型
				st.setNull(parameterIndex, sqlType);
				break;
		}
	}

	/**
	 * 从{@linkplain CSVRecord}解析列信息数组。
	 * 
	 * @param impt
	 * @param csvRecord
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected ColumnInfo[] resolveColumnInfos(CsvImport impt, CSVRecord csvRecord) throws ColumnNotFoundException
	{
		String[] columnNames = resolveCSVRecordValues(impt, csvRecord);

		return getColumnInfos(impt.getConnection(), impt.getTable(), columnNames);
	}

	/**
	 * 解析{@linkplain CSVRecord}值数组。
	 * 
	 * @param impt
	 * @param csvRecord
	 * @return
	 */
	protected String[] resolveCSVRecordValues(CsvImport impt, CSVRecord csvRecord)
	{
		int size = csvRecord.size();
		String[] values = new String[size];

		for (int i = 0; i < size; i++)
			values[i] = csvRecord.get(i);

		return values;
	}

	/**
	 * 构建{@linkplain CSVParser}。
	 * 
	 * @param impt
	 * @return
	 * @throws DataImportException
	 */
	protected CSVParser buildCSVParser(CsvImport impt) throws DataImportException
	{
		try
		{
			return CSVFormat.DEFAULT.parse(impt.getReader());
		}
		catch (IOException e)
		{
			throw new DataImportException(e);
		}
	}

	/**
	 * 构建预编译SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param columnInfos
	 * @return
	 * @throws SQLException
	 */
	protected String buildPreparedSql(Connection cn, String table, ColumnInfo[] columnInfos) throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columnInfos[i].getName()).append(quote);
		}

		sql.append(") VALUES (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append('?');
		}

		sql.append(")");

		return sql.toString();
	}

	/**
	 * 获取表指定列信息数组。
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected ColumnInfo[] getColumnInfos(Connection cn, String table, String[] columnNames)
			throws ColumnNotFoundException
	{
		ColumnInfo[] columnInfos = new ColumnInfo[columnNames.length];

		ColumnInfo[] allColumnInfos = this.databaseInfoResolver.getColumnInfos(cn, table);

		for (int i = 0; i < columnNames.length; i++)
		{
			ColumnInfo columnInfo = null;

			for (int j = 0; j < allColumnInfos.length; j++)
			{
				if (allColumnInfos[j].getName().equals(columnNames[i]))
				{
					columnInfo = allColumnInfos[j];
					break;
				}
			}

			if (columnInfo == null)
				throw new ColumnNotFoundException(table, columnNames[i]);

			columnInfos[i] = columnInfo;
		}

		return columnInfos;
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数支持上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class SetParameterContext extends DataFormatContext
	{
		private List<Closeable> closeResources = new LinkedList<Closeable>();

		public SetParameterContext()
		{
			super();
		}

		public SetParameterContext(DataFormat dataFormat)
		{
			super(dataFormat);
		}

		/**
		 * 添加一个待关闭的{@linkplain Closeable}。
		 * 
		 * @param closeable
		 */
		public void addCloseResource(Closeable closeable)
		{
			this.closeResources.add(closeable);
		}

		/**
		 * 清除并关闭所有{@linkplain Closeable}。
		 * 
		 * @return
		 */
		public int clearCloseResources()
		{
			int size = closeResources.size();

			for (int i = 0; i < size; i++)
			{
				Closeable closeable = this.closeResources.get(i);

				try
				{
					closeable.close();
				}
				catch (IOException e)
				{
				}
			}

			return size;
		}
	}
}
