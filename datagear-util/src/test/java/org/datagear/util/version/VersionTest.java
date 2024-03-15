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
		Version b = Version.valueOf("1.10");
		Version c = Version.valueOf("1.0");
		Version d = Version.valueOf("2.1");

		assertTrue(a.isLowerThan(b));
		assertFalse(b.isLowerThan(a));
		assertFalse(a.isEqual(b));

		assertFalse(a.isLowerThan(c));
		assertFalse(c.isLowerThan(a));
		assertTrue(a.isEqual(c));

		assertTrue(a.isLowerThan(d));
		assertFalse(d.isLowerThan(a));
		assertFalse(a.isEqual(d));
	}

	@Test
	public void isHigherThanTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("1.10");
		Version c = Version.valueOf("1.0");
		Version d = Version.valueOf("2.1");

		assertFalse(a.isHigherThan(b));
		assertTrue(b.isHigherThan(a));
		assertFalse(a.isEqual(b));

		assertFalse(a.isHigherThan(c));
		assertFalse(c.isHigherThan(a));
		assertTrue(a.isEqual(c));

		assertFalse(a.isHigherThan(d));
		assertTrue(d.isHigherThan(a));
		assertFalse(a.isEqual(d));
	}

	@Test
	public void isEqualTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("1.10");
		Version c = Version.valueOf("1.0");

		assertFalse(a.isEqual(b));
		assertTrue(a.equals(c));
	}

	@Test
	public void stringOfTest()
	{
		{
			String v = "1.0.0";
			Version version = Version.valueOf(v);
			assertEquals(v, version.stringOf());
			assertEquals(v, Version.stringOf(version));
		}

		{
			String v = "1.0.0-a1";
			Version version = Version.valueOf(v);
			assertEquals(v, version.stringOf());
			assertEquals(v, Version.stringOf(version));
		}
	}

	@Test
	public void compareToTest()
	{
		Version a = Version.valueOf("1.0");
		Version b = Version.valueOf("1.10");
		Version c = Version.valueOf("1.0");

		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
		assertEquals(0, a.compareTo(c));
		assertEquals(0, c.compareTo(a));
	}
}
