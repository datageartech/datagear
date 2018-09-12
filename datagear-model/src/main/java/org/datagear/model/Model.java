/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model;

/**
 * 模型。
 * <p>
 * <i>模型</i>描述数据对象元信息，由一组描述数据对象属性元信息的{@linkplain Property 属性}组成。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface Model extends Featured
{
	/**
	 * 获取名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取类型。
	 * 
	 * @return
	 */
	Class<?> getType();

	/**
	 * 是否包含{@linkplain Property 属性}。
	 * 
	 * @return
	 */
	boolean hasProperty();

	/**
	 * 获取所有{@linkplain Property 属性}数组。
	 * <p>
	 * 如果{@linkplain #hasProperty()}为{@code true}，此方法应该返回一个非空数组。
	 * </p>
	 * 
	 * @return
	 */
	Property[] getProperties();

	/**
	 * 获取指定属性名的{@linkplain Property 属性}。
	 * <p>
	 * 如果没有，此方法应该返回{@code null}。
	 * </p>
	 * 
	 * @param name
	 *            属性名称
	 * @return
	 */
	Property getProperty(String name);

	/**
	 * 获取指定位置的{@linkplain Property 属性}。
	 * 
	 * @param index
	 * @return
	 */
	Property getProperty(int index);

	/**
	 * 是否有ID属性。
	 * 
	 * @return
	 */
	boolean hasIdProperty();

	/**
	 * 获取ID属性数组。
	 * <p>
	 * 如果{@linkplain #hasIdProperty()}为{@code true}，此方法应该返回一个非空数组。
	 * </p>
	 * 
	 * @return
	 */
	Property[] getIdProperties();

	/**
	 * 是否有唯一键。
	 * 
	 * @return
	 */
	boolean hasUniqueProperty();

	/**
	 * 获取所有唯一键。
	 * <p>
	 * 每一个元素代表一组唯一键。
	 * </p>
	 * 
	 * @return
	 */
	Property[][] getUniqueProperties();

	/**
	 * 创建指此模型的实例对象。
	 * 
	 * @return
	 * @throws InstanceCreationException
	 */
	Object newInstance() throws InstanceCreationException;

	/**
	 * 是否是<i>镜像模型</i>。
	 * <p>
	 * <i>镜像模型</i>描述由某个实际模型的部分属性组成的概念性、虚拟性的模型。
	 * </p>
	 *
	 * @return
	 */
	// boolean isMirror();

	/**
	 * 获取此<i>镜像模型</i>对应的<i>真实模型</i>。
	 * <p>
	 * 如果{@linkplain #isMirror()}为{@code true}，此方法不应该返回{@code null}。
	 * </p>
	 *
	 * @return
	 */
	// Model getRealModel();

	/**
	 * 获取镜像属性名称映射表。
	 * <p>
	 * 返回映射表的关键字为<i>镜像属性名</i>，值为<i>真实属性名</i>。
	 * </p>
	 * <p>
	 * 如果{@linkplain #isMirror()}为{@code true}，此方法应该返回非空映射表，并且包含此<i>镜像模型</i>的所有
	 * {@linkplain #getProperties() 属性}名。
	 * </p>
	 *
	 * @return
	 */
	// Map<String, String> getPropertyMirrors();

	/**
	 * 获取指定<i>镜像属性名</i>的<i>真实属性名</i>。
	 * <p>
	 * 如果没找到，此方法应该返回{@code null}。
	 * </p>
	 *
	 * @param name
	 * @return
	 */
	// String getPropertyMirror(String name);
}
