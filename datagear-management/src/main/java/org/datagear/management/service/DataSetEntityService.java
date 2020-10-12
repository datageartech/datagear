/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import java.io.File;

import org.apache.hc.client5.http.classic.HttpClient;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.User;

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
}
