/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Role;
import org.datagear.management.service.RoleService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		Role role = new Role();

		setFormModel(model, role, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/role/role_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Role role)
	{
		if (isBlank(role.getName()))
			throw new IllegalInputException();

		role.setId(IDUtil.randomIdOnTime20());

		this.roleService.add(role);

		return optSuccessDataResponseEntity(request, role);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		Role role = getByIdForEdit(this.roleService, id);

		setFormModel(model, role, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		
		return "/role/role_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Role role)
	{
		if (isBlank(role.getName()))
			throw new IllegalInputException();

		this.roleService.update(role);

		return optSuccessDataResponseEntity(request, role);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		Role role = getByIdForView(this.roleService, id);

		setFormModel(model, role, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
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

	@RequestMapping(value = "/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setIsReadonlyAction(model, WebUtils.getUser());
		return "/role/role_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/role/role_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Role> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<Role> roles = this.roleService.pagingQuery(pagingQuery);
		return roles;
	}
}
