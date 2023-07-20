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

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ResourceFactory;
import org.datagear.util.resource.SimpleConnectionFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain JsonDataImportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataImportServiceTest extends DataexchangeTestSupport
{
	private JsonDataImportService jsonDataImportService;

	public JsonDataImportServiceTest()
	{
		super();
		this.jsonDataImportService = new JsonDataImportService(dbMetaResolver);
	}

	@Test
	public void exchangeTest_row_array() throws Throwable
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/JsonDataImportServiceTest_row_array.json");

			JsonDataImportOption valueDataImportOption = new JsonDataImportOption(ExceptionResolve.ABORT, false, false,
					true,
					JsonDataFormat.ROW_ARRAY);

			JsonDataImport impt = new JsonDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_DATA_IMPORT, readerFactory);

			clearTable(cn, TABLE_NAME_DATA_IMPORT);

			this.jsonDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME_DATA_IMPORT);

			Assert.assertEquals(3, count);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	@Test
	public void exchangeTest_table_object() throws Throwable
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/JsonDataImportServiceTest_table_object.json");

			JsonDataImportOption valueDataImportOption = new JsonDataImportOption(ExceptionResolve.ABORT, false, false,
					true, JsonDataFormat.TABLE_OBJECT);

			JsonDataImport impt = new JsonDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, readerFactory);

			clearTable(cn, TABLE_NAME_DATA_IMPORT);
			clearTable(cn, TABLE_NAME_DATA_EXPORT);

			this.jsonDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME_DATA_IMPORT);
			int count1 = getCount(cn, TABLE_NAME_DATA_EXPORT);

			Assert.assertEquals(3, count);
			Assert.assertEquals(4, count1);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}
}
