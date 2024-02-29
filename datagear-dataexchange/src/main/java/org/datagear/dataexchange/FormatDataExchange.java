/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 格式化数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class FormatDataExchange extends DataExchange
{
	/** 数据格式 */
	private DataFormat dataFormat;

	public FormatDataExchange()
	{
		super();
	}

	public FormatDataExchange(ConnectionFactory connectionFactory, DataFormat dataFormat)
	{
		super(connectionFactory);
		this.dataFormat = dataFormat;
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;
	}
}
