/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 抽象代理列表。
 * 
 * @author datagear@163.com
 *
 * @param <E>
 */
public class AbstractDelegatedList<E> implements List<E>
{
	private List<E> delegatedList;

	public AbstractDelegatedList()
	{
		super();
		this.delegatedList = new ArrayList<E>();
	}

	public AbstractDelegatedList(List<E> delegatedList)
	{
		super();
		this.delegatedList = delegatedList;
	}

	public List<E> getDelegatedList()
	{
		return delegatedList;
	}

	public void setDelegatedList(List<E> delegatedList)
	{
		this.delegatedList = delegatedList;
	}

	@Override
	public int size()
	{
		return this.delegatedList.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.delegatedList.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return this.delegatedList.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.delegatedList.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.delegatedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.delegatedList.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return this.delegatedList.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return this.delegatedList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.delegatedList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return this.delegatedList.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		return this.delegatedList.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return this.delegatedList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.delegatedList.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.delegatedList.clear();
	}

	@Override
	public E get(int index)
	{
		return this.delegatedList.get(index);
	}

	@Override
	public E set(int index, E element)
	{
		return this.delegatedList.set(index, element);
	}

	@Override
	public void add(int index, E element)
	{
		this.delegatedList.add(index, element);
	}

	@Override
	public E remove(int index)
	{
		return this.delegatedList.remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return this.delegatedList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return this.delegatedList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return this.delegatedList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return this.delegatedList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return this.delegatedList.subList(fromIndex, toIndex);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegatedList == null) ? 0 : delegatedList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDelegatedList<?> other = (AbstractDelegatedList<?>) obj;
		if (delegatedList == null)
		{
			if (other.delegatedList != null)
				return false;
		}
		else if (!delegatedList.equals(other.delegatedList))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.delegatedList.toString();
	}
}
