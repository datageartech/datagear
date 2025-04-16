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

package org.datagear.web.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.DashboardQueryConverter;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.FileTplDashboardWidgetResManager;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.analysis.support.html.HtmlChartWidgetJsonRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.IdJsonImportHtmlChartPluginVarNameResolver;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.DefaultConnectionSource;
import org.datagear.connection.GenericPropertiesProcessor;
import org.datagear.connection.XmlDriverEntityManager;
import org.datagear.connection.support.MySqlDevotedPropertiesProcessor;
import org.datagear.connection.support.OracleDevotedPropertiesProcessor;
import org.datagear.dataexchange.BatchDataExchange;
import org.datagear.dataexchange.BatchDataExchangeService;
import org.datagear.dataexchange.DataImportDependencyResolver;
import org.datagear.dataexchange.DevotedDataExchangeService;
import org.datagear.dataexchange.GenericDataExchangeService;
import org.datagear.dataexchange.support.CsvDataExportService;
import org.datagear.dataexchange.support.CsvDataImportService;
import org.datagear.dataexchange.support.ExcelDataExportService;
import org.datagear.dataexchange.support.ExcelDataImportService;
import org.datagear.dataexchange.support.JsonDataExportService;
import org.datagear.dataexchange.support.JsonDataImportService;
import org.datagear.dataexchange.support.SqlDataExportService;
import org.datagear.dataexchange.support.SqlDataImportService;
import org.datagear.management.dbversion.DbVersionManager;
import org.datagear.management.service.AnalysisProjectAuthorizationListener;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationListener;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.management.service.DashboardShareSetService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.service.DtbsSourceService;
import org.datagear.management.service.FileSourceService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.management.service.UserService;
import org.datagear.management.service.impl.AbstractMybatisDataPermissionEntityService;
import org.datagear.management.service.impl.AbstractMybatisEntityService;
import org.datagear.management.service.impl.AnalysisProjectAuthorizationListenerAware;
import org.datagear.management.service.impl.AnalysisProjectServiceImpl;
import org.datagear.management.service.impl.AuthorizationListenerAware;
import org.datagear.management.service.impl.AuthorizationServiceImpl;
import org.datagear.management.service.impl.BundleAnalysisProjectAuthorizationListener;
import org.datagear.management.service.impl.BundleAuthorizationListener;
import org.datagear.management.service.impl.DashboardShareSetServiceImpl;
import org.datagear.management.service.impl.DataSetEntityServiceImpl;
import org.datagear.management.service.impl.DtbsSourceGuardServiceImpl;
import org.datagear.management.service.impl.DtbsSourceServiceImpl;
import org.datagear.management.service.impl.FileSourceServiceImpl;
import org.datagear.management.service.impl.HtmlChartWidgetEntityServiceImpl;
import org.datagear.management.service.impl.HtmlTplDashboardWidgetEntityServiceImpl;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.SqlHistoryServiceImpl;
import org.datagear.management.service.impl.UserServiceImpl;
import org.datagear.management.util.DataPermissionSpec;
import org.datagear.management.util.ManagementSupport;
import org.datagear.management.util.RoleSpec;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.management.util.dialect.MbSqlDialectBuilder;
import org.datagear.management.util.typehandlers.DataFormatTypeHandler;
import org.datagear.management.util.typehandlers.LiteralBooleanTypeHandler;
import org.datagear.management.util.typehandlers.ResultDataFormatTypeHandler;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.meta.resolver.DefaultTableTypeResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.support.DefaultDialectSource;
import org.datagear.persistence.support.DefaultPersistenceManager;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.LastModifiedService;
import org.datagear.util.SimpleLastModifiedService;
import org.datagear.util.StringUtil;
import org.datagear.util.html.HtmlFilter;
import org.datagear.util.sqlvalidator.InvalidPatternSqlValidator;
import org.datagear.util.sqlvalidator.SqlValidator;
import org.datagear.util.version.ChangelogResolver;
import org.datagear.web.controller.LoginController;
import org.datagear.web.controller.RegisterController;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.json.jackson.ObjectMapperBuilder;
import org.datagear.web.security.AuthenticationSecurity;
import org.datagear.web.security.AuthenticationUserGetter;
import org.datagear.web.sqlpad.SqlPermissionValidator;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.sqlpad.SqlpadExecutionSubmit;
import org.datagear.web.util.AnalysisProjectAwareSupport;
import org.datagear.web.util.AuthorizationResMetaManager;
import org.datagear.web.util.AuthorizationResMetas;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.DefaultMessageChannel;
import org.datagear.web.util.DelegatingTextEncryptor;
import org.datagear.web.util.DelegatingTextEncryptor.EncryptType;
import org.datagear.web.util.DetectNewVersionScriptResolver;
import org.datagear.web.util.DirectoryFactory;
import org.datagear.web.util.DirectoryHtmlChartPluginManagerInitializer;
import org.datagear.web.util.DtbsSourceTableCache;
import org.datagear.web.util.ExpiredSessionAttrManager;
import org.datagear.web.util.MessageChannel;
import org.datagear.web.util.SessionDashboardInfoSupport;
import org.datagear.web.util.SessionIdParamResolver;
import org.datagear.web.util.SqlDriverChecker;
import org.datagear.web.util.WebDashboardQueryConverter;
import org.datagear.web.util.WebHtmlTplDashboardImportBuilderFactory;
import org.datagear.web.util.WelcomeContentLoader;
import org.datagear.web.util.XmlDriverEntityManagerInitializer;
import org.datagear.web.util.accesslatch.AccessLatch;
import org.datagear.web.util.accesslatch.IpLoginLatch;
import org.datagear.web.util.accesslatch.SimpleAccessLatch;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 核心配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * 
 * <pre>
 * {@code @Configuration}
 * {@code @EnableCaching}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 */
public class CoreConfigSupport implements ApplicationListener<ContextRefreshedEvent>
{
	public static final String NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY = "dashboardGlobalResRootDirectory";
	
	public static final String INVALID_SQL_KEYWORDS_PREFIX_REGEX="regex:";

	private Environment environment;

	private ApplicationPropertiesConfigSupport applicationPropertiesConfig;

	private DataSourceConfigSupport dataSourceConfig;

	private CacheManager _localCacheManager;

	@Autowired
	public CoreConfigSupport(Environment environment, ApplicationPropertiesConfigSupport applicationPropertiesConfig,
			DataSourceConfigSupport dataSourceConfig)
	{
		super();
		this.environment = environment;
		this.applicationPropertiesConfig = applicationPropertiesConfig;
		this.dataSourceConfig = dataSourceConfig;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	public ApplicationPropertiesConfigSupport getApplicationPropertiesConfig()
	{
		return applicationPropertiesConfig;
	}

	public void setApplicationPropertiesConfig(ApplicationPropertiesConfigSupport applicationPropertiesConfig)
	{
		this.applicationPropertiesConfig = applicationPropertiesConfig;
	}

	public DataSourceConfigSupport getDataSourceConfig()
	{
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfigSupport dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	@Bean
	public MessageSource messageSource()
	{
		ResourceBundleMessageSource bean = createResourceBundleMessageSource();
		bean.setBasenames(getMessageSourceBasenames());
		bean.setDefaultEncoding(IOUtil.CHARSET_UTF_8);
		// i18n找不到指定语言的bundle时不使用操作系统默认语言重新查找，直接使用默认bundle。
		// 系统目前只有默认bundle（无后缀）、英语bundle（"en"后缀），如果设置为true（默认值），
		// 当操作系统语言是英语时，会导致切换语言不起作用，i18n始终会被定位至英语bundle
		bean.setFallbackToSystemLocale(false);

		return bean;
	}

	protected ResourceBundleMessageSource createResourceBundleMessageSource()
	{
		return new ResourceBundleMessageSource();
	}

	protected String[] getMessageSourceBasenames()
	{
		return new String[] { "org.datagear.web.i18n.message" };
	}

	@Bean
	public WelcomeContentLoader welcomeContentLoader()
	{
		ApplicationProperties ps = getApplicationProperties();
		return new WelcomeContentLoader(ps.getWelcomeContent(), ps.getWelcomeContentEncoding());
	}

	@Bean
	public SqlDateFormatter sqlDateFormatter()
	{
		return new SqlDateFormatter();
	}

	@Bean
	public SqlTimeFormatter sqlTimeFormatter()
	{
		return new SqlTimeFormatter();
	}

	@Bean
	public SqlTimestampFormatter sqlTimestampFormatter()
	{
		return new SqlTimestampFormatter();
	}

	@Bean
	public DateFormatter dateFormatter()
	{
		return new DateFormatter();
	}

	@Bean
	public FormattingConversionService conversionService()
	{
		DefaultFormattingConversionService bean = createDefaultFormattingConversionService();

		bean.addFormatter(this.sqlDateFormatter());
		bean.addFormatter(this.sqlTimeFormatter());
		bean.addFormatter(this.sqlTimestampFormatter());
		bean.addFormatter(this.dateFormatter());

		DefaultFormattingConversionService.addDefaultFormatters(bean);

		return bean;
	}

	protected DefaultFormattingConversionService createDefaultFormattingConversionService()
	{
		return new DefaultFormattingConversionService(false);
	}

	@Bean
	public ObjectMapperBuilder objectMapperBuilder()
	{
		ObjectMapperBuilder bean = new ObjectMapperBuilder();
		return bean;
	}

	@Bean
	public File driverRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDriverRootDirectory(), true);
	}

	@Bean
	public File tempDirectory()
	{
		return createDirectory(getApplicationProperties().getTempDirectory(), true);
	}

	@Bean
	public File chartPluginRootDirectory()
	{
		return createDirectory(getApplicationProperties().getChartPluginRootDirectory(), true);
	}

	@Bean
	public File dashboardRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDashboardRootDirectory(), true);
	}

	@Bean(NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	public File dashboardGlobalResRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDashboardGlobalResRootDirectory(), true);
	}

	@Bean
	public File resetPasswordCheckFileDirectory()
	{
		return createDirectory(getApplicationProperties().getResetPasswordCheckFileDirectory(), true);
	}

	@Bean
	public File dataSetRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDataSetRootDirectory(), true);
	}

	@Bean
	public File dtbsSourceUrlBuilderScriptFile()
	{
		return FileUtil.getFile(getApplicationProperties().getDtbsSourceUrlBuilderScriptFile());
	}

	@Bean
	public CloseableHttpClient httpClient()
	{
		return HttpClients.createDefault();
	}

	@Bean
	public LastModifiedService lastModifiedService()
	{
		SimpleLastModifiedService bean = new SimpleLastModifiedService();
		return bean;
	}

	@Bean
	public ExpiredSessionAttrManager expiredSessionAttrManager()
	{
		ExpiredSessionAttrManager bean = new ExpiredSessionAttrManager();
		return bean;
	}

	@Bean
	public SessionDashboardInfoSupport sessionDashboardInfoSupport()
	{
		SessionDashboardInfoSupport bean = new SessionDashboardInfoSupport();
		return bean;
	}

	@Bean
	public SessionIdParamResolver sessionIdParamResolver()
	{
		SessionIdParamResolver bean = new SessionIdParamResolver();
		return bean;
	}

	@Bean
	public DetectNewVersionScriptResolver detectNewVersionScriptResolver()
	{
		DetectNewVersionScriptResolver bean = new DetectNewVersionScriptResolver();
		bean.setDisabled(getApplicationProperties().isDisableDetectNewVersion());

		return bean;
	}

	public File createDirectory(String directoryName, boolean createIfInexistence)
	{
		DirectoryFactory bean = new DirectoryFactory();
		bean.setDirectoryName(directoryName);
		bean.setCreateIfInexistence(createIfInexistence);

		try
		{
			bean.init();
		}
		catch (IOException e)
		{
			throw new BeanInitializationException("Init directory [" + directoryName + "] failed", e);
		}

		return bean.getDirectory();
	}

	@Bean
	public AuthenticationSecurity authenticationSecurity()
	{
		AuthenticationSecurity bean = new AuthenticationSecurity(getApplicationProperties().isDisableAnonymous());
		return bean;
	}

	@Bean
	public AuthenticationUserGetter authenticationUserGetter()
	{
		AuthenticationUserGetter bean = new AuthenticationUserGetter();
		return bean;
	}

	@Bean
	public DataPermissionSpec dataPermissionSpec()
	{
		DataPermissionSpec bean = new DataPermissionSpec();
		return bean;
	}

	@Bean
	public ManagementSupport managementSupport()
	{
		ManagementSupport bean = new ManagementSupport();
		return bean;
	}

	@Bean
	public AnalysisProjectAwareSupport analysisProjectAwareSupport()
	{
		AnalysisProjectAwareSupport bean = new AnalysisProjectAwareSupport(this.managementSupport());
		return bean;
	}

	@Bean(initMethod = "upgrade")
	public DbVersionManager dbVersionManager()
	{
		DbVersionManager bean = new DbVersionManager(this.dataSourceConfig.dataSource(),
				this.resourcePatternResolver());
		return bean;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory()
	{
		try
		{
			SqlSessionFactoryBean bean = this.sqlSessionFactoryBean();
			return bean.getObject();
		}
		catch (Exception e)
		{
			throw new BeanInitializationException("Init " + SqlSessionFactory.class + " failed", e);
		}
	}
	
	protected SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception
	{
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		
		bean.setDataSource(this.dataSourceConfig.dataSource());
		bean.setMapperLocations(this.getSqlSessionMapperResources());
		bean.setTypeHandlers(new TypeHandler<?>[] { new LiteralBooleanTypeHandler(), new DataFormatTypeHandler(),
				new ResultDataFormatTypeHandler() });
		
		MbSqlDialect dialect = this.mbSqlDialect();
		bean.setConfigurationProperties(dialect.getGlobalVariables());

		return bean;
	}
	
	protected Resource[] getSqlSessionMapperResources() throws Exception
	{
		PathMatchingResourcePatternResolver resolver = this.resourcePatternResolver();
		Resource[] mapperResources = resolver.getResources(
				ResourceLoader.CLASSPATH_URL_PREFIX + "org/datagear/management/mapper/*.xml");
		
		return mapperResources;
	}
	
	@Bean
	public PathMatchingResourcePatternResolver resourcePatternResolver()
	{
		PathMatchingResourcePatternResolver bean = new PathMatchingResourcePatternResolver();
		return bean;
	}

	@Bean
	public MbSqlDialectBuilder mbSqlDialectBuilder()
	{
		String dialectName = getApplicationProperties().getDatasourceDialect();

		MbSqlDialectBuilder builder = createMbSqlDialectBuilder();
		builder.setDialectName(dialectName);

		return builder;
	}

	protected MbSqlDialectBuilder createMbSqlDialectBuilder()
	{
		return new MbSqlDialectBuilder();
	}

	@Bean
	public MbSqlDialect mbSqlDialect()
	{
		try
		{
			return this.mbSqlDialectBuilder().build(this.dataSourceConfig.dataSource());
		}
		catch (Exception e)
		{
			throw new BeanInitializationException("Init " + MbSqlDialect.class + " failed", e);
		}
	}

	@Bean
	public DBMetaResolver dbMetaResolver()
	{
		DefaultTableTypeResolver tableTypeResolver = new DefaultTableTypeResolver();
		tableTypeResolver.setDbTableTypeSpecs(getApplicationProperties().getDbTableTypeSpecs());

		GenericDBMetaResolver bean = new GenericDBMetaResolver(tableTypeResolver);
		return bean;
	}

	@Bean(destroyMethod = "releaseAll")
	public XmlDriverEntityManager driverEntityManager()
	{
		XmlDriverEntityManager bean = new XmlDriverEntityManager(driverRootDirectory());
		return bean;
	}

	@Bean(initMethod = "init")
	public XmlDriverEntityManagerInitializer xmlDriverEntityManagerInitializer()
	{
		XmlDriverEntityManagerInitializer bean = new XmlDriverEntityManagerInitializer(this.driverEntityManager());
		return bean;
	}

	@Bean(destroyMethod = "close")
	public ConnectionSource connectionSource()
	{
		DefaultConnectionSource bean = new DefaultConnectionSource(this.driverEntityManager(),
				getApplicationProperties().getConnectionSourceProperties());
		bean.setDriverChecker(new SqlDriverChecker(this.dbMetaResolver()));

		GenericPropertiesProcessor genericPropertiesProcessor = new GenericPropertiesProcessor();
		genericPropertiesProcessor.setDevotedPropertiesProcessors(
				Arrays.asList(new MySqlDevotedPropertiesProcessor(), new OracleDevotedPropertiesProcessor()));

		bean.setPropertiesProcessor(genericPropertiesProcessor);

		return bean;
	}

	@Bean
	public DtbsSourceTableCache dtbsSourceTableCache()
	{
		DtbsSourceTableCache bean = new DtbsSourceTableCache();
		bean.setTableCacheMaxLength(getApplicationProperties().getDtbsSourceTableCacheMaxLength());

		return bean;
	}

	@Bean
	public DialectSource dialectSource()
	{
		DefaultDialectSource bean = new DefaultDialectSource(this.dbMetaResolver());
		return bean;
	}

	@Bean
	public PersistenceManager persistenceManager()
	{
		DefaultPersistenceManager bean = new DefaultPersistenceManager(this.dialectSource());
		bean.setQuerySqlValidator(this.dsmanagerQuerySqlValidator());

		return bean;
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		PasswordEncoder bean = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		return bean;
	}

	@Bean
	public HtmlFilter htmlFilter()
	{
		HtmlFilter bean = new HtmlFilter();
		return bean;
	}
	
	@Bean
	public DataSetParamValueConverter dataSetParamValueConverter()
	{
		DataSetParamValueConverter bean = new DataSetParamValueConverter();
		return bean;
	}
	
	@Bean
	public DashboardQueryConverter dashboardQueryConverter()
	{
		DashboardQueryConverter bean = new DashboardQueryConverter(this.dataSetParamValueConverter());
		return bean;
	}

	@Bean
	public WebDashboardQueryConverter webDashboardQueryConverter()
	{
		WebDashboardQueryConverter bean = new WebDashboardQueryConverter(this.dashboardQueryConverter());
		return bean;
	}

	@Bean
	public DashboardThemeSource dashboardThemeSource()
	{
		DashboardThemeSource bean = new SimpleDashboardThemeSource();
		return bean;
	}
	
	@Bean
	public WebHtmlTplDashboardImportBuilderFactory webHtmlTplDashboardImportBuilderFactory()
	{
		WebHtmlTplDashboardImportBuilderFactory bean = new WebHtmlTplDashboardImportBuilderFactory();
		return bean;
	}
	
	@Bean
	public AuthorizationResMetaManager authorizationResMetaManager()
	{
		AuthorizationResMetaManager bean = new AuthorizationResMetaManager();
		return bean;
	}
	
	@Bean
	public AuthorizationResMetas authorizationResMetas()
	{
		AuthorizationResMetas bean = new AuthorizationResMetas(this.authorizationResMetaManager());
		bean.register();
		
		return bean;
	}

	@Bean
	public List<DataPermissionEntityService<?, ?>> authorizationResourceServices()
	{
		return new ArrayList<>();
	}

	@Bean
	public AuthorizationService authorizationService()
	{
		AuthorizationServiceImpl bean = new AuthorizationServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationResourceServices());

		return bean;
	}

	@Bean
	public DtbsSourceService dtbsSourceService()
	{
		DtbsSourceServiceImpl bean = new DtbsSourceServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationService(), this.driverEntityManager(), this.userService(),
				this.dtbsSourceGuardService());
		bean.setTextEncryptor(this.dtbsSourcePsdEncryptor());

		return bean;
	}

	protected TextEncryptor dtbsSourcePsdEncryptor()
	{
		ApplicationProperties properties = getApplicationProperties();
		EncryptType encryptType = (properties.isDtbsSourcePsdCryptoEnabled() ? EncryptType.STD : EncryptType.NOOP);

		DelegatingTextEncryptor bean = new DelegatingTextEncryptor(encryptType,
				properties.getDtbsSourcePsdCryptoSecretKey(), properties.getDtbsSourcePsdCryptoSalt());
		// 这里必须设置为安全解密，不然如果配置里改了密钥、盐值，会导致相关功能解密报错而无法使用
		bean.setSafeDecrypt(true);
		// 这里应设置安全解密返回随机字符串，因为使用固定的默认值不安全
		bean.setRandomSafeDecryptValue(true);

		return bean;
	}

	@Bean
	public DtbsSourceGuardService dtbsSourceGuardService()
	{
		DtbsSourceGuardServiceImpl bean = new DtbsSourceGuardServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.lastModifiedService());

		return bean;
	}

	@Bean
	public UserService userService()
	{
		UserServiceImpl bean = new UserServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(), this.roleService());
		bean.setPasswordEncoder(this.passwordEncoder());

		return bean;
	}

	@Bean
	public RoleService roleService()
	{
		RoleServiceImpl bean = new RoleServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());
		bean.setRoleSpec(this.roleSpec());

		return bean;
	}

	@Bean
	public RoleSpec roleSpec()
	{
		RoleSpec bean = new RoleSpec();
		return bean;
	}

	@Bean
	public DataSetEntityService dataSetEntityService()
	{
		DataSetEntityServiceImpl bean = createDataSetEntityServiceImpl();
		bean.setSqlDataSetSqlValidator(this.sqlDataSetSqlValidator());
		bean.setDataSetCacheMaxLength(getApplicationProperties().getDataSetCacheMaxLength());

		return bean;
	}

	protected DataSetEntityServiceImpl createDataSetEntityServiceImpl()
	{
		DataSetEntityServiceImpl bean = new DataSetEntityServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationService(), this.connectionSource(), this.dtbsSourceService(),
				this.analysisProjectService(), this.userService(), this.fileSourceService(),
				this.dataSetRootDirectory(), this.httpClient());

		return bean;
	}

	@Bean
	public HtmlChartPluginLoader htmlChartPluginLoader()
	{
		HtmlChartPluginLoader bean = new HtmlChartPluginLoader();
		return bean;
	}

	@Bean
	public DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager()
	{
		DirectoryHtmlChartPluginManager bean = new DirectoryHtmlChartPluginManager(this.chartPluginRootDirectory(),
				this.htmlChartPluginLoader(), this.lastModifiedService());
		bean.setTmpDirectory(this.tempDirectory());

		return bean;
	}

	@Bean(initMethod = "init")
	public DirectoryHtmlChartPluginManagerInitializer directoryHtmlChartPluginManagerInitializer()
	{
		DirectoryHtmlChartPluginManagerInitializer bean = new DirectoryHtmlChartPluginManagerInitializer(
				this.resourcePatternResolver(), this.directoryHtmlChartPluginManager(),
				this.tempDirectory());
		
		bean.setClasspathPatterns(getBuiltInHtmlChartPluginClasspathPatterns());
		
		return bean;
	}
	
	protected String[] getBuiltInHtmlChartPluginClasspathPatterns()
	{
		return new String[] { DirectoryHtmlChartPluginManagerInitializer.DEFAULT_CLASSPATH_PATTERN };
	}

	@Bean
	public HtmlChartWidgetEntityService htmlChartWidgetEntityService()
	{
		HtmlChartWidgetEntityServiceImpl bean = new HtmlChartWidgetEntityServiceImpl(this.sqlSessionFactory(),
				this.mbSqlDialect(), this.authorizationService(), this.directoryHtmlChartPluginManager(),
				this.dataSetEntityService(), this.analysisProjectService(), this.userService());

		return bean;
	}

	@Bean
	public TplDashboardWidgetResManager tplDashboardWidgetResManager()
	{
		FileTplDashboardWidgetResManager bean = new FileTplDashboardWidgetResManager(
				this.dashboardRootDirectory());
		return bean;
	}

	@Bean
	public HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetRenderer()
	{
		HtmlTplDashboardWidgetHtmlRenderer bean = new HtmlTplDashboardWidgetHtmlRenderer(
				this.htmlChartWidgetEntityService());

		bean.setImportHtmlChartPluginVarNameResolver(this.idJsonImportHtmlChartPluginVarNameResolver());

		return bean;
	}

	@Bean
	public IdJsonImportHtmlChartPluginVarNameResolver idJsonImportHtmlChartPluginVarNameResolver()
	{
		IdJsonImportHtmlChartPluginVarNameResolver bean = new IdJsonImportHtmlChartPluginVarNameResolver();
		return bean;
	}

	@Bean
	public HtmlChartWidgetJsonRenderer htmlChartWidgetJsonRenderer()
	{
		HtmlChartWidgetJsonRenderer bean = new HtmlChartWidgetJsonRenderer();
		return bean;
	}

	@Bean
	public HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService()
	{
		HtmlTplDashboardWidgetEntityServiceImpl bean = new HtmlTplDashboardWidgetEntityServiceImpl(
				this.sqlSessionFactory(), this.mbSqlDialect(), this.authorizationService(),
				this.htmlTplDashboardWidgetRenderer(), this.tplDashboardWidgetResManager(),
				this.analysisProjectService(), this.userService());

		return bean;
	}

	@Bean
	public DashboardShareSetService dashboardShareSetService()
	{
		DashboardShareSetServiceImpl bean = new DashboardShareSetServiceImpl(this.sqlSessionFactory(),
				this.mbSqlDialect());
		bean.setTextEncryptor(this.dashboardSharePsdEncryptor());

		return bean;
	}

	protected TextEncryptor dashboardSharePsdEncryptor()
	{
		ApplicationProperties properties = getApplicationProperties();
		EncryptType encryptType = (properties.isDashboardSharePsdCryptoEnabled() ? EncryptType.STD : EncryptType.NOOP);
		
		DelegatingTextEncryptor bean = new DelegatingTextEncryptor(encryptType,
				properties.getDashboardSharePsdCryptoSecretKey(), properties.getDashboardSharePsdCryptoSalt());
		// 这里必须设置为安全解密，不然如果配置里改了密钥、盐值，会导致相关功能解密报错而无法使用
		bean.setSafeDecrypt(true);
		// 这里应设置安全解密返回随机字符串，因为使用固定的默认值不安全
		bean.setRandomSafeDecryptValue(true);
		
		return bean;
	}

	@Bean
	public AnalysisProjectService analysisProjectService()
	{
		AnalysisProjectService bean = new AnalysisProjectServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationService(), this.userService());
		return bean;
	}

	@Bean
	public FileSourceService fileSourceService()
	{
		FileSourceService bean = new FileSourceServiceImpl(this.sqlSessionFactory(),
				this.mbSqlDialect(), this.authorizationService(), this.userService());
		return bean;
	}

	@Bean
	public SqlHistoryService sqlHistoryService()
	{
		SqlHistoryServiceImpl bean = new SqlHistoryServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());
		return bean;
	}

	@Bean
	public ChangelogResolver changelogResolver()
	{
		return new ChangelogResolver(
				new ClassPathResource("org/datagear/web/changelog.txt", CoreConfigSupport.class.getClassLoader()));
	}

	@Bean
	public SqlSelectManager sqlSelectManager()
	{
		SqlSelectManager bean = new SqlSelectManager(this.dbMetaResolver());
		return bean;
	}

	@Bean
	public SqlPermissionValidator sqlPermissionValidator()
	{
		SqlPermissionValidator bean = new SqlPermissionValidator(
				this.dsmanagerSqlpadReadSqlValidator(),
				this.dsmanagerSqlpadEditSqlValidator(),
				this.dsmanagerSqlpadDeleteSqlValidator());

		return bean;
	}

	@Bean
	public SqlpadExecutionService sqlpadExecutionService()
	{
		SqlpadExecutionService bean = new SqlpadExecutionService(this.connectionSource(), this.messageSource(),
				this.sqlHistoryService(), this.sqlSelectManager(), this.sqlpadMessageChannel());
		bean.setSqlPermissionValidator(this.sqlPermissionValidator());

		return bean;
	}

	@Bean
	public MessageChannel sqlpadMessageChannel()
	{
		DefaultMessageChannel bean = new DefaultMessageChannel(
				SqlpadExecutionSubmit.MAX_PAUSE_OVER_TIME_THREASHOLD_MINUTES * 60);
		return bean;
	}

	@Bean
	public DataImportDependencyResolver dataImportDependencyResolver()
	{
		DataImportDependencyResolver bean = new DataImportDependencyResolver(this.dbMetaResolver());
		return bean;
	}

	@Bean
	public List<DevotedDataExchangeService<?>> devotedDataExchangeServices()
	{
		List<DevotedDataExchangeService<?>> bean = new ArrayList<>();

		SqlDataImportService sqlDataImportService = new SqlDataImportService();
		sqlDataImportService.setSqlValidator(this.dsmanagerImptsqlSqlValidator());

		bean.add(new CsvDataImportService(this.dbMetaResolver()));
		bean.add(new CsvDataExportService(this.dbMetaResolver()));
		bean.add(sqlDataImportService);
		bean.add(new SqlDataExportService(this.dbMetaResolver()));
		bean.add(new ExcelDataImportService(this.dbMetaResolver()));
		bean.add(new ExcelDataExportService(this.dbMetaResolver()));
		bean.add(new JsonDataImportService(this.dbMetaResolver()));
		bean.add(new JsonDataExportService(this.dbMetaResolver()));

		return bean;
	}

	@Bean(destroyMethod = "shutdown")
	public BatchDataExchangeService<BatchDataExchange> batchDataExchangeService()
	{
		BatchDataExchangeService<BatchDataExchange> bean = new BatchDataExchangeService<>();
		bean.setSubDataExchangeService(this.dataExchangeService());
		return bean;
	}

	@Bean
	public GenericDataExchangeService dataExchangeService()
	{
		GenericDataExchangeService bean = new GenericDataExchangeService();
		return bean;
	}

	@Bean
	public MessageChannel dataExchangeMessageChannel()
	{
		DefaultMessageChannel bean = new DefaultMessageChannel();
		return bean;
	}

	@Bean
	public CheckCodeManager checkCodeManager()
	{
		CheckCodeManager bean = new CheckCodeManager();

		bean.putModule(RegisterController.CHECK_CODE_MODULE_REGISTER);
		bean.putModule(LoginController.CHECK_CODE_MODULE_LOGIN);

		return bean;
	}

	@Bean
	public IpLoginLatch ipLoginLatch()
	{
		AccessLatch accessLatch = new SimpleAccessLatch(getApplicationProperties().getIpLoginLatchSeconds(),
				getApplicationProperties().getIpLoginLatchFrequency());

		IpLoginLatch bean = new IpLoginLatch(accessLatch);

		return bean;
	}

	@Bean
	public UsernameLoginLatch usernameLoginLatch()
	{
		AccessLatch accessLatch = new SimpleAccessLatch(getApplicationProperties().getUsernameLoginLatchSeconds(),
				getApplicationProperties().getUsernameLoginLatchFrequency());

		UsernameLoginLatch bean = new UsernameLoginLatch(accessLatch);

		return bean;
	}

	@Bean
	public SqlValidator sqlDataSetSqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getSqlDataSetInvalidSqlKeywords());

		return bean;
	}

	@Bean
	public SqlValidator dsmanagerQuerySqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getDsmanagerQueryInvalidSqlKeywords());

		return bean;
	}

	@Bean
	public SqlValidator dsmanagerImptsqlSqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getDsmanagerImptsqlInvalidSqlKeywords());

		return bean;
	}

	@Bean
	public SqlValidator dsmanagerSqlpadReadSqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getDsmanagerSqlpadReadInvalidSqlKeywords());

		return bean;
	}

	@Bean
	public SqlValidator dsmanagerSqlpadEditSqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getDsmanagerSqlpadEditInvalidSqlKeywords());

		return bean;
	}

	@Bean
	public SqlValidator dsmanagerSqlpadDeleteSqlValidator()
	{
		InvalidPatternSqlValidator bean = buildInvalidPatternSqlValidator(
				getApplicationProperties().getDsmanagerSqlpadDeleteInvalidSqlKeywords());

		return bean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		ApplicationContext context = event.getApplicationContext();

		initDataPermissionSpecAwares(context);
		initAuthorizationResourceServices(context);
		initAuthorizationListenerAwares(context);
		initAnalysisProjectAuthorizationListenerAwares(context);
		initCacheServices(context);
		initDevotedDataExchangeServices(context);
		initUserServiceCreateUserEntityServices(context);

		setZipSecureFileMinInflateRatio(getApplicationProperties().getPoiZipSecureFileMinInflateRatio());
	}

	public ApplicationProperties getApplicationProperties()
	{
		return this.applicationPropertiesConfig.applicationProperties();
	}

	protected InvalidPatternSqlValidator buildInvalidPatternSqlValidator(Map<String, String> keywordsMap)
	{
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();

		for (Map.Entry<String, String> entry : keywordsMap.entrySet())
		{
			String keywordsStr = entry.getValue();

			if (StringUtil.isEmpty(keywordsStr))
				continue;
			
			//正则
			if(keywordsStr.startsWith(INVALID_SQL_KEYWORDS_PREFIX_REGEX))
			{
				keywordsStr = keywordsStr.substring(INVALID_SQL_KEYWORDS_PREFIX_REGEX.length());

				if (!StringUtil.isEmpty(keywordsStr))
				{
					Pattern pattern = InvalidPatternSqlValidator.compileToSqlValidatorPattern(keywordsStr);
					patterns.put(entry.getKey(), pattern);
				}
			}
			//字面
			else
			{
				String[] keywords = StringUtil.split(keywordsStr, ",", true);
				if (keywords.length > 0)
				{
					Pattern pattern = InvalidPatternSqlValidator.toKeywordPattern(keywords);
					patterns.put(entry.getKey(), pattern);
				}
			}
		}

		InvalidPatternSqlValidator bean = new InvalidPatternSqlValidator(patterns);

		return bean;
	}

	@SuppressWarnings("rawtypes")
	protected void initDataPermissionSpecAwares(ApplicationContext context)
	{
		Map<String, AbstractMybatisDataPermissionEntityService> entityServices = context
				.getBeansOfType(AbstractMybatisDataPermissionEntityService.class);

		for (Map.Entry<String, AbstractMybatisDataPermissionEntityService> entry : entityServices.entrySet())
		{
			AbstractMybatisDataPermissionEntityService<?, ?> dpes = entry.getValue();
			dpes.setDataPermissionSpec(this.dataPermissionSpec());
		}
	}

	@SuppressWarnings("rawtypes")
	protected void initAuthorizationResourceServices(ApplicationContext context)
	{
		Map<String, DataPermissionEntityService> dataPermissionEntityServices = context
				.getBeansOfType(DataPermissionEntityService.class);

		@SuppressWarnings("unchecked")
		List<DataPermissionEntityService> resourceServices = (List) this.authorizationResourceServices();
		resourceServices.addAll(dataPermissionEntityServices.values());
	}

	protected void initAuthorizationListenerAwares(ApplicationContext context)
	{
		Map<String, AuthorizationListener> listenerMap = context.getBeansOfType(AuthorizationListener.class);
		List<AuthorizationListener> listenerList = new ArrayList<AuthorizationListener>(
				listenerMap.size());
		listenerList.addAll(listenerMap.values());

		AuthorizationListener listener = new BundleAuthorizationListener(listenerList);

		Map<String, AuthorizationListenerAware> awareMap = context.getBeansOfType(AuthorizationListenerAware.class);
		for (AuthorizationListenerAware aware : awareMap.values())
			aware.setAuthorizationListener(listener);
	}

	protected void initAnalysisProjectAuthorizationListenerAwares(ApplicationContext context)
	{
		Map<String, AnalysisProjectAuthorizationListener> listenerMap = context
				.getBeansOfType(AnalysisProjectAuthorizationListener.class);
		List<AnalysisProjectAuthorizationListener> listenerList = new ArrayList<AnalysisProjectAuthorizationListener>(
				listenerMap.size());
		listenerList.addAll(listenerMap.values());

		BundleAnalysisProjectAuthorizationListener listener = new BundleAnalysisProjectAuthorizationListener(
				listenerList);

		Map<String, AnalysisProjectAuthorizationListenerAware> awareMap = context
				.getBeansOfType(AnalysisProjectAuthorizationListenerAware.class);
		for (AnalysisProjectAuthorizationListenerAware aware : awareMap.values())
			aware.setAnalysisProjectAuthorizationListener(listener);
	}

	/**
	 * 初始化缓存相关服务类实例。
	 * 
	 * @param context
	 */
	protected void initCacheServices(ApplicationContext context)
	{
		initAbstractMybatisEntityServiceCaches(context);
		initDataSetEntityServiceCache(context);
		initHtmlTplDashboardWidgetHtmlRendererCaches(context);
		initDtbsSourceTableCache(context);
	}

	protected void initDtbsSourceTableCache(ApplicationContext context)
	{
		CacheManager cacheManager = getCacheManager(context);
		this.dtbsSourceTableCache().setCache(getCache(cacheManager, DtbsSourceTableCache.class.getSimpleName()));
	}

	@SuppressWarnings("rawtypes")
	protected void initAbstractMybatisEntityServiceCaches(ApplicationContext context)
	{
		CacheManager cacheManager = getCacheManager(context);
		Map<String, AbstractMybatisEntityService> entityServices = context
				.getBeansOfType(AbstractMybatisEntityService.class);

		for (Map.Entry<String, AbstractMybatisEntityService> entry : entityServices.entrySet())
		{
			// 缓存名应是固定不变的，避免多次重启导致占用过多缓存名
			String cacheName = entry.getKey();

			AbstractMybatisEntityService es = entry.getValue();
			es.setCache(getCache(cacheManager, cacheName));

			if (es instanceof AbstractMybatisDataPermissionEntityService<?, ?>)
			{
				AbstractMybatisDataPermissionEntityService<?, ?> dpes = (AbstractMybatisDataPermissionEntityService<?, ?>) es;
				dpes.setPermissionCache(getCache(cacheManager, cacheName + "Permission"));
				dpes.setPermissionCacheMaxLength(getApplicationProperties().getPermissionCacheMaxLength());
			}
		}
	}

	protected void initDataSetEntityServiceCache(ApplicationContext context)
	{
		CacheManager cacheManager = getCacheManager(context);

		Map<String, DataSetEntityService> entityServices = context.getBeansOfType(DataSetEntityService.class);

		for (Map.Entry<String, DataSetEntityService> entry : entityServices.entrySet())
		{
			// 缓存名应是固定不变的，避免多次重启导致占用过多缓存名
			String cacheName = entry.getKey();

			DataSetEntityService bean = entry.getValue();

			if (bean instanceof DataSetEntityServiceImpl)
			{
				((DataSetEntityServiceImpl) bean)
						.setDataSetResourceDataCache(getCache(cacheManager, cacheName + "DataSetResDataCache"));
			}
		}
	}

	protected void initHtmlTplDashboardWidgetHtmlRendererCaches(ApplicationContext context)
	{
		Map<String, HtmlTplDashboardWidgetHtmlRenderer> dashboardRenderers = context
				.getBeansOfType(HtmlTplDashboardWidgetHtmlRenderer.class);

		for (Map.Entry<String, HtmlTplDashboardWidgetHtmlRenderer> entry : dashboardRenderers.entrySet())
		{
			// 缓存名应是固定不变的，避免多次重启导致占用过多缓存名
			String cacheName = entry.getKey();
			entry.getValue().setCache(getLocalCache(cacheName));
		}
	}

	protected void initDevotedDataExchangeServices(ApplicationContext context)
	{
		// 处理循环依赖
		List<DevotedDataExchangeService<?>> devotedDataExchangeServices = this.devotedDataExchangeServices();
		devotedDataExchangeServices.add(this.batchDataExchangeService());
		this.dataExchangeService().setDevotedDataExchangeServices(devotedDataExchangeServices);
	}

	protected void initUserServiceCreateUserEntityServices(ApplicationContext context)
	{
		List<CreateUserEntityService> serviceList = getCreateUserEntityServices(context);

		UserService userService = this.userService();

		if (userService instanceof UserServiceImpl)
			((UserServiceImpl) userService).setCreateUserEntityServices(serviceList);
	}

	/**
	 * 获取{@linkplain Cache}。
	 * <p>
	 * 此方法会自动添加{@code "dgcache"}前缀。
	 * </p>
	 * 
	 * @param cacheManager
	 * @param name
	 * @return
	 */
	protected Cache getCache(CacheManager cacheManager, String name)
	{
		name = "dgcache" + name;
		return cacheManager.getCache(name);
	}

	/**
	 * 获取进程内{@linkplain Cache}。
	 * <p>
	 * 此方法会自动添加{@code "dgcache"}前缀。
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	protected Cache getLocalCache(String name)
	{
		return getCache(getLocalCacheManager(), name);
	}

	/**
	 * 获取系统级{@linkplain CacheManager}。
	 * 
	 * @param context
	 * @return
	 */
	protected CacheManager getCacheManager(ApplicationContext context)
	{
		CacheManager cacheManager = context.getBean(CacheManager.class);
		return cacheManager;
	}

	/**
	 * 获取进程内{@linkplain CacheManager}。
	 * 
	 * @return
	 */
	protected CacheManager getLocalCacheManager()
	{
		// 本地缓存不能在构造方法种初始化，会出现Spring循环依赖问题
		synchronized (this)
		{
			if (this._localCacheManager != null)
				return this._localCacheManager;

			CaffeineCacheManager cacheManager = new CaffeineCacheManager();

			String cacheSpec = this.environment.getProperty("spring.cache.caffeine.spec");
			if (!StringUtil.isEmpty(cacheSpec))
				cacheManager.setCacheSpecification(cacheSpec);

			this._localCacheManager = cacheManager;

			return this._localCacheManager;
		}
	}

	/**
	 * 设置系统使用的POI库的最小解压比率配置，
	 * 详细说明参考{@code application.properties}中的{@code poi.zipSecureFile.minInflateRatio}配置项说明。
	 * 
	 * @param ratio
	 */
	protected void setZipSecureFileMinInflateRatio(String ratio)
	{
		if (StringUtil.isEmpty(ratio))
			return;

		double rt = Double.valueOf(ratio);
		ZipSecureFile.setMinInflateRatio(rt);
	}

	/**
	 * 获取所有{@linkplain CreateUserEntityService}实例。
	 * 
	 * @param context
	 * @return
	 */
	protected List<CreateUserEntityService> getCreateUserEntityServices(ApplicationContext context)
	{
		Map<String, CreateUserEntityService> serviceMap = context.getBeansOfType(CreateUserEntityService.class);
		List<CreateUserEntityService> serviceList = new ArrayList<CreateUserEntityService>(serviceMap.size());
		serviceList.addAll(serviceMap.values());

		return serviceList;
	}
}
