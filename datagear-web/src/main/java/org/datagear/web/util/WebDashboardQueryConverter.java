/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.web.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.DashboardQueryConverter;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;

/**
 * Web环境的{@linkplain DashboardQuery}转换器。
 * 
 * @author datagear@163.com
 *
 */
public class WebDashboardQueryConverter
{
	private DashboardQueryConverter dashboardQueryConverter;

	public WebDashboardQueryConverter()
	{
		super();
	}

	public WebDashboardQueryConverter(DashboardQueryConverter dashboardQueryConverter)
	{
		super();
		this.dashboardQueryConverter = dashboardQueryConverter;
	}

	public DashboardQueryConverter getDashboardQueryConverter()
	{
		return dashboardQueryConverter;
	}

	public void setDashboardQueryConverter(DashboardQueryConverter dashboardQueryConverter)
	{
		this.dashboardQueryConverter = dashboardQueryConverter;
	}

	/**
	 * 转换{@linkplain DashboardQuery}。
	 * <p>
	 * 将{@linkplain DashboardQuery}包含的{@linkplain DataSetQuery}转换为符合{@linkplain DataSet#getParams()}类型。
	 * </p>
	 * 
	 * @param query
	 *            允许{@code null}
	 * @param chartDefs
	 *            {@linkplain DashboardQuery#getChartQueries()}的图表ID-{@linkplain ChartDefinition}映射表
	 * @param user
	 * @return
	 */
	public DashboardQuery convert(DashboardQuery query, Map<String, ? extends ChartDefinition> chartDefs, User user)
	{
		AnalysisUser analysisUser = AnalysisUser.valueOf(user);
		return convert(query, chartDefs, analysisUser);
	}

	/**
	 * 转换{@linkplain DashboardQuery}。
	 * <p>
	 * 将{@linkplain DashboardQuery}包含的{@linkplain DataSetQuery}转换为符合{@linkplain DataSet#getParams()}类型。
	 * </p>
	 * 
	 * @param query
	 *            允许{@code null}
	 * @param chartDefs
	 *            {@linkplain DashboardQuery#getChartQueries()}的图表ID-{@linkplain ChartDefinition}映射表
	 * @param analysisUser
	 * @return
	 */
	public DashboardQuery convert(DashboardQuery query, Map<String, ? extends ChartDefinition> chartDefs,
			AnalysisUser analysisUser)
	{
		DashboardQuery re = this.dashboardQueryConverter.convert(query, chartDefs);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @param user
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet, User user)
	{
		AnalysisUser analysisUser = AnalysisUser.valueOf(user);
		return convert(dataSetQuery, dataSet, analysisUser);
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @param analysisUser
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet, AnalysisUser analysisUser)
	{
		DataSetQuery re = getDataSetParamValueConverter().convert(dataSetQuery, dataSet);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换{@linkplain DataSetQuery}。
	 * 
	 * @param dataSetQuery
	 * @param dataSet
	 * @return
	 */
	public DataSetQuery convert(DataSetQuery dataSetQuery, DataSet dataSet)
	{
		return getDataSetParamValueConverter().convert(dataSetQuery, dataSet);
	}

	/**
	 * 转换为{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @param user
	 * @return
	 */
	public DataSetQuery convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams, User user)
	{
		AnalysisUser analysisUser = AnalysisUser.valueOf(user);
		return convert(paramValues, dataSetParams, analysisUser);
	}

	/**
	 * 转换为{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @param analysisUser
	 * @return
	 */
	public DataSetQuery convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams,
			AnalysisUser analysisUser)
	{
		Map<String, ?> converted = convert(paramValues, dataSetParams);
		DataSetQuery re = DataSetQuery.valueOf(converted);
		inflateAnalysisUser(re, analysisUser);

		return re;
	}

	/**
	 * 转换参数。
	 * 
	 * @param paramValues
	 * @param dataSetParams
	 * @return
	 */
	public Map<String, ?> convert(Map<String, ?> paramValues, Collection<? extends DataSetParam> dataSetParams)
	{
		return getDataSetParamValueConverter().convert(paramValues, dataSetParams);
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DashboardQuery}包含的所有{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param analysisUser
	 */
	public void inflateAnalysisUser(DashboardQuery query, AnalysisUser analysisUser)
	{
		List<String> analysisRoleNames = analysisUser.getEnabledRoleNames();

		Map<String, ChartQuery> chartQueries = query.getChartQueries();

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			ChartQuery chartQuery = entry.getValue();
			List<DataSetQuery> dataSetQueries = chartQuery.getDataSetQueries();

			for (DataSetQuery dataSetQuery : dataSetQueries)
			{
				analysisUser.setParamValue(dataSetQuery, analysisRoleNames);
			}
		}
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param user
	 */
	public void inflateAnalysisUser(DataSetQuery query, User user)
	{
		AnalysisUser analysisUser = AnalysisUser.valueOf(user);
		inflateAnalysisUser(query, analysisUser);
	}

	/**
	 * 将{@linkplain AnalysisUser}填充至{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @param analysisUser
	 */
	public void inflateAnalysisUser(DataSetQuery query, AnalysisUser analysisUser)
	{
		List<String> analysisRoleNames = analysisUser.getEnabledRoleNames();
		analysisUser.setParamValue(query, analysisRoleNames);
	}

	protected DataSetParamValueConverter getDataSetParamValueConverter()
	{
		return getDashboardQueryConverter().getDataSetParamValueConverter();
	}

	/**
	 * 数据分析用户。
	 * <p>
	 * 看板展示页面渲染上下文中的当前用户。
	 * </p>
	 * <p>
	 * 这里不直接使用{@linkplain User}，因为数据分析用户不应因{@linkplain User}的改变而改变。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AnalysisUser implements Serializable
	{
		private static final long serialVersionUID = 1L;
	
		/**
		 * 内置数据集参数：当前用户。
		 * <p>
		 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
		 * </p>
		 */
		public static final String DATA_SET_PARAM_NAME_CURRENT_USER = DataSetQuery.BUILTIN_PARAM_PREFIX + "USER";
	
		/**
		 * 内置数据集参数：当前角色名集。
		 * <p>
		 * 在数据集的参数化语境内，虽然可以通过{@code DG_USERS.ROLES}获取角色名集，但是语法较为繁琐，
		 * 考虑到角色名集可能使用较频繁，所以单独定义。
		 * </p>
		 * <p>
		 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
		 * </p>
		 */
		public static final String DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES = DataSetQuery.BUILTIN_PARAM_PREFIX
				+ "ROLE_NAMES";
	
		/** ID */
		private String id;
	
		/** 用户名 */
		private String name;
	
		/** 姓名 */
		private String realName;
	
		/** 是否管理员 */
		private boolean admin = false;
	
		/** 是否是匿名用户 */
		private boolean anonymous = false;
	
		/** 角色集 */
		private List<WebDashboardQueryConverter.AnalysisRole> roles = Collections.emptyList();
	
		public AnalysisUser(String id, String name, String realName, boolean admin, boolean anonymous,
				List<WebDashboardQueryConverter.AnalysisRole> roles)
		{
			super();
			this.id = id;
			this.name = name;
			this.realName = realName;
			this.admin = admin;
			this.anonymous = anonymous;
			this.roles = roles;
		}
	
		public AnalysisUser(User user)
		{
			this(user.getId(), user.getName(), user.getRealName(), user.isAdmin(), user.isAnonymous(),
					WebDashboardQueryConverter.AnalysisRole.valueOf(user.getRoles()));
		}
	
		public String getId()
		{
			return id;
		}
	
		public void setId(String id)
		{
			this.id = id;
		}
	
		public String getName()
		{
			return name;
		}
	
		public void setName(String name)
		{
			this.name = name;
		}
	
		public String getRealName()
		{
			return realName;
		}
	
		public void setRealName(String realName)
		{
			this.realName = realName;
		}
	
		public boolean isAdmin()
		{
			return admin;
		}
	
		public void setAdmin(boolean admin)
		{
			this.admin = admin;
		}
	
		public boolean isAnonymous()
		{
			return anonymous;
		}
	
		public void setAnonymous(boolean anonymous)
		{
			this.anonymous = anonymous;
		}
	
		public List<WebDashboardQueryConverter.AnalysisRole> getRoles()
		{
			return roles;
		}
	
		public void setRoles(List<WebDashboardQueryConverter.AnalysisRole> roles)
		{
			this.roles = roles;
		}
	
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalysisUser other = (AnalysisUser) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}
	
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", realName=" + realName + ", admin="
					+ admin + ", anonymous=" + anonymous + ", roles=" + roles + "]";
		}
	
		/**
		 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
		 * {@linkplain #getEnabledRoleNames(AnalysisUser)}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
		 * 名加入{@linkplain DataSetQuery#getParamValues()}。
		 * <p>
		 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
		 * </p>
		 * 
		 * @param dataSetQuery
		 */
		public void setParamValue(DataSetQuery dataSetQuery)
		{
			setParamValue(dataSetQuery, getEnabledRoleNames());
		}
	
		/**
		 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
		 * {@code analysisRoleNames}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
		 * 名加入{@linkplain DataSetQuery#getParamValues()}。
		 * <p>
		 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
		 * </p>
		 * 
		 * @param dataSetQuery
		 * @param analysisRoleNames
		 */
		public void setParamValue(DataSetQuery dataSetQuery, List<String> analysisRoleNames)
		{
			if (analysisRoleNames == null)
				throw new IllegalArgumentException("[analysisRoleNames] required");
	
			dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_USER, this);
			dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES, analysisRoleNames);
		}
	
		/**
		 * 获取{@linkplain AnalysisUser#getRoles()}列表中已启用的{@linkplain AnalysisRole#getName()}列表。
		 * 
		 * @return 不会为{@code null}
		 */
		public List<String> getEnabledRoleNames()
		{
			List<String> roleNames = new ArrayList<String>();
	
			if (this.roles != null)
			{
				for (AnalysisRole role : this.roles)
				{
					if (role.isEnabled())
						roleNames.add(role.getName());
				}
			}
	
			return roleNames;
		}
	
		/**
		 * 构建{@linkplain AnalysisUser}。
		 * 
		 * @param user
		 * @return
		 */
		public static AnalysisUser valueOf(User user)
		{
			return new AnalysisUser(user);
		}
	}

	/**
	 * 数据分析角色。
	 * <p>
	 * 这里不直接使用{@linkplain Role}，因为数据分析角色不应因{@linkplain Role}的改变而改变。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AnalysisRole implements Serializable
	{
		private static final long serialVersionUID = 1L;
	
		/** ID */
		private String id;
	
		/** 名称 */
		private String name;
	
		/** 是否启用 */
		private boolean enabled = true;
	
		public AnalysisRole(String id, String name, boolean enabled)
		{
			super();
			this.id = id;
			this.name = name;
			this.enabled = enabled;
		}
	
		public AnalysisRole(Role role)
		{
			this(role.getId(), role.getName(), role.isEnabled());
		}
	
		public String getId()
		{
			return id;
		}
	
		public void setId(String id)
		{
			this.id = id;
		}
	
		public String getName()
		{
			return name;
		}
	
		public void setName(String name)
		{
			this.name = name;
		}
	
		public boolean isEnabled()
		{
			return enabled;
		}
	
		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalysisRole other = (AnalysisRole) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}
	
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", enabled=" + enabled + "]";
		}
	
		/**
		 * 构建{@linkplain AnalysisRole}列表。
		 * 
		 * @param roles 允许为{@code null}
		 * @return 不会为{@code null}
		 */
		public static List<AnalysisRole> valueOf(Collection<Role> roles)
		{
			if (roles == null)
				return Collections.emptyList();
	
			List<AnalysisRole> analysisRoles = new ArrayList<AnalysisRole>(roles.size());
	
			for (Role role : roles)
				analysisRoles.add(new AnalysisRole(role));
	
			return analysisRoles;
		}
	}
}
