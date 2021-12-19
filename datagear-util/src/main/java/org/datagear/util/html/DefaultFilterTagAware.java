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
 * 默认{@linkplain FilterTagAware}。
 * <p>
 * 它的{@linkplain #isResolveTagAttrs(String)}始终返回{@code false}，其他方法则什么也不做。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultFilterTagAware implements FilterTagAware
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
	public void afterTagEnd(Reader in, Writer out, String tagName, String tagEnd) throws IOException
	{
	}
}
