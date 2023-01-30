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

package org.datagear.management.dbversion;

import java.io.IOException;
import java.util.List;

import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain DbVersionManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DbVersionManagerTest
{
	private DbVersionManager dbVersionManager = new DbVersionManager();

	@Test
	public void resolveUpgradeSqlVersionContentsTest() throws IOException
	{
		List<VersionContent> versionContents = dbVersionManager.resolveUpgradeSqlVersionContents(null);

		Assert.assertTrue(versionContents.size() > 0);

		{
			VersionContent versionContent = versionContents.get(0);
			List<String> contents = versionContent.getContents();

			Assert.assertEquals(Version.valueOf("2.13.0"), versionContent.getVersion());

			Assert.assertTrue(contents.get(0).startsWith("CREATE TABLE DATAGEAR_VERSION"));
			Assert.assertTrue(contents.get(0).endsWith(")"));

			Assert.assertTrue(contents.get(1).startsWith("CREATE TABLE DATAGEAR_USER"));
			Assert.assertTrue(contents.get(1).endsWith(")"));

			Assert.assertTrue(contents.get(contents.size() - 1).startsWith("CREATE FUNCTION DATAGEAR_FUNC_MODINT"));
			Assert.assertTrue(contents.get(contents.size() - 1).endsWith(".modInt'"));
		}

		{
			VersionContent versionContent = versionContents.get(1);
			List<String> contents = versionContent.getContents();

			Assert.assertEquals(Version.valueOf("3.0.0"), versionContent.getVersion());

			Assert.assertTrue(contents.get(0).startsWith("CREATE TABLE DATAGEAR_DB_SHARE_SET"));
			Assert.assertTrue(contents.get(0).endsWith(")"));
		}
	}
}
