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

import java.io.Reader;

/**
 * JSON数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public abstract class JsonDataSetResource extends DataSetResource
{
	private static final long serialVersionUID = 1L;
	
	private String dataJsonPath;

	public JsonDataSetResource()
	{
		super();
	}

	public JsonDataSetResource(String resolvedTemplate, String dataJsonPath)
	{
		super(resolvedTemplate);
		this.dataJsonPath = dataJsonPath;
	}

	public String getDataJsonPath()
	{
		return dataJsonPath;
	}

	/**
	 * 获取JSON输入流。
	 * <p>
	 * 输入流应该在此方法内创建，而不应该在实例内创建，因为采用缓存后不会每次都调用此方法。
	 * </p>
	 * 
	 * @return
	 * @throws Throwable
	 */
	public abstract Reader getReader() throws Throwable;

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataJsonPath == null) ? 0 : dataJsonPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonDataSetResource other = (JsonDataSetResource) obj;
		if (dataJsonPath == null)
		{
			if (other.dataJsonPath != null)
				return false;
		}
		else if (!dataJsonPath.equals(other.dataJsonPath))
			return false;
		return true;
	}
}