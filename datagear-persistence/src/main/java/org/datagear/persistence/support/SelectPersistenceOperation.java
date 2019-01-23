/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.features.NotReadable;
import org.datagear.model.features.Token;
import org.datagear.model.support.DynamicBean;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.model.support.PropertyPath;
import org.datagear.persistence.ColumnPropertyPath;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.Query;
import org.datagear.persistence.QueryResultMetaInfo;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.collection.SizeOnlyCollection;
import org.datagear.persistence.features.ManyToMany;
import org.datagear.persistence.features.ManyToOne;
import org.datagear.persistence.features.OneToMany;
import org.datagear.persistence.features.OneToOne;
import org.datagear.persistence.mapper.JoinTableMapper;
import org.datagear.persistence.mapper.ModelTableMapper;
import org.datagear.persistence.mapper.PropertyModelMapper;
import org.datagear.persistence.mapper.PropertyTableMapper;
import org.datagear.persistence.mapper.RelationMapper;

/**
 * 查询持久化操作。
 * 
 * @author datagear@163.com
 *
 */
public class SelectPersistenceOperation extends AbstractModelPersistenceOperation
{
	private SelectOptions selectOptions;

	private Map<Class<?>, Class<?>> collectionInstanceTypeMap = new HashMap<Class<?>, Class<?>>();

	public SelectPersistenceOperation()
	{
		super();
		init();
	}

	/**
	 * 初始化。
	 */
	protected void init()
	{
		collectionInstanceTypeMap.put(Collection.class, ArrayList.class);
		collectionInstanceTypeMap.put(AbstractCollection.class, ArrayList.class);

		collectionInstanceTypeMap.put(Deque.class, LinkedList.class);
		collectionInstanceTypeMap.put(BlockingDeque.class, LinkedBlockingDeque.class);

		collectionInstanceTypeMap.put(List.class, ArrayList.class);
		collectionInstanceTypeMap.put(AbstractList.class, ArrayList.class);
		collectionInstanceTypeMap.put(AbstractSequentialList.class, LinkedList.class);

		collectionInstanceTypeMap.put(Queue.class, LinkedList.class);
		collectionInstanceTypeMap.put(BlockingQueue.class, ArrayBlockingQueue.class);

		collectionInstanceTypeMap.put(Set.class, HashSet.class);
		collectionInstanceTypeMap.put(AbstractSet.class, HashSet.class);

		collectionInstanceTypeMap.put(NavigableSet.class, TreeSet.class);
		collectionInstanceTypeMap.put(SortedSet.class, TreeSet.class);
	}

	public SelectOptions getSelectOptions()
	{
		return selectOptions;
	}

	public void setSelectOptions(SelectOptions selectOptions)
	{
		this.selectOptions = selectOptions;
	}

	public Map<Class<?>, Class<?>> getCollectionInstanceTypeMap()
	{
		return collectionInstanceTypeMap;
	}

	public void setCollectionInstanceTypeMap(Map<Class<?>, Class<?>> collectionInstanceTypeMap)
	{
		this.collectionInstanceTypeMap.putAll(collectionInstanceTypeMap);
	}

	/**
	 * 判断实体对象是否存在。
	 * 
	 * @param model
	 * @param obj
	 * @return
	 * 
	 */
	public boolean isEntityExisting(Connection cn, Dialect dialect, Model model, Object obj)
	{
		String table = getTableName(model);
		String[] idColumnNamesQuote = toQuoteNames(dialect, getIdColumnNames(model));
		Object[] idColumnValues = getIdColumnValuesForObj(cn, model, obj);

		return isExisting(cn, dialect, table, model, idColumnNamesQuote, idColumnValues);
	}

	/**
	 * 判断实体对象是否存在。
	 * 
	 * @param model
	 * @param idPropertyValues
	 * @return
	 * 
	 */
	public boolean isEntityExisting(Connection cn, Dialect dialect, Model model, Object[] idPropertyValues)
	{
		String table = getTableName(model);
		String[] idColumnNamesQuote = toQuoteNames(dialect, getIdColumnNames(model));
		Object[] idColumnValues = getIdColumnValuesForId(cn, model, idPropertyValues);

		return isExisting(cn, dialect, table, model, idColumnNamesQuote, idColumnValues);
	}

	/**
	 * 判断对象是否存在。
	 * 
	 * @param table
	 * @param model
	 * @param columnNamesQuote
	 * @param columnValues
	 * @return
	 * 
	 */
	public boolean isExisting(Connection cn, Dialect dialect, String table, Model model, String[] columnNamesQuote,
			Object[] columnValues)
	{
		SqlBuilder sql = SqlBuilder.valueOf();
		sql.sql("SELECT COUNT(*) FROM ").sql(toQuoteName(dialect, table)).sql(" WHERE ").delimit(" AND ")
				.sqldSuffix(columnNamesQuote, "=?").arg(columnValues);

		long count = executeCountQuery(cn, sql);

		return (count > 0);
	}

	/**
	 * 根据ID获取。
	 *
	 * @param model
	 * @param id
	 * @return
	 */
	public Object getById(Connection cn, Dialect dialect, Model model, Object[] id)
	{
		String table = getTableName(model);

		SqlBuilder condition = buildIdCondition(cn, dialect, model, id);

		SqlBuilder query = buildQueryViewForModel(dialect, table, condition, null, model);

		List<Object> list = query(cn, query, model, 1, -1);

		return (list == null || list.isEmpty() ? null : list.get(0));
	}

	/**
	 * 根据参数获取对象。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param param
	 * @return
	 */
	public List<Object> getByParam(Connection cn, Dialect dialect, String table, Model model, Object param)
	{
		SqlBuilder condition = buildRecordCondition(cn, dialect, model, param, null);

		SqlBuilder query = buildQueryViewForModel(dialect, table, condition, null, model);

		List<Object> list = query(cn, query, model, 1, -1);

		return list;
	}

	/**
	 * 根据参数获取属性值。
	 * 
	 * @param cn
	 * @param model
	 * @param table
	 * @param param
	 * @param property
	 * @param propertyModel
	 * @return
	 */
	public List<Object> getPropValueByParam(Connection cn, Dialect dialect, String table, Model model, Object param,
			Property property, PropertyModel propertyModel)
	{
		SqlBuilder modelTableFieldCondition = buildRecordCondition(cn, dialect, model, param, null);

		RelationMapper relationMapper = getRelationMapper(model, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		SqlBuilder query = buildQueryViewForProperty(dialect, table, modelTableFieldCondition, null, model, property,
				propertyModelMapper, null);

		List<Object> list = queryPropValue(cn, dialect, table, query, model, property, propertyModelMapper, 1, -1);

		return list;
	}

	/**
	 * 根据参数获取多元属性值元素。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyModel
	 * @param propValueElementParam
	 * @return
	 */
	public List<Object> getMultiplePropValueElementByParam(Connection cn, Dialect dialect, String table, Model model,
			Object obj, Property property, PropertyModel propertyModel, Object propValueElementParam)
	{
		SqlBuilder modelTableFieldCondition = buildRecordCondition(cn, dialect, model, obj, null);

		RelationMapper relationMapper = getRelationMapper(model, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		SqlBuilder propertyTableFieldCondition = buildRecordCondition(cn, dialect, propertyModelMapper.getModel(),
				propValueElementParam, getMappedByWith(propertyModelMapper.getMapper()));

		SqlBuilder query = buildQueryViewForProperty(dialect, table, modelTableFieldCondition, null, model, property,
				propertyModelMapper, propertyTableFieldCondition);

		List<Object> list = queryPropValue(cn, dialect, table, query, model, property, propertyModelMapper, 1, -1);

		return list;
	}

	/**
	 * 分页查询。
	 * 
	 * @param table
	 * @param model
	 * @param pagingQuery
	 * @return
	 * 
	 */
	public PagingData<Object> pagingQuery(Connection cn, Dialect dialect, String table, Model model,
			PagingQuery pagingQuery)
	{
		List<ColumnPropertyPath> selectColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

		SqlBuilder queryView = buildQueryViewForModel(dialect, table, null, selectColumnPropertyPaths, model);

		SqlBuilder condition = buildQueryCondition(pagingQuery, selectColumnPropertyPaths);

		Order[] orders = buildQueryOrders(pagingQuery, selectColumnPropertyPaths);

		long total = queryCount(cn, queryView, condition);

		PagingData<Object> pagingData = new PagingData<Object>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		SqlBuilder query = null;

		int startRow = pagingData.getStartRow();
		int count = pagingData.getPageSize();

		// 数据库分页
		if (dialect.supportsPagingSql())
		{
			query = dialect.toPagingSql(queryView, condition, orders, startRow, count);

			if (query != null)
			{
				startRow = 1;
				count = -1;
			}
		}

		// 内存分页
		if (query == null)
			query = buildQuery(dialect, queryView, condition, orders);

		List<Object> list = query(cn, query, model, startRow, count);

		pagingData.setItems(list);

		return pagingData;
	}

	/**
	 * 查询属性。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyModel
	 * @param pagingQuery
	 * @param propertyModelQueryPattern
	 *            是否是属性模型级的查询方式：查询关键字、条件SQL、排序SQL仅包含属性模型级的属性路径，
	 *            具体参考{@linkplain #getQueryPropValueQueryResultMetaInfo(Dialect, String, Model, Property, PropertyModel, boolean)}
	 * @return
	 */
	public PagingData<Object> pagingQueryPropValue(Connection cn, Dialect dialect, String table, Model model,
			Object obj, Property property, PropertyModel propertyModel, PagingQuery pagingQuery,
			boolean propertyModelQueryPattern)
	{
		RelationMapper relationMapper = getRelationMapper(model, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		SqlBuilder modelTableFieldCondition = buildRecordCondition(cn, dialect, model, obj, null);
		List<ColumnPropertyPath> selectColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

		SqlBuilder queryView = buildQueryViewForProperty(dialect, table, modelTableFieldCondition,
				selectColumnPropertyPaths, model, property, propertyModelMapper, null);

		if (propertyModelQueryPattern)
			selectColumnPropertyPaths = toPropertyModelColumnPropertyPaths(selectColumnPropertyPaths, model, property,
					propertyModel);

		SqlBuilder condition = buildQueryCondition(pagingQuery, selectColumnPropertyPaths);

		Order[] orders = buildQueryOrders(pagingQuery, selectColumnPropertyPaths);

		long total = queryCount(cn, queryView, condition);

		PagingData<Object> pagingData = new PagingData<Object>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		SqlBuilder query = null;

		int startRow = pagingData.getStartRow();
		int count = pagingData.getPageSize();

		// 数据库分页
		if (dialect.supportsPagingSql())
		{
			query = dialect.toPagingSql(queryView, condition, orders, startRow, count);

			if (query != null)
			{
				startRow = 1;
				count = -1;
			}
		}

		// 内存分页
		if (query == null)
			query = buildQuery(dialect, queryView, condition, orders);

		List<Object> list = queryPropValue(cn, dialect, table, query, model, property, propertyModelMapper, startRow,
				count);

		pagingData.setItems(list);

		return pagingData;
	}

	/**
	 * 查询数量。
	 * 
	 * @param queryView
	 * @param condition
	 *            允许为{@code null}。
	 * @return
	 * 
	 */
	public long queryCount(Connection cn, SqlBuilder queryView, SqlBuilder condition)
	{
		SqlBuilder query = SqlBuilder.valueOf().sql("SELECT COUNT(*) FROM (").sql(queryView).sql(") A");

		if (!isEmptySqlBuilder(condition))
			query.sql(" WHERE ").sql(condition);

		long re = executeCountQuery(cn, query);

		return re;
	}

	/**
	 * 获取模型端最大排序值。
	 * 
	 * @param model
	 * @param obj
	 * @param idColumnValue
	 * @param property
	 * @param propertyModelMapper
	 * @param propValueEle
	 * @return
	 * 
	 */
	public long getMaxModelOrderForJoinTableMapper(Connection cn, Dialect dialect, Model model, Object obj,
			Object[] idColumnValue, Property property, PropertyModelMapper<JoinTableMapper> propertyModelMapper,
			Object propValueEle)
	{
		// TODO
		return 0;
	}

	/**
	 * 获取模型端最大排序值。
	 * 
	 * @param model
	 * @param obj
	 * @param property
	 * @param propertyModelMapper
	 * @param propValue
	 * @return
	 * 
	 */
	public long getMaxModelOrderForModelTableMapper(Connection cn, Dialect dialect, Model model, Object obj,
			Property property, PropertyModelMapper<ModelTableMapper> propertyModelMapper, Object propValue)
	{
		// TODO
		return 0;
	}

	/**
	 * 获取查询结果集{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @return
	 */
	public QueryResultMetaInfo getQueryResultMetaInfo(Dialect dialect, String table, Model model)
	{
		List<ColumnPropertyPath> selectColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

		TableAliasGenerator tableAliasGenerator = new SequentialTableAliasGenerator();

		String tableNameQuote = toQuoteName(dialect, table);
		String tableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

		appendModelQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, true, tableNameQuote,
				tableAliasQuote, null, null, tableAliasGenerator);
		// 查询模型数据使用“LEFT JOIN”，因为有属性值为null的情况。
		appendPropertyQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, true, tableAliasQuote,
				null, tableAliasGenerator, null, " LEFT JOIN ", true, this.selectOptions.getMaxQueryDepth());

		return new QueryResultMetaInfo(model, selectColumnPropertyPaths);
	}

	/**
	 * 获取属性查询结果集{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param dialect
	 * @param table
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @param propertyModelPattern
	 *            是否采用属性模型方式，如果为{@code true}，返回{@linkplain QueryResultMetaInfo#getColumnPropertyPaths()}列表中仅包含此属性模型的{@linkplain ColumnPropertyPath}，
	 *            且{@linkplain ColumnPropertyPath#getPropertyPath()}将被截取。
	 * @return
	 */
	public QueryResultMetaInfo getQueryPropValueQueryResultMetaInfo(Dialect dialect, String table, Model model,
			Property property, PropertyModel propertyModel, boolean propertyModelPattern)
	{
		RelationMapper relationMapper = getRelationMapper(model, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		List<ColumnPropertyPath> selectColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

		TableAliasGenerator tableAliasGenerator = new SequentialTableAliasGenerator();

		String tableNameQuote = toQuoteName(dialect, table);
		String tableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

		appendModelQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, false, tableNameQuote,
				tableAliasQuote, null, null, tableAliasGenerator);
		appendPropertyQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, false, tableAliasQuote,
				null, tableAliasGenerator, property.getName(), " LEFT JOIN ", true,
				this.selectOptions.getMaxQueryDepth());

		// 查询属性数据使用“INNER JOIN”，因为要排除属性值为null的情况。
		appendPropertyQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, true, tableAliasQuote,
				null, property, MU.getPropertyIndex(model, property), propertyModelMapper, tableAliasGenerator, null,
				" INNER JOIN ", false, true, false, this.selectOptions.getMaxQueryDepth());

		QueryResultMetaInfo queryResultMetaInfo = new QueryResultMetaInfo(model, selectColumnPropertyPaths);

		if (propertyModelPattern)
			queryResultMetaInfo = toPropertyModelQueryResultMetaInfo(queryResultMetaInfo, model, property,
					propertyModel);

		return queryResultMetaInfo;
	}

	/**
	 * 将模型对应的{@linkplain QueryResultMetaInfo}转换为属性模型对应的{@linkplain QueryResultMetaInfo}。
	 * 
	 * @param queryResultMetaInfo
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @return
	 */
	protected QueryResultMetaInfo toPropertyModelQueryResultMetaInfo(QueryResultMetaInfo queryResultMetaInfo,
			Model model, Property property, PropertyModel propertyModel)
	{
		List<ColumnPropertyPath> propertyModelColumnPropertyPaths = toPropertyModelColumnPropertyPaths(
				queryResultMetaInfo.getColumnPropertyPaths(), model, property, propertyModel);

		return new QueryResultMetaInfo(propertyModel.getModel(), propertyModelColumnPropertyPaths);
	}

	/**
	 * 将模型对应的{@linkplain ColumnPropertyPath}列表转换为属性模型对应的{@linkplain ColumnPropertyPath}列表。
	 * 
	 * @param modelColumnPropertyPaths
	 * @param model
	 * @param property
	 * @param propertyModel
	 * @return
	 */
	protected List<ColumnPropertyPath> toPropertyModelColumnPropertyPaths(
			List<ColumnPropertyPath> modelColumnPropertyPaths, Model model, Property property,
			PropertyModel propertyModel)
	{
		List<ColumnPropertyPath> propertyModelColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

		String myPropertyPathPrefix = getPropertyModelPropertyPath(model, property, propertyModel, null)
				+ PropertyPath.PROPERTY_STRING;

		for (ColumnPropertyPath columnPropertyPath : modelColumnPropertyPaths)
		{
			String propertyPath = columnPropertyPath.getPropertyPath();

			if (!propertyPath.startsWith(myPropertyPathPrefix))
				continue;

			propertyPath = propertyPath.substring(myPropertyPathPrefix.length());

			propertyModelColumnPropertyPaths.add(
					new ColumnPropertyPath(columnPropertyPath.getColumnName(), columnPropertyPath.getQuoteColumnName(),
							columnPropertyPath.isToken(), columnPropertyPath.isSizeColumn(), propertyPath));
		}

		return propertyModelColumnPropertyPaths;
	}

	/**
	 * 查询。
	 * 
	 * @param cn
	 * @param query
	 * @param model
	 * @param startRow
	 *            起始行号，以1开头
	 * @param count
	 *            读取记录数，如果{@code <0}，表示读取全部
	 * @return
	 */
	protected List<Object> query(Connection cn, SqlBuilder query, Model model, int startRow, int count)
	{
		List<Object> list = executeListQuery(cn, query, new ModelRowMapper(cn, model), startRow, count);
		return list;
	}

	/**
	 * 查询属性值。
	 * 
	 * @param cn
	 * @param dialect
	 * @param table
	 * @param query
	 * @param model
	 * @param property
	 * @param propertyModelMapper
	 * @param startRow
	 *            起始行号，以1开头
	 * @param count
	 *            读取记录数，如果{@code <0}，表示读取全部
	 * @return
	 */
	protected List<Object> queryPropValue(Connection cn, Dialect dialect, String table, SqlBuilder query, Model model,
			Property property, PropertyModelMapper<?> propertyModelMapper, int startRow, int count)
	{
		int propertyIndex = MU.getPropertyIndex(model, property);
		Model pmodel = propertyModelMapper.getModel();

		boolean canFetchPropertyColumnsOnly = false;

		if (MU.isPrimitiveModel(pmodel))
			canFetchPropertyColumnsOnly = true;
		else
		{
			String mappedByWith = getMappedByWith(propertyModelMapper.getMapper());

			canFetchPropertyColumnsOnly = (mappedByWith == null);
		}

		if (canFetchPropertyColumnsOnly)
		{
			TableAliasGenerator tableAliasGenerator = new SequentialTableAliasGenerator();

			String tableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

			List<ColumnPropertyPath> selectColumnPropertyPaths = new ArrayList<ColumnPropertyPath>();

			appendPropertyQueryView(dialect, model, null, null, selectColumnPropertyPaths, null, true, tableAliasQuote,
					null, property, MU.getPropertyIndex(model, property), propertyModelMapper, tableAliasGenerator,
					null, "", true, true, false, this.selectOptions.getMaxQueryDepth());

			SqlBuilder propValueQuery = SqlBuilder.valueOf().sql("SELECT ").delimit(",");

			for (ColumnPropertyPath columnPropertyPath : selectColumnPropertyPaths)
				propValueQuery.sqld(columnPropertyPath.getQuoteColumnName());

			propValueQuery.sql(" FROM (").sql(query).sql(") ").sql(tableAliasQuote);

			List<Object> list = executeListQuery(cn, propValueQuery,
					new PropertyRowMapper(cn, model, property, propertyIndex, propertyModelMapper), startRow, count);

			return list;
		}
		else
		{
			List<Object> propValueList = new ArrayList<Object>();

			List<Object> list = executeListQuery(cn, query, new ModelRowMapper(cn, model), startRow, count);

			if (list != null)
			{
				for (Object obj : list)
				{
					Object pv = property.get(obj);

					if (pv == null)
						;
					else if (pv instanceof Object[])
					{
						addArrayToList(propValueList, (Object[]) pv);
					}
					else if (pv instanceof Collection<?>)
					{
						propValueList.addAll((Collection<?>) pv);
					}
					else
						propValueList.add(pv);
				}
			}

			return propValueList;
		}
	}

	/**
	 * 构建查询语句。
	 * 
	 * @param dialect
	 * @param queryView
	 * @param condition
	 * @param orders
	 * @return
	 */
	protected SqlBuilder buildQuery(Dialect dialect, SqlBuilder queryView, SqlBuilder condition, Order[] orders)
	{
		SqlBuilder query;

		SqlBuilder orderSql = Order.toOrderSql(orders);

		if (isEmptySqlBuilder(condition) && isEmptySqlBuilder(orderSql))
			query = queryView;
		else
		{
			query = SqlBuilder.valueOf().sql("SELECT * FROM (").sql(queryView).sql(") T");

			if (!isEmptySqlBuilder(condition))
				query.sql(" WHERE ").sql(condition);

			if (!isEmptySqlBuilder(orderSql))
				query.sql(" ORDER BY ").sql(orderSql);

			return query;
		}

		return query;
	}

	/**
	 * 构建查询视图。
	 * 
	 * @param dialect
	 * @param table
	 * @param modelTableFieldCondition
	 *            模型表字段级查询条件，允许为{@code null}
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param model
	 * @return
	 */
	protected SqlBuilder buildQueryViewForModel(Dialect dialect, String table, SqlBuilder modelTableFieldCondition,
			List<ColumnPropertyPath> selectColumnPropertyPaths, Model model)
	{
		SqlBuilder selectSql = SqlBuilder.valueOf().sql("SELECT ").delimit(",");
		SqlBuilder fromSql = SqlBuilder.valueOf().sql(" FROM ");

		TableAliasGenerator tableAliasGenerator = new SequentialTableAliasGenerator();

		String tableNameQuote = toQuoteName(dialect, table);
		String tableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

		appendModelQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths, null, true, tableNameQuote,
				tableAliasQuote, modelTableFieldCondition, null, tableAliasGenerator);
		// 查询模型数据使用“LEFT JOIN”，因为有属性值为null的情况。
		appendPropertyQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths, null, true,
				tableAliasQuote, null, tableAliasGenerator, null, " LEFT JOIN ", true,
				this.selectOptions.getMaxQueryDepth());

		selectSql.sql(fromSql);

		return selectSql;
	}

	/**
	 * 构建查询属性数据的视图。
	 * 
	 * @param dialect
	 * @param table
	 * @param modelTableFieldCondition
	 *            模型表字段级查询条件，允许为{@code null}
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param model
	 * @param property
	 * @param propertyModelMapper
	 * @param propertyTableFieldCondition
	 *            属性表字段级查询条件，允许为{@code null}
	 * @return
	 */
	protected SqlBuilder buildQueryViewForProperty(Dialect dialect, String table, SqlBuilder modelTableFieldCondition,
			List<ColumnPropertyPath> selectColumnPropertyPaths, Model model, Property property,
			PropertyModelMapper<?> propertyModelMapper, SqlBuilder propertyTableFieldCondition)
	{
		SqlBuilder selectSql = SqlBuilder.valueOf().sql("SELECT ").delimit(",");
		SqlBuilder fromSql = SqlBuilder.valueOf().sql(" FROM ");

		TableAliasGenerator tableAliasGenerator = new SequentialTableAliasGenerator();

		String tableNameQuote = toQuoteName(dialect, table);
		String tableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

		appendModelQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths, null, false, tableNameQuote,
				tableAliasQuote, modelTableFieldCondition, null, tableAliasGenerator);
		appendPropertyQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths, null, false,
				tableAliasQuote, null, tableAliasGenerator, property.getName(), " LEFT JOIN ", true,
				this.selectOptions.getMaxQueryDepth());

		// 查询属性数据使用“INNER JOIN”，因为要排除属性值为null的情况。
		appendPropertyQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths, null, true,
				tableAliasQuote, null, property, MU.getPropertyIndex(model, property), propertyModelMapper,
				tableAliasGenerator, propertyTableFieldCondition, " INNER JOIN ", false, true, false,
				this.selectOptions.getMaxQueryDepth());

		selectSql.sql(fromSql);

		return selectSql;
	}

	/**
	 * 追加模型查询视图。
	 * <p>
	 * 此查询视图仅查询{@linkplain ModelTableMapper}的属性。
	 * </p>
	 * 
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asToken
	 *            是否作为Token。
	 * @param tableNameQuote
	 * @param tableAliasQuote
	 * @param tableFieldCondition
	 *            表字段级条件，允许为{@code null}
	 * @param columnAliasPrefix
	 *            允许为{@code null}，列别名前缀
	 * @param tableAliasGenerator
	 */
	protected void appendModelQueryView(Dialect dialect, Model model, SqlBuilder selectSql, SqlBuilder fromSql,
			List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath, boolean asToken,
			String tableNameQuote, String tableAliasQuote, SqlBuilder tableFieldCondition, String columnAliasPrefix,
			TableAliasGenerator tableAliasGenerator)
	{
		if (columnAliasPrefix == null)
			columnAliasPrefix = "";

		if (fromSql != null)
		{
			if (isEmptySqlBuilder(tableFieldCondition))
				fromSql.sql(tableNameQuote);
			else
				fromSql.sql("(SELECT * FROM ").sql(tableNameQuote).sql(" WHERE ").sql(tableFieldCondition).sql(")");

			fromSql.sql(" ").sql(tableAliasQuote);
		}

		Property[] properties = model.getProperties();
		for (int i = 0; i < properties.length; i++)
		{
			Property property = properties[i];

			if (property.hasFeature(NotReadable.class))
				continue;

			RelationMapper relationMapper = getRelationMapper(model, property);
			PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property, relationMapper);

			for (int j = 0; j < propertyModelMappers.length; j++)
			{
				PropertyModelMapper<?> pmm = propertyModelMappers[j];

				if (pmm.isModelTableMapperInfo())
				{
					PropertyModelMapper<ModelTableMapper> mpmm = pmm.castModelTableMapperInfo();

					appendPropertyQueryViewForModelTableMapper(dialect, model, selectSql, fromSql,
							selectColumnPropertyPaths, modelPropertyPath, isAsTokenProperty(asToken, model, property),
							tableAliasQuote, columnAliasPrefix, property, i, mpmm, tableAliasGenerator, null,
							" INNER JOIN ", true, false, 1);
				}
			}
		}
	}

	/**
	 * 追加属性查询视图。
	 * <p>
	 * 此查询视图仅查询{@linkplain OneToOne}或者{@linkplain ManyToOne}的属性，对于{@linkplain OneToMany}和{@linkplain ManyToMany}，则可选查询数目。
	 * </p>
	 * 
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asTokenModel
	 *            模型是否作为Token。
	 * @param modelTableAliasQuote
	 * @param modelColumnAliasPrefix
	 * @param tableAliasGenerator
	 * @param ignorePropertyName
	 *            允许为{@code null}
	 * @param joinTypeSql
	 * @param appendCountForMultipleProperty
	 *            对于多元属性，是否追加数目查询而非忽略
	 * @param queryDepth
	 */
	protected void appendPropertyQueryView(Dialect dialect, Model model, SqlBuilder selectSql, SqlBuilder fromSql,
			List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath, boolean asTokenModel,
			String modelTableAliasQuote, String modelColumnAliasPrefix, TableAliasGenerator tableAliasGenerator,
			String ignorePropertyName, String joinTypeSql, boolean appendCountForMultipleProperty, int queryDepth)
	{
		if (queryDepth < 0)
			return;

		if (modelColumnAliasPrefix == null)
			modelColumnAliasPrefix = "";

		Property[] properties = model.getProperties();
		for (int i = 0; i < properties.length; i++)
		{
			Property property = properties[i];

			if (property.hasFeature(NotReadable.class))
				continue;

			if (property.getName().equals(ignorePropertyName))
				continue;

			RelationMapper relationMapper = getRelationMapper(model, property);

			if (relationMapper.isOneToOne() || relationMapper.isManyToOne())
			{
				PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property, relationMapper);

				for (int j = 0; j < propertyModelMappers.length; j++)
				{
					PropertyModelMapper<?> propertyModelMapper = propertyModelMappers[j];

					appendPropertyQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths,
							modelPropertyPath, isAsTokenProperty(asTokenModel, model, property), modelTableAliasQuote,
							modelColumnAliasPrefix, property, i, propertyModelMapper, tableAliasGenerator, null,
							joinTypeSql, false, true, true, queryDepth);
				}
			}
			else if (relationMapper.isOneToMany() || relationMapper.isManyToMany())
			{
				if (appendCountForMultipleProperty)
				{
					PropertyModelMapper<?>[] propertyModelMappers = PropertyModelMapper.valueOf(property,
							relationMapper);

					for (int j = 0; j < propertyModelMappers.length; j++)
					{
						PropertyModelMapper<?> propertyModelMapper = propertyModelMappers[j];

						appendPropertyQueryView(dialect, model, selectSql, fromSql, selectColumnPropertyPaths,
								modelPropertyPath, isAsTokenProperty(asTokenModel, model, property),
								modelTableAliasQuote, modelColumnAliasPrefix, property, i, propertyModelMapper,
								tableAliasGenerator, null, joinTypeSql, false, true, true, queryDepth);
					}
				}
			}
			else
				throw new UnsupportedOperationException();
		}
	}

	/**
	 * 追加属性查询视图。
	 * 
	 * @param dialect
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asTokenProperty
	 *            属性是否作为Token。
	 * @param modelTableAliasQuote
	 * @param modelColumnAliasPrefix
	 * @param property
	 * @param propertyIndex
	 * @param propertyModelMapper
	 * @param tableAliasGenerator
	 * @param propertyTableFieldCondition
	 *            属性表字段级查询条件，允许为{@code null}
	 * @param joinTypeSql
	 * @param appendModelTableMapperPrimitiveValueProperty
	 * @param appendModelTableMapperEntityProperty
	 * @param onlyCountForMultipleProperty
	 *            对于多元属性，是否仅查询数目
	 * @param queryDepth
	 * @return
	 */
	protected boolean appendPropertyQueryView(Dialect dialect, Model model, SqlBuilder selectSql, SqlBuilder fromSql,
			List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath, boolean asTokenProperty,
			String modelTableAliasQuote, String modelColumnAliasPrefix, Property property, int propertyIndex,
			PropertyModelMapper<?> propertyModelMapper, TableAliasGenerator tableAliasGenerator,
			SqlBuilder propertyTableFieldCondition, String joinTypeSql,
			boolean appendModelTableMapperPrimitiveValueProperty, boolean appendModelTableMapperEntityProperty,
			boolean onlyCountForMultipleProperty, int queryDepth)
	{
		if (queryDepth < 0)
			return false;

		boolean appended = false;

		if (modelColumnAliasPrefix == null)
			modelColumnAliasPrefix = "";

		if (propertyModelMapper.isModelTableMapperInfo())
		{
			PropertyModelMapper<ModelTableMapper> mpmm = propertyModelMapper.castModelTableMapperInfo();

			return appendPropertyQueryViewForModelTableMapper(dialect, model, selectSql, fromSql,
					selectColumnPropertyPaths, modelPropertyPath, asTokenProperty, modelTableAliasQuote,
					modelColumnAliasPrefix, property, propertyIndex, mpmm, tableAliasGenerator,
					propertyTableFieldCondition, joinTypeSql, appendModelTableMapperPrimitiveValueProperty,
					appendModelTableMapperEntityProperty, queryDepth);
		}
		else if (propertyModelMapper.isPropertyTableMapperInfo())
		{
			PropertyModelMapper<PropertyTableMapper> ppmm = propertyModelMapper.castPropertyTableMapperInfo();

			appendPropertyQueryViewForPropertyTableMapper(dialect, model, selectSql, fromSql, selectColumnPropertyPaths,
					modelPropertyPath, asTokenProperty, modelTableAliasQuote, modelColumnAliasPrefix, property,
					propertyIndex, ppmm, tableAliasGenerator, propertyTableFieldCondition, joinTypeSql,
					onlyCountForMultipleProperty, queryDepth);

			appended = true;
		}
		else if (propertyModelMapper.isJoinTableMapperInfo())
		{
			PropertyModelMapper<JoinTableMapper> jpmm = propertyModelMapper.castJoinTableMapperInfo();

			appendPropertyQueryViewForJoinTableMapper(dialect, model, selectSql, fromSql, selectColumnPropertyPaths,
					modelPropertyPath, asTokenProperty, modelTableAliasQuote, modelColumnAliasPrefix, property,
					propertyIndex, jpmm, tableAliasGenerator, propertyTableFieldCondition, joinTypeSql,
					onlyCountForMultipleProperty, queryDepth);

			appended = true;
		}
		else
			throw new UnsupportedOperationException();

		return appended;
	}

	/**
	 * 追加属性查询视图。
	 * 
	 * @param dialect
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asTokenProperty
	 *            属性是否作为Token。
	 * @param modelTableAliasQuote
	 * @param modelColumnAliasPrefix
	 * @param property
	 * @param propertyIndex
	 * @param propertyModelMapper
	 * @param tableAliasGenerator
	 * @param propertyTableFieldCondition
	 *            属性表字段级查询条件，允许为{@code null}
	 * @param joinTypeSql
	 * @param appendModelTableMapperPrimitiveValueProperty
	 * @param appendModelTableMapperEntityProperty
	 * @param queryDepth
	 * @return
	 */
	protected boolean appendPropertyQueryViewForModelTableMapper(Dialect dialect, Model model, SqlBuilder selectSql,
			SqlBuilder fromSql, List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath,
			boolean asTokenProperty, String modelTableAliasQuote, String modelColumnAliasPrefix, Property property,
			int propertyIndex, PropertyModelMapper<ModelTableMapper> propertyModelMapper,
			TableAliasGenerator tableAliasGenerator, SqlBuilder propertyTableFieldCondition, String joinTypeSql,
			boolean appendModelTableMapperPrimitiveValueProperty, boolean appendModelTableMapperEntityProperty,
			int queryDepth)
	{
		if (queryDepth < 0)
			return false;

		boolean appended = false;

		ModelTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		if (mapper.isPrimitivePropertyMapper())
		{
			if (appendModelTableMapperPrimitiveValueProperty)
			{
				String columnNameQuote = toQuoteName(dialect, mapper.getPrimitiveColumnName());
				String columnAlias = getPropertyModelColumnAlias(modelColumnAliasPrefix, property, propertyIndex,
						propertyModelMapper, null);
				String columnAliasQuote = toQuoteName(dialect, columnAlias);

				if (selectSql != null)
					selectSql.sqld(modelTableAliasQuote + "." + columnNameQuote + " AS " + columnAliasQuote);

				if (selectColumnPropertyPaths != null)
				{
					ColumnPropertyPath columnPropertyPath = new ColumnPropertyPath(columnAlias, columnAliasQuote,
							asTokenProperty, false,
							getPropertyModelPropertyPath(model, property, propertyModelMapper, modelPropertyPath));
					selectColumnPropertyPaths.add(columnPropertyPath);
				}

				appended = true;
			}
		}
		else
		{
			String[] pkeyColumnNamesQuote = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

			if (appendModelTableMapperEntityProperty)
			{
				String ptableNameQuote = toQuoteName(dialect, getTableName(propertyModel));
				String[] ptableKeyColumnNamesQuote = toQuoteNames(dialect,
						getKeyColumnNames(propertyModel, getPropertyKeyProperties(mapper, propertyModel)));
				String ptableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

				String myColumnAliasPrefix = getPropertyModelColumnAlias(modelColumnAliasPrefix, property,
						propertyIndex, propertyModelMapper, PropertyPath.PROPERTY_STRING);

				String myModelPropertyPath = getPropertyModelPropertyPath(model, property, propertyModelMapper,
						modelPropertyPath);

				if (fromSql != null)
					fromSql.sql(joinTypeSql);

				appendModelQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
						myModelPropertyPath, asTokenProperty, ptableNameQuote, ptableAliasQuote,
						propertyTableFieldCondition, myColumnAliasPrefix, tableAliasGenerator);

				if (fromSql != null)
				{
					fromSql.sql(" ON ").delimit(" AND ");

					for (int i = 0; i < pkeyColumnNamesQuote.length; i++)
						fromSql.sqld(modelTableAliasQuote + "." + pkeyColumnNamesQuote[i] + "=" + ptableAliasQuote + "."
								+ ptableKeyColumnNamesQuote[i]);

					if (mapper.hasPropertyConcreteColumn())
						fromSql.sqld(modelTableAliasQuote + "."
								+ toQuoteName(dialect, mapper.getPropertyConcreteColumnName()) + "=?")
								.arg(mapper.getPropertyConcreteColumnValue());
				}

				// 属性的属性查询要使用LEFT JOIN，因为可能有NULL属性值
				appendPropertyQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
						myModelPropertyPath, asTokenProperty, ptableAliasQuote, myColumnAliasPrefix,
						tableAliasGenerator, getMappedByWith(mapper), " LEFT JOIN ", true, queryDepth - 1);

				appended = true;
			}
		}

		return appended;
	}

	/**
	 * 追加属性查询视图。
	 * 
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asTokenProperty
	 *            属性是否作为Token。
	 * @param modelTableAliasQuote
	 * @param modelColumnAliasPrefix
	 * @param property
	 * @param propertyIndex
	 * @param propertyModelMapper
	 * @param tableAliasGenerator
	 * @param propertyTableFieldCondition
	 *            属性表字段级查询条件，允许为{@code null}
	 * @param joinTypeSql
	 * @param onlyCountForMultipleProperty
	 * @param queryDepth
	 */
	protected void appendPropertyQueryViewForPropertyTableMapper(Dialect dialect, Model model, SqlBuilder selectSql,
			SqlBuilder fromSql, List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath,
			boolean asTokenProperty, String modelTableAliasQuote, String modelColumnAliasPrefix, Property property,
			int propertyIndex, PropertyModelMapper<PropertyTableMapper> propertyModelMapper,
			TableAliasGenerator tableAliasGenerator, SqlBuilder propertyTableFieldCondition, String joinTypeSql,
			boolean onlyCountForMultipleProperty, int queryDepth)
	{
		if (queryDepth < 0)
			return;

		RelationMapper relationMapper = propertyModelMapper.getRelationMapper();
		PropertyTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		String[] mtableKeyColumnNamesQuote = toQuoteNames(dialect,
				getKeyColumnNames(model, getModelKeyProperties(mapper, model)));
		String[] ptableMkeyColumnNamesQuote = toQuoteNames(dialect, mapper.getModelKeyColumnNames());

		String ptableNameQuote = toQuoteName(dialect,
				mapper.isPrimitivePropertyMapper() ? mapper.getPrimitiveTableName() : getTableName(propertyModel));
		String ptableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());
		String myColumnAliasPrefix = getPropertyModelColumnAlias(modelColumnAliasPrefix, property, propertyIndex,
				propertyModelMapper, PropertyPath.PROPERTY_STRING);

		String myModelPropertyPath = getPropertyModelPropertyPath(model, property, propertyModelMapper,
				modelPropertyPath);

		boolean isMultipleProperty = (relationMapper.isOneToMany() || relationMapper.isManyToMany());
		boolean onlyCount = (isMultipleProperty && onlyCountForMultipleProperty);

		if (fromSql != null)
			fromSql.sql(joinTypeSql);

		if (onlyCount)
		{
			String sizeAlias = myColumnAliasPrefix + SizeOnlyCollection.SIZE_PROPERTY_NAME;
			String sizeQuoteAlias = toQuoteName(dialect, sizeAlias);

			if (selectSql != null)
				selectSql.sqld("(CASE WHEN " + ptableAliasQuote + "." + sizeQuoteAlias + " IS NOT NULL THEN "
						+ ptableAliasQuote + "." + sizeQuoteAlias + " ELSE 0 END) AS " + sizeQuoteAlias);

			if (selectColumnPropertyPaths != null)
			{
				ColumnPropertyPath columnPropertyPath = new ColumnPropertyPath(sizeAlias, sizeQuoteAlias,
						asTokenProperty, true,
						PropertyPath.concatPropertyName(myModelPropertyPath, SizeOnlyCollection.SIZE_PROPERTY_NAME));
				selectColumnPropertyPaths.add(columnPropertyPath);
			}

			if (fromSql != null)
			{
				fromSql.sql(" (SELECT ").delimit(", ");

				fromSql.sqld(" COUNT(*) AS " + sizeQuoteAlias);

				for (int i = 0; i < ptableMkeyColumnNamesQuote.length; i++)
					fromSql.sqld(ptableMkeyColumnNamesQuote[i]);

				fromSql.sql(" FROM ").sql(ptableNameQuote);

				fromSql.sql(" GROUP BY ").delimit(", ");

				for (int i = 0; i < ptableMkeyColumnNamesQuote.length; i++)
					fromSql.sqld(ptableMkeyColumnNamesQuote[i]);

				fromSql.sql(") ").sql(ptableAliasQuote);
			}
		}
		else
		{
			appendModelQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
					myModelPropertyPath, asTokenProperty, ptableNameQuote, ptableAliasQuote,
					propertyTableFieldCondition, myColumnAliasPrefix, tableAliasGenerator);
		}

		if (fromSql != null)
		{
			fromSql.sql(" ON ").delimit(" AND ");

			for (int i = 0; i < ptableMkeyColumnNamesQuote.length; i++)
				fromSql.sqld(modelTableAliasQuote + "." + mtableKeyColumnNamesQuote[i] + "=" + ptableAliasQuote + "."
						+ ptableMkeyColumnNamesQuote[i]);

			if (mapper.hasModelConcreteColumn())
				fromSql.sqld(ptableAliasQuote + "." + toQuoteName(dialect, mapper.getModelConcreteColumnName()) + "=?")
						.arg(mapper.getModelConcreteColumnValue());
		}

		if (!onlyCount)
		{
			// 属性的属性查询要使用LEFT JOIN，因为可能有NULL属性值
			appendPropertyQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
					myModelPropertyPath, asTokenProperty, ptableAliasQuote, myColumnAliasPrefix, tableAliasGenerator,
					getMappedByWith(mapper), " LEFT JOIN ", true, queryDepth - 1);
		}
	}

	/**
	 * 追加属性查询视图。
	 * 
	 * @param model
	 * @param selectSql
	 *            允许为{@code null}，如果为{@code null}，则不写入SELECT语句
	 * @param fromSql
	 *            允许为{@code null}，如果为{@code null}，则不写入FROM语句
	 * @param selectColumnPropertyPaths
	 *            允许为{@code null}，写入SELECT结果集列别名信息的列表
	 * @param modelPropertyPath
	 *            允许为{@code null}，模型所处的上下文属性路径
	 * @param asTokenProperty
	 *            属性是否作为Token。
	 * @param modelTableAliasQuote
	 * @param modelColumnAliasPrefix
	 * @param property
	 * @param propertyIndex
	 * @param propertyModelMapper
	 * @param tableAliasGenerator
	 * @param propertyTableFieldCondition
	 *            属性表字段级查询条件，允许为{@code null}
	 * @param joinTypeSql
	 * @param onlyCountForMultipleProperty
	 * @param queryDepth
	 */
	protected void appendPropertyQueryViewForJoinTableMapper(Dialect dialect, Model model, SqlBuilder selectSql,
			SqlBuilder fromSql, List<ColumnPropertyPath> selectColumnPropertyPaths, String modelPropertyPath,
			boolean asTokenProperty, String modelTableAliasQuote, String modelColumnAliasPrefix, Property property,
			int propertyIndex, PropertyModelMapper<JoinTableMapper> propertyModelMapper,
			TableAliasGenerator tableAliasGenerator, SqlBuilder propertyTableFieldCondition, String joinTypeSql,
			boolean onlyCountForMultipleProperty, int queryDepth)
	{
		if (queryDepth < 0)
			return;

		RelationMapper relationMapper = propertyModelMapper.getRelationMapper();
		JoinTableMapper mapper = propertyModelMapper.getMapper();
		Model propertyModel = propertyModelMapper.getModel();

		String[] mkeyColumnNamesQuote = toQuoteNames(dialect, mapper.getModelKeyColumnNames());
		String[] pkeyColumnNamesQuote = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());

		String jointableNameQuote = toQuoteName(dialect, mapper.getJoinTableName());
		String jointableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

		String myColumnAliasPrefix = getPropertyModelColumnAlias(modelColumnAliasPrefix, property, propertyIndex,
				propertyModelMapper, PropertyPath.PROPERTY_STRING);

		String myModelPropertyPath = getPropertyModelPropertyPath(model, property, propertyModelMapper,
				modelPropertyPath);

		boolean isMultipleProperty = (relationMapper.isOneToMany() || relationMapper.isManyToMany());
		boolean onlyCount = (isMultipleProperty && onlyCountForMultipleProperty);

		if (fromSql != null)
			fromSql.sql(joinTypeSql);

		if (onlyCount)
		{
			String sizeAlias = myColumnAliasPrefix + SizeOnlyCollection.SIZE_PROPERTY_NAME;
			String sizeQuoteAlias = toQuoteName(dialect, sizeAlias);

			if (selectSql != null)
				selectSql.sqld("(CASE WHEN " + jointableAliasQuote + "." + sizeQuoteAlias + " IS NOT NULL THEN "
						+ jointableAliasQuote + "." + sizeQuoteAlias + " ELSE 0 END) AS " + sizeQuoteAlias);

			if (selectColumnPropertyPaths != null)
			{
				ColumnPropertyPath columnPropertyPath = new ColumnPropertyPath(sizeAlias, sizeQuoteAlias,
						asTokenProperty, true,
						PropertyPath.concatPropertyName(myModelPropertyPath, SizeOnlyCollection.SIZE_PROPERTY_NAME));
				selectColumnPropertyPaths.add(columnPropertyPath);
			}

			if (fromSql != null)
			{
				fromSql.sql(" (SELECT ").delimit(", ");

				fromSql.sqld(" COUNT(*) AS " + sizeQuoteAlias);

				for (int i = 0; i < mkeyColumnNamesQuote.length; i++)
					fromSql.sqld(mkeyColumnNamesQuote[i]);

				fromSql.sql(" FROM ").sql(jointableNameQuote);

				fromSql.sql(" GROUP BY ").delimit(", ");

				for (int i = 0; i < mkeyColumnNamesQuote.length; i++)
					fromSql.sqld(mkeyColumnNamesQuote[i]);

				fromSql.sql(") ").sql(jointableAliasQuote);
			}
		}
		else
		{
			if (fromSql != null)
				fromSql.sql(jointableNameQuote).sql(" ").sql(jointableAliasQuote);
		}

		if (fromSql != null)
			fromSql.sql(" ON ").delimit(" AND ");

		if (fromSql != null)
		{
			String[] mtableMKeyColumnNamesQuote = toQuoteNames(dialect,
					getKeyColumnNames(model, getModelKeyProperties(mapper, model)));
			String[] jtableMKeyColumnNamesQuote = toQuoteNames(dialect, mapper.getModelKeyColumnNames());

			for (int i = 0; i < mkeyColumnNamesQuote.length; i++)
				fromSql.sqld(modelTableAliasQuote + "." + mtableMKeyColumnNamesQuote[i] + "=" + jointableAliasQuote
						+ "." + jtableMKeyColumnNamesQuote[i]);

			if (mapper.hasModelConcreteColumn())
				fromSql.sqld(
						jointableAliasQuote + "." + toQuoteName(dialect, mapper.getModelConcreteColumnName()) + "=?")
						.arg(mapper.getModelConcreteColumnValue());
		}

		if (!onlyCount)
		{
			String ptableNameQuote = toQuoteName(dialect, getTableName(propertyModel));
			String ptableAliasQuote = toQuoteName(dialect, tableAliasGenerator.next());

			if (fromSql != null)
				fromSql.sql(joinTypeSql);

			appendModelQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
					myModelPropertyPath, asTokenProperty, ptableNameQuote, ptableAliasQuote,
					propertyTableFieldCondition, myColumnAliasPrefix, tableAliasGenerator);

			if (fromSql != null)
			{
				fromSql.sql(" ON ").delimit(" AND ");

				String[] jtablePKeyColumnNamesQuote = toQuoteNames(dialect, mapper.getPropertyKeyColumnNames());
				String[] ptablePKeyColumnNamesQuote = toQuoteNames(dialect,
						getKeyColumnNames(propertyModel, getPropertyKeyProperties(mapper, propertyModel)));

				for (int i = 0; i < pkeyColumnNamesQuote.length; i++)
					fromSql.sqld(jointableAliasQuote + "." + jtablePKeyColumnNamesQuote[i] + "=" + ptableAliasQuote
							+ "." + ptablePKeyColumnNamesQuote[i]);

				if (mapper.hasPropertyConcreteColumn())
					fromSql.sqld(jointableAliasQuote + "."
							+ toQuoteName(dialect, mapper.getPropertyConcreteColumnName()) + "=?")
							.arg(mapper.getPropertyConcreteColumnValue());
			}

			// 属性的属性查询要使用LEFT JOIN，因为可能有NULL属性值
			appendPropertyQueryView(dialect, propertyModel, selectSql, fromSql, selectColumnPropertyPaths,
					myModelPropertyPath, asTokenProperty, ptableAliasQuote, myColumnAliasPrefix, tableAliasGenerator,
					getMappedByWith(mapper), " LEFT JOIN ", true, queryDepth - 1);
		}
	}

	/**
	 * 获取属性模型的列别名。
	 * <p>
	 * 如果属性是具体属性，此方法返回“propertyIndex”格式的别名；
	 * </p>
	 * <p>
	 * 如果是抽象属性，此方法返回“propertyIndex&lt;property-concrete-model-index&gt;”格式的别名。
	 * </p>
	 * <p>
	 * 如果采用属性名作为别名，对于嵌套层级多的属性，会导致别名超过某些数据库的别名长度限制（比如Oracle仅允许30个字符长度）而引起异常，所以这里采用属性索引作为别名。
	 * </p>
	 * 
	 * @param prefix
	 * @param property
	 * @param propertyIndex
	 * @param pmm
	 * @param suffix
	 * @return
	 */
	protected String getPropertyModelColumnAlias(String prefix, Property property, int propertyIndex, PropertyModel pmm,
			String suffix)
	{
		String propertyIndexStr = Integer.toString(propertyIndex);

		String alias = (MU.isAbstractedProperty(property)
				? propertyIndexStr + PropertyPath.CONCRETE_L + pmm.getIndex() + PropertyPath.CONCRETE_L
				: propertyIndexStr);

		if (prefix != null)
			alias = prefix + alias;

		if (suffix != null)
			alias = alias + suffix;

		return alias;
	}

	/**
	 * 获取属性模型的属性路径。
	 * 
	 * @param model
	 * @param property
	 * @param pmm
	 * @param prefixPropertyPath
	 *            前置属性路径，允许为{@code null}
	 * @return
	 */
	protected String getPropertyModelPropertyPath(Model model, Property property, PropertyModel pmm,
			String prefixPropertyPath)
	{
		String propertyName = property.getName();

		String myPropertyPath = (MU.isAbstractedProperty(property)
				? PropertyPath.concatPropertyName(prefixPropertyPath, propertyName, pmm.getIndex())
				: PropertyPath.concatPropertyName(prefixPropertyPath, propertyName));

		return myPropertyPath;
	}

	/**
	 * 判断给定属性是否作为{@linkplain Token}属性。
	 * 
	 * @param isModelToken
	 * @param model
	 * @param property
	 * @return
	 */
	protected boolean isAsTokenProperty(boolean isModelToken, Model model, Property property)
	{
		if (!isModelToken)
			return false;

		return (property.hasFeature(Token.class));
	}

	protected boolean isEmptySqlBuilder(SqlBuilder sqlBuilder)
	{
		return sqlBuilder == null || sqlBuilder.isEmpty();
	}

	/**
	 * 将给定字符串中的{@linkplain ColumnPropertyPath#getPropertyPath()}替换为{@linkplain ColumnPropertyPath#getColumnName()}。
	 * 
	 * @param columnPropertyPaths
	 * @param str
	 * @return
	 */
	protected String replacePropertyPathToQuoteColumnName(List<ColumnPropertyPath> columnPropertyPaths, String str)
	{
		if (str == null || str.isEmpty())
			return str;

		List<ColumnPropertyPath> myColumnPropertyPaths = new ArrayList<ColumnPropertyPath>(columnPropertyPaths);

		// 优先替换更长的属性路径，避免长路径名里包含短路径名时导致替换错乱
		Collections.sort(myColumnPropertyPaths, new Comparator<ColumnPropertyPath>()
		{
			@Override
			public int compare(ColumnPropertyPath o1, ColumnPropertyPath o2)
			{
				String o1PropertyPath = o1.getPropertyPath();
				String o2PropertyPath = o2.getPropertyPath();

				if (o1PropertyPath.length() > o2PropertyPath.length())
					return -1;
				else
					return 1;
			}
		});

		for (ColumnPropertyPath columnPropertyPath : myColumnPropertyPaths)
		{
			String pp = columnPropertyPath.getPropertyPath();
			String qcn = columnPropertyPath.getQuoteColumnName();

			str = Pattern.compile(pp, Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher(str)
					.replaceAll(Matcher.quoteReplacement(qcn));
		}

		return str;
	}

	/**
	 * 由{@linkplain Query}构建查询条件SQL。
	 * 
	 * @param query
	 * @param selectColumnPropertyPaths
	 * @return
	 */
	protected SqlBuilder buildQueryCondition(Query query, List<ColumnPropertyPath> selectColumnPropertyPaths)
	{
		if (!query.hasKeyword() && !query.hasCondition())
			return null;

		String conditionStr = (query.hasCondition() ? query.getCondition().trim() : null);
		boolean hasCondition = !(conditionStr == null || conditionStr.isEmpty());

		SqlBuilder queryCondition = SqlBuilder.valueOf();

		boolean hasKeywordCondition = false;

		if (query.hasKeyword())
		{
			SqlBuilder keywordCondition = SqlBuilder.valueOf();

			String keyword = wrapLikeKeyword(query.getKeyword());

			String andSql = (query.isNotLike() ? " AND " : " OR ");
			String likeSql = (query.isNotLike() ? " NOT LIKE " : " LIKE ");

			if (hasCondition)
				keywordCondition.sql("(");

			int appendCount = 0;
			for (ColumnPropertyPath columnPropertyPath : selectColumnPropertyPaths)
			{
				if (!columnPropertyPath.isToken())
					continue;

				if (appendCount > 0)
					keywordCondition.sql(andSql);

				keywordCondition.sql(columnPropertyPath.getQuoteColumnName() + likeSql + "?", keyword);

				appendCount++;
			}

			if (hasCondition)
				keywordCondition.sql(")");

			if (appendCount > 0)
			{
				hasKeywordCondition = true;
				queryCondition.sql(keywordCondition);
			}
		}

		if (hasCondition)
		{
			conditionStr = replacePropertyPathToQuoteColumnName(selectColumnPropertyPaths, conditionStr);

			if (hasKeywordCondition)
				queryCondition.sql(" AND (");

			queryCondition.sql(conditionStr);

			if (hasKeywordCondition)
				queryCondition.sql(")");
		}

		return queryCondition;
	}

	/**
	 * 由{@linkplain Query}构建{@linkplain Order}数组。
	 * 
	 * @param query
	 * @param selectColumnPropertyPaths
	 * @return
	 */
	protected Order[] buildQueryOrders(Query query, List<ColumnPropertyPath> selectColumnPropertyPaths)
	{
		if (!query.hasOrder())
			return null;

		Order[] orders = query.getOrders();

		Order[] re = new Order[orders.length];

		for (int i = 0; i < orders.length; i++)
		{
			Order order = orders[i];

			String orderName = order.getName();
			String orderType = order.getType();

			for (ColumnPropertyPath columnPropertyPath : selectColumnPropertyPaths)
			{
				String propertyPath = columnPropertyPath.getPropertyPath();

				if (propertyPath.startsWith(orderName) && (propertyPath.length() == orderName.length()
						|| columnPropertyPath.isSizeColumn() || columnPropertyPath.isToken()))
				{
					orderName = columnPropertyPath.getQuoteColumnName();
				}
			}

			re[i] = Order.valueOf(orderName, orderType);
		}

		return re;
	}

	/**
	 * 包裹Like关键字。
	 * 
	 * @param keyword
	 * @return
	 */
	protected String wrapLikeKeyword(String keyword)
	{
		if (keyword == null || keyword.isEmpty())
			return keyword;

		char first = keyword.charAt(0), last = keyword.charAt(keyword.length() - 1);

		if (first != '%' && first != '_' && last != '%' && last != '_')
			return "%" + keyword + "%";

		return keyword;
	}

	/**
	 * 创建集合类实例。
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected <T extends Collection> T createCollectionInstance(Class<T> type) throws Exception
	{
		int mod = type.getModifiers();

		if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod))
		{
			T re = type.newInstance();
			return re;
		}

		@SuppressWarnings({ "unchecked" })
		Class<? extends T> targetClass = (Class<? extends T>) this.collectionInstanceTypeMap.get(type);

		return createCollectionInstance(targetClass);
	}

	protected abstract class AbstractRowMapper
	{
		private Connection connection;

		public AbstractRowMapper()
		{
			super();
		}

		public AbstractRowMapper(Connection connection)
		{
			super();
			this.connection = connection;
		}

		public Connection getConnection()
		{
			return connection;
		}

		public void setConnection(Connection connection)
		{
			this.connection = connection;
		}

		/**
		 * 将结果集转换为对象。
		 * 
		 * @param propertyColIndexMap
		 * @param rs
		 * @param row
		 * @param model
		 * @param nullIfAllColumnValuesNull
		 *            当所有列值都为{@code null}时，是否返回{@code null}。
		 * @return
		 * @throws Exception
		 */
		protected Object convert(Map<PropertyPath, Object> propertyColIndexMap, ResultSet rs, int row, Model model,
				boolean nullIfAllColumnValuesNull) throws Exception
		{
			boolean allColumnValuesNull = false;

			if (propertyColIndexMap == null || propertyColIndexMap.isEmpty())
				allColumnValuesNull = true;

			if (allColumnValuesNull && nullIfAllColumnValuesNull)
				return null;

			Object obj = model.newInstance();

			if (allColumnValuesNull)
				return obj;

			Map<Property, Object> propValues = new HashMap<Property, Object>();

			Set<PropertyPath> keys = propertyColIndexMap.keySet();

			Property[] properties = model.getProperties();

			allColumnValuesNull = true;

			for (PropertyPath key : keys)
			{
				if (!key.isPropertyHead())
					continue;

				int propertyIndex = toPropertyIndex(key.getPropertyNameHead());

				// 忽略无关的列
				if (propertyIndex < 0 || propertyIndex >= properties.length)
					continue;

				Property property = properties[propertyIndex];

				int myModelIndex = 0;

				if (key.hasPropertyModelIndexHead())
					myModelIndex = key.getPropertyModelIndexHead();

				PropertyModel propertyModel = PropertyModel.valueOf(property, myModelIndex);

				Object value = propertyColIndexMap.get(key);

				Object propValue = null;

				if (value instanceof Integer)
				{
					int colIndex = (Integer) value;

					propValue = toPropertyValue(this.connection, rs, row, colIndex, model, property, propertyModel);

					if (allColumnValuesNull && !rs.wasNull())
						allColumnValuesNull = false;
				}
				else if (value instanceof Map<?, ?>)
				{
					@SuppressWarnings("unchecked")
					Map<PropertyPath, Object> propPropColIndexMap = (Map<PropertyPath, Object>) value;

					if (MU.isMultipleProperty(property))
					{
						Integer sizeColIndex = (Integer) SizeOnlyCollection
								.getSizeValueForPropertyPathMap(propPropColIndexMap);

						// 仅查询集合属性值数目
						if (sizeColIndex != null && propPropColIndexMap.size() == 1)
						{
							int size = rs.getInt(sizeColIndex);

							// 当需要返回null时，此属性值应该也为null
							boolean setToNull = ((size == 0 || rs.wasNull()) && nullIfAllColumnValuesNull);

							if (!setToNull)
							{
								SizeOnlyCollection<Object> sizeOnlyCollection = SizeOnlyCollection
										.instance(property.getCollectionType());

								sizeOnlyCollection.setSize(size);

								propValue = sizeOnlyCollection;
							}
						}
						else
						{
							// INNER JOIN集合属性值元素，处理查询集合属性值元素映射
							propValue = convertToPropertyValue(propPropColIndexMap, rs, row, model, property,
									propertyModel, obj);

							if (propValue != null)
							{
								if (property.isArray())
								{
									Object[] array = (Object[]) Array.newInstance(propertyModel.getModel().getType(),
											1);
									array[0] = propValue;

									propValue = array;
								}
								else if (property.isCollection())
								{
									@SuppressWarnings("unchecked")
									Collection<Object> collection = createCollectionInstance(
											property.getCollectionType());

									collection.add(propValue);

									propValue = collection;
								}
								else
									throw new UnsupportedOperationException();
							}
						}
					}
					else
					{
						propValue = convertToPropertyValue(propPropColIndexMap, rs, row, model, property, propertyModel,
								obj);
					}

					if (allColumnValuesNull && propValue != null)
						allColumnValuesNull = false;
				}
				else
					throw new UnsupportedOperationException();

				// 抽象属性的化，仅添加不为null的属性值
				if (propValue != null)
					propValues.put(property, propValue);
				else
				{
					if (!propValues.containsKey(property))
						propValues.put(property, null);
				}
			}

			if (nullIfAllColumnValuesNull && (allColumnValuesNull || propValues.isEmpty()))
			{
				return null;
			}
			else
			{
				for (Map.Entry<Property, Object> entry : propValues.entrySet())
					entry.getKey().set(obj, entry.getValue());

				if (obj instanceof DynamicBean)
					((DynamicBean) obj).setModel(model);

				return obj;
			}
		}

		/**
		 * 将结果集转换为属性值。
		 * 
		 * @param propertyPropertyColIndexMap
		 * @param rs
		 * @param row
		 * @param model
		 * @param property
		 * @param propertyModel
		 * @param obj
		 * @return
		 */
		protected Object convertToPropertyValue(Map<PropertyPath, Object> propertyPropertyColIndexMap, ResultSet rs,
				int row, Model model, Property property, PropertyModel propertyModel, Object obj) throws Exception
		{
			Object propValue = convert(propertyPropertyColIndexMap, rs, row, propertyModel.getModel(), true);

			PMU.setPropertyValueMappedByIf(model, obj, propertyModel, propValue);

			return propValue;
		}

		/**
		 * 从结果集列名提取[属性名路径->列号]映射表。
		 * <p>
		 * 返回映射表的键值仅有两种类型：1、结果集列号；2、[属性名路径->列号]映射表，如下所示：
		 * </p>
		 * 
		 * <pre>
		 * id                   -> 1
		 * name                 -> 2
		 * address    -> 
		 *               city   -> 3
		 *               street -> 4
		 * product<0> -> 
		 *               id     -> 5
		 *               name   -> 6
		 *               price  -> 7
		 * </pre>
		 * 
		 * @param rs
		 * @param row
		 * @param deletedColumnNamePrefix
		 *            要删除的列名称前缀，允许为{@code null}
		 * @return
		 * 
		 */
		protected Map<PropertyPath, Object> extractPropertyColIndexMap(ResultSet rs, int row,
				String deletedColumnNamePrefix)
		{
			ResultSetMetaData rsMeta = null;
			int columnCount = -1;

			try
			{
				rsMeta = rs.getMetaData();
				columnCount = rsMeta.getColumnCount();
			}
			catch (SQLException e)
			{
				throw new PersistenceException(e);
			}

			Map<PropertyPath, Object> map = new HashMap<PropertyPath, Object>();

			for (int i = 1; i <= columnCount; i++)
			{
				String colName = lookupColumnName(rsMeta, i);

				if (deletedColumnNamePrefix != null && !deletedColumnNamePrefix.isEmpty())
				{
					if (colName.startsWith(deletedColumnNamePrefix))
						colName = colName.substring(deletedColumnNamePrefix.length());
				}

				PropertyPath propPath = PropertyPath.valueOf(colName);

				Map<PropertyPath, Object> parent = map;

				for (int j = 0, len = propPath.length(); j < len; j++)
				{
					PropertyPath myPropertyPath = propPath.sub(j);

					if (j == len - 1)
						parent.put(myPropertyPath, i);
					else
					{
						@SuppressWarnings("unchecked")
						Map<PropertyPath, Object> myMap = (Map<PropertyPath, Object>) parent.get(myPropertyPath);

						if (myMap == null)
						{
							myMap = new HashMap<PropertyPath, Object>();
							parent.put(myPropertyPath, myMap);
						}

						parent = myMap;
					}
				}
			}

			return map;
		}

		/**
		 * 将{@code propertyIndex}字符串转换为属性索引。
		 * 
		 * @param propertyIndex
		 * @return
		 */
		protected int toPropertyIndex(String propertyIndex)
		{
			if (propertyIndex == null || propertyIndex.isEmpty())
				return -1;

			char[] cs = propertyIndex.toCharArray();
			for (char c : cs)
			{
				if (c < '0' || c > '9')
					return -1;
			}

			return Integer.parseInt(propertyIndex);
		}
	}

	/**
	 * 模型行映射器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class ModelRowMapper extends AbstractRowMapper implements RowMapper<Object>
	{
		private Model model;

		public ModelRowMapper()
		{
			super();
		}

		public ModelRowMapper(Connection connection, Model model)
		{
			super(connection);
			this.model = model;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		@Override
		public Object mapRow(ResultSet rs, int row)
		{
			Map<PropertyPath, Object> propertyColIndexMap = extractPropertyColIndexMap(rs, row, null);

			try
			{
				return convert(propertyColIndexMap, rs, row, this.model, false);
			}
			catch (Exception e)
			{
				throw new PersistenceException(e);
			}
		}
	}

	/**
	 * 属性行映射器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class PropertyRowMapper extends AbstractRowMapper implements RowMapper<Object>
	{
		private Model model;

		private Property property;

		private int propertyIndex;

		private PropertyModel propertyModel;

		public PropertyRowMapper(Connection connection, Model model, Property property, int propertyIndex,
				PropertyModel propertyModel)
		{
			super(connection);
			this.model = model;
			this.property = property;
			this.propertyIndex = propertyIndex;
			this.propertyModel = propertyModel;
		}

		public Model getModel()
		{
			return model;
		}

		public void setModel(Model model)
		{
			this.model = model;
		}

		public Property getProperty()
		{
			return property;
		}

		public void setProperty(Property property)
		{
			this.property = property;
		}

		public int getPropertyIndex()
		{
			return propertyIndex;
		}

		public void setPropertyIndex(int propertyIndex)
		{
			this.propertyIndex = propertyIndex;
		}

		public PropertyModel getPropertyModel()
		{
			return propertyModel;
		}

		public void setPropertyModel(PropertyModel propertyModel)
		{
			this.propertyModel = propertyModel;
		}

		@Override
		public Object mapRow(ResultSet rs, int row)
		{
			Model pmodel = this.propertyModel.getModel();

			if (MU.isPrimitiveModel(pmodel))
			{
				return toPropertyValue(getConnection(), rs, row, 1, this.model, this.property, this.propertyModel);
			}
			else
			{
				String deletedColumnNamePrefix = getPropertyModelColumnAlias("", this.property, this.propertyIndex,
						this.propertyModel, ".");

				Map<PropertyPath, Object> propertyColIndexMap = extractPropertyColIndexMap(rs, row,
						deletedColumnNamePrefix);

				try
				{
					return convert(propertyColIndexMap, rs, row, pmodel, false);
				}
				catch (Exception e)
				{
					throw new PersistenceException(e);
				}
			}
		}
	}

	/**
	 * 表别名生成器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static interface TableAliasGenerator
	{
		/**
		 * 生成下一个别名。
		 * 
		 * @return
		 */
		String next();
	}

	/**
	 * 顺序递增的表别名生成器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class SequentialTableAliasGenerator implements TableAliasGenerator
	{
		private String prefix = "T_";

		private int counter = 0;

		public SequentialTableAliasGenerator()
		{
			super();
		}

		public SequentialTableAliasGenerator(String prefix)
		{
			super();
			this.prefix = prefix;
		}

		public SequentialTableAliasGenerator(int counter)
		{
			super();
			this.counter = counter;
		}

		public SequentialTableAliasGenerator(String prefix, int counter)
		{
			super();
			this.prefix = prefix;
			this.counter = counter;
		}

		@Override
		public String next()
		{
			return this.prefix + (this.counter++);
		}
	}

	/**
	 * 恒定的表别名生成器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class ConstantTableAliasGenerator implements TableAliasGenerator
	{
		private String alias = "T_";

		public ConstantTableAliasGenerator()
		{
			super();
		}

		public String getAlias()
		{
			return alias;
		}

		public void setAlias(String alias)
		{
			this.alias = alias;
		}

		@Override
		public String next()
		{
			return this.alias;
		}
	}
}
