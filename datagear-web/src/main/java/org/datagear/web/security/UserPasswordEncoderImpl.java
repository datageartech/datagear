/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.security;

import org.datagear.management.service.impl.UserPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@linkplain UserPasswordEncoder}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class UserPasswordEncoderImpl implements UserPasswordEncoder
{
	private PasswordEncoder passwordEncoder;

	public UserPasswordEncoderImpl()
	{
		super();
	}

	public UserPasswordEncoderImpl(PasswordEncoder passwordEncoder)
	{
		super();
		this.passwordEncoder = passwordEncoder;
	}

	public PasswordEncoder getPasswordEncoder()
	{
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder)
	{
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String encode(String rawPassword)
	{
		return this.passwordEncoder.encode(rawPassword);
	}
}
