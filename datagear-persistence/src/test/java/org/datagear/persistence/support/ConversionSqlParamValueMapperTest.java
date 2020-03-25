/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import static org.junit.Assert.assertEquals;

import org.datagear.persistence.PersistenceTestSupport;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.util.SqlParamValue;
import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * {@linkplain ConversionSqlParamValueMapper}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ConversionSqlParamValueMapperTest extends PersistenceTestSupport
{
	public ConversionSqlParamValueMapperTest()
	{
		super();
	}

	@Test
	public void mapTest()
	{
		ExpressionEvaluationContext context = new ExpressionEvaluationContext();
		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		mapper.setConversionService(new DefaultConversionService());
		mapper.setExpressionEvaluationContext(context);

		{
			SqlParamValue paramValueId = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_ID, "#{1:index+1}");
			SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-#{1}");
			SqlParamValue paramValueDesc = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_DESC,
					"DESC-#{1}-#{index + 2 + '-DESC'}");

			assertEquals(1, ((Number) paramValueId.getValue()).intValue());
			assertEquals("NAME-1", paramValueName.getValue());
			assertEquals("DESC-1-2-DESC", paramValueDesc.getValue());
		}
	}
}
