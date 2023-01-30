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

package org.datagear.web.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.derby.impl.sql.execute.ColumnInfo;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.meta.Column;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;
import org.datagear.meta.TableType;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SQL编辑器控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/sqlEditor")
public class SqlEditorController extends AbstractSchemaConnTableController
{
	public SqlEditorController()
	{
		super();
	}

	@RequestMapping(value = "/{schemaId}/findTableNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> findTableNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser();

		List<SimpleTable> tables = new ReturnSchemaConnExecutor<List<SimpleTable>>(request, response, springModel,
				schemaId, true)
		{
			@Override
			protected List<SimpleTable> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				List<SimpleTable> tables = getDbMetaResolver().getSimpleTables(cn);
				return TableType.filterUserDataTables(cn, getDbMetaResolver(), tables);
			}

		}.execute();

		SchemaController.sortByTableName(tables);

		List<SimpleTable> keywordTables = SchemaController.findByKeyword(tables, keyword);

		List<String> tableNames = new ArrayList<>();

		for (SimpleTable tableInfo : keywordTables)
			tableNames.add(tableInfo.getName());

		return tableNames;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{schemaId}/findColumns", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Column> findColumns(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("table") final String table,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser();

		Table tableObj = null;

		try
		{
			tableObj = new ReturnSchemaConnTableExecutor<Table>(request, response, springModel, schemaId, table, true)
			{
				@Override
				protected Table execute(HttpServletRequest request, HttpServletResponse response,
						org.springframework.ui.Model springModel, Schema schema, Table table) throws Exception
				{
					checkReadTableDataPermission(schema, user);

					return table;
				}
			}.execute();
		}
		catch (Throwable t)
		{
			// 避免出现TableNotFoundException导致界面出现错误提示
		}

		if (tableObj == null)
			return Collections.EMPTY_LIST;

		Column[] columns = tableObj.getColumns();
		
		List<Column> keywordColumns = findByKeyword(columns, keyword);
		//使用列定义顺序而非名称排序顺序更合适
		//Collections.sort(keywordColumns, COLUMN_SORT_BY_NAME_COMPARATOR);
		
		return keywordColumns;
	}

	/**
	 * 根据列名称关键字查询{@linkplain ColumnInfo}列表。
	 * 
	 * @param columnInfos
	 * @param columnNameKeyword
	 * @return
	 */
	public static List<Column> findByKeyword(Column[] columnInfos, String columnNameKeyword)
	{
		return KeywordMatcher.<Column> match(columnInfos, columnNameKeyword, new KeywordMatcher.MatchValue<Column>()
		{
			@Override
			public String[] get(Column t)
			{
				return new String[] { t.getName() };
			}
		});
	}

	public static Comparator<Column> COLUMN_SORT_BY_NAME_COMPARATOR = new Comparator<Column>()
	{
		@Override
		public int compare(Column o1, Column o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};
}
