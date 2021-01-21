/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.i18n;

/**
 * 可标签对象。
 * 
 * @author datagear@163.com
 *
 */
public interface Labeled
{
	public static final String PROPERTY_NAME_LABEL = "nameLabel";
	public static final String PROPERTY_DESC_LABEL = "descLabel";

	/**
	 * 获取名称标签。
	 * 
	 * @return
	 */
	Label getNameLabel();

	/**
	 * 设置名称标签。
	 * 
	 * @param nameLabel
	 */
	void setNameLabel(Label nameLabel);

	/**
	 * 获取描述标签。
	 * 
	 * @return
	 */
	Label getDescLabel();

	/**
	 * 设置描述标签。
	 * 
	 * @param descLabel
	 */
	void setDescLabel(Label descLabel);
}
