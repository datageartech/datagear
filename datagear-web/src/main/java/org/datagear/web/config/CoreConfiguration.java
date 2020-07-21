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

import javax.servlet.Filter;

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
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.RoleService;
import org.datagear.management.service.RoleUserService;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlDataSetEntityService;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.management.service.UserService;
import org.datagear.management.service.impl.AuthorizationServiceImpl;
import org.datagear.management.service.impl.HtmlChartWidgetEntityServiceImpl;
import org.datagear.management.service.impl.HtmlTplDashboardWidgetEntityServiceImpl;
import org.datagear.management.service.impl.RoleServiceImpl;
import org.datagear.management.service.impl.RoleUserServiceImpl;
import org.datagear.management.service.impl.SchemaServiceImpl;
import org.datagear.management.service.impl.SqlDataSetEntityServiceImpl;
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
import org.datagear.web.cometd.CustomJacksonJSONContextServer;
import org.datagear.web.cometd.dataexchange.DataExchangeCometdService;
import org.datagear.web.convert.CustomFormattingConversionServiceFactoryBean;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.json.jackson.LocaleDateSerializer;
import org.datagear.web.json.jackson.LocaleSqlDateSerializer;
import org.datagear.web.json.jackson.LocaleSqlTimeSerializer;
import org.datagear.web.json.jackson.LocaleSqlTimestampSerializer;
import org.datagear.web.json.jackson.ObjectMapperFactory;
import org.datagear.web.json.jackson.ObjectMapperFactory.JsonSerializerConfig;
import org.datagear.web.scheduling.DeleteExpiredFileJob;
import org.datagear.web.security.UserPasswordEncoderImpl;
import org.datagear.web.sqlpad.SqlpadCometdService;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.util.BayeuxServerFactory;
import org.datagear.web.util.ChangelogResolver;
import org.datagear.web.util.DirectoryFactory;
import org.datagear.web.util.DirectoryHtmlChartPluginManagerInitializer;
import org.datagear.web.util.SqlDriverChecker;
import org.datagear.web.util.TableCache;
import org.datagear.web.util.WebContextPath;
import org.datagear.web.util.WebContextPathFilter;
import org.datagear.web.util.XmlDriverEntityManagerInitializer;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.support.ServletContextAttributeExporter;

/**
 * 核心配置。
 * <p>
 * 依赖配置：{@linkplain PropertiesConfiguration}、{@linkplain DataSourceConfiguration}。
 * </p>
 * <p>
 * 注：依赖配置需要手动加载。
 * </p>
 * 
 * @author datagear@163.com
 */
@Configuration
@EnableTransactionManagement
public class CoreConfiguration implements InitializingBean
{
	public static final String NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "chartShowHtmlTplDashboardWidgetHtmlRenderer";

	public static final String NAME_DASHBOARD_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "htmlTplDashboardWidgetRenderer";

	@Autowired
	private DataSourceConfiguration dataSourceConfiguration;

	@Autowired
	private Environment environment;

	public CoreConfiguration()
	{
	}

	public CoreConfiguration(DataSourceConfiguration dataSourceConfiguration, Environment environment)
	{
		super();
		this.dataSourceConfiguration = dataSourceConfiguration;
		this.environment = environment;
	}

	public DataSourceConfiguration getDataSourceConfiguration()
	{
		return dataSourceConfiguration;
	}

	public void setDataSourceConfiguration(DataSourceConfiguration dataSourceConfiguration)
	{
		this.dataSourceConfiguration = dataSourceConfiguration;
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
	public Filter webContextPathFilter()
	{
		WebContextPathFilter bean = new WebContextPathFilter();

		WebContextPath webContextPath = new WebContextPath();
		webContextPath.setSubContextPath(environment.getProperty("subContextPath"));

		bean.setWebContextPath(webContextPath);

		return bean;
	}

	@Bean
	public MessageSource messageSource()
	{
		ResourceBundleMessageSource bean = new ResourceBundleMessageSource();
		bean.setBasename("org.datagear.web.locales.datagear");

		return bean;
	}

	@Bean
	public File driverEntityManagerRootDirectory() throws IOException
	{
		return createDirectory(environment.getProperty("directory.driver"), true);
	}

	@Bean
	public File tempDirectory() throws IOException
	{
		return createDirectory(environment.getProperty("directory.temp"), true);
	}

	@Bean
	public File chartPluginRootDirectory() throws IOException
	{
		return createDirectory(environment.getProperty("directory.chartPlugin"), true);
	}

	@Bean
	public File dashboardRootDirectory() throws IOException
	{
		return createDirectory(environment.getProperty("directory.dashboard"), true);
	}

	@Bean
	public File resetPasswordCheckFileDirectory() throws IOException
	{
		return createDirectory(environment.getProperty("directory.resetPasswordCheckFile"), true);
	}

	protected File createDirectory(String directoryName, boolean createIfInexistence) throws IOException
	{
		DirectoryFactory bean = new DirectoryFactory();
		bean.setDirectoryName(directoryName);
		bean.setCreateIfInexistence(createIfInexistence);
		bean.init();

		return bean.getDirectory();
	}

	@Bean(initMethod = "upgrade")
	public DbVersionManager dbVersionManager()
	{
		DbVersionManager bean = new DbVersionManager(this.dataSourceConfiguration.dataSource());
		return bean;
	}

	@Bean
	public PlatformTransactionManager transactionManager()
	{
		DataSourceTransactionManager bean = new DataSourceTransactionManager(this.dataSourceConfiguration.dataSource());
		return bean;
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactory()
	{
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(this.dataSourceConfiguration.dataSource());
		bean.setMapperLocations(
				new Resource[] { new ClassPathResource("classpath*:org/datagear/management/mapper/*.xml") });

		return bean;
	}

	@Bean
	public DBMetaResolver dbMetaResolver()
	{
		GenericDBMetaResolver bean = new GenericDBMetaResolver();
		return bean;
	}

	@Bean(destroyMethod = "releaseAll")
	public XmlDriverEntityManager driverEntityManager() throws IOException
	{
		XmlDriverEntityManager bean = new XmlDriverEntityManager(driverEntityManagerRootDirectory());
		return bean;
	}

	@Bean(initMethod = "init")
	public XmlDriverEntityManagerInitializer xmlDriverEntityManagerInitializer() throws IOException
	{
		XmlDriverEntityManagerInitializer bean = new XmlDriverEntityManagerInitializer(this.driverEntityManager());

		return bean;
	}

	@Bean(destroyMethod = "close")
	public ConnectionSource connectionSource() throws IOException
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
		StandardPasswordEncoder bean = new StandardPasswordEncoder();
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
	public AuthorizationService authorizationService() throws Exception
	{
		AuthorizationServiceImpl bean = new AuthorizationServiceImpl(this.sqlSessionFactory().getObject(),
				this.authorizationResourceServices());

		return bean;
	}

	@Bean
	public SchemaService schemaService() throws Exception
	{
		SchemaServiceImpl bean = new SchemaServiceImpl(this.sqlSessionFactory().getObject(), this.driverEntityManager(),
				this.authorizationService());

		return bean;
	}

	@Bean
	public UserService userService() throws Exception
	{
		UserServiceImpl bean = new UserServiceImpl(this.sqlSessionFactory().getObject());
		bean.setUserPasswordEncoder(this.userPasswordEncoder());
		return bean;
	}

	@Bean
	public RoleService roleService() throws Exception
	{
		RoleServiceImpl bean = new RoleServiceImpl(this.sqlSessionFactory().getObject());
		return bean;
	}

	@Bean
	public RoleUserService roleUserService() throws Exception
	{
		RoleUserServiceImpl bean = new RoleUserServiceImpl(this.sqlSessionFactory().getObject());
		return bean;
	}

	@Bean
	public SqlDataSetEntityService sqlDataSetEntityService() throws Exception
	{
		SqlDataSetEntityServiceImpl bean = new SqlDataSetEntityServiceImpl(this.sqlSessionFactory().getObject(),
				this.connectionSource(), this.schemaService(), this.authorizationService());
		return bean;
	}

	@Bean
	public FileTemplateDashboardWidgetResManager templateDashboardWidgetResManager() throws IOException
	{
		FileTemplateDashboardWidgetResManager bean = new FileTemplateDashboardWidgetResManager(
				this.dashboardRootDirectory());
		return bean;
	}

	@Bean
	public DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager() throws IOException
	{
		DirectoryHtmlChartPluginManager bean = new DirectoryHtmlChartPluginManager(this.chartPluginRootDirectory());
		return bean;
	}

	@Bean(initMethod = "init")
	public DirectoryHtmlChartPluginManagerInitializer directoryHtmlChartPluginManagerInitializer() throws IOException
	{
		DirectoryHtmlChartPluginManagerInitializer bean = new DirectoryHtmlChartPluginManagerInitializer(
				this.directoryHtmlChartPluginManager());
		return bean;
	}

	@Bean
	public HtmlChartWidgetEntityService htmlChartWidgetEntityService() throws Exception
	{
		HtmlChartWidgetEntityServiceImpl bean = new HtmlChartWidgetEntityServiceImpl(
				this.sqlSessionFactory().getObject(), this.directoryHtmlChartPluginManager(),
				this.sqlDataSetEntityService(), this.authorizationService());

		return bean;
	}

	@Bean(NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer)
	public HtmlTplDashboardWidgetHtmlRenderer chartShowHtmlTplDashboardWidgetHtmlRenderer() throws Exception
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
	public HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetRenderer() throws Exception
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
	public HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService() throws Exception
	{
		HtmlTplDashboardWidgetEntityServiceImpl bean = new HtmlTplDashboardWidgetEntityServiceImpl(
				this.sqlSessionFactory().getObject(), this.htmlTplDashboardWidgetRenderer(),
				this.authorizationService());

		return bean;
	}

	@Bean
	public SqlHistoryService sqlHistoryService() throws Exception
	{
		SqlHistoryServiceImpl bean = new SqlHistoryServiceImpl(this.sqlSessionFactory().getObject());
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
	public FormattingConversionServiceFactoryBean conversionService()
	{
		CustomFormattingConversionServiceFactoryBean bean = new CustomFormattingConversionServiceFactoryBean();
		return bean;
	}

	@Bean
	public LocaleDateSerializer localeDateSerializer()
	{
		LocaleDateSerializer bean = new LocaleDateSerializer();
		bean.setDateFormatter(this.dateFormatter());

		return bean;
	}

	@Bean
	public LocaleSqlDateSerializer localeSqlDateSerializer()
	{
		LocaleSqlDateSerializer bean = new LocaleSqlDateSerializer();
		bean.setSqlDateFormatter(this.sqlDateFormatter());

		return bean;
	}

	@Bean
	public LocaleSqlTimeSerializer localeSqlTimeSerializer()
	{
		LocaleSqlTimeSerializer bean = new LocaleSqlTimeSerializer();
		bean.setSqlTimeFormatter(this.sqlTimeFormatter());

		return bean;
	}

	@Bean
	public LocaleSqlTimestampSerializer localeSqlTimestampSerializer()
	{
		LocaleSqlTimestampSerializer bean = new LocaleSqlTimestampSerializer();
		bean.setSqlTimestampFormatter(this.sqlTimestampFormatter());

		return bean;
	}

	@Bean
	public ObjectMapperFactory objectMapperFactory()
	{
		ObjectMapperFactory bean = new ObjectMapperFactory();

		List<JsonSerializerConfig> jsonSerializerConfigs = new ArrayList<>();

		jsonSerializerConfigs.add(new JsonSerializerConfig(java.util.Date.class, this.localeDateSerializer()));
		jsonSerializerConfigs.add(new JsonSerializerConfig(java.sql.Date.class, this.localeSqlDateSerializer()));
		jsonSerializerConfigs.add(new JsonSerializerConfig(java.sql.Time.class, this.localeSqlTimeSerializer()));
		jsonSerializerConfigs
				.add(new JsonSerializerConfig(java.sql.Timestamp.class, this.localeSqlTimestampSerializer()));

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

	@Bean
	public ServletContextAttributeExporter bayeuxServerServletContextAttributeExporter()
	{
		ServletContextAttributeExporter bean = new ServletContextAttributeExporter();

		Map<String, Object> attributes = new HashMap<>();
		attributes.put("org.cometd.bayeux", this.bayeuxServer());

		return bean;
	}

	protected BayeuxServerFactory buildBayeuxServerFactory()
	{
		BayeuxServerFactory bean = new BayeuxServerFactory();

		Map<String, Object> options = new HashMap<>();
		options.put("logLevel", 3);
		options.put("timeout", 30000);
		options.put("maxInterval", 120000);
		options.put("jsonContext", new CustomJacksonJSONContextServer(this.objectMapperFactory()));

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
	public SqlpadExecutionService sqlpadExecutionService() throws Exception
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

	@Bean
	public SchedulerFactoryBean deleteTempFileScheduler() throws IOException
	{
		DeleteExpiredFileJob job = new DeleteExpiredFileJob(this.tempDirectory(), 1440);

		MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
		jobDetailFactory.setTargetObject(job);
		jobDetailFactory.setTargetMethod("delete");

		CronTriggerFactoryBean triggerFactory = new CronTriggerFactoryBean();
		triggerFactory.setJobDetail(jobDetailFactory.getObject());
		triggerFactory.setCronExpression("0 0 1 * * ?");

		SchedulerFactoryBean bean = new SchedulerFactoryBean();
		bean.setTriggers(triggerFactory.getObject());

		return bean;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		// 处理循环依赖
		{
			List<DataPermissionEntityService<?, ?>> resourceServices = this.authorizationResourceServices();

			resourceServices.add(this.schemaService());
			resourceServices.add(this.sqlDataSetEntityService());
			resourceServices.add(this.htmlChartWidgetEntityService());
			resourceServices.add(this.htmlTplDashboardWidgetEntityService());
		}

		// 处理循环依赖
		{
			List<DevotedDataExchangeService<?>> devotedDataExchangeServices = this.devotedDataExchangeServices();
			devotedDataExchangeServices.add(this.batchDataExchangeService());
		}
	}
}
