/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
