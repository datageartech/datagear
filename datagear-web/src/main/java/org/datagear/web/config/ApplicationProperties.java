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

package org.datagear.web.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

/**
 * 系统配置信息。
 * <p>
 * 对应{@code or./datagear.web.application.properties}中的配置项。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class ApplicationProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** #重设密码创建校验文件的目录 */
	@Value("${resetPasswordCheckFileDirectory}")
	private String resetPasswordCheckFileDirectory;

	/** 驱动程序管理主目录 */
	@Value("${driverRootDirectory}")
	private String driverRootDirectory;

	/** 系统使用的derby数据库主目录 */
	@Value("${derbyDirectory}")
	private String derbyDirectory;

	/** 临时文件目录 */
	@Value("${tempDirectory}")
	private String tempDirectory;

	/** 图表插件主目录 */
	@Value("${chartPluginRootDirectory}")
	private String chartPluginRootDirectory;

	/** 看板主目录 */
	@Value("${dashboardRootDirectory}")
	private String dashboardRootDirectory;

	/** 看板全局资源主目录 */
	@Value("${dashboardGlobalResRootDirectory}")
	private String dashboardGlobalResRootDirectory;

	/** 看板模板内引用全局资源的URL前缀 */
	@Value("${dashboardGlobalResUrlPrefix}")
	private String dashboardGlobalResUrlPrefix;

	/** 数据集文件主目录 */
	@Value("${dataSetRootDirectory}")
	private String dataSetRootDirectory;

	/** 数据编辑界面自定义URL构建器脚本文件 */
	@Value("${schemaUrlBuilderScriptFile}")
	private String schemaUrlBuilderScriptFile;

	/** 是否禁用匿名用户访问系统 */
	@Value("${disableAnonymous}")
	private boolean disableAnonymous;

	/** 是否禁用图表/看板展示操作的匿名用户访问 */
	@Value("${disableShowAnonymous}")
	private boolean disableShowAnonymous;

	/** 是否禁用注册功能 */
	@Value("${disableRegister}")
	private boolean disableRegister;

	/** 是否禁用检测新版本功能 */
	@Value("${disableDetectNewVersion}")
	private boolean disableDetectNewVersion;

	/** 默认角色：注册用户 */
	@Value("${defaultRole.register}")
	private String defaultRoleRegister;

	/** 默认角色：管理员添加用户 */
	@Value("${defaultRole.add}")
	private String defaultRoleAdd;

	/** 默认角色：匿名用户 */
	@Value("${defaultRole.anonymous}")
	private String defaultRoleAnonymous;

	/** 清理临时目录-可删除的过期文件分钟数 */
	@Value("${cleanTempDirectory.expiredMinutes}")
	private int cleanTempDirectoryExpiredMinutes;

	/** 清理临时目录-执行清理间隔 */
	@Value("${cleanTempDirectory.interval}")
	private String cleanTempDirectoryInterval;

	/** 数据库-驱动类名 */
	@Value("${datasource.driverClassName}")
	private String datasourceDriverClassName;

	/** 数据库-URL */
	@Value("${datasource.url}")
	private String datasourceUrl;

	/** 数据库-用户名 */
	@Value("${datasource.username}")
	private String datasourceUsername;

	/** 数据库-密码 */
	@Value("${datasource.password}")
	private String datasourcePassword;

	/** 数据库-数据库方言 */
	@Value("${datasourceDialect}")
	private String datasourceDialect;

	/** 数据源密码加密是否启用 */
	@Value("${schemaPsd.crypto.enabled}")
	private boolean schemaPsdCryptoEnabled;

	/** 数据源密码加密密钥 */
	@Value("${schemaPsd.crypto.secretKey}")
	private String schemaPsdCryptoSecretKey;

	/** 数据源密码加密盐值 */
	@Value("${schemaPsd.crypto.salt}")
	private String schemaPsdCryptoSalt;

	/** 看板分享密码加密是否启用 */
	@Value("${dashboardSharePsd.crypto.enabled}")
	private boolean dashboardSharePsdCryptoEnabled;

	/** 看板分享密码加密密钥 */
	@Value("${dashboardSharePsd.crypto.secretKey}")
	private String dashboardSharePsdCryptoSecretKey;

	/** 看板分享密码加密盐值 */
	@Value("${dashboardSharePsd.crypto.salt}")
	private String dashboardSharePsdCryptoSalt;

	/** 看板访问密码允许填错次数 */
	@Value("${dashboardSharePsd.authFailThreshold}")
	private int dashboardSharePsdAuthFailThreshold;

	/** 看板访问密码允许填错次数的限定分钟数 */
	@Value("${dashboardSharePsd.authFailPastMinutes}")
	private int dashboardSharePsdAuthFailPastMinutes;

	/** IP登录错误秒数限定 */
	@Value("${ipLoginLatch.seconds}")
	private int ipLoginLatchSeconds;

	/** IP登录错误次数限定 */
	@Value("${ipLoginLatch.frequency}")
	private int ipLoginLatchFrequency;

	/** 用户名登录错误秒数限定 */
	@Value("${usernameLoginLatch.seconds}")
	private int usernameLoginLatchSeconds;

	/** 用户名登录错误次数限定 */
	@Value("${usernameLoginLatch.frequency}")
	private int usernameLoginLatchFrequency;

	/** 是否禁用登录验证码 */
	@Value("${disableLoginCheckCode}")
	private boolean disableLoginCheckCode;

	/** 系统使用的POI库的最小解压比率配置 */
	@Value("${poi.zipSecureFile.minInflateRatio}")
	private String poiZipSecureFileMinInflateRatio = "";

	/** 每条记录权限缓存存储的最多用户权限数 */
	@Value("${permissionCacheMaxLength}")
	private int permissionCacheMaxLength;

	/** 数据源缓存表信息最大个数 */
	@Value("${schemaTableCacheMaxLength}")
	private int schemaTableCacheMaxLength;

	/** 数据集缓存数据的最大条目数 */
	@Value("${dataSetCacheMaxLength}")
	private int dataSetCacheMaxLength;

	/** SQL数据集的SQL关键字黑名单 */
	private Map<String, String> sqlDataSetInvalidSqlKeywords = Collections.emptyMap();

	/** 数据源管理查询操作SQL关键字黑名单 */
	private Map<String, String> dsmanagerQueryInvalidSqlKeywords = Collections.emptyMap();

	/** 数据源管理导入SQL操作SQL关键字黑名单 */
	private Map<String, String> dsmanagerImptsqlInvalidSqlKeywords = Collections.emptyMap();

	/** 数据源管理SQL工作台-读权限用户的SQL关键字黑名单 */
	private Map<String, String> dsmanagerSqlpadReadInvalidSqlKeywords = Collections.emptyMap();

	/** 数据源管理SQL工作台-写权限用户的SQL关键字黑名单 */
	private Map<String, String> dsmanagerSqlpadEditInvalidSqlKeywords = Collections.emptyMap();

	/** 数据源管理SQL工作台-删除权限用户的SQL关键字黑名单 */
	private Map<String, String> dsmanagerSqlpadDeleteInvalidSqlKeywords = Collections.emptyMap();

	/** 跨域请求配置列表 */
	private List<CrossOriginProperties> crossOriginPropertiess = Collections.emptyList();

	public ApplicationProperties()
	{
		super();
	}

	public String getResetPasswordCheckFileDirectory()
	{
		return resetPasswordCheckFileDirectory;
	}

	protected void setResetPasswordCheckFileDirectory(String resetPasswordCheckFileDirectory)
	{
		this.resetPasswordCheckFileDirectory = resetPasswordCheckFileDirectory;
	}

	public String getDriverRootDirectory()
	{
		return driverRootDirectory;
	}

	protected void setDriverRootDirectory(String driverRootDirectory)
	{
		this.driverRootDirectory = driverRootDirectory;
	}

	public String getDerbyDirectory()
	{
		return derbyDirectory;
	}

	protected void setDerbyDirectory(String derbyDirectory)
	{
		this.derbyDirectory = derbyDirectory;
	}

	public String getTempDirectory()
	{
		return tempDirectory;
	}

	protected void setTempDirectory(String tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public String getChartPluginRootDirectory()
	{
		return chartPluginRootDirectory;
	}

	protected void setChartPluginRootDirectory(String chartPluginRootDirectory)
	{
		this.chartPluginRootDirectory = chartPluginRootDirectory;
	}

	public String getDashboardRootDirectory()
	{
		return dashboardRootDirectory;
	}

	protected void setDashboardRootDirectory(String dashboardRootDirectory)
	{
		this.dashboardRootDirectory = dashboardRootDirectory;
	}

	public String getDashboardGlobalResRootDirectory()
	{
		return dashboardGlobalResRootDirectory;
	}

	protected void setDashboardGlobalResRootDirectory(String dashboardGlobalResRootDirectory)
	{
		this.dashboardGlobalResRootDirectory = dashboardGlobalResRootDirectory;
	}

	public String getDashboardGlobalResUrlPrefixName()
	{
		String prefix = getDashboardGlobalResUrlPrefix();
		return (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix);
	}

	public String getDashboardGlobalResUrlPrefix()
	{
		return dashboardGlobalResUrlPrefix;
	}

	protected void setDashboardGlobalResUrlPrefix(String dashboardGlobalResUrlPrefix)
	{
		this.dashboardGlobalResUrlPrefix = dashboardGlobalResUrlPrefix;
	}

	public String getDataSetRootDirectory()
	{
		return dataSetRootDirectory;
	}

	protected void setDataSetRootDirectory(String dataSetRootDirectory)
	{
		this.dataSetRootDirectory = dataSetRootDirectory;
	}

	public String getSchemaUrlBuilderScriptFile()
	{
		return schemaUrlBuilderScriptFile;
	}

	protected void setSchemaUrlBuilderScriptFile(String schemaUrlBuilderScriptFile)
	{
		this.schemaUrlBuilderScriptFile = schemaUrlBuilderScriptFile;
	}

	public boolean isDisableAnonymous()
	{
		return disableAnonymous;
	}

	protected void setDisableAnonymous(boolean disableAnonymous)
	{
		this.disableAnonymous = disableAnonymous;
	}

	public boolean isDisableShowAnonymous()
	{
		return disableShowAnonymous;
	}

	protected void setDisableShowAnonymous(boolean disableShowAnonymous)
	{
		this.disableShowAnonymous = disableShowAnonymous;
	}

	public boolean isDisableRegister()
	{
		return disableRegister;
	}

	protected void setDisableRegister(boolean disableRegister)
	{
		this.disableRegister = disableRegister;
	}

	public boolean isDisableDetectNewVersion()
	{
		return disableDetectNewVersion;
	}

	protected void setDisableDetectNewVersion(boolean disableDetectNewVersion)
	{
		this.disableDetectNewVersion = disableDetectNewVersion;
	}

	public String getDefaultRoleRegister()
	{
		return defaultRoleRegister;
	}

	protected void setDefaultRoleRegister(String defaultRoleRegister)
	{
		this.defaultRoleRegister = defaultRoleRegister;
	}

	public String getDefaultRoleAdd()
	{
		return defaultRoleAdd;
	}

	protected void setDefaultRoleAdd(String defaultRoleAdd)
	{
		this.defaultRoleAdd = defaultRoleAdd;
	}

	public String getDefaultRoleAnonymous()
	{
		return defaultRoleAnonymous;
	}

	protected void setDefaultRoleAnonymous(String defaultRoleAnonymous)
	{
		this.defaultRoleAnonymous = defaultRoleAnonymous;
	}

	public int getCleanTempDirectoryExpiredMinutes()
	{
		return cleanTempDirectoryExpiredMinutes;
	}

	protected void setCleanTempDirectoryExpiredMinutes(int cleanTempDirectoryExpiredMinutes)
	{
		this.cleanTempDirectoryExpiredMinutes = cleanTempDirectoryExpiredMinutes;
	}

	public String getCleanTempDirectoryInterval()
	{
		return cleanTempDirectoryInterval;
	}

	protected void setCleanTempDirectoryInterval(String cleanTempDirectoryInterval)
	{
		this.cleanTempDirectoryInterval = cleanTempDirectoryInterval;
	}

	public String getDatasourceDriverClassName()
	{
		return datasourceDriverClassName;
	}

	protected void setDatasourceDriverClassName(String datasourceDriverClassName)
	{
		this.datasourceDriverClassName = datasourceDriverClassName;
	}

	public String getDatasourceUrl()
	{
		return datasourceUrl;
	}

	protected void setDatasourceUrl(String datasourceUrl)
	{
		this.datasourceUrl = datasourceUrl;
	}

	public String getDatasourceUsername()
	{
		return datasourceUsername;
	}

	protected void setDatasourceUsername(String datasourceUsername)
	{
		this.datasourceUsername = datasourceUsername;
	}

	public String getDatasourcePassword()
	{
		return datasourcePassword;
	}

	public void setDatasourcePassword(String datasourcePassword)
	{
		this.datasourcePassword = datasourcePassword;
	}

	public String getDatasourceDialect()
	{
		return datasourceDialect;
	}

	protected void setDatasourceDialect(String datasourceDialect)
	{
		this.datasourceDialect = datasourceDialect;
	}

	public boolean isSchemaPsdCryptoEnabled()
	{
		return schemaPsdCryptoEnabled;
	}

	protected void setSchemaPsdCryptoEnabled(boolean schemaPsdCryptoEnabled)
	{
		this.schemaPsdCryptoEnabled = schemaPsdCryptoEnabled;
	}

	public String getSchemaPsdCryptoSecretKey()
	{
		return schemaPsdCryptoSecretKey;
	}

	protected void setSchemaPsdCryptoSecretKey(String schemaPsdCryptoSecretKey)
	{
		this.schemaPsdCryptoSecretKey = schemaPsdCryptoSecretKey;
	}

	public String getSchemaPsdCryptoSalt()
	{
		return schemaPsdCryptoSalt;
	}

	protected void setSchemaPsdCryptoSalt(String schemaPsdCryptoSalt)
	{
		this.schemaPsdCryptoSalt = schemaPsdCryptoSalt;
	}

	public boolean isDashboardSharePsdCryptoEnabled()
	{
		return dashboardSharePsdCryptoEnabled;
	}

	protected void setDashboardSharePsdCryptoEnabled(boolean dashboardSharePsdCryptoEnabled)
	{
		this.dashboardSharePsdCryptoEnabled = dashboardSharePsdCryptoEnabled;
	}

	public String getDashboardSharePsdCryptoSecretKey()
	{
		return dashboardSharePsdCryptoSecretKey;
	}

	protected void setDashboardSharePsdCryptoSecretKey(String dashboardSharePsdCryptoSecretKey)
	{
		this.dashboardSharePsdCryptoSecretKey = dashboardSharePsdCryptoSecretKey;
	}

	public String getDashboardSharePsdCryptoSalt()
	{
		return dashboardSharePsdCryptoSalt;
	}

	protected void setDashboardSharePsdCryptoSalt(String dashboardSharePsdCryptoSalt)
	{
		this.dashboardSharePsdCryptoSalt = dashboardSharePsdCryptoSalt;
	}

	public int getDashboardSharePsdAuthFailThreshold()
	{
		return dashboardSharePsdAuthFailThreshold;
	}

	protected void setDashboardSharePsdAuthFailThreshold(int dashboardSharePsdAuthFailThreshold)
	{
		this.dashboardSharePsdAuthFailThreshold = dashboardSharePsdAuthFailThreshold;
	}

	public int getDashboardSharePsdAuthFailPastMinutes()
	{
		return dashboardSharePsdAuthFailPastMinutes;
	}

	protected void setDashboardSharePsdAuthFailPastMinutes(int dashboardSharePsdAuthFailPastMinutes)
	{
		this.dashboardSharePsdAuthFailPastMinutes = dashboardSharePsdAuthFailPastMinutes;
	}

	public int getIpLoginLatchSeconds()
	{
		return ipLoginLatchSeconds;
	}

	protected void setIpLoginLatchSeconds(int ipLoginLatchSeconds)
	{
		this.ipLoginLatchSeconds = ipLoginLatchSeconds;
	}

	public int getIpLoginLatchFrequency()
	{
		return ipLoginLatchFrequency;
	}

	protected void setIpLoginLatchFrequency(int ipLoginLatchFrequency)
	{
		this.ipLoginLatchFrequency = ipLoginLatchFrequency;
	}

	public int getUsernameLoginLatchSeconds()
	{
		return usernameLoginLatchSeconds;
	}

	protected void setUsernameLoginLatchSeconds(int usernameLoginLatchSeconds)
	{
		this.usernameLoginLatchSeconds = usernameLoginLatchSeconds;
	}

	public int getUsernameLoginLatchFrequency()
	{
		return usernameLoginLatchFrequency;
	}

	protected void setUsernameLoginLatchFrequency(int usernameLoginLatchFrequency)
	{
		this.usernameLoginLatchFrequency = usernameLoginLatchFrequency;
	}

	public boolean isDisableLoginCheckCode()
	{
		return disableLoginCheckCode;
	}

	protected void setDisableLoginCheckCode(boolean disableLoginCheckCode)
	{
		this.disableLoginCheckCode = disableLoginCheckCode;
	}

	public String getPoiZipSecureFileMinInflateRatio()
	{
		return poiZipSecureFileMinInflateRatio;
	}

	protected void setPoiZipSecureFileMinInflateRatio(String poiZipSecureFileMinInflateRatio)
	{
		this.poiZipSecureFileMinInflateRatio = poiZipSecureFileMinInflateRatio;
	}

	public int getPermissionCacheMaxLength()
	{
		return permissionCacheMaxLength;
	}

	public void setPermissionCacheMaxLength(int permissionCacheMaxLength)
	{
		this.permissionCacheMaxLength = permissionCacheMaxLength;
	}

	public int getSchemaTableCacheMaxLength()
	{
		return schemaTableCacheMaxLength;
	}

	protected void setSchemaTableCacheMaxLength(int schemaTableCacheMaxLength)
	{
		this.schemaTableCacheMaxLength = schemaTableCacheMaxLength;
	}

	public int getDataSetCacheMaxLength()
	{
		return dataSetCacheMaxLength;
	}

	protected void setDataSetCacheMaxLength(int dataSetCacheMaxLength)
	{
		this.dataSetCacheMaxLength = dataSetCacheMaxLength;
	}

	public Map<String, String> getSqlDataSetInvalidSqlKeywords()
	{
		return sqlDataSetInvalidSqlKeywords;
	}

	public void setSqlDataSetInvalidSqlKeywords(Map<String, String> sqlDataSetInvalidSqlKeywords)
	{
		this.sqlDataSetInvalidSqlKeywords = sqlDataSetInvalidSqlKeywords;
	}

	public Map<String, String> getDsmanagerQueryInvalidSqlKeywords()
	{
		return dsmanagerQueryInvalidSqlKeywords;
	}

	public void setDsmanagerQueryInvalidSqlKeywords(Map<String, String> dsmanagerQueryInvalidSqlKeywords)
	{
		this.dsmanagerQueryInvalidSqlKeywords = dsmanagerQueryInvalidSqlKeywords;
	}

	public Map<String, String> getDsmanagerImptsqlInvalidSqlKeywords()
	{
		return dsmanagerImptsqlInvalidSqlKeywords;
	}

	public void setDsmanagerImptsqlInvalidSqlKeywords(Map<String, String> dsmanagerImptsqlInvalidSqlKeywords)
	{
		this.dsmanagerImptsqlInvalidSqlKeywords = dsmanagerImptsqlInvalidSqlKeywords;
	}

	public Map<String, String> getDsmanagerSqlpadReadInvalidSqlKeywords()
	{
		return dsmanagerSqlpadReadInvalidSqlKeywords;
	}

	public void setDsmanagerSqlpadReadInvalidSqlKeywords(Map<String, String> dsmanagerSqlpadReadInvalidSqlKeywords)
	{
		this.dsmanagerSqlpadReadInvalidSqlKeywords = dsmanagerSqlpadReadInvalidSqlKeywords;
	}

	public Map<String, String> getDsmanagerSqlpadEditInvalidSqlKeywords()
	{
		return dsmanagerSqlpadEditInvalidSqlKeywords;
	}

	public void setDsmanagerSqlpadEditInvalidSqlKeywords(Map<String, String> dsmanagerSqlpadEditInvalidSqlKeywords)
	{
		this.dsmanagerSqlpadEditInvalidSqlKeywords = dsmanagerSqlpadEditInvalidSqlKeywords;
	}

	public Map<String, String> getDsmanagerSqlpadDeleteInvalidSqlKeywords()
	{
		return dsmanagerSqlpadDeleteInvalidSqlKeywords;
	}

	public void setDsmanagerSqlpadDeleteInvalidSqlKeywords(Map<String, String> dsmanagerSqlpadDeleteInvalidSqlKeywords)
	{
		this.dsmanagerSqlpadDeleteInvalidSqlKeywords = dsmanagerSqlpadDeleteInvalidSqlKeywords;
	}

	public List<CrossOriginProperties> getCrossOriginPropertiess()
	{
		return crossOriginPropertiess;
	}

	@SuppressWarnings("unchecked")
	protected void setCrossOriginPropertiess(List<? extends CrossOriginProperties> crossOriginPropertiess)
	{
		this.crossOriginPropertiess = (List<CrossOriginProperties>) crossOriginPropertiess;
	}
}
