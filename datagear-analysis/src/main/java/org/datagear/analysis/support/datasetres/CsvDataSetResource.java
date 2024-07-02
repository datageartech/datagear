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
 * CSV数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public abstract class CsvDataSetResource extends DataSetResource
{
	private static final long serialVersionUID = 1L;
	
	private int nameRow;

	public CsvDataSetResource()
	{
		super();
	}

	public CsvDataSetResource(String resolvedTemplate, int nameRow)
	{
		super(resolvedTemplate);
		this.nameRow = nameRow;
	}

	public int getNameRow()
	{
		return nameRow;
	}

	public void setNameRow(int nameRow)
	{
		this.nameRow = nameRow;
	}

	/**
	 * 是否名称行。
	 * 
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isNameRow(int rowIndex)
	{
		return ((rowIndex + 1) == this.nameRow);
	}

	/**
	 * 是否在名称行之后。
	 * <p>
	 * 如果没有名称行，应返回{@code true}。
	 * </p>
	 * 
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isAfterNameRow(int rowIndex)
	{
		return ((rowIndex + 1) > this.nameRow);
	}

	/**
	 * 获取CSV输入流。
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
		result = prime * result + nameRow;
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
		CsvDataSetResource other = (CsvDataSetResource) obj;
		if (nameRow != other.nameRow)
			return false;
		return true;
	}
}