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
