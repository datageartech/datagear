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

package org.datagear.management.service;

import java.io.File;

import org.apache.hc.client5.http.classic.HttpClient;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.util.sqlvalidator.SqlValidator;

/**
 * {@linkplain DataSetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetEntityService extends DataPermissionEntityService<String, DataSetEntity>,
		CreateUserEntityService, AnalysisProjectAwareEntityService<DataSetEntity>
{
	/**
	 * 获取可用于执行分析的{@linkplain DataSet}。
	 * 
	 * @param id
	 * @return
	 */
	DataSet getDataSet(String id);

	/**
	 * 获取指定ID的{@linkplain ProfileDataSet}。
	 * 
	 * @param user
	 * @param id
	 * @return
	 */
	ProfileDataSet getProfileDataSet(User user, String id);

	/**
	 * 获取指定ID的文件存储目录。
	 * 
	 * @param dataSetId
	 * @return
	 */
	File getDataSetDirectory(String dataSetId);

	/**
	 * 获取{@linkplain HttpClient}。
	 * 
	 * @return
	 */
	HttpClient getHttpClient();

	/**
	 * 获取用于{@linkplain SqlDataSet}校验SQL的{@linkplain SqlValidator}。
	 * 
	 * @return {@code null}表示没有
	 */
	SqlValidator getSqlDataSetSqlValidator();
}
