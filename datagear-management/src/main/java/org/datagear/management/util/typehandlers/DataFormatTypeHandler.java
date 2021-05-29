/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.typehandlers;

import org.apache.ibatis.type.TypeHandler;
import org.datagear.analysis.support.DataFormat;

/**
 * {@linkplain DataFormat}的Mybatis {@linkplain TypeHandler}。 
 * 
 * @author datagear@163.com
 *
 */
public class DataFormatTypeHandler extends AbstractJsonTypeHandler<DataFormat>
{
	public DataFormatTypeHandler()
	{
		super();
	}

	@Override
	protected Class<DataFormat> getJsonObjectType()
	{
		return DataFormat.class;
	}
}
