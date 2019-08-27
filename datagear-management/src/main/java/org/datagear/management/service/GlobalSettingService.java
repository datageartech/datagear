/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import org.datagear.management.domain.GlobalSetting;

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
	 * 获取。
	 * 
	 * @return
	 */
	GlobalSetting get();
}
