/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.UserService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户管理控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/user")
public class UserController extends AbstractController
{
	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private SchemaService schemaService;

	@Autowired
	private ApplicationProperties applicationProperties;

	public UserController()
	{
		super();
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public RoleService getRoleService()
	{
		return roleService;
	}

	public void setRoleService(RoleService roleService)
	{
		this.roleService = roleService;
	}

	public SchemaService getSchemaService()
	{
		return schemaService;
	}

	public void setSchemaService(SchemaService schemaService)
	{
		this.schemaService = schemaService;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		User user = new User();

		Set<Role> dftRoles = RegisterController.buildUserRolesForSave(this.applicationProperties.getDefaultRoleAdd());
		Set<Role> addRoles = new HashSet<Role>(dftRoles.size());
		for (Role r : dftRoles)
		{
			Role role = this.roleService.getById(r.getId());
			if (role != null)
				addRoles.add(role);
		}
		user.setRoles(addRoles);

		model.addAttribute("user", user);
		model.addAttribute("userRoles", toWriteJsonTemplateModel(toUserRolesList(user)));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.addUser");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/user/user_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserForm userForm)
	{
		User user = userForm.getUser();
		String confirmPassword = userForm.getConfirmPassword();

		if (isBlank(user.getName()) || isBlank(user.getPassword()))
			throw new IllegalInputException();

		if (isBlank(confirmPassword) || !confirmPassword.equals(user.getPassword()))
			throw new IllegalInputException();

		User namedUser = this.userService.getByName(user.getName());

		if (namedUser != null)
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("userNameExists"), user.getName());

		user.setId(IDUtil.randomIdOnTime20());
		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));

		this.userService.add(user);

		return buildOperationMessageSaveSuccessResponseEntity(request, user.cloneNoPassword());
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = this.userService.getByIdNoPassword(id);

		if (user == null)
			throw new RecordNotFoundException();

		model.addAttribute("user", user);
		model.addAttribute("userRoles", toWriteJsonTemplateModel(toUserRolesList(user)));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.editUser");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/user/user_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserForm userForm)
	{
		User user = userForm.getUser();
		String confirmPassword = userForm.getConfirmPassword();

		if (isBlank(user.getName()))
			throw new IllegalInputException();

		if (!confirmPassword.equals(user.getPassword()))
			throw new IllegalInputException();

		User namedUser = this.userService.getByName(user.getName());

		if (namedUser != null && !namedUser.getId().equals(user.getId()))
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("userNameExists"), user.getName());

		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));

		this.userService.update(user);

		return buildOperationMessageSaveSuccessResponseEntity(request, user.cloneNoPassword());
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = this.userService.getByIdNoPassword(id);

		if (user == null)
			throw new RecordNotFoundException();

		model.addAttribute("user", user);
		model.addAttribute("userRoles", toWriteJsonTemplateModel(toUserRolesList(user)));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.viewUser");
		model.addAttribute(KEY_READONLY, true);

		return "/user/user_form";
	}

	@RequestMapping("/delete")
	public String delete(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String[] ids)
	{
		if (isEmpty(ids))
			throw new IllegalInputException();

		List<User> users = this.userService.getByIdsNoPassword(ids, true);

		model.addAttribute("deleteUsers", toWriteJsonTemplateModel(users));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.deleteUser");
		model.addAttribute(KEY_FORM_ACTION, "deleteDo");

		return "/user/user_delete";
	}

	@RequestMapping(value = "/deleteDo", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteDo(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DeleteUserForm form)
	{
		if (isEmpty(form.getIds()) || isEmpty(form.getMigrateToId()))
			throw new IllegalInputException();

		if (Arrays.asList(form.getIds()).contains(form.getMigrateToId()))
			throw new IllegalInputException();

		if (User.containsAdminUser(form.getIds()))
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("deleteAdminUserDenied"));

		User user = this.userService.getById(form.getMigrateToId());

		if (user == null)
			throw new IllegalInputException();

		this.userService.deleteByIds(form.getIds(), form.getMigrateToId());

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.manageUser");
		return "/user/user_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.selectUser");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/user/user_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<User> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);
		PagingData<User> pagingData = this.userService.pagingQuery(pagingQuery);
		return pagingData;
	}

	@RequestMapping("/personalSet")
	public String personalSet(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User operator = WebUtils.getUser(request, response);

		User user = this.userService.getById(operator.getId());

		if (user == null)
			throw new RecordNotFoundException();

		model.addAttribute("user", user);
		model.addAttribute("disableRoles", true);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.personalSet");
		model.addAttribute(KEY_FORM_ACTION, "savePersonalSet");

		return "/user/user_form";
	}

	@RequestMapping(value = "/savePersonalSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savePersonalSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody UserForm userForm)
	{
		User user = userForm.getUser();
		String confirmPassword = userForm.getConfirmPassword();

		if (isBlank(user.getName()) || !confirmPassword.equals(user.getPassword()))
			throw new IllegalInputException();

		User operator = WebUtils.getUser(request, response);

		user.setId(operator.getId());

		User namedUser = this.userService.getByName(user.getName());

		if (namedUser != null && !namedUser.getId().equals(user.getId()))
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("userNameExists"), user.getName());

		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));

		this.userService.updateIgnoreRole(user);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("user", code);
	}

	protected List<Role> toUserRolesList(User user)
	{
		List<Role> list = new ArrayList<Role>();

		Set<Role> roles = (user == null ? null : user.getRoles());
		if (roles != null)
			list.addAll(roles);

		Collections.sort(list, new Comparator<Role>()
		{
			@Override
			public int compare(Role o1, Role o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		return list;
	}

	public static class UserForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private User user;

		private String confirmPassword;

		public UserForm()
		{
			super();
		}

		public User getUser()
		{
			return user;
		}

		public void setUser(User user)
		{
			this.user = user;
		}

		public String getConfirmPassword()
		{
			return confirmPassword;
		}

		public void setConfirmPassword(String confirmPassword)
		{
			this.confirmPassword = confirmPassword;
		}
	}

	public static class DeleteUserForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		/** 要删除的用户ID */
		private String[] ids;

		/** 数据迁移的目标用户ID */
		private String migrateToId;

		public DeleteUserForm()
		{
			super();
		}

		public String[] getIds()
		{
			return ids;
		}

		public void setIds(String[] ids)
		{
			this.ids = ids;
		}

		public String getMigrateToId()
		{
			return migrateToId;
		}

		public void setMigrateToId(String migrateToId)
		{
			this.migrateToId = migrateToId;
		}
	}
}
