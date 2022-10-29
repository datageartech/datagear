/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 名称、类型、界面输入相关类接口。
 * 
 * @author datagear@163.com
 *
 */
public interface NameTypeInputAware extends NameTypeAware
{
	/**
	 * 是否必填。
	 * 
	 * @return
	 */
	boolean isRequired();
	
	/**
	 * 获取输入框类型。
	 * 
	 * @return 可能为{@code null}
	 */
	String getInputType();

	/**
	 * 获取输入框载荷。
	 * <p>
	 * 比如：下拉选择输入框时，可以用于定义选项数组；日期输入框时，可以用于定义日期格式字符串。
	 * </p>
	 * 
	 * @return 可能为{@code null}
	 */
	Object getInputPayload();
}
