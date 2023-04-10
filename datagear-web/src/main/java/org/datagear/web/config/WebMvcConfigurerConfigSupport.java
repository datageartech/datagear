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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.datagear.util.IOUtil;
import org.datagear.web.config.support.ClearCssCommentResourceTransformer;
import org.datagear.web.config.support.ClearJsCommentResourceTransformer;
import org.datagear.web.config.support.CustomErrorPageRegistrar;
import org.datagear.web.config.support.EnumCookieThemeResolver;
import org.datagear.web.freemarker.CustomFreeMarkerView;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.ThemeSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web配置。
 * <p>
 * 子类应该添加如下注解：
 * </p>
 * <pre>
 * {@code @Configuration}
 * {@code @ComponentScan(basePackageClasses = org.datagear.web.controller.MainController.class)}
 * </pre>
 * <p>
 * Spring会递归处理{@linkplain Configuration @Configuration}类的父类，可能会导致某些非预期的父类配置被加载，
 * 所以此类没有添加{@linkplain Configuration @Configuration}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class WebMvcConfigurerConfigSupport implements WebMvcConfigurer
{
	/** 系统静态资源路径通配模式 */
	public static final String STATIC_RES_PATH_PATTERN = "/static/**";

	/** 系统CSS静态资源路径通配模式 */
	public static final String STATIC_RES_CSS_PATH_PATTERN = "/static/css/**/*.css";

	/** 系统JS静态资源路径通配模式 */
	public static final String STATIC_RES_JS_PATH_PATTERN = "/static/script/**/*.js";

	/** 系统theme静态资源路径通配模式 */
	public static final String STATIC_RES_THEME_PATH_PATTERN = "/static/theme/**/*.css";

	/** 系统静态资源位置 */
	public static final String STATIC_RES_LOCATION = "classpath:/org/datagear/web/static/";

	/** Freemarker模板路径 */
	public static final String FREEMARKER_TEMPLATE_PATH = "classpath:org/datagear/web/templates/";

	/** 系统主题配置基础前缀 */
	public static final String THEME_SOURCE_BASENAME_PREFIX = "org.datagear.web.theme.";

	private CoreConfigSupport coreConfig;

	@Autowired
	public WebMvcConfigurerConfigSupport(CoreConfigSupport coreConfig)
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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler(STATIC_RES_PATH_PATTERN).addResourceLocations(STATIC_RES_LOCATION)
			.resourceChain(false)
			.addTransformer(clearCssCommentResourceTransformer())
			.addTransformer(clearJsCommentResourceTransformer());
	}
	
	protected ClearCssCommentResourceTransformer clearCssCommentResourceTransformer()
	{
		return new ClearCssCommentResourceTransformer(STATIC_RES_CSS_PATH_PATTERN, STATIC_RES_THEME_PATH_PATTERN);
	}
	
	protected ClearJsCommentResourceTransformer clearJsCommentResourceTransformer()
	{
		return new ClearJsCommentResourceTransformer(STATIC_RES_JS_PATH_PATTERN);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		ThemeChangeInterceptor themeChangeInterceptor = createThemeChangeInterceptor();
		registry.addInterceptor(themeChangeInterceptor).addPathPatterns("/changeThemeData");

		LocaleChangeInterceptor localeChangeInterceptor = createLocaleChangeInterceptor();
		// 忽略非法语言，避免抛出异常
		localeChangeInterceptor.setIgnoreInvalidLocale(true);
		registry.addInterceptor(localeChangeInterceptor).addPathPatterns("/changeLocale");
	}

	protected ThemeChangeInterceptor createThemeChangeInterceptor()
	{
		return new ThemeChangeInterceptor();
	}

	protected LocaleChangeInterceptor createLocaleChangeInterceptor()
	{
		return new LocaleChangeInterceptor();
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		int oldIndex = -1;

		for (int i = 0; i < converters.size(); i++)
		{
			HttpMessageConverter<?> converter = converters.get(i);

			if (converter instanceof MappingJackson2HttpMessageConverter)
			{
				oldIndex = i;
				break;
			}
		}

		ObjectMapper objectMapper = this.coreConfig.objectMapperBuilder().build();
		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

		if (oldIndex > -1)
			converters.add(oldIndex, messageConverter);
		else
			converters.add(messageConverter);
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry)
	{
		FreeMarkerViewResolver viewResolver = createFreeMarkerViewResolver();
		viewResolver.setViewClass(CustomFreeMarkerView.class);
		viewResolver.setContentType("text/html;charset=" + IOUtil.CHARSET_UTF_8);
		viewResolver.setExposeRequestAttributes(true);
		viewResolver.setAllowRequestOverride(true);
		viewResolver.setCache(true);
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".ftl");

		registry.viewResolver(viewResolver);
	}

	protected FreeMarkerViewResolver createFreeMarkerViewResolver()
	{
		return new FreeMarkerViewResolver();
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer()
	{
		FreeMarkerConfigurer bean = createFreeMarkerConfigurer();

		Properties settings = new Properties();
		settings.setProperty("datetime_format", org.datagear.util.DateFormat.DEFAULT_TIMESTAMP_FORMAT);
		settings.setProperty("date_format", org.datagear.util.DateFormat.DEFAULT_DATE_FORMAT);
		settings.setProperty("number_format",
				org.datagear.analysis.support.DataSetFmkTemplateResolver.FREEMARKER_NUMBER_FORMAT_COMPUTER);

		// 开启自动转义功能，并设置默认转义格式为HTML，页面不再需要每个地方都转义
		// ${content?html}
		// 可简写为
		// ${content}
		settings.setProperty("output_format", "HTMLOutputFormat");

		Map<String, Object> variables = new HashMap<>();
		variables.put("writeJson", new WriteJsonTemplateDirectiveModel(this.coreConfig.objectMapperBuilder()));

		bean.setTemplateLoaderPaths(getFreeMarkerTemplateLoaderPaths());
		bean.setDefaultEncoding(IOUtil.CHARSET_UTF_8);
		bean.setFreemarkerSettings(settings);
		bean.setFreemarkerVariables(variables);

		return bean;
	}
	
	protected FreeMarkerConfigurer createFreeMarkerConfigurer()
	{
		return new FreeMarkerConfigurer();
	}
	
	protected String[] getFreeMarkerTemplateLoaderPaths()
	{
		return new String[] { FREEMARKER_TEMPLATE_PATH };
	}

	@Bean("themeSource")
	public ResourceBundleThemeSource themeSource()
	{
		ResourceBundleThemeSource bean = new ResourceBundleThemeSource();
		bean.setBasenamePrefix(THEME_SOURCE_BASENAME_PREFIX);

		return bean;
	}

	@Bean("themeResolver")
	public EnumCookieThemeResolver themeResolver()
	{
		EnumCookieThemeResolver bean = new EnumCookieThemeResolver(this.themeSpec());
		return bean;
	}
	
	@Bean
	public ThemeSpec themeSpec()
	{
		ThemeSpec bean = new ThemeSpec();
		return bean;
	}

	@Bean("localeResolver")
	public LocaleResolver localeResolver()
	{
		CookieLocaleResolver bean = new CookieLocaleResolver();
		bean.setCookieMaxAge(60 * 60 * 24 * 365);
		bean.setCookieName("LOCALE");

		// 不设置默认语言，这样当没有"LOCALE"的cookie值时，可以根据客户端语言自动选择更合适的i18n
		// bean.setDefaultLocale(Locale.CHINESE);

		return bean;
	}

	@Bean("multipartResolver")
	@ConfigurationProperties("multipart")
	public MultipartResolver multipartResolver()
	{
		CommonsMultipartResolver bean = new CommonsMultipartResolver();
		return bean;
	}

	@Bean
	public ErrorPageRegistrar errorPageRegistrar()
	{
		CustomErrorPageRegistrar bean = new CustomErrorPageRegistrar();
		return bean;
	}
}
