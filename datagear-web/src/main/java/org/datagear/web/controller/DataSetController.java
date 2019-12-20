/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.SqlDataSetFactoryEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
public class DataSetController extends AbstractController
{
	@Autowired
	private SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService;

	public DataSetController()
	{
		super();
	}

	public DataSetController(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		super();
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
	}

	public SqlDataSetFactoryEntityService getSqlDataSetFactoryEntityService()
	{
		return sqlDataSetFactoryEntityService;
	}

	public void setSqlDataSetFactoryEntityService(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
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
		checkSaveEntity(dataSet);

		dataSet.setId(IDUtil.uuid());
		dataSet.setCreateUser(WebUtils.getUser(request, response));

		this.sqlDataSetFactoryEntityService.add(dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		SqlDataSetFactoryEntity dataSet = this.sqlDataSetFactoryEntityService.getById(id);

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
		checkSaveEntity(dataSet);

		this.sqlDataSetFactoryEntityService.update(dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		SqlDataSetFactoryEntity dataSet = this.sqlDataSetFactoryEntityService.getById(id);

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
		this.sqlDataSetFactoryEntityService.deleteByIds(ids);

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
