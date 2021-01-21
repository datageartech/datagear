/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
