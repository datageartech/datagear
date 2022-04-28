/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Serializable;

/**
 * 模板已解析的资源。
 * <p>
 * 如果子类的{@linkplain #isIdempotent()}为{@code true}，那么必须重写{@linkplain #hashCode()}、{@linkplain #equals(Object)}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class TemplateResolvedResource<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String resolvedTemplate;

	public TemplateResolvedResource(String resolvedTemplate)
	{
		super();
		this.resolvedTemplate = resolvedTemplate;
	}

	public boolean hasResolvedTemplate()
	{
		return (this.resolvedTemplate != null && !this.resolvedTemplate.isEmpty());
	}

	/**
	 * 获取已解析的模板文本。
	 * 
	 * @return 模板文本，{@code null}表示没有
	 */
	public String getResolvedTemplate()
	{
		return resolvedTemplate;
	}

	/**
	 * 是否是幂等的，即：相等{@linkplain TemplateResolvedResource}的{@linkplain #getResource()}表示的数据也是相等的。
	 * 
	 * @return
	 */
	public abstract boolean isIdempotent();

	/**
	 * 获取资源。
	 * <p>
	 * 如果{@linkplain #isIdempotent()}为{@code true}，那么资源应该在此方法内创建，而不应该在此实例内创建，因为采用缓存后不会每次都得调用此方法。
	 * </p>
	 * 
	 * @return
	 * @throws Throwable
	 */
	public abstract T getResource() throws Throwable;

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resolvedTemplate == null) ? 0 : resolvedTemplate.hashCode());
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
		TemplateResolvedResource<?> other = (TemplateResolvedResource<?>) obj;
		if (resolvedTemplate == null)
		{
			if (other.resolvedTemplate != null)
				return false;
		}
		else if (!resolvedTemplate.equals(other.resolvedTemplate))
			return false;
		return true;
	}

	/**
	 * 资源数据。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	public static class ResoureData<T> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final T data;

		public ResoureData(T data)
		{
			super();
			this.data = data;
		}

		/**
		 * 获取数据列表。
		 * <p>
		 * 返回值及其内容不应被修改，因为可能会缓存。
		 * </p>
		 * 
		 * @return
		 */
		public T getData()
		{
			return data;
		}
	}
}
