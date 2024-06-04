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

package org.datagear.web.controller;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.User;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.service.impl.SaveDtbsSourcePermissionDeniedException;
import org.datagear.management.util.GuardEntity;
import org.datagear.meta.Database;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;
import org.datagear.meta.TableUtil;
import org.datagear.persistence.Order;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * {@linkplain DtbsSource}控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/schema")
public class DtbsSourceController extends AbstractDtbsSourceConnTableController
{
	public static final String COOKIE_PAGINATION_SIZE = "SCHEMA_PAGINATION_PAGE_SIZE";

	@Autowired
	private DriverEntityManager driverEntityManager;

	@Autowired
	private DtbsSourceGuardService dtbsSourceGuardService;

	public DtbsSourceController()
	{
		super();
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public DtbsSourceGuardService getDtbsSourceGuardService()
	{
		return dtbsSourceGuardService;
	}

	public void setDtbsSourceGuardService(DtbsSourceGuardService dtbsSourceGuardService)
	{
		this.dtbsSourceGuardService = dtbsSourceGuardService;
	}

	@RequestMapping("/add")
	public String add(org.springframework.ui.Model model)
	{
		DtbsSource schema = new DtbsSource();
		setFormModel(model, schema, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/schema/schema_form";
	}

	@RequestMapping("/copy")
	public String copy(org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource schema = getByIdForView(getDtbsSourceService(), user, id);
		schema.setId(null);
		schema.clearPassword();
		schema.setDataPermission(DataPermissionEntity.PERMISSION_NOT_LOADED);
		
		setFormModel(model, schema, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE_ADD);
		return "/schema/schema_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource schema)
	{
		User user = getCurrentUser();

		if (isBlank(schema.getTitle()) || isBlank(schema.getUrl()))
			throw new IllegalInputException();

		schema.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(schema, user);

		getDtbsSourceService().add(user, schema);

		return optSuccessDataResponseEntity(request, schema);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource schema = getByIdForEdit(getDtbsSourceService(), user, id);
		schema.clearPassword();
		
		setFormModel(model, schema, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		return "/schema/schema_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource schema)
	{
		if (isBlank(schema.getTitle()) || isBlank(schema.getUrl()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		DtbsSource old = getDtbsSourceService().getById(schema.getId());

		boolean updated = getDtbsSourceService().update(user, schema);

		// 如果URL或者用户变更了，则需要清除缓存
		if (updated && old != null
				&& (!StringUtil.isEquals(schema.getUrl(), old.getUrl()) || !StringUtil.isEquals(schema.getUser(), old.getUser())))
			getDtbsSourceTableCache().invalidate(schema.getId());

		return optSuccessDataResponseEntity(request, schema);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource schema = getByIdForView(getDtbsSourceService(), user, id);
		schema.clearPassword();

		setFormModel(model, schema, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		return "/schema/schema_form";
	}
	
	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = getCurrentUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];

			boolean deleted = getDtbsSourceService().deleteById(user, id);

			// 清除缓存
			if (deleted)
				getDtbsSourceTableCache().invalidate(id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/query")
	public String query(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyAction(model);
		return "/schema/schema_tree";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/schema/schema_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSource> queryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<DtbsSource> schemas = getDtbsSourceService().query(user, pagingQuery);
		processForUI(request, schemas);

		return schemas;
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DtbsSource> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<DtbsSource> pagingData = getDtbsSourceService().pagingQuery(user, pagingQuery);
		processForUI(request, pagingData.getItems());

		return pagingData;
	}

	@RequestMapping(value = "/testConnection", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> testConnection(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource schema) throws Exception
	{
		if (isBlank(schema.getTitle()) || isBlank(schema.getUrl()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		if (!this.dtbsSourceGuardService.isPermitted(user, new GuardEntity(schema)))
			throw new SaveDtbsSourcePermissionDeniedException();

		// 用户选定驱动程序时
		if (!isEmpty(schema.getDriverEntity()) && !isEmpty(schema.getDriverEntity().getId()))
		{
			DriverEntity driverEntity = this.driverEntityManager.get(schema.getDriverEntity().getId());
			schema.setDriverEntity(driverEntity);
		}

		Connection cn = null;

		try
		{
			cn = getSchemaConnection(schema);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		return optSuccessDataResponseEntity(request, "schema.testConnection.ok");
	}

	@RequestMapping(value = "/list", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSource> list(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PagingQuery pagingQueryParam)
	{
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam, COOKIE_PAGINATION_SIZE);

		User user = getCurrentUser();

		pagingQuery.setOrders(Order.valueOf("title", Order.ASC));

		List<DtbsSource> schemas = getDtbsSourceService().query(user, pagingQuery);
		processForUI(request, schemas);

		return schemas;
	}

	@RequestMapping(value = "/{schemaId}/pagingQueryTable", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SimpleTable> pagingQueryTable(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestBody PagingQuery pagingQueryParam) throws Throwable
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam, COOKIE_PAGINATION_SIZE);

		List<SimpleTable> tables = new ReturnSchemaConnExecutor<List<SimpleTable>>(request, response, springModel,
				schemaId, true)
		{
			@Override
			protected List<SimpleTable> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource schema) throws Throwable
			{
				Connection cn = getConnection();
				List<SimpleTable> tables = getDbMetaResolver().getDataTables(cn);
				return tables;
			}

		}.execute();

		TableUtil.sortAscByName(tables);

		List<SimpleTable> keywordTables = TableUtil.findTable(tables, pagingQuery.getKeyword());

		PagingData<SimpleTable> pagingData = new PagingData<>(pagingQuery.getPage(), keywordTables.size(),
				pagingQuery.getPageSize());

		keywordTables = keywordTables.subList(pagingData.getStartIndex(), pagingData.getEndIndex());

		pagingData.setItems(keywordTables);

		return pagingData;
	}

	@RequestMapping(value = "/{schemaId}/table/{tableName}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Table getTable(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = "reload", required = false) Boolean forceReload) throws Throwable
	{
		if (Boolean.TRUE.equals(forceReload))
			getDtbsSourceTableCache().invalidate(schemaId, tableName);

		ReturnSchemaConnTableExecutor<Table> executor = new ReturnSchemaConnTableExecutor<Table>(request, response,
				springModel, schemaId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource schema, Table table) throws Exception
			{
				return table;
			}
		};

		return executor.execute();
	}

	@RequestMapping("/{schemaId}/tableMeta/{tableName}")
	public String viewTableMeta(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName) throws Throwable
	{
		ReturnSchemaConnTableExecutor<Table> executor = new ReturnSchemaConnTableExecutor<Table>(request, response,
				springModel, schemaId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource schema, Table table) throws Exception
			{
				return table;
			}
		};
		
		Table table = executor.execute();
		
		setFormModel(springModel, table, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
		return "/schema/schema_dbtable_meta";
	}

	@RequestMapping("/dbinfo")
	public String viewDbMeta(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestParam("id") String id) throws Throwable
	{
		User user = getCurrentUser();
		DtbsSource schema = getByIdForView(getDtbsSourceService(), user, id);

		DatabaseInfo di = new DatabaseInfo();

		Connection cn = null;
		try
		{
			cn = getSchemaConnection(schema);
			Database db = getDbMetaResolver().getDatabase(cn);
			String[] tableTypes = getDbMetaResolver().getTableTypes(cn);

			// 不显示较为敏感的版本信息
			// di.setVersion(db.getProductVersion());
			// di.setDriverVersion(db.getDriverVersion());

			di.setName(db.getProductName());
			di.setDriverName(db.getDriverName());
			di.setTableTypes(tableTypes == null ? Collections.emptyList() : Arrays.asList(tableTypes));
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		setFormModel(springModel, di, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		return "/schema/schema_dbinfo";
	}
	
	/**
	 * 处理展示。
	 * 
	 * @param request
	 * @param schemas
	 */
	protected void processForUI(HttpServletRequest request, List<DtbsSource> schemas)
	{
		if (schemas != null && !schemas.isEmpty())
		{
			for (DtbsSource schema : schemas)
			{
				// 清除密码，避免传输至客户端引起安全问题。
				schema.clearPassword();
			}
		}
	}

	public static class DatabaseInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 数据库产品名 */
		private String name = null;

		private String version = null;

		/** 驱动名 */
		private String driverName = null;

		/** 驱动版本 */
		private String driverVersion = null;

		/** 表类型 */
		private List<String> tableTypes = null;

		public DatabaseInfo()
		{
			super();
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getVersion()
		{
			return version;
		}

		public void setVersion(String version)
		{
			this.version = version;
		}

		public String getDriverName()
		{
			return driverName;
		}

		public void setDriverName(String driverName)
		{
			this.driverName = driverName;
		}

		public String getDriverVersion()
		{
			return driverVersion;
		}

		public void setDriverVersion(String driverVersion)
		{
			this.driverVersion = driverVersion;
		}

		public List<String> getTableTypes()
		{
			return tableTypes;
		}

		public void setTableTypes(List<String> tableTypes)
		{
			this.tableTypes = tableTypes;
		}
	}
}
