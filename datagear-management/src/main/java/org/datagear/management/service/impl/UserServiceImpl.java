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

package org.datagear.management.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationListener;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.UserService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@linkplain UserService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class UserServiceImpl extends AbstractMybatisEntityService<String, User>
		implements UserService, AuthorizationListenerAware
{
	protected static final String SQL_NAMESPACE = User.class.getName();

	private RoleService roleService;

	private PasswordEncoder passwordEncoder = null;

	private AuthorizationListener authorizationListener = null;

	private List<CreateUserEntityService> createUserEntityServices = null;

	public UserServiceImpl()
	{
		super();
	}

	public UserServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			RoleService roleService)
	{
		super(sqlSessionFactory, dialect);
		this.roleService = roleService;
	}

	public UserServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			RoleService roleService)
	{
		super(sqlSessionTemplate, dialect);
		this.roleService = roleService;
	}

	public RoleService getRoleService()
	{
		return roleService;
	}

	public void setRoleService(RoleService roleService)
	{
		this.roleService = roleService;
	}

	public PasswordEncoder getPasswordEncoder()
	{
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder)
	{
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public AuthorizationListener getAuthorizationListener()
	{
		return authorizationListener;
	}

	@Override
	public void setAuthorizationListener(AuthorizationListener authorizationListener)
	{
		this.authorizationListener = authorizationListener;
	}

	public List<CreateUserEntityService> getCreateUserEntityServices()
	{
		return createUserEntityServices;
	}

	public void setCreateUserEntityServices(List<CreateUserEntityService> createUserEntityServices)
	{
		this.createUserEntityServices = createUserEntityServices;
	}

	@Override
	public User getByName(String name)
	{
		Map<String, Object> params = buildParamMap();
		params.put("name", name);

		String id = selectOneMybatis("getIdByName", params);

		if (StringUtil.isEmpty(id))
			return null;

		return getById(id);
	}

	@Override
	public User getByNameNoPassword(String name)
	{
		User user = getByName(name);

		if (user != null)
			user.clearPassword();

		return user;
	}

	@Override
	public User getByIdNoPassword(String id)
	{
		User user = getById(id);

		if (user != null)
			user.clearPassword();

		return user;
	}

	@Override
	public User getByIdSimple(String id)
	{
		User user = getByIdNoPassword(id);

		if (user != null)
		{
			user = user.cloneSimple();
		}

		return user;
	}

	@Override
	public User getByNameSimple(String name)
	{
		User user = getByNameNoPassword(name);

		if (user != null)
		{
			user = user.cloneSimple();
		}

		return user;
	}

	@Override
	public List<User> getByIdsSimple(String[] ids, boolean discardNull)
	{
		List<User> users = new ArrayList<User>();

		for (String id : ids)
		{
			User user = getByIdSimple(id);

			if (user == null && discardNull)
				continue;

			users.add(user);
		}

		return users;
	}

	@Override
	public int getUserCount()
	{
		Number userCount = (Number) selectOneMybatis("getUserCount");
		return (userCount == null ? 0 : userCount.intValue());
	}

	@Override
	public boolean isPasswordMatchById(String id, String password)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		String nowPassword = selectOneMybatis("getPasswordById", params);

		return (this.passwordEncoder != null ? this.passwordEncoder.matches(password, nowPassword)
				: password.equals(nowPassword));
	}

	@Override
	public boolean updatePasswordById(String id, String newPassword, boolean encrypt)
	{
		if (encrypt && this.passwordEncoder != null)
			newPassword = this.passwordEncoder.encode(newPassword);

		Map<String, Object> params = buildParamMap();
		params.put("id", id);
		params.put("password", newPassword);

		cacheEvict(id);

		return updateMybatis("updatePasswordById", params) > 0;
	}

	@Override
	public boolean updateIgnoreRole(User user)
	{
		Map<String, Object> params = buildParamMap();
		setIgnoreUpdateRoleParam(params, true);

		return update(user, params);
	}

	@Override
	public void deleteByIds(String[] ids, String migrateToId)
	{
		if (this.createUserEntityServices != null)
		{
			for (CreateUserEntityService service : this.createUserEntityServices)
				service.updateCreateUserId(ids, migrateToId);
		}

		this.deleteByIds(ids);
	}

	@Override
	protected void add(User entity, Map<String, Object> params)
	{
		String password = entity.getPassword();

		if (password != null && !password.isEmpty() && this.passwordEncoder != null)
			entity.setPassword(this.passwordEncoder.encode(password));

		super.add(entity, params);
		saveUserRoles(entity);
	}

	@Override
	protected boolean update(User entity, Map<String, Object> params)
	{
		boolean updated = super.update(entity, params);

		if (!isIgnoreUpdateRoleParam(params))
		{
			saveUserRoles(entity);

			if (this.authorizationListener != null)
				this.authorizationListener.permissionUpdated();
		}

		return updated;
	}

	protected boolean isIgnoreUpdateRoleParam(Map<String, Object> params)
	{
		Boolean ignore = (Boolean) params.get("ignoreUpdateRole");
		return (ignore != null && ignore.booleanValue());
	}

	protected void setIgnoreUpdateRoleParam(Map<String, Object> params, boolean ignore)
	{
		params.put("ignoreUpdateRole", ignore);
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);
		deleteUserRoles(id);

		return deleted;
	}

	protected void saveUserRoles(User user)
	{
		if (user == null)
			return;

		deleteUserRoles(user.getId());

		Set<Role> roles = user.getRoles();
		if (roles != null && !roles.isEmpty())
		{
			Map<String, Object> params = buildParamMap();

			for (Role role : roles)
			{
				RoleUser ru = new RoleUser(IDUtil.randomIdOnTime20(), role.getId(), user.getId());
				params.put("entity", ru);

				insertMybatis("insertUserRole", params);
			}
		}
	}

	protected void deleteUserRoles(String userId)
	{
		Map<String, Object> params = buildParamMap();
		params.put("userId", userId);

		deleteMybatis("deleteUserRoles", params);
	}

	@Override
	protected User getByIdFromDB(String id, Map<String, Object> params)
	{
		User user = super.getByIdFromDB(id, params);

		if (user == null)
			return null;

		Map<String, Object> params1 = buildParamMap();
		params1.put("userId", user.getId());

		List<String> roleIds = selectListMybatis("getUserRoleIds", params1);
		if (roleIds != null && !roleIds.isEmpty())
		{
			Set<Role> roles = new HashSet<Role>(roleIds.size());
			for (String roleId : roleIds)
				roles.add(new Role(roleId, roleId));

			user.setRoles(roles);
		}

		return user;
	}

	@Override
	protected void postProcessQuery(List<User> list)
	{
		// 屏蔽查询结果密码，避免安全隐患
		for (User user : list)
			user.clearPassword();
	}

	@Override
	protected User postProcessGet(User entity)
	{
		Set<Role> roles = entity.getRoles();

		if (roles != null && !roles.isEmpty())
		{
			Set<Role> rolesNew = new HashSet<Role>(roles.size());

			for (Role role : roles)
			{
				role = this.roleService.getById(role.getId());
				addIfNonNull(rolesNew, role);
			}

			entity.setRoles(rolesNew);
		}

		return super.postProcessGet(entity);
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

	protected static class RoleUser implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String id;
		private String roleId;
		private String userId;

		public RoleUser()
		{
			super();
		}

		public RoleUser(String id, String roleId, String userId)
		{
			super();
			this.id = id;
			this.roleId = roleId;
			this.userId = userId;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getRoleId()
		{
			return roleId;
		}

		public void setRoleId(String roleId)
		{
			this.roleId = roleId;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}
	}
}
