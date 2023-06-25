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

	/** 工作空间主目录 */
	@Value("${DataGearWorkspace}")
	private String dataGearWorkspace;

	/** #重设密码创建校验文件的目录 */
	@Value("${directory.resetPasswordCheckFile}")
	private String directoryResetPasswordCheckFile;

	/** 驱动程序管理主目录 */
	@Value("${directory.driver}")
	private String directoryDriver;

	/** 系统使用的derby数据库主目录 */
	@Value("${directory.derby}")
	private String directoryDerby;

	/** 临时文件目录 */
	@Value("${directory.temp}")
	private String directoryTemp;

	/** 图表插件主目录 */
	@Value("${directory.chartPlugin}")
	private String directoryChartPlugin;

	/** 看板主目录 */
	@Value("${directory.dashboard}")
	private String directoryDashboard;

	/** 看板全局资源主目录 */
	@Value("${directory.dashboardGlobalRes}")
	private String directoryDashboardGlobalRes;

	/** 看板模板内引用全局资源的URL前缀 */
	@Value("${dashboardGlobalResUrlPrefix}")
	private String dashboardGlobalResUrlPrefix;

	/** 数据集文件主目录 */
	@Value("${directory.dataSet}")
	private String directoryDataSet;

	/** 数据编辑界面自定义URL构建器脚本文件 */
	@Value("${schemaUrlBuilderScriptFile}")
	private String schemaUrlBuilderScriptFile;

	/** 已载入过的图表插件上次修改时间信息存储文件 */
	@Value("${builtinChartPluginLastModifiedFile}")
	private String builtinChartPluginLastModifiedFile;

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

	/** 服务层缓存-是否禁用 */
	@Value("${cache.disabled}")
	private boolean cacheDisabled;

	/** 服务层缓存-置项 */
	@Value("${cache.spec}")
	private String cacheSpec;

	/** 看板分享密码加密是否禁用 */
	@Value("${dashboardSharePassword.crypto.disabled}")
	private boolean dashboardSharePasswordCryptoDisabled;

	/** 看板分享密码加密密钥 */
	@Value("${dashboardSharePassword.crypto.secretKey}")
	private String dashboardSharePasswordCryptoSecretKey;

	/** 看板分享密码加密盐值 */
	@Value("${dashboardSharePassword.crypto.salt}")
	private String dashboardSharePasswordCryptoSalt;

	/** 看板访问密码允许填错次数 */
	@Value("${dashboardSharePassword.authFailThreshold}")
	private int dashboardSharePasswordAuthFailThreshold;

	/** 看板访问密码允许填错次数的限定分钟数 */
	@Value("${dashboardSharePassword.authFailPastMinutes}")
	private int dashboardSharePasswordAuthFailPastMinutes;

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

	public String getDataGearWorkspace()
	{
		return dataGearWorkspace;
	}

	protected void setDataGearWorkspace(String dataGearWorkspace)
	{
		this.dataGearWorkspace = dataGearWorkspace;
	}

	public String getDirectoryResetPasswordCheckFile()
	{
		return directoryResetPasswordCheckFile;
	}

	protected void setDirectoryResetPasswordCheckFile(String directoryResetPasswordCheckFile)
	{
		this.directoryResetPasswordCheckFile = directoryResetPasswordCheckFile;
	}

	public String getDirectoryDriver()
	{
		return directoryDriver;
	}

	protected void setDirectoryDriver(String directoryDriver)
	{
		this.directoryDriver = directoryDriver;
	}

	public String getDirectoryDerby()
	{
		return directoryDerby;
	}

	protected void setDirectoryDerby(String directoryDerby)
	{
		this.directoryDerby = directoryDerby;
	}

	public String getDirectoryTemp()
	{
		return directoryTemp;
	}

	protected void setDirectoryTemp(String directoryTemp)
	{
		this.directoryTemp = directoryTemp;
	}

	public String getDirectoryChartPlugin()
	{
		return directoryChartPlugin;
	}

	protected void setDirectoryChartPlugin(String directoryChartPlugin)
	{
		this.directoryChartPlugin = directoryChartPlugin;
	}

	public String getDirectoryDashboard()
	{
		return directoryDashboard;
	}

	protected void setDirectoryDashboard(String directoryDashboard)
	{
		this.directoryDashboard = directoryDashboard;
	}

	public String getDirectoryDashboardGlobalRes()
	{
		return directoryDashboardGlobalRes;
	}

	protected void setDirectoryDashboardGlobalRes(String directoryDashboardGlobalRes)
	{
		this.directoryDashboardGlobalRes = directoryDashboardGlobalRes;
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

	public String getDirectoryDataSet()
	{
		return directoryDataSet;
	}

	protected void setDirectoryDataSet(String directoryDataSet)
	{
		this.directoryDataSet = directoryDataSet;
	}

	public String getSchemaUrlBuilderScriptFile()
	{
		return schemaUrlBuilderScriptFile;
	}

	protected void setSchemaUrlBuilderScriptFile(String schemaUrlBuilderScriptFile)
	{
		this.schemaUrlBuilderScriptFile = schemaUrlBuilderScriptFile;
	}

	public String getBuiltinChartPluginLastModifiedFile()
	{
		return builtinChartPluginLastModifiedFile;
	}

	public void setBuiltinChartPluginLastModifiedFile(String builtinChartPluginLastModifiedFile)
	{
		this.builtinChartPluginLastModifiedFile = builtinChartPluginLastModifiedFile;
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

	public boolean isCacheDisabled()
	{
		return cacheDisabled;
	}

	public void setCacheDisabled(boolean cacheDisabled)
	{
		this.cacheDisabled = cacheDisabled;
	}

	public String getCacheSpec()
	{
		return cacheSpec;
	}

	public void setCacheSpec(String cacheSpec)
	{
		this.cacheSpec = cacheSpec;
	}

	public boolean isDashboardSharePasswordCryptoDisabled()
	{
		return dashboardSharePasswordCryptoDisabled;
	}

	public void setDashboardSharePasswordCryptoDisabled(boolean dashboardSharePasswordCryptoDisabled)
	{
		this.dashboardSharePasswordCryptoDisabled = dashboardSharePasswordCryptoDisabled;
	}

	public String getDashboardSharePasswordCryptoSecretKey()
	{
		return dashboardSharePasswordCryptoSecretKey;
	}

	public void setDashboardSharePasswordCryptoSecretKey(String dashboardSharePasswordCryptoSecretKey)
	{
		this.dashboardSharePasswordCryptoSecretKey = dashboardSharePasswordCryptoSecretKey;
	}

	public String getDashboardSharePasswordCryptoSalt()
	{
		return dashboardSharePasswordCryptoSalt;
	}

	public void setDashboardSharePasswordCryptoSalt(String dashboardSharePasswordCryptoSalt)
	{
		this.dashboardSharePasswordCryptoSalt = dashboardSharePasswordCryptoSalt;
	}

	public int getDashboardSharePasswordAuthFailThreshold()
	{
		return dashboardSharePasswordAuthFailThreshold;
	}

	public void setDashboardSharePasswordAuthFailThreshold(int dashboardSharePasswordAuthFailThreshold)
	{
		this.dashboardSharePasswordAuthFailThreshold = dashboardSharePasswordAuthFailThreshold;
	}

	public int getDashboardSharePasswordAuthFailPastMinutes()
	{
		return dashboardSharePasswordAuthFailPastMinutes;
	}

	public void setDashboardSharePasswordAuthFailPastMinutes(int dashboardSharePasswordAuthFailPastMinutes)
	{
		this.dashboardSharePasswordAuthFailPastMinutes = dashboardSharePasswordAuthFailPastMinutes;
	}

	public int getIpLoginLatchSeconds()
	{
		return ipLoginLatchSeconds;
	}

	public void setIpLoginLatchSeconds(int ipLoginLatchSeconds)
	{
		this.ipLoginLatchSeconds = ipLoginLatchSeconds;
	}

	public int getIpLoginLatchFrequency()
	{
		return ipLoginLatchFrequency;
	}

	public void setIpLoginLatchFrequency(int ipLoginLatchFrequency)
	{
		this.ipLoginLatchFrequency = ipLoginLatchFrequency;
	}

	public int getUsernameLoginLatchSeconds()
	{
		return usernameLoginLatchSeconds;
	}

	public void setUsernameLoginLatchSeconds(int usernameLoginLatchSeconds)
	{
		this.usernameLoginLatchSeconds = usernameLoginLatchSeconds;
	}

	public int getUsernameLoginLatchFrequency()
	{
		return usernameLoginLatchFrequency;
	}

	public void setUsernameLoginLatchFrequency(int usernameLoginLatchFrequency)
	{
		this.usernameLoginLatchFrequency = usernameLoginLatchFrequency;
	}

	public boolean isDisableLoginCheckCode()
	{
		return disableLoginCheckCode;
	}

	public void setDisableLoginCheckCode(boolean disableLoginCheckCode)
	{
		this.disableLoginCheckCode = disableLoginCheckCode;
	}

	public String getPoiZipSecureFileMinInflateRatio()
	{
		return poiZipSecureFileMinInflateRatio;
	}

	public void setPoiZipSecureFileMinInflateRatio(String poiZipSecureFileMinInflateRatio)
	{
		this.poiZipSecureFileMinInflateRatio = poiZipSecureFileMinInflateRatio;
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
