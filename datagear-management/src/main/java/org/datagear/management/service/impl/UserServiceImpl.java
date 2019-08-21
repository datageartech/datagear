/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.User;
import org.datagear.management.service.UserService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain UserService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class UserServiceImpl extends AbstractMybatisEntityService<String, User> implements UserService
{
	protected static final String SQL_NAMESPACE = User.class.getName();

	private UserPasswordEncoder userPasswordEncoder;

	public UserServiceImpl()
	{
		super();
	}

	public UserServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public UserServiceImpl(SqlSessionTemplate sqlSessionTemplate)
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
	protected boolean add(User entity, Map<String, Object> params)
	{
		String password = entity.getPassword();

		if (password != null && !password.isEmpty() && this.userPasswordEncoder != null)
			entity.setPassword(this.userPasswordEncoder.encode(password));

		return super.add(entity, params);
	}

	@Override
	protected boolean update(User entity, Map<String, Object> params)
	{
		String password = entity.getPassword();

		if (password != null && !password.isEmpty())
		{
			if (this.userPasswordEncoder != null)
				entity.setPassword(this.userPasswordEncoder.encode(password));
		}
		else
			entity.setPassword(null);

		return super.update(entity, params);
	}

	@Override
	public User getByName(String name)
	{
		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("name", name);

		return selectOneMybatis("getByName", params);
	}

	@Override
	public boolean updatePasswordById(String id, String newPassword, boolean encrypt)
	{
		if (encrypt && this.userPasswordEncoder != null)
			newPassword = this.userPasswordEncoder.encode(newPassword);

		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("id", id);
		params.put("password", newPassword);

		return updateMybatis("updatePasswordById", params) > 0;
	}

	@Override
	protected void postProcessSelectList(List<User> list)
	{
		if (list == null)
			return;

		// 屏蔽查询结果密码，避免安全隐患
		for (User user : list)
			user.setPassword(null);
	}

	@Override
	protected void checkAddInput(User entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getName()) || isBlank(entity.getPassword()))
			throw new IllegalArgumentException();
	}

	@Override
	protected void checkUpdateInput(User entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getName()))
			throw new IllegalArgumentException();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}

}
