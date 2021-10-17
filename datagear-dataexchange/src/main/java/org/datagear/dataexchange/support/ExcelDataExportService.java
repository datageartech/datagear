/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataFormatContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.meta.Column;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ConnectionFactory;

/**
 * Excel导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataExportService extends AbstractDevotedDBMetaDataExchangeService<ExcelDataExport>
{
	public ExcelDataExportService()
	{
		super();
	}

	public ExcelDataExportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(ExcelDataExport dataExchange)
	{
		return new ExcelDataExportContext(dataExchange.getConnectionFactory(),
				new DataFormatContext(dataExchange.getDataFormat()));
	}

	@Override
	protected void exchange(ExcelDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		ExcelDataExportContext exportContext = (ExcelDataExportContext) context;

		OutputStream out = getResource(dataExchange.getOutputFactory(), exportContext);

		Connection cn = context.getConnection();
		JdbcUtil.setReadonlyIfSupports(cn, true);

		ResultSet rs = dataExchange.getQuery().execute(cn);
		List<Column> columns = getColumns(cn, rs);

		writeRecords(dataExchange, cn, columns, rs, out, exportContext);
	}

	/**
	 * 写记录。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columns
	 * @param rs
	 * @param out
	 * @param exportContext
	 * @throws Throwable
	 */
	protected void writeRecords(ExcelDataExport dataExchange, Connection cn, List<Column> columns, ResultSet rs,
			OutputStream out, ExcelDataExportContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		TextDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columns.size();

		int maxRows = SpreadsheetVersion.EXCEL2007.getMaxRows();

		SXSSFWorkbook wb = new SXSSFWorkbook(500);
		CreationHelper creationHelper = wb.getCreationHelper();

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
					Column column = columns.get(i);

					Cell cell = titleRow.createCell(i);
					cell.setCellValue(column.getName());
				}

				rowIndex++;
			}

			exportContext.setDataIndex(ExcelDataIndex.valueOf(sheetIndex, rowIndex));

			Row row = sheet.createRow(rowIndex);

			for (int i = 0; i < columnCount; i++)
			{
				Column column = columns.get(i);

				Cell cell = row.createCell(i);

				setCellValue(dataExchange, cn, rs, i + 1, column, exportOption, exportContext, wb, creationHelper,
						cell);
			}

			if (listener != null)
				listener.onSuccess(exportContext.getDataIndex());

			rowIndex++;
		}

		wb.write(out);

		wb.dispose();
	}

	/**
	 * 将字段值存入单元格。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param column
	 * @param exportOption
	 * @param exportContext
	 * @param workbook
	 * @param creationHelper
	 * @param cell
	 * @throws Throwable
	 */
	protected void setCellValue(ExcelDataExport dataExchange, Connection cn, ResultSet rs, int columnIndex,
			Column column, TextDataExportOption exportOption, ExcelDataExportContext exportContext,
			SXSSFWorkbook workbook, CreationHelper creationHelper, Cell cell) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		DataFormatContext dataFormatContext = exportContext.getDataFormatContext();

		Object value = null;

		try
		{
			value = getValue(cn, rs, columnIndex, column.getType());
		}
		catch (Throwable t)
		{
			if (exportOption.isNullForIllegalColumnValue())
			{
				value = null;

				if (listener != null)
					listener.onSetNullTextValue(exportContext.getDataIndex(), column.getName(),
							wrapToDataExchangeException(t));
			}
			else
				throw t;
		}

		if (value == null)
		{
			cell.setBlank();
		}
		else if (value instanceof String)
		{
			cell.setCellValue((String) value);
		}
		else if (value instanceof BigDecimal || value instanceof BigInteger)
		{
			cell.setCellValue(value.toString());
		}
		else if (value instanceof Number)
		{
			Number number = (Number) value;

			if (dataFormatContext.isPureNumberPattern())
			{
				cell.setCellValue(number.doubleValue());
			}
			else
			{
				cell.setCellStyle(exportContext.getDateCellStyle(workbook, creationHelper));

				if (value instanceof Double || value instanceof Float)
					cell.setCellValue(dataFormatContext.formatDouble(number.doubleValue()));
				else
					cell.setCellValue(dataFormatContext.formatLong(number.longValue()));
			}
		}
		else if (value instanceof Date)
		{
			if (dataFormatContext.isPureDatePattern())
			{
				cell.setCellStyle(exportContext.getDateCellStyle(workbook, creationHelper));
				cell.setCellValue((Date) value);
			}
			else
			{
				cell.setCellStyle(exportContext.getDateCellStyle(workbook, creationHelper));
				cell.setCellValue(dataFormatContext.formatDate((Date) value));
			}
		}
		else if (value instanceof Time)
		{
			cell.setCellStyle(exportContext.getTimeCellStyle(workbook, creationHelper));
			cell.setCellValue(dataFormatContext.formatTime((Time) value));
		}
		else if (value instanceof Timestamp)
		{
			cell.setCellStyle(exportContext.getTimestampCellStyle(workbook, creationHelper));
			cell.setCellValue(dataFormatContext.formatTimestamp((Timestamp) value));
		}
		else if (value instanceof Boolean)
		{
			cell.setCellValue((Boolean) value);
		}
		else if (value instanceof byte[])
		{
			cell.setCellValue(dataFormatContext.formatBytes((byte[]) value));
		}
		else
		{
			cell.setCellValue(value.toString());
		}
	}

	protected static class ExcelDataExportContext extends IndexFormatDataExchangeContext
	{
		private CellStyle _dateCellStyle;
		private CellStyle _timeCellStyle;
		private CellStyle _timestampCellStyle;
		private CellStyle _numberCellStyle;

		public ExcelDataExportContext()
		{
			super();
		}

		public ExcelDataExportContext(ConnectionFactory connectionFactory, DataFormatContext dataFormatContext)
		{
			super(connectionFactory, dataFormatContext);
		}

		public CellStyle getDateCellStyle(Workbook workbook, CreationHelper creationHelper)
		{
			if (this._dateCellStyle == null)
			{
				this._dateCellStyle = workbook.createCellStyle();
				this._dateCellStyle.setDataFormat(
						creationHelper.createDataFormat().getFormat(getDataFormatContext().getDatePattern()));
			}

			return this._dateCellStyle;
		}

		public CellStyle getTimeCellStyle(Workbook workbook, CreationHelper creationHelper)
		{
			if (this._timeCellStyle == null)
			{
				this._timeCellStyle = workbook.createCellStyle();
				this._timeCellStyle.setDataFormat(
						creationHelper.createDataFormat().getFormat(getDataFormatContext().getTimePattern()));
			}

			return this._timeCellStyle;
		}

		public CellStyle getTimestampCellStyle(Workbook workbook, CreationHelper creationHelper)
		{
			if (this._timestampCellStyle == null)
			{
				this._timestampCellStyle = workbook.createCellStyle();
				this._timestampCellStyle.setDataFormat(
						creationHelper.createDataFormat().getFormat(getDataFormatContext().getTimestampPattern()));
			}

			return this._timestampCellStyle;
		}

		public CellStyle getNumberCellStyle(Workbook workbook, CreationHelper creationHelper)
		{
			if (this._numberCellStyle == null)
			{
				this._numberCellStyle = workbook.createCellStyle();
				this._numberCellStyle.setDataFormat(
						creationHelper.createDataFormat().getFormat(getDataFormatContext().getNumberPattern()));
			}

			return this._numberCellStyle;
		}
	}
}
