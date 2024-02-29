/*
 * Copyright 2018-2024 datagear.tech
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
		this.roleServiceImpl = new RoleServiceImpl(getSqlSessionFactory(), getDialect());
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
