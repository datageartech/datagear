/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	boolean add(T entity);

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
