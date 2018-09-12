/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service;

import org.datagear.management.domain.GlobalSetting;
import org.datagear.management.domain.User;

/**
 * {@linkplain GlobalSetting}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface GlobalSettingService
{
	/**
	 * 保存。
	 * 
	 * @param globalSetting
	 */
	void save(GlobalSetting globalSetting);

	/**
	 * 授权保存。
	 * 
	 * @param user
	 * @param globalSetting
	 * @return
	 */
	boolean save(User user, GlobalSetting globalSetting);

	/**
	 * 获取。
	 * 
	 * @return
	 */
	GlobalSetting get();
}
