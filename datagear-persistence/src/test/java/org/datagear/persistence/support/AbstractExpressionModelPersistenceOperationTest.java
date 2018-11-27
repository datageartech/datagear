/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
public class AbstractExpressionModelPersistenceOperationTest
{
	@Test
	public void isSelectSqlTest()
	{
		AbstractExpressionModelPersistenceOperation persistenceOperation = new AbstractExpressionModelPersistenceOperation()
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
