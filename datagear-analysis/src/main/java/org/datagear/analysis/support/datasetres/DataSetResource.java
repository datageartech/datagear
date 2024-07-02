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

package org.datagear.analysis.support.datasetres;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import org.datagear.analysis.support.AbstractResolvableResourceDataSet;
import org.datagear.analysis.support.DataSetSourceFileNotFoundException;
import org.datagear.util.IOUtil;
import org.datagear.util.cache.CommonCacheKey;

/**
 * 数据集资源。
 * <p>
 * {@linkplain AbstractResolvableResourceDataSet}使用此类实现数据集结果数据缓存。
 * </p>
 * <p>
 * 如果子类的{@linkplain #isIdempotent()}为{@code true}，那么必须重写{@linkplain #hashCode()}、{@linkplain #equals(Object)}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataSetResource implements CommonCacheKey
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

	/**
	 * 获取输入流。
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws Throwable
	 */
	protected Reader getReader(File file, String encoding) throws Throwable
	{
		// 先校验以免泄露敏感信息
		if (!file.exists())
			throw new DataSetSourceFileNotFoundException(file.getName());

		return IOUtil.getReader(file, encoding);
	}

	/**
	 * 获取输入流。
	 * 
	 * @param file
	 * @return
	 * @throws Throwable
	 */
	protected InputStream getInputStream(File file) throws Throwable
	{
		// 先校验以免泄露敏感信息
		if (!file.exists())
			throw new DataSetSourceFileNotFoundException(file.getName());

		return IOUtil.getInputStream(file);
	}

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