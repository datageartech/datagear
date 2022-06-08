/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.UserService;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户注册控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/register")
public class RegisterController extends AbstractController
{
	public static final String SESSION_KEY_REGISTER_USER_NAME = "registerSuccessUserName";

	public static final String CHECK_CODE_MODULE_REGISTER = "REGISTER";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private UserService userService;

	@Autowired
	private CheckCodeManager checkCodeManager;

	public RegisterController()
	{
		super();
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@RequestMapping
	public String register(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		if (this.applicationProperties.isDisableRegister())
		{
			WebUtils.setOperationMessage(request,
					optMsgFail(request, buildMsgCode("registerDisabled")));
			return ERROR_PAGE_URL;
		}

		User user = new User();

		model.addAttribute("user", user);
		request.setAttribute("currentUser", WebUtils.getUser(request, response).cloneNoPassword());
		setDetectNewVersionScriptAttr(request, response, this.applicationProperties.isDisableDetectNewVersion());

		return "/register";
	}

	@RequestMapping(value = "/doRegister", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> doRegister(HttpServletRequest request, HttpServletResponse response,
			@RequestBody RegisterForm form)
	{
		if (this.applicationProperties.isDisableRegister())
			return optMsgFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMsgCode("registerDisabled"));
		
		if(!this.checkCodeManager.isCheckCode(request.getSession(), CHECK_CODE_MODULE_REGISTER, form.getCheckCode()))
			return optMsgFailResponseEntity(request, HttpStatus.BAD_REQUEST, "checkCodeError");

		User user = form.getUser();
		String confirmPassword = form.getConfirmPassword();

		if (isBlank(user.getName()) || isBlank(user.getPassword()) || isBlank(confirmPassword)
				|| !confirmPassword.equals(user.getPassword()))
			throw new IllegalInputException();

		user.setId(IDUtil.randomIdOnTime20());
		user.setAdmin(false);
		user.setAnonymous(false);
		user.setCreateTime(new Date());

		if (this.userService.getByNameNoPassword(user.getName()) != null)
			return optMsgFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMsgCode("userNameExists"), user.getName());

		user.setRoles(buildUserRolesForSave(this.applicationProperties.getDefaultRoleRegister()));

		this.userService.add(user);

		request.getSession().setAttribute(SESSION_KEY_REGISTER_USER_NAME, user.getName());

		return optMsgSuccessResponseEntity();
	}

	@RequestMapping("/success")
	public String registerSuccess(HttpServletRequest request)
	{
		String successUser = (String) request.getSession().getAttribute(SESSION_KEY_REGISTER_USER_NAME);

		if (isBlank(successUser))
			return "redirect:/register";
		else
			return "/register_success";
	}

	@Override
	protected String buildMsgCode(String code)
	{
		return buildMsgCode("register", code);
	}

	public static Set<Role> buildUserRolesForSave(String roleIdsStr)
	{
		Set<Role> roles = new HashSet<>();

		if (!StringUtil.isBlank(roleIdsStr))
		{
			String[] roleIds = StringUtil.split(roleIdsStr, ",", true);

			for (String roleId : roleIds)
				roles.add(new Role(roleId, roleId));
		}

		roles.add(new Role(Role.ROLE_REGISTRY, Role.ROLE_REGISTRY));

		return roles;
	}

	public static class RegisterForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private User user;

		private String confirmPassword;

		private String checkCode;

		public RegisterForm()
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

		public String getCheckCode()
		{
			return checkCode;
		}

		public void setCheckCode(String checkCode)
		{
			this.checkCode = checkCode;
		}
	}
}
