/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 黑名单正则模式{@linkplain SqlValidator}。
 * <p>
 * 如果SQL匹配黑名单正则模式，校验将不通过。
 * </p>
 * <p>
 * {@linkplain #getPatterns()}映射表中关键字为{@linkplain #DEFAULT_PATTERN_KEY}的正则模式将优先用于任意{@linkplain DatabaseProfile}，
 * 其次是关键字为{@linkplain DatabaseProfile#getName()}子串的正则模式、关键字以{@linkplain DatabaseProfile#getUrl()}开头的正则模式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class InvalidPatternSqlValidator extends AbstractSqlValidator
{
	public static final String DEFAULT_PATTERN_KEY = "default";

	/** 数据库名子串/URL子串 - 正则模式 */
	private Map<String, Pattern> patterns = Collections.emptyMap();

	private boolean ignoreSqlString = true;

	private boolean ignoreQuoteIdentifier = true;

	public InvalidPatternSqlValidator()
	{
		super();
	}

	public InvalidPatternSqlValidator(Map<String, Pattern> patterns)
	{
		super();
		this.patterns = patterns;
	}

	public Map<String, Pattern> getPatterns()
	{
		return patterns;
	}

	public void setPatterns(Map<String, Pattern> patterns)
	{
		this.patterns = patterns;
	}

	public boolean isIgnoreSqlString()
	{
		return ignoreSqlString;
	}

	public void setIgnoreSqlString(boolean ignoreSqlString)
	{
		this.ignoreSqlString = ignoreSqlString;
	}

	public boolean isIgnoreQuoteIdentifier()
	{
		return ignoreQuoteIdentifier;
	}

	public void setIgnoreQuoteIdentifier(boolean ignoreQuoteIdentifier)
	{
		this.ignoreQuoteIdentifier = ignoreQuoteIdentifier;
	}

	@Override
	public SqlValidation validate(String sql, DatabaseProfile profile)
	{
		SqlValidation validation = new SqlValidation(true);
		
		sql = replaceSqlIfNeed(sql, profile);

		List<Pattern> patterns = findPatterns(profile);
		
		for (Pattern pattern : patterns)
		{
			validation = validate(sql, pattern);

			if (!validation.isValid())
				return validation;
		}

		return validation;
	}

	protected List<Pattern> findPatterns(DatabaseProfile profile)
	{
		List<Pattern> patterns = new ArrayList<Pattern>(3);

		Pattern dft = this.patterns.get(DEFAULT_PATTERN_KEY);
		if (dft != null)
			patterns.add(dft);

		findMatchKey(this.patterns, profile, patterns);

		return patterns;
	}

	protected SqlValidation validate(String sql, Pattern pattern)
	{
		Matcher matcher = pattern.matcher(sql);
		boolean match = matcher.find();

		if (!match)
			return new SqlValidation(true);

		int start = matcher.start();
		int end = matcher.end();

		return new SqlValidation(sql.substring(start, end));
	}

	protected String replaceSqlIfNeed(String sql, DatabaseProfile profile)
	{
		if (sql == null)
			sql = "";

		if (!this.ignoreSqlString && !this.ignoreQuoteIdentifier)
			return sql;

		SqlReplacer sr = new SqlReplacer();
		sr.setReplaceSqlString(this.ignoreQuoteIdentifier);
		sr.setReplaceQuoteIdentifier(this.ignoreQuoteIdentifier);

		return sr.replace(sql, profile.getIdentifierQuote());
	}

	/**
	 * 构建关键字匹配模式。
	 * <p>
	 * 例如：
	 * </p>
	 * <p>
	 * <code>["UPDATE", "DELETE", "ALTER"]</code>
	 * </p>
	 * <p>
	 * 转换后的正则含义：
	 * </p>
	 * <p>
	 * 包含这些关键字且没以{@code _}、{@code 0-9}、{@code a-z}、{@code A-Z}开头，也没有以它们结尾
	 * </p>
	 * 
	 * @param keywords
	 * @return
	 */
	public static Pattern toKeywordPattern(String... keywords)
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < keywords.length; i++)
		{
			if (i > 0)
				sb.append('|');

			sb.append("(" + "[^\\_\\w]" + Pattern.quote(keywords[i]) + "[^\\_\\w]" + ")");
		}

		return compileToSqlValidatorPattern(sb.toString());
	}
	
	/**
	 * 将正则字符串编译为用于{@linkplain SqlValidator}的正则对象（匹配时忽略大小写）。
	 * 
	 * @param pattern
	 * @return
	 */
	public static Pattern compileToSqlValidatorPattern(String pattern)
	{
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}
}
