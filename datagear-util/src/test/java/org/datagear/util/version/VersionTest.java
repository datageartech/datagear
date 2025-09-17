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

import org.junit.Test;

/**
 * {@linkplain Version}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class VersionTest
{
	@Test
	public void isLowerThanTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("2.5");

		assertTrue(a.isLowerThan(b));
		assertFalse(b.isLowerThan(a));
	}

	@Test
	public void isHigherThanTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("2.5");

		assertFalse(a.isHigherThan(b));
		assertTrue(b.isHigherThan(a));
	}

	@Test
	public void isEqualToTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("1.0.0");
		Version c = Version.valueOf("2.5");

		assertTrue(a.isEqualTo(b));
		assertTrue(b.isEqualTo(a));
		assertFalse(a.isEqualTo(c));
		assertFalse(b.isEqualTo(c));
	}

	@Test
	public void stringValueTest()
	{
		{
			String v = "1.0";
			Version version = Version.valueOf(v);
			assertEquals("1.0.0", version.stringValue());
		}

		{
			String v = "1.0.0";
			Version version = Version.valueOf(v);
			assertEquals(v, version.stringValue());
		}

		{
			String v = "1.0.0-a1";
			Version version = Version.valueOf(v);
			assertEquals(v, version.stringValue());
		}
	}

	@Test
	public void compareToTest()
	{
		{
			Version a = Version.valueOf("1.0");
			Version b = Version.valueOf("2.0");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.1");
			Version b = Version.valueOf("1.2");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.1.1");
			Version b = Version.valueOf("1.1.2");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0");
			Version b = Version.valueOf("2.0-a1");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0.0-a1");
			Version b = Version.valueOf("1.0");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0.0-a1");
			Version b = Version.valueOf("1.0-a2");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0.0-a1");
			Version b = Version.valueOf("1.0.0-a2");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0.0-a1");
			Version b = Version.valueOf("1.0.0-b");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}

		{
			Version a = Version.valueOf("1.0.0-a1");
			Version b = Version.valueOf("1.0.0-release");

			assertTrue(a.compareTo(b) < 0);
			assertTrue(b.compareTo(a) > 0);
		}
	}
}
