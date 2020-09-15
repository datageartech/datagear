/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.io.File;

/**
 * 目录内文件数据集实体。
 * 
 * @author datagear@163.com
 *
 */
public interface DirectoryFileDataSetEntity extends DataSetEntity
{
	/**
	 * 获取目录。
	 * 
	 * @return
	 */
	File getDirectory();

	/**
	 * 设置目录。
	 * 
	 * @param directory
	 */
	void setDirectory(File directory);

	/**
	 * 获取文件名。
	 * 
	 * @return
	 */
	String getFileName();

	/**
	 * 设置文件名。
	 * 
	 * @param fileName
	 */
	void setFileName(String fileName);

	/**
	 * 获取文件展示名。
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * 设置文件展示名。
	 * 
	 * @param displayName
	 */
	void setDisplayName(String displayName);
}
