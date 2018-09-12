/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.GlobalSetting;
import org.datagear.management.domain.SmtpSetting;
import org.datagear.management.domain.User;
import org.datagear.management.service.GlobalSettingService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain GlobalSettingService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class GlobalSettingServiceImpl extends AbstractMybatisService<GlobalSetting> implements GlobalSettingService
{
	protected static final String SQL_NAMESPACE = GlobalSetting.class.getName();

	private SmtpSettingPasswordEncryptor smtpSettingPasswordEncryptor;

	public GlobalSettingServiceImpl()
	{
		super();
	}

	public GlobalSettingServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public GlobalSettingServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	public SmtpSettingPasswordEncryptor getSmtpSettingPasswordEncryptor()
	{
		return smtpSettingPasswordEncryptor;
	}

	public void setSmtpSettingPasswordEncryptor(SmtpSettingPasswordEncryptor smtpSettingPasswordEncryptor)
	{
		this.smtpSettingPasswordEncryptor = smtpSettingPasswordEncryptor;
	}

	@Override
	public void save(GlobalSetting globalSetting)
	{
		encryptSmtpSettingPasswordIf(globalSetting);

		if (!super.update(globalSetting))
			super.add(globalSetting);
	}

	@Override
	public boolean save(User user, GlobalSetting globalSetting)
	{
		encryptSmtpSettingPasswordIf(globalSetting);

		if (!user.isAdmin())
			return false;

		if (!super.update(globalSetting))
			super.add(globalSetting);

		return true;
	}

	@Override
	public GlobalSetting get()
	{
		GlobalSetting globalSetting = super.get(null);

		decryptSmtpSettingPasswordIf(globalSetting);

		return globalSetting;
	}

	/**
	 * 加密SMTP密码。
	 * 
	 * @param globalSetting
	 * @return
	 */
	protected boolean encryptSmtpSettingPasswordIf(GlobalSetting globalSetting)
	{
		if (globalSetting == null)
			return false;

		if (!globalSetting.hasSmtpSetting())
			return false;

		if (this.smtpSettingPasswordEncryptor == null)
			return false;

		SmtpSetting smtpSetting = globalSetting.getSmtpSetting();

		String rawPassword = smtpSetting.getPassword();

		if (rawPassword == null || rawPassword.isEmpty())
			return false;

		smtpSetting.setPassword(this.smtpSettingPasswordEncryptor.encrypt(rawPassword));

		return true;
	}

	/**
	 * 解密SMTP密码。
	 * 
	 * @param globalSetting
	 * @return
	 */
	protected boolean decryptSmtpSettingPasswordIf(GlobalSetting globalSetting)
	{
		if (globalSetting == null)
			return false;

		if (!globalSetting.hasSmtpSetting())
			return false;

		if (this.smtpSettingPasswordEncryptor == null)
			return false;

		SmtpSetting smtpSetting = globalSetting.getSmtpSetting();

		String encryptedPassword = smtpSetting.getPassword();

		if (encryptedPassword == null || encryptedPassword.isEmpty())
			return false;

		smtpSetting.setPassword(this.smtpSettingPasswordEncryptor.decrypt(encryptedPassword));

		return true;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
