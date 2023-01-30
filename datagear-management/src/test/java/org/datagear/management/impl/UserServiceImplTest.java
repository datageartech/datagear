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

package org.datagear.management.impl;

import static org.junit.Assert.assertEquals;

import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.UserServiceImpl;
import org.junit.Test;

/**
 * {@linkplain UserServiceImpl}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class UserServiceImplTest extends ServiceImplTestSupport
{
	private UserServiceImpl userServiceImpl;

	public UserServiceImplTest()
	{
		super();
		RoleService roleService = new RoleServiceImpl(getSqlSessionFactory(), getDialect());
		this.userServiceImpl = new UserServiceImpl(getSqlSessionFactory(), getDialect(), roleService);
	}

	@Test
	public void test()
	{
		String id = "id-for-test";
		String name = "name-for-test";

		try
		{
			this.userServiceImpl.add(new User(id, name, "psd"));

			User user = this.userServiceImpl.getById(id);

			assertEquals(id, user.getId());
			assertEquals(name, user.getName());
		}
		finally
		{
			this.userServiceImpl.deleteById(id);
		}
	}
}
