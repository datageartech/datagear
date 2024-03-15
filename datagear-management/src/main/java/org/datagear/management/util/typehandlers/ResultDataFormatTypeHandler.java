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

package org.datagear.management.util.typehandlers;

import org.apache.ibatis.type.TypeHandler;
import org.datagear.analysis.ResultDataFormat;

/**
 * {@linkplain ResultDataFormat}的Mybatis {@linkplain TypeHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class ResultDataFormatTypeHandler extends AbstractJsonTypeHandler<ResultDataFormat>
{
	public ResultDataFormatTypeHandler()
	{
		super();
	}

	@Override
	protected Class<ResultDataFormat> getJsonObjectType()
	{
		return ResultDataFormat.class;
	}
}
