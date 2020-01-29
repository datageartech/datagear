/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务上下文对象。
 * <p>
 * 此类用于向服务类传递参数。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ServiceContext
{
	private static final ThreadLocal<ServiceContext> THREAD_LOCAL_SERVICE_CONTEXT = new ThreadLocal<ServiceContext>()
	{
		@Override
		protected ServiceContext initialValue()
		{
			return new ServiceContext();
		}
	};

	private Map<String, Object> values = new HashMap<String, Object>();

	private ServiceContext()
	{
		super();
	}

	/**
	 * 设置上下文名字值。
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, Object value)
	{
		this.values.put(name, value);
	}

	/**
	 * 获取上下文名字值。
	 * 
	 * @param <T>
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String name)
	{
		return (T) this.values.get(name);
	}

	/**
	 * 移除上下文名字值。
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T removeValue(String name)
	{
		return (T) this.values.remove(name);
	}

	/**
	 * 获取全部上下文名字值。
	 * 
	 * @return
	 */
	public Map<String, Object> getValues()
	{
		return this.values;
	}

	/**
	 * 获取当前{@linkplain ServiceContext}。
	 * 
	 * @return
	 */
	public static ServiceContext get()
	{
		return THREAD_LOCAL_SERVICE_CONTEXT.get();
	}

	/**
	 * 删除当前{@linkplain ServiceContext}。
	 */
	public static void remove()
	{
		THREAD_LOCAL_SERVICE_CONTEXT.remove();
	}
}
