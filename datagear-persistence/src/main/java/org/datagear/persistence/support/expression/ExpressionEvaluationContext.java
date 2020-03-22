/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.persistence.support.expression;

import java.util.HashMap;
import java.util.Map;

import org.datagear.util.expression.ExpressionResolver;

/**
 * {@linkplain NameExpression}计算上下文。
 * <p>
 * 为了方便支持批量添加操作，此类的实例默认会添加{@linkplain #VARIABLE_INDEX}变量并且初值为{@code 0}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ExpressionEvaluationContext
{
	/** 变量名：index */
	public static final String VARIABLE_INDEX = "index";

	/** 值缓存 */
	private Map<String, Object> valueCache = new HashMap<String, Object>();

	/** 变量表达式求值Bean */
	private VariableExpressionBean variableExpressionBean = new VariableExpressionBean();

	public ExpressionEvaluationContext()
	{
		super();
		this.variableExpressionBean.setIndex(0);
	}

	public Map<String, Object> getValueCache()
	{
		return valueCache;
	}

	public void setValueCache(Map<String, Object> valueCache)
	{
		this.valueCache = valueCache;
	}

	public VariableExpressionBean getVariableExpressionBean()
	{
		return variableExpressionBean;
	}

	public void setVariableExpressionBean(VariableExpressionBean variableExpressionBean)
	{
		this.variableExpressionBean = variableExpressionBean;
	}

	/**
	 * 是否包含指定{@linkplain NameExpression}的缓存值。
	 * 
	 * @param expression
	 * @return
	 */
	public boolean containsCachedValue(NameExpression expression)
	{
		if (!expression.hasName())
			return false;

		String cacheKey = getCachedValueKey(expression);

		return this.valueCache.containsKey(cacheKey);
	}

	/**
	 * 获取指定{@linkplain NameExpression}的缓存值。
	 * 
	 * @param expression
	 * @return
	 */
	public Object getCachedValue(NameExpression expression)
	{
		if (!expression.hasName())
			throw new IllegalArgumentException("The [expression] must has name");

		String cacheKey = getCachedValueKey(expression);
		return this.valueCache.get(cacheKey);
	}

	/**
	 * 将指定{@linkplain NameExpression}的值加入缓存。
	 * 
	 * @param expression
	 * @param value
	 */
	public boolean putCachedValue(NameExpression expression, Object value)
	{
		if (!expression.hasName())
			return false;

		String cacheKey = getCachedValueKey(expression);
		this.valueCache.put(cacheKey, value);

		return true;
	}

	/**
	 * 清除所有缓存值。
	 */
	public void clearCachedValue()
	{
		this.valueCache.clear();
	}

	/**
	 * 获取{@linkplain #getVariableExpressionBean()}的{@linkplain VariableExpressionBean#getIndex()}的值。
	 * 
	 * @return
	 */
	public int getVariableIndex()
	{
		return this.variableExpressionBean.getIndex();
	}

	/**
	 * 设置{@linkplain #getVariableExpressionBean()}的{@linkplain VariableExpressionBean#getIndex()}的值。
	 * 
	 * @param value
	 */
	public void setVariableIndex(int value)
	{
		this.variableExpressionBean.setIndex(value);
	}

	/**
	 * 将{@linkplain #getVariableExpressionBean()}的{@linkplain VariableExpressionBean#getIndex()}的值加{@code 1}并返回。
	 * 
	 * @return
	 */
	public int incrementVariableIndex()
	{
		int v = getVariableIndex() + 1;
		setVariableIndex(v);

		return v;
	}

	/**
	 * 获取缓存值关键字。
	 * <p>
	 * 为了使{@linkplain ExpressionResolver#DEFAULT_START_IDENTIFIER_DOLLAR}、
	 * {@linkplain ExpressionResolver#DEFAULT_START_IDENTIFIER_SHARP}的表达式能使用同一个{@linkplain ExpressionEvaluationContext}，
	 * 此方法会生成<code>"${...}"</code>、<code>"#{...}"</code>格式的关键字。
	 * </p>
	 * 
	 * @param expression
	 * 
	 * @return
	 */
	protected String getCachedValueKey(NameExpression expression)
	{
		return expression.getStartIdentifier() + expression.getName() + expression.getEndIdentifier();
	}
}
