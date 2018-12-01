/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.Connection;

import org.datagear.model.Model;
import org.datagear.model.support.PropertyPathInfo;
import org.datagear.persistence.support.ExpressionEvaluationContext;

/**
 * 持久化管理器。
 * 
 * @author datagear@163.com
 * @createDate 2014年11月28日
 */
public interface PersistenceManager
{
	/**
	 * 获取指定{@linkplain Model}的表名称。
	 * 
	 * @param model
	 * @return
	 */
	String getTableName(Model model);

	/**
	 * 获取此{@linkplain PersistenceManager}使用的{@linkplain DialectSource}。
	 * 
	 * @return
	 */
	DialectSource getDialectSource();

	/**
	 * 插入数据。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @throws PersistenceException
	 */
	void insert(Connection cn, Model model, Object obj) throws PersistenceException;

	/**
	 * 插入数据
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param obj
	 * @param expressionEvaluationContext
	 * @return
	 * @throws PersistenceException
	 */
	int insert(Connection cn, Dialect dialect, String table, Model model, Object obj,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException;

	/**
	 * 更新数据。
	 * 
	 * @param cn
	 * @param model
	 * @param originalObj
	 * @param updateObj
	 * @param updateMultipleProperty
	 * @return
	 * @throws PersistenceException
	 */
	int update(Connection cn, Model model, Object originalObj, Object updateObj, boolean updateMultipleProperty)
			throws PersistenceException;

	/**
	 * 更新数据。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param originalObj
	 * @param updateObj
	 * @param expressionEvaluationContext
	 * @return
	 * @throws PersistenceException
	 */
	int update(Connection cn, Dialect dialect, String table, Model model, Object originalObj, Object updateObj,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException;

	/**
	 * 插入单元属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValue
	 * @throws PersistenceException
	 */
	void insertSinglePropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propValue) throws PersistenceException;

	/**
	 * 更新单元属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValue
	 * @throws PersistenceException
	 */
	void updateSinglePropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propValue) throws PersistenceException;

	/**
	 * 插入多元属性值元素。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValueElements
	 * @throws PersistenceException
	 */
	void insertMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object... propValueElements) throws PersistenceException;

	/**
	 * 插入多元属性值元素。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValueElement
	 * @param expressionEvaluationContext
	 * @throws PersistenceException
	 */
	void insertMultiplePropValueElement(Connection cn, Dialect dialect, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, Object propValueElement,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException;

	/**
	 * 更新多元属性值元素。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propertyValueElement
	 * @throws PersistenceException
	 */
	void updateMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propertyValueElement) throws PersistenceException;

	/**
	 * 删除对象。
	 * 
	 * @param cn
	 * @param model
	 * @param objs
	 * @return
	 * @throws PersistenceException
	 */
	int delete(Connection cn, Model model, Object... objs) throws PersistenceException;

	/**
	 * 删除多元属性值元素。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValueElements
	 * @throws PersistenceException
	 */
	void deleteMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object... propValueElements) throws PersistenceException;

	/**
	 * 获取对象。
	 * 
	 * @param cn
	 * @param model
	 * @param param
	 * @return
	 * @throws PersistenceException
	 */
	Object get(Connection cn, Model model, Object param) throws PersistenceException;

	/**
	 * 获取属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @return
	 * @throws PersistenceException
	 */
	Object getPropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo)
			throws PersistenceException;

	/**
	 * 获取属性值元素。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propValueElementParam
	 * @return
	 * @throws PersistenceException
	 */
	Object getMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propValueElementParam) throws PersistenceException;

	/**
	 * 分页查询。
	 * 
	 * @param cn
	 * @param model
	 * @param pagingQuery
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Object> query(Connection cn, Model model, PagingQuery pagingQuery) throws PersistenceException;

	/**
	 * 获取{@linkplain #query(Connection, Model, PagingQuery)}方法底层查询结果集的{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param cn
	 * @param model
	 * @return
	 * @throws PersistenceException
	 */
	QueryResultMetaInfo getQueryResultMetaInfo(Connection cn, Model model) throws PersistenceException;

	/**
	 * 分页查询多元属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param pagingQuery
	 * @param propertyModelQueryPattern
	 *            是否是属性模型级的查询方式：查询关键字、条件SQL、排序SQL仅包含属性模型级的属性路径，
	 *            具体参考{@linkplain #getQueryMultiplePropValueQueryResultMetaInfo(Connection, Model, Object, PropertyPathInfo, boolean)}。
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Object> queryMultiplePropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			PagingQuery pagingQuery, boolean propertyModelQueryPattern) throws PersistenceException;

	/**
	 * 获取{@linkplain #queryMultiplePropValue(Connection, Model, Object, PropertyPathInfo, PagingQuery)}方法底层查询结果集的{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param propertyModelPattern
	 *            是否采用属性模型方式，如果为{@code true}，返回{@linkplain QueryResultMetaInfo#getColumnPropertyPaths()}列表中仅包含此属性模型的{@linkplain ColumnPropertyPath}，
	 *            且{@linkplain ColumnPropertyPath#getPropertyPath()}将被截取至属性模型级的属性路径。
	 * @return
	 * @throws PersistenceException
	 */
	QueryResultMetaInfo getQueryMultiplePropValueQueryResultMetaInfo(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, boolean propertyModelPattern) throws PersistenceException;

	/**
	 * 分页查询属性值可选源。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @param pagingQuery
	 * @return
	 * @throws PersistenceException
	 */
	PagingData<Object> queryPropValueSource(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			PagingQuery pagingQuery) throws PersistenceException;

	/**
	 * 获取{@linkplain #queryPropValueSource(Connection, Model, Object, PropertyPathInfo, PagingQuery)}方法底层查询结果集的{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param cn
	 * @param model
	 * @param obj
	 * @param propertyPathInfo
	 * @return
	 * @throws PersistenceException
	 */
	QueryResultMetaInfo getQueryPropValueSourceQueryResultMetaInfo(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo) throws PersistenceException;

}
