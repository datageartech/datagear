/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.management.impl;

import static org.junit.Assert.assertEquals;

import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.RoleUserService;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.RoleUserServiceImpl;
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
		RoleUserService roleUserService = new RoleUserServiceImpl(getSqlSessionFactory());
		RoleService roleService = new RoleServiceImpl(getSqlSessionFactory());
		this.userServiceImpl = new UserServiceImpl(getSqlSessionFactory(), roleUserService, roleService);
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
