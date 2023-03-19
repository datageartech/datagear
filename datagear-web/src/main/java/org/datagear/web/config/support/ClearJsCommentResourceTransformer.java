/*
 * Copyright 2018-2023 datagear.tech
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
import java.io.Writer;

import org.springframework.web.servlet.resource.ResourceTransformer;

/**
 * 清除JS资源注释的{@linkplain ResourceTransformer}。
 * <p>
 * 清除JS资源内容中的块注释（<code>&#47&#42...&#42&#47</code>）、行注释（<code>&#47&#47...</code>）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ClearJsCommentResourceTransformer extends AbstractClearCommentResourceTransformer implements ResourceTransformer
{
	public ClearJsCommentResourceTransformer()
	{
		super();
	}

	public ClearJsCommentResourceTransformer(String... pathMatchPatterns)
	{
		super(pathMatchPatterns);
	}

	@Override
	protected void writeNoComment(Reader in, Writer out) throws IOException
	{
		boolean isFirstComment = true;
		boolean retentFirstComment = this.isRetentFirstComment();

		@SuppressWarnings("resource")
		NopWriter nopWriter = new NopWriter();

		int c = -1;
		while ((c = in.read()) > -1)
		{
			//转义符，忽略下一个字符，比如正则表达式语法中
			if(c == '\\')
			{
				int c1 = in.read();
				out.write(c);
				writeIfValid(out, c1);
			}
			// 字符串
			else if (isJsQuote(c))
			{
				out.write(c);
				writeAfterQuote(in, out, c, '\\');
			}
			else if (c == '/')
			{
				int c1 = in.read();
				
				//行注释
				if(c1 == '/')
				{
					boolean clear = (!isFirstComment || (isFirstComment && !retentFirstComment));
					Writer myOut = (clear ? nopWriter : out);
					
					myOut.write(c);
					myOut.write(c1);
					int next = writeAfterLineComment(in, myOut);
					writeIfValid(myOut, next);
					
					if(isFirstComment)
						isFirstComment = false;
				}
				//块注释
				else if(c1 == '*')
				{
					boolean clear = (!isFirstComment || (isFirstComment && !retentFirstComment));
					Writer myOut = (clear ? nopWriter : out);
					
					myOut.write(c);
					myOut.write(c1);
					writeAfterBlockComment(in, myOut);
					
					if(isFirstComment)
						isFirstComment = false;
				}
				else
				{
					out.write(c);
					writeIfValid(out, c1);
				}
			}
			else
				out.write(c);
		}
	}
}
