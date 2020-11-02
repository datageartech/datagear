/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.config.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

/**
 * 能够将控制器方法的{@linkplain RequestMapping#produces()}作为“Content-Type”传递给响应的{@linkplain ExceptionHandlerExceptionResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class DeliverContentTypeExceptionHandlerExceptionResolver extends ExceptionHandlerExceptionResolver
{
	public static final String KEY_HANDLER_CONTENT_TYPE = DeliverContentTypeExceptionHandlerExceptionResolver.class
			.getName() + ".handlerContentType";

	public DeliverContentTypeExceptionHandlerExceptionResolver()
	{
		super();
	}

	@Override
	protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response,
			HandlerMethod handlerMethod, Exception exception)
	{
		String contentType = response.getContentType();

		if (contentType == null || contentType.isEmpty())
		{
			contentType = getHandlerResponseContentType(handlerMethod);

			if (contentType != null && !contentType.isEmpty())
			{
				request.setAttribute(KEY_HANDLER_CONTENT_TYPE, contentType);
				response.setContentType(contentType);
			}
		}

		return super.doResolveHandlerMethodException(request, response, handlerMethod, exception);
	}

	protected String getHandlerResponseContentType(HandlerMethod handlerMethod)
	{
		if (handlerMethod == null)
			return null;

		RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);

		if (requestMapping == null)
			return null;

		String[] produces = requestMapping.produces();

		if (produces == null || produces.length == 0)
			return null;

		return produces[0];
	}

	/**
	 * 获取控制其方法设置的响应“Content-Type”。
	 * <p>
	 * 如果无法取得，将返回{@code null}。
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public static String getHandlerContentType(HttpServletRequest request)
	{
		return (String) request.getAttribute(KEY_HANDLER_CONTENT_TYPE);
	}
}
