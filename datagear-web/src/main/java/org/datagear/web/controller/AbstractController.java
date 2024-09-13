/*
 * Copyright 2018-present datagear.tech
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.CreateTimeEntity;
import org.datagear.management.domain.CreateUserEntity;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.DirectoryFileDataSetEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.EntityService;
import org.datagear.management.util.DataPermissionSpec;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.Global;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.dirquery.DirectoryPagingQuery;
import org.datagear.web.config.support.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.security.AuthenticationSecurity;
import org.datagear.web.security.AuthenticationUserGetter;
import org.datagear.web.util.MessageSourceSupport;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateModel;

/**
 * 抽象控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractController extends MessageSourceSupport
{
	/**
	 * 控制器类加载时间戳。
	 */
	public static final long CONTROLLER_LOAD_TIME = System.currentTimeMillis();
	
	public static final String RESPONSE_ENCODING = IOUtil.CHARSET_UTF_8;

	public static final String CONTENT_TYPE_JSON = "application/json";

	public static final String CONTENT_TYPE_HTML = "text/html";

	public static final String CONTENT_TYPE_CSS = "text/css";

	public static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript";

	public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

	public static final String DATA_FILTER_PARAM = DataFilterPagingQuery.PROPERTY_DATA_FILTER;

	public static final String KEY_ANALYSIS_PROJECT_ID = Global.NAME_SHORT_UCUS + "ANALYSIS_PROJECT_ID";

	public static final String ERROR_PAGE_URL = "/error";

	public static final String KEY_REQUEST_ACTION = "requestAction";
	public static final String REQUEST_ACTION_MANAGE = "manage";
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

	public static final String KEY_QUERY_DATA_URL = "queryDataUrl";

	public static final String KEY_FORM_MODEL = "formModel";
	
	public static final String KEY_IS_MULTIPLE_SELECT = "isMultipleSelect";

	public static final String KEY_CURRENT_ANALYSIS_PROJECT = "currentAnalysisProject";

	public static final String KEY_IS_READONLY_ACTION = "isReadonlyAction";

	@Autowired
	private ConversionService conversionService;

	@Autowired
	private AuthenticationSecurity authenticationSecurity;

	@Autowired
	private AuthenticationUserGetter authenticationUserGetter;

	@Autowired
	private DataPermissionSpec dataPermissionSpec;

	public AbstractController()
	{
		super();
	}

	@Autowired
	@Override
	public void setMessageSource(MessageSource messageSource)
	{
		super.setMessageSource(messageSource);
	}

	public ConversionService getConversionService()
	{
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService)
	{
		this.conversionService = conversionService;
	}

	public AuthenticationSecurity getAuthenticationSecurity()
	{
		return authenticationSecurity;
	}

	public void setAuthenticationSecurity(AuthenticationSecurity authenticationSecurity)
	{
		this.authenticationSecurity = authenticationSecurity;
	}

	public AuthenticationUserGetter getAuthenticationUserGetter()
	{
		return authenticationUserGetter;
	}

	public void setAuthenticationUserGetter(AuthenticationUserGetter authenticationUserGetter)
	{
		this.authenticationUserGetter = authenticationUserGetter;
	}

	public DataPermissionSpec getDataPermissionSpec()
	{
		return dataPermissionSpec;
	}

	public void setDataPermissionSpec(DataPermissionSpec dataPermissionSpec)
	{
		this.dataPermissionSpec = dataPermissionSpec;
	}

	protected User getCurrentUser()
	{
		return this.authenticationUserGetter.getUser();
	}

	protected User getCurrentUser(Authentication authentication)
	{
		return this.authenticationUserGetter.getUser(authentication);
	}

	protected Authentication getCurrentAuthentication()
	{
		return this.authenticationUserGetter.getAuthentication();
	}

	protected <ID, T extends Entity<ID>> T getByIdForEdit(EntityService<ID, T> service, ID id) throws RecordNotFoundException
	{
		T entity = service.getById(id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}

	protected <ID, T extends DataPermissionEntity & Entity<ID>> T getByIdForEdit(
			DataPermissionEntityService<ID, T> service,
			User user, ID id) throws RecordNotFoundException
	{
		T entity = service.getByIdForEdit(user, id);

		if (entity == null)
			throw new RecordNotFoundException();

		return entity;
	}

	protected <ID, T extends DataPermissionEntity & Entity<ID>> T getByIdForView(
			DataPermissionEntityService<ID, T> service,
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
	
	/**
	 * 设置创建时间。
	 * 
	 * @param entity
	 *            允许{@code null}
	 */
	protected void inflateCreateTime(CreateTimeEntity entity)
	{
		inflateCreateTime(entity, new java.util.Date());
	}

	/**
	 * 设置创建时间。
	 * 
	 * @param entity
	 *            允许{@code null}
	 * @param time
	 *            允许{@code null}
	 */
	protected void inflateCreateTime(CreateTimeEntity entity, Date time)
	{
		if (entity == null)
			return;

		entity.setCreateTime(time);
	}

	/**
	 * 设置创建用户、时间。
	 * 
	 * @param entity
	 *            允许{@code null}
	 * @param user
	 *            允许{@code null}
	 */
	protected void inflateCreateUserAndTime(CreateUserEntity entity, User user)
	{
		inflateCreateUserAndTime(entity, user, new Date());
	}

	/**
	 * 设置创建用户、时间。
	 * 
	 * @param entity
	 *            允许{@code null}
	 * @param user
	 *            允许{@code null}
	 * @param time
	 *            允许{@code null}
	 */
	protected void inflateCreateUserAndTime(CreateUserEntity entity, User user, Date time)
	{
		if (entity == null)
			return;

		entity.setCreateUser((user == null ? null : user.cloneSimple()));
		entity.setCreateTime(time);
	}

	/**
	 * 设置查询数据URL。
	 * 
	 * @param model
	 * @param url
	 */
	protected void setQueryDataUrl(Model model, String url)
	{
		model.addAttribute(KEY_QUERY_DATA_URL, url);
	}

	/**
	 * 设置当前用户是否只能执行只读操作。
	 * 
	 * @param model
	 * @return
	 */
	protected boolean setReadonlyAction(Model model)
	{
		boolean readonly = isReadonlyAction(model, getCurrentAuthentication());
		return setReadonlyAction(model, readonly);
	}

	/**
	 * 设置是否只能执行只读操作。
	 * 
	 * @param model
	 * @param readonly
	 * @return
	 */
	protected boolean setReadonlyAction(Model model, boolean readonly)
	{
		model.addAttribute(KEY_IS_READONLY_ACTION, readonly);
		return readonly;
	}

	/**
	 * 判断用户是否只能执行只读操作。
	 * 
	 * @param model
	 * @param auth
	 * @return
	 */
	protected boolean isReadonlyAction(Model model, Authentication auth)
	{
		boolean readonly = true;

		if (this.authenticationSecurity.isAnonymous(auth))
		{
			readonly = true;
		}
		else if (this.authenticationSecurity.hasDataManager(auth))
		{
			readonly = false;
		}

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
			AnalysisProjectService analysisProjectService, AnalysisProjectAwareEntity entity)
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
		User user = getCurrentUser();

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
	 * 如果{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}为{@code null}或其ID为空，
	 * 则应将其改为{@code null}，因为存储时相关外键不允许空字符串
	 * 
	 * @param entity
	 */
	protected void trimAnalysisProjectAwareEntityForSave(AnalysisProjectAwareEntity entity)
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
	 * 如果{@linkplain DirectoryFileDataSetEntity#getFileSource()}为{@code null}或其ID为空，
	 * 则应将其改为{@code null}，因为存储时相关外键不允许空字符串
	 * 
	 * @param entity
	 */
	protected void trimDirectoryFileDataSetEntityForSave(DirectoryFileDataSetEntity entity)
	{
		if (entity == null)
			return;

		if (entity.getFileSource() == null)
			return;

		if (isEmpty(entity.getFileSource().getId()))
			entity.setFileSource(null);
	}

	/**
	 * 如果用户对{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}没有权限，则置为{@code null}。
	 * 
	 * @param user
	 * @param entity
	 * @param service
	 */
	protected void setNullAnalysisProjectIfNoPermission(User user,
			AnalysisProjectAwareEntity entity, AnalysisProjectService service)
	{
		AnalysisProject analysisProject = entity.getAnalysisProject();
		int apPermission = (analysisProject != null
				? service.getPermission(user, analysisProject.getId())
				: DataPermissionEntityService.PERMISSION_NOT_FOUND);

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
		
		boolean multiple = isMultipleSelectRequest(request);

		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_SELECT);
		model.addAttribute(KEY_IS_MULTIPLE_SELECT, multiple);
		
		return multiple;
	}

	/**
	 * 是否多选请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isMultipleSelectRequest(HttpServletRequest request)
	{
		String multipleParam = request.getParameter("multiple");
		return (multipleParam != null);
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
			Integer pageSize = resolveCookiePageSize(request, cookiePaginationSize);

			if (pageSize != null)
				pagingQuery.setPageSize(pageSize);
		}

		return pagingQuery;
	}

	/**
	 * 检查并补充{@linkplain DirectoryPagingQuery}。
	 * 
	 * @param request
	 * @param pagingQuery
	 *            允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected DirectoryPagingQuery inflateDirectoryPagingQuery(HttpServletRequest request,
			DirectoryPagingQuery pagingQuery)
	{
		return inflateDirectoryPagingQuery(request, pagingQuery, WebUtils.COOKIE_PAGINATION_SIZE);
	}

	/**
	 * 检查并补充{@linkplain DirectoryPagingQuery}。
	 * 
	 * @param request
	 * @param pagingQuery
	 *            允许为{@code null}
	 * @param cookiePaginationSize
	 *            允许为{@code null}
	 * @return 不会为{@code null}
	 */
	protected DirectoryPagingQuery inflateDirectoryPagingQuery(HttpServletRequest request,
			DirectoryPagingQuery pagingQuery, String cookiePaginationSize)
	{
		if (pagingQuery == null)
		{
			pagingQuery = new DirectoryPagingQuery();
			Integer pageSize = resolveCookiePageSize(request, cookiePaginationSize);

			if (pageSize != null)
				pagingQuery.setPageSize(pageSize);
		}

		return pagingQuery;
	}

	/**
	 * 解析Cookie中的页大小。
	 * 
	 * @param request
	 * @param cookiePageSize
	 *            允许为{@code null}
	 * @return {@code null}表示未解析到
	 */
	protected Integer resolveCookiePageSize(HttpServletRequest request, String cookiePageSize)
	{
		Integer pageSize = null;

		if (!isEmpty(cookiePageSize))
		{
			try
			{
				String pss = WebUtils.getCookieValue(request, cookiePageSize);

				if (!isEmpty(pss))
					pageSize = Integer.parseInt(pss);
			}
			catch (Exception e)
			{
			}
		}

		return pageSize;
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
	 * 返回{@linkplain HttpServletResponse#SC_BAD_REQUEST}错误消息页面。
	 * @param request
	 * @param response
	 * @param msgCode
	 * @param msgArgs
	 * @return
	 */
	protected String errorViewOptMsg(HttpServletRequest request, HttpServletResponse response, String msgCode, Object... msgArgs)
	{
		return errorViewOptMsg(request, response, HttpServletResponse.SC_BAD_REQUEST, msgCode, msgArgs);
	}

	/**
	 * 返回{@linkplain HttpServletResponse#SC_BAD_REQUEST}错误消息页面。
	 * 
	 * @param request
	 * @param response
	 * @param msg
	 * @return
	 */
	protected String errorViewOptMsg(HttpServletRequest request, HttpServletResponse response, OperationMessage msg)
	{
		return errorViewOptMsg(request, response, HttpServletResponse.SC_BAD_REQUEST, msg);
	}

	/**
	 * 返回错误消息页面。
	 * 
	 * @param request
	 * @param response
	 * @param statusCode
	 * @param msgCode
	 * @param msgArgs
	 * @return
	 */
	protected String errorViewOptMsg(HttpServletRequest request, HttpServletResponse response, int statusCode,
			String msgCode, Object... msgArgs)
	{
		response.setStatus(statusCode);
		setOperationMessage(request, optMsgFail(request, msgCode, msgArgs));
		return getErrorView(request, response);
	}

	/**
	 * 返回错误消息页面。
	 * 
	 * @param request
	 * @param response
	 * @param statusCode
	 * @param msg
	 * @return
	 */
	protected String errorViewOptMsg(HttpServletRequest request, HttpServletResponse response, int statusCode,
			OperationMessage msg)
	{
		response.setStatus(statusCode);
		setOperationMessage(request, msg);
		return getErrorView(request, response);
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
		setErrorAttrIfIsJsonResponse(request, response);
		return ERROR_PAGE_URL;
	}

	/**
	 * 设置JSON响应的错误页面属性。
	 * 
	 * @param request
	 * @param response
	 */
	protected void setErrorAttrIfIsJsonResponse(HttpServletRequest request, HttpServletResponse response)
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

		@org.datagear.util.JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
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

		@org.datagear.util.JDBCCompatiblity("JDBC兼容问题，某些驱动不能正确返回更新记录数，比如Hive jdbc始终返回0，所以这里暂时禁用此逻辑")
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
	 * 获取HTTP错误时的操作消息。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected OperationMessage getOptMsgForHttpError(HttpServletRequest request, HttpServletResponse response)
	{
		OperationMessage operationMessage = WebUtils.getOperationMessage(request);

		// 尝试从session中取，并在之后移除
		if (operationMessage == null)
		{
			HttpSession session = request.getSession();
			operationMessage = WebUtils.getOperationMessage(session);
			
			if(operationMessage != null)
				WebUtils.removeOperationMessage(session);
		}

		if (operationMessage == null)
		{
			Exception exception = null;

			Object exceptionAttr = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
			if (exceptionAttr instanceof Exception)
				exception = (Exception) exceptionAttr;

			if (exception != null)
			{
				String code = buildExceptionMsgCode(exception.getClass());
				String message = getMessageNullable(request, code, exception.getMessage());

				if (message != null)
					operationMessage = OperationMessage.valueOfFail(code, message);
			}
		}

		if (operationMessage == null)
		{
			Integer statusCode = null;

			Object statusCodeAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
			if (statusCodeAttr instanceof Integer)
				statusCode = (Integer) statusCodeAttr;

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

		// 尝试从session中取，并在之后移除
		if (operationMessage == null)
		{
			HttpSession session = request.getSession();
			operationMessage = WebUtils.getOperationMessage(session);

			if (operationMessage != null)
				WebUtils.removeOperationMessage(session);
		}

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
		String rawMsg = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

		if (rawMsg == null)
			rawMsg = "";

		String statusCodeKey = null;
		String message = null;

		if (statusCode != null)
		{
			statusCodeKey = "error.httpError" + "." + statusCode.intValue();
			message = getMessageNullable(request, statusCodeKey, rawMsg);
		}

		if (message == null)
		{
			statusCodeKey = "error.httpError";
			message = getMessage(request, statusCodeKey, rawMsg);
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
	 * 解析请求路径中{@code pathPrefix}之后的路径名。
	 * 
	 * @param request
	 * @param pathPrefix
	 * @return
	 * @see {@linkplain WebUtils#resolvePathAfter(HttpServletRequest, String)}
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
		return WebUtils.addUrlParam(url, qs);
	}
	
	/**
	 * 设置下载文件响应头。
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 * @throws IOException
	 */
	protected void setDownloadResponseHeader(HttpServletRequest request, HttpServletResponse response, String fileName) throws IOException
	{
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, fileName));
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

	/**
	 * 获取{@linkplain AuthenticationException}。
	 * 
	 * @param request
	 * @param removeSession
	 * @return 返回{@code null}表示没有
	 */
	protected AuthenticationException getAuthenticationException(HttpServletRequest request, boolean removeSession)
	{
		// 参考org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler.saveException()

		AuthenticationException exception = (AuthenticationException) request
				.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		if (exception == null)
		{
			HttpSession session = request.getSession();

			exception = (AuthenticationException) session
					.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

			if (exception != null && removeSession)
				session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}

		return exception;
	}

	/**
	 * 写上传文件。
	 * 
	 * @param multipartFile
	 * @param file
	 * @throws IOException
	 */
	protected void writeMultipartFile(MultipartFile multipartFile, File file) throws IOException
	{
		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = multipartFile.getInputStream();
			out = IOUtil.getOutputStream(file);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	/**
	 * 写上传文件。
	 * 
	 * @param multipartFile
	 * @param out
	 * @param closeOut
	 * @throws IOException
	 */
	protected void writeMultipartFile(MultipartFile multipartFile, OutputStream out, boolean closeOut)
			throws IOException
	{
		InputStream in = null;

		try
		{
			in = multipartFile.getInputStream();
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);

			if (closeOut)
			{
				IOUtil.close(out);
			}
		}
	}
}
