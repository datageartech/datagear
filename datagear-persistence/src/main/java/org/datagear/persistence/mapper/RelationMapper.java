/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 关系映射接口类。
 * <p>
 * 持久化处理时，会大量使用此接口，为了提高效率和便于使用，将此接口设计为{@linkplain PersistenceFeature}特性。
 * </p>
 * <p>
 * 注意：此特性的逻辑依赖于{@linkplain org.datagear.persistence.features} 包下的特性，因此在调用
 * {@linkplain RelationMapperResolver}解析时，要确保依赖特性都已设置完成。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface RelationMapper extends PropertyFeature, PersistenceFeature
{
	/**
	 * 获取{@linkplain Mapper}数组。
	 * <p>
	 * 如果属性是抽象属性，则返回数组与抽象属性的具体模型数组一一对应；否则，返回数组仅包含一个元素；
	 * </p>
	 * 
	 * @return
	 */
	Mapper[] getMappers();

	/**
	 * 是否是一对一关联。
	 * 
	 * @return
	 */
	boolean isOneToOne();

	/**
	 * 是否是一对多关联。
	 * 
	 * @return
	 */
	boolean isOneToMany();

	/**
	 * 是否是多对一关联。
	 * 
	 * @return
	 */
	boolean isManyToOne();

	/**
	 * 是否是多对多关联。
	 * 
	 * @return
	 */
	boolean isManyToMany();
}
