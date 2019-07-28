/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.InputStream;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
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

		InputStream in = getResource(dataExchange.getInputFactory(), importContext);
		POIFSFileSystem poifs = new POIFSFileSystem(in);
		InputStream din = poifs.createDocumentInputStream("Workbook");
		importContext.addContextCloseable(din);

		HSSFRequest req = new HSSFRequest();
		req.addListenerForAllRecords(new XlsEventListener());

		HSSFEventFactory factory = new HSSFEventFactory();
		factory.processEvents(req, din);
	}

	/**
	 * xls格式的Excel处理器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class XlsEventListener implements HSSFListener
	{
		private SSTRecord sstrec;

		public XlsEventListener()
		{
			super();
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
						System.out.println("Encountered workbook");
					}
					else if (bof.getType() == BOFRecord.TYPE_WORKSHEET)
					{
						System.out.println("Encountered sheet reference");
					}

					break;
				}
				case BoundSheetRecord.sid:
				{
					BoundSheetRecord bsr = (BoundSheetRecord) record;
					System.out.println("New sheet named: " + bsr.getSheetname());
					break;
				}
				case RowRecord.sid:
				{
					RowRecord rowrec = (RowRecord) record;
					System.out.println("Row found, first column at "
							+ rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
					break;
				}
				case NumberRecord.sid:
				{
					NumberRecord numrec = (NumberRecord) record;
					System.out.println("Cell found with value " + numrec.getValue()
							+ " at row " + numrec.getRow() + " and column " + numrec.getColumn());
					break;
					// SSTRecords store a array of unique strings used in Excel.
				}
				case SSTRecord.sid:
				{
					sstrec = (SSTRecord) record;
					for (int k = 0; k < sstrec.getNumUniqueStrings(); k++)
					{
						System.out.println("String table value " + k + " = " + sstrec.getString(k));
					}
					break;
				}
				case LabelSSTRecord.sid:
				{
					LabelSSTRecord lrec = (LabelSSTRecord) record;
					System.out.println("String cell found with value "
							+ sstrec.getString(lrec.getSSTIndex()));
					break;
				}
			}
		}
	}
}
