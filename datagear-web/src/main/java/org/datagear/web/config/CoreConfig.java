/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.config;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.AbstractCsvDataSet;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.analysis.support.AbstractJsonDataSet;
import org.datagear.analysis.support.DataSetFmkTemplateResolver;
import org.datagear.analysis.support.DataSetFmkTemplateResolver.NameTemplateLoader;
import org.datagear.analysis.support.FileTplDashboardWidgetResManager;
import org.datagear.analysis.support.SqlDataSet;
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
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.SchemaGuardService;
import org.datagear.management.service.SchemaService;
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
import org.datagear.management.service.impl.DashboardSharePasswordCrypto;
import org.datagear.management.service.impl.DashboardShareSetServiceImpl;
import org.datagear.management.service.impl.DataSetEntityServiceImpl;
import org.datagear.management.service.impl.DataSetResDirectoryServiceImpl;
import org.datagear.management.service.impl.HtmlChartWidgetEntityServiceImpl;
import org.datagear.management.service.impl.HtmlTplDashboardWidgetEntityServiceImpl;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.SchemaGuardServiceImpl;
import org.datagear.management.service.impl.SchemaServiceImpl;
import org.datagear.management.service.impl.SqlHistoryServiceImpl;
import org.datagear.management.service.impl.UserPasswordEncoder;
import org.datagear.management.service.impl.UserServiceImpl;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.management.util.dialect.MbSqlDialectBuilder;
import org.datagear.management.util.typehandlers.DataFormatTypeHandler;
import org.datagear.management.util.typehandlers.LiteralBooleanTypeHandler;
import org.datagear.management.util.typehandlers.ResultDataFormatTypeHandler;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.support.DefaultDialectSource;
import org.datagear.persistence.support.DefaultPersistenceManager;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.html.HtmlFilter;
import org.datagear.util.sqlvalidator.InvalidPatternSqlValidator;
import org.datagear.util.sqlvalidator.SqlValidator;
import org.datagear.web.controller.LoginController;
import org.datagear.web.controller.RegisterController;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.json.jackson.FormatterDeserializer;
import org.datagear.web.json.jackson.FormatterSerializer;
import org.datagear.web.json.jackson.ObjectMapperBuilder;
import org.datagear.web.security.UserPasswordEncoderImpl;
import org.datagear.web.sqlpad.SqlPermissionValidator;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.DashboardSharePasswordCryptoImpl;
import org.datagear.web.util.DashboardSharePasswordCryptoImpl.EncryptType;
import org.datagear.web.util.DirectoryFactory;
import org.datagear.web.util.DirectoryHtmlChartPluginManagerInitializer;
import org.datagear.web.util.SqlDriverChecker;
import org.datagear.web.util.TableCache;
import org.datagear.web.util.XmlDriverEntityManagerInitializer;
import org.datagear.web.util.accesslatch.AccessLatch;
import org.datagear.web.util.accesslatch.IpLoginLatch;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 核心配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@EnableCaching
public class CoreConfig implements ApplicationListener<ContextRefreshedEvent>
{
	public static final String NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY = "dashboardGlobalResRootDirectory";
	
	public static final String INVALID_SQL_KEYWORDS_PREFIX_REGEX="regex:";

	private ApplicationPropertiesConfig applicationPropertiesConfig;

	private DataSourceConfig dataSourceConfig;

	private CacheServiceConfig cacheServiceConfig;

	@Autowired
	public CoreConfig(ApplicationPropertiesConfig applicationPropertiesConfig, DataSourceConfig dataSourceConfig,
			CacheServiceConfig cacheServiceConfig)
	{
		super();
		this.applicationPropertiesConfig = applicationPropertiesConfig;
		this.dataSourceConfig = dataSourceConfig;
		this.cacheServiceConfig = cacheServiceConfig;
	}

	public ApplicationPropertiesConfig getApplicationPropertiesConfig()
	{
		return applicationPropertiesConfig;
	}

	public void setApplicationPropertiesConfig(ApplicationPropertiesConfig applicationPropertiesConfig)
	{
		this.applicationPropertiesConfig = applicationPropertiesConfig;
	}

	public DataSourceConfig getDataSourceConfig()
	{
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfig dataSourceConfig)
	{
		this.dataSourceConfig = dataSourceConfig;
	}

	public CacheServiceConfig getCacheServiceConfig()
	{
		return cacheServiceConfig;
	}

	public void setCacheServiceConfig(CacheServiceConfig cacheServiceConfig)
	{
		this.cacheServiceConfig = cacheServiceConfig;
	}

	@Bean
	public MessageSource messageSource()
	{
		ResourceBundleMessageSource bean = new ResourceBundleMessageSource();
		bean.setBasename("org.datagear.web.i18n.message");
		bean.setDefaultEncoding(IOUtil.CHARSET_UTF_8);
		// i18n找不到指定语言的bundle时不使用操作系统默认语言重新查找，直接使用默认bundle。
		// 系统目前只有默认bundle（无后缀）、英语bundle（"en"后缀），如果设置为true（默认值），
		// 当操作系统语言是英语时，会导致切换语言不起作用，i18n始终会被定位至英语bundle
		bean.setFallbackToSystemLocale(false);

		return bean;
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
		DefaultFormattingConversionService bean = new DefaultFormattingConversionService(false);

		bean.addFormatter(this.sqlDateFormatter());
		bean.addFormatter(this.sqlTimeFormatter());
		bean.addFormatter(this.sqlTimestampFormatter());
		bean.addFormatter(this.dateFormatter());

		DefaultFormattingConversionService.addDefaultFormatters(bean);

		return bean;
	}

	@Bean
	public ObjectMapperBuilder objectMapperBuilder()
	{
		ObjectMapperBuilder bean = new ObjectMapperBuilder();

		FormatterSerializer<java.sql.Date> sqlDateSerializer = new FormatterSerializer<java.sql.Date>(
				this.sqlDateFormatter());
		FormatterSerializer<java.sql.Time> sqlTimeSerializer = new FormatterSerializer<java.sql.Time>(
				this.sqlTimeFormatter());
		FormatterSerializer<java.sql.Timestamp> sqlTimestampSerializer = new FormatterSerializer<java.sql.Timestamp>(
				this.sqlTimestampFormatter());
		FormatterSerializer<java.util.Date> dateSerializer = new FormatterSerializer<java.util.Date>(
				this.dateFormatter());

		bean.addJsonSerializer(java.sql.Date.class, sqlDateSerializer);
		bean.addJsonSerializer(java.sql.Time.class, sqlTimeSerializer);
		bean.addJsonSerializer(java.sql.Timestamp.class, sqlTimestampSerializer);
		bean.addJsonSerializer(java.util.Date.class, dateSerializer);

		FormatterDeserializer<java.sql.Date> sqlDateDeserializer = new FormatterDeserializer<java.sql.Date>(
				java.sql.Date.class, this.sqlDateFormatter());
		FormatterDeserializer<java.sql.Time> sqlTimeDeserializer = new FormatterDeserializer<java.sql.Time>(
				java.sql.Time.class, this.sqlTimeFormatter());
		FormatterDeserializer<java.sql.Timestamp> sqlTimestampDeserializer = new FormatterDeserializer<java.sql.Timestamp>(
				java.sql.Timestamp.class, this.sqlTimestampFormatter());
		FormatterDeserializer<java.util.Date> dateDeserializer = new FormatterDeserializer<java.util.Date>(
				java.sql.Timestamp.class, this.dateFormatter());

		bean.addJsonDeserializer(java.sql.Date.class, sqlDateDeserializer);
		bean.addJsonDeserializer(java.sql.Time.class, sqlTimeDeserializer);
		bean.addJsonDeserializer(java.sql.Timestamp.class, sqlTimestampDeserializer);
		bean.addJsonDeserializer(java.util.Date.class, dateDeserializer);

		return bean;
	}

	@Bean
	public File driverEntityManagerRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryDriver(), true);
	}

	@Bean
	public File tempDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryTemp(), true);
	}

	@Bean
	public File chartPluginRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryChartPlugin(), true);
	}

	@Bean
	public File dashboardRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryDashboard(), true);
	}

	@Bean(NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	public File dashboardGlobalResRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryDashboardGlobalRes(), true);
	}

	@Bean
	public File resetPasswordCheckFileDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryResetPasswordCheckFile(), true);
	}

	@Bean
	public File dataSetRootDirectory()
	{
		return createDirectory(getApplicationProperties().getDirectoryDataSet(), true);
	}

	@Bean
	public File schemaUrlBuilderScriptFile()
	{
		return FileUtil.getFile(getApplicationProperties().getSchemaUrlBuilderScriptFile());
	}

	@Bean
	public File builtinChartPluginLastModifiedFile()
	{
		return FileUtil.getFile(getApplicationProperties().getBuiltinChartPluginLastModifiedFile());
	}

	@Bean
	public CloseableHttpClient httpClient()
	{
		return HttpClients.createDefault();
	}

	protected File createDirectory(String directoryName, boolean createIfInexistence)
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

	@Bean(initMethod = "upgrade")
	public DbVersionManager dbVersionManager()
	{
		DbVersionManager bean = new DbVersionManager(this.dataSourceConfig.dataSource());
		return bean;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory()
	{
		try
		{
			SqlSessionFactoryBean bean = this.getSqlSessionFactoryBean();
			return bean.getObject();
		}
		catch (Exception e)
		{
			throw new BeanInitializationException("Init " + SqlSessionFactory.class + " failed", e);
		}
	}
	
	protected SqlSessionFactoryBean getSqlSessionFactoryBean() throws Exception
	{
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		
		bean.setDataSource(this.dataSourceConfig.dataSource());
		bean.setMapperLocations(this.getSqlSessionMapperResources());
		bean.setTypeHandlers(new TypeHandler<?>[] { new LiteralBooleanTypeHandler(), new DataFormatTypeHandler(),
				new ResultDataFormatTypeHandler() });
		
		return bean;
	}
	
	protected Resource[] getSqlSessionMapperResources() throws Exception
	{
		PathMatchingResourcePatternResolver resolver = this.resourcePatternResolver();
		Resource[] mapperResources = resolver.getResources(
				org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
				+ "org/datagear/management/mapper/*.xml");
		
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

		MbSqlDialectBuilder builder = new MbSqlDialectBuilder();
		builder.setDialectName(dialectName);

		return builder;
	}

	@Bean
	public MbSqlDialect mbSqlDialect()
	{
		try
		{
			return this.mbSqlDialectBuilder().build(this.dataSourceConfig.dataSource());
		}
		catch (SQLException e)
		{
			throw new BeanInitializationException("Init " + MbSqlDialect.class + " failed", e);
		}
	}

	@Bean
	public DBMetaResolver dbMetaResolver()
	{
		GenericDBMetaResolver bean = new GenericDBMetaResolver();
		return bean;
	}

	@Bean(destroyMethod = "releaseAll")
	public XmlDriverEntityManager driverEntityManager()
	{
		XmlDriverEntityManager bean = new XmlDriverEntityManager(driverEntityManagerRootDirectory());
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
		DefaultConnectionSource bean = new DefaultConnectionSource(this.driverEntityManager());
		bean.setDriverChecker(new SqlDriverChecker(this.dbMetaResolver()));

		GenericPropertiesProcessor genericPropertiesProcessor = new GenericPropertiesProcessor();
		genericPropertiesProcessor.setDevotedPropertiesProcessors(
				Arrays.asList(new MySqlDevotedPropertiesProcessor(), new OracleDevotedPropertiesProcessor()));

		bean.setPropertiesProcessor(genericPropertiesProcessor);

		return bean;
	}

	@Bean(initMethod = "init")
	public TableCache tableCache()
	{
		TableCache bean = new TableCache();
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
		@SuppressWarnings("deprecation")
		org.springframework.security.crypto.password.StandardPasswordEncoder bean =
				//
				new org.springframework.security.crypto.password.StandardPasswordEncoder();

		return bean;
	}

	@Bean
	public UserPasswordEncoder userPasswordEncoder()
	{
		UserPasswordEncoderImpl bean = new UserPasswordEncoderImpl(this.passwordEncoder());
		return bean;
	}

	@Bean
	public HtmlFilter htmlFilter()
	{
		HtmlFilter bean = new HtmlFilter();
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
	public SchemaService schemaService()
	{
		SchemaServiceImpl bean = new SchemaServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationService(), this.driverEntityManager(), this.userService(), this.schemaGuardService());

		return bean;
	}

	@Bean
	public SchemaGuardService schemaGuardService()
	{
		SchemaGuardServiceImpl bean = new SchemaGuardServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());

		return bean;
	}

	@Bean
	public UserService userService()
	{
		UserServiceImpl bean = new UserServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(), this.roleService());
		bean.setUserPasswordEncoder(this.userPasswordEncoder());

		return bean;
	}

	@Bean
	public RoleService roleService()
	{
		RoleServiceImpl bean = new RoleServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());
		return bean;
	}

	@Bean
	public DataSetEntityService dataSetEntityService()
	{
		DataSetEntityServiceImpl bean = new DataSetEntityServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationService(), this.connectionSource(), this.schemaService(),
				this.analysisProjectService(), this.userService(), this.dataSetResDirectoryService(),
				this.dataSetRootDirectory(),
				this.httpClient());

		bean.setDataSetResourceDataCacheService(this.cacheServiceConfig
				.createCacheService(DataSetEntityService.class.getName() + ".dataSetResourceDataCacheService"));

		bean.setSqlDataSetSqlValidator(this.sqlDataSetSqlValidator());

		return bean;
	}

	@Bean
	public DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager()
	{
		HtmlChartPluginLoader htmlChartPluginLoader = new HtmlChartPluginLoader();
		htmlChartPluginLoader.setTmpDirectory(this.tempDirectory());

		DirectoryHtmlChartPluginManager bean = new DirectoryHtmlChartPluginManager(this.chartPluginRootDirectory(),
				htmlChartPluginLoader);
		bean.setTmpDirectory(this.tempDirectory());

		return bean;
	}

	@Bean(initMethod = "init")
	public DirectoryHtmlChartPluginManagerInitializer directoryHtmlChartPluginManagerInitializer()
	{
		DirectoryHtmlChartPluginManagerInitializer bean = new DirectoryHtmlChartPluginManagerInitializer(
				this.resourcePatternResolver(), this.directoryHtmlChartPluginManager(),
				this.tempDirectory(), this.builtinChartPluginLastModifiedFile());
		
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
		DashboardShareSetService bean = new DashboardShareSetServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.dashboardSharePasswordCrypto());
		return bean;
	}

	@Bean
	public DashboardSharePasswordCrypto dashboardSharePasswordCrypto()
	{
		EncryptType encryptType = (getApplicationProperties().isDashboardSharePasswordCryptoDisabled() ?  EncryptType.NOOP : EncryptType.STD);
		
		DashboardSharePasswordCrypto bean = new DashboardSharePasswordCryptoImpl(encryptType,
				getApplicationProperties().getDashboardSharePasswordCryptoSecretKey(),
				getApplicationProperties().getDashboardSharePasswordCryptoSalt());
		
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
	public DataSetResDirectoryService dataSetResDirectoryService()
	{
		DataSetResDirectoryService bean = new DataSetResDirectoryServiceImpl(this.sqlSessionFactory(),
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
		return new ChangelogResolver();
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
				this.sqlHistoryService(), this.sqlSelectManager());
		bean.setSqlPermissionValidator(this.sqlPermissionValidator());

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
		AccessLatch accessLatch = new AccessLatch(getApplicationProperties().getIpLoginLatchSeconds(),
				getApplicationProperties().getIpLoginLatchFrequency());

		IpLoginLatch bean = new IpLoginLatch(accessLatch);

		return bean;
	}

	@Bean
	public UsernameLoginLatch usernameLoginLatch()
	{
		AccessLatch accessLatch = new AccessLatch(getApplicationProperties().getUsernameLoginLatchSeconds(),
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

	@SuppressWarnings("rawtypes")
	protected void initCacheServices(ApplicationContext context)
	{
		Map<String, AbstractMybatisEntityService> entityServices = context
				.getBeansOfType(AbstractMybatisEntityService.class);

		for (AbstractMybatisEntityService es : entityServices.values())
		{
			es.setCacheService(this.cacheServiceConfig.createCacheService(es.getClass()));

			if (es instanceof AbstractMybatisDataPermissionEntityService<?, ?>)
			{
				((AbstractMybatisDataPermissionEntityService<?, ?>) es)
						.setPermissionCacheService(this.cacheServiceConfig.createPermissionCacheService(es.getClass()));
			}
		}

		DataSetFmkTemplateResolver generalTemplateResolver = AbstractDataSet.GENERAL_TEMPLATE_RESOLVER;
		DataSetFmkTemplateResolver csvTemplateResolver = AbstractCsvDataSet.CSV_TEMPLATE_RESOLVER;
		DataSetFmkTemplateResolver jsonTemplateResolver = AbstractJsonDataSet.JSON_TEMPLATE_RESOLVER;
		DataSetFmkTemplateResolver sqlTemplateResolver = SqlDataSet.SQL_TEMPLATE_RESOLVER;

		generalTemplateResolver.getNameTemplateLoader().setCacheService(
				this.cacheServiceConfig.createCacheService(NameTemplateLoader.class.getName() + ".general"));
		csvTemplateResolver.getNameTemplateLoader().setCacheService(
				this.cacheServiceConfig.createCacheService(NameTemplateLoader.class.getName() + ".csv"));
		jsonTemplateResolver.getNameTemplateLoader().setCacheService(
				this.cacheServiceConfig.createCacheService(NameTemplateLoader.class.getName() + ".json"));
		sqlTemplateResolver.getNameTemplateLoader().setCacheService(
				this.cacheServiceConfig.createCacheService(NameTemplateLoader.class.getName() + ".sql"));

		Map<String, HtmlTplDashboardWidgetHtmlRenderer> renderers = context
				.getBeansOfType(HtmlTplDashboardWidgetHtmlRenderer.class);

		for (HtmlTplDashboardWidgetHtmlRenderer renderer : renderers.values())
		{
			renderer.setCacheService(this.cacheServiceConfig.createCacheService(renderer.getClass()));
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
	public static List<CreateUserEntityService> getCreateUserEntityServices(ApplicationContext context)
	{
		Map<String, CreateUserEntityService> serviceMap = context.getBeansOfType(CreateUserEntityService.class);
		List<CreateUserEntityService> serviceList = new ArrayList<CreateUserEntityService>(serviceMap.size());
		serviceList.addAll(serviceMap.values());

		return serviceList;
	}
}
