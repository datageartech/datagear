/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.ResetPasswordRequest;
import org.datagear.management.service.ResetPasswordRequestService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain ResetPasswordRequestService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class ResetPasswordRequestServiceImpl extends AbstractMybatisEntityService<String, ResetPasswordRequest>
		implements ResetPasswordRequestService
{
	protected static final String SQL_NAMESPACE = ResetPasswordRequest.class.getName();

	private UserPasswordEncoder userPasswordEncoder;

	private ConcurrentMap<String, ResetPasswordRequest> _resetPasswordRequestConcurrentMap = new ConcurrentHashMap<String, ResetPasswordRequest>();

	private volatile boolean _loadedFromDB = false;

	public ResetPasswordRequestServiceImpl()
	{
		super();
	}

	public ResetPasswordRequestServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public ResetPasswordRequestServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	public UserPasswordEncoder getUserPasswordEncoder()
	{
		return userPasswordEncoder;
	}

	public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder)
	{
		this.userPasswordEncoder = userPasswordEncoder;
	}

	@Override
	public ResetPasswordRequest getByUserId(String userId)
	{
		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("userId", userId);

		return selectOneMybatis("getByUserId", params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ResetPasswordRequest> getAll()
	{
		if (!this._loadedFromDB)
		{
			Map<String, Object> params = buildParamMap();
			addIdentifierQuoteParameter(params);

			List<ResetPasswordRequest> resetPasswordRequests = selectListMybatis("getAll", params);

			for (ResetPasswordRequest resetPasswordRequest : resetPasswordRequests)
				this._resetPasswordRequestConcurrentMap.putIfAbsent(resetPasswordRequest.getId(), resetPasswordRequest);

			this._loadedFromDB = true;
		}

		if (this._resetPasswordRequestConcurrentMap.isEmpty())
			return Collections.EMPTY_LIST;
		else
		{
			Collection<ResetPasswordRequest> resetPasswordRequests = this._resetPasswordRequestConcurrentMap.values();

			List<ResetPasswordRequest> re = new ArrayList<ResetPasswordRequest>();

			for (ResetPasswordRequest resetPasswordRequest : resetPasswordRequests)
				re.add(resetPasswordRequest);

			return re;
		}
	}

	@Override
	public ResetPasswordRequest addIfNone(ResetPasswordRequest resetPasswordRequest)
	{
		ResetPasswordRequest old = getByUserId(resetPasswordRequest.getUser().getId());

		if (old != null)
			return old;
		else
		{
			String password = resetPasswordRequest.getPassword();

			if (password != null && !password.isEmpty() && this.userPasswordEncoder != null)
				resetPasswordRequest.setPassword(this.userPasswordEncoder.encode(password));

			super.add(resetPasswordRequest);

			this._resetPasswordRequestConcurrentMap.put(resetPasswordRequest.getId(), resetPasswordRequest);

			return null;
		}
	}

	@Override
	public boolean deleteById(String id)
	{
		this._resetPasswordRequestConcurrentMap.remove(id);
		return super.deleteById(id);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
