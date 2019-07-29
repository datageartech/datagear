/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * Excel导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataImportService extends AbstractDevotedDbInfoAwareDataExchangeService<ExcelDataImport>
{
	public ExcelDataImportService()
	{
		super();
	}

	public ExcelDataImportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(ExcelDataImport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(ExcelDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext importContext = IndexFormatDataExchangeContext.cast(context);

		Connection cn = context.getConnection();
		cn.setAutoCommit(false);

		InputStream in = getResource(dataExchange.getInputFactory(), importContext);
		POIFSFileSystem poifs = new POIFSFileSystem(in);

		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(new XlsEventListener(dataExchange, importContext, cn));

		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, poifs);

		commit(cn);
	}

	@Override
	protected void onException(ExcelDataImport dataExchange, DataExchangeContext context, DataExchangeException e)
			throws DataExchangeException
	{
		processTransactionForDataExchangeException(context, e, dataExchange.getImportOption().getExceptionResolve());

		super.onException(dataExchange, context, e);
	}

	/**
	 * {@code .xls}格式的Excel处理器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class XlsEventListener implements HSSFListener
	{
		private ExcelDataImport excelDataImport;
		private IndexFormatDataExchangeContext importContext;
		private Connection connection;

		private String _sheetName;
		private int _sheetIndex = -1;
		private SSTRecord _sstRecord;
		private List<String> _columnNames = new ArrayList<String>();
		private List<Object> _columnValues = new ArrayList<Object>();
		private int _rowIndex = 1;
		private List<ColumnInfo> _columnInfos = null;
		private PreparedStatement _statement;

		public XlsEventListener()
		{
			super();
		}

		public XlsEventListener(ExcelDataImport excelDataImport, IndexFormatDataExchangeContext importContext,
				Connection connection)
		{
			super();
			this.excelDataImport = excelDataImport;
			this.importContext = importContext;
			this.connection = connection;
		}

		public ExcelDataImport getExcelDataImport()
		{
			return excelDataImport;
		}

		public void setExcelDataImport(ExcelDataImport excelDataImport)
		{
			this.excelDataImport = excelDataImport;
		}

		public IndexFormatDataExchangeContext getImportContext()
		{
			return importContext;
		}

		public void setImportContext(IndexFormatDataExchangeContext importContext)
		{
			this.importContext = importContext;
		}

		public Connection getConnection()
		{
			return connection;
		}

		public void setConnection(Connection connection)
		{
			this.connection = connection;
		}

		@Override
		public void processRecord(Record record)
		{
			switch (record.getSid())
			{
				case BOFRecord.sid:
				{
					BOFRecord bof = (BOFRecord) record;

					if (bof.getType() == BOFRecord.TYPE_WORKBOOK)
					{
					}
					else if (bof.getType() == BOFRecord.TYPE_WORKSHEET)
					{

					}

					break;
				}
				case BoundSheetRecord.sid:
				{
					BoundSheetRecord bsr = (BoundSheetRecord) record;
					this._sheetName = bsr.getSheetname();
					this._sheetIndex++;

					this._columnNames.clear();
					this._columnValues.clear();
					this._rowIndex = 1;
					this._columnInfos = null;

					break;
				}
				case RowRecord.sid:
				{
					break;
				}
				case BlankRecord.sid:
				{
					this._columnValues.add(null);

					break;
				}
				case NumberRecord.sid:
				{
					NumberRecord numrec = (NumberRecord) record;
					this._columnValues.add(numrec.getValue());

					break;
				}
				case SSTRecord.sid:
				{
					_sstRecord = (SSTRecord) record;

					break;
				}
				case LabelSSTRecord.sid:
				{
					LabelSSTRecord lrec = (LabelSSTRecord) record;

					String value = _sstRecord.getString(lrec.getSSTIndex()).toString();

					// 列名称
					if (lrec.getRow() == 0)
						this._columnNames.add(value);
					// 列值
					else
						this._columnValues.add(value);

					break;
				}
			}

			int columnNameSize = this._columnNames.size();

			if (columnNameSize > 0 && this._columnValues.size() == columnNameSize)
			{
				if (this._columnInfos == null)
				{
					String tableName = (this.excelDataImport.hasUnifiedTable() ? this.excelDataImport.getUnifiedTable()
							: this._sheetName);
					this._columnInfos = ExcelDataImportService.this.getColumnInfos(this.connection, tableName,
							this._columnNames, false);

					String sql = buildInsertPreparedSqlUnchecked(this.connection, tableName, this._columnInfos);
					this._statement = createPreparedStatementUnchecked(this.connection, sql);
				}

				this.importContext.setDataIndex(ExcelDataIndex.valueOf(this._sheetIndex, this._rowIndex));

				ExcelDataImportService.this.importValueData(this.connection, this._statement, this._columnInfos,
						_columnValues, this.importContext.getDataIndex(),
						this.excelDataImport.getImportOption().isNullForIllegalColumnValue(),
						this.excelDataImport.getImportOption().getExceptionResolve(),
						this.importContext.getDataFormatContext(), this.excelDataImport.getListener());

				this._columnValues.clear();

				this._rowIndex++;
			}
		}
	}
}
