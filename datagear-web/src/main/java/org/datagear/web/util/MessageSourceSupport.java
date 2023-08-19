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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * 消息I18N支持类。
 * 
 * @author datagear@163.com
 *
 */
public class MessageSourceSupport
{
	private MessageSource messageSource;

	public MessageSourceSupport()
	{
		super();
	}

	public MessageSourceSupport(MessageSource messageSource)
	{
		super();
		this.messageSource = messageSource;
	}

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	/**
	 * 获取I18N消息内容。
	 * <p>
	 * 如果找不到对应消息码的消息，则返回<code>"???[code]???"<code>（例如：{@code "???error???"}）。
	 * </p>
	 * 
	 * @param request
	 * @param code
	 * @param args
	 * @return
	 */
	public String getMessage(HttpServletRequest request, String code, Object... args)
	{
		return getMessage(getLocale(request), code, args);
	}

	/**
	 * 获取I18N消息内容。
	 * <p>
	 * 如果找不到对应消息码的消息，则返回<code>"???[code]???"<code>（例如：{@code "???error???"}）。
	 * </p>
	 * 
	 * @param locale
	 * @param code
	 * @param args
	 * @return
	 */
	public String getMessage(Locale locale, String code, Object... args)
	{
		try
		{
			return this.messageSource.getMessage(code, args, locale);
		}
		catch (NoSuchMessageException e)
		{
			return "???" + code + "???";
		}
	}

	/**
	 * 获取请求地区。
	 * 
	 * @param request
	 * @return
	 */
	public Locale getLocale(HttpServletRequest request)
	{
		return WebUtils.getLocale(request);
	}
}
