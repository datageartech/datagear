/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.collection;

import java.util.Set;

/**
 * 仅有大小信息的{@linkplain Set}。
 * 
 * @author datagear@163.com
 *
 * @param <E>
 */
public class SizeOnlySet<E> extends SizeOnlyCollection<E> implements Set<E>
{
	public SizeOnlySet()
	{
		super();
	}

	public SizeOnlySet(int size)
	{
		super(size);
	}
}
