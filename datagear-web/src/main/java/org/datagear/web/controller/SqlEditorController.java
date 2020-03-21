/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

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
		final User user = WebUtils.getUser(request, response);

		List<SimpleTable> tables = new ReturnSchemaConnExecutor<List<SimpleTable>>(request, response, springModel,
				schemaId, true)
		{
			@Override
			protected List<SimpleTable> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);
				return getDbMetaResolver().getSimpleTables(getConnection());
			}

		}.execute();

		List<SimpleTable> keywordTables = SchemaController.findByKeyword(tables, keyword);
		Collections.sort(keywordTables, SchemaController.TABLE_SORT_BY_NAME_COMPARATOR);

		List<String> tableNames = new ArrayList<>();

		for (SimpleTable tableInfo : keywordTables)
			tableNames.add(tableInfo.getName());

		return tableNames;
	}

	@RequestMapping(value = "/{schemaId}/findColumnNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> findColumnNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("table") final String table,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		Table tableObj = new ReturnSchemaConnTableExecutor<Table>(request, response, springModel, schemaId, table, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Exception
			{
				checkReadTableDataPermission(schema, user);

				return table;
			}
		}.execute();

		Column[] columns = tableObj.getColumns();

		List<Column> keywordColumns = findByKeyword(columns, keyword);
		Collections.sort(keywordColumns, COLUMN_SORT_BY_NAME_COMPARATOR);

		List<String> columnNames = new ArrayList<>();

		for (Column columnInfo : keywordColumns)
			columnNames.add(columnInfo.getName());

		return columnNames;
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
