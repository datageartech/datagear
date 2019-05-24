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
import org.datagear.dataexchange.ImportResult;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV {@linkplain DevotedDataImporter}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImporter extends AbstractTextDevotedDataImporter<CsvImport>
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
					String sql = buildInsertPreparedSql(cn, impt.getTable(), columnInfos);
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

		return getColumnInfos(impt.getConnection(), impt.getTable(), columnNames, this.databaseInfoResolver);
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
}
