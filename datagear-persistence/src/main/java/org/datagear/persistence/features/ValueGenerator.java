/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性值生成器。
 * <p>
 * 它可用于如下场景：
 * </p>
 * <ul>
 * <li>所有属性：
 * <p>
 * 用于定义插入时的属性值生成逻辑。
 * </p>
 * </li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public interface ValueGenerator extends PropertyFeature, PersistenceFeature
{
	/**
	 * 生成的属性值是否是SQL语句。
	 * 
	 * @param model
	 * @param property
	 * @param obj
	 * @return
	 */
	boolean isSql(Model model, Property property, Object obj);

	/**
	 * 生成属性值。
	 * 
	 * @param model
	 *            当前模型
	 * @param property
	 *            当前属性
	 * @param obj
	 *            当前模型数据对象
	 * @return
	 */
	Object generate(Model model, Property property, Object obj);
}
