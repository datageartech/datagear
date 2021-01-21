/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
