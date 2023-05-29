/*
 * Copyright 2018-2023 datagear.tech
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.User;
import org.datagear.util.IOUtil;
import org.datagear.web.security.LoginCheckCodeErrorException;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.AccessLatch;
import org.datagear.web.util.accesslatch.IpLoginLatch;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
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
	public String login(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		User user = new User();
		user.setName(resolveLoginUsername(request, response));

		setFormModel(model, user, "login", "doLogin");
		WebUtils.setEnableDetectNewVersionRequest(request);
		
		return "/login";
	}

	@RequestMapping(value = "/success", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> loginSuccess(HttpServletRequest request, HttpServletResponse response)
	{
		ResponseEntity<OperationMessage> responseEntity = optSuccessDataResponseEntity(request, "loginSuccess");

		Map<String, Object> data = new HashMap<String, Object>();

		String redirectUrl = WebUtils.INDEX_PAGE_URL;

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
		AuthenticationException ae = getAuthenticationExceptionWithRemove(request);
		if (ae != null)
		{
			if (ae instanceof LoginCheckCodeErrorException)
			{
				return optFailResponseEntity(request, HttpStatus.UNAUTHORIZED, "checkCodeError");
			}
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

	protected AuthenticationException getAuthenticationExceptionWithRemove(HttpServletRequest request)
	{
		// 参考org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler.saveException()

		AuthenticationException authenticationException = (AuthenticationException) request
				.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		if (authenticationException == null)
		{
			HttpSession session = request.getSession();

			authenticationException = (AuthenticationException) session
					.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

			if (authenticationException != null)
				session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}

		return authenticationException;
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

	/**
	 * 将用户名编码为可存储至Cookie的字符串。
	 * 
	 * @param username
	 * @return
	 */
	public static String encodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return URLEncoder.encode(username, IOUtil.CHARSET_UTF_8);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}

	/**
	 * 将{@linkplain #encodeCookieUserName(String)}的用户名解码。
	 * 
	 * @param username
	 * @return
	 */
	public static String decodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return WebUtils.decodeURL(username);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}
}
