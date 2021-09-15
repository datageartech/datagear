/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

/**
 * 带有创建用户信息的实体服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface CreateUserEntityService
{
	/**
	 * 更新创建用户。
	 * 
	 * @param oldUserId
	 *            待更新的旧用户ID
	 * @param newUserId
	 *            要更新到的新用户ID
	 * @return
	 */
	int updateCreateUserId(String oldUserId, String newUserId);

	/**
	 * 更新创建用户。
	 * 
	 * @param oldUserIds
	 *            待更新的旧用户ID
	 * @param newUserId
	 *            要更新到的新用户ID
	 * @return
	 */
	int updateCreateUserId(String[] oldUserIds, String newUserId);

	/**
	 * 删除指定用户ID的所有实体。
	 * 
	 * <p>
	 * 注意：
	 * </p>
	 * <p>
	 * 这个接口的目的是为删除用户时同时删除其创建的数据提供支持，但它并不能很好地实现，原因如下：
	 * </p>
	 * <ul>
	 * <li>有些业务的删除操作并不是仅删除数据库记录，也可能有相关的外部资源，导致其无法直接通过SQL实现；</li>
	 * <li>待删除的数据有可能被外键引用，而导致删除SQL出错，或者从业务角度来考虑它们不应被删除；</li>
	 * </ul>
	 * <p>
	 * 所以，这里先注释此接口。
	 * </p>
	 * <p>
	 * 对于删除用户操作，应采用如下方式：
	 * </p>
	 * <ol>
	 * <li>使用{@linkplain #updateCreateUserId(String, String)}先将待删除用户的数据迁移另一个用户；</li>
	 * <li>删除用户信息。</li>
	 * </ol>
	 * 
	 * @param userIds
	 * @return
	 */
	// int deleteByUserId(String... userIds);
}
