/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 基于关键字黑名单的{@linkplain SqlValidator}。
 * 
 * @author datagear@163.com
 *
 */
public class KeywordBacklistSqlValidator extends AbstractSqlValidator
{
	public static final String KEY_DEFAULT = "default";

	private Map<String, Pattern> databaseKeywordRegexes = Collections.emptyMap();

	private SqlReplacer sqlReplacer = new SqlReplacer();

	public KeywordBacklistSqlValidator(Map<String, Pattern> databaseKeywordRegexes)
	{
		super();
		this.databaseKeywordRegexes = databaseKeywordRegexes;
	}

	public Map<String, Pattern> getDatabaseKeywordRegexes()
	{
		return databaseKeywordRegexes;
	}

	public void setDatabaseKeywordRegexes(Map<String, Pattern> databaseKeywordRegexes)
	{
		this.databaseKeywordRegexes = databaseKeywordRegexes;
	}

	public SqlReplacer getSqlReplacer()
	{
		return sqlReplacer;
	}

	public void setSqlReplacer(SqlReplacer sqlReplacer)
	{
		this.sqlReplacer = sqlReplacer;
	}

	@Override
	public boolean validate(String sql, DatabaseProfile profile)
	{
		sql = this.sqlReplacer.replace(sql, profile.getIdentifierQuote());

		// TODO

		return false;
	}

	protected String replaceSqlStringAndQuoteIdentifier(String sql, DatabaseProfile profile)
	{
		return this.sqlReplacer.replace(sql, profile.getIdentifierQuote());
	}

	public static class KeywordList implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String keywords;

		public KeywordList(String keywords)
		{
			super();
			this.keywords = keywords;
		}

		public String getKeywords()
		{
			return keywords;
		}

		public void setKeywords(String keywords)
		{
			this.keywords = keywords;
		}
	}
}
