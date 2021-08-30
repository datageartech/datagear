/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 数据源管控。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuard extends AbstractStringIdEntity
{
	private static final long serialVersionUID = 1L;

	/** 匹配模式 */
	private String pattern;

	/** 是否允许：true 允许；false 禁止 */
	private boolean permitted;

	/** 优先级 */
	private int priority;

	/** 是否启用 */
	private boolean enabled = true;

	/** 创建时间 */
	private Date createTime;

	public SchemaGuard()
	{
		super();
	}

	public SchemaGuard(String id)
	{
		super(id);
	}

	public SchemaGuard(String id, String pattern, boolean permitted, int priority)
	{
		super(id);
		this.pattern = pattern;
		this.permitted = permitted;
		this.priority = priority;
		this.enabled = true;
		this.createTime = new Date();
	}

	public String getPattern()
	{
		return pattern;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public boolean isPermitted()
	{
		return permitted;
	}

	public void setPermitted(boolean permitted)
	{
		this.permitted = permitted;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	/**
	 * 将{@linkplain SchemaGuard}列表按照优先级排序，{@linkplain SchemaGuard#getPriority()}越大越靠前、{@linkplain SchemaGuard#getCreateTime()}越新越靠前。
	 * 
	 * @param schemaGuards
	 */
	public void sortByPriority(List<? extends SchemaGuard> schemaGuards)
	{
		if (schemaGuards == null)
			return;

		Collections.sort(schemaGuards, SCHEMA_GUARD_PRIORITY_COMPARATOR);
	}

	private static final Comparator<SchemaGuard> SCHEMA_GUARD_PRIORITY_COMPARATOR = new Comparator<SchemaGuard>()
	{
		@Override
		public int compare(SchemaGuard o1, SchemaGuard o2)
		{
			// 优先级越高越靠前
			int p = Integer.valueOf(o2.priority).compareTo(o1.priority);

			if (p == 0)
			{
				// 越新创建越靠前

				Date o1d = o1.createTime;
				Date o2d = o2.createTime;

				if (o1d == null && o2d == null)
					return 0;
				else if (o1d == null)
					return 1;
				else if (o2d == null)
					return -1;
				else
					return o2d.compareTo(o1d);
			}
			else
				return p;
		}
	};
}
