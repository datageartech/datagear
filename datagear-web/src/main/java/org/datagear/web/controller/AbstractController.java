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

import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.DirectoryFileDataSetEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.EntityService;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.StringUtil;
import org.datagear.web.config.support.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import freemarker.template.TemplateModel;

/**
 * 抽象控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractController
{
	/**
	 * 控制器类加载时间戳。
	 */
	public static final long CONTROLLER_LOAD_TIME = System.currentTimeMillis();
	
	public static final String RESPONSE_ENCODING = "UTF-8";

	public static final String CONTENT_TYPE_JSON = "application/json";

	public static final String CONTENT_TYPE_HTML = "text/html";

	public static final String CONTENT_TYPE_CSS = "text/css";

	public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript";

	@Deprecated
	public static final String KEY_TITLE_MESSAGE_KEY = "titleMessageKey";

	@Deprecated
	public static final String KEY_FORM_ACTION = "formAction";

	@Deprecated
	public static final String KEY_READONLY = "readonly";

	@Deprecated
	public static final String KEY_SELECT_OPERATION = "selectOperation";

	public static final String DATA_FILTER_PARAM = DataFilterPagingQuery.PROPERTY_DATA_FILTER;

	public static final String DATA_FILTER_COOKIE = "DATA_FILTER_SEARCH";

	public static final String KEY_ANALYSIS_PROJECT_ID = "ANALYSIS_PROJECT_ID";

	public static final String ERROR_PAGE_URL = "/error";

	public static final String KEY_REQUEST_ACTION = "requestAction";
	public static final String REQUEST_ACTION_QUERY = "query";
	public static final String REQUEST_ACTION_SELECT = "select";
	public static final String REQUEST_ACTION_ADD = "add";
	public static final String REQUEST_ACTION_EDIT = "edit";
	public static final String REQUEST_ACTION_VIEW = "view";
	public static final String REQUEST_ACTION_DELETE = "delete";
	public static final String REQUEST_ACTION_UPLOAD = "upload";
	public static final String REQUEST_ACTION_COPY = "copy";
	public static final String REQUEST_ACTION_IMPORT = "import";

	public static final String KEY_SUBMIT_ACTION = "submitAction";
	public static final String SUBMIT_ACTION_SAVE_ADD = "saveAdd";
	public static final String SUBMIT_ACTION_SAVE_EDIT = "saveEdit";
	public static final String SUBMIT_ACTION_SAVE = "save";
	public static final String SUBMIT_ACTION_SAVE_UPLOAD = "saveUpload";
	public static final String SUBMIT_ACTION_SAVE_IMPORT = "saveImport";
	public static final String SUBMIT_ACTION_NONE = "#";

	public static final String KEY_FORM_MODEL = "formModel";
	
	public static final String KEY_IS_MULTIPLE_SELECT = "isMultipleSelect";

	public static final String KEY_CURRENT_ANALYSIS_PROJECT = "currentAnalysisProject";

	public static final String KEY_IS_READONLY_ACTION = "isReadonlyAction";

	@Autowired
	private ConversionService conversionService;

	@Autowired
	private MessageSource messageSource;

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

	protected <ID, T extends Entity<ID>> T getByIdForEdit(EntityService<ID, T> service, ID id) throws RecordNotFoundException
	{
		T entity = service.getById(id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}

	protected <ID, T extends DataPermissionEntity<ID>> T getByIdForEdit(DataPermissionEntityService<ID, T> service,
			User user, ID id) throws RecordNotFoundException
	{
		T entity = service.getByIdForEdit(user, id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}

	protected <ID, T extends DataPermissionEntity<ID>> T getByIdForView(DataPermissionEntityService<ID, T> service,
			User user, ID id) throws RecordNotFoundException
	{
		T entity = service.getById(user, id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}

	protected <ID, T extends Entity<ID>> T getByIdForView(EntityService<ID, T> service, ID id) throws RecordNotFoundException
	{
		T entity = service.getById(id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}
	
	protected boolean setReadonlyActionByRole(Model model, User user)
	{
		boolean readonly = true;
		
		if(user == null || user.isAnonymous())
		{
			readonly = true;
		}
		else if(user.isAdmin() || user.hasRole(Role.ROLE_DATA_MANAGER))
		{
			readonly = false;
		}
		
		return setReadonlyAction(model, readonly);
	}

	protected boolean setReadonlyAction(Model model, boolean readonly)
	{
		model.addAttribute(KEY_IS_READONLY_ACTION, readonly);
		return readonly;
	}

	protected void setFormModel(Model model, Object formModel, String requestAction, String submitAction)
	{
		addAttributeForWriteJson(model, KEY_FORM_MODEL, formModel);
		setFormAction(model, requestAction, submitAction);
	}
	
	protected void setFormAction(Model model, String requestAction, String submitAction)
	{
		model.addAttribute(KEY_REQUEST_ACTION, requestAction);
		model.addAttribute(KEY_SUBMIT_ACTION, submitAction);
	}

	protected void addAttributeForWriteJson(Model model, String name, Object value)
	{
		model.addAttribute(name, toWriteJsonTemplateModel(value));
	}

	protected void setRequestAnalysisProjectIfValid(HttpServletRequest request, HttpServletResponse response,
			AnalysisProjectService analysisProjectService, AnalysisProjectAwareEntity<?> entity)
	{
		entity.setAnalysisProject(getRequestAnalysisProject(request, response, analysisProjectService));
	}

	/**
	 * 获取请求中的{@linkplain AnalysisProject}，没有则返回{@code null}。
	 * 
	 * @param request
	 * @param response
	 * @param analysisProjectService
	 * @return
	 */
	protected AnalysisProject getRequestAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			AnalysisProjectService analysisProjectService)
	{
		User user = WebUtils.getUser();

		String analysisId = request.getParameter(KEY_ANALYSIS_PROJECT_ID);
		
		if(StringUtil.isEmpty(analysisId))
			analysisId = WebUtils.getCookieValue(request, KEY_ANALYSIS_PROJECT_ID);
		
		if(isEmpty(analysisId))
			return null;
		
		try
		{
			return analysisProjectService.getById(user, analysisId);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/**
	 * 整理保存时的{@linkplain AnalysisProjectAwareEntity}：
	 * 如果analysisProject.id为空字符串，则应将其改为null，因为存储时相关外键不允许空字符串
	 * 
	 * @param entity
	 */
	protected void trimAnalysisProjectAwareEntityForSave(AnalysisProjectAwareEntity<?> entity)
	{
		if (entity == null)
			return;

		if (entity.getAnalysisProject() == null)
			return;

		if (isEmpty(entity.getAnalysisProject().getId()))
			entity.setAnalysisProject(null);
	}

	/**
	 * 整理保存时的{@linkplain DirectoryFileDataSetEntity}：
	 * 如果dataSetResDirectory.id为空字符串，则应将其改为null，因为存储时相关外键不允许空字符串
	 * 
	 * @param entity
	 */
	protected void trimDirectoryFileDataSetEntityForSave(DirectoryFileDataSetEntity entity)
	{
		if (entity == null)
			return;

		if (entity.getDataSetResDirectory() == null)
			return;

		if (isEmpty(entity.getDataSetResDirectory().getId()))
			entity.setDataSetResDirectory(null);
	}

	/**
	 * 如果用户对{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}没有权限，则置为{@code null}。
	 * 
	 * @param user
	 * @param entity
	 * @param service
	 */
	protected void setNullAnalysisProjectIfNoPermission(User user,
			AnalysisProjectAwareEntity<?> entity, AnalysisProjectService service)
	{
		AnalysisProject analysisProject = entity.getAnalysisProject();
		int apPermission = (analysisProject != null
				? service.getPermission(user, analysisProject.getId())
				: Authorization.PERMISSION_NONE_START);

		// 没有读权限，应置为null
		if (!Authorization.canRead(apPermission))
			entity.setAnalysisProject(null);
	}

	/**
	 * 检查并补充{@linkplain APIDDataFilterPagingQuery}。
	 * 
	 * @param request
	 * @param pagingQuery
	 *            允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected APIDDataFilterPagingQuery inflateAPIDDataFilterPagingQuery(HttpServletRequest request,
			APIDDataFilterPagingQuery pagingQuery)
	{
		DataFilterPagingQuery pq = inflateDataFilterPagingQuery(request, pagingQuery);

		if (pagingQuery == null)
		{
			pagingQuery = new APIDDataFilterPagingQuery(pq.getPage(), pq.getPageSize(), pq.getKeyword(),
					pq.getCondition());
			pagingQuery.setNotLike(pq.isNotLike());
			pagingQuery.setDataFilter(pq.getDataFilter());

			pagingQuery.setAnalysisProjectId(WebUtils.getCookieValue(request, KEY_ANALYSIS_PROJECT_ID));
		}

		return pagingQuery;
	}

	/**
	 * 检查并补充{@linkplain DataFilterPagingQuery#getDataFilter()}。
	 * 
	 * @param request
	 * @param pagingQuery
	 *            允许为{@code null}
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
	 * @param pagingQuery
	 *            允许为{@code null}
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
			value = DataPermissionEntityService.DATA_FILTER_VALUE_ALL;

		pagingQuery.setDataFilter(value);

		return pagingQuery;
	}

	/**
	 * 设置单选或多选请求操作。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	protected boolean setSelectAction(HttpServletRequest request, org.springframework.ui.Model model)
	{
		setReadonlyAction(model, true);
		
		boolean multiple = false;
		if (request.getParameter("multiple") != null)
			multiple = true;

		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_SELECT);
		model.addAttribute(KEY_IS_MULTIPLE_SELECT, multiple);
		
		return multiple;
	}

	/**
	 * 设置{@code isMultipleSelect}属性。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Deprecated
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
	 * @param pagingQuery
	 *            允许为{@code null}
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
	 * @param pagingQuery
	 *            允许为{@code null}
	 * @param cookiePaginationSize
	 *            允许为{@code null}
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
				catch (Exception e)
				{
				}
			}
		}

		return pagingQuery;
	}
	
	/**
	 * 为异常设置{@linkplain OperationMessage}。
	 * <p>
	 * 消息码参考{@linkplain #buildExceptionMsgCode(Class, boolean)}。
	 * </p>
	 * 
	 * @param request
	 * @param clazz
	 * @param msgArgs 默认为{@code t.getMessage()}
	 * @return
	 */
	protected OperationMessage setOptMsgForThrowable(HttpServletRequest request, Throwable t, Object... msgArgs)
	{
		String msgCode = buildExceptionMsgCode(t.getClass());
		return setOptMsgForThrowableMsgCode(request, t, msgCode, msgArgs);
	}

	/**
	 * 为异常设置{@linkplain OperationMessage}。
	 * 
	 * @param request
	 * @param t
	 * @param msgCode
	 * @param msgArgs 默认为{@code t.getMessage()}
	 * @return
	 */
	protected OperationMessage setOptMsgForThrowableMsgCode(HttpServletRequest request, Throwable t, String msgCode, Object... msgArgs)
	{
		if(msgArgs == null || msgArgs.length == 0)
			msgArgs = getDefaultThrowableMsgArgs(t);
		
		OperationMessage operationMessage = optMsgFail(request, msgCode, msgArgs);
		setOperationMessage(request, operationMessage);
		return operationMessage;
	}
	
	protected Object[] getDefaultThrowableMsgArgs(Throwable t)
	{
		String msgArg = getRootMessage(t);
		return new String[] { msgArg };
	}

	protected String getRootMessage(Throwable t)
	{
		String msg = "";
		
		while(t != null)
		{
			//FileNotFoundException应屏蔽文件路径
			msg = (t instanceof java.io.FileNotFoundException ? "File not found" : t.getMessage());
			t= t.getCause();
		}
		
		return msg;
	}
	
	/**
	 * 设置{@linkplain OperationMessage}。
	 * 
	 * @param request
	 * @param operationMessage
	 */
	protected void setOperationMessage(HttpServletRequest request, OperationMessage operationMessage)
	{
		WebUtils.setOperationMessage(request, operationMessage);
	}

	/**
	 * 构建异常i18n消息码。
	 * <p>
	 * 返回{@code "error." + clazz.getSimpleName()}消息码。
	 * </p>
	 * 
	 * @param buildExceptionMsgCode
	 * @return
	 */
	protected String buildExceptionMsgCode(Class<? extends Throwable> clazz)
	{
		return buildExceptionMsgCode(clazz, false);
	}

	/**
	 * 构建异常i18n消息码。
	 * <p>
	 * 返回{@code "error." + clazz.getSimpleName()}或{@code "error." + clazz.getName()}消息码。
	 * </p>
	 * @param clazz
	 * @param fullname
	 * @return
	 */
	protected String buildExceptionMsgCode(Class<? extends Throwable> clazz, boolean fullname)
	{
		return "error." + (fullname ? clazz.getName() : clazz.getSimpleName());
	}

	/**
	 * 获取错误信息视图。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected String getErrorView(HttpServletRequest request, HttpServletResponse response)
	{
		setAttributeIfIsJsonResponse(request, response);
		return ERROR_PAGE_URL;
	}

	/**
	 * 设置JSON响应的错误页面属性。
	 * 
	 * @param request
	 * @param response
	 */
	protected void setAttributeIfIsJsonResponse(HttpServletRequest request, HttpServletResponse response)
	{
		String expectedContentType = DeliverContentTypeExceptionHandlerExceptionResolver.getHandlerContentType(request);
		if (expectedContentType != null && !expectedContentType.isEmpty())
			response.setContentType(expectedContentType);

		boolean isJsonResponse = WebUtils.isJsonResponse(response);

		request.setAttribute("isJsonResponse", isJsonResponse);

		if (isJsonResponse)
		{
			OperationMessage operationMessage = getOptMsgForHttpError(request, response);

			request.setAttribute(WebUtils.KEY_OPERATION_MESSAGE,
					WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(operationMessage));

			response.setContentType(CONTENT_TYPE_JSON);
		}
	}

	/**
	 * 构建“操作成功”消息响应体。
	 * 
	 * @param request
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optSuccessResponseEntity(HttpServletRequest request)
	{
		return optSuccessDataResponseEntity(request, null);
	}

	/**
	 * 构建“操作成功”消息响应体。
	 * 
	 * @param request
	 * @param data
	 *            允许为{@code null}
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optSuccessDataResponseEntity(HttpServletRequest request, Object data)
	{
		ResponseEntity<OperationMessage> responseEntity = optSuccessResponseEntity(request, "operationSuccess");

		if (data != null)
			responseEntity.getBody().setData(data);

		return responseEntity;
	}

	/**
	 * 构建操作成功消息响应体。
	 * @param request
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optSuccessResponseEntity(HttpServletRequest request, String code,
			Object... messageArgs)
	{
		OperationMessage operationMessage = optMsgSuccess(request, code, messageArgs);
		return optResponseEntity(HttpStatus.OK, operationMessage);
	}

	/**
	 * 构建“操作失败”消息响应体。
	 * 
	 * @param request
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optFailResponseEntity(HttpServletRequest request)
	{
		return optFailDataResponseEntity(request, null);
	}

	/**
	 * 构建“操作失败”消息响应体。
	 * 
	 * @param request
	 * @param data
	 *            允许为{@code null}
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optFailDataResponseEntity(HttpServletRequest request, Object data)
	{
		ResponseEntity<OperationMessage> responseEntity = optFailResponseEntity(request, "operationFail");

		if (data != null)
			responseEntity.getBody().setData(data);

		return responseEntity;
	}

	/**
	 * 构建操作失败消息响应体，HTTP状态为{@linkplain HttpStatus#BAD_REQUEST}。
	 * 
	 * @param request
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optFailResponseEntity(HttpServletRequest request, String code,
			Object... messageArgs)
	{
		OperationMessage operationMessage = optMsgFail(request, code, messageArgs);
		return optResponseEntity(HttpStatus.BAD_REQUEST, operationMessage);
	}

	/**
	 * 构建操作失败消息响应体。
	 * 
	 * @param request
	 * @param httpStatus
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optFailResponseEntity(HttpServletRequest request,
			HttpStatus httpStatus, String code, Object... messageArgs)
	{
		OperationMessage operationMessage = optMsgFail(request, code, messageArgs);
		return optResponseEntity(httpStatus, operationMessage);
	}

	/**
	 * 构建操作消息响应体。
	 * 
	 * @param httpStatus
	 * @param operationMessage
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optResponseEntity(HttpStatus httpStatus,
			OperationMessage operationMessage)
	{
		return new ResponseEntity<>(operationMessage, httpStatus);
	}

	/**
	 * 构建“操作成功，保存了[{0}]条记录”操作成功消息响应体。
	 * 
	 * @param request
	 * @param saveCount
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optSaveCountSuccessResponseEntity(HttpServletRequest request,
			int saveCount)
	{
		if (saveCount > 0)
			return optSuccessResponseEntity(request, "operationSuccess.withSaveCount", saveCount);

		@JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
		// if (saveCount == 0)
		// return buildOperationMessageFailResponseEntity(request,
		// HttpStatus.BAD_REQUEST, "saveFail.zeroCount");

		ResponseEntity<OperationMessage> re = optSuccessResponseEntity(request);
		return re;
	}

	/**
	 * 构建“操作成功，删除了[{0}]条记录”操作成功消息响应体。
	 * 
	 * @param request
	 * @param deleteCount 实际删除数目
	 * @return
	 */
	protected ResponseEntity<OperationMessage> optDeleteCountSuccessResponseEntity(HttpServletRequest request,
			int deleteCount)
	{
		if (deleteCount > 0)
			return optSuccessResponseEntity(request, "operationSuccess.withDeleteCount", deleteCount);

		@JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
		// if (deleteCount == 0)
		// return buildOperationMessageFailResponseEntity(request,
		// HttpStatus.BAD_REQUEST, "deleteFail.zeroCount");

		ResponseEntity<OperationMessage> re = optSuccessResponseEntity(request);
		return re;
	}

	/**
	 * 构建操作成功消息。
	 * 
	 * @param request
	 * @param code
	 * @param messageArgs
	 * @return
	 */
	protected OperationMessage optMsgSuccess(HttpServletRequest request, String code, Object... messageArgs)
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
	protected OperationMessage optMsgFail(HttpServletRequest request, String code, Object... messageArgs)
	{
		String message = getMessage(request, code, messageArgs);
		return OperationMessage.valueOfFail(code, message);
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
	 * 获取HTTP错误时的操作消息。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected OperationMessage getOptMsgForHttpError(HttpServletRequest request, HttpServletResponse response)
	{
		OperationMessage operationMessage = WebUtils.getOperationMessage(request);

		if (operationMessage == null)
		{
			Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

			if (statusCode == null)
				statusCode = response.getStatus();

			operationMessage = buildOptMsgForHttpError(request, statusCode);
		}

		return operationMessage;
	}

	/**
	 * 获取HTTP错误时的操作消息。
	 * 
	 * @param request
	 * @param statusCode 允许为{@code null}
	 * @return
	 */
	protected OperationMessage getOptMsgForHttpError(HttpServletRequest request, Integer statusCode)
	{
		OperationMessage operationMessage = WebUtils.getOperationMessage(request);

		if (operationMessage == null)
			operationMessage = buildOptMsgForHttpError(request, statusCode);

		return operationMessage;
	}

	/**
	 * 构建HTTP错误操作消息。
	 * 
	 * @param request
	 * @param statusCode 允许为{@code null}
	 * @return
	 */
	protected OperationMessage buildOptMsgForHttpError(HttpServletRequest request, Integer statusCode)
	{
		String message = (String) request.getAttribute("javax.servlet.error.message");

		String statusCodeKey = "error.httpError";

		if (statusCode != null)
			statusCodeKey += "." + statusCode.intValue();

		try
		{
			message = getMessage(request, statusCodeKey, new Object[0]);
		}
		catch (Throwable t)
		{
		}

		return OperationMessage.valueOfFail(statusCodeKey, message);
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
		return WebUtils.resolvePathAfter(request, pathPrefix);
	}

	/**
	 * 为指定URL添加请求的查询参数。
	 * 
	 * @param url
	 * @param request
	 * @return
	 */
	protected String appendRequestQueryString(String url, HttpServletRequest request)
	{
		String qs = request.getQueryString();

		if (StringUtil.isEmpty(qs))
			return url;

		int qmIdx = url.lastIndexOf('?');

		if (qmIdx < 0)
			return url + "?" + qs;
		else
			return url + "&" + qs;
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
	 * 对象是否为{@code null}。
	 * 
	 * @param obj
	 * @return
	 */
	protected boolean isNull(Object obj)
	{
		return (obj == null);
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

	/**
	 * 将HTTP响应{@code "Cache-Control"}头设置为：{@code "no-cache"}。
	 * <p>
	 * 当使用
	 * </p>
	 * 
	 * <code>
	 * <pre>
	 * if (webRequest.checkNotModified(lastModified))
	 *   return;
	 * </pre>
	 * </code>
	 * <p>
	 * 设置缓存时，需要配合此方法才能起作用。
	 * </p>
	 * 
	 * @param response
	 */
	protected void setCacheControlNoCache(HttpServletResponse response)
	{
		response.setHeader("Cache-Control", "no-cache");
	}
}
