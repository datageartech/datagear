/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import java.io.Serializable;

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

	/** 是否禁用匿名用户 */
	@Value("${disableAnonymous}")
	private boolean disableAnonymous;

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
	@Value("${cacheService.disabled}")
	private boolean cacheServiceDisabled;

	/** 服务层缓存-置项 */
	@Value("${cacheService.spec}")
	private String cacheServiceSpec;

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

	public boolean isCacheServiceDisabled()
	{
		return cacheServiceDisabled;
	}

	public void setCacheServiceDisabled(boolean cacheServiceDisabled)
	{
		this.cacheServiceDisabled = cacheServiceDisabled;
	}

	public String getCacheServiceSpec()
	{
		return cacheServiceSpec;
	}

	public void setCacheServiceSpec(String cacheServiceSpec)
	{
		this.cacheServiceSpec = cacheServiceSpec;
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
}
