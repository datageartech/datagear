/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.management.util;

import java.util.Collections;
import java.util.List;

import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.domain.SchemaProperty;
import org.datagear.management.domain.SchemaPropertyPattern;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.StringUtil;

/**
 * {@linkplain SchemaGuard}校验类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuardChecker
{
	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher(true);

	public SchemaGuardChecker()
	{
		super();
	}

	public AsteriskPatternMatcher getAsteriskPatternMatcher()
	{
		return asteriskPatternMatcher;
	}

	public void setAsteriskPatternMatcher(AsteriskPatternMatcher asteriskPatternMatcher)
	{
		this.asteriskPatternMatcher = asteriskPatternMatcher;
	}

	/**
	 * 是否准许。
	 * 
	 * @param schemaGuards
	 * @param guardEntity
	 * @return
	 */
	public boolean isPermitted(List<SchemaGuard> schemaGuards, GuardEntity guardEntity)
	{
		// 默认应为true，比如当没有定义任何SchemaGuard时
		boolean permitted = true;

		for (SchemaGuard schemaGuard : schemaGuards)
		{
			if (!schemaGuard.isEnabled())
				continue;

			boolean matches = isUrlMatched(schemaGuard, guardEntity) && isUserMatched(schemaGuard, guardEntity)
					&& isPropertiesMatched(schemaGuard, guardEntity);

			if (matches)
			{
				permitted = schemaGuard.isPermitted();
				break;
			}
		}

		return permitted;
	}

	protected boolean isUrlMatched(SchemaGuard schemaGuard, GuardEntity guardEntity)
	{
		String pattern = (StringUtil.isEmpty(schemaGuard.getPattern()) ? AsteriskPatternMatcher.ALL_PATTERN
				: schemaGuard.getPattern());
		String url = (guardEntity.getUrl() == null ? "" : guardEntity.getUrl());

		return this.asteriskPatternMatcher.matches(pattern, url);
	}

	protected boolean isUserMatched(SchemaGuard schemaGuard, GuardEntity guardEntity)
	{
		String pattern = (StringUtil.isEmpty(schemaGuard.getUserPattern()) ? AsteriskPatternMatcher.ALL_PATTERN
				: schemaGuard.getUserPattern());
		String user = (guardEntity.getUser() == null ? "" : guardEntity.getUser());

		return this.asteriskPatternMatcher.matches(pattern, user);
	}

	protected boolean isPropertiesMatched(SchemaGuard schemaGuard, GuardEntity guardEntity)
	{
		List<SchemaPropertyPattern> patterns = (schemaGuard.getPropertyPatterns() == null ? Collections.emptyList()
				: schemaGuard.getPropertyPatterns());
		List<SchemaProperty> properties = (guardEntity.getProperties() == null ? Collections.emptyList()
				: guardEntity.getProperties());

		if (patterns.isEmpty())
		{
			if(schemaGuard.isEmptyPropertyPatternsForAll())
				return true;
			else
				return properties.isEmpty();
		}

		if (properties.isEmpty())
			return false;

		if (SchemaGuard.PROPERTIES_MATCH_MODE_ANY.equalsIgnoreCase(schemaGuard.getPropertiesMatchMode()))
		{
			for (SchemaPropertyPattern pattern : patterns)
			{
				String namePattern = (StringUtil.isEmpty(pattern.getNamePattern()) ? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getNamePattern());
				String valuePattern = (StringUtil.isEmpty(pattern.getValuePattern())
						? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getValuePattern());

				boolean myMatches = false;

				for (SchemaProperty p : properties)
				{
					if (this.asteriskPatternMatcher.matches(namePattern, p.getName())
							&& this.asteriskPatternMatcher.matches(valuePattern, p.getValue()))
					{
						myMatches = true;
						break;
					}
				}

				if (myMatches)
					return true;
			}

			return false;
		}
		else if (SchemaGuard.PROPERTIES_MATCH_MODE_ALL.equalsIgnoreCase(schemaGuard.getPropertiesMatchMode()))
		{
			for (SchemaPropertyPattern pattern : patterns)
			{
				String namePattern = (StringUtil.isEmpty(pattern.getNamePattern()) ? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getNamePattern());
				String valuePattern = (StringUtil.isEmpty(pattern.getValuePattern())
						? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getValuePattern());

				boolean myMatches = false;

				for (SchemaProperty p : properties)
				{
					if (this.asteriskPatternMatcher.matches(namePattern, p.getName())
							&& this.asteriskPatternMatcher.matches(valuePattern, p.getValue()))
					{
						myMatches = true;
						break;
					}
				}

				if (!myMatches)
					return false;
			}

			return true;
		}
		else
		{
			return false;
		}
	}
}
