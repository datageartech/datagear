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
import org.datagear.management.util.RoleSpec;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.OperationMessage;
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

	@Autowired
	private RoleSpec roleSpec;

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

	public RoleSpec getRoleSpec()
	{
		return roleSpec;
	}

	public void setRoleSpec(RoleSpec roleSpec)
	{
		this.roleSpec = roleSpec;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		User user = prepareAddUser(request, model);
		setAddUserRoles(request, model, user);
		setFormModel(model, user, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);
		model.addAttribute("enablePassword", true);

		return "/user/user_form";
	}

	protected User prepareAddUser(HttpServletRequest request, org.springframework.ui.Model model)
	{
		return new User();
	}

	protected void setAddUserRoles(HttpServletRequest request, org.springframework.ui.Model model, User user)
	{
		Set<Role> dftRoles = this.roleSpec.buildRolesByIds(this.applicationProperties.getDefaultRoleAdd(), true);
		Set<Role> addRoles = new HashSet<Role>(dftRoles.size());

		for (Role r : dftRoles)
		{
			Role role = this.roleService.getById(r.getId());
			if (role != null)
				addRoles.add(role);
		}

		user.setRoles(addRoles);
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody User user)
	{
		if (isBlank(user.getName()) || isBlank(user.getPassword()))
			throw new IllegalInputException();

		User namedUser = this.userService.getByNameNoPassword(user.getName());

		if (namedUser != null)
			return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "usernameExists", user.getName());

		user.setId(IDUtil.randomIdOnTime20());
		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));
		inflateCreateTime(user);

		saveAddUser(user);

		return optSuccessDataResponseEntity(request, user);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = this.userService.getByIdNoPassword(id);

		if (user == null)
			throw new RecordNotFoundException();

		setFormModel(model, user, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		return "/user/user_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody User user)
	{
		if (isBlank(user.getName()))
			throw new IllegalInputException();

		User namedUser = this.userService.getByNameNoPassword(user.getName());

		if (namedUser != null && !namedUser.getId().equals(user.getId()))
			return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "usernameExists", user.getName());

		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));

		saveEditUser(user);

		return optSuccessDataResponseEntity(request, user);
	}

	@RequestMapping("/editPsd")
	public String editPassword(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = this.userService.getByIdSimple(id);

		if (user == null)
			throw new RecordNotFoundException();

		user = user.cloneNoPassword();

		setFormModel(model, toEditPsdForm(user), "editPassword", "saveEditPsd");
		model.addAttribute("enableOldPassword", false);

		return "/user/user_psd_form";
	}

	@RequestMapping(value = "/saveEditPsd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditPassword(HttpServletRequest request, HttpServletResponse response,
			@RequestBody EditPsdForm form)
	{
		if (isEmpty(form.getId()) || isBlank(form.getPassword()))
			throw new IllegalInputException();

		this.userService.updatePasswordById(form.getId(), form.getPassword(), true);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = this.userService.getByIdNoPassword(id);

		if (user == null)
			throw new RecordNotFoundException();

		setFormModel(model, user, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		return "/user/user_form";
	}

	@RequestMapping("/delete")
	public String delete(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String[] ids)
	{
		if (isEmpty(ids))
			throw new IllegalInputException();

		List<User> users = this.userService.getByIdsSimple(ids, true);

		setFormModel(model, users, REQUEST_ACTION_DELETE, "deleteDo");

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
			return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "deleteUserCanNotBeMigrateUser");

		if (User.containsAdminUser(form.getIds()))
			return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "deleteAdminUserDenied");

		User user = this.userService.getById(form.getMigrateToId());

		if (user == null)
			throw new IllegalInputException();

		this.userService.deleteByIds(form.getIds(), form.getMigrateToId());

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		setQueryDataUrl(model, "/user/pagingQueryData");
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyAction(model);

		return "/user/user_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setQueryDataUrl(model, "/user/pagingQueryData");
		setSelectAction(request, model);

		return "/user/user_table";
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
		User operator = getCurrentUser();

		User user = this.userService.getByIdNoPassword(operator.getId());

		if (user == null)
			throw new RecordNotFoundException();

		model.addAttribute("disableRoles", true);
		model.addAttribute("disableEditName", getApplicationProperties().isDisablePersonalSetName());
		setFormModel(model, user, "personalSet", "savePersonalSet");

		return "/user/user_form";
	}

	@RequestMapping(value = "/savePersonalSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savePersonalSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody User user)
	{
		if (isBlank(user.getName()))
			throw new IllegalInputException();

		User operator = getCurrentUser();
		user.setId(operator.getId());

		if (getApplicationProperties().isDisablePersonalSetName())
		{
			User u = getByIdForView(this.userService, user.getId());
			user.setName(u.getName());
		}
		else
		{
			User namedUser = this.userService.getByNameNoPassword(user.getName());

			if (namedUser != null && !namedUser.getId().equals(user.getId()))
				return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "usernameExists", user.getName());
		}

		// 禁用新建管理员账号功能
		user.setAdmin(User.isAdminUser(user));

		savePersonalSetUser(user);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/personalPsd")
	public String personalPsd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User operator = getCurrentUser();

		User user = this.userService.getByIdSimple(operator.getId());

		if (user == null)
			throw new RecordNotFoundException();

		setFormModel(model, toPersonalEditPsdForm(user), "editPassword", "savePersonalPsd");
		model.addAttribute("enableOldPassword", true);

		return "/user/user_psd_form";
	}

	@RequestMapping(value = "/savePersonalPsd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savePersonalPsd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody PersonalEditPsdForm form)
	{
		if (isBlank(form.getOldPassword()) || isBlank(form.getPassword()))
			throw new IllegalInputException();

		User operator = getCurrentUser();

		if (!this.userService.isPasswordMatchById(operator.getId(), form.getOldPassword()))
			return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "oldPasswordError");

		this.userService.updatePasswordById(operator.getId(), form.getPassword(), true);

		return optSuccessResponseEntity(request);
	}

	protected EditPsdForm toEditPsdForm(User user)
	{
		EditPsdForm fm = new EditPsdForm(user.getId(), user.getName());
		return fm;
	}

	protected PersonalEditPsdForm toPersonalEditPsdForm(User user)
	{
		PersonalEditPsdForm fm = new PersonalEditPsdForm(user.getId(), user.getName());
		return fm;
	}

	protected void saveAddUser(User user)
	{
		this.userService.add(user);
	}

	protected void saveEditUser(User user)
	{
		this.userService.update(user);
	}

	protected void savePersonalSetUser(User user)
	{
		this.userService.updateIgnoreRole(user);
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

	public static class EditPsdForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String id;

		/** 用户名 */
		private String name;

		/** 新密码 */
		private String password;

		public EditPsdForm()
		{
			super();
		}

		public EditPsdForm(String id, String name)
		{
			super();
			this.id = id;
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}
	}

	public static class PersonalEditPsdForm extends EditPsdForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		/** 旧密码 */
		private String oldPassword;

		public PersonalEditPsdForm()
		{
			super();
		}

		public PersonalEditPsdForm(String id, String name)
		{
			super(id, name);
		}

		public String getOldPassword()
		{
			return oldPassword;
		}

		public void setOldPassword(String oldPassword)
		{
			this.oldPassword = oldPassword;
		}
	}
}
