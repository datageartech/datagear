/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 标识符{@linkplain PropertyNameResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class IdentifierPropertyNameResolver extends NameResolverSupport implements PropertyNameResolver
{
	public IdentifierPropertyNameResolver()
	{
		super();
	}

	@Override
	public String resolve(PropertyNameContext context, String... candidates)
	{
		int loop = 0;

		while (true)
		{
			for (String candidate : candidates)
			{
				String name = resolveForIndentifier(candidate);

				if (loop > 0)
					name += "_" + loop;

				if (!context.isDuplicate(name))
					return name;
			}

			loop++;
		}
	}
}
