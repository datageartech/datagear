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

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

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
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.meta.Column;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
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
public class ExcelDataImportService extends AbstractDevotedDBMetaDataExchangeService<ExcelDataImport>
{
	public ExcelDataImportService()
	{
		super();
	}

	public ExcelDataImportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
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
		JdbcUtil.setAutoCommitIfSupports(cn, false);
		JdbcUtil.setReadonlyIfSupports(cn, false);

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
				importXlsxSheet(dataExchange, importContext, cn, strings, styles, sheetName, index, in);
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
	 * @param dataExchange
	 * @param importContext
	 * @param cn
	 * @param sharedStringsTable
	 * @param stylesTable
	 * @param sheetName
	 * @param sheetIndex
	 * @param sheetInputStream
	 * @throws Throwable
	 */
	public void importXlsxSheet(ExcelDataImport dataExchange, IndexFormatDataExchangeContext importContext,
			Connection cn, ReadOnlySharedStringsTable sharedStringsTable, StylesTable stylesTable, String sheetName,
			int sheetIndex, InputStream sheetInputStream) throws Throwable
	{
		InputSource sheetSource = new InputSource(sheetInputStream);

		XMLReader sheetParser = XMLHelper.newXMLReader();
		ContentHandler handler = new XlsxSheetHandler(dataExchange, importContext, cn, stylesTable, sharedStringsTable,
				sheetName, sheetIndex);
		sheetParser.setContentHandler(handler);
		sheetParser.parse(sheetSource);
	}

	protected <T> List<T> createListWithNullElements(int size)
	{
		List<T> list = new ArrayList<>(size);

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

	protected <T> void setElementWithExpand(List<? super T> list, int index, T element)
	{
		int expandCount = index - list.size() + 1;
		for (int i = 0; i < expandCount; i++)
			list.add(null);

		list.set(index, element);
	}

	/**
	 * {@code .xls}格式的Excel处理器。
	 * <p>
	 * 注意：xls格式的Record记录事件顺序为：全部BoundSheetRecord -> 全部RowRecord -> 全部cell记录
	 * </p>
	 */
	protected class XlsEventListener implements HSSFListener
	{
		private ExcelDataImport excelDataImport;
		private IndexFormatDataExchangeContext importContext;
		private Connection connection;

		// 存储所有sheet列表，因为processRecord先处理完所有BoundSheetRecord，再处理其他
		private List<String> _sheetNames = new ArrayList<>();
		// 当前sheet索引
		private int _sheetIndex = -1;
		// 当前行索引
		private int _rowIndex = 1;
		private SSTRecord _sstRecord;
		private List<String> _columnNames = null;
		private List<Object> _columnValues = null;
		private List<Column> _columns = null;
		private List<Column> _noNullColumns = null;
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

					if (this._columns != null && cellColumn < this._columns.size())
					{
						Column column = this._columns.get(cellColumn);
						if (column != null)
						{
							int sqlType = column.getType();

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
					this._columns = ExcelDataImportService.this.getColumns(this.connection, tableName,
							this._columnNames, this.excelDataImport.getImportOption().isIgnoreInexistentColumn());

					this._noNullColumns = removeNullColumns(this._columns);

					// 表不匹配
					if (this._noNullColumns == null || this._noNullColumns.isEmpty())
						throw new TableMismatchException(tableName);

					String sql = buildInsertPreparedSqlUnchecked(this.connection, tableName, this._noNullColumns);
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
						List<Object> columnValues = removeNullColumnValues(this._columns, this._noNullColumns,
								this._columnValues);

						this.importContext.setDataIndex(ExcelDataIndex.valueOf(this._sheetIndex, this._rowIndex));

						ExcelDataImportService.this.importValueData(this.connection, this._statement,
								this._noNullColumns, columnValues, this.importContext.getDataIndex(),
								this.excelDataImport.getImportOption().isNullForIllegalColumnValue(),
								this.excelDataImport.getImportOption().getExceptionResolve(),
								this.importContext.getDataFormatContext(), this.excelDataImport.getListener());
					}
				}

				this._columnValues = createListWithNullElements(this._columnNames.size());
				this._rowIndex++;
			}
		}
	}

	/**
	 * 此类参考自{@code org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler}。
	 * <p>
	 * 注意：xlsx格式的单元格可能有两种格式（将文件改为zip后解压缩可见）：
	 * </p>
	 * <p>
	 * &lt;c&gt;&lt;v&gt;......&lt;/v&gt;&lt;/c&gt;
	 * </p>
	 * 或者
	 * <p>
	 * &lt;c&gt;&lt;is&gt;&lt;t&gt;......&lt;/t&gt;&lt;/is&gt;&lt;/c&gt;
	 * </p>
	 */
	protected class XlsxSheetHandler extends DefaultHandler
	{
		private ExcelDataImport excelDataImport;
		private IndexFormatDataExchangeContext importContext;
		private Connection connection;

		private StylesTable stylesTable;
		private ReadOnlySharedStringsTable sharedStringsTable;
		private String sheetName;
		private int sheetIndex;

		// 当前行索引
		private int _rowIndex = 0;
		// 备用行索引
		private int _nextRowIndex = 0;
		// 当前单元格索引
		private int _cellIndex = 0;
		// 当前单元格内容构建器
		private StringBuilder _cellContents = new StringBuilder();
		// 当前单元格类型
		private XssfCellType _cellType = XssfCellType.NUMBER;

		// 是否在单元格内容元素内
		private boolean _inCellContentElement = false;
		// 是否在<is>元素内
		private boolean _inIsElement = false;

		// 数据库列名称
		private List<String> _columnNames = new ArrayList<>();
		// 当前行的数据库列值
		private List<Object> _columnValues = new ArrayList<>();
		private List<Column> _columns = null;
		private List<Column> _noNullColumns = null;
		private PreparedStatement _statement = null;

		public XlsxSheetHandler()
		{
			super();
		}

		public XlsxSheetHandler(ExcelDataImport excelDataImport, IndexFormatDataExchangeContext importContext,
				Connection connection, StylesTable stylesTable, ReadOnlySharedStringsTable sharedStringsTable,
				String sheetName, int sheetIndex)
		{
			super();
			this.excelDataImport = excelDataImport;
			this.importContext = importContext;
			this.connection = connection;
			this.stylesTable = stylesTable;
			this.sharedStringsTable = sharedStringsTable;
			this.sheetName = sheetName;
			this.sheetIndex = sheetIndex;
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
			if (uri != null && !uri.equals(NS_SPREADSHEETML))
				return;

			if (isCellContentElement(localName))
			{
				_inCellContentElement = true;

				if (this._cellContents.length() > 0)
					this._cellContents.delete(0, this._cellContents.length());
			}
			else if ("is".equals(localName))
			{
				_inIsElement = true;
			}
			else if ("row".equals(name))
			{
				String rowIndexStr = attributes.getValue("r");

				if (rowIndexStr != null)
					this._rowIndex = Integer.parseInt(rowIndexStr) - 1;
				else
					this._rowIndex = this._nextRowIndex;

				if (this._rowIndex == 0)
					this._columnNames.clear();
				else
					this._columnValues.clear();

				// println("start row : " + this._rowIndex + "-------------");
			}
			else if ("c".equals(name))
			{
				String cellType = attributes.getValue("t");
				String cellRef = attributes.getValue("r");

				if ("b".equals(cellType))
					this._cellType = XssfCellType.BOOLEAN;
				else if ("e".equals(cellType))
					this._cellType = XssfCellType.ERROR;
				else if ("inlineStr".equals(cellType))
					this._cellType = XssfCellType.INLINE_STRING;
				else if ("s".equals(cellType))
					this._cellType = XssfCellType.SST_STRING;
				else if ("str".equals(cellType))
					this._cellType = XssfCellType.FORMULA;
				else
				{
					this._cellType = XssfCellType.NUMBER;
				}

				CellReference cellReference = new CellReference(cellRef);
				this._cellIndex = cellReference.getCol();
			}
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException
		{
			if (uri != null && !uri.equals(NS_SPREADSHEETML))
				return;

			// 单元格内容
			if (isCellContentElement(localName))
			{
				_inCellContentElement = false;

				Object value = null;

				if (XssfCellType.BOOLEAN.equals(this._cellType))
				{
					String content = this._cellContents.toString();
					value = ("true".equalsIgnoreCase(content) || "1".equals(content) || "on".equalsIgnoreCase(content));
				}
				else if (XssfCellType.NUMBER.equals(this._cellType))
				{
					String content = this._cellContents.toString();
					Double cv = (content.isEmpty() ? null : Double.parseDouble(content));

					boolean isDate = false;

					if (this._columns != null && this._cellIndex < this._columns.size())
					{
						Column column = this._columns.get(this._cellIndex);
						if (column != null)
						{
							int sqlType = column.getType();

							if (Types.DATE == sqlType || Types.TIME == sqlType || Types.TIMESTAMP == sqlType)
								isDate = true;
						}
					}

					if (isDate && cv != null)
						value = DateUtil.getJavaDate(cv);
					else
						value = cv;
				}
				else if (XssfCellType.INLINE_STRING.equals(this._cellType))
				{
					XSSFRichTextString rtsi = new XSSFRichTextString(this._cellContents.toString());
					value = rtsi.toString();
				}
				else if (XssfCellType.SST_STRING.equals(this._cellType))
				{
					String sstIndex = this._cellContents.toString();
					int idx = Integer.parseInt(sstIndex);
					RichTextString rtss = sharedStringsTable.getItemAt(idx);

					value = rtss.toString();
				}
				else
				{
					int idx = Integer.parseInt(this._cellContents.toString());
					value = sharedStringsTable.getItemAt(idx);
				}

				if (this._rowIndex == 0)
					setElementWithExpand(this._columnNames, this._cellIndex, value.toString());
				else
					setElementWithExpand(this._columnValues, this._cellIndex, value);

				// println("cell [" + this._rowIndex + ", " + this._cellIndex +
				// "] value : " + value);
			}
			else if ("is".equals(localName))
			{
				_inIsElement = false;
			}
			else if ("row".equals(name))
			{
				// 初始化列信息
				if (this._rowIndex == 0)
				{
					String tableName = (this.excelDataImport.hasUnifiedTable() ? this.excelDataImport.getUnifiedTable()
							: this.sheetName);
					this._columns = ExcelDataImportService.this.getColumns(this.connection, tableName,
							this._columnNames, this.excelDataImport.getImportOption().isIgnoreInexistentColumn());

					this._noNullColumns = removeNullColumns(this._columns);

					// 表不匹配
					if (this._noNullColumns == null || this._noNullColumns.isEmpty())
						throw new TableMismatchException(tableName);

					String sql = buildInsertPreparedSqlUnchecked(this.connection, tableName, this._noNullColumns);
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
						List<Object> columnValues = removeNullColumnValues(this._columns, this._noNullColumns,
								this._columnValues);

						this.importContext.setDataIndex(ExcelDataIndex.valueOf(this.sheetIndex, this._rowIndex));

						ExcelDataImportService.this.importValueData(this.connection, this._statement,
								this._noNullColumns, columnValues, this.importContext.getDataIndex(),
								this.excelDataImport.getImportOption().isNullForIllegalColumnValue(),
								this.excelDataImport.getImportOption().getExceptionResolve(),
								this.importContext.getDataFormatContext(), this.excelDataImport.getListener());
					}
				}

				this._nextRowIndex = this._rowIndex + 1;

				// println("end row :" + this._rowIndex + "-------------");
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		{
			if (_inCellContentElement)
				this._cellContents.append(new String(ch, start, length));
		}

		/**
		 * 参考{@code org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.isTextTag(String)}。
		 * 
		 * @param name
		 * @return
		 */
		protected boolean isCellContentElement(String name)
		{
			if ("v".equals(name))
				return true;
			if ("inlineStr".equals(name))
				return true;
			if ("t".equals(name) && _inIsElement)
				return true;
			else
				return false;
		}

		// protected void println(String s)
		// {
		// System.out.println(s);
		// }
	}

	/**
	 * xlsx单元格类型，参考{@code org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.xssfDataType}。
	 */
	protected static enum XssfCellType
	{
		BOOLEAN, ERROR, FORMULA, INLINE_STRING, SST_STRING, NUMBER,
	}
}
