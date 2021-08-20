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

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
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

		model.addAttribute("role", role);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "role.addRole");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/role/role_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response, Role role)
	{
		if (isBlank(role.getName()))
			throw new IllegalInputException();

		role.setId(IDUtil.randomIdOnTime20());

		this.roleService.add(role);

		return buildOperationMessageSaveSuccessResponseEntity(request, role);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		Role role = this.roleService.getById(id);

		model.addAttribute("role", role);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "role.editRole");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/role/role_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			Role role)
	{
		if (isBlank(role.getName()))
			throw new IllegalInputException();

		this.roleService.update(role);

		return buildOperationMessageSaveSuccessResponseEntity(request, role);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		Role role = this.roleService.getById(id);

		if (role == null)
			throw new RecordNotFoundException();

		model.addAttribute("role", role);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "role.viewRole");
		model.addAttribute(KEY_READONLY, true);

		return "/role/role_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.roleService.deleteByIds(ids);

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "role.manageRole");

		return "/role/role_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "role.selectRole");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/role/role_grid";
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

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("role", code);
	}

	public static class RoleUsersForm
	{
		private Role role;
		private List<User> users;

		public RoleUsersForm()
		{
			super();
		}

		public Role getRole()
		{
			return role;
		}

		public void setRole(Role role)
		{
			this.role = role;
		}

		public List<User> getUsers()
		{
			return users;
		}

		public void setUsers(List<User> users)
		{
			this.users = users;
		}
	}
}
