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

package org.datagear.util.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * {@linkplain VersionPattern}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class VersionPatternTest
{
	private VersionPattern pattern = new VersionPattern();

	@Test
	public void matchesTest()
	{
		{
			String p = null;

			assertTrue(pattern.matches(p, "0.0"));
			assertTrue(pattern.matches(p, "1.0"));
			assertTrue(pattern.matches(p, "5.0"));
		}

		{
			String p = "";

			assertTrue(pattern.matches(p, "0.0"));
			assertTrue(pattern.matches(p, "1.0"));
			assertTrue(pattern.matches(p, "5.0"));
		}

		{
			String p = "   ";

			assertTrue(pattern.matches(p, "0.0"));
			assertTrue(pattern.matches(p, "1.0"));
			assertTrue(pattern.matches(p, "5.0"));
		}

		{
			String p = "1.0";

			assertTrue(pattern.matches(p, "1.0"));
			assertTrue(pattern.matches(p, "1.0.0"));
			assertFalse(pattern.matches(p, "1.0.1"));
			assertFalse(pattern.matches(p, "5.0"));
		}

		{
			String p = "1.0.3";

			assertFalse(pattern.matches(p, "1.0"));
			assertFalse(pattern.matches(p, "1.0.0"));
			assertFalse(pattern.matches(p, "1.0.1"));
			assertTrue(pattern.matches(p, "1.0.3"));
			assertFalse(pattern.matches(p, "5.0"));
		}

		{
			String p = "^1.2";

			assertFalse(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
		}

		{
			String p = "^1.2.3";

			assertFalse(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertFalse(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.9"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
		}

		{
			String p = "~1.2";

			assertFalse(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
		}

		{
			String p = "~1.2.3";

			assertFalse(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertFalse(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
		}

		{
			String p = ">=1.2";

			assertFalse(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertTrue(pattern.matches(p, "2.0"));
			assertTrue(pattern.matches(p, "9.25.3"));
		}

		{
			String p = ">=1.2.1";

			assertFalse(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertTrue(pattern.matches(p, "2.0"));
			assertTrue(pattern.matches(p, "9.25.3"));
		}

		{
			String p = "<=1.2";

			assertTrue(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertFalse(pattern.matches(p, "1.2.1"));
			assertFalse(pattern.matches(p, "1.2.3"));
			assertFalse(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = "<=1.2.1";

			assertTrue(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertFalse(pattern.matches(p, "1.2.3"));
			assertFalse(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = "<1.2";

			assertTrue(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertFalse(pattern.matches(p, "1.2.1"));
			assertFalse(pattern.matches(p, "1.2.3"));
			assertFalse(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = "<1.2.1";

			assertTrue(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertFalse(pattern.matches(p, "1.2.1"));
			assertFalse(pattern.matches(p, "1.2.3"));
			assertFalse(pattern.matches(p, "1.2.99"));
			assertFalse(pattern.matches(p, "1.3"));
			assertFalse(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = ">=1.2 <2.0";

			assertFalse(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = ">=1.2 <=2.0";

			assertFalse(pattern.matches(p, "1.1"));
			assertTrue(pattern.matches(p, "1.2"));
			assertTrue(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertTrue(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = ">1.2 <2.0";

			assertFalse(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertFalse(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}

		{
			String p = ">1.2 <=2.0";

			assertFalse(pattern.matches(p, "1.1"));
			assertFalse(pattern.matches(p, "1.2"));
			assertFalse(pattern.matches(p, "1.2.0"));
			assertTrue(pattern.matches(p, "1.2.1"));
			assertTrue(pattern.matches(p, "1.2.3"));
			assertTrue(pattern.matches(p, "1.2.99"));
			assertTrue(pattern.matches(p, "1.3"));
			assertTrue(pattern.matches(p, "1.9.5"));
			assertTrue(pattern.matches(p, "2.0"));
			assertFalse(pattern.matches(p, "9.25.3"));
		}
	}

	@Test
	public void matchesOfStringTest()
	{
		String p = ">1.2 <=2.0";

		List<String> vs = Arrays.asList("1.1", "1.2", "1.2.0", "1.2.1", "1.2.3", "1.2.99", "1.3", "1.9.5", "2.0",
				"9.25.3");
		List<String> re = pattern.matchesOfString(p, vs);

		assertEquals(6, re.size());

		assertEquals("1.2.1", re.get(0));
		assertEquals("1.2.3", re.get(1));
		assertEquals("1.2.99", re.get(2));
		assertEquals("1.3", re.get(3));
		assertEquals("1.9.5", re.get(4));
		assertEquals("2.0", re.get(5));
	}

	@Test
	public void matchesOfVersionTest()
	{
		String p = ">1.2 <=2.0";

		List<Version> vs = Arrays.asList(Version.valueOf("1.1"), Version.valueOf("1.2"), Version.valueOf("1.2.0"),
				Version.valueOf("1.2.1"), Version.valueOf("1.2.3"), Version.valueOf("1.2.99"), Version.valueOf("1.3"),
				Version.valueOf("1.9.5"), Version.valueOf("2.0"), Version.valueOf("9.25.3"));
		List<Version> re = pattern.matchesOfVersion(p, vs);

		assertEquals(6, re.size());

		assertEquals(Version.valueOf("1.2.1"), re.get(0));
		assertEquals(Version.valueOf("1.2.3"), re.get(1));
		assertEquals(Version.valueOf("1.2.99"), re.get(2));
		assertEquals(Version.valueOf("1.3"), re.get(3));
		assertEquals(Version.valueOf("1.9.5"), re.get(4));
		assertEquals(Version.valueOf("2.0"), re.get(5));
	}
}
