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

package org.datagear.web.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;

/**
 * {@linkplain DataSetQuery}内置参数规范类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetQueryParams
{
	/**
	 * 当前用户。
	 * <p>
	 * 注意：不要修改此常量值，因为它可能已被用于系统已创建的数据集中，修改它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String USER = DataSetQuery.BUILTIN_PARAM_PREFIX + "USER";

	/**
	 * 当前角色名集。
	 * <p>
	 * 在数据集的参数化语境内，虽然可以通过{@code DG_USERS.ROLES}获取角色名集，但是语法较为繁琐，
	 * 考虑到角色名集可能使用较频繁，所以单独定义。
	 * </p>
	 * <p>
	 * 注意：不要修改此常量值，因为它可能已被用于系统已创建的数据集中，修改它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String ROLE_NAMES = DataSetQuery.BUILTIN_PARAM_PREFIX + "ROLE_NAMES";

	/**
	 * 当前日期时间。
	 * <p>
	 * 注意：不要修改此常量值，因为它可能已被用于系统已创建的数据集中，修改它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String DATETIME = DataSetQuery.BUILTIN_PARAM_PREFIX + "DATETIME";

	/**
	 * 当前日期。
	 * <p>
	 * 注意：不要修改此常量值，因为它可能已被用于系统已创建的数据集中，修改它将导致这些数据集执行出错。
	 * </p>
	 */
	public static final String DATE = DataSetQuery.BUILTIN_PARAM_PREFIX + "DATE";

	/**
	 * 日期时间格式："yyyy-MM-dd HH:mm:ss"
	 * <p>
	 * 这里没有采用{@code ISO 8601}的"yyyy-MM-ddTHH:mm:ss"的格式，因为它并不是数据库的常用格式。
	 * </p>
	 */
	protected static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 日期时间格式（ISO 8601）："yyyy-MM-dd"
	 */
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * 设置全部内置参数：{@linkplain #USER}、{@linkplain #ROLE_NAMES}、{@linkplain #DATETIME}、{@linkplain #DATE}。
	 * 
	 * @param dataSetQuery
	 * @param user
	 * @param analysisRoleNames
	 * @param dateTime
	 */
	public void setParamValues(DataSetQuery dataSetQuery, AnalysisUser user, List<String> analysisRoleNames,
			LocalDateTime dateTime)
	{
		setUser(dataSetQuery, user, analysisRoleNames);
		setDateTime(dataSetQuery, dateTime);
	}

	/**
	 * 将{@linkplain AnalysisUser}以{@linkplain #USER}参数名、
	 * {@linkplain AnalysisUser#getEnabledRoleNames(AnalysisUser)}以{@linkplain #ROLE_NAMES}
	 * 参数名加入{@linkplain DataSetQuery#getParamValues()}。
	 * <p>
	 * 参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}内部可支持根据当前用户、角色返回不同的数据。
	 * </p>
	 * 
	 * @param dataSetQuery
	 * @param user
	 */
	public void setUser(DataSetQuery dataSetQuery, AnalysisUser user)
	{
		if (user == null)
			throw new IllegalArgumentException("[user] required");

		setUser(dataSetQuery, user, user.getEnabledRoleNames());
	}

	/**
	 * 将{@linkplain AnalysisUser}以{@linkplain #USER}参数名、
	 * {@code analysisRoleNames}以{@linkplain #ROLE_NAMES}
	 * 参数名加入{@linkplain DataSetQuery#getParamValues()}。
	 * <p>
	 * 参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}内部可支持根据当前用户、角色返回不同的数据。
	 * </p>
	 * 
	 * @param dataSetQuery
	 * @param user
	 * @param analysisRoleNames
	 */
	public void setUser(DataSetQuery dataSetQuery, AnalysisUser user, List<String> analysisRoleNames)
	{
		if (user == null)
			throw new IllegalArgumentException("[user] required");

		if (analysisRoleNames == null)
			throw new IllegalArgumentException("[analysisRoleNames] required");

		dataSetQuery.setParamValue(USER, user);
		dataSetQuery.setParamValue(ROLE_NAMES, analysisRoleNames);
	}

	/**
	 * 将{@linkplain LocalDateTime#now()}以{@linkplain #DATETIME}参数名、
	 * {@linkplain LocalDateTime#toLocalDate()}以{@linkplain #DATE}参数名加入{@linkplain DataSetQuery#getParamValues()}。
	 * 
	 * @param dataSetQuery
	 * @param dateTime
	 */
	public void setDateTime(DataSetQuery dataSetQuery)
	{
		setDateTime(dataSetQuery, LocalDateTime.now());
	}

	/**
	 * 将{@linkplain LocalDateTime}以{@linkplain #DATETIME}参数名、
	 * {@linkplain LocalDateTime#toLocalDate()}以{@linkplain #DATE}参数名加入{@linkplain DataSetQuery#getParamValues()}。
	 * 
	 * @param dataSetQuery
	 * @param dateTime
	 */
	public void setDateTime(DataSetQuery dataSetQuery, LocalDateTime dateTime)
	{
		if (dateTime == null)
			throw new IllegalArgumentException("[dateTime] required");

		LocalDate date = dateTime.toLocalDate();
		String dateTimeStr = DATETIME_FORMATTER.format(dateTime);
		String dateStr = DATE_FORMATTER.format(date);

		dataSetQuery.setParamValue(DATETIME, dateTimeStr);
		dataSetQuery.setParamValue(DATE, dateStr);
	}
}
