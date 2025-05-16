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

package org.datagear.util.spel;

import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * 自定义的{@linkplain Map}访问器。
 * <p>
 * 默认{@linkplain org.springframework.context.expression.MapAccessor}的{@code canRead(...)}方法实现只在包含给定关键字时才返回{@code true}，
 * 这使得Spel对于{@code "['size']"}语法，是计算关键字值，对于{@code "size"}则是执行{@code Map}的{@linkplain Map#size()}。
 * 这对于{@linkplain BaseSpelExpressionParser#readonlyMapSimplifyContext(ConversionService)}要支持的{@linkplain Map}简化语法会产生歧义，
 * 因此，这里自定义了此类，使得{@code "['size']"}、{@code "size"}语法都是计算{@code "size"}关键字的值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MapAccessor implements PropertyAccessor
{
	public MapAccessor()
	{
		super();
	}

	@Override
	public Class<?>[] getSpecificTargetClasses()
	{
		return new Class<?>[] { Map.class };
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException
	{
		return (target instanceof Map);
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException
	{
		Object value = ((Map<?, ?>) target).get(name);
		return new TypedValue(value);
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException
	{
		return true;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException
	{
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) target;
		map.put(name, newValue);
	}
}