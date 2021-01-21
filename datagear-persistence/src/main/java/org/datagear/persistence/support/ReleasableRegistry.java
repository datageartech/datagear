/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.util.IOUtil;

/**
 * 可释放资源注册中心。
 * <p>
 * 注意：此类的{@linkplain #release()}方法仅支持{@linkplain AutoCloseable}对象，子类可以扩展更多支持。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ReleasableRegistry
{
	private List<Object> releasables;

	public ReleasableRegistry()
	{
		this(2);
	}

	public ReleasableRegistry(int initialCapacity)
	{
		super();
		this.releasables = new ArrayList<Object>(initialCapacity);
	}

	public List<Object> getReleasables()
	{
		return releasables;
	}

	public void setReleasables(List<Object> releasables)
	{
		this.releasables = releasables;
	}

	/**
	 * 是否是可释放对象。
	 * 
	 * @param obj
	 * @return
	 */
	public boolean isReleasable(Object obj)
	{
		return (obj instanceof AutoCloseable);
	}

	/**
	 * 注册一个可释放对象。
	 * 
	 * @param obj
	 * @return 返回{@code true} 注册成功；返回{@code false} 不是可释放对象，注册失败。
	 */
	public boolean register(Object obj)
	{
		if (isReleasable(obj))
		{
			this.releasables.add(obj);
			return true;
		}
		else
			return false;
	}

	/**
	 * 释放所有资源。
	 * <p>
	 * 此方法不抛出任何异常。
	 * </p>
	 */
	public void release()
	{
		IOUtil.closeIf(this.releasables);
	}

	/**
	 * 释放并清空所有资源。
	 */
	public void releaseClear()
	{
		release();
		this.releasables.clear();
	}
}
