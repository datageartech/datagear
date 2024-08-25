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
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard();
		dtbsSourceGuard.setUserPattern(AsteriskPatternMatcher.ALL_PATTERN);
		dtbsSourceGuard.setPermitted(false);

		setFormModel(model, dtbsSourceGuard, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/dtbsSourceGuard/dtbsSourceGuard_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSourceGuard dtbsSourceGuard)
	{
		checkSaveEntity(dtbsSourceGuard);

		dtbsSourceGuard.setId(IDUtil.randomIdOnTime20());
		inflateCreateTime(dtbsSourceGuard);

		this.dtbsSourceGuardService.add(dtbsSourceGuard);

		return optSuccessDataResponseEntity(request, dtbsSourceGuard);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		DtbsSourceGuard dtbsSourceGuard = getByIdForEdit(this.dtbsSourceGuardService, id);

		setFormModel(model, dtbsSourceGuard, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		
		return "/dtbsSourceGuard/dtbsSourceGuard_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DtbsSourceGuard dtbsSourceGuard)
	{
		checkSaveEntity(dtbsSourceGuard);

		this.dtbsSourceGuardService.update(dtbsSourceGuard);

		return optSuccessDataResponseEntity(request, dtbsSourceGuard);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		DtbsSourceGuard dtbsSourceGuard = getByIdForView(this.dtbsSourceGuardService, id);

		setFormModel(model, dtbsSourceGuard, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
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
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyAction(model);
		return "/dtbsSourceGuard/dtbsSourceGuard_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DtbsSourceGuard> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		List<DtbsSourceGuard> dtbsSourceGuards = this.dtbsSourceGuardService.query(pagingQuery);
		DtbsSourceGuard.sortByPriority(dtbsSourceGuards);

		return dtbsSourceGuards;
	}

	protected void checkSaveEntity(DtbsSourceGuard dtbsSourceGuard)
	{
		if (isBlank(dtbsSourceGuard.getName()) || isBlank(dtbsSourceGuard.getPattern()))
			throw new IllegalInputException();
	}

	@RequestMapping("/test")
	public String test(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
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
}
