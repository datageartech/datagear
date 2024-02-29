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

package org.datagear.analysis.support;

import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginAttribute.DataType;

/**
 * {@linkplain ChartPluginAttribute}值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginAttributeValueConverter extends DataValueConverter
{
	public ChartPluginAttributeValueConverter()
	{
		super();
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (DataType.STRING.equals(type))
			return convertToString(value, DataType.STRING);
		else if (DataType.NUMBER.equals(type))
			return convertToNumber(value, DataType.NUMBER);
		else if (DataType.BOOLEAN.equals(type))
			return convertToBoolean(value, DataType.BOOLEAN);
		else
			return convertExt(value, type);
	}
}
