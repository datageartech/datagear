/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.service.SchemaGuardService;
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
 * {@linkplain SchemaGuard}控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/schemaGuard")
public class SchemaGuardController extends AbstractController
{
	@Autowired
	private SchemaGuardService schemaGuardService;

	public SchemaGuardController()
	{
		super();
	}

	public SchemaGuardService getSchemaGuardService()
	{
		return schemaGuardService;
	}

	public void setSchemaGuardService(SchemaGuardService schemaGuardService)
	{
		this.schemaGuardService = schemaGuardService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		SchemaGuard schemaGuard = new SchemaGuard();

		setFormModel(model, schemaGuard, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/schemaGuard/schemaGuard_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SchemaGuard schemaGuard)
	{
		checkSaveEntity(schemaGuard);

		schemaGuard.setId(IDUtil.randomIdOnTime20());

		this.schemaGuardService.add(schemaGuard);

		return optSuccessDataResponseEntity(request, schemaGuard);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		SchemaGuard schemaGuard = getByIdForEdit(this.schemaGuardService, id);

		setFormModel(model, schemaGuard, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		
		return "/schemaGuard/schemaGuard_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SchemaGuard schemaGuard)
	{
		checkSaveEntity(schemaGuard);

		this.schemaGuardService.update(schemaGuard);

		return optSuccessDataResponseEntity(request, schemaGuard);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		SchemaGuard schemaGuard = getByIdForView(this.schemaGuardService, id);

		setFormModel(model, schemaGuard, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
		return "/schemaGuard/schemaGuard_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.schemaGuardService.deleteByIds(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/query")
	public String query(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyActionOnRoleDataAnalyst(model, WebUtils.getUser());
		return "/schemaGuard/schemaGuard_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<SchemaGuard> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		List<SchemaGuard> schemaGuards = this.schemaGuardService.query(pagingQuery);

		return schemaGuards;
	}

	protected void checkSaveEntity(SchemaGuard schemaGuard)
	{
		if (isBlank(schemaGuard.getPattern()))
			throw new IllegalInputException();
	}

	@RequestMapping("/test")
	public String test(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setFormAction(model, "test", "testExecute");

		return "/schemaGuard/schemaGuard_test";
	}

	@RequestMapping(value = "/testExecute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> testExecute(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SchemaGuardTestForm form)
	{
		String url = form.getUrl();

		boolean permitted = this.schemaGuardService.isPermitted(url);

		return optSuccessDataResponseEntity(request, permitted);
	}

	public static class SchemaGuardTestForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String url;

		public SchemaGuardTestForm()
		{
			super();
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}
	}
}
