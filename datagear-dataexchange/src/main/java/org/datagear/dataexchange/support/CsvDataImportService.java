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
import org.datagear.dataexchange.AbstractDevotedTextValueDataImportService;
import org.datagear.dataexchange.ColumnNotFoundException;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImportService extends AbstractDevotedTextValueDataImportService<CsvDataImport>
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
	protected void exchange(CsvDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext importContext = castDataExchangeContext(context);

		Reader csvReader = getResource(dataExchange.getReaderFactory(), importContext);

		Connection cn = context.getConnection();
		cn.setAutoCommit(false);
		PreparedStatement st = null;

		List<ColumnInfo> rawColumnInfos = null;
		List<ColumnInfo> noNullColumnInfos = null;

		CSVParser csvParser = buildCSVParser(csvReader);

		long row = 0;

		for (CSVRecord csvRecord : csvParser)
		{
			importContext.setDataIndex(RowDataIndex.valueOf(row));

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

				importData(dataExchange, cn, st, noNullColumnInfos, columnValues, importContext);
			}

			row++;
		}

		commit(cn);
	}

	@Override
	protected void onException(CsvDataImport dataExchange, DataExchangeContext context, DataExchangeException e)
			throws DataExchangeException
	{
		processTransactionForDataExchangeException(context, e, dataExchange.getImportOption().getExceptionResolve());

		super.onException(dataExchange, context, e);
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
