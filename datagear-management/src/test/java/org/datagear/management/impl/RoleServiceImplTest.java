/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.impl;

import static org.junit.Assert.assertEquals;

import org.datagear.management.domain.Role;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.junit.Test;

/**
 * {@linkplain RoleServiceImpl}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class RoleServiceImplTest extends ServiceImplTestSupport
{
	private RoleServiceImpl roleServiceImpl;

	public RoleServiceImplTest()
	{
		super();
		this.roleServiceImpl = new RoleServiceImpl(getSqlSessionFactory());
	}

	@Test
	public void test()
	{
		String id = "id-for-test";
		String name = "name-for-test";

		try
		{
			this.roleServiceImpl.add(new Role(id, name));

			Role role = this.roleServiceImpl.getById(id);

			assertEquals(id, role.getId());
			assertEquals(name, role.getName());
		}
		finally
		{
			this.roleServiceImpl.deleteById(id);
		}
	}
}
