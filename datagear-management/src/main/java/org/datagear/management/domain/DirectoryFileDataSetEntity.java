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

package org.datagear.management.domain;

import java.io.File;

import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.analysis.support.FileResolvedInfo;
import org.datagear.util.FileUtil;

/**
 * 目录内文件数据集实体。
 * <p>
 * 文件有两种类型：
 * 用户上传的文件，保存至{@linkplain #getDirectory()}内的{@linkplain #getFileName()}文件；
 * 服务器端文件，本来就存储在服务器磁盘指定目录内的文件，
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DirectoryFileDataSetEntity extends DataSetEntity
{
	/** 文件类型：用户上传文件 */
	String FILE_SOURCE_TYPE_UPLOAD = "UPLOAD";

	/** 文件类型：服务器端文件 */
	String FILE_SOURCE_TYPE_SERVER = "SERVER";

	/**
	 * 获取文件源类型。
	 * 
	 * @return
	 */
	String getFileSourceType();

	/**
	 * 设置文件类型：{@linkplain #FILE_SOURCE_TYPE_UPLOAD}、{@linkplain #FILE_SOURCE_TYPE_SERVER}。
	 * 
	 * @param fileSourceType
	 */
	void setFileSourceType(String fileSourceType);

	/**
	 * 获取用户上传文件的存储目录。
	 * 
	 * @return 当{@linkplain #getFileSourceType()}为{@linkplain #FILE_SOURCE_TYPE_UPLOAD}时不应为{@code null}
	 */
	File getDirectory();

	/**
	 * 设置用户上传文件的存储目录。
	 * 
	 * @param directory
	 */
	void setDirectory(File directory);

	/**
	 * 获取用户上传文件的文件名。
	 * 
	 * @return 当{@linkplain #getFileSourceType()}为{@linkplain #FILE_SOURCE_TYPE_UPLOAD}时不应为{@code null}
	 */
	String getFileName();

	/**
	 * 设置用户上传文件的存储文件名。
	 * 
	 * @param fileName
	 */
	void setFileName(String fileName);

	/**
	 * 获取用户上传文件的展示名。
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * 设置用户上传文件的文件展示名。
	 * 
	 * @param displayName
	 */
	void setDisplayName(String displayName);

	/**
	 * 获取服务器端文件所在的目录。
	 * 
	 * @return 当{@linkplain #getFileSourceType()}为{@linkplain #FILE_SOURCE_TYPE_SERVER}时不应为{@code null}
	 */
	FileSource getFileSource();

	/**
	 * 设置服务器端文件所在的目录。
	 * 
	 * @param fileSource
	 */
	void setFileSource(FileSource fileSource);

	/**
	 * 获取服务器端文件的文件名（相对于{@linkplain #getFileSource()}）。
	 * 
	 * @return 当{@linkplain #getFileSourceType()}为{@linkplain #FILE_SOURCE_TYPE_SERVER}时不应为{@code null}
	 */
	String getDataSetResFileName();

	/**
	 * 设置服务器端文件的文件名（相对于{@linkplain #getFileSource()}）。
	 * 
	 * @param fileName
	 */
	void setDataSetResFileName(String fileName);

	/**
	 * 解析模板：文件名。
	 * <p>
	 * 实现规则应与{@linkplain AbstractDataSet#resolveTemplatePlain(String, DataSetQuery)}一致。
	 * </p>
	 * 
	 * @param fileName
	 * @param query
	 * @return
	 */
	String resolveTemplateFileName(String fileName, DataSetQuery query);

	/**
	 * 获取文件。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	default FileResolvedInfo getFileForDataSetQuery(DataSetQuery query) throws Throwable
	{
		File file = null;

		if (FILE_SOURCE_TYPE_UPLOAD.equals(getFileSourceType()))
		{
			file = FileUtil.getFile(getDirectory(), getFileName());

			return new FileResolvedInfo(file);
		}
		else if (FILE_SOURCE_TYPE_SERVER.equals(getFileSourceType()))
		{
			// 服务器端文件名允许参数化
			String fileName = resolveTemplateFileName(getDataSetResFileName(), query);

			File directory = FileUtil.getDirectory(getFileSource().getDirectory(), false);
			file = FileUtil.getFile(directory, fileName, false);

			return new FileResolvedInfo(file, fileName);
		}
		else
			throw new IllegalStateException("Unknown file source type :" + getFileSourceType());
	}
}
