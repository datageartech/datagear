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

import org.datagear.management.domain.Role;
import org.datagear.management.service.RoleService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 角色管理控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/role")
public class RoleController extends AbstractController
{
	@Autowired
	private RoleService roleService;

	public RoleController()
	{
		super();
	}

	public RoleService getRoleService()
	{
		return roleService;
	}

	public void setRoleService(RoleService roleService)
	{
		this.roleService = roleService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, Model model)
	{
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		Role entity = createAdd(request, model);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/role/role_form";
	}

	protected Role createAdd(HttpServletRequest request, Model model)
	{
		return createInstance();
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Role entity)
	{
		inflateSaveEntity(request, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		entity.setId(IDUtil.randomIdOnTime20());

		this.roleService.add(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		Role entity = getByIdForEdit(this.roleService, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		
		return "/role/role_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Role entity)
	{
		inflateSaveEntity(request, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		this.roleService.update(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		Role entity = getByIdForView(this.roleService, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		
		return "/role/role_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.roleService.deleteByIds(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response,
			Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/role/role_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setSelectAction(request, model);
		return "/role/role_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Role> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<Role> pagingData = this.roleService.pagingQuery(pagingQuery);
		toQueryResponseData(request, pagingData.getItems());

		return pagingData;
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, Role entity)
	{
		if (isBlank(entity.getName()))
			throw new IllegalInputException();

		return null;
	}

	protected void setFormPageAttr(HttpServletRequest request, Model model, Role entity)
	{
		setFormModel(model, entity);
	}

	protected void inflateSaveEntity(HttpServletRequest request, Role entity)
	{
	}

	protected void toFormResponseData(HttpServletRequest request, Role entity)
	{
	}

	protected void toQueryResponseData(HttpServletRequest request, List<Role> items)
	{
	}

	protected Role createInstance()
	{
		return new Role();
	}
}
