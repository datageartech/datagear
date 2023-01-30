/*
 * Copyright 2018-2023 datagear.tech
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
	static
	{
		AuthorizationResourceMetas.registerForShare(AnalysisProject.AUTHORIZATION_RESOURCE_TYPE);
	}

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
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		AnalysisProject analysisProject = new AnalysisProject();

		setFormModel(model, analysisProject, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject analysisProject)
	{
		checkSaveEntity(analysisProject);

		User user = WebUtils.getUser();

		analysisProject.setId(IDUtil.randomIdOnTime20());
		analysisProject.setCreateUser(user);

		this.analysisProjectService.add(analysisProject);

		return optSuccessDataResponseEntity(request, analysisProject);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();
		AnalysisProject analysisProject = getByIdForEdit(this.analysisProjectService, user, id);
		
		setFormModel(model, analysisProject, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject analysisProject)
	{
		checkSaveEntity(analysisProject);

		User user = WebUtils.getUser();

		this.analysisProjectService.update(user, analysisProject);

		return optSuccessDataResponseEntity(request, analysisProject);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();
		AnalysisProject analysisProject = getByIdForView(this.analysisProjectService, user, id);

		setFormModel(model, analysisProject, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/getByIdSilently", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public AnalysisProject getByIdSilently(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser();

		AnalysisProject analysisProject = null;

		try
		{
			analysisProject = this.analysisProjectService.getById(user, id);
		}
		catch (Throwable t)
		{
		}

		return analysisProject;
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = WebUtils.getUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.analysisProjectService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyActionByRole(model, WebUtils.getUser());
		return "/analysisProject/analysisProject_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/analysisProject/analysisProject_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<AnalysisProject> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser();
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<AnalysisProject> pagingData = this.analysisProjectService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter());

		return pagingData;
	}

	protected void checkSaveEntity(AnalysisProject analysisProject)
	{
		if (isBlank(analysisProject.getName()))
			throw new IllegalInputException();
	}
}
