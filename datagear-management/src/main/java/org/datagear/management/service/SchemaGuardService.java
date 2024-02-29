/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.service;

import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.domain.User;
import org.datagear.management.util.GuardEntity;
import org.datagear.util.AsteriskPatternMatcher;

/**
 * {@linkplain SchemaGuard}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SchemaGuardService extends EntityService<String, SchemaGuard>
{
	/**
	 * 是否允许创建指定的{@linkplain GuardEntity}。
	 * <p>
	 * 实现类应支持{@linkplain AsteriskPatternMatcher}匹配规则，
	 * 并且，如果没有定义任何{@linkplain SchemaGuard}，应返回{@code true}。
	 * </p>
	 * 
	 * @param guardEntity
	 * @return
	 */
	boolean isPermitted(GuardEntity guardEntity);

	/**
	 * 是否允许创建指定的{@linkplain GuardEntity}。
	 * 
	 * @param user
	 * @param guardEntity
	 * @return
	 */
	boolean isPermitted(User user, GuardEntity guardEntity);
}
