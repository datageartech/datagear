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

import org.springframework.web.cors.CorsConfiguration;

/**
 * 跨域请求配置项。
 * <p>
 * 参考{@linkplain CorsConfiguration}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class CrossOriginProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String[] paths;

	private String[] allowedOrigins;

	private String[] allowedOriginPatterns;

	private String[] allowedMethods;

	private String[] allowedHeaders;

	private String[] exposedHeaders;

	private boolean allowCredentials;

	private Long maxAge;

	public CrossOriginProperties()
	{
		super();
	}

	public String[] getPaths()
	{
		return paths;
	}

	protected void setPaths(String[] paths)
	{
		this.paths = paths;
	}

	public String[] getAllowedOrigins()
	{
		return allowedOrigins;
	}

	public void setAllowedOrigins(String[] allowedOrigins)
	{
		this.allowedOrigins = allowedOrigins;
	}

	public String[] getAllowedOriginPatterns()
	{
		return allowedOriginPatterns;
	}

	protected void setAllowedOriginPatterns(String[] allowedOriginPatterns)
	{
		this.allowedOriginPatterns = allowedOriginPatterns;
	}

	public String[] getAllowedMethods()
	{
		return allowedMethods;
	}

	protected void setAllowedMethods(String[] allowedMethods)
	{
		this.allowedMethods = allowedMethods;
	}

	public String[] getAllowedHeaders()
	{
		return allowedHeaders;
	}

	protected void setAllowedHeaders(String[] allowedHeaders)
	{
		this.allowedHeaders = allowedHeaders;
	}

	public String[] getExposedHeaders()
	{
		return exposedHeaders;
	}

	protected void setExposedHeaders(String[] exposedHeaders)
	{
		this.exposedHeaders = exposedHeaders;
	}

	public boolean isAllowCredentials()
	{
		return allowCredentials;
	}

	protected void setAllowCredentials(boolean allowCredentials)
	{
		this.allowCredentials = allowCredentials;
	}

	public Long getMaxAge()
	{
		return maxAge;
	}

	protected void setMaxAge(Long maxAge)
	{
		this.maxAge = maxAge;
	}
}
