/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.config.support;

import org.datagear.web.controller.AbstractController;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;

/**
 * 自定义{@linkplain ErrorPageRegistrar}。
 * 
 * @author datagear@163.com
 *
 */
public class CustomErrorPageRegistrar implements ErrorPageRegistrar
{
	@Override
	public void registerErrorPages(ErrorPageRegistry registry)
	{
		registry.addErrorPages(new ErrorPage(AbstractController.ERROR_PAGE_URL));
	}
}
