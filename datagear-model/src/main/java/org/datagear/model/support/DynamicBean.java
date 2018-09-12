/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.util.Map;

import org.datagear.model.Model;

/**
 * 动态Bean。
 * 
 * @author datagear@163.com
 *
 */
public interface DynamicBean extends Map<String, Object>
{
	/**
	 * 获取此动态Bean对应的{@linkplain Model 模型}。
	 * 
	 * @return
	 */
	Model getModel();

	/**
	 * 设置此动态Bean对应的{@linkplain Model 模型}。
	 * 
	 * @param model
	 */
	void setModel(Model model);
}
