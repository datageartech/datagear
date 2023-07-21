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

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.ColumnNotFoundException;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;

/**
 * CSV导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImportService extends AbstractDevotedDBMetaDataExchangeService<CsvDataImport>
{
	public CsvDataImportService()
	{
		super();
	}

	public CsvDataImportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(CsvDataImport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(CsvDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		ValueDataImportOption importOption = dataExchange.getImportOption();
		IndexFormatDataExchangeContext importContext = IndexFormatDataExchangeContext.cast(context);

		Reader csvReader = getResource(dataExchange.getReaderFactory(), importContext);

		Connection cn = context.getConnection();
		JdbcUtil.setAutoCommitIfSupports(cn, false);
		JdbcUtil.setReadonlyIfSupports(cn, false);

		Table table = this.getTableIfValid(cn, dataExchange.getTable());
		String tableName = table.getName();
		List<Column> columns = null;
		List<Column> nonNullColumns = null;
		List<Boolean> nonNullImportKeyColumns = null;

		PreparedStatement st = null;
		CSVParser csvParser = buildCSVParser(csvReader);
		long row = 0;

		for (CSVRecord csvRecord : csvParser)
		{
			importContext.setDataIndex(RowDataIndex.valueOf(row));

			if (columns == null)
			{
				columns = resolveColumns(table, dataExchange, csvRecord);
				nonNullColumns = removeNullColumns(columns);
				nonNullImportKeyColumns = isImportKeyColumns(table, nonNullColumns);

				// 表不匹配
				if (StringUtil.isEmpty(nonNullColumns))
					throw new TableMismatchException(tableName);

				String sql = buildInsertPreparedSql(cn, tableName, nonNullColumns);
				st = cn.prepareStatement(sql);
			}
			else
			{
				List<String> columnValues = resolveCSVRecordValues(dataExchange, csvRecord, columns, nonNullColumns,
						nonNullImportKeyColumns);

				importValueData(cn, st, nonNullColumns, columnValues, importContext.getDataIndex(),
						importOption.isNullForIllegalColumnValue(), importOption.getExceptionResolve(),
						importContext.getDataFormatContext(), dataExchange.getListener());
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
	 * @param table
	 * @param impt
	 * @param csvRecord
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected List<Column> resolveColumns(Table table, CsvDataImport impt, CSVRecord csvRecord)
			throws ColumnNotFoundException
	{
		List<String> columnNames = resolveCSVRecordValues(impt, csvRecord);
		return findColumns(table, columnNames, impt.getImportOption().isIgnoreInexistentColumn());
	}

	/**
	 * 解析{@linkplain CSVRecord}值列表。
	 * 
	 * @param impt
	 * @param csvRecord
	 * @param columns
	 * @param nonNullColumns
	 * @param nonNullImportKeyColumns
	 * @return
	 */
	protected List<String> resolveCSVRecordValues(CsvDataImport impt, CSVRecord csvRecord, List<Column> columns,
			List<Column> nonNullColumns, List<Boolean> nonNullImportKeyColumns)
	{
		List<String> values = resolveCSVRecordValues(impt, csvRecord);
		// 这里需移除null列的列值
		values = removeValueOfNullColumnExpanded(columns, values, null);

		if (impt.getImportOption().isNullForEmptyImportKey())
		{
			setNullForEmptyIfImportKey(nonNullColumns, nonNullImportKeyColumns, values);
		}

		return values;
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
		List<String> list = new ArrayList<>(size);

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
