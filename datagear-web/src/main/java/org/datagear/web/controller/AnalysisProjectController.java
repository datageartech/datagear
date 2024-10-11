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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据分析项目控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysisProject")
public class AnalysisProjectController extends AbstractController
{
	@Autowired
	private AnalysisProjectService analysisProjectService;

	public AnalysisProjectController()
	{
		super();
	}

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, Model model)
	{
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		AnalysisProject entity = createAdd(request, model);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/analysisProject/analysisProject_form";
	}

	protected AnalysisProject createAdd(HttpServletRequest request, Model model)
	{
		return createInstance();
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject entity)
	{
		User user = getCurrentUser();

		entity.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(entity, user);
		inflateSaveEntity(request, user, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity);

		if (re != null)
			return re;

		this.analysisProjectService.add(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		AnalysisProject entity = getByIdForEdit(this.analysisProjectService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		
		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject entity)
	{
		User user = getCurrentUser();

		inflateSaveEntity(request, user, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity);

		if (re != null)
			return re;

		this.analysisProjectService.update(user, entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		AnalysisProject entity = getByIdForView(this.analysisProjectService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/getByIdSilently", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public AnalysisProject getByIdSilently(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id)
	{
		User user = getCurrentUser();

		AnalysisProject entity = null;

		try
		{
			entity = this.analysisProjectService.getById(user, id);
		}
		catch (Throwable t)
		{
		}

		if (entity != null)
		{
			toFormResponseData(request, entity);
		}

		return entity;
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
			this.analysisProjectService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response,
			Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/analysisProject/analysisProject_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setSelectAction(request, model);
		return "/analysisProject/analysisProject_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<AnalysisProject> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			Model springModel, @RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<AnalysisProject> pagingData = this.analysisProjectService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter());
		toQueryResponseData(request, pagingData.getItems());

		return pagingData;
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, User user,
			AnalysisProject entity)
	{
		if (isEmpty(entity.getId()) || isBlank(entity.getName()))
			throw new IllegalInputException();

		return null;
	}

	protected void setFormPageAttr(HttpServletRequest request, Model model, AnalysisProject entity)
	{
		setFormModel(model, entity);
	}

	protected void inflateSaveEntity(HttpServletRequest request, User user, AnalysisProject entity)
	{
	}

	protected void toFormResponseData(HttpServletRequest request, AnalysisProject entity)
	{
	}

	protected void toQueryResponseData(HttpServletRequest request, List<AnalysisProject> items)
	{
	}

	protected AnalysisProject createInstance()
	{
		return new AnalysisProject();
	}
}
