/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractResolvableResourceDataSet.DataSetResource;

/**
 * 抽象资源{@linkplain ResolvableDataSet}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractResolvableResourceDataSet<T extends DataSetResource> extends AbstractResolvableDataSet
{
	private static final long serialVersionUID = 1L;

	public AbstractResolvableResourceDataSet()
	{
		super();
	}

	public AbstractResolvableResourceDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractResolvableResourceDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	/**
	 * 解析结果。
	 * <p>
	 * 如果{@linkplain #getResource(DataSetQuery, List, boolean)}返回有{@linkplain DataSetResource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		T resource = null;

		try
		{
			resource = getResource(query, properties, resolveProperties);
			ResourceData resourceData = resolveResourceData(resource);

			ResolvedDataSetResult result = resolveResult(query, resourceData, properties, resolveProperties);

			if (resource.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(),
						resource.getResolvedTemplate());

			return result;
		}
		catch(DataSetException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			throw new DataSetSourceParseException(t, (resource == null ? null : resource.getResolvedTemplate()));
		}
	}

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param resourceData
	 * @param properties        允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, ResourceData resourceData,
			List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		List<DataSetProperty> resProperties = resourceData.getProperties();
		Object resData = resourceData.getData();

		return resolveResult(query, resData, resProperties, properties, resolveProperties);
	}

	/**
	 * 获取资源。
	 * 
	 * @param query
	 * @param properties
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected abstract T getResource(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws Throwable;

	/**
	 * 解析资源数据。
	 * 
	 * @param resource
	 * @return
	 * @throws Throwable
	 */
	protected abstract ResourceData resolveResourceData(T resource) throws Throwable;

	/**
	 * 数据集资源。
	 * <p>
	 * 如果子类的{@linkplain #isIdempotent()}为{@code true}，那么必须重写{@linkplain #hashCode()}、{@linkplain #equals(Object)}方法。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static abstract class DataSetResource implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String resolvedTemplate = null;

		public DataSetResource()
		{
			super();
		}

		public DataSetResource(String resolvedTemplate)
		{
			super();
			this.resolvedTemplate = resolvedTemplate;
		}

		/**
		 * 是否有已解析的模板文本。
		 * 
		 * @return
		 */
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
		 * 是否是幂等的，即：相等{@linkplain DataSetResource}的{@linkplain #getResource()}表示的数据也是相等的。
		 * 
		 * @return
		 */
		public abstract boolean isIdempotent();

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
			DataSetResource other = (DataSetResource) obj;
			if (resolvedTemplate == null)
			{
				if (other.resolvedTemplate != null)
					return false;
			}
			else if (!resolvedTemplate.equals(other.resolvedTemplate))
				return false;
			return true;
		}
	}

	/**
	 * 数据集资源数据。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	public static class ResourceData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Object data = null;

		private List<DataSetProperty> properties = Collections.emptyList();

		public ResourceData()
		{
			super();
		}

		public ResourceData(Object data, List<DataSetProperty> properties)
		{
			super();
			this.data = data;
			this.properties = properties;
		}

		/**
		 * 获取数据。
		 * <p>
		 * 返回值及其内容不应被修改，因为可能会缓存。
		 * </p>
		 * 
		 * @return 为{@code null}表示无数据
		 */
		public Object getData()
		{
			return data;
		}

		public List<DataSetProperty> getProperties()
		{
			return properties;
		}

		public void setProperties(List<DataSetProperty> properties)
		{
			this.properties = properties;
		}

		public void setData(Object data)
		{
			this.data = data;
		}
	}
}
