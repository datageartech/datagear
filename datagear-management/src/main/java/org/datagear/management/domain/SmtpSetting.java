/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.io.Serializable;

/**
 * SMTP设置。
 * 
 * @author datagear@163.com
 *
 */
public class SmtpSetting implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 服务器主机 */
	private String host;

	/** 服务器端口 */
	private int port;

	/** 用户名 */
	private String username;

	/** 密码 */
	private String password;

	/** 连接类型 */
	private ConnectionType connectionType;

	/** 系统邮箱 */
	private String systemEmail;

	public SmtpSetting()
	{
		super();
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public ConnectionType getConnectionType()
	{
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType)
	{
		this.connectionType = connectionType;
	}

	public String getSystemEmail()
	{
		return systemEmail;
	}

	public void setSystemEmail(String systemEmail)
	{
		this.systemEmail = systemEmail;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connectionType == null) ? 0 : connectionType.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + ((systemEmail == null) ? 0 : systemEmail.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		SmtpSetting other = (SmtpSetting) obj;
		if (connectionType != other.connectionType)
			return false;
		if (host == null)
		{
			if (other.host != null)
				return false;
		}
		else if (!host.equals(other.host))
			return false;
		if (password == null)
		{
			if (other.password != null)
				return false;
		}
		else if (!password.equals(other.password))
			return false;
		if (port != other.port)
			return false;
		if (systemEmail == null)
		{
			if (other.systemEmail != null)
				return false;
		}
		else if (!systemEmail.equals(other.systemEmail))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [host=" + host + ", port=" + port + ", username=" + username
				+ ", password=" + password + ", connectionType=" + connectionType + ", systemEmail=" + systemEmail
				+ "]";
	}

	public static enum ConnectionType
	{
		/** 简单SMPT连接 */
		PLAIN,

		/** SSL安全连接 */
		SSL,

		/** TLS安全连接 */
		TLS
	}
}
