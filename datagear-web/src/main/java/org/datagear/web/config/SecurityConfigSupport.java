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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.datagear.management.domain.Role;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.util.StringUtil;
import org.datagear.web.controller.LoginController;
import org.datagear.web.security.AnonymousAuthenticationFilterExt;
import org.datagear.web.security.AuthUser;
import org.datagear.web.security.AuthenticationFailureHandlerImpl;
import org.datagear.web.security.AuthenticationSuccessHandlerImpl;
import org.datagear.web.security.LoginLatchFilter;
import org.datagear.web.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * 安全配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * <pre>
 * {@code @Configuration}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 */
public class SecurityConfigSupport implements ApplicationListener<ContextRefreshedEvent>
{
	public static final String LOGIN_PROCESS_URL = "/login/doLogin";

	/**
	 * 授权角色：登录用户 且 数据管理员
	 */
	protected static final String AUTH_USER_AND_DATA_MANAGER = "hasAuthority('" + AuthUser.ROLE_USER+ "')"
			+" and hasAuthority('" + Role.ROLE_DATA_MANAGER + "')";

	/**
	 * 授权角色：登录用户 且 (数据管理员 或 数据分析员)
	 */
	protected static final String AUTH_USER_AND_DATA_MANAGER_OR_ANALYST = "hasAnyAuthority('" + AuthUser.ROLE_USER + "')"
			+" and hasAnyAuthority('" + Role.ROLE_DATA_MANAGER + "','" + Role.ROLE_DATA_ANALYST + "')";

	/**
	 * 授权角色：数据管理员 或 数据分析员
	 */
	protected static final String AUTH_DATA_MANAGER_OR_ANALYST = "hasAnyAuthority('" + Role.ROLE_DATA_MANAGER + "','" + Role.ROLE_DATA_ANALYST + "')";

	/**
	 * 授权角色：系统管理员
	 */
	protected static final String AUTH_ADMIN = "hasAuthority('" + AuthUser.ROLE_ADMIN + "')";

	/**
	 * 授权角色：登录用户
	 */
	protected static final String AUTH_USER = "hasAuthority('" + AuthUser.ROLE_USER + "')";

	/**
	 * 授权角色：匿名用户
	 */
	protected static final String AUTH_ANONYMOUS = "hasAuthority('" + AuthUser.ROLE_ANONYMOUS + "')";

	/**
	 * 授权角色：匿名用户 或 登录用户
	 */
	protected static final String AUTH_ANONYMOUS_OR_USER = "hasAnyAuthority('" + AuthUser.ROLE_ANONYMOUS + "','"
			+ AuthUser.ROLE_USER + "')";

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
		AuthenticationSuccessHandlerImpl bean = new AuthenticationSuccessHandlerImpl(
				this.coreConfig.usernameLoginLatch(), this.coreConfig.checkCodeManager());

		return bean;
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler()
	{
		AuthenticationFailureHandlerImpl bean = new AuthenticationFailureHandlerImpl("/login/error",
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
		// 默认是开启CSRF的，系统目前没有提供相关支持，因此需禁用
		http.csrf().disable();

		// 默认"X-Frame-Options"值为"DENY"，这会导致系统的图表/看板展示页面无法被其他应用嵌入iframe，因此需禁用
		http.headers().frameOptions().disable();

		configAccessForStatic(http);
		configAccessForShowChartAndDashboard(http);
		configAccessForSchema(http);
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

		configLoginAndOutForm(http);
		configureAnonymous(http);
		configLoginLatchFilter(http);
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
						(AuthenticationFailureHandlerImpl) this.authenticationFailureHandler(),
						this.coreConfig.getApplicationProperties(), this.coreConfig.checkCodeManager()),
				UsernamePasswordAuthenticationFilter.class);
	}

	/**
	 * 配置登录、退出相关访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configLoginAndOutForm(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
				// 登录
				.and().formLogin().loginPage(LoginController.LOGIN_PAGE).loginProcessingUrl(LOGIN_PROCESS_URL)
				.usernameParameter(LoginController.LOGIN_PARAM_USER_NAME)
				.passwordParameter(LoginController.LOGIN_PARAM_PASSWORD)
				.successHandler(this.authenticationSuccessHandler()).failureHandler(this.authenticationFailureHandler())

				// 退出
				.and().logout().logoutUrl("/logout").invalidateHttpSession(true).logoutSuccessUrl("/")

				// 记住登录
				.and().rememberMe().key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24 * 365)
				.rememberMeParameter(LoginController.LOGIN_PARAM_REMEMBER_ME).rememberMeCookieName("REMEMBER_ME");
	}

	/**
	 * 配置静态资源访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForStatic(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/static/**").permitAll();
	}

	/**
	 * 配置图表、看板展示功能访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForShowChartAndDashboard(HttpSecurity http) throws Exception
	{
		// 展示图表和看板
		// 注意：无论系统是否允许匿名用户访问，它们都应允许匿名用户访问，用于支持外部系统iframe嵌套场景
		http.authorizeRequests().antMatchers(
				// 图表插件
				"/chartPlugin/chartPluginManager.js", "/chartPlugin/icon/*", "/chartPlugin/resource/**",
				// 图表展示
				"/chart/show/**", "/chart/showData",
				// 看板展示
				"/dashboard/show/**", "/dashboard/showData", "/dashboard/loadChart", "/dashboard/heartbeat",
				"/dashboard/serverTime.js", "/dashboard/auth/**", "/dashboard/authcheck/**")
				.access(AUTH_ANONYMOUS_OR_USER)

				// 展示图表和看板
				// 用于兼容2.6.0版本的图表、看板展示URL，参考CompatibleController
				.antMatchers("/analysis/chart/show/**", "/analysis/dashboard/show/**").access(AUTH_ANONYMOUS_OR_USER);
	}

	/**
	 * 配置看板模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDashboard(HttpSecurity http) throws Exception
	{
		configAccessForRoleManagerAnalyst(http, //
				new String[] { "/dashboard/add", "/dashboard/edit", "/dashboard/save", "/dashboard/copy",
						"/dashboard/saveTemplateNames", "/dashboard/deleteResource", "/dashboard/uploadResourceFile",
						"/dashboard/saveUploadResourceFile", "/dashboard/saveResourceContent", "/dashboard/import",
						"/dashboard/uploadImportFile", "/dashboard/saveImport", "/dashboard/delete",
						"/dashboard/shareSet", "/dashboard/saveShareSet", "/dashboard/export" },
				"/dashboard/**");
	}

	/**
	 * 配置图表模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChart(HttpSecurity http) throws Exception
	{
		configAccessForRoleManagerAnalyst(http, //
				new String[] { "/chart/add", "/chart/edit", "/chart/copy", "/chart/save", "/chart/delete" },
				"/chart/**");
	}

	/**
	 * 配置数据集模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDataSet(HttpSecurity http) throws Exception
	{
		configAccessForRoleManagerAnalyst(http, //
				new String[] { "/dataSet/addFor*", "/dataSet/saveAddFor*", "/dataSet/edit", "/dataSet/saveEditFor*",
						"/dataSet/copy", "/dataSet/delete", "/dataSet/uploadFile" },
				"/dataSet/**");
	}

	/**
	 * 配置数据分析项目模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForAnalysisProject(HttpSecurity http) throws Exception
	{
		configAccessForRoleManagerAnalyst(http,
				new String[] { "/analysisProject/add", "/analysisProject/saveAdd", "/analysisProject/edit",
						"/analysisProject/saveEdit", "/analysisProject/delete" },
				"/analysisProject/**");
	}

	/**
	 * 配置数据源模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchema(HttpSecurity http) throws Exception
	{
		boolean disableAnonymous = this.coreConfig.getApplicationProperties().isDisableAnonymous();

		configAccessForRoleManagerAnalyst(http,
				new String[] { "/schema/add", "/schema/saveAdd", "/schema/edit", "/schema/saveEdit", "/schema/delete" },
				"/schema/**");

		// 数据源数据管理、导入导出、SQL工作台、SQL编辑器
		// 用户针对数据源数据的所有操作都已受其所属数据源权限控制，所以不必再引入数据管理员/数据分析员权限
		http.authorizeRequests() //
				.antMatchers("/data/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/dataexchange/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/sqlpad/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/sqlEditor/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER);

	}

	/**
	 * 配置图表插件模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChartPlugin(HttpSecurity http) throws Exception
	{
		configAccessForRoleAdmin(http, //
				"/chartPlugin/**",
				new String[] { "/chartPlugin/select", "/chartPlugin/selectData" });
	}

	/**
	 * 配置数据集资源目录模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDataSetResDirectory(HttpSecurity http) throws Exception
	{
		configAccessForRoleAdmin(http, //
				"/dataSetResDirectory/**", //
				new String[] { "/dataSetResDirectory/view", "/dataSetResDirectory/select",
						"/dataSetResDirectory/pagingQueryData", "/dataSetResDirectory/listFiles" });
	}

	/**
	 * 配置看板全局资源模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDashboardGlobalRes(HttpSecurity http) throws Exception
	{
		configAccessForRoleAdmin(http, //
				"/dashboardGlobalRes/**", //
				new String[] { "/dashboardGlobalRes/queryData" });
	}

	/**
	 * 配置数据源驱动程序模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForDriverEntity(HttpSecurity http) throws Exception
	{
		configAccessForRoleAdmin(http, //
				"/driverEntity/**", //
				new String[] { "/driverEntity/view", "/driverEntity/select", "/driverEntity/queryData",
						"/driverEntity/downloadDriverFile", "/driverEntity/listDriverFile" });
	}

	/**
	 * 配置数据源URL构建器模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchemaUrlBuilder(HttpSecurity http) throws Exception
	{
		configAccessForRoleAdmin(http, //
				"/schemaUrlBuilder/*", //
				new String[] { "/schemaUrlBuilder/build" });
	}

	/**
	 * 配置用户模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForUser(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
				// 个人设置
				.antMatchers("/user/personalSet", "/user/savePersonalSet").access(AUTH_USER)
				// 选择
				.antMatchers("/user/select", "/user/pagingQueryData").access(AUTH_USER)
				// 管理
				.antMatchers("/user/**").access(AUTH_ADMIN);
	}

	/**
	 * 配置角色模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForRole(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
				// 选择
				.antMatchers("/role/select", "/role/pagingQueryData").access(AUTH_USER)
				// 管理
				.antMatchers("/role/**").access(AUTH_ADMIN);
	}

	/**
	 * 配置数据源防护模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForSchemaGuard(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
				.antMatchers("/schemaGuard/**").access(AUTH_ADMIN);
	}

	/**
	 * 配置数据授权模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForAuthorization(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/authorization/**").access(AUTH_USER_AND_DATA_MANAGER);
	}

	/**
	 * 配置登录模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForLogin(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/login/**").permitAll();
	}

	/**
	 * 配置注册模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForRegister(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/register/**").permitAll();
	}

	/**
	 * 配置重置密码模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForResetPassword(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/resetPassword/**").permitAll();
	}

	/**
	 * 配置切换主题模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForChangeTheme(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/changeThemeData/**").permitAll();
	}

	/**
	 * 配置校验码模块访问权限。
	 * 
	 * @param http
	 * @throws Exception
	 */
	protected void configAccessForCheckCode(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/checkCode/**").permitAll();
	}

	/**
	 * 配置{@linkplain #configAccessBeforeAllOther(HttpSecurity)}之前的访问权限。
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
		boolean disableAnonymous = this.coreConfig.getApplicationProperties().isDisableAnonymous();

		http.authorizeRequests().antMatchers("/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER);
	}

	/**
	 * 配置{@linkplain Role#ROLE_DATA_MANAGER}、{@linkplain Role#ROLE_DATA_ANALYST}角色相关的访问权限。
	 * 
	 * @param http
	 * @param managerUrlPatterns
	 * @param otherUrlPatterns
	 * @throws Exception
	 */
	protected void configAccessForRoleManagerAnalyst(HttpSecurity http, String[] managerUrlPatterns,
			String otherUrlPattern) throws Exception
	{
		boolean disableAnonymous = this.coreConfig.getApplicationProperties().isDisableAnonymous();

		http.authorizeRequests()
				// 管理
				.antMatchers(managerUrlPatterns).access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers(otherUrlPattern)
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST);
	}

	/**
	 * 配置{@linkplain AuthUser#ROLE_ADMIN}角色相关的访问权限。
	 * 
	 * @param http
	 * @param managerUrlPattern
	 * @param otherUrlPatterns
	 * @throws Exception
	 */
	protected void configAccessForRoleAdmin(HttpSecurity http, String managerUrlPattern,
			String[] otherUrlPatterns) throws Exception
	{
		boolean disableAnonymous = this.coreConfig.getApplicationProperties().isDisableAnonymous();

		http.authorizeRequests()
				// 其他
				.antMatchers(otherUrlPatterns)
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers(managerUrlPattern).access(AUTH_ADMIN);
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

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		ApplicationContext context = event.getApplicationContext();

		inflateAuthenticationSuccessHandler(context);
	}

	protected void inflateAuthenticationSuccessHandler(ApplicationContext context)
	{
		List<CreateUserEntityService> serviceList = CoreConfigSupport.getCreateUserEntityServices(context);

		AuthenticationSuccessHandler ash = authenticationSuccessHandler();

		if (ash instanceof AuthenticationSuccessHandlerImpl)
			((AuthenticationSuccessHandlerImpl) ash).setCreateUserEntityServices(serviceList);
	}
}
