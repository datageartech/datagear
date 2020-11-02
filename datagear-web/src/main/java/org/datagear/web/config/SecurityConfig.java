/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.util.Arrays;
import java.util.List;

import org.datagear.management.service.CreateUserEntityService;
import org.datagear.web.security.AuthUser;
import org.datagear.web.security.AuthenticationSuccessHandlerImpl;
import org.datagear.web.security.UserDetailsServiceImpl;
import org.datagear.web.util.WebContextPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 安全配置。
 * 
 * @author datagear@163.com
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
	protected static final String[] ROLES_ANONYMOUS_AND_USER = { AuthUser.ROLE_ANONYMOUS, AuthUser.ROLE_USER };

	protected static final String[] ROLES_USER = { AuthUser.ROLE_USER };

	private CoreConfig coreConfig;

	private Environment environment;

	@Autowired
	public SecurityConfig(CoreConfig coreConfig, Environment environment)
	{
		super();
		this.coreConfig = coreConfig;
		this.environment = environment;
	}

	public CoreConfig getCoreConfig()
	{
		return coreConfig;
	}

	public void setCoreConfig(CoreConfig coreConfig)
	{
		this.coreConfig = coreConfig;
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	/**
	 * 是否禁用匿名用户使用系统。
	 * 
	 * @return
	 */
	protected boolean isDisableAnonymous()
	{
		String disableStr = this.environment.getProperty("disableAnonymous");
		return (disableStr == null ? false : Boolean.TRUE.toString().equals(disableStr));
	}

	/**
	 * 获取子应用上下文路径，参考{@linkplain WebContextPath}。
	 * 
	 * @return
	 */
	protected String getSubContextPath()
	{
		String subContextPath = this.environment.getProperty("subContextPath");
		return WebContextPath.trimSubContextPath(subContextPath);
	}

	protected String concatPath(String subContextPath, String path)
	{
		return subContextPath + path;
	}

	protected AuthenticationSuccessHandler getAuthenticationSuccessHandler()
	{
		AuthenticationSuccessHandlerImpl bean = new AuthenticationSuccessHandlerImpl();

		List<CreateUserEntityService> createUserEntityServices = Arrays.asList(this.coreConfig.schemaService(),
				this.coreConfig.dataSetEntityService(), this.coreConfig.htmlChartWidgetEntityService(),
				this.coreConfig.htmlTplDashboardWidgetEntityService(), this.coreConfig.analysisProjectService());

		bean.setCreateUserEntityServices(createUserEntityServices);

		return bean;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		String subContextPath = getSubContextPath();
		boolean disableAnonymous = isDisableAnonymous();

		http.authorizeRequests()
				// 静态资源
				.antMatchers(concatPath(subContextPath, "/static/**")).permitAll()

				// 图表、看板展示功能始终允许匿名用户访问，用于支持外部系统iframe嵌套场景
				.antMatchers(concatPath(subContextPath, "/analysis/chartPlugin/icon/*"),
						concatPath(subContextPath, "/analysis/chartPlugin/chartPluginManager.js"),
						concatPath(subContextPath, "/analysis/chart/show/**"),
						concatPath(subContextPath, "/analysis/chart/showData"),
						concatPath(subContextPath, "/analysis/dashboard/show/**"),
						concatPath(subContextPath, "/analysis/dashboard/showData"),
						concatPath(subContextPath, "/analysis/dashboard/loadChart"),
						concatPath(subContextPath, "/analysis/dashboard/heartbeat"))
				.permitAll()

				// 切换主题
				.antMatchers(concatPath(subContextPath, "/changeThemeData/**")).permitAll()

				// cometd
				.antMatchers(concatPath(subContextPath, "/cometd/**"))
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)

				// 驱动程序管理
				.antMatchers(concatPath(subContextPath, "/driverEntity/add"),
						concatPath(subContextPath, "/driverEntity/saveAdd"),
						concatPath(subContextPath, "/driverEntity/import"),
						concatPath(subContextPath, "/driverEntity/uploadImportFile"),
						concatPath(subContextPath, "/driverEntity/saveImport"),
						concatPath(subContextPath, "/driverEntity/edit"),
						concatPath(subContextPath, "/driverEntity/saveEdit"),
						concatPath(subContextPath, "/driverEntity/delete"),
						concatPath(subContextPath, "/driverEntity/query"),
						concatPath(subContextPath, "/driverEntity/uploadDriverFile"),
						concatPath(subContextPath, "/driverEntity/deleteDriverFile"))
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 用户管理
				.antMatchers(concatPath(subContextPath, "/user/personalSet"),
						concatPath(subContextPath, "/user/savePersonalSet"), concatPath(subContextPath, "/user/select"),
						concatPath(subContextPath, "/user/queryData"))
				.hasAuthority(AuthUser.ROLE_USER).antMatchers(concatPath(subContextPath, "/user/**"))
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 角色管理
				.antMatchers(concatPath(subContextPath, "/role/select"), concatPath(subContextPath, "/role/queryData"))
				.hasAuthority(AuthUser.ROLE_USER).antMatchers(concatPath(subContextPath, "/role/**"))
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 权限管理
				.antMatchers(concatPath(subContextPath, "/authorization/**")).hasAuthority(AuthUser.ROLE_USER)

				// 设置数据库URL构建器脚本
				.antMatchers(concatPath(subContextPath, "/schemaUrlBuilder/editScriptCode"),
						concatPath(subContextPath, "/schemaUrlBuilder/saveScriptCode"),
						concatPath(subContextPath, "/schemaUrlBuilder/previewScriptCode"))
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 图表插件管理
				.antMatchers(concatPath(subContextPath, "/analysis/chartPlugin/select"),
						concatPath(subContextPath, "/analysis/chartPlugin/selectData"))
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)
				.antMatchers(concatPath(subContextPath, "/analysis/chartPlugin/**")).hasAuthority(AuthUser.ROLE_ADMIN)

				// 数据集资源目录管理
				.antMatchers(concatPath(subContextPath, "/dataSetResDirectory/view"),
						concatPath(subContextPath, "/dataSetResDirectory/select"),
						concatPath(subContextPath, "/dataSetResDirectory/pagingQueryData"),
						concatPath(subContextPath, "/dataSetResDirectory/listFiles"))
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)
				.antMatchers(concatPath(subContextPath, "/dataSetResDirectory/**")).hasAuthority(AuthUser.ROLE_ADMIN)

				//
				.antMatchers(concatPath(subContextPath, "/login/**"), concatPath(subContextPath, "/register/**"),
						concatPath(subContextPath, "/resetPassword/**"))
				.hasAuthority(AuthUser.ROLE_ANONYMOUS)

				//
				.antMatchers(concatPath(subContextPath, "/**"))
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)

				.and().formLogin().loginPage(concatPath(subContextPath, "/login"))
				.loginProcessingUrl(concatPath(subContextPath, "/login/doLogin")).usernameParameter("name")
				.passwordParameter("password").successHandler(getAuthenticationSuccessHandler())

				.and().logout().logoutUrl(concatPath(subContextPath, "/logout")).invalidateHttpSession(true)
				.logoutSuccessUrl(concatPath(subContextPath, "/"))

				.and().rememberMe().key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24 * 365)
		// TODO 配置"remember-me-parameter"为"autoLogin"
		;
	}

	@Override
	protected UserDetailsService userDetailsService()
	{
		UserDetailsService bean = new UserDetailsServiceImpl(this.coreConfig.userService());
		return bean;
	}
}
