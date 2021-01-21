/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.datagear.management.service.impl.SmtpSettingPasswordEncryptor;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;

/**
 * {@linkplain SmtpSettingPasswordEncryptor}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SmtpSettingPasswordEncryptorImpl implements SmtpSettingPasswordEncryptor
{
	private static final String ALGORITHM_DES = "DES";
	private static final String ENCRYPT_PASSWORD = "ZY150401";

	public SmtpSettingPasswordEncryptorImpl()
	{
		super();
	}

	@Override
	public String encrypt(String password)
	{
		SecureRandom random = new SecureRandom();

		try
		{
			DESKeySpec desKey = new DESKeySpec(ENCRYPT_PASSWORD.getBytes("iso-8859-1"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

			byte[] passwordBytes = Utf8.encode(password);
			byte[] result = cipher.doFinal(passwordBytes);

			return new String(Hex.encode(result));
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new EncryptorException(e);
		}
	}

	@Override
	public String decrypt(String encryptedPassword)
	{
		SecureRandom random = new SecureRandom();

		try
		{
			DESKeySpec desKey = new DESKeySpec(ENCRYPT_PASSWORD.getBytes("iso-8859-1"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);

			byte[] passwordBytes = Hex.decode(encryptedPassword);
			byte[] result = cipher.doFinal(passwordBytes);

			return new String(Utf8.decode(result));
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new EncryptorException(e);
		}
	}
}
