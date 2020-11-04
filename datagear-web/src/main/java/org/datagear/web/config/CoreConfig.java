/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.ibatis.session.SqlSessionFactory;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.Extension;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.datagear.analysis.support.FileTemplateDashboardWidgetResManager;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.TemplateImportHtmlChartPluginVarNameResolver;
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
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.RoleUserService;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.management.service.UserService;
import org.datagear.management.service.impl.AnalysisProjectServiceImpl;
import org.datagear.management.service.impl.AuthorizationServiceImpl;
import org.datagear.management.service.impl.DataSetEntityServiceImpl;
import org.datagear.management.service.impl.DataSetResDirectoryServiceImpl;
import org.datagear.management.service.impl.HtmlChartWidgetEntityServiceImpl;
import org.datagear.management.service.impl.HtmlTplDashboardWidgetEntityServiceImpl;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.RoleUserServiceImpl;
import org.datagear.management.service.impl.SchemaServiceImpl;
import org.datagear.management.service.impl.SqlHistoryServiceImpl;
import org.datagear.management.service.impl.UserPasswordEncoder;
import org.datagear.management.service.impl.UserServiceImpl;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.meta.resolver.GenericDBMetaResolver;
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.support.DefaultDialectSource;
import org.datagear.persistence.support.DefaultPersistenceManager;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.util.IOUtil;
import org.datagear.web.cometd.CustomJacksonJSONContextServer;
import org.datagear.web.cometd.dataexchange.DataExchangeCometdService;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.json.jackson.LocaleDateSerializer;
import org.datagear.web.json.jackson.LocaleSqlDateSerializer;
import org.datagear.web.json.jackson.LocaleSqlTimeSerializer;
import org.datagear.web.json.jackson.LocaleSqlTimestampSerializer;
import org.datagear.web.json.jackson.ObjectMapperBuilder;
import org.datagear.web.json.jackson.ObjectMapperBuilder.JsonSerializerConfig;
import org.datagear.web.security.UserPasswordEncoderImpl;
import org.datagear.web.sqlpad.SqlpadCometdService;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.util.BayeuxServerFactory;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.DirectoryFactory;
import org.datagear.web.util.DirectoryHtmlChartPluginManagerInitializer;
import org.datagear.web.util.SqlDriverChecker;
import org.datagear.web.util.TableCache;
import org.datagear.web.util.XmlDriverEntityManagerInitializer;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 核心配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@EnableTransactionManagement
public class CoreConfig implements InitializingBean
{
	public static final String NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "chartShowHtmlTplDashboardWidgetHtmlRenderer";

	public static final String NAME_DASHBOARD_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "htmlTplDashboardWidgetRenderer";

	private DataSourceConfig dataSourceConfig;

	private Environment environment;

	@Autowired
	public CoreConfig(DataSourceConfig dataSourceConfig, Environment environment)
	{
		super();
		this.dataSourceConfig = dataSourceConfig;
		this.environment = environment;
	}

	public DataSourceConfig getDataSourceConfig()
	{
		return dataSourceConfig;
	}

	public void setDataSourceConfig(DataSourceConfig dataSourceConfig)
	{
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

	@Bean
	public MessageSource messageSource()
	{
		ResourceBundleMessageSource bean = new ResourceBundleMessageSource();
		bean.setBasename("org.datagear.web.messages.message");
		bean.setDefaultEncoding(IOUtil.CHARSET_UTF_8);

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

		LocaleDateSerializer localeDateSerializer = new LocaleDateSerializer();
		localeDateSerializer.setDateFormatter(this.dateFormatter());

		LocaleSqlDateSerializer localeSqlDateSerializer = new LocaleSqlDateSerializer();
		localeSqlDateSerializer.setSqlDateFormatter(this.sqlDateFormatter());

		LocaleSqlTimeSerializer localeSqlTimeSerializer = new LocaleSqlTimeSerializer();
		localeSqlTimeSerializer.setSqlTimeFormatter(this.sqlTimeFormatter());

		LocaleSqlTimestampSerializer localeSqlTimestampSerializer = new LocaleSqlTimestampSerializer();
		localeSqlTimestampSerializer.setSqlTimestampFormatter(this.sqlTimestampFormatter());

		List<JsonSerializerConfig> jsonSerializerConfigs = new ArrayList<>();

		jsonSerializerConfigs.add(new JsonSerializerConfig(java.util.Date.class, localeDateSerializer));
		jsonSerializerConfigs.add(new JsonSerializerConfig(java.sql.Date.class, localeSqlDateSerializer));
		jsonSerializerConfigs.add(new JsonSerializerConfig(java.sql.Time.class, localeSqlTimeSerializer));
		jsonSerializerConfigs.add(new JsonSerializerConfig(java.sql.Timestamp.class, localeSqlTimestampSerializer));

		bean.setJsonSerializerConfigs(jsonSerializerConfigs);

		return bean;
	}

	@Bean
	public File driverEntityManagerRootDirectory()
	{
		return createDirectory(environment.getProperty("directory.driver"), true);
	}

	@Bean
	public File tempDirectory()
	{
		return createDirectory(environment.getProperty("directory.temp"), true);
	}

	@Bean
	public File chartPluginRootDirectory()
	{
		return createDirectory(environment.getProperty("directory.chartPlugin"), true);
	}

	@Bean
	public File dashboardRootDirectory()
	{
		return createDirectory(environment.getProperty("directory.dashboard"), true);
	}

	@Bean
	public File resetPasswordCheckFileDirectory()
	{
		return createDirectory(environment.getProperty("directory.resetPasswordCheckFile"), true);
	}

	@Bean
	public File dataSetRootDirectory()
	{
		return createDirectory(environment.getProperty("directory.dataSet"), true);
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
	public PlatformTransactionManager transactionManager()
	{
		DataSourceTransactionManager bean = new DataSourceTransactionManager(this.dataSourceConfig.dataSource());
		return bean;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory()
	{
		try
		{
			PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
			Resource[] mapperResources = pathResolver.getResources("classpath*:org/datagear/management/mapper/*.xml");

			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
			bean.setDataSource(this.dataSourceConfig.dataSource());
			bean.setMapperLocations(mapperResources);
			return bean.getObject();
		}
		catch (Exception e)
		{
			throw new BeanInitializationException("Init " + SqlSessionFactory.class + " failed", e);
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
	public List<DataPermissionEntityService<?, ?>> authorizationResourceServices()
	{
		return new ArrayList<>();
	}

	@Bean
	public AuthorizationService authorizationService()
	{
		AuthorizationServiceImpl bean = new AuthorizationServiceImpl(this.sqlSessionFactory(),
				this.authorizationResourceServices());

		return bean;
	}

	@Bean
	public SchemaService schemaService()
	{
		SchemaServiceImpl bean = new SchemaServiceImpl(this.sqlSessionFactory(), this.driverEntityManager(),
				this.authorizationService());

		return bean;
	}

	@Bean
	public UserService userService()
	{
		UserServiceImpl bean = new UserServiceImpl(this.sqlSessionFactory());
		bean.setUserPasswordEncoder(this.userPasswordEncoder());
		return bean;
	}

	@Bean
	public RoleService roleService()
	{
		RoleServiceImpl bean = new RoleServiceImpl(this.sqlSessionFactory());
		return bean;
	}

	@Bean
	public RoleUserService roleUserService()
	{
		RoleUserServiceImpl bean = new RoleUserServiceImpl(this.sqlSessionFactory());
		return bean;
	}

	@Bean
	public DataSetEntityService dataSetEntityService()
	{
		DataSetEntityServiceImpl bean = new DataSetEntityServiceImpl(this.sqlSessionFactory(), this.connectionSource(),
				this.schemaService(), this.authorizationService(), this.dataSetRootDirectory(), this.httpClient());
		return bean;
	}

	@Bean
	public FileTemplateDashboardWidgetResManager templateDashboardWidgetResManager()
	{
		FileTemplateDashboardWidgetResManager bean = new FileTemplateDashboardWidgetResManager(
				this.dashboardRootDirectory());
		return bean;
	}

	@Bean
	public DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager()
	{
		DirectoryHtmlChartPluginManager bean = new DirectoryHtmlChartPluginManager(this.chartPluginRootDirectory());
		return bean;
	}

	@Bean(initMethod = "init")
	public DirectoryHtmlChartPluginManagerInitializer directoryHtmlChartPluginManagerInitializer()
	{
		DirectoryHtmlChartPluginManagerInitializer bean = new DirectoryHtmlChartPluginManagerInitializer(
				this.directoryHtmlChartPluginManager());
		return bean;
	}

	@Bean
	public HtmlChartWidgetEntityService htmlChartWidgetEntityService()
	{
		HtmlChartWidgetEntityServiceImpl bean = new HtmlChartWidgetEntityServiceImpl(this.sqlSessionFactory(),
				this.directoryHtmlChartPluginManager(), this.dataSetEntityService(), this.authorizationService());

		return bean;
	}

	@Bean(NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer)
	public HtmlTplDashboardWidgetHtmlRenderer chartShowHtmlTplDashboardWidgetHtmlRenderer()
	{
		FileTemplateDashboardWidgetResManager resManager = new FileTemplateDashboardWidgetResManager(
				this.dashboardRootDirectory());
		resManager.setTemplateAsContent(true);

		HtmlTplDashboardWidgetHtmlRenderer bean = new HtmlTplDashboardWidgetHtmlRenderer(resManager,
				this.htmlChartWidgetEntityService());

		bean.setDashboardImports(this.buildHtmlTplDashboardWidgetRendererd_dshboardImports());
		bean.setImportHtmlChartPluginVarNameResolver(
				this.buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver());

		return bean;
	}

	@Bean(NAME_DASHBOARD_SHOW_HtmlTplDashboardWidgetHtmlRenderer)
	public HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetRenderer()
	{
		HtmlTplDashboardWidgetHtmlRenderer bean = new HtmlTplDashboardWidgetHtmlRenderer(
				this.templateDashboardWidgetResManager(), this.htmlChartWidgetEntityService());

		bean.setDashboardImports(this.buildHtmlTplDashboardWidgetRendererd_dshboardImports());
		bean.setImportHtmlChartPluginVarNameResolver(
				this.buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver());

		return bean;
	}

	protected List<HtmlTplDashboardImport> buildHtmlTplDashboardWidgetRendererd_dshboardImports()
	{
		List<HtmlTplDashboardImport> imports = new ArrayList<>();

		String cp = HtmlTplDashboardWidgetRenderer.DEFAULT_CONTEXT_PATH_PLACE_HOLDER;
		String vp = HtmlTplDashboardWidgetRenderer.DEFAULT_VERSION_PLACE_HOLDER;

		String staticPrefix = cp + "/static";
		String cssPrefix = staticPrefix + "/css";
		String scriptPrefix = staticPrefix + "/script";

		// CSS
		imports.add(
				new HtmlTplDashboardImport("dataTableStyle", "<link type='text/css' res-name='dataTableStyle' href='"
						+ cssPrefix + "/DataTables-1.10.18/datatables.min.css?v=" + vp + "'  rel='stylesheet' />"));

		imports.add(new HtmlTplDashboardImport("datetimepickerStyle",
				"<link type='text/css' res-name='datetimepickerStyle' href='" + scriptPrefix
						+ "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css?v=" + vp
						+ "'  rel='stylesheet' />"));

		imports.add(
				new HtmlTplDashboardImport("dashboardStyle", "<link type='text/css' res-name='dashboardStyle' href='"
						+ cssPrefix + "/analysis.css?v=" + vp + "'  rel='stylesheet' />"));

		// JS
		imports.add(new HtmlTplDashboardImport("jquery", "<script type='text/javascript' res-name='jquery' src='"
				+ scriptPrefix + "/jquery/jquery-1.12.4.min.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("echarts", "<script type='text/javascript' res-name='echarts' src='"
				+ scriptPrefix + "/echarts-4.7.0/echarts.min.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("echarts-wordcloud",
				"<script type='text/javascript' res-name='echarts-wordcloud' src='" + scriptPrefix
						+ "/echarts-wordcloud-1.1.2/echarts-wordcloud.min.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("dataTable", "<script type='text/javascript' res-name='dataTable' src='"
				+ scriptPrefix + "/DataTables-1.10.18/datatables.min.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("datetimepicker",
				"<script type='text/javascript' res-name='datetimepicker' src='" + scriptPrefix
						+ "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("chartFactory",
				"<script type='text/javascript' res-name='chartFactory' src='" + scriptPrefix
						+ "/datagear-chartFactory.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("dashboardFactory",
				"<script type='text/javascript' res-name='dashboardFactory' src='" + scriptPrefix
						+ "/datagear-dashboardFactory.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("chartSupport",
				"<script type='text/javascript' res-name='chartSupport' src='" + scriptPrefix
						+ "/datagear-chartSupport.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("chartForm", "<script type='text/javascript' res-name='chartForm' src='"
				+ scriptPrefix + "/datagear-chartForm.js?v=" + vp + "'></script>"));

		imports.add(new HtmlTplDashboardImport("chartPluginManager",
				"<script type='text/javascript' res-name='chartPluginManager' src='" + cp
						+ "/analysis/chartPlugin/chartPluginManager.js?v=" + vp + "'></script>"));

		return imports;
	}

	protected TemplateImportHtmlChartPluginVarNameResolver buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver()
	{
		String pp = TemplateImportHtmlChartPluginVarNameResolver.PLACEHOLDER_CHART_PLUGIN_ID;

		TemplateImportHtmlChartPluginVarNameResolver resolver = new TemplateImportHtmlChartPluginVarNameResolver(
				"chartFactory.chartPluginManager.get('" + pp + "')");

		return resolver;
	}

	@Bean
	public HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService()
	{
		HtmlTplDashboardWidgetEntityServiceImpl bean = new HtmlTplDashboardWidgetEntityServiceImpl(
				this.sqlSessionFactory(), this.htmlTplDashboardWidgetRenderer(), this.authorizationService());

		return bean;
	}

	@Bean
	public AnalysisProjectService analysisProjectService()
	{
		AnalysisProjectService bean = new AnalysisProjectServiceImpl(this.sqlSessionFactory());
		return bean;
	}

	@Bean
	public DataSetResDirectoryService dataSetResDirectoryService()
	{
		DataSetResDirectoryService bean = new DataSetResDirectoryServiceImpl(this.sqlSessionFactory());
		return bean;
	}

	@Bean
	public SqlHistoryService sqlHistoryService()
	{
		SqlHistoryServiceImpl bean = new SqlHistoryServiceImpl(this.sqlSessionFactory());
		return bean;
	}

	@Bean
	public ChangelogResolver changelogResolver()
	{
		return new ChangelogResolver();
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public BayeuxServer bayeuxServer()
	{
		return buildBayeuxServerFactory().getBayeuxServer();
	}

	protected BayeuxServerFactory buildBayeuxServerFactory()
	{
		BayeuxServerFactory bean = new BayeuxServerFactory();

		Map<String, Object> options = new HashMap<>();
		options.put("logLevel", 3);
		options.put("timeout", 30000);
		options.put("maxInterval", 120000);
		options.put("jsonContext", new CustomJacksonJSONContextServer(this.objectMapperBuilder()));

		List<Extension> extensions = new ArrayList<>();
		extensions.add(new AcknowledgedMessagesExtension());

		bean.setOptions(options);
		bean.setExtensions(extensions);

		return bean;
	}

	@Bean
	public SqlSelectManager sqlSelectManager()
	{
		SqlSelectManager bean = new SqlSelectManager(this.dbMetaResolver());
		return bean;
	}

	@Bean
	public SqlpadCometdService sqlpadCometdService()
	{
		SqlpadCometdService bean = new SqlpadCometdService(this.bayeuxServer());
		return bean;
	}

	@Bean
	public SqlpadExecutionService sqlpadExecutionService()
	{
		SqlpadExecutionService bean = new SqlpadExecutionService(this.connectionSource(), this.messageSource(),
				this.sqlpadCometdService(), this.sqlHistoryService(), this.sqlSelectManager());
		return bean;
	}

	@Bean
	public List<DevotedDataExchangeService<?>> devotedDataExchangeServices()
	{
		List<DevotedDataExchangeService<?>> bean = new ArrayList<>();

		bean.add(new CsvDataImportService(this.dbMetaResolver()));
		bean.add(new CsvDataExportService(this.dbMetaResolver()));
		bean.add(new SqlDataImportService());
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
		GenericDataExchangeService bean = new GenericDataExchangeService(this.devotedDataExchangeServices());
		return bean;
	}

	@Bean
	public DataExchangeCometdService dataExchangeCometdService()
	{
		DataExchangeCometdService bean = new DataExchangeCometdService(this.bayeuxServer());
		return bean;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		// 处理循环依赖
		{
			List<DataPermissionEntityService<?, ?>> resourceServices = this.authorizationResourceServices();

			resourceServices.add(this.schemaService());
			resourceServices.add(this.dataSetEntityService());
			resourceServices.add(this.htmlChartWidgetEntityService());
			resourceServices.add(this.htmlTplDashboardWidgetEntityService());
			resourceServices.add(this.analysisProjectService());
		}

		// 处理循环依赖
		{
			List<DevotedDataExchangeService<?>> devotedDataExchangeServices = this.devotedDataExchangeServices();
			devotedDataExchangeServices.add(this.batchDataExchangeService());
		}
	}
}
