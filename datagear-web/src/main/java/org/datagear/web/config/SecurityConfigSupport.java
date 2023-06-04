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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.datagear.util.Global;
import org.datagear.util.StringUtil;
import org.datagear.web.controller.LoginController;
import org.datagear.web.security.AjaxAwareAuthenticationEntryPoint;
import org.datagear.web.security.AnonymousAuthenticationFilterExt;
import org.datagear.web.security.AuthenticationFailureHandlerExt;
import org.datagear.web.security.AuthenticationSecurity;
import org.datagear.web.security.AuthenticationSuccessHandlerExt;
import org.datagear.web.security.LoginLatchFilter;
import org.datagear.web.security.UserDetailsServiceImpl;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

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
	public AuthenticationSuccessHandler authenticationSuccessHandler()
	{
		AuthenticationSuccessHandlerExt bean = new AuthenticationSuccessHandlerExt(
				LoginController.LOGIN_PAGE_SUCCESS, this.coreConfig.usernameLoginLatch(),
				this.coreConfig.checkCodeManager(), this.coreConfig.authenticationUserGetter());

		return bean;
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler()
	{
		AuthenticationFailureHandlerExt bean = new AuthenticationFailureHandlerExt(LoginController.LOGIN_PAGE_ERROR,
				this.coreConfig.ipLoginLatch(), this.coreConfig.usernameLoginLatch(),
				LoginController.LOGIN_PARAM_USER_NAME);

		return bean;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		configureHttpSecurity(http);
		return http.build();
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
		configAccessForLogin(http);
		configAccessForRegister(http);
		configAccessForResetPassword(http);
		configAccessForChangeTheme(http);
		configAccessForCheckCode(http);
		configAccessBeforeAllOther(http);

		configAccessAllOther(http);

		configLoginAndOut(http);
		configureAnonymous(http);
		configLoginLatchFilter(http);
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
	 * 配置{@linkplain LoginLatchFilter}过滤器。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configLoginLatchFilter(HttpSecurity http) throws Exception
	{
		http.addFilterBefore(
				new LoginLatchFilter(LOGIN_PROCESS_URL,
						(AuthenticationFailureHandlerExt) this.authenticationFailureHandler(),
						this.coreConfig.getApplicationProperties(), this.coreConfig.checkCodeManager()),
				UsernamePasswordAuthenticationFilter.class);
	}

	/**
	 * 配置登录/退出。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configLoginAndOut(HttpSecurity http) throws Exception
	{
		FormLoginConfigurer<HttpSecurity> formConfig = http.formLogin();

		// 登录
		formConfig.loginPage(LoginController.LOGIN_PAGE).loginProcessingUrl(LOGIN_PROCESS_URL)
				.usernameParameter(LoginController.LOGIN_PARAM_USER_NAME)
				.passwordParameter(LoginController.LOGIN_PARAM_PASSWORD)
				.successHandler(this.authenticationSuccessHandler()).failureHandler(this.authenticationFailureHandler())

				// 退出
				.and().logout().logoutUrl(LOGOUT_URL).invalidateHttpSession(true)
				.logoutSuccessUrl(WebUtils.INDEX_PAGE_URL)

				// 记住登录
				.and().rememberMe().key(Global.NAME_SHORT_UCUS + "REMEMBER_ME_KEY")
				.tokenValiditySeconds(60 * 60 * 24 * 365)
				.rememberMeParameter(LoginController.LOGIN_PARAM_REMEMBER_ME).rememberMeCookieName(Global.NAME_SHORT_UCUS + "REMEMBER_ME");

		AjaxAwareAuthenticationEntryPoint entryPoint = new AjaxAwareAuthenticationEntryPoint(
				new LoginUrlAuthenticationEntryPoint(LoginController.LOGIN_PAGE));

		http.exceptionHandling().authenticationEntryPoint(entryPoint);
	}

	/**
	 * 配置静态资源访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForStatic(HttpSecurity http) throws Exception
	{
		http.authorizeHttpRequests().antMatchers("/static/**").permitAll();
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
		
		UrlsAccess showStatic = new UrlsAccess(authorizationManager,
				// 图表插件
				"/chartPlugin/chartPluginManager.js", "/chartPlugin/icon/*", "/chartPlugin/resource/**",
				// 看板心跳
				"/dashboard/heartbeat",
				// 看板服务端时间
				"/dashboard/serverTime.js");

		UrlsAccess show = new UrlsAccess(authorizationManager,
				// 图表展示
				"/chart/show/**", "/chart/showData",
				// 看板展示
				"/dashboard/show/**", "/dashboard/showData", "/dashboard/loadChart", "/dashboard/auth/**",
				"/dashboard/authcheck/**",

				// 旧版图表和看板展示
				// 用于兼容2.6.0版本的图表、看板展示URL，参考CompatibleController
				"/analysis/chart/show/**", "/analysis/dashboard/show/**");

		return new ModuleAccess(showStatic, show);
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
		String anonymousAuthKey = UUID.randomUUID().toString();

		String[] anonymousRoleIds = StringUtil
				.split(this.coreConfig.getApplicationProperties().getDefaultRoleAnonymous(), ",", true);
		Set<String> anonymousRoleIdSet = new HashSet<>();
		anonymousRoleIdSet.addAll(Arrays.asList(anonymousRoleIds));

		AnonymousAuthenticationFilterExt anonymousAuthenticationFilter = new AnonymousAuthenticationFilterExt(
				anonymousAuthKey);
		anonymousAuthenticationFilter.setAnonymousRoleIds(anonymousRoleIdSet);
		anonymousAuthenticationFilter.setRoleService(this.coreConfig.roleService());

		http.anonymous().authenticationProvider(new AnonymousAuthenticationProvider(anonymousAuthKey))
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
