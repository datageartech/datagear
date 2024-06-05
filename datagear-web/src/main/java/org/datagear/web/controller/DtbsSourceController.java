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
@RequestMapping("/dtbsSource")
public class DtbsSourceController extends AbstractDtbsSourceConnTableController
{
	public static final String COOKIE_PAGINATION_SIZE = "DTBSSOURCE_PAGING_SIZE";

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
		DtbsSource dtbsSource = new DtbsSource();
		setFormModel(model, dtbsSource, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/dtbsSource/dtbsSource_form";
	}

	@RequestMapping("/copy")
	public String copy(org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource dtbsSource = getByIdForView(getDtbsSourceService(), user, id);
		dtbsSource.setId(null);
		dtbsSource.clearPassword();
		dtbsSource.setDataPermission(DataPermissionEntity.PERMISSION_NOT_LOADED);
		
		setFormModel(model, dtbsSource, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE_ADD);
		return "/dtbsSource/dtbsSource_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource dtbsSource)
	{
		User user = getCurrentUser();

		if (isBlank(dtbsSource.getTitle()) || isBlank(dtbsSource.getUrl()))
			throw new IllegalInputException();

		dtbsSource.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(dtbsSource, user);

		getDtbsSourceService().add(user, dtbsSource);

		return optSuccessDataResponseEntity(request, dtbsSource);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource dtbsSource = getByIdForEdit(getDtbsSourceService(), user, id);
		dtbsSource.clearPassword();
		
		setFormModel(model, dtbsSource, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		return "/dtbsSource/dtbsSource_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource dtbsSource)
	{
		if (isBlank(dtbsSource.getTitle()) || isBlank(dtbsSource.getUrl()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		DtbsSource old = getDtbsSourceService().getById(dtbsSource.getId());

		boolean updated = getDtbsSourceService().update(user, dtbsSource);

		// 如果URL或者用户变更了，则需要清除缓存
		if (updated && old != null
				&& (!StringUtil.isEquals(dtbsSource.getUrl(), old.getUrl())
						|| !StringUtil.isEquals(dtbsSource.getUser(), old.getUser())))
			getDtbsSourceTableCache().invalidate(dtbsSource.getId());

		return optSuccessDataResponseEntity(request, dtbsSource);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		DtbsSource dtbsSource = getByIdForView(getDtbsSourceService(), user, id);
		dtbsSource.clearPassword();

		setFormModel(model, dtbsSource, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		return "/dtbsSource/dtbsSource_form";
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
		return "/dtbsSource/dtbsSource_tree";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/dtbsSource/dtbsSource_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSource> queryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<DtbsSource> dtbsSources = getDtbsSourceService().query(user, pagingQuery);
		processForUI(request, dtbsSources);

		return dtbsSources;
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
			@RequestBody DtbsSource dtbsSource) throws Exception
	{
		if (isBlank(dtbsSource.getTitle()) || isBlank(dtbsSource.getUrl()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		if (!this.dtbsSourceGuardService.isPermitted(user, new GuardEntity(dtbsSource)))
			throw new SaveDtbsSourcePermissionDeniedException();

		// 用户选定驱动程序时
		if (!isEmpty(dtbsSource.getDriverEntity()) && !isEmpty(dtbsSource.getDriverEntity().getId()))
		{
			DriverEntity driverEntity = this.driverEntityManager.get(dtbsSource.getDriverEntity().getId());
			dtbsSource.setDriverEntity(driverEntity);
		}

		Connection cn = null;

		try
		{
			cn = getDtbsSourceConnection(dtbsSource);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		return optSuccessDataResponseEntity(request, "dtbsSource.testConnection.ok");
	}

	@RequestMapping(value = "/list", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSource> list(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PagingQuery pagingQueryParam)
	{
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam, COOKIE_PAGINATION_SIZE);

		User user = getCurrentUser();

		pagingQuery.setOrders(Order.valueOf("title", Order.ASC));

		List<DtbsSource> dtbsSources = getDtbsSourceService().query(user, pagingQuery);
		processForUI(request, dtbsSources);

		return dtbsSources;
	}

	@RequestMapping(value = "/{dtbsSourceId}/pagingQueryTable", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SimpleTable> pagingQueryTable(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody PagingQuery pagingQueryParam) throws Throwable
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam, COOKIE_PAGINATION_SIZE);

		List<SimpleTable> tables = new ReturnDtbsSourceConnExecutor<List<SimpleTable>>(request, response, springModel,
				dtbsSourceId, true)
		{
			@Override
			protected List<SimpleTable> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable
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

	@RequestMapping(value = "/{dtbsSourceId}/table/{tableName}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Table getTable(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = "reload", required = false) Boolean forceReload) throws Throwable
	{
		if (Boolean.TRUE.equals(forceReload))
			getDtbsSourceTableCache().invalidate(dtbsSourceId, tableName);

		ReturnDtbsSourceConnTableExecutor<Table> executor = new ReturnDtbsSourceConnTableExecutor<Table>(request,
				response,
				springModel, dtbsSourceId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Exception
			{
				return table;
			}
		};

		return executor.execute();
	}

	@RequestMapping("/{dtbsSourceId}/tableMeta/{tableName}")
	public String viewTableMeta(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @PathVariable("tableName") String tableName)
			throws Throwable
	{
		ReturnDtbsSourceConnTableExecutor<Table> executor = new ReturnDtbsSourceConnTableExecutor<Table>(request,
				response,
				springModel, dtbsSourceId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource dtbsSource, Table table) throws Exception
			{
				return table;
			}
		};
		
		Table table = executor.execute();
		
		setFormModel(springModel, table, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
		return "/dtbsSource/dtbsSource_dbtable_meta";
	}

	@RequestMapping("/dbinfo")
	public String viewDbMeta(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestParam("id") String id) throws Throwable
	{
		User user = getCurrentUser();
		DtbsSource dtbsSource = getByIdForView(getDtbsSourceService(), user, id);

		DatabaseInfo di = new DatabaseInfo();

		Connection cn = null;
		try
		{
			cn = getDtbsSourceConnection(dtbsSource);
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

		return "/dtbsSource/dtbsSource_dbinfo";
	}
	
	/**
	 * 处理展示。
	 * 
	 * @param request
	 * @param dtbsSources
	 */
	protected void processForUI(HttpServletRequest request, List<DtbsSource> dtbsSources)
	{
		if (dtbsSources != null && !dtbsSources.isEmpty())
		{
			for (DtbsSource dtbsSource : dtbsSources)
			{
				// 清除密码，避免传输至客户端引起安全问题。
				dtbsSource.clearPassword();
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
