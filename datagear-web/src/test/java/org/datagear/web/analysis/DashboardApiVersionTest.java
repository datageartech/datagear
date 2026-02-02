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

package org.datagear.web.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain DashboardApiVersion}单元测试
 * 
 * @author datagear@163.com
 *
 */
public class DashboardApiVersionTest
{
	@Test
	public void isV1Test()
	{
		assertTrue(DashboardApiVersion.isV1("1.0"));
		assertFalse(DashboardApiVersion.isV1("2.0"));
		assertFalse(DashboardApiVersion.isV1(null));
	}

	@Test
	public void isV2Test()
	{
		assertTrue(DashboardApiVersion.isV2("2.0"));
		assertFalse(DashboardApiVersion.isV2("1.0"));
		assertFalse(DashboardApiVersion.isV2(null));
	}

	@Test
	public void toValidVersionTest()
	{
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.toValidVersion(null));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.toValidVersion(""));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.toValidVersion("  "));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.toValidVersion("1.0"));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.toValidVersion(" 1.0 "));
		assertEquals(DashboardApiVersion.V2, DashboardApiVersion.toValidVersion("2.0"));
		assertEquals(DashboardApiVersion.V2, DashboardApiVersion.toValidVersion(" 2.0 "));
		assertEquals(DashboardApiVersion.V2, DashboardApiVersion.toValidVersion("sdf"));
	}

	@Test
	public void trimVersionTest()
	{
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.trimVersion(null));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.trimVersion(""));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.trimVersion("  "));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.trimVersion("1.0"));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.trimVersion(" 1.0 "));
		assertEquals(DashboardApiVersion.V2, DashboardApiVersion.trimVersion("2.0"));
		assertEquals("2.0", DashboardApiVersion.trimVersion(" 2.0 "));
		assertEquals("sdf", DashboardApiVersion.trimVersion("sdf"));
	}

	@Test
	public void normalizeTest()
	{
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.normalize(null, null));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.normalize("", "sdf"));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.normalize("  ", "sdf"));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.normalize("1.0", "sdf"));
		assertEquals(DashboardApiVersion.V1, DashboardApiVersion.normalize(" 1.0 ", "sdf"));
		assertEquals(DashboardApiVersion.V2, DashboardApiVersion.normalize("2.0", "sdf"));
		assertEquals("2.0", DashboardApiVersion.normalize(" 2.0 ", "sdf"));
		assertEquals("sdf", DashboardApiVersion.normalize("sdf", "sdf"));
	}
}
