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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.datagear.management.impl.ServiceImplTestSupport;
import org.datagear.util.FileUtil;
import org.datagear.util.Global;
import org.datagear.util.version.Version;
import org.datagear.util.version.VersionContent;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * {@linkplain DbVersionManager}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class DbVersionManagerTest extends ServiceImplTestSupport
{
	private DbVersionManager dbVersionManager = new DbVersionManager();

	public DbVersionManagerTest()
	{
		super();

		FileUtil.deleteFile(new File("target/test/derby-for-db-upgrade"));

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dataSource.setUrl("jdbc:derby:target/test/derby-for-db-upgrade;create=true");

		this.dbVersionManager = new DbVersionManager(dataSource, new PathMatchingResourcePatternResolver());
	}

	@Test
	public void upgrade() throws Exception
	{
		this.dbVersionManager.upgrade();
	}

	@Test
	public void resolveUpgradeSqlVersionContentsTest() throws IOException
	{
		{
			List<VersionContent> versionContents = dbVersionManager.resolveUpgradeSqlVersionContents(
					Version.valueOf("2.13.0"), Version.valueOf("3.0.0"));

			Assert.assertEquals(1, versionContents.size());

			{
				VersionContent versionContent = versionContents.get(0);
				List<String> contents = versionContent.getContents();

				Assert.assertEquals(Version.valueOf("3.0.0"), versionContent.getVersion());

				Assert.assertTrue(contents.get(0).startsWith("CREATE TABLE DATAGEAR_DB_SHARE_SET"));
				Assert.assertTrue(contents.get(0).endsWith(")"));
			}
		}

		{
			List<VersionContent> versionContents = dbVersionManager.resolveUpgradeSqlVersionContents(
					Version.ZERO_VERSION, Version.valueOf("3.0.0"));

			Assert.assertEquals(2, versionContents.size());

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

		{
			List<VersionContent> versionContents = dbVersionManager.resolveUpgradeSqlVersionContents(
					Version.valueOf("3.0.0"), Version.valueOf("3.0.0"));

			Assert.assertEquals(0, versionContents.size());
		}

		{
			List<VersionContent> versionContents = dbVersionManager.resolveUpgradeSqlVersionContents(
					Version.ZERO_VERSION, Version.valueOf(Global.VERSION));

			Assert.assertTrue(versionContents.size() > 2);

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
}
