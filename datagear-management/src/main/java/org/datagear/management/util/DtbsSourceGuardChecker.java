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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.management.domain.DtbsSourceGuard;
import org.datagear.management.domain.DtbsSourceProperty;
import org.datagear.management.domain.DtbsSourcePropertyPattern;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.StringUtil;
import org.datagear.util.spel.BaseSpelExpressionParser;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * {@linkplain DtbsSourceGuard}校验类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceGuardChecker
{
	/**
	 * 匹配表达式的变量：URL。
	 * <p>
	 * 此变量表示{@linkplain DtbsSourceGuard#getPattern()}的匹配结果。
	 * </p>
	 * <p>
	 * 注意：不要修改此值，因为可能已在系统录入使用。
	 * </p>
	 */
	public static final String EVAL_EXP_VAR_URL = "url";

	/**
	 * 匹配表达式的变量：用户名。
	 * <p>
	 * 此变量表示{@linkplain DtbsSourceGuard#getUserPattern()}的匹配结果。
	 * </p>
	 * <p>
	 * 注意：不要修改此值，因为可能已在系统录入使用。
	 * </p>
	 */
	public static final String EVAL_EXP_VAR_USER = "user";

	/**
	 * 匹配表达式的变量：连接属性。
	 * <p>
	 * 此变量表示{@linkplain DtbsSourceGuard#getPropertyPatterns()}的匹配结果。
	 * </p>
	 * <p>
	 * 注意：不要修改此值，因为可能已在系统录入使用。
	 * </p>
	 */
	public static final String EVAL_EXP_VAR_PROPERTIES = "props";

	/**
	 * 默认最终匹配结果表达式。
	 * <p>
	 * 注意：不要修改此表达式逻辑规则，原因：
	 * </p>
	 * <p>
	 * 1. 系统{@code 6.0.0}及之前版本没有此功能，这个规则是旧版的兼容规则；
	 * </p>
	 * <p>
	 * 2. 这个规则更易于理解，符合常规认知。
	 * </p>
	 */
	public static final String DEFAULT_EVAL_EXP = EVAL_EXP_VAR_URL + " && " + EVAL_EXP_VAR_USER + " && "
			+ EVAL_EXP_VAR_PROPERTIES;
	
	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher(true);

	private BaseSpelExpressionParser spelExpressionParser = BaseSpelExpressionParser.DEFAULT;

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

	public BaseSpelExpressionParser getSpelExpressionParser()
	{
		return spelExpressionParser;
	}

	public void setSpelExpressionParser(BaseSpelExpressionParser spelExpressionParser)
	{
		this.spelExpressionParser = spelExpressionParser;
	}

	/**
	 * 是否准许。
	 * 
	 * @param dtbsSourceGuards
	 * @param guardEntity
	 * @return
	 * @throws DtbsSourceGuardEvalExpException
	 */
	public boolean isPermitted(List<DtbsSourceGuard> dtbsSourceGuards, GuardEntity guardEntity)
			throws DtbsSourceGuardEvalExpException
	{
		// 默认应为true，比如当没有定义任何DtbsSourceGuard时
		boolean permitted = true;

		EvaluationContext ctx = this.spelExpressionParser.readonlyMapSimplifyContext();

		for (DtbsSourceGuard dtbsSourceGuard : dtbsSourceGuards)
		{
			if (!dtbsSourceGuard.isEnabled())
				continue;

			boolean matched = evalMatched(dtbsSourceGuard, guardEntity, ctx);

			if (matched)
			{
				permitted = dtbsSourceGuard.isPermitted();
				break;
			}
		}

		return permitted;
	}

	protected boolean evalMatched(DtbsSourceGuard dtbsSourceGuard, GuardEntity guardEntity, EvaluationContext ctx)
			throws DtbsSourceGuardEvalExpException
	{
		boolean urlMatched = isUrlMatched(dtbsSourceGuard, guardEntity);
		boolean userMatched = isUserMatched(dtbsSourceGuard, guardEntity);
		boolean propertiesMatched = isPropertiesMatched(dtbsSourceGuard, guardEntity);

		String evalExp = dtbsSourceGuard.getEvalExp();

		if (StringUtil.isBlank(evalExp))
			evalExp = DEFAULT_EVAL_EXP;

		try
		{
			Expression exp = this.spelExpressionParser.parseExpression(evalExp);

			Map<String, Object> data = new HashMap<>();
			data.put(EVAL_EXP_VAR_URL, urlMatched);
			data.put(EVAL_EXP_VAR_USER, userMatched);
			data.put(EVAL_EXP_VAR_PROPERTIES, propertiesMatched);

			Object matched = this.spelExpressionParser.getValue(exp, ctx, data);
			
			if (matched == null || !(matched instanceof Boolean))
				throw new DtbsSourceGuardEvalExpException("Illegal expression");

			return ((Boolean) matched).booleanValue();
		}
		catch (DtbsSourceGuardEvalExpException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new DtbsSourceGuardEvalExpException(e);
		}
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
