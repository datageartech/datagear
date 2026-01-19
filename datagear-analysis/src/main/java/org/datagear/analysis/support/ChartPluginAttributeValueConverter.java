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

import org.datagear.analysis.ChartPluginAttributeForm;
import org.datagear.analysis.form.PropertyType;

/**
 * {@linkplain ChartPluginAttributeForm}值转换器。
 * 
 * @author datagear@163.com
 * @deprecated 插件属性表单数据值没有后台相关逻辑和安全风险，所以不必在后台进行类型转换
 */
@Deprecated
public class ChartPluginAttributeValueConverter extends DataValueConverter
{
	public ChartPluginAttributeValueConverter()
	{
		super();
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (PropertyType.STRING.equals(type))
			return convertToString(value, PropertyType.STRING);
		else if (PropertyType.NUMBER.equals(type))
			return convertToNumber(value, PropertyType.NUMBER);
		else if (PropertyType.INTEGER.equals(type))
			return convertToInteger(value, PropertyType.INTEGER);
		else if (PropertyType.BOOLEAN.equals(type))
			return convertToBoolean(value, PropertyType.BOOLEAN);
		else
			return convertExt(value, type);
	}
}
