/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.springframework.core.convert.ConversionService;

/**
 * 代理{@linkplain ConversionPstParamMapper}实现。
 * <p>
 * 它将类型转换代理给{@linkplain ConversionService}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DelegationConversionPstParamMapper extends ConversionPstParamMapper
{
	private ConversionService conversionService;

	public DelegationConversionPstParamMapper()
	{
		super();
	}

	public DelegationConversionPstParamMapper(ConversionService conversionService)
	{
		super();
		this.conversionService = conversionService;
	}

	public DelegationConversionPstParamMapper(ConversionService conversionService,
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

	@SuppressWarnings("unchecked")
	@Override
	protected Object convertToPstParamExt(Connection cn, Object value, int sqlType, Class<?> suggestType)
			throws Throwable
	{
		return this.conversionService.convert(value, (Class<Object>) suggestType);
	}
}
