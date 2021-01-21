/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain SmtpSettingPasswordEncryptorImpl}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class SmtpSettingPasswordEncryptorImplTest
{
	private SmtpSettingPasswordEncryptorImpl smtpSettingPasswordEncryptorImpl = new SmtpSettingPasswordEncryptorImpl();

	@Test
	public void test()
	{
		String password = "i am a password";

		String encryptedPassword = smtpSettingPasswordEncryptorImpl.encrypt(password);

		String decryptedPassword = smtpSettingPasswordEncryptorImpl.decrypt(encryptedPassword);

		Assert.assertEquals(password, decryptedPassword);
	}
}
