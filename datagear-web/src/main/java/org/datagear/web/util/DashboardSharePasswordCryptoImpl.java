/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Scanner;

import org.datagear.management.service.impl.DashboardSharePasswordCrypto;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * {@linkplain DashboardSharePasswordCrypto}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardSharePasswordCryptoImpl implements DashboardSharePasswordCrypto
{
	private TextEncryptor textEncryptor;

	public DashboardSharePasswordCryptoImpl()
	{
		super();
	}

	public DashboardSharePasswordCryptoImpl(TextEncryptor textEncryptor)
	{
		super();
		this.textEncryptor = textEncryptor;
	}

	public TextEncryptor getTextEncryptor()
	{
		return textEncryptor;
	}

	public void setTextEncryptor(TextEncryptor textEncryptor)
	{
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String encrypt(String password)
	{
		if (this.textEncryptor == null)
			return password;

		if (password == null || "".equals(password))
			return password;

		return this.textEncryptor.encrypt(password);
	}

	@Override
	public String decrypt(String password)
	{
		if (this.textEncryptor == null)
			return password;

		if (password == null || "".equals(password))
			return password;

		return this.textEncryptor.decrypt(password);
	}
	
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);

		String secretKey = readNextInput(scanner, "Enter raw secret key:");
		secretKey = base64(secretKey);
		println("Base64 secret key:");
		println(secretKey);

		String salt = readNextInput(scanner, "Enter salt:");
		salt = salt(salt);
		println("Salt:");
		println(salt);

		DashboardSharePasswordCrypto crypto = new DashboardSharePasswordCryptoImpl(Encryptors.text(secretKey, salt));

		String command = "";

		while (scanner.hasNextLine())
		{
			String input = scanner.nextLine().trim();

			if ("0".equals(command))
			{
				println(crypto.encrypt(input));
				command = "";
			}
			else if ("1".equals(command))
			{
				println(crypto.decrypt(input));
				command = "";
			}
			else if ("0".equals(input))
			{
				command = input;
				println("Enter text for encrypt:");
			}
			else if ("1".equals(input))
			{
				command = input;
				println("Enter text for decrypt:");
			}
			else if ("exit".equalsIgnoreCase(input))
			{
				println("Bye!");
				scanner.close();
				System.exit(0);
			}
		}
	}

	protected static String readNextInput(Scanner scanner, String tip)
	{
		String re = "";

		println(tip);

		while (scanner.hasNextLine())
		{
			String input = scanner.nextLine().trim();

			if ("exit".equalsIgnoreCase(input))
			{
				println("Bye!");
				scanner.close();
				System.exit(0);
			}
			else
			{
				re = input;
				break;
			}
		}

		return re;
	}

	protected static void println(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.println(str);
	}

	protected static void println()
	{
		System.out.println();
	}

	protected static String base64(String text)
	{
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(text.getBytes(Charset.forName("iso-8859-1")));
	}

	protected static String salt(String text)
	{
		if (text == null || "".equals(text))
			return "";

		byte[] bytes = text.getBytes(Charset.forName("iso-8859-1"));

		return new String(Hex.encode(bytes));
	}
}
