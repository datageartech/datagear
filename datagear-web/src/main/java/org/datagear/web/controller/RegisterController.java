/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.management.service.UserService;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	@Autowired
	private UserService userService;

	private boolean disableRegister = false;

	public RegisterController()
	{
		super();
	}

	public RegisterController(MessageSource messageSource, ClassDataConverter classDataConverter,
			UserService userService)
	{
		super(messageSource, classDataConverter);
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public boolean isDisableRegister()
	{
		return disableRegister;
	}

	@Value("${disableRegister}")
	public void setDisableRegister(boolean disableRegister)
	{
		this.disableRegister = disableRegister;
	}

	@RequestMapping
	public String register(HttpServletRequest request, org.springframework.ui.Model model)
	{
		if (this.disableRegister)
		{
			WebUtils.setOperationMessage(request,
					buildOperationMessageFail(request, buildMessageCode("registerDisabled")));
			return ERROR_PAGE_URL;
		}

		User user = new User();

		model.addAttribute("user", user);

		return "/register";
	}

	@RequestMapping(value = "/doRegister", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> doRegister(HttpServletRequest request, HttpServletResponse response,
			User user, @RequestParam("confirmPassword") String confirmPassword)
	{
		if (this.disableRegister)
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("registerDisabled"));

		if (isBlank(user.getName()) || isBlank(user.getPassword()) || isBlank(confirmPassword)
				|| !confirmPassword.equals(user.getPassword()))
			throw new IllegalInputException();

		user.setId(IDUtil.uuid());
		user.setAdmin(false);
		user.setAnonymous(false);
		user.setCreateTime(new Date());

		if (this.userService.getByName(user.getName()) != null)
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("userNameExists"), user.getName());

		this.userService.add(user);

		request.getSession().setAttribute(SESSION_KEY_REGISTER_USER_NAME, user.getName());

		return buildOperationMessageSuccessEmptyResponseEntity();
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
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("register", code);
	}
}
