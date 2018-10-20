/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.collection;

import java.util.Queue;

/**
 * 仅有大小信息的{@linkplain Queue}。
 * 
 * @author datagear@163.com
 *
 * @param <E>
 */
public class SizeOnlyQueue<E> extends SizeOnlyCollection<E> implements Queue<E>
{
	public SizeOnlyQueue()
	{
		super();
	}

	public SizeOnlyQueue(int size)
	{
		super(size);
	}

	@Override
	public boolean offer(E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E poll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E element()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E peek()
	{
		throw new UnsupportedOperationException();
	}
}
