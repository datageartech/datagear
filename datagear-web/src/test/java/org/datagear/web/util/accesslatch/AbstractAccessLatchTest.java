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

package org.datagear.web.util.accesslatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@linkplain AbstractAccessLatch}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractAccessLatchTest
{
	@Test
	public void remainTest() throws Exception
	{
		String identity0 = "user0";
		String identity1 = "user1";
		String resource = "resource";

		// 不限
		{
			SimpleAccessLatch al = new SimpleAccessLatch(-1, 3);

			assertTrue(AccessLatch.isNonLatch(al.remain(identity0, resource)));
			assertTrue(AccessLatch.isNonLatch(al.remain(identity1, resource)));
		}
		{
			SimpleAccessLatch al = new SimpleAccessLatch(30, -1);
			assertTrue(AccessLatch.isNonLatch(al.remain(identity0, resource)));
			assertTrue(AccessLatch.isNonLatch(al.remain(identity1, resource)));
		}
		{
			SimpleAccessLatch al = new SimpleAccessLatch(-1, -1);
			assertTrue(AccessLatch.isNonLatch(al.remain(identity0, resource)));
			assertTrue(AccessLatch.isNonLatch(al.remain(identity1, resource)));
		}

		{
			int seconds = 3;
			SimpleAccessLatch al = new SimpleAccessLatch(seconds, 3);

			{
				assertEquals(3, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(2, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(1, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(0, al.remain(identity0, resource));

				assertFalse(al.access(identity0, resource));
				assertFalse(al.access(identity0, resource));
			}

			{
				assertEquals(3, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(2, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(1, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(0, al.remain(identity1, resource));

				assertFalse(al.access(identity1, resource));
				assertFalse(al.access(identity1, resource));
			}

			Thread.sleep((seconds + 1) * 1000);

			{
				assertEquals(3, al.remain(identity0, resource));
				assertEquals(3, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(2, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(1, al.remain(identity0, resource));

				assertTrue(al.access(identity0, resource));
				assertEquals(0, al.remain(identity0, resource));

				assertFalse(al.access(identity0, resource));
				assertFalse(al.access(identity0, resource));
			}

			{
				assertEquals(3, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(2, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(1, al.remain(identity1, resource));

				assertTrue(al.access(identity1, resource));
				assertEquals(0, al.remain(identity1, resource));

				assertFalse(al.access(identity1, resource));
				assertFalse(al.access(identity1, resource));
			}
		}
	}
}
