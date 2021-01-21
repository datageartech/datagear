/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.ChartParam;
import org.datagear.analysis.ChartParam.DataType;

/**
 * {@linkplain ChartParam}值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class ChartParamValueConverter extends DataValueConverter
{
	public ChartParamValueConverter()
	{
		super();
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (DataType.STRING.equals(type))
			return convertToString(value, DataType.STRING);
		else if (DataType.NUMBER.equals(type))
			return convertToNumber(type, DataType.NUMBER);
		else if (DataType.BOOLEAN.equals(type))
			return convertToBoolean(value, DataType.BOOLEAN);
		else
			return convertExt(value, type);
	}
}
