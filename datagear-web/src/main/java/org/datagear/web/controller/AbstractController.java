/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.StringUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.StringToJsonConverter;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.WebContextPath;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import freemarker.template.TemplateModel;

/**
 * 抽象控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractController
{
	public static final String RESPONSE_ENCODING = "UTF-8";

	public static final String CONTENT_TYPE_JSON = "application/json;";

	public static final String CONTENT_TYPE_HTML = "text/html;";

	public static final String CONTENT_TYPE_CSS = "text/css;";

	public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript;";

	public static final String KEY_TITLE_MESSAGE_KEY = "titleMessageKey";

	public static final String KEY_FORM_ACTION = "formAction";

	public static final String KEY_READONLY = "readonly";

	public static final String KEY_SELECT_OPERATION = "selectOperation";

	public static final String DATA_FILTER_PARAM = DataFilterPagingQuery.PROPERTY_DATA_FILTER;

	public static final String DATA_FILTER_COOKIE = "DATA_FILTER_SEARCH";

	public static final String ERROR_PAGE_URL = "/error";

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ConversionService conversionService;

	private StringToJsonConverter stringToJsonConverter = new StringToJsonConverter();

	public AbstractController()
	{
		super();
	}

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	public StringToJsonConverter getStringToJsonConverter()
	{
		return stringToJsonConverter;
	}

	public void setStringToJsonConverter(StringToJsonConverter stringToJsonConverter)
	{
		this.stringToJsonConverter = stringToJsonConverter;
	}

	/**
	 * 检查并补充{@linkplain DataFilterPagingQuery#getDataFilter()}。
	 * 
	 * @param request
	 * @param pagingQuery 允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected DataFilterPagingQuery inflateDataFilterPagingQuery(HttpServletRequest request,
			DataFilterPagingQuery pagingQuery)
	{
		return inflateDataFilterPagingQuery(request, pagingQuery, WebUtils.COOKIE_PAGINATION_SIZE);
	}

	/**
	 * 检查并补充{@linkplain DataFilterPagingQuery#getDataFilter()}。
	 * 
	 * @param request
	 * @param pagingQuery          允许为{@code null}
	 * @param cookiePaginationSize
	 * @return 不会为{@code null}
	 */
	protected DataFilterPagingQuery inflateDataFilterPagingQuery(HttpServletRequest request,
			DataFilterPagingQuery pagingQuery, String cookiePaginationSize)
	{
		PagingQuery pq = inflatePagingQuery(request, pagingQuery, cookiePaginationSize);

		if (pagingQuery == null)
		{
			pagingQuery = new DataFilterPagingQuery(pq.getPage(), pq.getPageSize(), pq.getKeyword(), pq.getCondition());
			pagingQuery.setNotLike(pq.isNotLike());
		}

		String value = pagingQuery.getDataFilter();

		if (isEmpty(value))
			value = WebUtils.getCookieValue(request, DATA_FILTER_COOKIE);

		if (DataPermissionEntityService.DATA_FILTER_VALUE_MINE.equalsIgnoreCase(value))
			value = DataPermissionEntityService.DATA_FILTER_VALUE_MINE;
		else if (DataPermissionEntityService.DATA_FILTER_VALUE_OTHER.equalsIgnoreCase(value))
			value = DataPermissionEntityService.DATA_FILTER_VALUE_OTHER;
		else if (DataPermissionEntityService.DATA_FILTER_VALUE_ALL.equalsIgnoreCase(value))
			value = DataPermissionEntityService.DATA_FILTER_VALUE_ALL;
		else
			value = DataPermissionEntityService.DATA_FILTER_VALUE_MINE;

		pagingQuery.setDataFilter(value);

		return pagingQuery;
	}

	/**
	 * 获取当前请求的{@linkplain WebContextPath}。
	 * 
	 * @param request
	 * @return
	 */
	protected WebContextPath getWebContextPath(HttpServletRequest request)
	{
		return WebContextPath.getWebContextPath(request);
	}

	/**
	 * 设置{@code isMultipleSelect}属性。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	protected boolean setIsMultipleSelectAttribute(HttpServletRequest request, org.springframework.ui.Model model)
	{
		boolean isMultipleSelect = false;
		if (request.getParameter("multiple") != null)
			isMultipleSelect = true;

		model.addAttribute("isMultipleSelect", isMultipleSelect);

		return isMultipleSelect;
	}

	/**
	 * 检查并补充{@linkplain PagingQuery}。
	 * 
	 * @param request
	 * @param pagingQuery 允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected PagingQuery inflatePagingQuery(HttpServletRequest request, PagingQuery pagingQuery)
	{
		return inflatePagingQuery(request, pagingQuery, WebUtils.COOKIE_PAGINATION_SIZE);
	}

	/**
	 * 检查并补充{@linkplain PagingQuery}。
	 * 
	 * @param request
	 * @param pagingQuery          允许为{@code null}
	 * @param cookiePaginationSize 允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected PagingQuery inflatePagingQuery(HttpServletRequest request, PagingQuery pagingQuery,
			String cookiePaginationSize)
	{
		if (pagingQuery == null)
		{
			pagingQuery = new PagingQuery();

			if (!isEmpty(cookiePaginationSize))
			{
				try
				{
					String pss = WebUtils.getCookieValue(request, cookiePaginationSize);

					if (!isEmpty(pss))
						pagingQuery.setPageSize(Integer.parseInt(pss));
				}
				catch(Exception e)
				{
				}
			}
		}

		return pagingQuery;
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

		operationMessage.setData(messageArgs);

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
		return new ResponseEntity<>(operationMessage, HttpStatus.OK);
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
	 * 构建“保存成功”操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageSaveSuccessResponseEntity(
			HttpServletRequest request, Object data)
	{
		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSuccessResponseEntity(request,
				"saveSuccess");
		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	/**
	 * 构建保存操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageSaveCountResponseEntity(HttpServletRequest request,
			int saveCount)
	{
		if (saveCount > 0)
			return buildOperationMessageSuccessResponseEntity(request, "saveSuccess.withCount", saveCount);

		@JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
		// if (saveCount == 0)
		// return buildOperationMessageFailResponseEntity(request,
		// HttpStatus.BAD_REQUEST, "saveFail.zeroCount");

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSuccessResponseEntity(request,
				"saveSuccess");
		return responseEntity;
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
	 * 构建删除操作消息对应的{@linkplain ResponseEntity}。
	 * 
	 * @param request
	 * @param deleteCount
	 *            实际删除数目
	 * @return
	 */
	protected ResponseEntity<OperationMessage> buildOperationMessageDeleteCountResponseEntity(
			HttpServletRequest request, int deleteCount)
	{
		if (deleteCount > 0)
			return buildOperationMessageSuccessResponseEntity(request, "deleteSuccess.withCount", deleteCount);

		@JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
		// if (deleteCount == 0)
		// return buildOperationMessageFailResponseEntity(request,
		// HttpStatus.BAD_REQUEST, "deleteFail.zeroCount");

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSuccessResponseEntity(request,
				"deleteSuccess");
		return responseEntity;
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
		return new ResponseEntity<>(operationMessage, HttpStatus.OK);
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
		return new ResponseEntity<>(operationMessage, httpStatus);
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
		return new ResponseEntity<>(operationMessage, httpStatus);
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
			return this.messageSource.getMessage(code, args, getLocale(request));
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
	protected Locale getLocale(HttpServletRequest request)
	{
		return WebUtils.getLocale(request);
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
	protected String getMessage(Locale locale, String code, Object... args)
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
	 * 获取HTTP错误时的{@linkplain OperationMessage}。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected OperationMessage getOperationMessageForHttpError(HttpServletRequest request, HttpServletResponse response)
	{
		OperationMessage operationMessage = WebUtils.getOperationMessage(request);

		if (operationMessage == null)
		{
			Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

			if (statusCode == null)
				statusCode = response.getStatus();

			String message = (String) request.getAttribute("javax.servlet.error.message");

			String statusCodeKey = "error.httpError";

			if (statusCode != null)
			{
				int sc = statusCode.intValue();
				statusCodeKey += "." + sc;
			}

			try
			{
				message = getMessage(request, statusCodeKey, new Object[0]);
			}
			catch (Throwable t)
			{
			}

			operationMessage = OperationMessage.valueOfFail(statusCodeKey, message);
		}

		return operationMessage;
	}

	/**
	 * 将对象转换为可作为页面使用<code>&lt;@writeJson var=... /&gt;</code>的对象。
	 * 
	 * @param object
	 * @return
	 */
	protected TemplateModel toWriteJsonTemplateModel(Object object)
	{
		return WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(object);
	}

	/**
	 * 设置看板资源响应内容类型。
	 * 
	 * @param request
	 * @param response
	 * @param resName
	 */
	protected String setContentTypeByName(HttpServletRequest request, HttpServletResponse response,
			ServletContext servletContext, String resName)
	{
		String mimeType = servletContext.getMimeType(resName);
		if (!isEmpty(mimeType))
			response.setContentType(mimeType);

		return mimeType;
	}

	/**
	 * 解析请求路径中{@code pathPrefix}之后的路径名，如果路径不包含{@code pathPrefix}，则返回{@code null}。
	 * 
	 * @param request
	 * @param pathPrefix
	 *            为空或{@code null}，则返回整个请求路径
	 * @return
	 */
	protected String resolvePathAfter(HttpServletRequest request, String pathPrefix)
	{
		String pathInfo = request.getPathInfo();

		if (StringUtil.isEmpty(pathPrefix))
			return pathInfo;

		if (pathInfo.endsWith(pathPrefix))
			return "";

		int index = pathInfo.indexOf(pathPrefix);

		if (index < 0)
			return null;

		return pathInfo.substring(index + pathPrefix.length());
	}

	/**
	 * 将文件名转换为作为响应下载文件名。
	 * <p>
	 * 此方法会对处理中文乱码问题。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected String toResponseAttachmentFileName(HttpServletRequest request, HttpServletResponse response,
			String fileName) throws IOException
	{
		return new String(fileName.getBytes(RESPONSE_ENCODING), IOUtil.CHARSET_ISO_8859_1);
	}

	/**
	 * 判断对象、字符串、数组、集合、Map是否为空。
	 * 
	 * @param obj
	 * @return
	 */
	protected boolean isEmpty(Object obj)
	{
		return StringUtil.isEmpty(obj);
	}

	/**
	 * 字符串是否为空。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isEmpty(String s)
	{
		return StringUtil.isEmpty(s);
	}

	/**
	 * 字符串是否为空格串。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isBlank(String s)
	{
		return StringUtil.isBlank(s);
	}

	/**
	 * 获取可用字符集名称。
	 * 
	 * @return
	 */
	protected List<String> getAvailableCharsetNames()
	{
		List<String> charsetNames = new ArrayList<>();

		Map<String, Charset> map = Charset.availableCharsets();
		Set<String> names = map.keySet();
		for (String name : names)
		{
			// 排除未在IANA注册的字符集
			if (name.startsWith("x-") || name.startsWith("X-"))
				continue;

			charsetNames.add(name);
		}

		return charsetNames;
	}
}
