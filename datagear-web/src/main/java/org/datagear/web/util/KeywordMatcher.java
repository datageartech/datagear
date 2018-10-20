/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 关键字查找工具类。
 * 
 * @author datagear@163.com
 *
 */
public class KeywordMatcher
{
	/**
	 * 关键字匹配。
	 * 
	 * @param list
	 * @param keyword
	 * @param matchValue
	 * @return
	 */
	public static <T> List<T> match(List<T> list, String keyword, MatchValue<T> matchValue)
	{
		KeywordInfo keywordInfo = resolveKeywordInfo(keyword);

		if (keywordInfo == null)
			return list;

		List<T> result = new ArrayList<T>();

		for (T obj : list)
		{
			if (match(obj, keywordInfo, matchValue))
				result.add(obj);
		}

		return result;
	}

	/**
	 * 关键字匹配。
	 * 
	 * @param array
	 * @param keyword
	 * @param matchValue
	 * @return
	 */
	public static <T> List<T> match(T[] array, String keyword, MatchValue<T> matchValue)
	{
		KeywordInfo keywordInfo = resolveKeywordInfo(keyword);

		if (keywordInfo == null)
			return Arrays.asList(array);

		List<T> result = new ArrayList<T>();

		for (T obj : array)
		{
			if (match(obj, keywordInfo, matchValue))
				result.add(obj);
		}

		return result;
	}

	protected static <T> boolean match(T obj, KeywordInfo keywordInfo, MatchValue<T> matchValue)
	{
		String[] upperValues = matchValue.get(obj);

		if (upperValues == null)
			return false;

		MatchType matchType = keywordInfo.getMatchType();
		String keyword = keywordInfo.getUpperKeyword();

		for (int i = 0; i < upperValues.length; i++)
		{
			if (upperValues[i] != null)
				upperValues[i] = upperValues[i].toUpperCase();
		}

		if (MatchType.START.equals(keywordInfo.matchType))
		{
			for (String upperValue : upperValues)
			{
				if (upperValue != null && upperValue.startsWith(keyword))
					return true;
			}

			return false;
		}
		else if (MatchType.END.equals(matchType))
		{
			for (String upperValue : upperValues)
			{
				if (upperValue != null && upperValue.endsWith(keyword))
					return true;
			}

			return false;
		}
		else if (MatchType.CONTAIN.equals(matchType))
		{
			for (String upperValue : upperValues)
			{
				if (upperValue != null && upperValue.indexOf(keyword) >= 0)
					return true;
			}

			return false;
		}
		else
			return false;
	}

	/**
	 * 解析{@linkplain KeywordInfo}。
	 * 
	 * @param keyword
	 * @return
	 */
	protected static KeywordInfo resolveKeywordInfo(String keyword)
	{
		if (keyword == null)
			return null;

		keyword = keyword.trim();

		if (keyword.isEmpty())
			return null;

		MatchType matchType;

		if (keyword.startsWith("%"))
		{
			matchType = MatchType.END;
			keyword = keyword.substring(1);

			if (keyword.endsWith("%"))
			{
				matchType = MatchType.CONTAIN;
				keyword = keyword.substring(0, keyword.length() - 1);
			}
		}
		else
		{
			matchType = MatchType.CONTAIN;

			if (keyword.endsWith("%"))
			{
				matchType = MatchType.START;
				keyword = keyword.substring(0, keyword.length() - 1);
			}
		}

		return new KeywordInfo(matchType, keyword);
	}

	protected static class KeywordInfo
	{
		private MatchType matchType;

		private String keyword;

		private String upperKeyword;

		public KeywordInfo()
		{
			super();
		}

		public KeywordInfo(MatchType matchType, String keyword)
		{
			super();
			this.matchType = matchType;
			this.keyword = keyword;
			this.upperKeyword = keyword.toUpperCase();
		}

		public MatchType getMatchType()
		{
			return matchType;
		}

		public void setMatchType(MatchType matchType)
		{
			this.matchType = matchType;
		}

		public String getKeyword()
		{
			return keyword;
		}

		public void setKeyword(String keyword)
		{
			this.keyword = keyword;
		}

		public String getUpperKeyword()
		{
			return upperKeyword;
		}
	}

	protected enum MatchType
	{
		START,

		CONTAIN,

		END
	}

	/**
	 * 用于提取关键字匹配值的接口类。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	public static interface MatchValue<T>
	{
		/**
		 * 提取关键字匹配值。
		 * 
		 * @param t
		 * @return
		 */
		String[] get(T t);
	}
}
