/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataType;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.analysis.support.SqlDataSetSupport;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.SqlDataSetEntityService;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.persistence.support.SqlSelectResult;
import org.datagear.util.IDUtil;
import org.datagear.util.SqlType;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据集控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysis/dataSet")
public class DataSetController extends AbstractSchemaConnController
{
	public static final int DEFAULT_SQL_RESULTSET_FETCH_SIZE = 20;

	public static final String DATA_SET_PROPERTY_LABELS_SPLITTER = ",";

	static
	{
		AuthorizationResourceMetas.registerForShare(SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE, "dataSet");
	}

	@Autowired
	private SqlDataSetEntityService sqlDataSetEntityService;

	@Autowired
	private SqlSelectManager sqlSelectManager;

	private SqlDataSetSupport sqlDataSetSupport = new SqlDataSetSupport();

	public DataSetController()
	{
		super();
	}

	public SqlDataSetEntityService getSqlDataSetEntityService()
	{
		return sqlDataSetEntityService;
	}

	public void setSqlDataSetEntityService(SqlDataSetEntityService sqlDataSetEntityService)
	{
		this.sqlDataSetEntityService = sqlDataSetEntityService;
	}

	public SqlSelectManager getSqlSelectManager()
	{
		return sqlSelectManager;
	}

	public void setSqlSelectManager(SqlSelectManager sqlSelectManager)
	{
		this.sqlSelectManager = sqlSelectManager;
	}

	public SqlDataSetSupport getSqlDataSetSupport()
	{
		return sqlDataSetSupport;
	}

	public void setSqlDataSetSupport(SqlDataSetSupport sqlDataSetSupport)
	{
		this.sqlDataSetSupport = sqlDataSetSupport;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		SqlDataSetEntity dataSet = new SqlDataSetEntity();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysis/dataSet/dataSet_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user);
		inflateDataSetProperties(request, dataSet);

		checkSaveEntity(dataSet);

		this.sqlDataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		SqlDataSetEntity dataSet = this.sqlDataSetEntityService.getByIdForEdit(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("dataSetPropertyLabelsText",
				DataSetProperty.concatLabels(dataSet.getProperties(), DATA_SET_PROPERTY_LABELS_SPLITTER));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.editDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysis/dataSet/dataSet_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		inflateDataSetProperties(request, dataSet);

		checkSaveEntity(dataSet);

		this.sqlDataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		SqlDataSetEntity dataSet = this.sqlDataSetEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("dataSetPropertyLabelsText",
				DataSetProperty.concatLabels(dataSet.getProperties(), DATA_SET_PROPERTY_LABELS_SPLITTER));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.viewDataSet");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/dataSet/dataSet_form";
	}

	@RequestMapping(value = "/getById", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public SqlDataSetEntity getById(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		SqlDataSetEntity dataSet = this.sqlDataSetEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		return dataSet;
	}

	@RequestMapping(value = "/getByIds", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<SqlDataSetEntity> getByIds(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String[] ids)
	{
		List<SqlDataSetEntity> dataSets = new ArrayList<>();

		if (!isEmpty(ids))
		{
			User user = WebUtils.getUser(request, response);

			for (String id : ids)
			{
				SqlDataSetEntity dataSet = this.sqlDataSetEntityService.getById(user, id);

				if (dataSet == null)
					throw new RecordNotFoundException();

				dataSets.add(dataSet);
			}
		}

		return dataSets;
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
	{
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.sqlDataSetEntityService.deleteById(user, id);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.manageDataSet");

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.selectDataSet");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SqlDataSetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<SqlDataSetEntity> pagingData = this.sqlDataSetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter());

		return pagingData;
	}

	@RequestMapping(value = "/sqlPreview/{schemaId}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public DataSetSqlSelectResult sqlPreview(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sql") final String sql, @RequestParam(value = "startRow", required = false) Integer startRow,
			@RequestParam(value = "fetchSize", required = false) Integer fetchSize,
			@RequestParam(value = "returnMeta", required = false) Boolean returnMeta) throws Throwable
	{
		DataSetSqlSelectResult result = executeSelect(request, response, springModel, schemaId, sql, startRow,
				fetchSize);

		if (!Boolean.TRUE.equals(returnMeta))
			result.setTable(null);

		return result;
	}

	protected DataSetSqlSelectResult executeSelect(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, String schemaId, final String sql, Integer startRow,
			Integer fetchSize) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		if (startRow == null)
			startRow = 1;
		if (fetchSize == null)
			fetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;

		if (fetchSize < 1)
			fetchSize = 1;
		if (fetchSize > 1000)
			fetchSize = 1000;

		final int startRowFinal = startRow;
		final int fetchSizeFinal = fetchSize;

		DataSetSqlSelectResult modelSqlResult = new ReturnSchemaConnExecutor<DataSetSqlSelectResult>(request, response,
				springModel, schemaId, true)
		{
			@Override
			protected DataSetSqlSelectResult execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				try
				{
					SqlSelectResult result = sqlSelectManager.select(getConnection(), sql, startRowFinal,
							fetchSizeFinal);
					List<DataSetProperty> dataSetProperties = resolveDataSetProperties(result.getTable(), null);

					return new DataSetSqlSelectResult(result, dataSetProperties);
				}
				catch (SQLException e)
				{
					throw new UserSQLException(e);
				}
			}
		}.execute();

		return modelSqlResult;
	}

	protected void inflateDataSetProperties(HttpServletRequest request, AbstractDataSet dataSet)
	{
		String labelsText = request.getParameter("dataSetPropertyLabelsText");
		if (labelsText == null)
			labelsText = "";

		String[] dataSetPropertyNames = request.getParameterValues("dataSetPropertyNames");
		String[] dataSetPropertyTypes = request.getParameterValues("dataSetPropertyTypes");
		String[] dataSetPropertyLabels = DataSetProperty.splitLabels(labelsText, DATA_SET_PROPERTY_LABELS_SPLITTER);

		if (dataSetPropertyNames == null)
			return;

		List<DataSetProperty> dataSetProperties = new ArrayList<>(dataSetPropertyNames.length);

		for (int i = 0; i < dataSetPropertyNames.length; i++)
		{
			DataSetProperty dataSetProperty = new DataSetProperty(dataSetPropertyNames[i],
					Enum.valueOf(DataType.class, dataSetPropertyTypes[i]));

			if (dataSetPropertyLabels != null && dataSetPropertyLabels.length > i)
				dataSetProperty.setLabel(dataSetPropertyLabels[i]);

			dataSetProperties.add(dataSetProperty);
		}

		dataSet.setProperties(dataSetProperties);
	}

	/**
	 * 解析{@linkplain DataSetProperty}列表。
	 * 
	 * @param table
	 * @param labels
	 *            {@linkplain DataSetProperty#getLabel()}数组，允许为{@code null}或任意长度的数组
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveDataSetProperties(Table table, String[] labels) throws Throwable
	{
		Column[] properties = table.getColumns();

		List<DataSetProperty> dataSetProperties = new ArrayList<>(properties == null ? 0 : properties.length);

		if (properties != null)
		{
			for (int i = 0; i < properties.length; i++)
			{
				Column column = properties[i];

				DataSetProperty dataSetProperty = new DataSetProperty(column.getName(),
						sqlDataSetSupport.toDataType(new SqlType(column.getType(), column.getTypeName())));

				if (labels != null && labels.length > i)
					dataSetProperty.setLabel(labels[i]);

				dataSetProperties.add(dataSetProperty);
			}
		}

		return dataSetProperties;
	}

	protected void checkSaveEntity(SqlDataSetEntity dataSet)
	{
		if (isBlank(dataSet.getName()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema().getId()))
			throw new IllegalInputException();

		if (isBlank(dataSet.getSql()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getProperties()))
			throw new IllegalInputException();
	}

	public static class DataSetSqlSelectResult extends SqlSelectResult
	{
		private List<DataSetProperty> dataSetProperties;

		public DataSetSqlSelectResult()
		{
			super();
		}

		public DataSetSqlSelectResult(SqlSelectResult modelSqlResult, List<DataSetProperty> dataSetProperties)
		{
			super(modelSqlResult.getSql(), modelSqlResult.getTable(), modelSqlResult.getStartRow(),
					modelSqlResult.getFetchSize(), modelSqlResult.getRows());
			this.dataSetProperties = dataSetProperties;
		}

		public List<DataSetProperty> getDataSetProperties()
		{
			return dataSetProperties;
		}

		public void setDataSetProperties(List<DataSetProperty> dataSetProperties)
		{
			this.dataSetProperties = dataSetProperties;
		}
	}
}
