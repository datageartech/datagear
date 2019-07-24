/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.datagear.dataexchange.AbstractDevotedTextDataExportService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * Excel导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataExportService extends AbstractDevotedTextDataExportService<ExcelDataExport>
{
	public ExcelDataExportService()
	{
		super();
	}

	public ExcelDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected void exchange(ExcelDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		TextDataExportContext exportContext = (TextDataExportContext) context;

		OutputStream out = getResource(dataExchange.getOutputFactory(), exportContext);

		Connection cn = context.getConnection();
		cn.setReadOnly(true);

		ResultSet rs = dataExchange.getQuery().execute(cn);
		List<ColumnInfo> columnInfos = getColumnInfos(cn, rs);

		writeRecords(dataExchange, cn, columnInfos, rs, out, exportContext);
	}

	/**
	 * 写记录。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columnInfos
	 * @param rs
	 * @param out
	 * @param exportContext
	 * @throws Throwable
	 */
	protected void writeRecords(ExcelDataExport dataExchange, Connection cn, List<ColumnInfo> columnInfos, ResultSet rs,
			OutputStream out, TextDataExportContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		TextDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columnInfos.size();

		int maxRows = SpreadsheetVersion.EXCEL2007.getMaxRows();

		SXSSFWorkbook wb = new SXSSFWorkbook(500);

		exportContext.addContextCloseable(wb);

		int sheetIndex = 0;
		int rowIndex = 0;
		Sheet sheet = wb.createSheet();

		while (rs.next())
		{
			// 当记录数大于sheet允许最大行时，新建一个sheet
			if (rowIndex >= maxRows)
			{
				sheetIndex++;
				rowIndex = 0;
				sheet = wb.createSheet();
			}

			if (rowIndex == 0)
			{
				Row titleRow = sheet.createRow(rowIndex);

				for (int i = 0; i < columnCount; i++)
				{
					ColumnInfo columnInfo = columnInfos.get(i);

					Cell cell = titleRow.createCell(i);

					cell.setCellType(CellType.STRING);
					cell.setCellValue(columnInfo.getName());
				}

				rowIndex++;
			}

			exportContext.setDataIndex(ExcelDataIndex.valueOf(sheetIndex, rowIndex));

			Row row = sheet.createRow(rowIndex);

			for (int i = 0; i < columnCount; i++)
			{
				ColumnInfo columnInfo = columnInfos.get(i);

				Cell cell = row.createCell(i);

				String value = null;

				try
				{
					value = getStringValue(cn, rs, i + 1, columnInfo.getType(), exportContext.getDataFormatContext());
				}
				catch (Throwable t)
				{
					if (exportOption.isNullForIllegalColumnValue())
					{
						value = null;

						if (listener != null)
							listener.onSetNullTextValue(exportContext.getDataIndex(), columnInfo.getName(),
									wrapToDataExchangeException(t));
					}
					else
						throw t;
				}

				cell.setCellType(CellType.STRING);
				cell.setCellValue(value);
			}

			if (listener != null)
				listener.onSuccess(exportContext.getDataIndex());

			rowIndex++;
		}

		wb.write(out);

		wb.dispose();
	}
}
