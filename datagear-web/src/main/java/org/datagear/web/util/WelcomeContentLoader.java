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

package org.datagear.web.util;

import java.io.File;

import org.datagear.util.FileContentLoader;
import org.datagear.util.FileUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 欢迎页内容加载类。
 * <p>
 * 如果{@linkplain #getContent()}是以{@code "file:"}开头，将从文件中加载。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class WelcomeContentLoader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeContentLoader.class);

	public static final String FILE_PREFIX = "file:";

	private final String content;

	private final FileContentLoader _fileContentLoader;

	public WelcomeContentLoader(String content, String fileEncoding)
	{
		super();
		this.content = content;

		// 文件
		if (!StringUtil.isEmpty(content) && content.startsWith(FILE_PREFIX))
		{
			File file = FileUtil.getFile(content.substring(FILE_PREFIX.length()));
			this._fileContentLoader = new FileContentLoader(file, fileEncoding);
		}
		else
			this._fileContentLoader = null;
	}

	public String getContent()
	{
		return content;
	}

	/**
	 * 加载欢迎内容。
	 * 
	 * @return 可能{@code null}
	 */
	public String load()
	{
		String re = null;

		if (this._fileContentLoader == null)
		{
			re = this.content;
		}
		else
		{
			try
			{
				re = this._fileContentLoader.load();
			}
			catch (Exception e)
			{
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Load welcome content error", e);

				re = null;
			}
		}

		return re;
	}
}
