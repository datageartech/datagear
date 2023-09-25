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

package org.datagear.management.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.domain.SchemaProperty;
import org.datagear.management.domain.SchemaPropertyPattern;
import org.junit.Test;

/**
 * {@linkplain SchemaGuardChecker}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuardCheckerTest
{
	private SchemaGuardChecker schemaGuardChecker = new SchemaGuardChecker();

	private List<SchemaProperty> schemaProperties = Arrays.asList(new SchemaProperty("firstProperty", "123"),
			new SchemaProperty("secondProperty", "456"));

	@Test
	public void isPermittedTest()
	{
		// 防护列表为空
		{
			List<SchemaGuard> schemaGuards = Collections.emptyList();

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 全部允许
		{
			List<SchemaGuard> schemaGuards = Arrays.asList( //
					new SchemaGuard("1", "*", true));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 全部禁止
		{
			List<SchemaGuard> schemaGuards = Arrays.asList( //
					new SchemaGuard("1", "*", false));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅允许指定URL
		{
			List<SchemaGuard> schemaGuards = Arrays.asList( //
					new SchemaGuard("1", "*192.168.1.1*"), //
					new SchemaGuard("2", "*", false));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定URL
		{
			List<SchemaGuard> schemaGuards = Arrays.asList( //
					new SchemaGuard("1", "*192.168.1.1*", false), //
					new SchemaGuard("2", "*", true));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定用户名
		{
			List<SchemaGuard> schemaGuards = Arrays.asList( //
					new SchemaGuard("1", "*", "root", false), //
					new SchemaGuard("2", "*", "*", true));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "dg")));
		}

		// 仅禁止指定URL、且包含指定属性名
		{
			SchemaGuard schemaGuard0 = new SchemaGuard("1", "*192.168.1.1*", false);
			schemaGuard0.setPropertyPatterns(Arrays.asList(new SchemaPropertyPattern("*first*")));

			List<SchemaGuard> schemaGuards = Arrays.asList( //
					schemaGuard0, //
					new SchemaGuard("2", "*", true));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root", schemaProperties)));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root", schemaProperties)));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定URL、或者包含指定属性名
		{
			SchemaGuard schemaGuard0 = new SchemaGuard("1", "*192.168.1.1*", false);

			SchemaGuard schemaGuard1 = new SchemaGuard("2", "*", false);
			schemaGuard1.setPropertyPatterns(Arrays.asList(new SchemaPropertyPattern("*first*")));

			List<SchemaGuard> schemaGuards = Arrays.asList( //
					schemaGuard0, //
					schemaGuard1, //
					new SchemaGuard("3", "*", true));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root", schemaProperties)));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root", schemaProperties)));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));

			assertTrue(this.schemaGuardChecker.isPermitted(schemaGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root",
							Arrays.asList(new SchemaProperty("secondProperty", "456")))));
		}
	}

	@Test
	public void isUrlMatchedTest()
	{
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*192.168.1.1*");

			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertFalse(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 忽略大小写
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*abc*");

			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("abc")));
			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard, new GuardEntity("aBc")));
			assertFalse(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("def")));
		}

		// 模式为null
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", null);

			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 模式为""
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "");

			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 目标为null
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "");

			assertTrue(this.schemaGuardChecker.isUrlMatched(schemaGuard, new GuardEntity((String) null)));
		}
	}

	@Test
	public void isUserMatchedTest()
	{
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "", "*root*");

			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard,
					new GuardEntity("", "root")));
			assertFalse(this.schemaGuardChecker.isUserMatched(schemaGuard, new GuardEntity("", "test")));
		}

		// 忽略大小写
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "", "*abc*");

			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard, new GuardEntity("", "abc")));
			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard, new GuardEntity("", "aBc")));
			assertFalse(this.schemaGuardChecker.isUserMatched(schemaGuard, new GuardEntity("", "def")));
		}

		// 模式为null
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "", null);

			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard,
					new GuardEntity("", "abc")));
			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard,
					new GuardEntity("", "def")));
		}

		// 模式为""
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "");

			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard,
					new GuardEntity("", "abc")));
			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard,
					new GuardEntity("", "def")));
		}

		// 目标为null
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "", "");

			assertTrue(this.schemaGuardChecker.isUserMatched(schemaGuard, new GuardEntity((String) null, null)));
		}
	}

	@Test
	public void isPropertiesMatchedTest()
	{
		// 空属性列表匹配模式
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setEmptyPropertyPatternsForAll(true);

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "")));

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", schemaProperties)));
		}
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setEmptyPropertyPatternsForAll(false);

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "")));

			assertFalse(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", schemaProperties)));
		}

		// 目标属性列表为空
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setPropertyPatterns(Arrays.asList(new SchemaPropertyPattern("*first*")));

			assertFalse(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "")));
		}

		// 匹配任一属性名
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setPropertyPatterns(
					Arrays.asList(new SchemaPropertyPattern("*first*"), new SchemaPropertyPattern("*second*")));

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", schemaProperties)));

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "",
							Arrays.asList(new SchemaProperty("firstProperty", "123"),
									new SchemaProperty("werwerw23", "123")))));

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard, new GuardEntity("", "dg", Arrays
					.asList(new SchemaProperty("secondsProperty", "123"), new SchemaProperty("werwerw23", "123")))));
		}
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setPropertyPatterns(
					Arrays.asList(new SchemaPropertyPattern("*first*"), new SchemaPropertyPattern("*second*")));
			schemaGuard.setPropertiesMatchMode(SchemaGuard.PROPERTIES_MATCH_MODE_ANY);

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", schemaProperties)));

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "",
							Arrays.asList(new SchemaProperty("firstProperty", "123")))));
		}

		// 匹配所有属性名
		{
			SchemaGuard schemaGuard = new SchemaGuard("1", "*", "*", true);
			schemaGuard.setPropertyPatterns(
					Arrays.asList(new SchemaPropertyPattern("*first*"), new SchemaPropertyPattern("*second*")));
			schemaGuard.setPropertiesMatchMode(SchemaGuard.PROPERTIES_MATCH_MODE_ALL);

			assertTrue(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", schemaProperties)));

			assertFalse(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", Arrays.asList(new SchemaProperty("firstProperty", "123")))));

			assertFalse(this.schemaGuardChecker.isPropertiesMatched(schemaGuard,
					new GuardEntity("", "", Arrays.asList(new SchemaProperty("secondProperty", "123")))));

		}
	}
}
