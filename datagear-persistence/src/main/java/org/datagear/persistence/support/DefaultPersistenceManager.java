/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;
import java.util.List;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.model.support.PropertyModel;
import org.datagear.model.support.PropertyPathInfo;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.QueryResultMetaInfo;
import org.datagear.persistence.SqlBuilder;
import org.datagear.persistence.mapper.PropertyModelMapper;
import org.datagear.persistence.mapper.RelationMapper;
import org.springframework.core.convert.ConversionService;

/**
 * 默认持久化管理器。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultPersistenceManager extends AbstractModelDataAccessObject implements PersistenceManager
{
	protected static final String COLLECTION_PROPERTY_SIZE_NAME = "size";

	/** 数据库方言源 */
	private DialectSource dialectSource;

	private ConversionService conversionService;

	private ExpressionResolver variableExpressionResolver;

	private ExpressionResolver sqlExpressionResolver;

	private InsertPersistenceOperation insertPersistenceOperation;

	private UpdatePersistenceOperation updatePersistenceOperation;

	private DeletePersistenceOperation deletePersistenceOperation;

	private SelectPersistenceOperation selectPersistenceOperation;

	public DefaultPersistenceManager()
	{
		this(null, null);
	}

	public DefaultPersistenceManager(DialectSource dialectSource, ConversionService conversionService)
	{
		super();

		this.dialectSource = dialectSource;
		this.conversionService = conversionService;

		this.variableExpressionResolver = new VariableExpressionResolver();

		this.sqlExpressionResolver = new SqlExpressionResolver();

		this.insertPersistenceOperation = new InsertPersistenceOperation(this.conversionService,
				this.variableExpressionResolver, this.sqlExpressionResolver);

		this.deletePersistenceOperation = new DeletePersistenceOperation();

		this.updatePersistenceOperation = new UpdatePersistenceOperation(this.insertPersistenceOperation,
				this.deletePersistenceOperation, this.conversionService, this.variableExpressionResolver,
				this.sqlExpressionResolver);

		this.selectPersistenceOperation = new SelectPersistenceOperation();
	}

	@Override
	public DialectSource getDialectSource()
	{
		return dialectSource;
	}

	public void setDialectSource(DialectSource dialectSource)
	{
		this.dialectSource = dialectSource;
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
		this.insertPersistenceOperation.setConversionService(conversionService);
		this.updatePersistenceOperation.setConversionService(conversionService);
	}

	public ExpressionResolver getVariableExpressionResolver()
	{
		return variableExpressionResolver;
	}

	public void setVariableExpressionResolver(ExpressionResolver variableExpressionResolver)
	{
		this.variableExpressionResolver = variableExpressionResolver;
		this.insertPersistenceOperation.setVariableExpressionResolver(variableExpressionResolver);
		this.updatePersistenceOperation.setVariableExpressionResolver(variableExpressionResolver);
	}

	public ExpressionResolver getSqlExpressionResolver()
	{
		return sqlExpressionResolver;
	}

	public void setSqlExpressionResolver(ExpressionResolver sqlExpressionResolver)
	{
		this.sqlExpressionResolver = sqlExpressionResolver;
		this.insertPersistenceOperation.setSqlExpressionResolver(sqlExpressionResolver);
		this.updatePersistenceOperation.setSqlExpressionResolver(sqlExpressionResolver);
	}

	public InsertPersistenceOperation getInsertPersistenceOperation()
	{
		return insertPersistenceOperation;
	}

	public void setInsertPersistenceOperation(InsertPersistenceOperation insertPersistenceOperation)
	{
		this.insertPersistenceOperation = insertPersistenceOperation;
	}

	public UpdatePersistenceOperation getUpdatePersistenceOperation()
	{
		return updatePersistenceOperation;
	}

	public void setUpdatePersistenceOperation(UpdatePersistenceOperation updatePersistenceOperation)
	{
		this.updatePersistenceOperation = updatePersistenceOperation;
	}

	public DeletePersistenceOperation getDeletePersistenceOperation()
	{
		return deletePersistenceOperation;
	}

	public void setDeletePersistenceOperation(DeletePersistenceOperation deletePersistenceOperation)
	{
		this.deletePersistenceOperation = deletePersistenceOperation;
	}

	public SelectPersistenceOperation getSelectPersistenceOperation()
	{
		return selectPersistenceOperation;
	}

	public void setSelectPersistenceOperation(SelectPersistenceOperation selectPersistenceOperation)
	{
		this.selectPersistenceOperation = selectPersistenceOperation;
	}

	@Override
	public String getTableName(Model model)
	{
		return super.getTableName(model);
	}

	@Override
	public int insert(Connection cn, Model model, Object obj) throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.insertPersistenceOperation.insert(cn, dialect, getTableName(model), model, obj);
	}

	@Override
	public int insert(Connection cn, Dialect dialect, String table, Model model, Object obj,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException
	{
		return this.insertPersistenceOperation.insert(cn, dialect, table, model, obj, expressionEvaluationContext);
	}

	@Override
	public int update(Connection cn, Model model, Object originalObj, Object updateObj, boolean updateMultipleProperty)
			throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.updatePersistenceOperation.update(cn, dialect, getTableName(model), model, originalObj, updateObj);
	}

	@Override
	public int update(Connection cn, Dialect dialect, String table, Model model, Object originalObj, Object updateObj,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException
	{
		return this.updatePersistenceOperation.update(cn, dialect, table, model, originalObj, updateObj,
				expressionEvaluationContext);
	}

	@Override
	public int insertSinglePropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propValue) throws PersistenceException
	{
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return insertPersistenceOperation.insertPropertyTableData(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerObj, property, propValue);
	}

	@Override
	public int updateSinglePropValue(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propValue) throws PersistenceException
	{
		checkPropertyPathInfoModelTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);
		checkPropertyPathInfoValueTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();
		Model propertyModel = propertyPathInfo.getModelTail();
		RelationMapper relationMapper = getRelationMapper(ownerModel, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		Dialect dialect = this.dialectSource.getDialect(cn);

		SqlBuilder condition = buildRecordCondition(cn, dialect, ownerModel, ownerObj, null);

		return updatePersistenceOperation.updatePropertyTableData(cn, dialect, getTableName(ownerModel), model,
				condition, property, propertyModelMapper, propertyPathInfo.getValueTail(), propValue);
	}

	@Override
	public int insertMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object... propValueElements) throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return insertPersistenceOperation.insertPropertyTableData(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerObj, property, propValueElements);
	}

	@Override
	public int insertMultiplePropValueElement(Connection cn, Dialect dialect, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, Object propValueElement,
			ExpressionEvaluationContext expressionEvaluationContext) throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		return insertPersistenceOperation.insertPropertyTableData(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerObj, property, propValueElement, expressionEvaluationContext);
	}

	@Override
	public int updateMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object propertyValueElement) throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);
		checkPropertyPathInfoValueTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Object oldPropValueElement = propertyPathInfo.getValueTail();
		Property property = propertyPathInfo.getPropertyTail();
		Model propertyModel = propertyPathInfo.getModelTail();
		RelationMapper relationMapper = getRelationMapper(ownerModel, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		Dialect dialect = this.dialectSource.getDialect(cn);

		SqlBuilder condition = buildRecordCondition(cn, dialect, ownerModel, ownerObj, null);

		return updatePersistenceOperation.updatePropertyTableData(cn, dialect, getTableName(ownerModel), ownerModel,
				condition, property, propertyModelMapper, oldPropValueElement, propertyValueElement);
	}

	@Override
	public int delete(Connection cn, Model model, Object... objs) throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.deletePersistenceOperation.delete(cn, dialect, getTableName(model), model, objs);
	}

	@Override
	public int deleteMultiplePropValueElement(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo,
			Object... propValueElements) throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();
		Model propertyModel = propertyPathInfo.getModelTail();
		RelationMapper relationMapper = getRelationMapper(ownerModel, property);
		PropertyModelMapper<?> propertyModelMapper = PropertyModelMapper.valueOf(property, relationMapper,
				propertyModel);

		Dialect dialect = this.dialectSource.getDialect(cn);

		SqlBuilder ownerRecordCondition = buildRecordCondition(cn, dialect, ownerModel, ownerObj, null);
		SqlBuilder propertyTableCondition = buildRecordCondition(cn, dialect, propertyModel, propValueElements,
				getMappedByWith(propertyModelMapper.getMapper()));

		return deletePersistenceOperation.deletePropertyTableData(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerRecordCondition, property, propertyModelMapper, propertyTableCondition, true);
	}

	@Override
	public List<Object> getByParam(Connection cn, Model model, Object param) throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.selectPersistenceOperation.getByParam(cn, dialect, getTableName(model), model, param);
	}

	@Override
	public List<Object> getPropValueByParam(Connection cn, Model model, Object obj, PropertyPathInfo propertyPathInfo)
			throws PersistenceException
	{
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.selectPersistenceOperation.getPropValueByParam(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerObj, property, PropertyModel.valueOf(property, propertyPathInfo.getModelTail()));
	}

	@Override
	public List<Object> getMultiplePropValueElementByParam(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, Object propValueElementParam) throws PersistenceException
	{
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.selectPersistenceOperation.getMultiplePropValueElementByParam(cn, dialect, getTableName(ownerModel),
				ownerModel, ownerObj, property, PropertyModel.valueOf(property, propertyPathInfo.getModelTail()),
				propValueElementParam);
	}

	@Override
	public PagingData<Object> query(Connection cn, Model model, PagingQuery pagingQuery) throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.selectPersistenceOperation.pagingQuery(cn, dialect, getTableName(model), model, pagingQuery);
	}

	@Override
	public QueryResultMetaInfo getQueryResultMetaInfo(Connection cn, Model model) throws PersistenceException
	{
		Dialect dialect = this.dialectSource.getDialect(cn);

		QueryResultMetaInfo queryResultMetaInfo = this.selectPersistenceOperation.getQueryResultMetaInfo(dialect,
				getTableName(model), model);

		return queryResultMetaInfo;
	}

	@Override
	public PagingData<Object> queryMultiplePropValue(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, PagingQuery pagingQuery, boolean propertyModelQueryPattern)
			throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoOwnerObjTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Object ownerObj = propertyPathInfo.getOwnerObjTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return this.selectPersistenceOperation.pagingQueryPropValue(cn, dialect, getTableName(ownerModel), ownerModel,
				ownerObj, property, PropertyModel.valueOf(property, propertyPathInfo.getModelTail()), pagingQuery,
				propertyModelQueryPattern);
	}

	@Override
	public QueryResultMetaInfo getQueryMultiplePropValueQueryResultMetaInfo(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, boolean propertyModelPattern) throws PersistenceException
	{
		checkPropertyPathInfoMultipleTail(propertyPathInfo);
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model ownerModel = propertyPathInfo.getOwnerModelTail();
		Property property = propertyPathInfo.getPropertyTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		QueryResultMetaInfo queryResultMetaInfo = this.selectPersistenceOperation.getQueryPropValueQueryResultMetaInfo(
				dialect, getTableName(ownerModel), ownerModel, property,
				PropertyModel.valueOf(property, propertyPathInfo.getModelTail()), propertyModelPattern);

		return queryResultMetaInfo;
	}

	@Override
	public PagingData<Object> queryPropValueSource(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo, PagingQuery pagingQuery) throws PersistenceException
	{
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model pmodel = propertyPathInfo.getModelTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		return selectPersistenceOperation.pagingQuery(cn, dialect, getTableName(pmodel), pmodel, pagingQuery);
	}

	@Override
	public QueryResultMetaInfo getQueryPropValueSourceQueryResultMetaInfo(Connection cn, Model model, Object obj,
			PropertyPathInfo propertyPathInfo) throws PersistenceException
	{
		checkPropertyPathInfoModelTail(propertyPathInfo);

		Model pmodel = propertyPathInfo.getModelTail();

		Dialect dialect = this.dialectSource.getDialect(cn);

		QueryResultMetaInfo queryResultMetaInfo = this.selectPersistenceOperation.getQueryResultMetaInfo(dialect,
				getTableName(pmodel), model);

		return queryResultMetaInfo;
	}

	/**
	 * 检查属性路径合法性。
	 * 
	 * @param propertyPathInfo
	 */
	protected void checkPropertyPathInfoMultipleTail(PropertyPathInfo propertyPathInfo)
	{
		Property property = propertyPathInfo.getPropertyTail();

		if (!MU.isMultipleProperty(property))
			throw new IllegalArgumentException("[propertyPathInfo] is illegal : not multiple");
	}

	/**
	 * 检查属性路径合法性。
	 * 
	 * @param propertyPathInfo
	 */
	protected void checkPropertyPathInfoOwnerObjTail(PropertyPathInfo propertyPathInfo)
	{
		if (!propertyPathInfo.hasOwnerObjTail())
			throw new IllegalArgumentException("[propertyPathInfo] is illegal : no owner obj for tail");
	}

	/**
	 * 检查属性路径合法性。
	 * 
	 * @param propertyPathInfo
	 */
	protected void checkPropertyPathInfoModelTail(PropertyPathInfo propertyPathInfo)
	{
		if (!propertyPathInfo.hasModelTail())
			throw new IllegalArgumentException("[propertyPathInfo] is illegal : no model tail");
	}

	/**
	 * 检查属性路径合法性。
	 * 
	 * @param propertyPathInfo
	 */
	protected void checkPropertyPathInfoValueTail(PropertyPathInfo propertyPathInfo)
	{
		if (!propertyPathInfo.hasValueTail())
			throw new IllegalArgumentException("[propertyPathInfo] is illegal : no value tail");
	}
}