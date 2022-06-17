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
 * 关键字黑名单的{@linkplain SqlValidator}。
 * <p>
 * 如果SQL中出现黑名单中的关键字，校验将不通过。
 * </p>
 * <p>
 * {@linkplain #getDatabaseKeywordRegexes()}映射表中关键字为{@linkplain #DEFAULT_PATTERN_KEY}的匹配模式将优先用于任意{@linkplain DatabaseProfile}，
 * 其次是关键字为{@linkplain DatabaseProfile#getName()}子串的匹配模式、关键字为{@linkplain DatabaseProfile#getUrl()}子串的匹配模式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class KeywordBlacklistSqlValidator extends AbstractSqlValidator
{
	public static final String DEFAULT_PATTERN_KEY = "default";

	private Map<String, Pattern> keywordPatterns = Collections.emptyMap();

	private boolean ignoreSqlString = true;

	private boolean ignoreQuoteIdentifier = true;

	public KeywordBlacklistSqlValidator()
	{
		super();
	}

	public KeywordBlacklistSqlValidator(Map<String, Pattern> keywordPatterns)
	{
		super();
		this.keywordPatterns = keywordPatterns;
	}

	public Map<String, Pattern> getKeywordPatterns()
	{
		return keywordPatterns;
	}

	public void setKeywordPatterns(Map<String, Pattern> keywordPatterns)
	{
		this.keywordPatterns = keywordPatterns;
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

		Pattern dft = this.keywordPatterns.get(DEFAULT_PATTERN_KEY);
		if (dft != null)
			patterns.add(dft);

		findLikeKey(this.keywordPatterns, profile, patterns);

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
	 * 将被转换为：
	 * </p>
	 * <p>
	 * <code>(UPDATE)|(DELETE)|(ALTER)</code>
	 * </p>
	 * <p>
	 * 且忽略大小写。
	 * </p>
	 * 
	 * @param keywords
	 * @return
	 */
	public static Pattern toKeywordsPattern(String... keywords)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < keywords.length; i++)
		{
			if (i > 0)
				sb.append('|');

			sb.append("(" + Pattern.quote(keywords[i]) + ")");
		}

		return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
	}
}
