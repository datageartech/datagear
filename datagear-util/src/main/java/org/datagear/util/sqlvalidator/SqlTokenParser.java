/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.sqlvalidator;

import java.util.Collections;
import java.util.List;

import org.datagear.util.StringUtil;
import org.datagear.util.TextParserSupport;

/**
 * SQL单词解析器。
 * <p>
 * 此类依据空格符（空格、换行、制表符等）、SQL字符串（{@code '...'}）、SQL标识引用符串（{@code 引用符...引用符}）、
 * 行注释（<code>&#47&#47...</code>）、块注释（<code>&#47&#42...&#42&#47</code>）解析单词。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlTokenParser extends TextParserSupport
{
	/**
	 * 解析SQL单词。
	 * 
	 * @param sql
	 * @param identifierQuote 数据库标识引用符
	 * @return
	 */
	public List<String> parse(String sql, String identifierQuote)
	{
		if (StringUtil.isEmpty(sql))
			return Collections.emptyList();

		// TODO

		return null;
	}
}
