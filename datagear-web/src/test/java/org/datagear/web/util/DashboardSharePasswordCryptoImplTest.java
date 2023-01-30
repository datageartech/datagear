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
