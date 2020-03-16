/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import org.datagear.model.Label;

/**
 * 标签工厂。
 * 
 * @author zzf
 * @createDate 2015年8月3日
 * 
 */
public interface LabelFactory
{
	/**
	 * 获取制定关键字对应的{@linkplain Label 标签}。
	 * 
	 * @param key
	 * @return
	 */
	Label getLabel(String key);
}
