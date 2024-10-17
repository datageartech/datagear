/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.domain;

import java.util.Date;
import java.util.List;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.management.util.DtbsSourceConnectionFactory;
import org.datagear.util.resource.ConnectionFactory;
import org.springframework.beans.BeanUtils;

/**
 * {@linkplain SqlDataSet}实体。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetEntity extends SqlDataSet implements DataSetEntity, CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime = null;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private AnalysisProject analysisProject = null;

	public SqlDataSetEntity()
	{
		super();
	}

	public SqlDataSetEntity(String id, String name, List<DataSetField> fields,
			DtbsSourceConnectionFactory connectionFactory, String sql, User createUser)
	{
		super(id, name, fields, connectionFactory, sql);
		this.createUser = createUser;
	}

	@Override
	public DtbsSourceConnectionFactory getConnectionFactory()
	{
		return (DtbsSourceConnectionFactory) super.getConnectionFactory();
	}

	@Override
	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		if (connectionFactory != null && !(connectionFactory instanceof DtbsSourceConnectionFactory))
			throw new IllegalArgumentException();

		super.setConnectionFactory(connectionFactory);
	}

	/**
	 * 获取{@linkplain DtbsSourceConnectionFactory}。
	 * <p>
	 * 注意：
	 * </p>
	 * <p>
	 * 此方法与{@linkplain #getConnectionFactory()}功能一致，另参考{@linkplain #setDtbsCnFty(DtbsSourceConnectionFactory)}。
	 * </p>
	 * 
	 * @return
	 */
	public DtbsSourceConnectionFactory getDtbsCnFty()
	{
		return getConnectionFactory();
	}

	/**
	 * 设置{@linkplain DtbsSourceConnectionFactory}。
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
	 * @param connectionFactory
	 */
	public void setDtbsCnFty(DtbsSourceConnectionFactory connectionFactory)
	{
		setConnectionFactory(connectionFactory);
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
		this.dataPermission = dataPermission;
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
	
	@Override
	public SqlDataSetEntity clone()
	{
		SqlDataSetEntity entity = new SqlDataSetEntity();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
