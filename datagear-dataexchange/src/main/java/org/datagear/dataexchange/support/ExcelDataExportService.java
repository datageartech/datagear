/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataFormatContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ConnectionFactory;

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
			OutputStream out, ExcelDataExportContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		TextDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columnInfos.size();

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

				setCellValue(dataExchange, cn, rs, i + 1, columnInfo, exportOption, exportContext, wb, creationHelper,
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
	 * @param columnInfo
	 * @param exportOption
	 * @param exportContext
	 * @param workbook
	 * @param creationHelper
	 * @param cell
	 * @throws Throwable
	 */
	protected void setCellValue(ExcelDataExport dataExchange, Connection cn, ResultSet rs, int columnIndex,
			ColumnInfo columnInfo, TextDataExportOption exportOption, ExcelDataExportContext exportContext,
			SXSSFWorkbook workbook, CreationHelper creationHelper, Cell cell) throws Throwable
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
		else if (value instanceof BigDecimal || value instanceof BigInteger)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellValue(value.toString());
		}
		else if (value instanceof Number)
		{
			Number number = (Number) value;

			if (dataFormatContext.isPureNumberPattern())
			{
				cell.setCellType(CellType.NUMERIC);
				cell.setCellValue(number.doubleValue());
			}
			else
			{
				cell.setCellType(CellType.STRING);
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
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(exportContext.getDateCellStyle(workbook, creationHelper));
				cell.setCellValue((Date) value);
			}
			else
			{
				cell.setCellType(CellType.STRING);
				cell.setCellStyle(exportContext.getDateCellStyle(workbook, creationHelper));
				cell.setCellValue(dataFormatContext.formatDate((Date) value));
			}
		}
		else if (value instanceof Time)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(exportContext.getTimeCellStyle(workbook, creationHelper));
			cell.setCellValue(dataFormatContext.formatTime((Time) value));
		}
		else if (value instanceof Timestamp)
		{
			cell.setCellType(CellType.STRING);
			cell.setCellStyle(exportContext.getTimestampCellStyle(workbook, creationHelper));
			cell.setCellValue(dataFormatContext.formatTimestamp((Timestamp) value));
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
