/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
