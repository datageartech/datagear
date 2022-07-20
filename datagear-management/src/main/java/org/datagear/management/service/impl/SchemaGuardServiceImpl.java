/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaGuardService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.Query;
import org.datagear.util.AsteriskPatternMatcher;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SchemaGuardService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuardServiceImpl extends AbstractMybatisEntityService<String, SchemaGuard>
		implements SchemaGuardService
{
	protected static final String SQL_NAMESPACE = SchemaGuard.class.getName();

	private AsteriskPatternMatcher asteriskPatternMatcher = new AsteriskPatternMatcher();

	private volatile List<SchemaGuard> _schemaGuardListCache = null;

	public SchemaGuardServiceImpl()
	{
		super();
	}

	public SchemaGuardServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super(sqlSessionFactory, dialect);
	}

	public SchemaGuardServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super(sqlSessionTemplate, dialect);
	}

	public AsteriskPatternMatcher getAsteriskPatternMatcher()
	{
		return asteriskPatternMatcher;
	}

	public void setAsteriskPatternMatcher(AsteriskPatternMatcher asteriskPatternMatcher)
	{
		this.asteriskPatternMatcher = asteriskPatternMatcher;
	}

	@Override
	public boolean isPermitted(String schemaUrl)
	{
		if (this._schemaGuardListCache == null)
		{
			List<SchemaGuard> schemaGuards = query("getAll", new Query(), buildParamMap(), true);
			SchemaGuard.sortByPriority(schemaGuards);
			this._schemaGuardListCache = Collections.unmodifiableList(new ArrayList<SchemaGuard>(schemaGuards));
		}

		// 默认为true，表示允许，比如当没有定义任何SchemaGuard时
		boolean permitted = true;

		for (SchemaGuard schemaGuard : this._schemaGuardListCache)
		{
			if (!schemaGuard.isEnabled())
				continue;

			String pattern = schemaGuard.getPattern();
			
			if(StringUtil.isEmpty(pattern))
				continue;
			
			if (this.asteriskPatternMatcher.matches(pattern, schemaUrl))
			{
				permitted = schemaGuard.isPermitted();
				break;
			}
		}

		return permitted;
	}

	@Override
	public boolean isPermitted(User user, String schemaUrl)
	{
		if (user.isAdmin())
			return true;

		return this.isPermitted(schemaUrl);
	}

	@Override
	protected boolean update(SchemaGuard entity, Map<String, Object> params)
	{
		boolean re = super.update(entity, params);

		this._schemaGuardListCache = null;

		return re;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean re = super.deleteById(id, params);

		this._schemaGuardListCache = null;

		return re;
	}

	@Override
	protected void add(SchemaGuard entity, Map<String, Object> params)
	{
		super.add(entity, params);

		this._schemaGuardListCache = null;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
