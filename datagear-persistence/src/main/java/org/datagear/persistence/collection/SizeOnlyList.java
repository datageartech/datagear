/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * 仅有大小信息的{@linkplain List}。
 * 
 * @author datagear@163.com
 *
 * @param <E>
 */
public class SizeOnlyList<E> extends SizeOnlyCollection<E> implements List<E>
{
	public SizeOnlyList()
	{
		super();
	}

	public SizeOnlyList(int size)
	{
		super(size);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		return false;
	}

	@Override
	public E get(int index)
	{
		return null;
	}

	@Override
	public E set(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o)
	{
		return -1;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return -1;
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return Collections.<E> emptyList().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return Collections.<E> emptyList().listIterator();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return Collections.emptyList();
	}
}
