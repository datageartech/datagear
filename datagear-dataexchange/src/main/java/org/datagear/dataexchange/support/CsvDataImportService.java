/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataImportListener;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImportService extends AbstractDevotedTextDataImportService<CsvDataImport>
{
	public CsvDataImportService()
	{
		super();
	}

	public CsvDataImportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	public void exchange(CsvDataImport dataExchange) throws DataExchangeException
	{
		TextDataImportListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		Reader csvReader = null;
		Connection cn = null;
		PreparedStatement st = null;

		ExceptionResolve exceptionResolve = null;
		ConnectionFactory connectionFactory = null;

		try
		{
			exceptionResolve = dataExchange.getImportOption().getExceptionResolve();

			if (exceptionResolve == null)
				exceptionResolve = ExceptionResolve.ROLLBACK;

			connectionFactory = dataExchange.getConnectionFactory();

			TextDataImportContext importContext = buildTextDataImportContext(dataExchange);

			List<ColumnInfo> rawColumnInfos = null;
			List<ColumnInfo> noNullColumnInfos = null;

			csvReader = getResource(dataExchange.getReaderFactory());
			CSVParser csvParser = buildCSVParser(csvReader);

			cn = connectionFactory.get();
			cn.setAutoCommit(false);

			for (CSVRecord csvRecord : csvParser)
			{
				if (rawColumnInfos == null)
				{
					rawColumnInfos = resolveColumnInfos(dataExchange, cn, csvRecord);
					noNullColumnInfos = removeNullColumnInfos(rawColumnInfos);

					// 表不匹配
					if (noNullColumnInfos == null || noNullColumnInfos.isEmpty())
						throw new TableMismatchException(dataExchange.getTable());

					String sql = buildInsertPreparedSql(cn, dataExchange.getTable(), noNullColumnInfos);
					st = cn.prepareStatement(sql);
				}
				else
				{
					List<String> columnValues = resolveCSVRecordValues(dataExchange, csvRecord, rawColumnInfos,
							noNullColumnInfos);

					importNextData(dataExchange, cn, st, noNullColumnInfos, columnValues, importContext);
				}
			}

			commit(cn);

			if (listener != null)
				listener.onSuccess();
		}
		catch (Throwable t)
		{
			DataExchangeException e = wrapToDataExchangeException(t);

			if (ExceptionResolve.ABORT.equals(exceptionResolve))
				commitSilently(cn);
			else if (ExceptionResolve.ROLLBACK.equals(exceptionResolve))
				rollbackSilently(cn);
			else if (ExceptionResolve.IGNORE.equals(exceptionResolve))
				commitSilently(cn);
			else
				throw new UnsupportedOperationException();

			if (listener != null)
				listener.onException(e);
			else
				throw e;
		}
		finally
		{
			releaseResource(dataExchange.getReaderFactory(), csvReader);
			JdbcUtil.closeStatement(st);
			releaseResource(connectionFactory, cn);

			if (listener != null)
				listener.onFinish();
		}
	}

	/**
	 * 从{@linkplain CSVRecord}解析列信息数组。
	 * <p>
	 * 当指定名称的列不存在时，如果{@code CsvImport#isIgnoreInexistentColumn()}为{@code true}，返回数组对应位置将为{@code null}，
	 * 否则，将立刻抛出{@linkplain ColumnNotFoundException}。
	 * </p>
	 * 
	 * @param impt
	 * @param cn
	 * @param csvRecord
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected List<ColumnInfo> resolveColumnInfos(CsvDataImport impt, Connection cn, CSVRecord csvRecord)
			throws ColumnNotFoundException
	{
		List<String> columnNames = resolveCSVRecordValues(impt, csvRecord);

		return getColumnInfos(cn, impt.getTable(), columnNames, impt.getImportOption().isIgnoreInexistentColumn());
	}

	/**
	 * 解析{@linkplain CSVRecord}值数组。
	 * 
	 * @param impt
	 * @param csvRecord
	 * @param rawColumnInfos
	 * @param noNullColumnInfos
	 * @return
	 */
	protected List<String> resolveCSVRecordValues(CsvDataImport impt, CSVRecord csvRecord,
			List<ColumnInfo> rawColumnInfos, List<ColumnInfo> noNullColumnInfos)
	{
		List<String> values = resolveCSVRecordValues(impt, csvRecord);

		return removeNullColumnValues(rawColumnInfos, noNullColumnInfos, values);
	}

	/**
	 * 解析{@linkplain CSVRecord}值数组。
	 * 
	 * @param impt
	 * @param csvRecord
	 * @return
	 */
	protected List<String> resolveCSVRecordValues(CsvDataImport impt, CSVRecord csvRecord)
	{
		int size = csvRecord.size();
		List<String> list = new ArrayList<String>(size);

		for (int i = 0; i < size; i++)
			list.add(csvRecord.get(i));

		return list;
	}

	/**
	 * 构建{@linkplain CSVParser}。
	 * 
	 * @param reader
	 * @return
	 * @throws DataExchangeException
	 */
	protected CSVParser buildCSVParser(Reader reader) throws DataExchangeException
	{
		try
		{
			return CSVFormat.DEFAULT.parse(reader);
		}
		catch (Exception e)
		{
			throw new DataExchangeException(e);
		}
	}
}
