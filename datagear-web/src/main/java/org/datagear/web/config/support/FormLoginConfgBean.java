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

package org.datagear.web.config.support;

import org.datagear.util.Global;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.config.SecurityConfigSupport;
import org.datagear.web.controller.LoginController;
import org.datagear.web.security.AjaxAwareAuthenticationEntryPoint;
import org.datagear.web.security.AuthenticationFailureHandlerExt;
import org.datagear.web.security.AuthenticationSuccessHandlerExt;
import org.datagear.web.security.AuthenticationUserGetter;
import org.datagear.web.security.LoginLatchFilter;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.IpLoginLatch;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 用户名/密码表单登录配置Bean。
 * 
 * @author datagear@163.com
 *
 */
public class FormLoginConfgBean
{
	private ApplicationProperties applicationProperties;

	private CheckCodeManager checkCodeManager;

	private IpLoginLatch ipLoginLatch;

	private UsernameLoginLatch usernameLoginLatch;

	private AuthenticationUserGetter authenticationUserGetter;

	private String loginPage = LoginController.LOGIN_PAGE;

	private String loginParamUserName = LoginController.LOGIN_PARAM_USER_NAME;

	private String loginParamPassword = LoginController.LOGIN_PARAM_PASSWORD;

	private String loginParamRememberMe = LoginController.LOGIN_PARAM_REMEMBER_ME;

	private String loginProcessUrl = SecurityConfigSupport.LOGIN_PROCESS_URL;

	private String loginPageSuccess = LoginController.LOGIN_PAGE_SUCCESS;

	private String loginPageError = LoginController.LOGIN_PAGE_ERROR;

	private String logoutUrl = SecurityConfigSupport.LOGOUT_URL;

	private String logoutSuccessUrl = WebUtils.INDEX_PAGE_URL;

	public FormLoginConfgBean()
	{
		super();
	}

	public FormLoginConfgBean(ApplicationProperties applicationProperties, CheckCodeManager checkCodeManager,
			IpLoginLatch ipLoginLatch, UsernameLoginLatch usernameLoginLatch,
			AuthenticationUserGetter authenticationUserGetter)
	{
		super();
		this.applicationProperties = applicationProperties;
		this.checkCodeManager = checkCodeManager;
		this.ipLoginLatch = ipLoginLatch;
		this.usernameLoginLatch = usernameLoginLatch;
		this.authenticationUserGetter = authenticationUserGetter;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	public String getLoginPage()
	{
		return loginPage;
	}

	public void setLoginPage(String loginPage)
	{
		this.loginPage = loginPage;
	}

	public String getLoginParamUserName()
	{
		return loginParamUserName;
	}

	public void setLoginParamUserName(String loginParamUserName)
	{
		this.loginParamUserName = loginParamUserName;
	}

	public String getLoginParamPassword()
	{
		return loginParamPassword;
	}

	public void setLoginParamPassword(String loginParamPassword)
	{
		this.loginParamPassword = loginParamPassword;
	}

	public String getLoginParamRememberMe()
	{
		return loginParamRememberMe;
	}

	public void setLoginParamRememberMe(String loginParamRememberMe)
	{
		this.loginParamRememberMe = loginParamRememberMe;
	}

	public String getLoginProcessUrl()
	{
		return loginProcessUrl;
	}

	public void setLoginProcessUrl(String loginProcessUrl)
	{
		this.loginProcessUrl = loginProcessUrl;
	}

	public String getLoginPageSuccess()
	{
		return loginPageSuccess;
	}

	public void setLoginPageSuccess(String loginPageSuccess)
	{
		this.loginPageSuccess = loginPageSuccess;
	}

	public String getLoginPageError()
	{
		return loginPageError;
	}

	public void setLoginPageError(String loginPageError)
	{
		this.loginPageError = loginPageError;
	}

	public String getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl)
	{
		this.logoutUrl = logoutUrl;
	}

	public String getLogoutSuccessUrl()
	{
		return logoutSuccessUrl;
	}

	public void setLogoutSuccessUrl(String logoutSuccessUrl)
	{
		this.logoutSuccessUrl = logoutSuccessUrl;
	}

	/**
	 * 配置{@linkplain HttpSecurity}。
	 * 
	 * @param http
	 * @throws Exception
	 */
	public void configHttpSecurity(HttpSecurity http) throws Exception
	{
		FormLoginConfigurer<HttpSecurity> formConfig = http.formLogin();

		// 登录
		formConfig.loginPage(this.loginPage).loginProcessingUrl(this.loginProcessUrl)
				.usernameParameter(this.loginParamUserName)
				.passwordParameter(this.loginParamPassword)
				.successHandler(this.authenticationSuccessHandler()).failureHandler(this.authenticationFailureHandler())

				// 退出
				.and().logout().logoutUrl(this.logoutUrl).logoutSuccessUrl(this.logoutSuccessUrl)

				// 记住登录
				.and().rememberMe().key(Global.NAME_SHORT_UCUS + "REMEMBER_ME_KEY")
				.tokenValiditySeconds(60 * 60 * 24 * 365).rememberMeParameter(this.loginParamRememberMe)
				.rememberMeCookieName(Global.NAME_SHORT_UCUS + "REMEMBER_ME");

		AjaxAwareAuthenticationEntryPoint entryPoint = new AjaxAwareAuthenticationEntryPoint(
				new LoginUrlAuthenticationEntryPoint(this.loginPage));

		http.exceptionHandling().authenticationEntryPoint(entryPoint);

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
				new LoginLatchFilter(this.loginProcessUrl,
						(AuthenticationFailureHandlerExt) this.authenticationFailureHandler(),
						this.applicationProperties, this.checkCodeManager),
				UsernamePasswordAuthenticationFilter.class);
	}

	protected AuthenticationSuccessHandler authenticationSuccessHandler()
	{
		AuthenticationSuccessHandlerExt bean = new AuthenticationSuccessHandlerExt(this.loginPageSuccess,
				this.usernameLoginLatch, this.checkCodeManager, this.authenticationUserGetter);

		return bean;
	}

	protected AuthenticationFailureHandler authenticationFailureHandler()
	{
		AuthenticationFailureHandlerExt bean = new AuthenticationFailureHandlerExt(this.loginPageError,
				this.ipLoginLatch, this.usernameLoginLatch, this.loginParamUserName);

		return bean;
	}
}
