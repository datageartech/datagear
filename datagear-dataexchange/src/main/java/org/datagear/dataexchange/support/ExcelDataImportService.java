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
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.IOUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

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

		if (dataExchange.isXls())
			importXls(dataExchange, importContext, cn);
		else
			importXlsx(dataExchange, importContext, cn);

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
	 * 导入{@code .xls}文件。
	 * 
	 * @param dataExchange
	 * @param importContext
	 * @param cn
	 * @throws Throwable
	 */
	protected void importXls(ExcelDataImport dataExchange, IndexFormatDataExchangeContext importContext, Connection cn)
			throws Throwable
	{
		POIFSFileSystem poifs = new POIFSFileSystem(dataExchange.getFile(), true);

		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(
				new MissingRecordAwareHSSFListener(new XlsEventListener(dataExchange, importContext, cn)));

		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processWorkbookEvents(req, poifs);
	}

	/**
	 * 导入{@code .xlsx}文件。
	 * 
	 * @param dataExchange
	 * @param importContext
	 * @param cn
	 * @throws Throwable
	 */
	protected void importXlsx(ExcelDataImport dataExchange, IndexFormatDataExchangeContext importContext, Connection cn)
			throws Throwable
	{
		OPCPackage opcPackage = OPCPackage.open(dataExchange.getFile(), PackageAccess.READ);
		importContext.addContextCloseable(opcPackage);

		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
		XSSFReader xssfReader = new XSSFReader(opcPackage);
		StylesTable styles = xssfReader.getStylesTable();

		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		int index = 0;

		while (iter.hasNext())
		{
			InputStream in = null;

			try
			{
				in = iter.next();

				String sheetName = iter.getSheetName();
				importXlsxSheet(strings, styles, sheetName, index, in);
			}
			finally
			{
				IOUtil.close(in);
			}

			index++;
		}
	}

	/**
	 * 导入{@code .xlsx}单个sheet。
	 * 
	 * @param sharedStringsTable
	 * @param stylesTable
	 * @param sheetName
	 * @param sheetIndex
	 * @param sheetInputStream
	 * @throws Throwable
	 */
	public void importXlsxSheet(ReadOnlySharedStringsTable sharedStringsTable, StylesTable stylesTable,
			String sheetName, int sheetIndex, InputStream sheetInputStream) throws Throwable
	{
		InputSource sheetSource = new InputSource(sheetInputStream);

		XMLReader sheetParser = SAXHelper.newXMLReader();
		ContentHandler handler = new XlsxSheetHandler(stylesTable, sharedStringsTable, sheetName, sheetIndex);
		sheetParser.setContentHandler(handler);
		sheetParser.parse(sheetSource);
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

	protected class XlsxSheetHandler extends DefaultHandler
	{
		private StylesTable stylesTable;
		private ReadOnlySharedStringsTable sharedStringsTable;
		private String sheetName;
		private int sheetIndex;

		private int _rowIndex = 0;
		private int _nextRowIndex = 0;

		private StringBuilder _lastContents = new StringBuilder();
		private boolean _nextIsString;

		public XlsxSheetHandler()
		{
			super();
		}

		public XlsxSheetHandler(StylesTable stylesTable, ReadOnlySharedStringsTable sharedStringsTable,
				String sheetName, int sheetIndex)
		{
			super();
			this.stylesTable = stylesTable;
			this.sharedStringsTable = sharedStringsTable;
			this.sheetName = sheetName;
			this.sheetIndex = sheetIndex;
		}

		public StylesTable getStylesTable()
		{
			return stylesTable;
		}

		public void setStylesTable(StylesTable stylesTable)
		{
			this.stylesTable = stylesTable;
		}

		public ReadOnlySharedStringsTable getSharedStringsTable()
		{
			return sharedStringsTable;
		}

		public void setSharedStringsTable(ReadOnlySharedStringsTable sharedStringsTable)
		{
			this.sharedStringsTable = sharedStringsTable;
		}

		public String getSheetName()
		{
			return sheetName;
		}

		public void setSheetName(String sheetName)
		{
			this.sheetName = sheetName;
		}

		public int getSheetIndex()
		{
			return sheetIndex;
		}

		public void setSheetIndex(int sheetIndex)
		{
			this.sheetIndex = sheetIndex;
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
		{
			if ("row".equals(name))
			{
				String rowIndexStr = attributes.getValue("r");
				if (rowIndexStr != null)
				{
					this._rowIndex = Integer.parseInt(rowIndexStr) - 1;
				}
				else
				{
					this._rowIndex = this._nextRowIndex;
				}
			}
			else if ("c".equals(name))
			{
				String cellType = attributes.getValue("t");
				if (cellType != null && cellType.equals("s"))
				{
					_nextIsString = true;
				}
				else
				{
					_nextIsString = false;
				}
			}

			if (this._lastContents.length() > 0)
				this._lastContents.delete(0, this._lastContents.length());

			System.out.println("Sheet [" + this.sheetName + "] start element :" + localName + ", " + name);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException
		{
			if ("row".equals(name))
			{
				this._nextRowIndex = this._rowIndex + 1;
			}

			if (_nextIsString)
			{
				int idx = Integer.parseInt(this._lastContents.toString());
				this._lastContents.delete(0, this._lastContents.length());
				this._lastContents.append(sharedStringsTable.getEntryAt(idx));

				_nextIsString = false;
			}

			if (name.equals("v"))
			{
				System.out.println("[row=" + this._rowIndex + "] : " + this._lastContents);
			}

			System.out.println("Sheet [" + this.sheetName + "] end element :" + localName + ", " + name);
		}

		@Override
		public void characters(char[] ch, int start, int length)
		{
			this._lastContents.append(new String(ch, start, length));
		}
	}
}
