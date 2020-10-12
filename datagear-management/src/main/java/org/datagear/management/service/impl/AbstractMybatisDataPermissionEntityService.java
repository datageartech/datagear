/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataIdPermission;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectAwareEntityService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * 抽象基于Mybatis的{@linkplain DataPermissionEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisDataPermissionEntityService<ID, T extends DataPermissionEntity<ID>>
		extends AbstractMybatisEntityService<ID, T> implements DataPermissionEntityService<ID, T>
{
	public AbstractMybatisDataPermissionEntityService()
	{
		super();
	}

	public AbstractMybatisDataPermissionEntityService(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public AbstractMybatisDataPermissionEntityService(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	public int getPermission(User user, ID id)
	{
		List<ID> ids = new ArrayList<>(1);
		ids.add(id);

		List<Integer> permissions = getPermissions(user, ids, PERMISSION_NOT_FOUND);

		return permissions.get(0);
	}

	@Override
	public int[] getPermissions(User user, ID[] ids)
	{
		List<ID> idList = Arrays.asList(ids);

		List<Integer> permissions = getPermissions(user, idList, PERMISSION_NOT_FOUND);

		int[] re = new int[permissions.size()];

		for (int i = 0; i < re.length; i++)
			re[i] = permissions.get(i);

		return re;
	}

	@Override
	public boolean add(User user, T entity) throws PermissionDeniedException
	{
		return super.add(entity);
	}

	@Override
	public boolean update(User user, T entity) throws PermissionDeniedException
	{
		int permission = getPermission(user, entity.getId());

		if (!Authorization.canEdit(permission))
			throw new PermissionDeniedException();

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return update(entity, params);
	}

	@Override
	public boolean deleteById(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (!Authorization.canDelete(permission))
			throw new PermissionDeniedException();

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return deleteById(id, params);
	}

	@Override
	public boolean[] deleteByIds(User user, ID[] ids) throws PermissionDeniedException
	{
		int[] permissions = getPermissions(user, ids);

		for (int i = 0; i < permissions.length; i++)
		{
			if (!Authorization.canDelete(permissions[i]))
				throw new PermissionDeniedException();
		}

		boolean[] re = new boolean[ids.length];

		for (int i = 0; i < ids.length; i++)
		{
			Map<String, Object> params = buildParamMap();
			addDataPermissionParameters(params, user);

			re[i] = deleteById(ids[i], params);
		}

		return re;
	}

	@Override
	public T getById(User user, ID id) throws PermissionDeniedException
	{
		return getById(user, id, true);
	}

	protected T getById(User user, ID id, boolean postProcessSelect) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (!Authorization.canRead(permission))
			throw new PermissionDeniedException();

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return getById(id, params, postProcessSelect);
	}

	@Override
	public T getByIdForEdit(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (!Authorization.canEdit(permission))
			throw new PermissionDeniedException();

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return getById(id, params);
	}

	@Override
	public List<T> query(User user, Query query)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return query(query, params);
	}

	@Override
	public PagingData<T> pagingQuery(User user, PagingQuery pagingQuery)
	{
		return pagingQuery(user, pagingQuery, null);
	}

	@Override
	public PagingData<T> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		setDataFilterParam(params, dataFilter);

		return pagingQuery(pagingQuery, params);
	}

	protected void setDataFilterParam(Map<String, Object> params, String dataFilter)
	{
		if (!StringUtil.isEmpty(dataFilter))
			params.put("_dataFilter", dataFilter);
	}

	protected PagingData<T> pagingQueryForAnalysisProjectId(User user, PagingQuery pagingQuery, String dataFilter,
			String analysisProjectId)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		setDataFilterParam(params, dataFilter);

		if (!StringUtil.isEmpty(analysisProjectId))
			params.put(AnalysisProjectAwareEntityService.QUERY_PARAM_ANALYSIS_PROJECT_ID, analysisProjectId);

		return pagingQuery(pagingQuery, params);
	}

	/**
	 * 获取权限列表。
	 * 
	 * @param user
	 * @param ids
	 * @param permissionForAbsence
	 * @return
	 */
	protected List<Integer> getPermissions(User user, List<ID> ids, int permissionForAbsence)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		addDataPermissionParameters(params, user);
		params.put("ids", ids);

		List<DataIdPermission> dataPermissions = selectListMybatis("getDataIdPermissions", params);

		List<Integer> re = new ArrayList<>(ids.size());

		for (int i = 0, len = ids.size(); i < len; i++)
		{
			Integer permission = null;
			String myId = ids.get(i).toString();

			for (DataIdPermission p : dataPermissions)
			{
				if (myId.equals(p.getDataId()))
				{
					permission = p.getDataPermission();
					break;
				}
			}

			if (permission == null)
				permission = permissionForAbsence;

			re.add(permission);
		}

		return re;
	}

	/**
	 * 添加数据权限SQL参数。
	 * 
	 * @param params
	 * @param user
	 */
	protected abstract void addDataPermissionParameters(Map<String, Object> params, User user);

	/**
	 * 添加数据权限SQL参数。
	 * 
	 * @param params
	 * @param user
	 * @param resourceType
	 * @param resourceSupportPattern
	 * @param resourceHasCreator
	 */
	protected void addDataPermissionParameters(Map<String, Object> params, User user, String resourceType,
			boolean resourceSupportPattern, boolean resourceHasCreator)
	{
		params.put(DATA_PERMISSION_PARAM_CURRENT_USER, user);
		params.put(DATA_PERMISSION_PARAM_RESOURCE_TYPE, resourceType);
		params.put(DATA_PERMISSION_PARAM_RESOURCE_SUPPORT_PATTERN, resourceSupportPattern);
		params.put(DATA_PERMISSION_PARAM_RESOURCE_HAS_CREATOR, resourceHasCreator);
		params.put(DATA_PERMISSION_PARAM_MIN_READ_PERMISSION, Authorization.PERMISSION_READ_START);
		params.put(DATA_PERMISSION_PARAM_MAX_PERMISSION, Authorization.PERMISSION_MAX);
	}
}
