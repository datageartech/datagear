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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

/**
 * 校验码管理器。
 * 
 * @author datagear@163.com
 *
 */
public class CheckCodeManager
{
	public static final char[] CODES = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	/**
	 * 默认校验码长度。
	 */
	public static final int CODE_LEN = 4;

	public static final String SESSION_KEY_CHECK_CODES = CheckCodes.class.getName();

	private ConcurrentMap<String, Boolean> modules;

	public CheckCodeManager()
	{
		super();
		this.modules = new ConcurrentHashMap<String, Boolean>();
	}

	public ConcurrentMap<String, Boolean> getModules()
	{
		return modules;
	}

	public void setModules(ConcurrentMap<String, Boolean> modules)
	{
		this.modules = modules;
	}

	public boolean hasModule(String name)
	{
		return this.modules.containsKey(name);
	}

	public void putModule(String name)
	{
		this.modules.put(name, true);
	}

	public Set<String> modules()
	{
		return Collections.unmodifiableSet(this.modules.keySet());
	}

	/**
	 * 生成一个4位长度的随机校验码。
	 * 
	 * @return
	 */
	public String generate()
	{
		return generate(CODE_LEN);
	}

	/**
	 * 生成随机校验码。
	 * 
	 * @param length
	 * @return
	 */
	public String generate(int length)
	{
		Random random = new Random();

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
		{
			char c = CODES[random.nextInt(CODES.length)];
			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 设置指定模块的校验码。
	 * 
	 * @param session
	 * @param module
	 * @param code
	 * @throws IllegalArgumentException {@code module}未注册时
	 */
	public void setCheckCode(HttpSession session, String module, String code) throws IllegalArgumentException
	{
		if (!this.hasModule(module))
			throw new IllegalArgumentException("Illegal module : " + module);

		CheckCodes ccs;

		synchronized (session)
		{
			ccs = (CheckCodes) session.getAttribute(SESSION_KEY_CHECK_CODES);

			if (ccs == null)
			{
				ccs = new CheckCodes();
				session.setAttribute(SESSION_KEY_CHECK_CODES, ccs);
			}
		}

		ccs.set(module, code);
	}

	/**
	 * 获取指定模块的校验码。
	 * 
	 * @param session
	 * @param module
	 * @return {@code null}表示没有
	 */
	public String getCheckCode(HttpSession session, String module)
	{
		CheckCodes ccs = (CheckCodes) session.getAttribute(SESSION_KEY_CHECK_CODES);

		return (ccs == null ? null : ccs.get(module));
	}

	/**
	 * 删除指定模块的校验码。
	 * 
	 * @param session
	 * @param module
	 */
	public void removeCheckCode(HttpSession session, String module)
	{
		CheckCodes ccs = (CheckCodes) session.getAttribute(SESSION_KEY_CHECK_CODES);

		if (ccs != null)
			ccs.remove(module);
	}

	/**
	 * 判断指定模块的校验码是否正确。
	 * 
	 * @param session
	 * @param module
	 * @param code
	 * @return
	 */
	public boolean isCheckCode(HttpSession session, String module, String code)
	{
		String c = getCheckCode(session, module);

		return (c == null ? false : c.equals(code));
	}

	protected static class CheckCodes implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Map<String, String> codes = new HashMap<String, String>();

		public CheckCodes()
		{
			super();
		}

		public synchronized void set(String module, String code)
		{
			this.codes.put(module, code);
		}

		public synchronized String get(String module)
		{
			return this.codes.get(module);
		}

		public synchronized void remove(String module)
		{
			this.codes.remove(module);
		}
	}
}
