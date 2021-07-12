/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
		AuthorizationResourceMetas.registerForShare(AnalysisProject.AUTHORIZATION_RESOURCE_TYPE, "analysisProject");
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

		model.addAttribute("analysisProject", analysisProject);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "analysisProject.addAnalysisProject");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject analysisProject)
	{
		checkSaveEntity(analysisProject);

		User user = WebUtils.getUser(request, response);

		analysisProject.setId(IDUtil.randomIdOnTime20());
		analysisProject.setCreateUser(user);

		this.analysisProjectService.add(analysisProject);

		return buildOperationMessageSaveSuccessResponseEntity(request, analysisProject);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		AnalysisProject analysisProject = this.analysisProjectService.getByIdForEdit(user, id);

		if (analysisProject == null)
			throw new RecordNotFoundException();

		model.addAttribute("analysisProject", analysisProject);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "analysisProject.editAnalysisProject");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody AnalysisProject analysisProject)
	{
		checkSaveEntity(analysisProject);

		User user = WebUtils.getUser(request, response);

		this.analysisProjectService.update(user, analysisProject);

		return buildOperationMessageSaveSuccessResponseEntity(request, analysisProject);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		AnalysisProject analysisProject = this.analysisProjectService.getById(user, id);

		if (analysisProject == null)
			throw new RecordNotFoundException();

		model.addAttribute("analysisProject", analysisProject);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "analysisProject.viewAnalysisProject");
		model.addAttribute(KEY_READONLY, true);

		return "/analysisProject/analysisProject_form";
	}

	@RequestMapping(value = "/getByIdSilently", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public AnalysisProject getByIdSilently(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

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
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.analysisProjectService.deleteById(user, id);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "analysisProject.manageAnalysisProject");

		return "/analysisProject/analysisProject_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "analysisProject.selectAnalysisProject");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/analysisProject/analysisProject_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<AnalysisProject> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
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
