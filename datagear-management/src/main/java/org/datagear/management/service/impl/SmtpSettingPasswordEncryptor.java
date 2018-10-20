/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import org.datagear.management.domain.SmtpSetting;

/**
 * {@linkplain SmtpSetting#getPassword()}加密、解密类。
 * 
 * @author datagear@163.com
 *
 */
public interface SmtpSettingPasswordEncryptor
{
	/**
	 * 加密。
	 * 
	 * @param password
	 * @return
	 */
	String encrypt(String password);

	/**
	 * 解密。
	 * 
	 * @param encryptedPassword
	 * @return
	 */
	String decrypt(String encryptedPassword);
}
