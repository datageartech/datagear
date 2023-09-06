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

package org.datagear.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.datagear.web.util.DelegatingTextEncryptor.EncryptType;
import org.junit.Test;

/**
 * {@linkplain DelegatingTextEncryptor}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class DelegatingTextEncryptorTest
{
	protected static final String SECRET_KEY = "RGF0YUdlYXI=";
	protected static final String SALT = "4461746147656172";
	
	private DelegatingTextEncryptor encryptorNoop = new DelegatingTextEncryptor(EncryptType.NOOP, SECRET_KEY, SALT);
	private DelegatingTextEncryptor encryptorStd = new DelegatingTextEncryptor(EncryptType.STD, SECRET_KEY, SALT);
	
	@Test
	public void test()
	{
		String password = "1234";
		
		{
			String encrypt = encryptorNoop.encrypt(password);
			
			String decrypt0 = encryptorNoop.decrypt(encrypt);
			String decrypt1 = encryptorStd.decrypt(encrypt);
			
			assertEquals(DelegatingTextEncryptor.ENCRYPT_TYPE_PREFIX_NOOP + password, encrypt);
			assertEquals(password, decrypt0);
			assertEquals(password, decrypt1);
		}

		{
			String encrypt = encryptorStd.encrypt(password);
			
			String decrypt0 = encryptorNoop.decrypt(encrypt);
			String decrypt1 = encryptorStd.decrypt(encrypt);
			
			assertTrue(encrypt.startsWith(DelegatingTextEncryptor.ENCRYPT_TYPE_PREFIX_STD));
			assertEquals(password, decrypt0);
			assertEquals(password, decrypt1);
		}
	}

	@Test
	public void test_change_secret_key()
	{
		String password = "1234";
		String encrypt = encryptorStd.encrypt(password);

		DelegatingTextEncryptor myEncryptorStd = new DelegatingTextEncryptor(EncryptType.STD,
				"SD" + SECRET_KEY.substring(2), SALT);

		assertThrows(Exception.class, () ->
		{
			myEncryptorStd.decrypt(encrypt);
		});
	}

	@Test
	public void test_change_salt()
	{
		String password = "1234";
		String encrypt = encryptorStd.encrypt(password);

		DelegatingTextEncryptor myEncryptorStd = new DelegatingTextEncryptor(EncryptType.STD, SECRET_KEY,
				SALT.substring(0, SALT.length() - 2) + "35");

		assertThrows(Exception.class, () ->
		{
			myEncryptorStd.decrypt(encrypt);
		});
	}

	@Test
	public void test_safeDecrypt()
	{
		String password = "1234";
		String encrypt = encryptorStd.encrypt(password);

		DelegatingTextEncryptor myEncryptorStd = new DelegatingTextEncryptor(EncryptType.STD, SECRET_KEY,
				SALT.substring(0, SALT.length() - 2) + "35");
		myEncryptorStd.setSafeDecrypt(true);

		{
			String decrypt = myEncryptorStd.decrypt(encrypt);
			assertEquals("", decrypt);
		}

		{
			myEncryptorStd.setDefaultSafeDecryptValue("myDefault");
			String decrypt = myEncryptorStd.decrypt(encrypt);
			assertEquals("myDefault", decrypt);
		}
	}
}
