/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.persistence.Order;
import org.datagear.persistence.Paging;
import org.datagear.persistence.PagingQuery;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 抽象控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractController
{
	public static final String CONTENT_TYPE_JSON = "application/json;";

	public static final String KEY_TITLE_MESSAGE_KEY = "titleMessageKey";

	public static final String KEY_FORM_ACTION = "formAction";

	public static final String KEY_READONLY = "readonly";

	public static final String KEY_SELECTONLY = "selectonly";

	protected static final String ERROR_PAGE_URL = "/error";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ClassDataConverter classDataConverter;

	public AbstractController()
	{
		super();
	}

	public AbstractController(MessageSource messageSource, ClassDataConverter classDataConverter)
	{
		super();
		this.messageSource = messageSource;
		this.classDataConverter = classDataConverter;
	}

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	public ClassDataConverter getClassDataConverter()
	{
		return classDataConverter;
	}

	public void setClassDataConverter(ClassDataConverter classDataConverter)
	{
		this.classDataConverter = classDataConverter;
	}

	/**
	 * 获取{@linkplain PagingQuery}。
	 * 
	 * @param request
	 * @param cookiePaginationSize
	 *            允许为{@code null}
	 * @return
	 * @throws Exception
	 */
	protected PagingQuery getPagingQuery(HttpServletRequest request, String cookiePaginationSize) throws Exception
	{
		String pageStr = request.getParameter("page");
		String pageSizeStr = request.getParameter("pageSize");
		String keyword = request.getParameter("keyword");
		String condition = request.getParameter("condition");
		String notLike = request.getParameter("notLike");
		Object ordersParam = getParamObj(request, "order");

		Integer page = null;
		Integer pageSize = null;

		if (pageStr != null && !pageStr.isEmpty())
		{
			try
			{
				page = Integer.parseInt(pageStr);
			}
			catch (Exception e)
			{
			}
		}

		if (pageSizeStr != null && !pageSizeStr.isEmpty())
		{
			try
			{
				pageSize = Integer.parseInt(pageSizeStr);
			}
			catch (Exception e)
			{
			}
		}

		if (page == null)
			page = 1;

		if (pageSize == null && cookiePaginationSize != null)
		{
			try
			{
				String pss = WebUtils.getCookieValue(request, cookiePaginationSize);
				if (pss != null)
					pageSize = Integer.parseInt(pss);
			}
			catch (Exception e)
			{
			}
		}

		if (pageSize == null)
			pageSize = Paging.DEFAULT_PAGE_SIZE;

		Order[] orders = null;

		if (ordersParam != null)
			orders = this.classDataConverter.convertToArray(ordersParam, Order.class);

		PagingQuery pagingQuery = new PagingQuery(page, pageSize, keyword, condition);

		pagingQuery.setNotLike(notLike != null && !notLike.isEmpty());
		pagingQuery.setOrders(orders);

		return pagingQuery;
	}

	/**
	 * 获取参数映射表。
	 * <p>
	 * 它查找以{@code param + '.'}、{@code param + '['}、 {@code param + '('}
	 * 开头的所有参数，将它们归入一个映射表，然后返回。
	 * </p>
	 * 
	 * @param request
	 * @param param
	 * @return
	 */
	protected Map<String, ?> getParamMap(HttpServletRequest request, String param)
	{
		Map<String, String[]> filteredParam = new HashMap<String, String[]>();

		int paramLen = param.length();

		@SuppressWarnings("unchecked")
		Map<String, String[]> paramMap = request.getParameterMap();

		for (Map.Entry<String, String[]> entry : paramMap.entrySet())
		{
			String name = entry.getKey();
			String[] pvalue = entry.getValue();

			if (name.length() <= paramLen || !name.startsWith(param))
				continue;

			boolean put;

			char nextChar = name.charAt(paramLen);

			// javaBean
			if (nextChar == '.')
			{
				name = name.substring(paramLen + 1);
				put = true;
			}
			// array, map
			else if (nextChar == '[' || nextChar == '(')
			{
				name = name.substring(paramLen);
				put = true;
			}
			else
				put = false;

			if (put)
				filteredParam.put(name, pvalue);
		}

		return (filteredParam.isEmpty() ? null : filteredParam);
	}

	/**
	 * 获取参数对象。
	 * <p>
	 * 如果参数中没有直接包含参数值，它还会查找以{@code param + '.'}、{@code param + '['}、
	 * {@code param + '('}开头的所有参数，将它们归入一个映射表，然后返回。
	 * </p>
	 * 
	 * @param request
	 * @param param
	 * @return
	 */
	protected Object getParamObj(HttpServletRequest request, String param)
	{
		String[] pstrs = request.getParameterValues(param);

		if (pstrs != null)
			return pstrs;

		return getParamMap(request, param);
	}

	/**
	 * 为异常设置{@linkplain OperationMessage}。
	 * 
	 * @param request
	 * @param messageCode
	 * @param throwable
	 * @param traceException
	 * @param messageArgs
	 */
	protected void setOperationMessageForThrowable(HttpServletRequest request, String messageCode, Throwable throwable,
			boolean traceException, Object... messageArgs)
	{
		OperationMessage operationMessage = buildOperationMessageFail(request, messageCode, messageArgs);
		if (traceException)
			operationMessage.setThrowable(throwable);

		WebUtils.setOperationMessage(request, operationMessage);
	}

	/**
	 * 构建操作成功消息（无消息内容）对应的{@linkplain ResponseEntity}。
	 * <p>
	 * 无消息内容，浏览器端不会弹出操作提示。
	 * </p>
	 * 
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageSuccessEmptyResponseEntity()
	{
		OperationMessage operationMessage = OperationMessage.valueOfSuccess("success", "");
		return new ResponseEntity<OperationMessage>(operationMessage, HttpStatus.OK);
	}

	/**
	 * 构建“保存成功”操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageSaveSuccessResponseEntity(
			HttpServletRequest request)
	{
		return buildOperationMessageSuccessResponseEntity(request, "saveSuccess");
	}

	/**
	 * 构建“删除成功”操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageDeleteSuccessResponseEntity(
			HttpServletRequest request)
	{
		return buildOperationMessageSuccessResponseEntity(request, "deleteSuccess");
	}

	/**
	 * 构建操作成功消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @param request
	 * @param httpStatus
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageSuccessResponseEntity(HttpServletRequest request,
			String code, Object... messageArgs)
	{
		OperationMessage operationMessage = buildOperationMessageSuccess(request, code, messageArgs);
		return new ResponseEntity<OperationMessage>(operationMessage, HttpStatus.OK);
	}

	/**
	 * 构建操作失败消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @param request
	 * @param httpStatus
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageFailResponseEntity(HttpServletRequest request,
			HttpStatus httpStatus, String code, Object... messageArgs)
	{
		OperationMessage operationMessage = buildOperationMessageFail(request, code, messageArgs);
		return new ResponseEntity<OperationMessage>(operationMessage, httpStatus);
	}

	/**
	 * 构建操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @param httpStatus
	 * @param operationMessage
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageResponseEntity(HttpStatus httpStatus,
			OperationMessage operationMessage)
	{
		return new ResponseEntity<OperationMessage>(operationMessage, httpStatus);
	}

	/**
	 * 构建操作成功消息。
	 * 
	 * @param request
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected OperationMessage buildOperationMessageSuccess(HttpServletRequest request, String code,
			Object... messageArgs)
	{
		String message = getMessage(request, code, messageArgs);
		return OperationMessage.valueOfSuccess(code, message);
	}

	/**
	 * 构建操作失败消息。
	 * 
	 * @param request
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected OperationMessage buildOperationMessageFail(HttpServletRequest request, String code, Object... messageArgs)
	{
		String message = getMessage(request, code, messageArgs);
		return OperationMessage.valueOfFail(code, message);
	}

	/**
	 * 构建异常消息码。
	 * 
	 * @param basename
	 * @param e
	 * @return
	 */
	protected String buildExceptionMessageCode(String basename, Exception e)
	{
		return buildMessageCode(basename, e.getClass().getSimpleName());
	}

	/**
	 * 构建消息码。
	 * <p>
	 * 此方法是一个未实现的模板方法，子类可以重写它以便隐藏{@linkplain #buildMessageCode(String, String)}的{@code basename}参数。
	 * </p>
	 * 
	 * @param code
	 * @return
	 */
	protected String buildMessageCode(String code)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * 构建消息码。
	 * 
	 * @param basename
	 * @param code
	 * @return
	 */
	protected String buildMessageCode(String basename, String code)
	{
		if (!isEmpty(basename))
			return basename + "." + code;
		else
			return code;
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
	protected String getMessage(HttpServletRequest request, String code, Object... args)
	{
		try
		{
			return this.messageSource.getMessage(code, args, WebUtils.getLocale(request));
		}
		catch (NoSuchMessageException e)
		{
			return "???" + code + "???";
		}
	}

	/**
	 * 字符串是否为空。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isEmpty(String s)
	{
		return (s == null || s.isEmpty());
	}

	/**
	 * 字符串是否为空格串。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isBlank(String s)
	{
		if (s == null)
			return true;

		if (s.isEmpty())
			return true;

		if (s.trim().isEmpty())
			return true;

		return false;
	}
}
