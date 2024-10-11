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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.web.security.LoginCheckCodeErrorException;
import org.datagear.web.util.DetectNewVersionScriptResolver;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.AccessLatch;
import org.datagear.web.util.accesslatch.IpLoginLatch;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 登录控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/login")
public class LoginController extends AbstractController
{
	/**
	 * 登录页
	 */
	public static final String LOGIN_PAGE = "/login";

	/**
	 * 登录成功页
	 */
	public static final String LOGIN_PAGE_SUCCESS = "/login/success";

	/**
	 * 登录错误页
	 */
	public static final String LOGIN_PAGE_ERROR = "/login/error";

	/**
	 * 登录参数：用户名
	 */
	public static final String LOGIN_PARAM_USER_NAME = "name";

	/**
	 * 登录参数：密码
	 */
	public static final String LOGIN_PARAM_PASSWORD = "password";

	/**
	 * 登录参数：记住登录
	 */
	public static final String LOGIN_PARAM_REMEMBER_ME = "rememberMe";

	/**
	 * 登录参数：校验码
	 */
	public static final String LOGIN_PARAM_CHECK_CODE = "checkCode";

	public static final String CHECK_CODE_MODULE_LOGIN = "LOGIN";

	@Autowired
	private IpLoginLatch ipLoginLatch;

	@Autowired
	private UsernameLoginLatch usernameLoginLatch;

	@Autowired
	private DetectNewVersionScriptResolver detectNewVersionScriptResolver;

	private RequestCache requestCache = new HttpSessionRequestCache();

	public LoginController()
	{
		super();
	}

	public IpLoginLatch getIpLoginLatch()
	{
		return ipLoginLatch;
	}

	public void setIpLoginLatch(IpLoginLatch ipLoginLatch)
	{
		this.ipLoginLatch = ipLoginLatch;
	}

	public UsernameLoginLatch getUsernameLoginLatch()
	{
		return usernameLoginLatch;
	}

	public void setUsernameLoginLatch(UsernameLoginLatch usernameLoginLatch)
	{
		this.usernameLoginLatch = usernameLoginLatch;
	}

	public DetectNewVersionScriptResolver getDetectNewVersionScriptResolver()
	{
		return detectNewVersionScriptResolver;
	}

	public void setDetectNewVersionScriptResolver(DetectNewVersionScriptResolver detectNewVersionScriptResolver)
	{
		this.detectNewVersionScriptResolver = detectNewVersionScriptResolver;
	}

	public RequestCache getRequestCache()
	{
		return requestCache;
	}

	public void setRequestCache(RequestCache requestCache)
	{
		this.requestCache = requestCache;
	}

	/**
	 * 打开登录界面。
	 * 
	 * @return
	 */
	@RequestMapping
	public String login(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setFormAction(model, "login", "doLogin");

		LoginForm form = createLoginForm(request, response, model);
		setFormModel(model, form);
		this.detectNewVersionScriptResolver.enableIf(request);
		
		return "/login";
	}

	protected LoginForm createLoginForm(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		LoginForm form = new LoginForm();
		form.setName(resolveLoginUsername(request, response));

		return form;
	}

	@RequestMapping(value = "/success", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> loginSuccess(HttpServletRequest request, HttpServletResponse response)
	{
		ResponseEntity<OperationMessage> responseEntity = optSuccessDataResponseEntity(request, "loginSuccess");

		Map<String, Object> data = new HashMap<String, Object>();

		String redirectUrl = WebUtils.getIndexPath(request);

		// 参考：
		// org.springframework.security.web.access.ExceptionTranslationFilter.sendStartAuthentication()
		// org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
		SavedRequest savedRequest = this.requestCache.getRequest(request, response);
		if (savedRequest != null)
			redirectUrl = savedRequest.getRedirectUrl();

		data.put("redirectUrl", redirectUrl);

		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	@RequestMapping(value = "/error", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> loginError(HttpServletRequest request, HttpServletResponse response)
	{
		AuthenticationException exception = getAuthenticationException(request, true);
		return handleLoginError(request, response, exception);
	}

	/**
	 * 处理登录错误。
	 * 
	 * @param request
	 * @param response
	 * @param exception
	 *            可能为{@code null}
	 * @return
	 */
	protected ResponseEntity<OperationMessage> handleLoginError(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
	{
		if (exception instanceof LoginCheckCodeErrorException)
		{
			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "checkCodeError");
		}

		int ipLoginRemain = this.ipLoginLatch.remain(request);

		if (AccessLatch.isLatched(ipLoginRemain))
		{
			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "ipLoginLatched");
		}

		int usernameLoginRemain = this.usernameLoginLatch.remain(request.getParameter(LOGIN_PARAM_USER_NAME));

		if (AccessLatch.isLatched(usernameLoginRemain))
		{
			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "usernameLoginLatched");
		}
		else if (AccessLatch.isNonLatch(usernameLoginRemain))
		{
			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "usernameOrPasswordError");
		}
		else if (usernameLoginRemain > 0)
		{
			if (ipLoginRemain >= 0)
				usernameLoginRemain = Math.min(ipLoginRemain, usernameLoginRemain);

			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "usernameOrPasswordErrorRemain",
					usernameLoginRemain);
		}
		else
		{
			return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "usernameOrPasswordError");
		}
	}

	protected String resolveLoginUsername(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession();

		String username = (String) session.getAttribute(RegisterController.SESSION_KEY_REGISTER_USER_NAME);
		session.removeAttribute(RegisterController.SESSION_KEY_REGISTER_USER_NAME);

		if (username == null)
			username = "";

		return username;
	}

	public static class LoginForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String name = "";

		private String password = "";

		private String checkCode = "";

		private boolean rememberMe = false;

		public LoginForm()
		{
			super();
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

		public String getCheckCode()
		{
			return checkCode;
		}

		public void setCheckCode(String checkCode)
		{
			this.checkCode = checkCode;
		}

		public boolean isRememberMe()
		{
			return rememberMe;
		}

		public void setRememberMe(boolean rememberMe)
		{
			this.rememberMe = rememberMe;
		}
	}
}
