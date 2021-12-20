/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * 默认{@linkplain TagListener}。
 * <p>
 * 它的{@linkplain #isResolveTagAttrs(String)}、{@linkplain #afterTagEnd(Reader, Writer, String, String)}始终返回{@code false}，其他方法则什么也不做。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultTagListener implements TagListener
{
	@Override
	public void beforeTagStart(Reader in, Writer out, String tagName) throws IOException
	{
	}

	@Override
	public boolean isResolveTagAttrs(Reader in, Writer out, String tagName)
	{
		return false;
	}

	@Override
	public void beforeTagEnd(Reader in, Writer out, String tagName, String tagEnd, Map<String, String> attrs)
			throws IOException
	{
	}

	@Override
	public boolean afterTagEnd(Reader in, Writer out, String tagName, String tagEnd) throws IOException
	{
		return false;
	}

	/**
	 * 是否忽略大小写相等。
	 * 
	 * @param str0
	 *            允许为{@code null}
	 * @param str1
	 *            允许为{@code null}
	 * @return
	 */
	protected boolean equalsIgnoreCase(String str0, String str1)
	{
		if (str0 == null)
			return (str1 == null);
		else
			return str0.equalsIgnoreCase(str1);
	}

	/**
	 * 是否是自关闭标签结束符：{@code />}
	 * 
	 * @param tagEnd
	 * @return
	 */
	protected boolean isSelfCloseTagEnd(String tagEnd)
	{
		return "/>".equals(tagEnd);
	}

	/**
	 * 删除字符串首位空格。
	 * 
	 * @param str
	 *            允许为{@code null}
	 * @return
	 */
	protected String trim(String str)
	{
		return (str == null ? null : str.trim());
	}
}
