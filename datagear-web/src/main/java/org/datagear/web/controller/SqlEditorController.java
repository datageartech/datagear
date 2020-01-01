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

import org.datagear.connection.ConnectionSource;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
public class SqlEditorController extends AbstractSchemaConnController
{
	@Autowired
	private DatabaseInfoResolver databaseInfoResolver;

	public SqlEditorController()
	{
		super();
	}

	public SqlEditorController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource, DatabaseInfoResolver databaseInfoResolver)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	@RequestMapping(value = "/{schemaId}/findTableNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> findTableNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		TableInfo[] tableInfos = new ReturnSchemaConnExecutor<TableInfo[]>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected TableInfo[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				return getDatabaseInfoResolver().getTableInfos(getConnection());
			}
		}.execute();

		List<TableInfo> tableInfoList = SchemaController.findByKeyword(tableInfos, keyword);
		Collections.sort(tableInfoList, SchemaController.TABLE_INFO_SORT_BY_NAME_COMPARATOR);

		List<String> tableNames = new ArrayList<String>();

		for (TableInfo tableInfo : tableInfoList)
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

		ColumnInfo[] columnInfos = new ReturnSchemaConnExecutor<ColumnInfo[]>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected ColumnInfo[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				return getDatabaseInfoResolver().getColumnInfos(getConnection(), table);
			}
		}.execute();

		List<ColumnInfo> columnInfoList = findByKeyword(columnInfos, keyword);
		Collections.sort(columnInfoList, COLUMNINFO_INFO_SORT_BY_NAME_COMPARATOR);

		List<String> columnNames = new ArrayList<String>();

		for (ColumnInfo columnInfo : columnInfoList)
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
	public static List<ColumnInfo> findByKeyword(ColumnInfo[] columnInfos, String columnNameKeyword)
	{
		return KeywordMatcher.<ColumnInfo> match(columnInfos, columnNameKeyword,
				new KeywordMatcher.MatchValue<ColumnInfo>()
				{
					@Override
					public String[] get(ColumnInfo t)
					{
						return new String[] { t.getName() };
					}
				});
	}

	public static Comparator<ColumnInfo> COLUMNINFO_INFO_SORT_BY_NAME_COMPARATOR = new Comparator<ColumnInfo>()
	{
		@Override
		public int compare(ColumnInfo o1, ColumnInfo o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};
}
