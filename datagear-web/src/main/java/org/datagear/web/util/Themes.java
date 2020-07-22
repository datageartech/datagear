/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.util;

import java.util.Arrays;
import java.util.List;

/**
 * 系统支持的主题枚举。
 * 
 * @author datagear@163.com
 */
public interface Themes
{
	/** 浅色 */
	String LIGHT = "light";

	/** 暗色 */
	String DARK = "dark";

	/** 绿色 */
	String GREEN = "green";

	/** 所有主题列表 */
	List<String> THEMES = Arrays.asList(LIGHT, DARK, GREEN);
}
