/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.util.SqlParamValue;
import org.springframework.core.convert.ConversionService;

/**
 * 代理{@linkplain ConversionSqlParamValueMapper}实现。
 * <p>
 * 它将类型转换代理给{@linkplain ConversionService}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DelegationConversionSqlParamValueMapper extends ConversionSqlParamValueMapper
{
	private ConversionService conversionService;

	public DelegationConversionSqlParamValueMapper()
	{
		super();
	}

	public DelegationConversionSqlParamValueMapper(ConversionService conversionService)
	{
		super();
		this.conversionService = conversionService;
	}

	public DelegationConversionSqlParamValueMapper(ConversionService conversionService,
			ExpressionEvaluationContext expressionEvaluationContext)
	{
		super();
		this.conversionService = conversionService;
		super.setExpressionEvaluationContext(expressionEvaluationContext);
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	@Override
	protected SqlParamValue convertToSqlParamValueExt(Connection cn, Table table, Column column, Object value,
			Class<?> suggestType) throws Throwable, SqlParamValueMapperException
	{
		@SuppressWarnings("unchecked")
		Object paramValue = this.conversionService.convert(value, (Class<Object>) suggestType);
		return toSqlParamValue(column, paramValue);
	}
}
