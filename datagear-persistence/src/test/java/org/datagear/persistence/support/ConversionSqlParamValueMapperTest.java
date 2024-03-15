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

package org.datagear.persistence.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.Statement;

import org.datagear.persistence.LiteralSqlParamValue;
import org.datagear.persistence.PersistenceTestSupport;
import org.datagear.persistence.SqlParamValueMapperException;
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
	public void mapTest() throws Throwable
	{
		{
			ConversionSqlParamValueMapper mapper = createMapper();

			SqlParamValue paramValueId = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_ID, "#{1:index+1}");
			SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-#{1}");
			SqlParamValue paramValueDesc = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_DESC,
					"DESC-#{1}-#{index + 2 + '-DESC'}");

			assertEquals(1, ((Number) paramValueId.getValue()).intValue());
			assertEquals("NAME-1", paramValueName.getValue());
			assertEquals("DESC-1-2-DESC", paramValueDesc.getValue());
		}

		{
			ConversionSqlParamValueMapper mapper = createMapper();

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-#{index}");
				assertEquals("NAME-0", paramValueName.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-#{1:index}");
				assertEquals("NAME-0", paramValueName.getValue());

				SqlParamValue paramValueDesc = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_DESC, "NAME-#{1}");
				assertEquals("NAME-0", paramValueDesc.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"NAME-#{invalid-expression-for-text}");
				assertEquals("NAME-#{invalid-expression-for-text}", paramValueName.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-\\#{index}");
				assertEquals("NAME-#{index}", paramValueName.getValue());
			}

			{
				mapper.setEnableVariableExpression(false);
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME, "NAME-#{index}");
				assertEquals("NAME-#{index}", paramValueName.getValue());
				mapper.setEnableVariableExpression(true);
			}
		}

		{
			ConversionSqlParamValueMapper mapper = createMapper();

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"${select 1 from t}");
				assertTrue(paramValueName instanceof LiteralSqlParamValue);
				assertEquals("select 1 from t", paramValueName.getValue());
			}

			{
				String sqlContent = "SELECT COUNT(*) FROM T_ACCOUNT";

				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"NAME-${" + sqlContent + "}-NAME");

				int sqlResult = -1;
				try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sqlContent))
				{
					rs.next();
					sqlResult = rs.getInt(1);
				}

				assertEquals("NAME-" + sqlResult + "-NAME", paramValueName.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"${1:select 1 from t}");
				SqlParamValue paramValueDesc = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_DESC, "${1}");

				assertTrue(paramValueName instanceof LiteralSqlParamValue);
				assertEquals("select 1 from t", paramValueName.getValue());

				assertTrue(paramValueDesc instanceof LiteralSqlParamValue);
				assertEquals("select 1 from t", paramValueDesc.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"NAME-${not-select-sql}");
				assertEquals("NAME-${not-select-sql}", paramValueName.getValue());
			}

			{
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"NAME-\\${select 1 from t}");
				assertEquals("NAME-${select 1 from t}", paramValueName.getValue());
			}

			{
				mapper.setEnableSqlExpression(false);
				SqlParamValue paramValueName = mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_NAME,
						"NAME-${select 1 from t}");
				assertEquals("NAME-${select 1 from t}", paramValueName.getValue());
				mapper.setEnableSqlExpression(true);
			}
		}
	}

	@Test
	public void mapTest_error_variable_expression_for_non_text()
	{
		assertThrows(SqlParamValueVariableExpressionException.class, () ->
		{
			ExpressionEvaluationContext context = new ExpressionEvaluationContext();
			ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
			mapper.setConversionService(new DefaultConversionService());
			mapper.setExpressionEvaluationContext(context);

			mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_ID, "#{invalid-expression-for-non-text}");
		});
	}

	@Test
	public void mapTest_error_sql_expression_for_non_text()
	{
		assertThrows(SqlParamValueMapperException.class, () ->
		{
			ExpressionEvaluationContext context = new ExpressionEvaluationContext();
			ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
			mapper.setConversionService(new DefaultConversionService());
			mapper.setExpressionEvaluationContext(context);

			mapper.map(connection, MOCK_TABLE, MOCK_COLUMN_ID, "100${invalid-expression-for-non-text}");
		});
	}

	protected ConversionSqlParamValueMapper createMapper()
	{
		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		mapper.setConversionService(new DefaultConversionService());
		return mapper;
	}
}
