/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service;

import java.util.List;

import org.datagear.management.domain.User;
import org.datagear.model.support.Entity;
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
	 * 授权更新。
	 * <p>
	 * 返回{@code false}表明记录已不存在或者操作用户无权限。
	 * </p>
	 * 
	 * @param user
	 *            操作用户
	 * @param schema
	 * @return
	 */
	boolean update(User user, T entity);

	/**
	 * 删除。
	 * 
	 * @param id
	 */
	boolean deleteById(ID id);

	/**
	 * 授权删除。
	 * <p>
	 * 返回{@code false}表明记录已不存在或者操作用户无权限。
	 * </p>
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 */
	boolean deleteById(User user, ID id);

	/**
	 * 根据ID获取。
	 * 
	 * @param id
	 * @return
	 */
	T getById(ID id);

	/**
	 * 授权根据ID获取。
	 * <p>
	 * 返回{@code null}表明记录已不存在或者操作用户无权限。
	 * </p>
	 * 
	 * @param user
	 *            操作用户
	 * @param id
	 * @return
	 */
	T getById(User user, ID id);

	/**
	 * 查询。
	 * 
	 * @param query
	 * @return
	 */
	List<T> query(Query query);

	/**
	 * 授权查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param query
	 * @return
	 */
	List<T> query(User user, Query query);

	/**
	 * 分页查询。
	 * 
	 * @param user
	 * @param pagingQuery
	 * @return
	 */
	PagingData<T> pagingQuery(PagingQuery pagingQuery);

	/**
	 * 授权分页查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param pagingQuery
	 * @return
	 */
	PagingData<T> pagingQuery(User user, PagingQuery pagingQuery);
}
