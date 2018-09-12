/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.security;

import org.springframework.security.crypto.password.StandardPasswordEncoder;

public class StandardPasswordEncoderPrinter
{
	private static StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();

	public static void main(String[] args)
	{
		System.out.println(standardPasswordEncoder.encode("admin"));
	}
}
