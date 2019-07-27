/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataFormatContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
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
public class ExcelDataExportService extends AbstractDevotedDbInfoAwareDataExchangeService<ExcelDataExport>
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
	protected DataExchangeContext createDataExchangeContext(ExcelDataExport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(ExcelDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext exportContext = IndexFormatDataExchangeContext.cast(context);

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
			OutputStream out, IndexFormatDataExchangeContext exportContext) throws Throwable
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

				setCellValue(dataExchange, cn, rs, i + 1, columnInfo, exportOption, exportContext, cell);
			}

			if (listener != null)
				listener.onSuccess(exportContext.getDataIndex());

			rowIndex++;
		}

		wb.write(out);

		wb.dispose();
	}

	/**
	 * 将字段值设置为单元格值。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param columnInfo
	 * @param exportOption
	 * @param exportContext
	 * @param cell
	 * @throws Throwable
	 */
	protected void setCellValue(ExcelDataExport dataExchange, Connection cn, ResultSet rs, int columnIndex,
			ColumnInfo columnInfo, TextDataExportOption exportOption, IndexFormatDataExchangeContext exportContext,
			Cell cell) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		DataFormatContext dataFormatContext = exportContext.getDataFormatContext();

		Object value = null;

		try
		{
			value = getValue(cn, rs, columnIndex, columnInfo.getType());
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

		if (value == null)
		{
			cell.setCellType(CellType.BLANK);
		}
		else if (value instanceof String)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue((String) value);
		}
		else if (value instanceof Number)
		{
			cell.setCellType(CellType.NUMERIC);
			cell.setCellValue(((Number) value).doubleValue());
		}
		else if (value instanceof Date)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(dataFormatContext.formatDate((Date) value));
		}
		else if (value instanceof Timestamp)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(dataFormatContext.formatTimestamp((Timestamp) value));
		}
		else if (value instanceof Time)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(dataFormatContext.formatTime((Time) value));
		}
		else if (value instanceof Boolean)
		{
			cell.setCellType(CellType.BOOLEAN);
			cell.setCellValue((Boolean) value);
		}
		else if (value instanceof byte[])
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(dataFormatContext.formatBytes((byte[]) value));
		}
		else
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(value.toString());
		}
	}
}
