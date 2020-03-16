/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

/**
 * 抽象持久化操作类。
 * <p>
 * 此类是持久化操作类（insert、update、delete、select等）的上级类，封装公用方法。
 * </p>
 * <p>
 * 如果把所有持久化操作都封装到一个类中，会使这个类非常庞大，难于维护（之前的实现{@code DefaultPersistenceManager}
 * 即是如此），因此考虑按照操作类型拆分类。
 * </p>
 * 
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractModelPersistenceOperation extends AbstractModelDataAccessObject
{
	public AbstractModelPersistenceOperation()
	{
		super();
	}

}
