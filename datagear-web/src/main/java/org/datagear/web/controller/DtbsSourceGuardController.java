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

import org.datagear.management.domain.DtbsSourceGuard;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.util.GuardEntity;
import org.datagear.util.AsteriskPatternMatcher;
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
 * {@linkplain DtbsSourceGuard}控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dtbsSourceGuard")
public class DtbsSourceGuardController extends AbstractController
{
	@Autowired
	private DtbsSourceGuardService dtbsSourceGuardService;

	public DtbsSourceGuardController()
	{
		super();
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
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		DtbsSourceGuard entity = createAdd(request, model);
		setFormModel(model, entity);

		return "/dtbsSourceGuard/dtbsSourceGuard_form";
	}

	protected DtbsSourceGuard createAdd(HttpServletRequest request, Model model)
	{
		DtbsSourceGuard entity = new DtbsSourceGuard();
		entity.setUserPattern(AsteriskPatternMatcher.ALL_PATTERN);
		entity.setPermitted(false);

		return entity;
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSourceGuard entity)
	{
		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		entity.setId(IDUtil.randomIdOnTime20());
		inflateCreateTime(entity);

		this.dtbsSourceGuardService.add(entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		DtbsSourceGuard entity = getByIdForEdit(this.dtbsSourceGuardService, id);
		convertToFormModel(request, model, entity);
		setFormModel(model, entity);
		
		return "/dtbsSourceGuard/dtbsSourceGuard_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSourceGuard entity)
	{
		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		this.dtbsSourceGuardService.update(entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		DtbsSourceGuard entity = getByIdForView(this.dtbsSourceGuardService, id);
		convertToFormModel(request, model, entity);
		setFormModel(model, entity);
		
		return "/dtbsSourceGuard/dtbsSourceGuard_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.dtbsSourceGuardService.deleteByIds(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response,
			Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/dtbsSourceGuard/dtbsSourceGuard_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSourceGuard> queryData(HttpServletRequest request, HttpServletResponse response,
			final Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		List<DtbsSourceGuard> items = this.dtbsSourceGuardService.query(pagingQuery);
		handleQueryData(request, items);

		return items;
	}

	@RequestMapping("/test")
	public String test(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setFormAction(model, "test", "testExecute");

		return "/dtbsSourceGuard/dtbsSourceGuard_test";
	}

	@RequestMapping(value = "/testExecute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> testExecute(HttpServletRequest request, HttpServletResponse response,
			@RequestBody GuardEntity form)
	{
		boolean permitted = this.dtbsSourceGuardService.isPermitted(form);
		return optSuccessDataResponseEntity(request, permitted);
	}

	protected void convertToFormModel(HttpServletRequest request, Model model, DtbsSourceGuard entity)
	{
	}

	protected void handleQueryData(HttpServletRequest request, List<DtbsSourceGuard> items)
	{
		DtbsSourceGuard.sortByPriority(items);
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, DtbsSourceGuard entity)
	{
		if (isBlank(entity.getName()) || isBlank(entity.getPattern()))
			throw new IllegalInputException();

		return null;
	}
}
