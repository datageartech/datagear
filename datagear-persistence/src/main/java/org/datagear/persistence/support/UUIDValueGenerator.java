/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

import java.util.UUID;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.persistence.features.ValueGenerator;

/**
 * UUID属性值生成器。
 * <p>
 * 它使用{@linkplain UUID#toString() UUID.randomUUID().toString()}生成。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UUIDValueGenerator implements ValueGenerator
{
	public UUIDValueGenerator()
	{
		super();
	}

	@Override
	public boolean isSql(Model model, Property property, Object obj)
	{
		return false;
	}

	@Override
	public Object generate(Model model, Property property, Object obj)
	{
		return UUID.randomUUID().toString();
	}
}