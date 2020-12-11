/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.datagear.management.service.CreateUserEntityService;
import org.datagear.util.StringUtil;
import org.datagear.web.security.AnonymousAuthenticationFilterExt;
import org.datagear.web.security.AuthUser;
import org.datagear.web.security.AuthenticationSuccessHandlerImpl;
import org.datagear.web.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * 安全配置。
 * 
 * @author datagear@163.com
 */
@Configuration
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
		boolean disableAnonymous = isDisableAnonymous();

		// 默认是开启CSRF的，系统目前没有提供相关支持，因此需禁用
		http.csrf().disable();

		// 默认"X-Frame-Options"值为"DENY"，这会导致系统的图表/看板展示页面无法被其他应用嵌入iframe，因此需禁用
		http.headers().frameOptions().disable();

		http.authorizeRequests()
				// 静态资源
				.antMatchers("/static/**").permitAll()

				// 图表、看板展示功能始终允许匿名用户访问，用于支持外部系统iframe嵌套场景
				.antMatchers("/analysis/chartPlugin/icon/*", "/analysis/chartPlugin/chartPluginManager.js",
						"/analysis/chart/show/**", "/analysis/chart/showData", "/analysis/dashboard/show/**",
						"/analysis/dashboard/showData", "/analysis/dashboard/loadChart",
						"/analysis/dashboard/heartbeat")
				.permitAll()

				// 切换主题
				.antMatchers("/changeThemeData/**").permitAll()

				// 驱动程序管理
				.antMatchers("/driverEntity/add", "/driverEntity/saveAdd", "/driverEntity/import",
						"/driverEntity/uploadImportFile", "/driverEntity/saveImport", "/driverEntity/edit",
						"/driverEntity/saveEdit", "/driverEntity/delete", "/driverEntity/query",
						"/driverEntity/uploadDriverFile", "/driverEntity/deleteDriverFile")
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 用户管理
				.antMatchers("/user/personalSet", "/user/savePersonalSet", "/user/select", "/user/pagingQueryData")
				.hasAuthority(AuthUser.ROLE_USER)
				//
				.antMatchers("/user/**").hasAuthority(AuthUser.ROLE_ADMIN)

				// 角色管理
				.antMatchers("/role/select", "/role/pagingQueryData").hasAuthority(AuthUser.ROLE_USER)
				//
				.antMatchers("/role/**").hasAuthority(AuthUser.ROLE_ADMIN)

				// 权限管理
				.antMatchers("/authorization/**").hasAuthority(AuthUser.ROLE_USER)

				// 设置数据库URL构建器脚本
				.antMatchers("/schemaUrlBuilder/editScriptCode", "/schemaUrlBuilder/saveScriptCode",
						"/schemaUrlBuilder/previewScriptCode")
				.hasAuthority(AuthUser.ROLE_ADMIN)

				// 图表插件管理
				.antMatchers("/analysis/chartPlugin/select", "/analysis/chartPlugin/selectData")
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)
				.antMatchers("/analysis/chartPlugin/**").hasAuthority(AuthUser.ROLE_ADMIN)

				// 数据集资源目录管理
				.antMatchers("/dataSetResDirectory/view", "/dataSetResDirectory/select",
						"/dataSetResDirectory/pagingQueryData", "/dataSetResDirectory/listFiles")
				.hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)
				.antMatchers("/dataSetResDirectory/**").hasAuthority(AuthUser.ROLE_ADMIN)

				//
				.antMatchers("/login/**", "/register/**", "/resetPassword/**").hasAuthority(AuthUser.ROLE_ANONYMOUS)

				//
				.antMatchers("/**").hasAnyAuthority(disableAnonymous ? ROLES_USER : ROLES_ANONYMOUS_AND_USER)

				.and().formLogin().loginPage("/login").loginProcessingUrl("/login/doLogin").usernameParameter("name")
				.passwordParameter("password").successHandler(getAuthenticationSuccessHandler())

				.and().logout().logoutUrl("/logout").invalidateHttpSession(true).logoutSuccessUrl("/")

				.and().rememberMe().key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24 * 365)
				.rememberMeParameter("rememberMe").rememberMeCookieName("REMEMBER_ME");

		configureAnonymous(http);
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

		String[] anonymousRoleIds = StringUtil.split(this.environment.getProperty("defaultRole.anonymous"), ",", true);
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
}
