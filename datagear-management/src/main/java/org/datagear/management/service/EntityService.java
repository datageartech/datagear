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

import java.util.List;

import org.datagear.management.domain.Entity;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;

/**
 * 基础服务接口。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface EntityService<ID, T extends Entity<ID>>
{
	/**
	 * 添加。
	 * 
	 * @param entity
	 * @return
	 */
	void add(T entity);

	/**
	 * 更新。
	 * 
	 * @param entity
	 */
	boolean update(T entity);

	/**
	 * 删除。
	 * 
	 * @param id
	 */
	boolean deleteById(ID id);

	/**
	 * 删除。
	 * 
	 * @param ids
	 */
	boolean[] deleteByIds(ID[] ids);

	/**
	 * 根据ID获取。
	 * 
	 * @param id
	 * @return
	 */
	T getById(ID id);

	/**
	 * 查询。
	 * 
	 * @param query
	 * @return
	 */
	List<T> query(Query query);

	/**
	 * 分页查询。
	 * 
	 * @param user
	 * @param pagingQuery
	 * @return
	 */
	PagingData<T> pagingQuery(PagingQuery pagingQuery);
}
