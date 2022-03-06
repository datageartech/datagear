/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import org.datagear.management.service.impl.DashboardSharePasswordCrypto;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * {@linkplain DashboardSharePasswordCrypto}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardSharePasswordCryptoImpl implements DashboardSharePasswordCrypto
{
	private TextEncryptor textEncryptor;

	public DashboardSharePasswordCryptoImpl()
	{
		super();
	}

	public TextEncryptor getTextEncryptor()
	{
		return textEncryptor;
	}

	public void setTextEncryptor(TextEncryptor textEncryptor)
	{
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String encrypt(String password)
	{
		if (this.textEncryptor == null)
			return password;

		if (password == null || "".equals(password))
			return password;

		return this.textEncryptor.encrypt(password);
	}

	@Override
	public String decrypt(String password)
	{
		if (this.textEncryptor == null)
			return password;

		if (password == null || "".equals(password))
			return password;

		return this.textEncryptor.decrypt(password);
	}
}
