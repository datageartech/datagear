/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.web.config.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.datagear.util.IOUtil;
import org.datagear.util.TextParserSupport;
import org.datagear.web.util.WebUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

/**
 * 抽象清除资源注释的{@linkplain ResourceTransformer}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractClearCommentResourceTransformer extends TextParserSupport implements ResourceTransformer
{
	/** 路径匹配模式 */
	private String[] pathMatchPatterns;

	/** 资源编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;
	
	/** 是否保留第一个注释 */
	private boolean retentFirstComment = true;
	
	private PathMatcher pathMatcher = new AntPathMatcher();

	public AbstractClearCommentResourceTransformer()
	{
		super();
	}

	public AbstractClearCommentResourceTransformer(String... pathMatchPatterns)
	{
		super();
		this.pathMatchPatterns = pathMatchPatterns;
	}

	public String[] getPathMatchPatterns()
	{
		return pathMatchPatterns;
	}

	public void setPathMatchPatterns(String[] pathMatchPatterns)
	{
		this.pathMatchPatterns = pathMatchPatterns;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public boolean isRetentFirstComment()
	{
		return retentFirstComment;
	}

	public void setRetentFirstComment(boolean retentFirstComment)
	{
		this.retentFirstComment = retentFirstComment;
	}
	
	public PathMatcher getPathMatcher()
	{
		return pathMatcher;
	}

	public void setPathMatcher(PathMatcher pathMatcher)
	{
		this.pathMatcher = pathMatcher;
	}

	@Override
	public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain)
			throws IOException
	{
		String requestPath = WebUtils.resolvePathAfter(request, "");
		resource = transformerChain.transform(request, resource);
		
		if(!isMatchRequestPath(requestPath))
			return resource;
		
		Reader in = null;
		StringWriter out = null;
		
		try
		{
			in = IOUtil.getReader(resource.getInputStream(), this.encoding);
			out = new StringWriter();
			writeNoComment(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
		
		return new TransformedResource(resource, out.toString().getBytes(this.encoding));
	}
	
	/**
	 * 写入并忽略注释。
	 * <p>
	 * 如果{@linkplain #isRetentFirstComment()}为{@code true}，第一个注释应保留。
	 * </p>
	 * <p>
	 * 如果{@linkplain #getFirstCommentReplacement()}不为空，应作为替换文本。
	 * </p>
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	protected abstract void writeNoComment(Reader in, Writer out) throws IOException;
	
	protected boolean isMatchRequestPath(String requestPath)
	{
		for(String pattern : this.pathMatchPatterns)
		{
			if(this.pathMatcher.match(pattern, requestPath))
				return true;
		}
		
		return false;
	}
	
	protected static class NopWriter extends Writer
	{
		public NopWriter()
		{
			super();
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException
		{
		}

		@Override
		public void flush() throws IOException
		{
		}

		@Override
		public void close() throws IOException
		{
		}
	}
}
