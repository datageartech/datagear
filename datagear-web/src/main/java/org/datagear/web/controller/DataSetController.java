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
import org.datagear.connection.ConnectionSource;
import org.datagear.dbmodel.DatabaseModelResolver;
import org.datagear.dbmodel.ModelSqlSelectService;
import org.datagear.dbmodel.ModelSqlSelectService.ModelSqlResult;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlDataSetEntityService;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.features.ColumnName;
import org.datagear.persistence.features.JdbcType;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
	private ModelSqlSelectService modelSqlSelectService;

	@Autowired
	private DatabaseModelResolver databaseModelResolver;

	private SqlDataSetSupport sqlDataSetSupport = new SqlDataSetSupport();

	public DataSetController()
	{
		super();
	}

	public DataSetController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource,
			SqlDataSetEntityService sqlDataSetEntityService, ModelSqlSelectService modelSqlSelectService,
			DatabaseModelResolver databaseModelResolver)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.sqlDataSetEntityService = sqlDataSetEntityService;
		this.modelSqlSelectService = modelSqlSelectService;
		this.databaseModelResolver = databaseModelResolver;

	}

	public SqlDataSetEntityService getSqlDataSetEntityService()
	{
		return sqlDataSetEntityService;
	}

	public void setSqlDataSetEntityService(SqlDataSetEntityService sqlDataSetEntityService)
	{
		this.sqlDataSetEntityService = sqlDataSetEntityService;
	}

	public ModelSqlSelectService getModelSqlSelectService()
	{
		return modelSqlSelectService;
	}

	public void setModelSqlSelectService(ModelSqlSelectService modelSqlSelectService)
	{
		this.modelSqlSelectService = modelSqlSelectService;
	}

	public DatabaseModelResolver getDatabaseModelResolver()
	{
		return databaseModelResolver;
	}

	public void setDatabaseModelResolver(DatabaseModelResolver databaseModelResolver)
	{
		this.databaseModelResolver = databaseModelResolver;
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

		dataSet.setId(IDUtil.uuid());
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
		List<SqlDataSetEntity> dataSets = new ArrayList<SqlDataSetEntity>();

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
		model.addAttribute(KEY_SELECTONLY, true);
		setIsMultipleSelectAttribute(request, model);

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SqlDataSetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		PagingQuery pagingQuery = getPagingQuery(request, WebUtils.COOKIE_PAGINATION_SIZE);
		String dataFilter = getDataFilterValue(request);

		PagingData<SqlDataSetEntity> pagingData = this.sqlDataSetEntityService.pagingQuery(user, pagingQuery,
				dataFilter);

		return pagingData;
	}

	@RequestMapping(value = "/sqlPreview/{schemaId}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public DataSetModelSqlResult sqlPreview(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sql") final String sql, @RequestParam(value = "startRow", required = false) Integer startRow,
			@RequestParam(value = "fetchSize", required = false) Integer fetchSize,
			@RequestParam(value = "returnModel", required = false) Boolean returnModel) throws Throwable
	{
		DataSetModelSqlResult modelSqlResult = executeSelect(request, response, springModel, schemaId, sql, startRow,
				fetchSize);

		if (!Boolean.TRUE.equals(returnModel))
			modelSqlResult.setModel(null);

		return modelSqlResult;
	}

	protected DataSetModelSqlResult executeSelect(HttpServletRequest request, HttpServletResponse response,
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

		DataSetModelSqlResult modelSqlResult = new ReturnSchemaConnExecutor<DataSetModelSqlResult>(request, response,
				springModel, schemaId, true)
		{
			@Override
			protected DataSetModelSqlResult execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				try
				{
					ModelSqlResult modelSqlResult = modelSqlSelectService.select(getConnection(), sql, startRowFinal,
							fetchSizeFinal, databaseModelResolver);

					List<DataSetProperty> dataSetProperties = resolveDataSetProperties(modelSqlResult.getModel(), null);

					return new DataSetModelSqlResult(modelSqlResult, dataSetProperties);
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

		List<DataSetProperty> dataSetProperties = new ArrayList<DataSetProperty>(dataSetPropertyNames.length);

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
	 * @param model
	 * @param labels
	 *            {@linkplain DataSetProperty#getLabel()}数组，允许为{@code null}或任意长度的数组
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveDataSetProperties(Model model, String[] labels) throws Throwable
	{
		Property[] properties = model.getProperties();

		List<DataSetProperty> dataSetProperties = new ArrayList<DataSetProperty>(
				properties == null ? 0 : properties.length);

		if (properties != null)
		{
			for (int i = 0; i < properties.length; i++)
			{
				Property property = properties[i];

				ColumnName columnName = property.getFeature(ColumnName.class);

				if (columnName == null)
					throw new UnsupportedOperationException("Column name can not be resolved");

				JdbcType jdbcType = property.getFeature(JdbcType.class);

				if (jdbcType == null)
					throw new UnsupportedOperationException("Jdbc type can not be resolved");

				DataSetProperty dataSetProperty = new DataSetProperty(columnName.getValue(),
						sqlDataSetSupport.toDataType(jdbcType.getValue()));

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

	public static class DataSetModelSqlResult extends ModelSqlResult
	{
		private List<DataSetProperty> dataSetProperties;

		public DataSetModelSqlResult()
		{
			super();
		}

		public DataSetModelSqlResult(ModelSqlResult modelSqlResult, List<DataSetProperty> dataSetProperties)
		{
			super(modelSqlResult.getSql(), modelSqlResult.getModel(), modelSqlResult.getStartRow(),
					modelSqlResult.getFetchSize(), modelSqlResult.getDatas());
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
