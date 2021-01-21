/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.RoleUser;
import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.RoleUserService;
import org.datagear.management.service.UserService;
import org.datagear.util.IDUtil;
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

	private RoleUserService roleUserService;

	private RoleService roleService;

	private UserPasswordEncoder userPasswordEncoder = null;

	public UserServiceImpl()
	{
		super();
	}

	public UserServiceImpl(SqlSessionFactory sqlSessionFactory, RoleUserService roleUserService,
			RoleService roleService)
	{
		super(sqlSessionFactory);
		this.roleUserService = roleUserService;
		this.roleService = roleService;
	}

	public UserServiceImpl(SqlSessionTemplate sqlSessionTemplate, RoleUserService roleUserService,
			RoleService roleService)
	{
		super(sqlSessionTemplate);
		this.roleUserService = roleUserService;
		this.roleService = roleService;
	}

	public RoleUserService getRoleUserService()
	{
		return roleUserService;
	}

	public void setRoleUserService(RoleUserService roleUserService)
	{
		this.roleUserService = roleUserService;
	}

	public RoleService getRoleService()
	{
		return roleService;
	}

	public void setRoleService(RoleService roleService)
	{
		this.roleService = roleService;
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

		boolean add = super.add(entity, params);

		if (add)
		{
			RoleUser roleUser = new RoleUser(IDUtil.randomIdOnTime20(), new Role(Role.ROLE_REGISTRY, ""), entity);
			this.roleUserService.add(roleUser);

			Set<Role> roles = entity.getRoles();

			if (roles != null && !roles.isEmpty())
			{
				for (Role role : roles)
				{
					if (Role.ROLE_REGISTRY.equals(role.getId()))
						continue;

					RoleUser ru = new RoleUser(IDUtil.randomIdOnTime20(), role, entity);
					this.roleUserService.add(ru);
				}
			}
		}

		return add;
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

		User user = selectOneMybatis("getByName", params);
		postProcessSelect(user);

		return user;
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
	protected void postProcessSelects(List<User> list)
	{
		if (list == null)
			return;

		// 屏蔽查询结果密码，避免安全隐患
		for (User user : list)
			user.setPassword(null);
	}

	@Override
	protected User postProcessSelect(User obj)
	{
		if (obj == null)
			return obj;

		Set<Role> roles = this.roleService.findByUserId(obj.getId());
		obj.setRoles(roles);

		return obj;
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
