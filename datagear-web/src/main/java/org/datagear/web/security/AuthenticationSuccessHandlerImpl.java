/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.web.util.WebUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * {@linkplain AuthenticationSuccessHandler}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler
{
	private SchemaService schemaService;

	public AuthenticationSuccessHandlerImpl()
	{
		super();
	}

	public AuthenticationSuccessHandlerImpl(SchemaService schemaService)
	{
		super();
		this.schemaService = schemaService;
	}

	public SchemaService getSchemaService()
	{
		return schemaService;
	}

	public void setSchemaService(SchemaService schemaService)
	{
		this.schemaService = schemaService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException
	{
		mergeAnonymousUserSchema(request, response, authentication);

		response.sendRedirect(request.getContextPath() + "/");
	}

	/**
	 * 将匿名用户的{@linkplain Schema}迁移至登录用户上。
	 * 
	 * @param request
	 * @param response
	 * @param loginUser
	 */
	protected void mergeAnonymousUserSchema(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		User loginUser = WebUtils.getUser(authentication);

		if (loginUser == null)
			return;

		String anonymousUserId = WebUtils.getCookieValue(request, WebUtils.COOKIE_USER_ID_ANONYMOUS);

		if (anonymousUserId != null && !anonymousUserId.isEmpty())
			this.schemaService.updateCreateUserId(anonymousUserId, loginUser.getId());
	}
}
