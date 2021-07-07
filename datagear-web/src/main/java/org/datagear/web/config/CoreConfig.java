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
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.FileTemplateDashboardWidgetResManager;
import org.datagear.analysis.support.NameAsTemplateDashboardWidgetResManager;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.analysis.support.html.HtmlChartWidgetJsonWriter;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardImport.ImportItem;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.TemplateImportHtmlChartPluginVarNameResolver;
import org.datagear.analysis.support.html.SimpleHtmlTplDashboardImport;
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
import org.datagear.util.IOUtil;
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
import org.datagear.web.sqlpad.SqlpadExecutionService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 核心配置。
 * 
 * @author datagear@163.com
 */
@Configuration
public class CoreConfig implements InitializingBean
{
	public static final String NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "chartShowHtmlTplDashboardWidgetHtmlRenderer";

	public static final String NAME_DASHBOARD_SHOW_HtmlTplDashboardWidgetHtmlRenderer = "htmlTplDashboardWidgetRenderer";

	public static final String NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY = "dashboardGlobalResRootDirectory";

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

	@Bean(NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	public File dashboardGlobalResRootDirectory()
	{
		return createDirectory(environment.getProperty("directory.dashboardGlobalRes"), true);
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
	public SqlSessionFactory sqlSessionFactory()
	{
		try
		{
			PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();
			Resource[] mapperResources = pathResolver.getResources("classpath*:org/datagear/management/mapper/*.xml");

			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
			bean.setDataSource(this.dataSourceConfig.dataSource());
			bean.setMapperLocations(mapperResources);
			bean.setTypeHandlers(new TypeHandler<?>[] { new LiteralBooleanTypeHandler(), new DataFormatTypeHandler(),
					new ResultDataFormatTypeHandler() });
			return bean.getObject();
		}
		catch (Exception e)
		{
			throw new BeanInitializationException("Init " + SqlSessionFactory.class + " failed", e);
		}
	}

	@Bean
	public MbSqlDialectBuilder mbSqlDialectBuilder()
	{
		String dialectName = environment.getProperty("datasourceDialect");

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
		AuthorizationServiceImpl bean = new AuthorizationServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.authorizationResourceServices());

		return bean;
	}

	@Bean
	public SchemaService schemaService()
	{
		SchemaServiceImpl bean = new SchemaServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.driverEntityManager(), this.authorizationService());

		return bean;
	}

	@Bean
	public UserService userService()
	{
		UserServiceImpl bean = new UserServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.roleUserService(), this.roleService());
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
	public RoleUserService roleUserService()
	{
		RoleUserServiceImpl bean = new RoleUserServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());
		return bean;
	}

	@Bean
	public DataSetEntityService dataSetEntityService()
	{
		DataSetEntityServiceImpl bean = new DataSetEntityServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect(),
				this.connectionSource(), this.schemaService(), this.authorizationService(), this.dataSetRootDirectory(),
				this.httpClient());
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
				this.directoryHtmlChartPluginManager());
		bean.setTmpDirectory(this.tempDirectory());

		return bean;
	}

	@Bean
	public HtmlChartWidgetEntityService htmlChartWidgetEntityService()
	{
		HtmlChartWidgetEntityServiceImpl bean = new HtmlChartWidgetEntityServiceImpl(this.sqlSessionFactory(),
				this.mbSqlDialect(), this.directoryHtmlChartPluginManager(), this.dataSetEntityService(),
				this.authorizationService());

		return bean;
	}

	@Bean(NAME_CHART_SHOW_HtmlTplDashboardWidgetHtmlRenderer)
	public HtmlTplDashboardWidgetHtmlRenderer chartShowHtmlTplDashboardWidgetHtmlRenderer()
	{
		TemplateDashboardWidgetResManager resManager = new NameAsTemplateDashboardWidgetResManager();

		HtmlTplDashboardWidgetHtmlRenderer bean = new HtmlTplDashboardWidgetHtmlRenderer(resManager,
				this.htmlChartWidgetEntityService());

		bean.setHtmlTplDashboardImport(this.buildHtmlTplDashboardWidgetRenderer_dshboardImport(bean));
		bean.setImportHtmlChartPluginVarNameResolver(
				this.buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver(bean));

		return bean;
	}

	@Bean(NAME_DASHBOARD_SHOW_HtmlTplDashboardWidgetHtmlRenderer)
	public HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetRenderer()
	{
		HtmlTplDashboardWidgetHtmlRenderer bean = new HtmlTplDashboardWidgetHtmlRenderer(
				this.templateDashboardWidgetResManager(), this.htmlChartWidgetEntityService());

		bean.setHtmlTplDashboardImport(this.buildHtmlTplDashboardWidgetRenderer_dshboardImport(bean));
		bean.setImportHtmlChartPluginVarNameResolver(
				this.buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver(bean));

		return bean;
	}

	protected HtmlTplDashboardImport buildHtmlTplDashboardWidgetRenderer_dshboardImport(
			HtmlTplDashboardWidgetRenderer renderer)
	{
		SimpleHtmlTplDashboardImport dashboardImport = new SimpleHtmlTplDashboardImport();

		List<ImportItem> importItems = new ArrayList<>();

		String cp = renderer.getContextPathPlaceholder();
		String vp = renderer.getVersionPlaceholder();
		String rp = renderer.getRandomCodePlaceholder();

		String staticPrefix = cp + "/static";
		String libPrefix = staticPrefix + "/lib";
		String cssPrefix = staticPrefix + "/css";
		String scriptPrefix = staticPrefix + "/script";

		// CSS
		importItems.add(
				ImportItem.valueOfLinkCss("dataTableStyle", libPrefix + "/DataTables-1.10.18/css/datatables.min.css"));
		importItems.add(ImportItem.valueOfLinkCss("datetimepickerStyle",
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css"));
		importItems.add(ImportItem.valueOfLinkCss("dashboardStyle", cssPrefix + "/analysis.css?v=" + vp));

		// JS
		importItems.add(ImportItem.valueOfJavaScript("jquery", libPrefix + "/jquery-1.12.4/jquery-1.12.4.min.js"));
		importItems.add(ImportItem.valueOfJavaScript("echarts", libPrefix + "/echarts-5.1.2/echarts.js"));
		importItems.add(ImportItem.valueOfJavaScript("wordcloud",
				libPrefix + "/echarts-wordcloud-2.0.0/echarts-wordcloud.min.js"));
		importItems.add(ImportItem.valueOfJavaScript("liquidfill",
				libPrefix + "/echarts-liquidfill-3.0.0/echarts-liquidfill.min.js"));
		importItems
				.add(ImportItem.valueOfJavaScript("dataTable", libPrefix + "/DataTables-1.10.18/js/datatables.min.js"));
		importItems.add(ImportItem.valueOfJavaScript("datetimepicker",
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js"));
		importItems
				.add(ImportItem.valueOfJavaScript("chartFactory", scriptPrefix + "/datagear-chartFactory.js?v=" + vp));
		importItems.add(ImportItem.valueOfJavaScript("dashboardFactory",
				scriptPrefix + "/datagear-dashboardFactory.js?v=" + vp));
		importItems.add(
				ImportItem.valueOfJavaScript("serverTime", cp + "/analysis/dashboard/servertime.js?v=" + rp));
		importItems
				.add(ImportItem.valueOfJavaScript("chartSupport", scriptPrefix + "/datagear-chartSupport.js?v=" + vp));
		importItems
				.add(ImportItem.valueOfJavaScript("chartSetting", scriptPrefix + "/datagear-chartSetting.js?v=" + vp));
		importItems.add(ImportItem.valueOfJavaScript("chartPluginManager",
				cp + "/analysis/chartPlugin/chartPluginManager.js?v=" + vp));

		dashboardImport.setImportItems(importItems);

		return dashboardImport;
	}

	protected TemplateImportHtmlChartPluginVarNameResolver buildHtmlTplDashboardWidgetRendererd_importHtmlChartPluginVarNameResolver(
			HtmlTplDashboardWidgetRenderer renderer)
	{
		String pp = TemplateImportHtmlChartPluginVarNameResolver.PLACEHOLDER_CHART_PLUGIN_ID;

		TemplateImportHtmlChartPluginVarNameResolver resolver = new TemplateImportHtmlChartPluginVarNameResolver(
				"chartFactory.chartPluginManager.get('" + pp + "')");

		return resolver;
	}

	@Bean
	public HtmlChartWidgetJsonWriter htmlChartWidgetJsonWriter()
	{
		HtmlChartWidgetJsonWriter bean = new HtmlChartWidgetJsonWriter();
		return bean;
	}

	@Bean
	public HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService()
	{
		HtmlTplDashboardWidgetEntityServiceImpl bean = new HtmlTplDashboardWidgetEntityServiceImpl(
				this.sqlSessionFactory(), this.mbSqlDialect(), this.htmlTplDashboardWidgetRenderer(),
				this.authorizationService());

		return bean;
	}

	@Bean
	public AnalysisProjectService analysisProjectService()
	{
		AnalysisProjectService bean = new AnalysisProjectServiceImpl(this.sqlSessionFactory(), this.mbSqlDialect());
		return bean;
	}

	@Bean
	public DataSetResDirectoryService dataSetResDirectoryService()
	{
		DataSetResDirectoryService bean = new DataSetResDirectoryServiceImpl(this.sqlSessionFactory(),
				this.mbSqlDialect());
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
	public SqlpadExecutionService sqlpadExecutionService()
	{
		SqlpadExecutionService bean = new SqlpadExecutionService(this.connectionSource(), this.messageSource(),
				this.sqlHistoryService(), this.sqlSelectManager());
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
		GenericDataExchangeService bean = new GenericDataExchangeService();
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
			this.dataExchangeService().setDevotedDataExchangeServices(devotedDataExchangeServices);
		}
	}
}
