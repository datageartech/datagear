/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.management.dbversion;

import java.io.IOException;
import java.util.List;

import org.datagear.management.util.Version;
import org.datagear.management.util.VersionContent;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain DbVersionManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DbVersionManagerTestTest
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

			Assert.assertEquals(Version.valueOf("1.0.0"), versionContent.getVersion());
			Assert.assertEquals(7, contents.size());

			Assert.assertTrue(contents.get(0).startsWith("CREATE TABLE DATAGEAR_VERSION"));
			Assert.assertTrue(contents.get(0).endsWith(")"));

			Assert.assertTrue(contents.get(1).startsWith("CREATE TABLE DATAGEAR_GLOBALSETTING"));
			Assert.assertTrue(contents.get(1).endsWith(")"));

			Assert.assertTrue(contents.get(2).startsWith("CREATE TABLE DATAGEAR_USER"));
			Assert.assertTrue(contents.get(2).endsWith(")"));

			Assert.assertTrue(contents.get(3).startsWith("INSERT INTO DATAGEAR_USER"));
			Assert.assertTrue(contents.get(3).endsWith("CURRENT_TIMESTAMP)"));

			Assert.assertTrue(contents.get(4).startsWith("CREATE TABLE DATAGEAR_RESET_PSD_REQUEST"));
			Assert.assertTrue(contents.get(4).endsWith(")"));

			Assert.assertTrue(contents.get(5).startsWith("CREATE TABLE DATAGEAR_RESET_PSD_REQUEST_HISTORY"));
			Assert.assertTrue(contents.get(5).endsWith(")"));

			Assert.assertTrue(contents.get(6).startsWith("CREATE TABLE DATAGEAR_SCHEMA"));
			Assert.assertTrue(contents.get(6).endsWith(")"));
		}
	}
}
