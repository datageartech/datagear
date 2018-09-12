/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.webembd;

import java.io.Serializable;

/**
 * 程序配置。
 * 
 * @author datagear@163.com
 *
 */
public class AppConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 服务端口 */
	private int serverPort;

	/** web应用的位置 */
	private String webappLocation;

	public AppConfig()
	{
		super();
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
	}

	public String getWebappLocation()
	{
		return webappLocation;
	}

	public void setWebappLocation(String webappLocation)
	{
		this.webappLocation = webappLocation;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + serverPort;
		result = prime * result + ((webappLocation == null) ? 0 : webappLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppConfig other = (AppConfig) obj;
		if (serverPort != other.serverPort)
			return false;
		if (webappLocation == null)
		{
			if (other.webappLocation != null)
				return false;
		}
		else if (!webappLocation.equals(other.webappLocation))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [serverPort=" + serverPort + ", webappLocation=" + webappLocation + "]";
	}
}
