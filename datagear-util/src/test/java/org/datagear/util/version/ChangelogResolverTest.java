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

package org.datagear.util.version;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * {@linkplain ChangelogResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ChangelogResolverTest
{
	private ChangelogResolver resolver = new ChangelogResolver(
			new ClassPathResource("org/datagear/util/version/changelog.txt"));

	@Test
	public void resolveAllTest() throws Exception
	{
		List<VersionContent> versions = resolver.resolveAll();

		assertEquals(2, versions.size());

		{
			VersionContent vc = versions.get(0);
			List<String> contents = vc.getContents();

			assertEquals("2.0.0", vc.getVersion().toString());
			assertEquals(3, contents.size());
			assertEquals("日志2", contents.get(0));
			assertEquals("日志3", contents.get(1));
			assertEquals("日志4", contents.get(2));
		}

		{
			VersionContent vc = versions.get(1);
			List<String> contents = vc.getContents();

			assertEquals("1.0.0", vc.getVersion().toString());
			assertEquals(2, contents.size());
			assertEquals("日志0", contents.get(0));
			assertEquals("日志1", contents.get(1));
		}
	}

	@Test
	public void resolveRecentsTest() throws Exception
	{
		List<VersionContent> versions = resolver.resolveRecents(1);

		assertEquals(1, versions.size());

		{
			VersionContent vc = versions.get(0);
			List<String> contents = vc.getContents();

			assertEquals("2.0.0", vc.getVersion().toString());
			assertEquals(3, contents.size());
			assertEquals("日志2", contents.get(0));
			assertEquals("日志3", contents.get(1));
			assertEquals("日志4", contents.get(2));
		}
	}
}
