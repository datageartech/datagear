/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.web.controller.LoginController;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 用于ajax的{@linkplain AuthenticationSuccessHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler
{
	private List<CreateUserEntityService> createUserEntityServices;

	private UsernameLoginLatch usernameLoginLatch;

	private CheckCodeManager checkCodeManager;

	public AuthenticationSuccessHandlerImpl(UsernameLoginLatch usernameLoginLatch, CheckCodeManager checkCodeManager)
	{
		super();
		this.usernameLoginLatch = usernameLoginLatch;
		this.checkCodeManager = checkCodeManager;
	}

	public List<CreateUserEntityService> getCreateUserEntityServices()
	{
		return createUserEntityServices;
	}

	public void setCreateUserEntityServices(List<CreateUserEntityService> createUserEntityServices)
	{
		this.createUserEntityServices = createUserEntityServices;
	}

	public UsernameLoginLatch getUsernameLoginLatch()
	{
		return usernameLoginLatch;
	}

	public void setUsernameLoginLatch(UsernameLoginLatch usernameLoginLatch)
	{
		this.usernameLoginLatch = usernameLoginLatch;
	}

	public CheckCodeManager getCheckCodeManager()
	{
		return checkCodeManager;
	}

	public void setCheckCodeManager(CheckCodeManager checkCodeManager)
	{
		this.checkCodeManager = checkCodeManager;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException
	{
		clearLoginCheckCode(request, response, authentication);
		clearUsernameLoginLatch(request, response, authentication);

		migrateAnonymousUserData(request, response, authentication);
		request.getRequestDispatcher("/login/success").forward(request, response);
	}

	protected void clearLoginCheckCode(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		this.checkCodeManager.removeCheckCode(request.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN);
	}

	protected void clearUsernameLoginLatch(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		User user = WebUtils.getUser(authentication);
		this.usernameLoginLatch.clear(user.getName());
	}

	/**
	 * 将匿名用户的数据迁移至登录用户上。
	 * 
	 * @param request
	 * @param response
	 * @param loginUser
	 */
	protected void migrateAnonymousUserData(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		User user = WebUtils.getUser(authentication);

		if (user.isAnonymous())
			return;

		// 不迁移至管理员用户上
		if (user.isAdmin())
			return;

		String anonymousUserId = WebUtils.getCookieValue(request,
				AnonymousAuthenticationFilterExt.COOKIE_USER_ID_ANONYMOUS);

		if (anonymousUserId != null && !anonymousUserId.isEmpty())
		{
			if (this.createUserEntityServices != null)
			{
				for (CreateUserEntityService service : this.createUserEntityServices)
				{
					service.updateCreateUserId(anonymousUserId, user.getId());
				}
			}
		}
	}
}
