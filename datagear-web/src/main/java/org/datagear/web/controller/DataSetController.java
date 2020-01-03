/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.dbmodel.DatabaseModelResolver;
import org.datagear.dbmodel.ModelSqlSelectService;
import org.datagear.dbmodel.ModelSqlSelectService.ModelSqlResult;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlDataSetFactoryEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

	@Autowired
	private SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService;

	@Autowired
	private ModelSqlSelectService modelSqlSelectService;

	@Autowired
	private DatabaseModelResolver databaseModelResolver;

	public DataSetController()
	{
		super();
	}

	public DataSetController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource,
			SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService, ModelSqlSelectService modelSqlSelectService,
			DatabaseModelResolver databaseModelResolver)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
		this.modelSqlSelectService = modelSqlSelectService;
		this.databaseModelResolver = databaseModelResolver;

	}

	public SqlDataSetFactoryEntityService getSqlDataSetFactoryEntityService()
	{
		return sqlDataSetFactoryEntityService;
	}

	public void setSqlDataSetFactoryEntityService(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
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

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		SqlDataSetFactoryEntity dataSet = new SqlDataSetFactoryEntity();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysis/dataSet/dataSet_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			SqlDataSetFactoryEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		checkSaveEntity(dataSet);

		dataSet.setId(IDUtil.uuid());
		dataSet.setCreateUser(user);

		this.sqlDataSetFactoryEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		SqlDataSetFactoryEntity dataSet = this.sqlDataSetFactoryEntityService.getByIdForEdit(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.editDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysis/dataSet/dataSet_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			SqlDataSetFactoryEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		checkSaveEntity(dataSet);

		this.sqlDataSetFactoryEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		SqlDataSetFactoryEntity dataSet = this.sqlDataSetFactoryEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.viewDataSet");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/dataSet/dataSet_form";
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
			this.sqlDataSetFactoryEntityService.deleteById(user, id);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.manageDataSet");

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		boolean isMultipleSelect = false;
		if (request.getParameter("multiple") != null)
			isMultipleSelect = true;

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.selectDataSet");
		model.addAttribute(KEY_SELECTONLY, true);
		model.addAttribute("isMultipleSelect", isMultipleSelect);

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SqlDataSetFactoryEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		PagingQuery pagingQuery = getPagingQuery(request, WebUtils.COOKIE_PAGINATION_SIZE);

		PagingData<SqlDataSetFactoryEntity> pagingData = this.sqlDataSetFactoryEntityService.pagingQuery(user,
				pagingQuery);

		return pagingData;
	}

	@RequestMapping(value = "/sqlPreview/{schemaId}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ModelSqlResult select(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sql") final String sql, @RequestParam(value = "startRow", required = false) Integer startRow,
			@RequestParam(value = "fetchSize", required = false) Integer fetchSize,
			@RequestParam(value = "returnModel", required = false) Boolean returnModel) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		if (startRow == null)
			startRow = 1;
		if (fetchSize == null)
			fetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;
		if (returnModel == null)
			returnModel = false;

		if (fetchSize < 1)
			fetchSize = 1;
		if (fetchSize > 1000)
			fetchSize = 1000;

		final int startRowFinal = startRow;
		final int fetchSizeFinal = fetchSize;

		ModelSqlResult modelSqlResult = new ReturnSchemaConnExecutor<ModelSqlResult>(request, response, springModel,
				schemaId, true)
		{
			@Override
			protected ModelSqlResult execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				try
				{
					ModelSqlResult modelSqlResult = modelSqlSelectService.select(getConnection(), sql, startRowFinal,
							fetchSizeFinal, databaseModelResolver);

					return modelSqlResult;
				}
				catch (SQLException e)
				{
					throw new UserSQLException(e);
				}
			}
		}.execute();

		if (!Boolean.TRUE.equals(returnModel))
			modelSqlResult.setModel(null);

		return modelSqlResult;
	}

	@ExceptionHandler(IllegalImportDriverEntityFileFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleIllegalImportDriverEntityFileFormatException(HttpServletRequest request,
			HttpServletResponse response, IllegalImportDriverEntityFileFormatException exception)
	{
		String code = buildMessageCode("import." + IllegalImportDriverEntityFileFormatException.class.getSimpleName());

		setOperationMessageForThrowable(request, code, exception, false);

		return ERROR_PAGE_URL;
	}

	protected void checkSaveEntity(SqlDataSetFactoryEntity dataSet)
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
	}
}
