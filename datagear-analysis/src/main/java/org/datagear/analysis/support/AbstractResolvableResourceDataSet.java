/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.datasetres.DataSetResource;
import org.datagear.analysis.support.datasetres.ResourceResult;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 抽象资源{@linkplain ResolvableDataSet}。
 * <p>
 * 从相同资源解析而得的结果通常是不变的，此类用于处理这种场景，通过缓存以提升性能。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractResolvableResourceDataSet<T extends DataSetResource> extends AbstractResolvableDataSet
{
	private static final long serialVersionUID = 1L;

	private transient Cache cache = null;

	/** 缓存数据的最大条目数 */
	private int dataCacheMaxLength = 500;

	public AbstractResolvableResourceDataSet()
	{
		super();
	}

	public AbstractResolvableResourceDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractResolvableResourceDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
	}

	public Cache getCache()
	{
		return cache;
	}

	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	public int getDataCacheMaxLength()
	{
		return dataCacheMaxLength;
	}

	public void setDataCacheMaxLength(int dataCacheMaxLength)
	{
		this.dataCacheMaxLength = dataCacheMaxLength;
	}

	/**
	 * 解析结果。
	 * <p>
	 * 如果{@linkplain #getResource(DataSetQuery)}返回有{@linkplain DataSetResource#hasResolvedTemplate()}，
	 * 此方法将返回{@linkplain TemplateResolvedDataSetResult}。
	 * </p>
	 */
	@Override
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveFields) throws DataSetException
	{
		T resource = null;

		try
		{
			resource = getResource(query);
			ResourceResult resourceData = getResourceResult(resource, resolveFields);

			ResolvedDataSetResult result = resolveResult(query, resourceData, resolveFields);

			if (resource.hasResolvedTemplate())
				result = new TemplateResolvedDataSetResult(result.getResult(), result.getFields(),
						resource.getResolvedTemplate());

			return result;
		}
		catch(DataSetException e)
		{
			throw e;
		}
		// 需特殊处理此异常，避免泄露文件路径信息
		catch (FileNotFoundException e)
		{
			throw new DataSetSourceFileNotFoundException("File not found",
					(resource == null ? null : resource.getResolvedTemplate()));
		}
		catch(Throwable t)
		{
			throw new DataSetSourceParseException(t, (resource == null ? null : resource.getResolvedTemplate()));
		}
	}

	/**
	 * 获取资源结果。
	 * 
	 * @param resource
	 * @param resolveFields
	 * @return
	 * @throws Throwable
	 */
	protected ResourceResult getResourceResult(T resource, boolean resolveFields) throws Throwable
	{
		if (!resource.isIdempotent() || this.cache == null)
			return resolveResourceResult(resource, resolveFields);

		ResourceResult rr = null;

		if (resolveFields)
		{
			rr = resolveResourceResult(resource, true);
			setCacheResourceResult(resource, rr);
		}
		else
		{
			ValueWrapper vw = getCacheResourceResult(resource);
			rr = (vw == null ? null : (ResourceResult) vw.get());

			if (rr == null)
			{
				rr = resolveResourceResult(resource, false);
				setCacheResourceResult(resource, rr);
			}
		}

		return rr;
	}

	protected ResourceResult toCacheResourceResult(ResourceResult result) throws Throwable
	{
		ResolvedDataSetResult dr = result.getResult();
		// 缓存中无需存储字段信息
		ResolvedDataSetResult cacheDr = new ResolvedDataSetResult(dr.getResult());
		ResourceResult cacheRr = new ResourceResult(cacheDr);

		return cacheRr;
	}

	/**
	 * 从缓存中获取。
	 * 
	 * @param resource
	 * @return 可能为{@code null}
	 * @throws Throwable
	 */
	protected ValueWrapper getCacheResourceResult(T resource) throws Throwable
	{
		if (this.cache == null)
			return null;

		return this.cache.get(resource);
	}

	/**
	 * 缓存。
	 * 
	 * @param resource
	 * @param result
	 * @return
	 * @throws Throwable
	 */
	protected boolean setCacheResourceResult(T resource, ResourceResult result) throws Throwable
	{
		if (this.cache == null)
			return false;

		if (result != null && result.dataSize() > this.dataCacheMaxLength)
			return false;

		ResourceResult cacheRr = toCacheResourceResult(result);
		this.cache.put(resource, cacheRr);

		return true;
	}

	/**
	 * 解析结果。
	 * 
	 * @param query
	 * @param result
	 * @param resolveFields
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(DataSetQuery query, ResourceResult result,
			boolean resolveFields) throws Throwable
	{
		ResolvedDataSetResult rdr = result.getResult();
		DataSetResult dr = rdr.getResult();

		return resolveResult(query, dr.getData(), (resolveFields ? rdr.getFields() : null));
	}

	protected ResourceResult toResourceResult(Object data, List<DataSetField> fields) throws Throwable
	{
		return toResourceResult(data, null, fields);
	}

	protected ResourceResult toResourceResult(Object data, Map<String, ?> additions, List<DataSetField> fields)
			throws Throwable
	{
		DataSetResult dr = new DataSetResult(data);
		dr.setAdditions(additions);
		ResolvedDataSetResult rdr = new ResolvedDataSetResult(dr, fields);
		return new ResourceResult(rdr);
	}

	/**
	 * 获取资源。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract T getResource(DataSetQuery query) throws Throwable;

	/**
	 * 解析资源结果。
	 * 
	 * @param resource
	 * @param resolveFields
	 *            是否同时解析并设置{@linkplain ResourceResult#getResult()}的{@linkplain ResolvedDataSetResult#getFields()}，
	 *            如果为{@code true}，返回的{@linkplain ResolvedDataSetResult#getFields()}不应为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected abstract ResourceResult resolveResourceResult(T resource, boolean resolveFields) throws Throwable;
}
