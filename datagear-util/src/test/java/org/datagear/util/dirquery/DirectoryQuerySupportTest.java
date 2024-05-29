/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.util.dirquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.datagear.util.FileUtil;
import org.datagear.util.query.PagingData;
import org.junit.Test;

/**
 * {@linkplain DirectoryQuerySupport}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryQuerySupportTest
{
	@Test
	public void queryTest()
	{
		DirectoryQuerySupport qs = new DirectoryQuerySupport(FileUtil.getFile("src/main/java/org/datagear/util"));

		{
			DirectoryQuery query = new DirectoryQuery();
			List<ResultFileInfo> fileInfos = qs.query(query);

			assertTrue(fileInfos.size() > 0);
		}

		// 包含
		{
			DirectoryQuery query = new DirectoryQuery("query", DirectoryQuery.nameAscOrder());
			List<ResultFileInfo> fileInfos = qs.query(query);

			assertTrue(fileInfos.size() == 3);
			assertEquals("dirquery", fileInfos.get(0).getName());
			assertEquals("query", fileInfos.get(1).getName());
		}

		// 开头
		{
			DirectoryQuery query = new DirectoryQuery("query*", DirectoryQuery.nameAscOrder());
			List<ResultFileInfo> fileInfos = qs.query(query);

			assertTrue(fileInfos.size() == 2);
		}

		// 结尾
		{
			DirectoryQuery query = new DirectoryQuery("*query", DirectoryQuery.nameAscOrder());
			List<ResultFileInfo> fileInfos = qs.query(query);

			assertTrue(fileInfos.size() == 2);
		}

		// 下级目录
		{
			DirectoryQuery query = new DirectoryQuery("query", DirectoryQuery.nameAscOrder(),
					DirectoryQuery.QUERY_RANGE_DESCENDANT);
			List<ResultFileInfo> fileInfos = qs.query(query);

			assertTrue(fileInfos.size() > 6);
			assertEquals("dirquery", fileInfos.get(0).getName());
			assertEquals("dirquery\\DirectoryPagingQuery.java", fileInfos.get(1).getName());
		}

		// 子目录内
		{
			DirectoryQuery query = new DirectoryQuery("query", DirectoryQuery.nameAscOrder(),
					DirectoryQuery.QUERY_RANGE_DESCENDANT);
			List<ResultFileInfo> fileInfos = qs.query(query, "dirquery");

			assertTrue(fileInfos.size() == 3);
			assertEquals("DirectoryPagingQuery.java", fileInfos.get(0).getName());
			assertEquals("DirectoryQuery.java", fileInfos.get(1).getName());
			assertEquals("DirectoryQuerySupport.java", fileInfos.get(2).getName());
		}
	}

	@Test
	public void pagingQueryTest()
	{
		DirectoryQuerySupport qs = new DirectoryQuerySupport(FileUtil.getFile("src/main/java/org/datagear/util"));

		// 子目录内
		{
			DirectoryPagingQuery query = new DirectoryPagingQuery(1, "query", DirectoryQuery.nameAscOrder());
			query.setPageSize(2);
			PagingData<ResultFileInfo> pd = qs.pagingQuery(query, "dirquery");
			List<ResultFileInfo> fileInfos = pd.getItems();

			assertTrue(fileInfos.size() == 2);
			assertEquals("DirectoryPagingQuery.java", fileInfos.get(0).getName());
			assertEquals("DirectoryQuery.java", fileInfos.get(1).getName());
		}
		{
			DirectoryPagingQuery query = new DirectoryPagingQuery(2, "query", DirectoryQuery.nameAscOrder());
			query.setPageSize(2);
			PagingData<ResultFileInfo> pd = qs.pagingQuery(query, "dirquery");
			List<ResultFileInfo> fileInfos = pd.getItems();

			assertTrue(fileInfos.size() == 1);
			assertEquals("DirectoryQuerySupport.java", fileInfos.get(0).getName());
		}
	}
}
