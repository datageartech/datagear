/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.security;

import org.springframework.security.crypto.password.StandardPasswordEncoder;

@SuppressWarnings("deprecation")
public class StandardPasswordEncoderPrinter
{
	private static StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();

	public static void main(String[] args)
	{
		System.out.println(standardPasswordEncoder.encode("admin"));
	}
}
