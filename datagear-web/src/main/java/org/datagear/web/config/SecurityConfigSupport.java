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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datagear.util.Global;
import org.datagear.util.StringUtil;
import org.datagear.web.config.support.FormLoginConfgBean;
import org.datagear.web.security.AnonymousAuthenticationFilterExt;
import org.datagear.web.security.AuthenticationSecurity;
import org.datagear.web.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 安全配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * 
 * <pre>
 * {@code @Configuration}
 * {@code @EnableWebSecurity}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 */
public class SecurityConfigSupport
{
	/**
	 * 处理登录URL。
	 */
	public static final String LOGIN_PROCESS_URL = "/login/doLogin";

	/**
	 * 退出URL。
	 */
	public static final String LOGOUT_URL = "/logout";

	public static final String SHOW_CHART_DASHBOARD_PATH_PLACEHOLDER = "SHOW_PATHS";

	private CoreConfigSupport coreConfig;

	@Autowired
	public SecurityConfigSupport(CoreConfigSupport coreConfig)
	{
		super();
		this.coreConfig = coreConfig;
	}

	public CoreConfigSupport getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfigSupport coreConfig)
	{
		this.coreConfig = coreConfig;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() throws Exception
	{
		return ((webSecurity) ->
		{
			this.configWebSecurity(webSecurity);
		});
	}

	protected void configWebSecurity(WebSecurity webSecurity)
	{
		this.configAccessForStatic(webSecurity);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		configureHttpSecurity(http);
		return http.build();
	}

	@Bean
	public FormLoginConfgBean formLoginConfgBean()
	{
		CoreConfigSupport coreConfig = getCoreConfig();
		FormLoginConfgBean bean = new FormLoginConfgBean(coreConfig.getApplicationProperties(),
				coreConfig.checkCodeManager(), coreConfig.ipLoginLatch(), coreConfig.usernameLoginLatch(),
				coreConfig.authenticationUserGetter());

		return bean;
	}

	/**
	 * 配置{@linkplain HttpSecurity}。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configureHttpSecurity(HttpSecurity http) throws Exception
	{
		configureCsrf(http);
		configureHeaders(http);
		configCors(http);
		
		configAccessForStatic(http);
		configAccessForShowChartAndDashboard(http);
		configAccessForSchema(http);
		configAccessForSchemaData(http);
		configAccessForAnalysisProject(http);
		configAccessForDataSet(http);
		configAccessForChart(http);
		configAccessForDashboard(http);
		configAccessForChartPlugin(http);
		configAccessForDataSetResDirectory(http);
		configAccessForDashboardGlobalRes(http);
		configAccessForDriverEntity(http);
		configAccessForSchemaUrlBuilder(http);
		configAccessForUser(http);
		configAccessForRole(http);
		configAccessForSchemaGuard(http);
		configAccessForAuthorization(http);
		configAccessForError(http);
		configAccessForLogin(http);
		configAccessForRegister(http);
		configAccessForResetPassword(http);
		configAccessForChangeTheme(http);
		configAccessForCheckCode(http);
		configAccessForAbout(http);
		configAccessForChangelog(http);
		configAccessBeforeAllOther(http);

		configAccessAllOther(http);

		configLoginAndOut(http);
		configureAnonymous(http);
	}

	/**
	 * 配置CSRF。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configureCsrf(HttpSecurity http) throws Exception
	{
		// 默认是开启CSRF的，系统目前没有提供相关支持，因此需禁用
		http.csrf().disable();
	}

	/**
	 * 配置headers。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configureHeaders(HttpSecurity http) throws Exception
	{
		// 默认"X-Frame-Options"值为"DENY"，这会导致系统的图表/看板展示页面无法被其他应用嵌入iframe，因此需禁用
		http.headers().frameOptions().disable();
	}

	/**
	 * 配置跨域访问。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configCors(HttpSecurity http) throws Exception
	{
		http.cors((customizer) ->
		{
			customizer.configurationSource(corsConfigurationSource());
		});
	}

	protected CorsConfigurationSource corsConfigurationSource()
	{
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		ApplicationProperties properties = getCoreConfig().getApplicationProperties();
		List<CrossOriginProperties> corsPropertiess = properties.getCrossOriginPropertiess();

		for (CrossOriginProperties corsps : corsPropertiess)
		{
			String[] paths = corsps.getPaths();

			if (paths == null || paths.length == 0)
				continue;

			if (paths.length == 1 && SHOW_CHART_DASHBOARD_PATH_PLACEHOLDER.equalsIgnoreCase(paths[0]))
				paths = resolveShowChartAndDashboardUrls();

			for (String path : paths)
			{
				CorsConfiguration coscfg = new CorsConfiguration();

				if (corsps.getAllowedOrigins() != null && corsps.getAllowedOrigins().length > 0)
					coscfg.setAllowedOriginPatterns(Arrays.asList(corsps.getAllowedOrigins()));
				else if (corsps.getAllowedOriginPatterns() != null && corsps.getAllowedOriginPatterns().length > 0)
					coscfg.setAllowedOriginPatterns(Arrays.asList(corsps.getAllowedOriginPatterns()));

				if (corsps.getAllowedMethods() != null && corsps.getAllowedMethods().length > 0)
					coscfg.setAllowedMethods(Arrays.asList(corsps.getAllowedMethods()));

				if (corsps.getAllowedHeaders() != null && corsps.getAllowedHeaders().length > 0)
					coscfg.setAllowedHeaders(Arrays.asList(corsps.getAllowedHeaders()));

				if (corsps.getExposedHeaders() != null && corsps.getExposedHeaders().length > 0)
					coscfg.setExposedHeaders(Arrays.asList(corsps.getExposedHeaders()));

				coscfg.setAllowCredentials(corsps.isAllowCredentials());

				if (corsps.getMaxAge() != null)
					coscfg.setMaxAge(corsps.getMaxAge());

				source.registerCorsConfiguration(path, coscfg);
			}
		}

		return source;
	}

	protected String[] resolveShowChartAndDashboardUrls()
	{
		List<String> urls = new ArrayList<String>();

		ModuleAccess moduleAccess = this.showChartAndDashboardModuleAccess();
		List<UrlsAccess> urlsAccesses = moduleAccess.getUrlsAccesses();

		for (UrlsAccess ua : urlsAccesses)
			urls.addAll(ua.getUrls());

		return urls.toArray(new String[urls.size()]);
	}

	/**
	 * 配置登录/退出。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configLoginAndOut(HttpSecurity http) throws Exception
	{
		FormLoginConfgBean formLoginConfgBean = this.formLoginConfgBean();
		formLoginConfgBean.configHttpSecurity(http);
	}

	/**
	 * 配置静态资源访问权限。
	 * 
	 * @param webSecurity
	 * @throws Exception
	 */
	protected void configAccessForStatic(WebSecurity webSecurity)
	{
		webSecurity.ignoring().antMatchers(staticResourcePathPatterns());
	}

	/**
	 * 获取静态资源路径匹配模式。
	 * <p>
	 * 系统静态资源应在此配置和返回，避免仍会执行安全框架相关逻辑而影响性能（比如{@linkplain AnonymousAuthenticationFilterExt}）。
	 * </p>
	 * 
	 * @return
	 */
	protected String[] staticResourcePathPatterns()
	{
		return new String[] { "/static/**" };
	}

	/**
	 * 配置静态资源访问权限。
	 * <p>
	 * 在此方法内使用{@code http.authorizeHttpRequests().antMatchers("/static/**").permitAll()}的方式配置静态资源，
	 * 在请求时仍然会执行安全框架相关逻辑，影响性能，因此这里弃用，改为采用{@linkplain #configAccessForStatic(WebSecurity)}方式。
	 * </p>
	 * 
	 * @param http
	 * @throws Exception
	 */
	@Deprecated
	protected void configAccessForStatic(HttpSecurity http) throws Exception
	{
	}

	/**
	 * 配置图表、看板展示功能访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForShowChartAndDashboard(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, showChartAndDashboardModuleAccess());
	}

	protected ModuleAccess showChartAndDashboardModuleAccess()
	{
		// 展示图表和看板
		// 注意：无论系统是否允许匿名用户访问，它们都应允许匿名用户访问，用于支持外部系统iframe嵌套场景

		AuthorizationManager<RequestAuthorizationContext> authorizationManager = showChartAndDashboardAuthorizationManager();
		
		UrlsAccess showStatic = new UrlsAccess(authorizationManager, showChartAndDashboardStaticUrlPattern());

		UrlsAccess show = new UrlsAccess(authorizationManager,
				// 图表展示
				"/chart/show/**", "/chart/showData*",
				// 看板展示
				"/dashboard/show/**", "/dashboard/showData*", "/dashboard/loadChart*", "/dashboard/auth/**",
				"/dashboard/authcheck/**",
				// 看板心跳
				"/dashboard/heartbeat*",
				// 看板卸载
				"/dashboard/unload*",

				// 旧版图表和看板展示
				// 用于兼容2.6.0版本的图表、看板展示URL，参考CompatibleController
				"/analysis/chart/show/**", "/analysis/dashboard/show/**");

		return new ModuleAccess(showStatic, show);
	}
	
	protected String[] showChartAndDashboardStaticUrlPattern()
	{
		return new String[] {
				// 图表插件
				"/chartPlugin/chartPluginManager.js", "/chartPlugin/icon/*", "/chartPlugin/resource/**",
				// 看板服务端时间
				"/dashboard/serverTime.js"
				//
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> showChartAndDashboardAuthorizationManager()
	{
		ApplicationProperties properties = getCoreConfig().getApplicationProperties();
		AuthorizationManager<RequestAuthorizationContext> anonymousAuthManager = anonymousAuthorizationManager();
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			if(properties.isDisableShowAnonymous())
			{
				return new AuthorizationDecision(authSecurity.hasUser(auth.get()));
			}
			else
				return anonymousAuthManager.check(auth, request);
		};
	}

	/**
	 * 配置看板模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDashboard(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, dashboardModuleAccess());
	}

	protected ModuleAccess dashboardModuleAccess()
	{
		UrlsAccess edit = new UrlsAccess(dataManagerAuthorizationManager(), //
				"/dashboard/add", "/dashboard/edit",
				"/dashboard/save", "/dashboard/copy",
				"/dashboard/saveTemplateNames", "/dashboard/deleteResource", "/dashboard/uploadResourceFile",
				"/dashboard/saveUploadResourceFile", "/dashboard/saveResourceContent", "/dashboard/import",
				"/dashboard/uploadImportFile", "/dashboard/saveImport", "/dashboard/delete",
				"/dashboard/shareSet", "/dashboard/saveShareSet", "/dashboard/export");

		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(), "/dashboard/**");

		return new ModuleAccess(edit, read);
	}

	/**
	 * 配置图表模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChart(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, chartModuleAccess());
	}

	protected ModuleAccess chartModuleAccess()
	{
		UrlsAccess edit = new UrlsAccess(dataManagerAuthorizationManager(), //
				"/chart/add", "/chart/edit", "/chart/copy",
				"/chart/save", "/chart/delete");

		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(), "/chart/**");

		return new ModuleAccess(edit, read);
	}

	/**
	 * 配置数据集模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDataSet(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, dataSetModuleAccess());
	}

	protected ModuleAccess dataSetModuleAccess()
	{
		UrlsAccess edit = new UrlsAccess(dataManagerAuthorizationManager(), //
				"/dataSet/addFor*", "/dataSet/saveAddFor*",
				"/dataSet/edit", "/dataSet/saveEditFor*",
				"/dataSet/copy", "/dataSet/delete", "/dataSet/uploadFile");

		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(), "/dataSet/**");

		return new ModuleAccess(edit, read);
	}

	/**
	 * 配置数据分析项目模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForAnalysisProject(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, analysisProjectModuleAccess());
	}

	protected ModuleAccess analysisProjectModuleAccess()
	{
		UrlsAccess edit = new UrlsAccess(dataManagerAuthorizationManager(), //
				"/analysisProject/add", "/analysisProject/saveAdd",
				"/analysisProject/edit",
				"/analysisProject/saveEdit", "/analysisProject/delete");

		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(), "/analysisProject/**");

		return new ModuleAccess(edit, read);
	}

	/**
	 * 配置数据源模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchema(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, schemaModuleAccess());
	}

	protected ModuleAccess schemaModuleAccess()
	{
		UrlsAccess edit = new UrlsAccess(dataManagerAuthorizationManager(), //
				"/schema/add", "/schema/saveAdd", "/schema/edit",
				"/schema/saveEdit", "/schema/delete");

		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(), "/schema/**");

		return new ModuleAccess(edit, read);
	}

	/**
	 * 配置数据源模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchemaData(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, schemaDataModuleAccess());
	}

	protected ModuleAccess schemaDataModuleAccess()
	{
		// 数据源数据管理、导入导出、SQL工作台、SQL编辑器
		// 用户针对数据源数据的所有操作都已受其所属数据源权限控制，所以不必再引入数据管理员/数据分析员权限
		UrlsAccess ua = new UrlsAccess(accessAuthorizationManager(),
				"/data/**", "/dataexchange/**", "/sqlpad/**", "/sqlEditor/**");

		return new ModuleAccess(ua);
	}

	/**
	 * 配置图表插件模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChartPlugin(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, chartPluginModuleAccess());
	}

	protected ModuleAccess chartPluginModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(),
				"/chartPlugin/select", "/chartPlugin/selectData");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/chartPlugin/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置数据集资源目录模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDataSetResDirectory(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, dataSetResDirectoryModuleAccess());
	}

	protected ModuleAccess dataSetResDirectoryModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(),
				"/dataSetResDirectory/view", "/dataSetResDirectory/select",
				"/dataSetResDirectory/pagingQueryData", "/dataSetResDirectory/listFiles");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/dataSetResDirectory/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置看板全局资源模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDashboardGlobalRes(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, dashboardGlobalResModuleAccess());
	}

	protected ModuleAccess dashboardGlobalResModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(),
				"/dashboardGlobalRes/queryData");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/dashboardGlobalRes/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置数据源驱动程序模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDriverEntity(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, driverEntityModuleAccess());
	}

	protected ModuleAccess driverEntityModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(),
				"/driverEntity/view", "/driverEntity/select", "/driverEntity/queryData",
				"/driverEntity/downloadDriverFile", "/driverEntity/listDriverFile");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/driverEntity/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置数据源URL构建器模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchemaUrlBuilder(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, schemaUrlBuilderModuleAccess());
	}

	protected ModuleAccess schemaUrlBuilderModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(dataAnalystAuthorizationManager(),
				"/schemaUrlBuilder/build");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/schemaUrlBuilder/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置用户模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForUser(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, userModuleAccess());
	}

	protected ModuleAccess userModuleAccess()
	{
		UrlsAccess other = new UrlsAccess(userAuthorizationManager(),
				"/user/personalSet", "/user/savePersonalSet", "/user/select", "/user/pagingQueryData");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/user/**");

		return new ModuleAccess(other, edit);
	}

	/**
	 * 配置角色模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForRole(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, roleModuleAccess());
	}

	protected ModuleAccess roleModuleAccess()
	{
		UrlsAccess read = new UrlsAccess(userAuthorizationManager(),
				"/role/select", "/role/pagingQueryData");

		UrlsAccess edit = new UrlsAccess(adminAuthorizationManager(), "/role/**");

		return new ModuleAccess(read, edit);
	}

	/**
	 * 配置数据源防护模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchemaGuard(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, schemaGuardModuleAccess());
	}

	protected ModuleAccess schemaGuardModuleAccess()
	{
		UrlsAccess ua = new UrlsAccess(adminAuthorizationManager(), "/schemaGuard/**");

		return new ModuleAccess(ua);
	}

	/**
	 * 配置数据授权模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForAuthorization(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, authorizationModuleAccess());
	}

	protected ModuleAccess authorizationModuleAccess()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		UrlsAccess ua = new UrlsAccess((auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasDataManager(auth.get()));
		}, //
				"/authorization/**");

		return new ModuleAccess(ua);
	}

	/**
	 * 配置错误页访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForError(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/error/**").permitAll();
	}

	/**
	 * 配置登录模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForLogin(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/login/**").permitAll();
	}

	/**
	 * 配置注册模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForRegister(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/register/**").permitAll();
	}

	/**
	 * 配置重置密码模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForResetPassword(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/resetPassword/**").permitAll();
	}

	/**
	 * 配置切换主题模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChangeTheme(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/changeThemeData/**").permitAll();
	}

	/**
	 * 配置校验码模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForCheckCode(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/checkCode/**").permitAll();
	}

	/**
	 * 配置关于页面访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForAbout(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/about/**").permitAll();
	}

	/**
	 * 配置日志页面访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChangelog(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/changelog/**", "/changelogs/**").permitAll();
	}

	/**
	 * 配置{@linkplain #configAccessAllOther(HttpSecurity)}之前的访问权限。
	 * <p>
	 * 此方法默认什么也不做，留作扩展。
	 * </p>
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessBeforeAllOther(HttpSecurity http) throws Exception
	{
	}

	/**
	 * 配置所有其他功能访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessAllOther(HttpSecurity http) throws Exception
	{
		configAccessForModuleAccess(http, allOtherModuleAccess());
	}

	protected ModuleAccess allOtherModuleAccess()
	{
		UrlsAccess ua = new UrlsAccess(accessAuthorizationManager(), "/**");

		return new ModuleAccess(ua);
	}

	protected void configAccessForModuleAccess(HttpSecurity http, ModuleAccess ma) throws Exception
	{
		for(UrlsAccess ua : ma.getUrlsAccesses())
		{
			if(ua.getAuthManager() != null)
			{
				http.authorizeHttpRequests(authorize ->
				{
					authorize.mvcMatchers(ua.getUrlArray()).access(ua.getAuthManager());
				});
			}
		}
	}

	protected AuthorizationManager<RequestAuthorizationContext> dataManagerAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasDataManager(auth.get()));
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> dataAnalystAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasDataAnalyst(auth.get()));
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> adminAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasAdmin(auth.get()));
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> userAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasUser(auth.get()));
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> anonymousAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasAnonymous(auth.get()));
		};
	}

	protected AuthorizationManager<RequestAuthorizationContext> accessAuthorizationManager()
	{
		AuthenticationSecurity authSecurity = getAuthenticationSecurity();

		return (auth, request) ->
		{
			return new AuthorizationDecision(authSecurity.hasAccess(auth.get()));
		};
	}

	/**
	 * 将默认的{@linkplain AnonymousAuthenticationFilter}配置改为{@linkplain AnonymousAuthenticationFilterExt}。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configureAnonymous(HttpSecurity http) throws Exception
	{
		String[] anonymousRoleIds = StringUtil
				.split(this.coreConfig.getApplicationProperties().getDefaultRoleAnonymous(), ",", true);
		Set<String> anonymousRoleIdSet = new HashSet<>();
		anonymousRoleIdSet.addAll(Arrays.asList(anonymousRoleIds));

		AnonymousAuthenticationFilterExt anonymousAuthenticationFilter = new AnonymousAuthenticationFilterExt(
				Global.NAME_SHORT_UCUS + "ANONYMOUS_AUTH_FILTER");
		anonymousAuthenticationFilter.setAnonymousRoleIds(anonymousRoleIdSet);
		anonymousAuthenticationFilter.setRoleService(this.coreConfig.roleService());

		http.anonymous()
				.authenticationProvider(
						new AnonymousAuthenticationProvider(Global.NAME_SHORT_UCUS + "ANONYMOUS_AUTH_PROVIDER"))
				.authenticationFilter(anonymousAuthenticationFilter);
	}

	protected boolean isDisableAnonymous()
	{
		return this.coreConfig.getApplicationProperties().isDisableAnonymous();
	}

	protected AuthenticationSecurity getAuthenticationSecurity()
	{
		return getCoreConfig().authenticationSecurity();
	}

	@Bean
	public UserDetailsService userDetailsService()
	{
		UserDetailsService bean = new UserDetailsServiceImpl(this.coreConfig.userService());
		return bean;
	}

	@Bean
	public StrictHttpFirewall httpFirewall()
	{
		StrictHttpFirewall firewall = new StrictHttpFirewall();

		// 看板有些功能需要URL中允许分号（;）
		// 参考：AbstractDataAnalysisController.addJsessionidParam(String, String)，
		// 因此这里需要设置为允许，不然功能将无法使用
		firewall.setAllowSemicolon(true);
		return firewall;
	}

	/**
	 * 模块访问配置。
	 */
	public static class ModuleAccess implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private List<UrlsAccess> urlsAccesses = new ArrayList<UrlsAccess>(3);

		public ModuleAccess()
		{
			super();
		}

		public ModuleAccess(List<UrlsAccess> urlsAccesses)
		{
			super();
			this.urlsAccesses.addAll(urlsAccesses);
		}

		public ModuleAccess(UrlsAccess... urlsAccesses)
		{
			super();
			Collections.addAll(this.urlsAccesses, urlsAccesses);
		}

		public List<UrlsAccess> getUrlsAccesses()
		{
			return urlsAccesses;
		}

		public void setUrlsAccesses(List<UrlsAccess> urlsAccesses)
		{
			this.urlsAccesses = urlsAccesses;
		}

		public void addUrlsAccesses(List<UrlsAccess> urlsAccesses)
		{
			this.urlsAccesses.addAll(urlsAccesses);
		}

		public void addUrlsAccesses(UrlsAccess... urlsAccesses)
		{
			Collections.addAll(this.urlsAccesses, urlsAccesses);
		}

		public UrlsAccess getUrlsAccess(int index)
		{
			return this.urlsAccesses.get(index);
		}

		public void setUrlsAccess(int index, UrlsAccess urlsAccess)
		{
			this.urlsAccesses.set(index, urlsAccess);
		}
	}

	/**
	 * URL访问配置。
	 */
	public static class UrlsAccess implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private AuthorizationManager<RequestAuthorizationContext> authManager = null;

		private List<String> urls = new ArrayList<String>();

		public UrlsAccess()
		{
			super();
		}

		public UrlsAccess(AuthorizationManager<RequestAuthorizationContext> authManager, List<String> urls)
		{
			super();
			this.authManager = authManager;
			this.urls.addAll(urls);
		}

		public UrlsAccess(AuthorizationManager<RequestAuthorizationContext> authManager, String... urls)
		{
			super();
			this.authManager = authManager;
			Collections.addAll(this.urls, urls);
		}

		public AuthorizationManager<RequestAuthorizationContext> getAuthManager()
		{
			return authManager;
		}

		public void setAuthManager(AuthorizationManager<RequestAuthorizationContext> authManager)
		{
			this.authManager = authManager;
		}

		public List<String> getUrls()
		{
			return urls;
		}

		public void setUrls(List<String> urls)
		{
			this.urls = urls;
		}

		public String[] getUrlArray()
		{
			return this.urls.toArray(new String[this.urls.size()]);
		}

		public void addUrls(List<String> urls)
		{
			this.urls.addAll(urls);
		}

		public void addUrls(String... urls)
		{
			Collections.addAll(this.urls, urls);
		}
	}
}
