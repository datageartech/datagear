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

package org.datagear.persistence.support.expression;

import java.util.HashMap;
import java.util.Map;

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
	private Map<String, Object> valueCache = new HashMap<String, Object>(3);

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
	 * 获取缓存关键字。
	 * 
	 * @param expression
	 * 
	 * @return
	 */
	public String getCachedKey(NameExpression expression)
	{
		// 为了使ExpressionResolver.DEFAULT_START_IDENTIFIER_DOLLAR、
		// ExpressionResolver.DEFAULT_START_IDENTIFIER_SHARP的表达式能使用同一个计算上下文，
		// 所以这里生成"${...}"、"#{...}"格式的关键字。

		String key = (expression.hasName() ? expression.getName() : expression.getContent());
		return expression.getStartIdentifier() + key + expression.getEndIdentifier();
	}

	/**
	 * 是否包含指定关键字的缓存值。
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsCachedValue(String key)
	{
		return this.valueCache.containsKey(key);
	}

	/**
	 * 获取指定关键字的缓存值。
	 * 
	 * @param key
	 * @return
	 */
	public Object getCachedValue(String key)
	{
		return this.valueCache.get(key);
	}

	/**
	 * 将指定关键字的值加入缓存。
	 * 
	 * @param key
	 * @param value
	 */
	public void putCachedValue(String key, Object value)
	{
		this.valueCache.put(key, value);
	}

	/**
	 * 将指定{@linkplain NameExpression}的值加入缓存。
	 * <p>
	 * 注意：只有{@linkplain NameExpression#hasName()}才可加入。
	 * </p>
	 * 
	 * @param expression
	 * @param value
	 */
	public boolean putCachedValue(NameExpression expression, Object value)
	{
		if (!expression.hasName())
			return false;

		String key = getCachedKey(expression);
		this.valueCache.put(key, value);

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
}
