/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataImportException;
import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dataexchange.Import;
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
					setPreparedStatementParameters(impt, st, columnInfos, recordValues);

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
	 * 设置{@linkplain PreparedStatement}参数。
	 * 
	 * @param impt
	 * @param st
	 * @param parameterColumnInfos
	 * @param parameterValues
	 * @throws SQLException
	 */
	protected void setPreparedStatementParameters(Import impt, PreparedStatement st, ColumnInfo[] parameterColumnInfos,
			Object[] parameterValues) throws SQLException
	{
		for (int i = 0; i < parameterColumnInfos.length; i++)
		{
			// TODO
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
}
