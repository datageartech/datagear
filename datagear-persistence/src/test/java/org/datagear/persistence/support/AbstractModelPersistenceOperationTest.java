/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain AbstractModelPersistenceOperation}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractModelPersistenceOperationTest
{
	@Test
	public void isSelectSqlTest()
	{
		AbstractModelPersistenceOperation persistenceOperation = new AbstractModelPersistenceOperation()
		{
		};

		Assert.assertTrue(persistenceOperation.isSelectSql("select from  "));
		Assert.assertTrue(persistenceOperation.isSelectSql("  select from  "));
		Assert.assertTrue(persistenceOperation.isSelectSql("  select s"));
		Assert.assertTrue(persistenceOperation.isSelectSql("  Select s"));
		Assert.assertTrue(persistenceOperation.isSelectSql("  SeleCt s"));
		Assert.assertTrue(persistenceOperation.isSelectSql("  SELECT s"));
		Assert.assertFalse(persistenceOperation.isSelectSql("  select "));
	}
}
