/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import java.util.Date;
import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.util.resource.ConnectionFactory;

/**
 * {@linkplain SqlDataSet}实体。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetEntity extends SqlDataSet implements DataSetEntity
{
	private static final long serialVersionUID = 1L;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private AnalysisProject analysisProject = null;

	public SqlDataSetEntity()
	{
		super();
		this.createTime = new Date();
	}

	public SqlDataSetEntity(String id, String name, List<DataSetProperty> properties,
			SchemaConnectionFactory connectionFactory, String sql, User createUser)
	{
		super(id, name, properties, connectionFactory, sql);
		this.createTime = new Date();
		this.createUser = createUser;
	}

	@Override
	public SchemaConnectionFactory getConnectionFactory()
	{
		return (SchemaConnectionFactory) super.getConnectionFactory();
	}

	@Override
	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		if (connectionFactory != null && !(connectionFactory instanceof SchemaConnectionFactory))
			throw new IllegalArgumentException();

		super.setConnectionFactory(connectionFactory);
	}

	/**
	 * 获取{@linkplain SchemaConnectionFactory}。
	 * <p>
	 * 注意：
	 * </p>
	 * <p>
	 * 此方法与{@linkplain #getConnectionFactory()}功能一致，另参考{@linkplain #setShmConFactory(SchemaConnectionFactory)}。
	 * </p>
	 * 
	 * @return
	 */
	public SchemaConnectionFactory getShmConFactory()
	{
		return getConnectionFactory();
	}

	/**
	 * 设置{@linkplain SchemaConnectionFactory}。
	 * <p>
	 * 注意：
	 * </p>
	 * <p>
	 * 此方法与{@linkplain #setConnectionFactory(ConnectionFactory)}功能一致，主要用于序列化、反序列化此类实体对象（页面输入转换、ORM映射）时明确类型。
	 * </p>
	 * <p>
	 * 此方法名不应过长，某些数据库对标识符长度有限制，过长可能导致底层ORM的SQL语法错误（比如Oracle-12.2及以下版本限定标识符最长30个字符）。
	 * </p>
	 * 
	 * @param schemaConnectionFactory
	 */
	public void setShmConFactory(SchemaConnectionFactory schemaConnectionFactory)
	{
		setConnectionFactory(schemaConnectionFactory);
	}

	@Override
	public String getDataSetType()
	{
		return DataSetEntity.DATA_SET_TYPE_SQL;
	}

	@Override
	public void setDataSetType(String dataSetType)
	{
		// XXX 什么也不做，不采用抛出异常的方式，便于底层ORM统一SQL查询语句
		// throw new UnsupportedOperationException();
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public void setCreateUser(User createUser)
	{
		this.createUser = createUser;
	}

	@Override
	public Date getCreateTime()
	{
		return createTime;
	}

	@Override
	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = Authorization.trimPermission(dataPermission);
	}

	@Override
	public AnalysisProject getAnalysisProject()
	{
		return analysisProject;
	}

	@Override
	public void setAnalysisProject(AnalysisProject analysisProject)
	{
		this.analysisProject = analysisProject;
	}
}
