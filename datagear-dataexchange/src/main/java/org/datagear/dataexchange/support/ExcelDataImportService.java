/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DateUtil;
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
		req.addListenerForAllRecords(
				new MissingRecordAwareHSSFListener(new XlsEventListener(dataExchange, importContext, cn)));

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

		private List<String> _sheetNames = new ArrayList<String>();
		private int _sheetIndex = -1;
		private SSTRecord _sstRecord;
		private List<String> _columnNames = null;
		private int _rowIndex = 1;
		private List<Object> _columnValues = null;
		private List<ColumnInfo> _columnInfos = null;
		private List<ColumnInfo> _noNullColumnInfos = null;
		private PreparedStatement _statement = null;

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
			int cellRow = -1;
			int cellColumn = -1;
			Object cellValue = null;

			switch (record.getSid())
			{
				case BOFRecord.sid:
				{
					BOFRecord bofRecord = (BOFRecord) record;

					if (bofRecord.getType() == BOFRecord.TYPE_WORKBOOK)
					{
						this._sheetNames.clear();
						this._sheetIndex = -1;

						// System.out.println("encounter Workbook");
					}
					else if (bofRecord.getType() == BOFRecord.TYPE_WORKSHEET)
					{
						this._sheetIndex++;
						// System.out.println("Sheet index : " +
						// this._sheetIndex);
					}

					break;
				}
				case BoundSheetRecord.sid:
				{
					BoundSheetRecord sheet = (BoundSheetRecord) record;
					this._sheetNames.add(sheet.getSheetname());

					// System.out.println("Sheet : " + sheet.getSheetname());

					break;
				}
				case RowRecord.sid:
				{
					RowRecord rowRecord = (RowRecord) record;

					if (rowRecord.getRowNumber() == 0)
					{
						this._columnNames = createListWithNullElements(rowRecord.getLastCol());
						this._rowIndex = 1;
					}

					// System.out.println("Row found, first column at " +
					// rowRecord.getFirstCol() + " last column at "
					// + rowRecord.getLastCol());

					break;
				}
				case BlankRecord.sid:
				{
					BlankRecord blankRecord = (BlankRecord) record;

					cellRow = blankRecord.getRow();
					cellColumn = blankRecord.getColumn();
					cellValue = null;

					// System.out.println("blank cell [" + blankRecord.getRow()
					// + ", " + blankRecord.getColumn() + "]");

					break;
				}
				case NumberRecord.sid:
				{
					NumberRecord numberRecord = (NumberRecord) record;

					cellRow = numberRecord.getRow();
					cellColumn = numberRecord.getColumn();

					boolean isDate = false;

					if (this._columnInfos != null && cellColumn < this._columnInfos.size())
					{
						ColumnInfo columnInfo = this._columnInfos.get(cellColumn);
						if (columnInfo != null)
						{
							int sqlType = columnInfo.getType();

							if (Types.DATE == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType)
								isDate = true;
						}
					}

					if (isDate)
						cellValue = DateUtil.getJavaDate(numberRecord.getValue());
					else
						cellValue = numberRecord.getValue();

					// System.out.println("number cell [" +
					// numberRecord.getRow() + ", " + numberRecord.getColumn()
					// + "] :" + numberRecord.getValue());

					break;
				}
				case SSTRecord.sid:
				{
					_sstRecord = (SSTRecord) record;

					break;
				}
				case LabelRecord.sid:
				{
					LabelRecord labelRecord = (LabelRecord) record;

					cellRow = labelRecord.getRow();
					cellColumn = labelRecord.getColumn();
					cellValue = labelRecord.getValue();
				}
				case LabelSSTRecord.sid:
				{
					LabelSSTRecord labelSSTRecord = (LabelSSTRecord) record;

					cellRow = labelSSTRecord.getRow();
					cellColumn = labelSSTRecord.getColumn();
					cellValue = _sstRecord.getString(labelSSTRecord.getSSTIndex()).toString();

					// System.out.println("String cell [" +
					// labelSSTRecord.getRow() + ", " +
					// labelSSTRecord.getColumn()
					// + "] :" + cellValue);

					break;
				}
			}

			if (cellRow < 0)
				;
			else if (cellRow == 0)
			{
				this._columnNames.set(cellColumn, (cellValue == null ? "" : cellValue.toString()));
			}
			else
			{
				this._columnValues.set(cellColumn, cellValue);
			}

			if (record instanceof LastCellOfRowDummyRecord)
			{
				LastCellOfRowDummyRecord lastDummyRecord = (LastCellOfRowDummyRecord) record;
				int row = lastDummyRecord.getRow();

				// System.out.println("dummy cell [" + lastDummyRecord.getRow()
				// + ", " + "]");

				// 初始化列信息
				if (row == 0)
				{
					String tableName = (this.excelDataImport.hasUnifiedTable() ? this.excelDataImport.getUnifiedTable()
							: this._sheetNames.get(this._sheetIndex));
					this._columnInfos = ExcelDataImportService.this.getColumnInfos(this.connection, tableName,
							this._columnNames, this.excelDataImport.getImportOption().isIgnoreInexistentColumn());

					this._noNullColumnInfos = removeNullColumnInfos(this._columnInfos);

					String sql = buildInsertPreparedSqlUnchecked(this.connection, tableName, this._noNullColumnInfos);
					this._statement = createPreparedStatementUnchecked(this.connection, sql);
				}
				// 导入数据
				else
				{
					// 空行
					if (isAllElementsNull(this._columnValues))
						;
					else
					{
						List<Object> columnValues = removeNullColumnValues(this._columnInfos, this._noNullColumnInfos,
								this._columnValues);

						this.importContext.setDataIndex(ExcelDataIndex.valueOf(this._sheetIndex, this._rowIndex));

						ExcelDataImportService.this.importValueData(this.connection, this._statement,
								this._noNullColumnInfos, columnValues, this.importContext.getDataIndex(),
								this.excelDataImport.getImportOption().isNullForIllegalColumnValue(),
								this.excelDataImport.getImportOption().getExceptionResolve(),
								this.importContext.getDataFormatContext(), this.excelDataImport.getListener());
					}
				}

				this._columnValues = createListWithNullElements(this._columnNames.size());
				this._rowIndex++;
			}
		}

		protected <T> List<T> createListWithNullElements(int size)
		{
			List<T> list = new ArrayList<T>(size);

			for (int i = 0; i < size; i++)
				list.add(null);

			return list;
		}

		protected boolean isAllElementsNull(List<? extends Object> list)
		{
			if (list == null)
				return true;

			for (int i = 0, len = list.size(); i < len; i++)
			{
				if (list.get(i) != null)
					return false;
			}

			return true;
		}
	}
}
