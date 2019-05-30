/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dataexchange.DataImport;

/**
 * 抽象{@linkplain DevotedDataImporter}。
 * <p>
 * 它默认实现了{@linkplain #supports(DataImport)}，并且始终返回{@code true}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataImporter<T extends DataImport> implements DevotedDataImporter<T>
{
	public AbstractDevotedDataImporter()
	{
		super();
	}

	@Override
	public boolean supports(T impt)
	{
		return true;
	}
}
