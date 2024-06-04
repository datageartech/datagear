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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.AbstractResolvableResourceDataSet;
import org.datagear.analysis.support.DataFormat;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.CsvFileDataSetEntity;
import org.datagear.management.domain.CsvValueDataSetEntity;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.DirectoryFileDataSetEntity;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.ExcelDataSetEntity;
import org.datagear.management.domain.HttpDataSetEntity;
import org.datagear.management.domain.JsonFileDataSetEntity;
import org.datagear.management.domain.JsonValueDataSetEntity;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.SummaryDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.DtbsSourceService;
import org.datagear.management.service.FileSourceService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.UserService;
import org.datagear.management.util.DtbsSourceConnectionFactory;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.sqlvalidator.SqlValidator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.cache.Cache;

/**
 * {@linkplain DataSetEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetEntityServiceImpl extends AbstractMybatisDataPermissionEntityService<String, DataSetEntity>
		implements DataSetEntityService
{
	protected static final String SQL_NAMESPACE = DataSetEntity.class.getName();

	private ConnectionSource connectionSource;

	private DtbsSourceService dtbsSourceService;

	private AnalysisProjectService analysisProjectService;

	private UserService userService;

	private FileSourceService fileSourceService;

	/** 数据集文件存储根目录 */
	private File dataSetRootDirectory;

	private HttpClient httpClient;

	private Cache dataSetResourceDataCache = null;

	/** 数据集缓存数据的最大条目数 */
	private int dataSetCacheMaxLength = 500;

	private SqlValidator sqlDataSetSqlValidator;

	public DataSetEntityServiceImpl()
	{
		super();
	}

	public DataSetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			ConnectionSource connectionSource, DtbsSourceService dtbsSourceService,
			AnalysisProjectService analysisProjectService,
			UserService userService, FileSourceService fileSourceService,
			File dataSetRootDirectory, HttpClient httpClient)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.connectionSource = connectionSource;
		this.dtbsSourceService = dtbsSourceService;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
		this.fileSourceService = fileSourceService;
		setDataSetRootDirectory(dataSetRootDirectory);
		this.httpClient = httpClient;
	}

	public DataSetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			ConnectionSource connectionSource, DtbsSourceService dtbsSourceService,
			AnalysisProjectService analysisProjectService,
			UserService userService, FileSourceService fileSourceService,
			File dataSetRootDirectory, HttpClient httpClient)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.connectionSource = connectionSource;
		this.dtbsSourceService = dtbsSourceService;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
		this.fileSourceService = fileSourceService;
		setDataSetRootDirectory(dataSetRootDirectory);
		this.httpClient = httpClient;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public DtbsSourceService getDtbsSourceService()
	{
		return dtbsSourceService;
	}

	public void setDtbsSourceService(DtbsSourceService dtbsSourceService)
	{
		this.dtbsSourceService = dtbsSourceService;
	}

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public FileSourceService getFileSourceService()
	{
		return fileSourceService;
	}

	public void setFileSourceService(FileSourceService fileSourceService)
	{
		this.fileSourceService = fileSourceService;
	}

	public File getDataSetRootDirectory()
	{
		return dataSetRootDirectory;
	}

	public void setDataSetRootDirectory(File dataSetRootDirectory)
	{
		this.dataSetRootDirectory = dataSetRootDirectory;
	}

	@Override
	public HttpClient getHttpClient()
	{
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	public Cache getDataSetResourceDataCache()
	{
		return dataSetResourceDataCache;
	}

	public void setDataSetResourceDataCache(Cache dataSetResourceDataCache)
	{
		this.dataSetResourceDataCache = dataSetResourceDataCache;
	}

	public int getDataSetCacheMaxLength()
	{
		return dataSetCacheMaxLength;
	}

	public void setDataSetCacheMaxLength(int dataSetCacheMaxLength)
	{
		this.dataSetCacheMaxLength = dataSetCacheMaxLength;
	}

	@Override
	public SqlValidator getSqlDataSetSqlValidator()
	{
		return sqlDataSetSqlValidator;
	}

	public void setSqlDataSetSqlValidator(SqlValidator sqlDataSetSqlValidator)
	{
		this.sqlDataSetSqlValidator = sqlDataSetSqlValidator;
	}

	@Override
	public File getDataSetDirectory(String dataSetId)
	{
		return FileUtil.getDirectory(getDataSetRootDirectory(), dataSetId);
	}

	@Override
	public DataSet getDataSet(String id)
	{
		DataSetEntity entity = getById(id);

		if (entity instanceof SqlDataSetEntity)
		{
			SqlDataSetEntity sqlDataSetEntity = (SqlDataSetEntity) entity;
			DtbsSourceConnectionFactory connectionFactory = sqlDataSetEntity.getConnectionFactory();
			
			if(connectionFactory != null)
			{
				connectionFactory = new DtbsSourceConnectionFactory(this.connectionSource, connectionFactory.getSchema());
				sqlDataSetEntity.setConnectionFactory(connectionFactory);
			}

			sqlDataSetEntity.setSqlValidator(this.sqlDataSetSqlValidator);
		}

		if (entity instanceof DirectoryFileDataSetEntity)
			((DirectoryFileDataSetEntity) entity).setDirectory(getDataSetDirectory(entity.getId()));

		if (entity instanceof HttpDataSetEntity)
			((HttpDataSetEntity) entity).setHttpClient(this.httpClient);

		if (entity instanceof AbstractResolvableResourceDataSet<?>)
		{
			AbstractResolvableResourceDataSet<?> rds = (AbstractResolvableResourceDataSet<?>) entity;
			rds.setCache(this.dataSetResourceDataCache);
			rds.setDataCacheMaxLength(this.dataSetCacheMaxLength);
		}

		return entity;
	}
	
	@Override
	public ProfileDataSet getProfileDataSet(User user, String id)
	{
		ProfileDataSet profileDataSet = null;

		DataSetEntity entity = getById(user, id);

		profileDataSet = ProfileDataSet.valueOf(entity);

		return profileDataSet;
	}

	@Override
	public int updateCreateUserId(String oldUserId, String newUserId)
	{
		return super.updateCreateUserId(oldUserId, newUserId);
	}

	@Override
	public int updateCreateUserId(String[] oldUserIds, String newUserId)
	{
		return super.updateCreateUserId(oldUserIds, newUserId);
	}

	@Override
	public PagingData<DataSetEntity> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter,
			String analysisProjectId)
	{
		return pagingQueryForAnalysisProjectId(user, pagingQuery, dataFilter, analysisProjectId, true);
	}

	@Override
	public void authorizationUpdated(String... analysisProjects)
	{
		permissionCacheInvalidate();
	}

	@Override
	protected void add(DataSetEntity entity, Map<String, Object> params)
	{
		if (entity instanceof SummaryDataSetEntity)
			throw new IllegalArgumentException();

		super.add(toAddDataSetEntity(entity), params);

		if (entity instanceof SqlDataSetEntity)
			addSqlDataSetEntity((SqlDataSetEntity) entity);
		else if (entity instanceof JsonValueDataSetEntity)
			addJsonValueDataSetEntity((JsonValueDataSetEntity) entity);
		else if (entity instanceof JsonFileDataSetEntity)
			addJsonFileDataSetEntity((JsonFileDataSetEntity) entity);
		else if (entity instanceof ExcelDataSetEntity)
			addExcelDataSetEntity((ExcelDataSetEntity) entity);
		else if (entity instanceof CsvValueDataSetEntity)
			addCsvValueDataSetEntity((CsvValueDataSetEntity) entity);
		else if (entity instanceof CsvFileDataSetEntity)
			addCsvFileDataSetEntity((CsvFileDataSetEntity) entity);
		else if (entity instanceof HttpDataSetEntity)
			addHttpDataSetEntity((HttpDataSetEntity) entity);
		else
			addExtDataSetEntity(entity, params);

		saveDataSetChildren(entity);
	}

	protected SummaryDataSetEntity toAddDataSetEntity(DataSetEntity entity)
	{
		SummaryDataSetEntity re = new SummaryDataSetEntity(entity);

		// 不保存默认数据格式，避免占用存储空间
		if (DataFormat.DEFAULT.equals(re.getDataFormat()))
			re.setDataFormat(null);

		return re;
	}

	protected boolean addExtDataSetEntity(DataSetEntity entity, Map<String, Object> params)
	{
		return true;
	}

	protected boolean addSqlDataSetEntity(SqlDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertSqlDataSetEntity", params) > 0);
	}

	protected boolean addJsonValueDataSetEntity(JsonValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertJsonValueDataSetEntity", params) > 0);
	}

	protected boolean addJsonFileDataSetEntity(JsonFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertJsonFileDataSetEntity", params) > 0);
	}

	protected boolean addExcelDataSetEntity(ExcelDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertExcelDataSetEntity", params) > 0);
	}

	protected boolean addCsvValueDataSetEntity(CsvValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertCsvValueDataSetEntity", params) > 0);
	}

	protected boolean addCsvFileDataSetEntity(CsvFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertCsvFileDataSetEntity", params) > 0);
	}

	protected boolean addHttpDataSetEntity(HttpDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("insertHttpDataSetEntity", params) > 0);
	}

	@Override
	protected boolean update(DataSetEntity entity, Map<String, Object> params)
	{
		if (entity instanceof SummaryDataSetEntity)
			throw new IllegalArgumentException();

		boolean success = super.update(toUpdateDataSetEntity(entity), params);

		if (success)
		{
			if (entity instanceof SqlDataSetEntity)
				success = updateSqlDataSetEntity((SqlDataSetEntity) entity);
			else if (entity instanceof JsonValueDataSetEntity)
				success = updateJsonValueDataSetEntity((JsonValueDataSetEntity) entity);
			else if (entity instanceof JsonFileDataSetEntity)
				success = updateJsonFileDataSetEntity((JsonFileDataSetEntity) entity);
			else if (entity instanceof ExcelDataSetEntity)
				success = updateExcelDataSetEntity((ExcelDataSetEntity) entity);
			else if (entity instanceof CsvValueDataSetEntity)
				success = updateCsvValueDataSetEntity((CsvValueDataSetEntity) entity);
			else if (entity instanceof CsvFileDataSetEntity)
				success = updateCsvFileDataSetEntity((CsvFileDataSetEntity) entity);
			else if (entity instanceof HttpDataSetEntity)
				success = updateHttpDataSetEntity((HttpDataSetEntity) entity);
			else
				success = updateExtDataSetEntity(entity, params);
		}

		if (success)
			saveDataSetChildren(entity);

		return success;
	}

	protected SummaryDataSetEntity toUpdateDataSetEntity(DataSetEntity entity)
	{
		SummaryDataSetEntity re = new SummaryDataSetEntity(entity);

		// 不保存默认数据格式，避免占用存储空间
		if (DataFormat.DEFAULT.equals(re.getDataFormat()))
			re.setDataFormat(null);

		return re;
	}

	protected boolean updateExtDataSetEntity(DataSetEntity entity, Map<String, Object> params)
	{
		return true;
	}

	protected boolean updateSqlDataSetEntity(SqlDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateSqlDataSetEntity", params) > 0);
	}

	protected boolean updateJsonValueDataSetEntity(JsonValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateJsonValueDataSetEntity", params) > 0);
	}

	protected boolean updateJsonFileDataSetEntity(JsonFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateJsonFileDataSetEntity", params) > 0);
	}

	protected boolean updateExcelDataSetEntity(ExcelDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateExcelDataSetEntity", params) > 0);
	}

	protected boolean updateCsvValueDataSetEntity(CsvValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateCsvValueDataSetEntity", params) > 0);
	}

	protected boolean updateCsvFileDataSetEntity(CsvFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateCsvFileDataSetEntity", params) > 0);
	}

	protected boolean updateHttpDataSetEntity(HttpDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMap();
		params.put("entity", entity);

		return (updateMybatis("updateHttpDataSetEntity", params) > 0);
	}

	@Override
	public String getResourceType()
	{
		return SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public DataSetEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return getById(user, id);
	}

	@Override
	protected DataSetEntity getByIdFromDB(String id, Map<String, Object> params)
	{
		DataSetEntity entity = super.getByIdFromDB(id, params);

		if (entity == null)
			return null;

		if (DataSetEntity.DATA_SET_TYPE_SQL.equals(entity.getDataSetType()))
			entity = getSqlDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_JsonValue.equals(entity.getDataSetType()))
			entity = getJsonValueDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(entity.getDataSetType()))
			entity = getJsonFileDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_Excel.equals(entity.getDataSetType()))
			entity = getExcelDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_CsvValue.equals(entity.getDataSetType()))
			entity = getCsvValueDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_CsvFile.equals(entity.getDataSetType()))
			entity = getCsvFileDataSetEntityById(entity.getId());
		else if (DataSetEntity.DATA_SET_TYPE_Http.equals(entity.getDataSetType()))
			entity = getHttpDataSetEntityById(entity.getId());
		else
			entity = getExtDataSetEntityById(entity, params);

		inflateParamsAndProperties(entity);

		return entity;
	}

	protected DataSetEntity getExtDataSetEntityById(DataSetEntity entity, Map<String, Object> params)
	{
		return entity;
	}

	protected void inflateParamsAndProperties(DataSetEntity dataSetEntity)
	{
		if (dataSetEntity == null)
			return;

		Map<String, Object> params = buildParamMap();
		params.put("dataSetId", dataSetEntity.getId());

		List<DataSetPropertyPO> propertyPOs = selectListMybatis("getPropertyPOs", params);
		List<DataSetProperty> dataSetProperties = DataSetPropertyPO.to(propertyPOs);
		dataSetEntity.setProperties(dataSetProperties);

		List<DataSetParamPO> paramPOs = selectListMybatis("getParamPOs", params);
		List<DataSetParam> dataSetParams = DataSetParamPO.to(paramPOs);
		dataSetEntity.setParams(dataSetParams);
	}

	protected SqlDataSetEntity getSqlDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		SqlDataSetEntity entity = selectOneMybatis("getSqlDataSetEntityById", params);

		return entity;
	}

	protected JsonValueDataSetEntity getJsonValueDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		JsonValueDataSetEntity entity = selectOneMybatis("getJsonValueDataSetEntityById", params);

		return entity;
	}

	protected JsonFileDataSetEntity getJsonFileDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		JsonFileDataSetEntity entity = selectOneMybatis("getJsonFileDataSetEntityById", params);

		return entity;
	}

	protected ExcelDataSetEntity getExcelDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		ExcelDataSetEntity entity = selectOneMybatis("getExcelDataSetEntityById", params);

		return entity;
	}

	protected CsvValueDataSetEntity getCsvValueDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		CsvValueDataSetEntity entity = selectOneMybatis("getCsvValueDataSetEntityById", params);

		return entity;
	}

	protected CsvFileDataSetEntity getCsvFileDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		CsvFileDataSetEntity entity = selectOneMybatis("getCsvFileDataSetEntityById", params);

		return entity;
	}

	protected HttpDataSetEntity getHttpDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMap();
		params.put("id", id);

		HttpDataSetEntity entity = selectOneMybatis("getHttpDataSetEntityById", params);

		return entity;
	}

	@Override
	protected DataSetEntity postProcessGet(DataSetEntity obj)
	{
		inflateAnalysisProjectAwareEntity(obj, this.analysisProjectService);
		inflateCreateUserEntity(obj, this.userService);

		// 设置默认数据格式
		if (obj.getDataFormat() == null)
			obj.setDataFormat(DataFormat.DEFAULT);

		if (obj instanceof DirectoryFileDataSetEntity)
			inflateDirectoryFileDataSetEntity((DirectoryFileDataSetEntity) obj, this.fileSourceService);

		if (obj instanceof SqlDataSetEntity)
		{
			SqlDataSetEntity entity = (SqlDataSetEntity) obj;

			DtbsSourceConnectionFactory connectionFactory = entity.getConnectionFactory();
			DtbsSource schema = (connectionFactory == null ? null : connectionFactory.getSchema());
			String schemaId = (schema == null ? null : schema.getId());

			if (!StringUtil.isEmpty(schemaId))
				connectionFactory.setSchema(this.dtbsSourceService.getById(schemaId));
		}

		return super.postProcessGet(obj);
	}

	@Override
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		super.addDataPermissionParameters(params, user);
		params.put(AnalysisProjectAwareEntity.DATA_PERMISSION_PARAM_RESOURCE_TYPE_ANALYSIS_PROJECT,
				AnalysisProject.AUTHORIZATION_RESOURCE_TYPE);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}

	protected void saveDataSetChildren(DataSetEntity entity)
	{
		saveDataSetPropertyPOs(entity);
		saveDataSetParamPOs(entity);
	}

	protected void saveDataSetPropertyPOs(DataSetEntity entity)
	{
		if (entity == null)
			return;

		Map<String, Object> delParams = buildParamMap();
		delParams.put("dataSetId", entity.getId());

		deleteMybatis("deletePropertyPOs", delParams);

		List<DataSetPropertyPO> pos = DataSetPropertyPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetPropertyPO relation : pos)
			{
				Map<String, Object> insertParams = buildParamMap();
				insertParams.put("entity", relation);

				insertMybatis("insertPropertyPO", insertParams);
			}
		}
	}

	protected void saveDataSetParamPOs(DataSetEntity entity)
	{
		if (entity == null)
			return;

		Map<String, Object> delParams = buildParamMap();
		delParams.put("dataSetId", entity.getId());

		deleteMybatis("deleteParamPOs", delParams);

		List<DataSetParamPO> pos = DataSetParamPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetParamPO relation : pos)
			{
				Map<String, Object> insertParams = buildParamMap();
				insertParams.put("entity", relation);

				insertMybatis("insertParamPO", insertParams);
			}
		}
	}

	public static abstract class DataSetChildPO<T>
	{
		private String dataSetId;
		private T child;
		private int order = 0;

		public DataSetChildPO()
		{
			super();
		}

		public DataSetChildPO(String dataSetId, T child, int order)
		{
			super();
			this.dataSetId = dataSetId;
			this.child = child;
			this.order = order;
		}

		public String getDataSetId()
		{
			return dataSetId;
		}

		public void setDataSetId(String dataSetId)
		{
			this.dataSetId = dataSetId;
		}

		public T getChild()
		{
			return child;
		}

		public void setChild(T child)
		{
			this.child = child;
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		public static <T> List<T> to(List<? extends DataSetChildPO<T>> pos)
		{
			List<T> childs = new ArrayList<>();

			if (pos != null)
			{
				for (DataSetChildPO<T> po : pos)
					childs.add(po.getChild());
			}

			return childs;
		}
	}

	public static class DataSetPropertyPO extends DataSetChildPO<DataSetProperty>
	{
		public DataSetPropertyPO()
		{
			super();
		}

		public DataSetPropertyPO(String dataSetId, DataSetProperty child, int order)
		{
			super(dataSetId, child, order);
		}

		@Override
		public DataSetProperty getChild()
		{
			return super.getChild();
		}

		@Override
		public void setChild(DataSetProperty child)
		{
			super.setChild(child);
		}

		public static List<DataSetPropertyPO> from(DataSet dataSet)
		{
			List<DataSetPropertyPO> pos = new ArrayList<>();

			List<DataSetProperty> properties = dataSet.getProperties();

			if (properties != null)
			{
				for (int i = 0; i < properties.size(); i++)
				{
					DataSetPropertyPO po = new DataSetPropertyPO(dataSet.getId(), properties.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}

	public static class DataSetParamPO extends DataSetChildPO<DataSetParam>
	{
		public DataSetParamPO()
		{
			super();
		}

		public DataSetParamPO(String dataSetId, DataSetParam child, int order)
		{
			super(dataSetId, child, order);
		}

		@Override
		public DataSetParam getChild()
		{
			return super.getChild();
		}

		@Override
		public void setChild(DataSetParam child)
		{
			super.setChild(child);
		}

		public static List<DataSetParamPO> from(DataSet dataSet)
		{
			List<DataSetParamPO> pos = new ArrayList<>();

			List<DataSetParam> params = dataSet.getParams();

			if (params != null)
			{
				for (int i = 0; i < params.size(); i++)
				{
					DataSetParamPO po = new DataSetParamPO(dataSet.getId(), params.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}
}
