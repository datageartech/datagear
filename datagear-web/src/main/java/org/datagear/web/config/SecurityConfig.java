/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * 安全配置。
 * 
 * @author datagear@163.com
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationListener<ContextRefreshedEvent>
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

	private CoreConfig coreConfig;

	@Autowired
	public SecurityConfig(CoreConfig coreConfig)
	{
		super();
		this.coreConfig = coreConfig;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
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

	@Override
	public void configure(WebSecurity web) throws Exception
	{
		// 静态资源
		web.ignoring().antMatchers("/static/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		boolean disableAnonymous = this.coreConfig.getApplicationProperties().isDisableAnonymous();

		// 默认是开启CSRF的，系统目前没有提供相关支持，因此需禁用
		http.csrf().disable();

		// 默认"X-Frame-Options"值为"DENY"，这会导致系统的图表/看板展示页面无法被其他应用嵌入iframe，因此需禁用
		http.headers().frameOptions().disable();

		http.authorizeRequests()

				// 切换主题、校验码
				.antMatchers("/changeThemeData/**", "/checkCode/**").permitAll()

				// 展示图表和看板
				// 注意：无论系统是否允许匿名用户访问，它们都应允许匿名用户访问，用于支持外部系统iframe嵌套场景
				.antMatchers(
						//图表插件
						"/chartPlugin/icon/*", "/chartPlugin/chartPluginManager.js",
						//图表展示
						"/chart/show/**", "/chart/showData",
						//看板展示
						"/dashboard/show/**", "/dashboard/showData", "/dashboard/loadChart", "/dashboard/heartbeat",
						"/dashboard/serverTime.js", "/dashboard/auth/**", "/dashboard/authcheck/**")
				.access(AUTH_ANONYMOUS_OR_USER)

				// 展示图表和看板
				// 用于兼容2.6.0版本的图表、看板展示URL，参考CompatibleController
				.antMatchers("/analysis/chart/show/**", "/analysis/dashboard/show/**")
				.access(AUTH_ANONYMOUS_OR_USER)

				// 数据源
				// 编辑
				.antMatchers("/schema/add", "/schema/saveAdd",
						"/schema/edit", "/schema/saveEdit",
						"/schema/delete")
				.access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers("/schema/**")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)

				// 数据源数据管理、导入导出、SQL工作台、SQL编辑器
				// 用户针对数据源数据的所有操作都已受其所属数据源权限控制，所以不必再引入数据管理员/数据分析员权限
				.antMatchers("/data/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/dataexchange/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/sqlpad/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)
				.antMatchers("/sqlEditor/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)

				// 数据分析项目
				// 编辑
				.antMatchers("/analysisProject/add", "/analysisProject/saveAdd",
						"/analysisProject/edit", "/analysisProject/saveEdit",
						"/analysisProject/delete")
				.access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers("/analysisProject/**")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)

				// 数据集
				// 编辑
				.antMatchers("/dataSet/addFor*", "/dataSet/saveAddFor*",
						"/dataSet/edit", "/dataSet/saveEditFor*", "/dataSet/copy",
						"/dataSet/delete",
						"/dataSet/uploadFile")
				.access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers("/dataSet/**")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)

				// 图表
				// 编辑
				.antMatchers("/chart/add", "/chart/edit",  "/chart/copy",
						"/chart/save", "/chart/delete")
				.access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers("/chart/**")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)

				// 看板
				// 编辑
				.antMatchers("/dashboard/add", "/dashboard/edit", "/dashboard/save", "/dashboard/copy",
						"/dashboard/saveTemplateNames", "/dashboard/deleteResource",
						"/dashboard/uploadResourceFile", "/dashboard/saveUploadResourceFile",
						"/dashboard/saveResourceContent",
						"/dashboard/import", "/dashboard/uploadImportFile",
						"/dashboard/saveImport", "/dashboard/delete",
						"/dashboard/shareSet", "/dashboard/saveShareSet")
				.access(AUTH_USER_AND_DATA_MANAGER)
				// 其他
				.antMatchers("/dashboard/**")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)

				// 图表插件
				// 选择
				.antMatchers("/chartPlugin/select", "/chartPlugin/selectData")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers("/chartPlugin/**").access(AUTH_ADMIN)

				// 数据集资源
				// 选择
				.antMatchers("/dataSetResDirectory/view", "/dataSetResDirectory/select",
						"/dataSetResDirectory/pagingQueryData", "/dataSetResDirectory/listFiles")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers("/dataSetResDirectory/**").access(AUTH_ADMIN)

				// 看板全局资源
				// 选择
				.antMatchers("/dashboardGlobalRes/queryData")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers("/dashboardGlobalRes/**").access(AUTH_ADMIN)

				// 数据授权
				.antMatchers("/authorization/**").access(AUTH_USER_AND_DATA_MANAGER)

				// 驱动程序
				// 选择
				.antMatchers("/driverEntity/view", "/driverEntity/select", "/driverEntity/queryData",
						"/driverEntity/downloadDriverFile", "/driverEntity/listDriverFile")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers("/driverEntity/**").access(AUTH_ADMIN)

				// 数据源URL构建器
				// 构建
				.antMatchers("/schemaUrlBuilder/build")
				.access(disableAnonymous ? AUTH_USER_AND_DATA_MANAGER_OR_ANALYST : AUTH_DATA_MANAGER_OR_ANALYST)
				// 管理
				.antMatchers("/schemaUrlBuilder/*").access(AUTH_ADMIN)

				// 用户
				// 个人设置
				.antMatchers("/user/personalSet", "/user/savePersonalSet").access(AUTH_USER)
				// 选择
				.antMatchers("/user/select", "/user/pagingQueryData").access(AUTH_USER)
				// 管理
				.antMatchers("/user/**").access(AUTH_ADMIN)

				// 角色
				// 选择
				.antMatchers("/role/select", "/role/pagingQueryData").access(AUTH_USER)
				// 管理
				.antMatchers("/role/**").access(AUTH_ADMIN)

				// 数据源防护
				.antMatchers("/schemaGuard/**").access(AUTH_ADMIN)

				//
				.antMatchers("/login/**", "/register/**", "/resetPassword/**").permitAll()

				//
				.antMatchers("/**").access(disableAnonymous ? AUTH_USER : AUTH_ANONYMOUS_OR_USER)

				.and().formLogin().loginPage(LoginController.LOGIN_PAGE).loginProcessingUrl(LOGIN_PROCESS_URL)
				.usernameParameter(LoginController.LOGIN_PARAM_USER_NAME)
				.passwordParameter(LoginController.LOGIN_PARAM_PASSWORD)
				.successHandler(this.authenticationSuccessHandler())
				.failureHandler(this.authenticationFailureHandler())

				.and().logout().logoutUrl("/logout").invalidateHttpSession(true).logoutSuccessUrl("/")

				.and().rememberMe().key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24 * 365)
				.rememberMeParameter(LoginController.LOGIN_PARAM_REMEMBER_ME).rememberMeCookieName("REMEMBER_ME");

		configureAnonymous(http);

		http.addFilterBefore(
				new LoginLatchFilter(LOGIN_PROCESS_URL,
						(AuthenticationFailureHandlerImpl) this.authenticationFailureHandler(),
						this.coreConfig.getApplicationProperties(), this.coreConfig.checkCodeManager()),
				UsernamePasswordAuthenticationFilter.class);
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
	@Override
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
		List<CreateUserEntityService> serviceList = CoreConfig.getCreateUserEntityServices(context);

		AuthenticationSuccessHandler ash = authenticationSuccessHandler();

		if (ash instanceof AuthenticationSuccessHandlerImpl)
			((AuthenticationSuccessHandlerImpl) ash).setCreateUserEntityServices(serviceList);
	}
}
