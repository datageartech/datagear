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
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.User;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.service.impl.SaveDtbsSourcePermissionDeniedException;
import org.datagear.management.util.GuardEntity;
import org.datagear.meta.Database;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.Table;
import org.datagear.meta.TableUtil;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public String add(HttpServletRequest request, Model model)
	{
		DtbsSource dtbsSource = createAdd(request, model);

		setFormModel(model, dtbsSource, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/dtbsSource/dtbsSource_form";
	}

	protected DtbsSource createAdd(HttpServletRequest request, Model model)
	{
		return new DtbsSource();
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, Model model, @RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		// 敏感信息较多，至少有编辑权限才允许复制
		DtbsSource entity = getByIdForEdit(getDtbsSourceService(), user, id);
		handleCopyFormModel(request, model, user, entity);

		setFormModel(model, entity, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE_ADD);
		return "/dtbsSource/dtbsSource_form";
	}

	protected void handleCopyFormModel(HttpServletRequest request, Model model, User user, DtbsSource entity)
			throws Exception
	{
		entity.setId(null);
		convertToFormModel(request, model, entity);
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource entity)
	{
		User user = getCurrentUser();

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity);

		if (re != null)
			return re;

		entity.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(entity, user);

		getDtbsSourceService().add(user, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		
		DtbsSource entity = getByIdForEdit(getDtbsSourceService(), user, id);
		convertToFormModel(request, model, entity);

		setFormModel(model, entity, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		return "/dtbsSource/dtbsSource_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource entity)
	{
		User user = getCurrentUser();

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity);

		if (re != null)
			return re;

		DtbsSource persist = getDtbsSourceService().getById(entity.getId());

		boolean updated = getDtbsSourceService().update(user, entity);

		// 如果URL或者用户变更了，则需要清除缓存
		if (updated && persist != null
				&& (!StringUtil.isEquals(entity.getUrl(), persist.getUrl())
						|| !StringUtil.isEquals(entity.getUser(), persist.getUser())
						|| !StringUtil.isEquals(entity.getSchemaName(), persist.getSchemaName())))
		{
			getDtbsSourceTableCache().invalidate(entity.getId());
		}

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		DtbsSource entity = getByIdForView(getDtbsSourceService(), user, id);
		convertToFormModel(request, model, entity);

		boolean hideSensitiveInfo = !Authorization.canEdit(entity.getDataPermission());

		if (hideSensitiveInfo)
			clearSensitiveInfo(entity);

		setFormModel(model, entity, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		model.addAttribute("hideSensitiveInfo", hideSensitiveInfo);

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

	@RequestMapping(value = "/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/dtbsSource/dtbsSource_tree";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, Model model)
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

		List<DtbsSource> items = getDtbsSourceService().query(user, pagingQuery);
		handleQueryData(request, items);

		return items;
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DtbsSource> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<DtbsSource> pagingData = getDtbsSourceService().pagingQuery(user, pagingQuery);
		handleQueryData(request, pagingData.getItems());

		return pagingData;
	}

	@RequestMapping(value = "/testConnection", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> testConnection(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSource entity) throws Exception
	{
		if (isBlank(entity.getTitle()) || isBlank(entity.getUrl()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		if (!this.dtbsSourceGuardService.isPermitted(user, new GuardEntity(entity)))
			throw new SaveDtbsSourcePermissionDeniedException();

		// 用户选定驱动程序时
		if (!isEmpty(entity.getDriverEntity()) && !isEmpty(entity.getDriverEntity().getId()))
		{
			DriverEntity driverEntity = this.driverEntityManager.get(entity.getDriverEntity().getId());
			entity.setDriverEntity(driverEntity);
		}

		Connection cn = null;

		try
		{
			cn = getDtbsSourceConnection(entity);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		return optSuccessDataResponseEntity(request, "dtbsSource.testConnection.ok");
	}

	@RequestMapping(value = "/{dtbsSourceId}/pagingQueryTable", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SimpleTable> pagingQueryTable(HttpServletRequest request, HttpServletResponse response,
			Model model, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody PagingQuery pagingQueryParam) throws Throwable
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<SimpleTable> tables = new ReturnDtbsSourceConnExecutor<List<SimpleTable>>(request, response, model,
				dtbsSourceId, true)
		{
			@Override
			protected List<SimpleTable> execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, DtbsSource dtbsSource) throws Throwable
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
			Model model, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = "reload", required = false) Boolean forceReload) throws Throwable
	{
		if (Boolean.TRUE.equals(forceReload))
			getDtbsSourceTableCache().invalidate(dtbsSourceId, tableName);

		ReturnDtbsSourceConnTableExecutor<Table> executor = new ReturnDtbsSourceConnTableExecutor<Table>(request,
				response,
				model, dtbsSourceId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, DtbsSource dtbsSource, Table table) throws Exception
			{
				return table;
			}
		};

		return executor.execute();
	}

	@RequestMapping("/{dtbsSourceId}/tableMeta/{tableName}")
	public String viewTableMeta(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @PathVariable("tableName") String tableName)
			throws Throwable
	{
		ReturnDtbsSourceConnTableExecutor<Table> executor = new ReturnDtbsSourceConnTableExecutor<Table>(request,
				response,
				model, dtbsSourceId, tableName, true)
		{
			@Override
			protected Table execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, DtbsSource dtbsSource, Table table) throws Exception
			{
				return table;
			}
		};
		
		Table table = executor.execute();
		
		setFormModel(model, table, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
		return "/dtbsSource/dtbsSource_dbtable_meta";
	}

	@RequestMapping("/dbinfo")
	public String viewDbMeta(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id) throws Throwable
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

		setFormModel(model, di, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		return "/dtbsSource/dtbsSource_dbinfo";
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, User user,
			DtbsSource entity)
	{
		if (isBlank(entity.getTitle()) || isBlank(entity.getUrl()))
			throw new IllegalInputException();

		return null;
	}
	
	protected void convertToFormModel(HttpServletRequest request, Model model, DtbsSource entity)
	{
		entity.clearPassword();
	}

	/**
	 * 处理查询结果。
	 * 
	 * @param request
	 * @param dtbsSources
	 */
	protected void handleQueryData(HttpServletRequest request, List<DtbsSource> dtbsSources)
	{
		if (dtbsSources != null && !dtbsSources.isEmpty())
		{
			for (DtbsSource dtbsSource : dtbsSources)
			{
				clearSensitiveInfo(dtbsSource);
			}
		}
	}

	protected void clearSensitiveInfo(DtbsSource entity)
	{
		if (entity == null)
			return;
		
		entity.clearSensitiveInfo();
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
