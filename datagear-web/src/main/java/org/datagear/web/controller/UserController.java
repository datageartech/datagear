/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.UserService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	private SchemaService schemaService;

	/** 添加用户默认角色 */
	private String defaultRoleAdd = "";

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

	public SchemaService getSchemaService()
	{
		return schemaService;
	}

	public void setSchemaService(SchemaService schemaService)
	{
		this.schemaService = schemaService;
	}

	public String getDefaultRoleAdd()
	{
		return defaultRoleAdd;
	}

	@Value("${defaultRole.add}")
	public void setDefaultRoleAdd(String defaultRoleAdd)
	{
		this.defaultRoleAdd = defaultRoleAdd;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		User user = new User();

		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.addUser");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/user/user_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response, User user,
			@RequestParam("confirmPassword") String confirmPassword)
	{
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
		user.setRoles(RegisterController.buildUserRolesForSave(this.defaultRoleAdd));

		this.userService.add(user);

		return buildOperationMessageSaveSuccessResponseEntity(request, user.cloneNoPassword());
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = this.userService.getById(id);

		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.editUser");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/user/user_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			User user, @RequestParam("confirmPassword") String confirmPassword)
	{
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
		User user = this.userService.getById(id);

		if (user == null)
			throw new RecordNotFoundException();

		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.viewUser");
		model.addAttribute(KEY_READONLY, true);

		return "/user/user_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.userService.deleteByIds(ids);

		this.schemaService.deleteByUserId(ids);
		// TODO 应该删除所有其他用户创建的数据

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
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "user.personalSet");
		model.addAttribute(KEY_FORM_ACTION, "savePersonalSet");

		return "/user/user_form";
	}

	@RequestMapping(value = "/savePersonalSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savePersonalSet(HttpServletRequest request, HttpServletResponse response,
			User user, @RequestParam("confirmPassword") String confirmPassword)
	{
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
}
