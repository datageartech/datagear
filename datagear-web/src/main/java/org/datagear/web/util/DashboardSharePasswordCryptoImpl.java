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
 * <p>
 * 此类的设计参考了{@linkplain org.springframework.security.crypto.password.DelegatingPasswordEncoder}类，
 * 使其支持切换加密类型：
 * </p>
 * <p>
 * {@linkplain EncryptType#NOOP} 原始明文
 * </p>
 * <p>
 * {@linkplain EncryptType#STD} 标准加密
 * </p>
 * <p>
 * 而不会影响已存储的密码解密。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DashboardSharePasswordCryptoImpl implements DashboardSharePasswordCrypto
{
	public static final String ENCRYPT_TYPE_PREFIX_NOOP = "{noop}";
	
	public static final String ENCRYPT_TYPE_PREFIX_STD = "{std}";
	
	private EncryptType encryptType;
	
	private final TextEncryptor noopTextEncryptor = Encryptors.noOpText();
	
	private final TextEncryptor stdTextEncryptor;

	public DashboardSharePasswordCryptoImpl(EncryptType encryptType, String secretKey, String salt)
	{
		super();
		this.encryptType = encryptType;
		this.stdTextEncryptor =  Encryptors.text(secretKey, salt);
	}

	public EncryptType getEncryptType()
	{
		return encryptType;
	}

	public void setEncryptType(EncryptType encryptType)
	{
		this.encryptType = encryptType;
	}

	public TextEncryptor getNoopTextEncryptor()
	{
		return noopTextEncryptor;
	}

	public TextEncryptor getStdTextEncryptor()
	{
		return stdTextEncryptor;
	}

	@Override
	public String encrypt(String password)
	{
		if (password == null || "".equals(password))
			return password;

		TextEncryptor textEncryptor = null;
		String prefix = null;
		
		if(EncryptType.NOOP.equals(encryptType))
		{
			textEncryptor = this.noopTextEncryptor;
			prefix = ENCRYPT_TYPE_PREFIX_NOOP;
		}
		else if(EncryptType.STD.equals(encryptType))
		{
			textEncryptor = this.stdTextEncryptor;
			prefix = ENCRYPT_TYPE_PREFIX_STD;
		}
		else
			throw new UnsupportedOperationException();
		
		String re = textEncryptor.encrypt(password);
		re = prefix + re;
		
		return  re;
	}

	@Override
	public String decrypt(String password)
	{
		if (password == null || "".equals(password))
			return password;
		
		TextEncryptor textEncryptor = null;
		
		if(password.indexOf(ENCRYPT_TYPE_PREFIX_NOOP) == 0)
		{
			textEncryptor = this.noopTextEncryptor;
			password = password.substring(ENCRYPT_TYPE_PREFIX_NOOP.length());
		}
		else if(password.indexOf(ENCRYPT_TYPE_PREFIX_STD) == 0)
		{
			textEncryptor = this.stdTextEncryptor;
			password = password.substring(ENCRYPT_TYPE_PREFIX_STD.length());
		}
		else
			throw new UnsupportedOperationException();
		
		return textEncryptor.decrypt(password);
	}
	
	/**
	 * 加密类型。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum EncryptType
	{
		/**明文*/
		NOOP,
		
		/**标准*/
		STD
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

		DashboardSharePasswordCrypto crypto = new DashboardSharePasswordCryptoImpl(EncryptType.STD, secretKey, salt);

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
