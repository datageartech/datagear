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

package org.datagear.management.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datagear.management.domain.DtbsSourceGuard;
import org.datagear.management.domain.DtbsSourceProperty;
import org.datagear.management.domain.DtbsSourcePropertyPattern;
import org.junit.Test;

/**
 * {@linkplain DtbsSourceGuardChecker}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceGuardCheckerTest
{
	private DtbsSourceGuardChecker dtbsSourceGuardChecker = new DtbsSourceGuardChecker();

	private List<DtbsSourceProperty> dtbsSourceProperties = Arrays.asList(
			new DtbsSourceProperty("firstProperty", "123"),
			new DtbsSourceProperty("secondProperty", "456"));

	@Test
	public void isPermittedTest()
	{
		// 防护列表为空
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Collections.emptyList();

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 全部允许
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					new DtbsSourceGuard("1", "1", "1", "*", true));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 全部禁止
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					new DtbsSourceGuard("1", "1", "*", false));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅允许指定URL
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					new DtbsSourceGuard("1", "1", "*192.168.1.1*"), //
					new DtbsSourceGuard("2", "2", "*", false));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定URL
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					new DtbsSourceGuard("1", "1", "*192.168.1.1*", false), //
					new DtbsSourceGuard("2", "2", "*", true));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定用户名
		{
			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					new DtbsSourceGuard("1", "1", "*", "root", false), //
					new DtbsSourceGuard("2", "2", "*", "*", true));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "dg")));
		}

		// 仅禁止指定URL、且包含指定属性名
		{
			DtbsSourceGuard dtbsSourceGuard0 = new DtbsSourceGuard("1", "1", "*192.168.1.1*", false);
			dtbsSourceGuard0.setPropertyPatterns(Arrays.asList(new DtbsSourcePropertyPattern("*first*")));

			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					dtbsSourceGuard0, //
					new DtbsSourceGuard("2", "2", "*", true));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root", dtbsSourceProperties)));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root", dtbsSourceProperties)));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));
		}

		// 仅禁止指定URL、或者包含指定属性名
		{
			DtbsSourceGuard dtbsSourceGuard0 = new DtbsSourceGuard("1", "1", "*192.168.1.1*", false);

			DtbsSourceGuard dtbsSourceGuard1 = new DtbsSourceGuard("2", "2", "*", false);
			dtbsSourceGuard1.setPropertyPatterns(Arrays.asList(new DtbsSourcePropertyPattern("*first*")));

			List<DtbsSourceGuard> dtbsSourceGuards = Arrays.asList( //
					dtbsSourceGuard0, //
					dtbsSourceGuard1, //
					new DtbsSourceGuard("3", "3", "*", true));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root", dtbsSourceProperties)));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test", "root")));

			assertFalse(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root", dtbsSourceProperties)));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPermitted(dtbsSourceGuards,
					new GuardEntity("jdbc:mysql://192.168.1.2:3306/test", "root",
							Arrays.asList(new DtbsSourceProperty("secondProperty", "456")))));
		}
	}

	@Test
	public void isUrlMatchedTest()
	{
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*192.168.1.1*");

			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertFalse(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 忽略大小写
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*abc*");

			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("abc")));
			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard, new GuardEntity("aBc")));
			assertFalse(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("def")));
		}

		// 模式为null
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", null);

			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 模式为""
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "");

			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://192.168.1.1:3306/test")));
			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard,
					new GuardEntity("jdbc:mysql://127.0.0.1:3306/test")));
		}

		// 目标为null
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "");

			assertTrue(this.dtbsSourceGuardChecker.isUrlMatched(dtbsSourceGuard, new GuardEntity((String) null)));
		}
	}

	@Test
	public void isUserMatchedTest()
	{
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "", "*root*");

			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard,
					new GuardEntity("", "root")));
			assertFalse(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard, new GuardEntity("", "test")));
		}

		// 忽略大小写
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "", "*abc*");

			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard, new GuardEntity("", "abc")));
			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard, new GuardEntity("", "aBc")));
			assertFalse(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard, new GuardEntity("", "def")));
		}

		// 模式为null
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "", null);

			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard,
					new GuardEntity("", "abc")));
			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard,
					new GuardEntity("", "def")));
		}

		// 模式为""
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "");

			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard,
					new GuardEntity("", "abc")));
			assertTrue(this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard,
					new GuardEntity("", "def")));
		}

		// 目标为null
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "", "");

			assertTrue(
					this.dtbsSourceGuardChecker.isUserMatched(dtbsSourceGuard, new GuardEntity((String) null, null)));
		}
	}

	@Test
	public void isPropertiesMatchedTest()
	{
		// 空属性列表匹配模式
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setEmptyPropertyPatternsForAll(true);

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "")));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));
		}
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setEmptyPropertyPatternsForAll(false);

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "")));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));
		}

		// 目标属性列表为空
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setPropertyPatterns(Arrays.asList(new DtbsSourcePropertyPattern("*first*")));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "")));
		}

		// 匹配任一属性名
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setPropertyPatterns(
					Arrays.asList(new DtbsSourcePropertyPattern("*first*"), new DtbsSourcePropertyPattern("*second*")));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "",
							Arrays.asList(new DtbsSourceProperty("firstProperty", "123"),
									new DtbsSourceProperty("werwerw23", "123")))));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard, new GuardEntity("", "dg", Arrays
					.asList(new DtbsSourceProperty("secondsProperty", "123"), new DtbsSourceProperty("werwerw23", "123")))));
		}
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setPropertyPatterns(
					Arrays.asList(new DtbsSourcePropertyPattern("*first*"), new DtbsSourcePropertyPattern("*second*")));
			dtbsSourceGuard.setPropertiesMatchMode(DtbsSourceGuard.PROPERTIES_MATCH_MODE_ANY);

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "",
							Arrays.asList(new DtbsSourceProperty("firstProperty", "123")))));
		}

		// 匹配所有属性名
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setPropertyPatterns(
					Arrays.asList(new DtbsSourcePropertyPattern("*first*"), new DtbsSourcePropertyPattern("*second*")));
			dtbsSourceGuard.setPropertiesMatchMode(DtbsSourceGuard.PROPERTIES_MATCH_MODE_ALL);

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", Arrays.asList(new DtbsSourceProperty("firstProperty", "123")))));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", Arrays.asList(new DtbsSourceProperty("secondProperty", "123")))));
		}

		// 匹配属性名、属性值
		{
			DtbsSourceGuard dtbsSourceGuard = new DtbsSourceGuard("1", "1", "*", "*", true);
			dtbsSourceGuard.setPropertyPatterns(
					Arrays.asList(new DtbsSourcePropertyPattern("*first*", "root")));

			assertTrue(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", Arrays.asList(new DtbsSourceProperty("firstProperty", "root")))));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", dtbsSourceProperties)));

			assertFalse(this.dtbsSourceGuardChecker.isPropertiesMatched(dtbsSourceGuard,
					new GuardEntity("", "", Arrays.asList(new DtbsSourceProperty("firstProperty", "123")))));
		}
	}
}
