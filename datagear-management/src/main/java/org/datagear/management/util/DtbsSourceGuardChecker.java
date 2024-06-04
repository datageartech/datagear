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

package org.datagear.management.util;

import java.util.Collections;
import java.util.List;

import org.datagear.management.domain.DtbsSourceGuard;
import org.datagear.management.domain.DtbsSourceProperty;
import org.datagear.management.domain.DtbsSourcePropertyPattern;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.StringUtil;

/**
 * {@linkplain DtbsSourceGuard}校验类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceGuardChecker
{
	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher(true);

	public DtbsSourceGuardChecker()
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
	 * @param dtbsSourceGuards
	 * @param guardEntity
	 * @return
	 */
	public boolean isPermitted(List<DtbsSourceGuard> dtbsSourceGuards, GuardEntity guardEntity)
	{
		// 默认应为true，比如当没有定义任何DtbsSourceGuard时
		boolean permitted = true;

		for (DtbsSourceGuard dtbsSourceGuard : dtbsSourceGuards)
		{
			if (!dtbsSourceGuard.isEnabled())
				continue;

			boolean matches = isUrlMatched(dtbsSourceGuard, guardEntity) && isUserMatched(dtbsSourceGuard, guardEntity)
					&& isPropertiesMatched(dtbsSourceGuard, guardEntity);

			if (matches)
			{
				permitted = dtbsSourceGuard.isPermitted();
				break;
			}
		}

		return permitted;
	}

	protected boolean isUrlMatched(DtbsSourceGuard dtbsSourceGuard, GuardEntity guardEntity)
	{
		String pattern = (StringUtil.isEmpty(dtbsSourceGuard.getPattern()) ? AsteriskPatternMatcher.ALL_PATTERN
				: dtbsSourceGuard.getPattern());
		String url = (guardEntity.getUrl() == null ? "" : guardEntity.getUrl());

		return this.asteriskPatternMatcher.matches(pattern, url);
	}

	protected boolean isUserMatched(DtbsSourceGuard dtbsSourceGuard, GuardEntity guardEntity)
	{
		String pattern = (StringUtil.isEmpty(dtbsSourceGuard.getUserPattern()) ? AsteriskPatternMatcher.ALL_PATTERN
				: dtbsSourceGuard.getUserPattern());
		String user = (guardEntity.getUser() == null ? "" : guardEntity.getUser());

		return this.asteriskPatternMatcher.matches(pattern, user);
	}

	protected boolean isPropertiesMatched(DtbsSourceGuard dtbsSourceGuard, GuardEntity guardEntity)
	{
		List<DtbsSourcePropertyPattern> patterns = (dtbsSourceGuard.getPropertyPatterns() == null
				? Collections.emptyList()
				: dtbsSourceGuard.getPropertyPatterns());
		List<DtbsSourceProperty> properties = (guardEntity.getProperties() == null ? Collections.emptyList()
				: guardEntity.getProperties());

		if (patterns.isEmpty())
		{
			if (dtbsSourceGuard.isEmptyPropertyPatternsForAll())
				return true;
			else
				return properties.isEmpty();
		}

		if (properties.isEmpty())
			return false;

		String pmm = dtbsSourceGuard.getPropertiesMatchMode();
		if (StringUtil.isEmpty(pmm))
			pmm = DtbsSourceGuard.PROPERTIES_MATCH_MODE_ANY;

		if (DtbsSourceGuard.PROPERTIES_MATCH_MODE_ANY.equalsIgnoreCase(pmm))
		{
			for (DtbsSourcePropertyPattern pattern : patterns)
			{
				String namePattern = (StringUtil.isEmpty(pattern.getNamePattern()) ? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getNamePattern());
				String valuePattern = (StringUtil.isEmpty(pattern.getValuePattern())
						? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getValuePattern());

				boolean myMatches = false;

				for (DtbsSourceProperty p : properties)
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
		else if (DtbsSourceGuard.PROPERTIES_MATCH_MODE_ALL.equalsIgnoreCase(pmm))
		{
			for (DtbsSourcePropertyPattern pattern : patterns)
			{
				String namePattern = (StringUtil.isEmpty(pattern.getNamePattern()) ? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getNamePattern());
				String valuePattern = (StringUtil.isEmpty(pattern.getValuePattern())
						? AsteriskPatternMatcher.ALL_PATTERN
						: pattern.getValuePattern());

				boolean myMatches = false;

				for (DtbsSourceProperty p : properties)
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
