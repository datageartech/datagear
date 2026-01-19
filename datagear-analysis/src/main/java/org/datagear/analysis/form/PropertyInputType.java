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

package org.datagear.analysis.form;

/**
 * 常用的{@linkplain InputFormProperty#getInputType()}枚举。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyInputType
{
	/** 文本框 */
	public static final String TEXT = "text";

	/** 下拉框 */
	public static final String SELECT = "select";

	/** 单选框 */
	public static final String RADIO = "radio";

	/** 复选框 */
	public static final String CHECKBOX = "checkbox";

	/** 文本域 */
	public static final String TEXTAREA = "textarea";

	/** 颜色 */
	public static final String COLOR = "color";
}