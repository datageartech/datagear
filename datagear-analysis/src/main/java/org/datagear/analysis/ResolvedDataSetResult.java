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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 数据集解析结果。
 * 
 * @author datagear@163.com
 *
 */
public class ResolvedDataSetResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private DataSetResult result;

	private List<DataSetField> fields = Collections.emptyList();

	public ResolvedDataSetResult()
	{
	}

	public ResolvedDataSetResult(DataSetResult result)
	{
		super();
		this.result = result;
	}

	public ResolvedDataSetResult(DataSetResult result, List<DataSetField> fields)
	{
		super();
		this.result = result;
		this.fields = fields;
	}

	public DataSetResult getResult()
	{
		return result;
	}

	public void setResult(DataSetResult result)
	{
		this.result = result;
	}

	public List<DataSetField> getFields()
	{
		return fields;
	}

	public void setFields(List<DataSetField> fields)
	{
		this.fields = fields;
	}
}
