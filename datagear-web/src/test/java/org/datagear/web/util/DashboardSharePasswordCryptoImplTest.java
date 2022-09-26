/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import static org.junit.Assert.*;

import org.datagear.web.util.DashboardSharePasswordCryptoImpl.EncryptType;
import org.junit.Test;

/**
 * {@linkplain DashboardSharePasswordCryptoImpl}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardSharePasswordCryptoImplTest
{
	protected static final String SECRET_KEY = "RGF0YUdlYXI=";
	protected static final String SALT = "4461746147656172";
	
	private DashboardSharePasswordCryptoImpl dspcNoop = new DashboardSharePasswordCryptoImpl(EncryptType.NOOP, SECRET_KEY, SALT);
	private DashboardSharePasswordCryptoImpl dspcStd = new DashboardSharePasswordCryptoImpl(EncryptType.STD, SECRET_KEY, SALT);
	
	@Test
	public void test()
	{
		String password = "1234";
		
		{
			String encrypt = dspcNoop.encrypt(password);
			
			String decrypt0 = dspcNoop.decrypt(encrypt);
			String decrypt1 = dspcStd.decrypt(encrypt);
			
			assertEquals(password, decrypt0);
			assertEquals(password, decrypt1);
		}

		{
			String encrypt = dspcStd.encrypt(password);
			
			String decrypt0 = dspcNoop.decrypt(encrypt);
			String decrypt1 = dspcStd.decrypt(encrypt);
			
			assertEquals(password, decrypt0);
			assertEquals(password, decrypt1);
		}
	}
}
