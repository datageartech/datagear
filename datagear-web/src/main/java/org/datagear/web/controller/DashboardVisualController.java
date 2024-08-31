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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.datagear.analysis.support.html.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlChartWidgetJsonRenderer;
import org.datagear.analysis.support.html.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.LoadableChartWidgets;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DashboardShareSet;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.DashboardShareSetService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.util.FileUtil;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.html.DefaultFilterHandler;
import org.datagear.util.html.HeadBodyAwareFilterHandler;
import org.datagear.util.html.HtmlFilter;
import org.datagear.util.html.RedirectWriter;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.config.CoreConfigSupport;
import org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.EditHtmlInfo;
import org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.EditHtmlInfoFilterHandler;
import org.datagear.web.controller.DashboardVisualController.DashboardShowForEdit.ShowHtmlFilterHandler;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.SessionDashboardInfoSupport.DashboardInfo;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

/**
 * 看板展示控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping(DashboardVisualController.PATH_PREFIX)
public class DashboardVisualController extends AbstractDataAnalysisController implements ServletContextAware
{
	/** 展示页路径前缀 */
	public static final String PATH_PREFIX = "/dv";

	/** 加载看板图表参数：看板ID */
	public static final String LOAD_CHART_PARAM_DASHBOARD_ID = "dashboardId";

	/** 加载看板图表参数：图表部件ID */
	public static final String LOAD_CHART_PARAM_CHART_WIDGET_ID = "chartWidgetId";

	/** 看板心跳参数：看板ID */
	public static final String HEARTBEAT_PARAM_DASHBOARD_ID = AbstractDataAnalysisController.HEARTBEAT_PARAM_DASHBOARD_ID;

	/** 看板卸载参数：看板ID */
	public static final String UNLOAD_PARAM_DASHBOARD_ID = AbstractDataAnalysisController.UNLOAD_PARAM_DASHBOARD_ID;

	/**
	 * 看板内置渲染上下文属性名：{@linkplain EditHtmlInfo}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "EDIT_HTML_INFO";

	/**
	 * 看板分享密码占位。
	 */
	public static final String DASHBOARD_SHARE_SET_PASSWORD_PLACEHOLDER = "DSP-PLACEHOLDER-"
			+ IDUtil.randomIdOnTime20();

	public static final String DASHBOARD_SHOW_AUTH_PARAM_NAME = "name";

	public static final String LOAD_CHART_FOR_EDITOR_PARAM = "loadChartForEditor";

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	@Qualifier(CoreConfigSupport.NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	private File dashboardGlobalResRootDirectory;

	@Autowired
	private DashboardShareSetService dashboardShareSetService;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private HtmlChartWidgetJsonRenderer htmlChartWidgetJsonRenderer;

	@Autowired
	private HtmlFilter htmlFilter;

	private ServletContext servletContext;

	public DashboardVisualController()
	{
		super();
	}

	public HtmlTplDashboardWidgetEntityService getHtmlTplDashboardWidgetEntityService()
	{
		return htmlTplDashboardWidgetEntityService;
	}

	public void setHtmlTplDashboardWidgetEntityService(
			HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService)
	{
		this.htmlTplDashboardWidgetEntityService = htmlTplDashboardWidgetEntityService;
	}

	public File getDashboardGlobalResRootDirectory()
	{
		return dashboardGlobalResRootDirectory;
	}

	public void setDashboardGlobalResRootDirectory(File dashboardGlobalResRootDirectory)
	{
		this.dashboardGlobalResRootDirectory = dashboardGlobalResRootDirectory;
	}

	public DashboardShareSetService getDashboardShareSetService()
	{
		return dashboardShareSetService;
	}

	public void setDashboardShareSetService(DashboardShareSetService dashboardShareSetService)
	{
		this.dashboardShareSetService = dashboardShareSetService;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	public HtmlChartWidgetJsonRenderer getHtmlChartWidgetJsonRenderer()
	{
		return htmlChartWidgetJsonRenderer;
	}

	public void setHtmlChartWidgetJsonRenderer(HtmlChartWidgetJsonRenderer htmlChartWidgetJsonRenderer)
	{
		this.htmlChartWidgetJsonRenderer = htmlChartWidgetJsonRenderer;
	}

	public HtmlFilter getHtmlFilter()
	{
		return htmlFilter;
	}

	public void setHtmlFilter(HtmlFilter htmlFilter)
	{
		this.htmlFilter = htmlFilter;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	/**
	 * 看板展示认证页面。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 *            看板ID
	 * @param name
	 *            重定向的看板资源名，允许为{@code null}，格式为："abc.html"、"def/def.js"、"detail/detail.html?param=0"
	 * @throws Exception
	 */
	@RequestMapping({ "/auth/{id}", "/auth/{id}/**" })
	public String showAuth(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id,
			@RequestParam(value = DASHBOARD_SHOW_AUTH_PARAM_NAME, required = false) String name) throws Exception
	{
		User user = getCurrentUser();
		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, id);

		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		if (!StringUtil.isEmpty(name))
			name = WebUtils.decodeURL(name);

		String redirectPath = WebUtils.getContextPath(request) + resolveShowPath(request, id);
		String submitPath = resolveAuthSubmitPath(request);

		if (!StringUtil.isEmpty(name))
		{
			redirectPath = FileUtil.concatPath(redirectPath, name, FileUtil.PATH_SEPARATOR_SLASH, false);
		}

		redirectPath = addSessionIdParamIfNeed(redirectPath, request);
		submitPath = addSessionIdParamIfNeed(submitPath, request);

		boolean authed = isShowAuthed(request, user, dashboardWidget);

		Map<String, Object> authModel = new HashMap<String, Object>();
		authModel.put("id", id);
		authModel.put("name", (name == null ? "" : name));
		authModel.put("redirectPath", redirectPath);
		authModel.put("authed", authed);
		authModel.put("dashboardNameMask", StringUtil.mask(dashboardWidget.getName(), 2, 2, 6));

		setFormModel(model, authModel, "auth", "authcheck");
		model.addAttribute("authed", authed);
		model.addAttribute("submitPath", submitPath);
		model.addAttribute("redirectPath", redirectPath);

		return "/dashboard/dashboard_show_auth";
	}

	/**
	 * 看板展示认证校验。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param form
	 * @throws Exception
	 */
	@RequestMapping(value = "/authcheck", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> showAuthCheck(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody ShowAuthCheckForm form) throws Exception
	{
		if (isEmpty(form.getId()))
			throw new IllegalInputException();

		User user = getCurrentUser();
		HtmlTplDashboardWidgetEntity dashboardWidget = getByIdForView(this.htmlTplDashboardWidgetEntityService, user,
				form.getId());

		String password = (StringUtil.isEmpty(form.getPassword()) ? "" : form.getPassword());

		ResponseEntity<OperationMessage> responseEntity = null;

		String dashboardWidgetId = dashboardWidget.getId();
		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(dashboardWidgetId);

		if (dashboardShareSet == null || !dashboardShareSet.isEnablePassword())
		{
			responseEntity = optSuccessDataResponseEntity(request,
					ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_SUCCESS));
		}
		else
		{
			DashboardShowAuthCheckManager manager = getSessionDashboardShowAuthCheckManager(request);

			if (manager.isAuthDenied(dashboardWidgetId))
			{
				responseEntity = optSuccessDataResponseEntity(request, ShowAuthCheckResponse
						.valueOf(ShowAuthCheckResponse.TYPE_DENY, manager.getAuthFailThreshold(), 0));
			}
			else if (password.equals(dashboardShareSet.getPassword()))
			{
				manager.setAuthed(dashboardWidgetId, true);
				responseEntity = optSuccessDataResponseEntity(request,
						ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_SUCCESS));
			}
			else
			{
				manager.setAuthed(dashboardWidgetId, false);
				int authRemain = manager.authRemain(dashboardWidgetId);

				if (authRemain > 0)
				{
					responseEntity = optSuccessDataResponseEntity(request, ShowAuthCheckResponse
							.valueOf(ShowAuthCheckResponse.TYPE_FAIL, manager.getAuthFailThreshold(), authRemain));
				}
				else
				{
					responseEntity = optSuccessDataResponseEntity(request, ShowAuthCheckResponse
							.valueOf(ShowAuthCheckResponse.TYPE_DENY, manager.getAuthFailThreshold(), 0));
				}
			}

			setSessionDashboardShowAuthCheckManager(request, manager);
		}

		return responseEntity;
	}

	/**
	 * 看板展示请求是否已通过密码验证。
	 * 
	 * @param request
	 * @param user
	 * @param dashboardWidget
	 * @return
	 */
	protected boolean isShowAuthed(HttpServletRequest request, User user, HtmlTplDashboardWidgetEntity dashboardWidget)
	{
		// 有编辑权限，无需认证
		if (Authorization.canEdit(dashboardWidget.getDataPermission()))
			return true;

		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(dashboardWidget.getId());

		if (dashboardShareSet == null || !dashboardShareSet.isEnablePassword())
			return true;

		if (dashboardShareSet.isAnonymousPassword() && !user.isAnonymous())
			return true;

		DashboardShowAuthCheckManager manager = getSessionDashboardShowAuthCheckManager(request);
		return manager.isAuthed(dashboardWidget.getId());
	}

	protected String buildShowAuthUrlForShowRequest(HttpServletRequest request, HttpServletResponse response, String id,
			String resName) throws Exception
	{
		resName = appendRequestQueryString((resName == null ? "" : resName), request);
		String authPath = WebUtils.getContextPath(request) + resolveAuthPath(request, id);
		authPath = addSessionIdParamIfNeed(authPath, request);
		authPath = addSafeSessionParamIfNeed(authPath, request);

		if (!StringUtil.isEmpty(resName))
		{
			authPath = WebUtils.addUrlParam(authPath, DASHBOARD_SHOW_AUTH_PARAM_NAME, WebUtils.encodeURL(resName));
		}

		return authPath;
	}

	/**
	 * 获取会话中的{@linkplain DashboardShowAuthCheckManager}。
	 * <p>
	 * 如果修改了获取的{@linkplain DashboardShowAuthCheckManager}的状态，应在修改之后调用{@linkplain #setSessionDashboardShowAuthCheckManager(HttpServletRequest, DashboardShowAuthCheckManager)}，
	 * 以为可能扩展的分布式会话提供支持。
	 * </p>
	 * 
	 * @param request
	 * @return 非{@code null}
	 */
	protected DashboardShowAuthCheckManager getSessionDashboardShowAuthCheckManager(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		DashboardShowAuthCheckManager manager = null;

		synchronized (session)
		{
			manager = (DashboardShowAuthCheckManager) session
					.getAttribute(DashboardShowAuthCheckManager.class.getName());

			if (manager == null)
			{
				manager = new DashboardShowAuthCheckManager(
						this.applicationProperties.getDashboardSharePsdAuthFailThreshold(),
						this.applicationProperties.getDashboardSharePsdAuthFailPastMinutes() * 60 * 1000);

				session.setAttribute(DashboardShowAuthCheckManager.class.getName(), manager);
			}
		}

		return manager;
	}

	/**
	 * 设置会话中的{@linkplain DashboardShowAuthCheckManager}。
	 * 
	 * @param request
	 * @param manager
	 */
	protected void setSessionDashboardShowAuthCheckManager(HttpServletRequest request,
			DashboardShowAuthCheckManager manager)
	{
		HttpSession session = request.getSession();
		session.setAttribute(DashboardShowAuthCheckManager.class.getName(), manager);
	}

	/**
	 * 展示看板首页。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping({ "/{id}/", "/{id}" })
	public void show(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id) throws Exception
	{
		String requestPath = resolvePathAfter(request, "");
		String correctPath = WebUtils.getContextPath(request) + resolveShowPath(request, id);

		// 如果是"/{id}"请求，则应跳转到"/{id}/"，因为看板内的超链接使用的都是相对路径，
		// 如果末尾不加"/"，将会导致这些超链接路径错误
		if (requestPath.indexOf(correctPath) < 0)
		{
			String redirectPath = correctPath;
			redirectPath = addSessionIdParamIfNeed(redirectPath, request);
			redirectPath = appendRequestQueryString(redirectPath, request);
			response.sendRedirect(redirectPath);
		}
		else
		{
			User user = getCurrentUser();
			HtmlTplDashboardWidgetEntity dashboardWidget = getHtmlTplDashboardWidgetEntityForShow(request, user, id);

			if (!isShowAuthed(request, user, dashboardWidget))
			{
				String authPath = buildShowAuthUrlForShowRequest(request, response, id, "");
				response.sendRedirect(authPath);
				return;
			}

			String firstTemplate = dashboardWidget.getFirstTemplate();

			// 如果首页模板是在嵌套路径下，则应重定向到具体路径，避免页面内以相对路径引用的资源找不到
			int subPathSlashIdx = firstTemplate.indexOf(FileUtil.PATH_SEPARATOR_SLASH);
			if (subPathSlashIdx > 0 && subPathSlashIdx < firstTemplate.length() - 1)
			{
				String redirectPath = correctPath + WebUtils.encodePathURL(firstTemplate);
				redirectPath = addSessionIdParamIfNeed(redirectPath, request);
				redirectPath = appendRequestQueryString(redirectPath, request);

				response.sendRedirect(redirectPath);
			}
			else
			{
				showDashboard(request, response, model, user, dashboardWidget, firstTemplate,
						isDashboardShowForEditRequest(request, dashboardWidget));
			}
		}
	}

	/**
	 * 加载看板资源。
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			Model model, @PathVariable("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboardWidget = getHtmlTplDashboardWidgetEntityForShow(request, user, id);
		String resName = resolvePathAfter(request, resolveShowPath(request, id));

		if (resName == null)
			throw new FileNotFoundException(resName);

		resName = WebUtils.decodeURL(resName);

		// 对于“/xxx/;jsessionid=xxxx”的请求，resName将会是空字符串
		if (StringUtil.isEmpty(resName))
		{
			resName = dashboardWidget.getFirstTemplate();

			// 如果首页模板是在嵌套路径下，则应重定向到具体路径，避免页面内以相对路径引用的资源找不到
			int subPathSlashIdx = resName.indexOf(FileUtil.PATH_SEPARATOR_SLASH);
			if (subPathSlashIdx > 0 && subPathSlashIdx < resName.length() - 1)
			{
				String redirectPath = WebUtils.getContextPath(request) + resolveShowPath(request, id)
						+ WebUtils.encodePathURL(resName);
				redirectPath = addSessionIdParamIfNeed(redirectPath, request);
				redirectPath = appendRequestQueryString(redirectPath, request);

				response.sendRedirect(redirectPath);
				return;
			}
		}

		if (!isShowAuthed(request, user, dashboardWidget))
		{
			String authPath = buildShowAuthUrlForShowRequest(request, response, id, resName);
			response.sendRedirect(authPath);
			return;
		}

		boolean isShowForEdit = isDashboardShowForEditRequest(request, dashboardWidget);

		if (dashboardWidget.isTemplate(resName) || isShowForEdit)
		{
			showDashboard(request, response, model, user, dashboardWidget, resName, isShowForEdit);
		}
		else
		{
			InputStream in = null;

			TplDashboardWidgetResManager resManager = this.htmlTplDashboardWidgetEntityService
					.getTplDashboardWidgetResManager();

			// 优先本地资源
			if (resManager.exists(id, resName))
			{
				long lastModified = resManager.lastModified(id, resName);
				if (webRequest.checkNotModified(lastModified))
					return;

				setContentTypeByName(request, response, getServletContext(), resName);
				setCacheControlNoCache(response);

				in = resManager.getInputStream(id, resName);
			}
			// 其次全局资源
			else
			{
				if (!StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix())
						&& resName.startsWith(this.applicationProperties.getDashboardGlobalResUrlPrefix()))
				{
					resName = resName.substring(this.applicationProperties.getDashboardGlobalResUrlPrefix().length());
				}

				File globalRes = FileUtil.getFile(dashboardGlobalResRootDirectory, resName);

				if (globalRes.exists() && !globalRes.isDirectory())
				{
					long lastModified = globalRes.lastModified();
					if (webRequest.checkNotModified(lastModified))
						return;

					setContentTypeByName(request, response, getServletContext(), resName);
					setCacheControlNoCache(response);

					in = IOUtil.getInputStream(globalRes);
				}
			}

			if (in != null)
			{
				in = IOUtil.getBufferedInputStream(in);
				OutputStream out = IOUtil.getBufferedOutputStream(response.getOutputStream());

				try
				{
					IOUtil.write(in, out);
				}
				finally
				{
					IOUtil.close(in);
					IOUtil.close(out);
				}
			}
			else
				throw new FileNotFoundException(resName);
		}
	}

	/**
	 * 加载看板全局资源。
	 * <p>
	 * 上述{@linkplain #showResource(HttpServletRequest, HttpServletResponse, WebRequest, Model, String)}
	 * 支持在看版内以{@code "global/**"}相对地址的方式引入全局资源，这简化了写法，但会导致浏览器无法跨看板缓存全局资源，
	 * 因为全局资源的绝对路径对于不同的看板也是不同的。
	 * </p>
	 * <p>
	 * 此方法则是以固定的绝对路径加载全局资源，使得浏览器可以跨看板缓存它们，缺点是看板内需以{@code "../global/**"}相对地址的方式引入全局资源。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @throws Exception
	 * @see {@linkplain ApplicationProperties#getDashboardGlobalResUrlPrefixName()}
	 */
	@RequestMapping("/#{applicationProperties.getDashboardGlobalResUrlPrefixName()}/**")
	public void showGlobalResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
			throws Exception
	{
		String resName = resolvePathAfter(request, resolveGlobalResPath(request));

		if (StringUtil.isEmpty(resName))
			throw new FileNotFoundException(resName);

		resName = WebUtils.decodeURL(resName);
		File globalRes = FileUtil.getFile(dashboardGlobalResRootDirectory, resName);

		if (!globalRes.exists() || globalRes.isDirectory())
			throw new FileNotFoundException(resName);

		long lastModified = globalRes.lastModified();
		if (webRequest.checkNotModified(lastModified))
			return;

		setContentTypeByName(request, response, getServletContext(), resName);
		setCacheControlNoCache(response);

		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = IOUtil.getBufferedInputStream(IOUtil.getInputStream(globalRes));
			out = IOUtil.getBufferedOutputStream(response.getOutputStream());

			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	/**
	 * 展示看板。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param currentUser
	 * @param dashboardWidget
	 * @param template
	 * @param isShowForEdit
	 * @throws Exception
	 */
	protected void showDashboard(HttpServletRequest request, HttpServletResponse response,
			Model model, User currentUser, HtmlTplDashboardWidgetEntity dashboardWidget,
			String template, boolean isShowForEdit) throws Exception
	{
		User createUser = dashboardWidget.getCreateUser();

		String showHtml = (isShowForEdit
				? buildShowForEditShowHtml(request.getParameter(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT))
				: "");
		EditHtmlInfo editHtmlInfo = (isShowForEdit ? buildShowForEditEditHtmlInfo(showHtml) : null);

		Reader showHtmlIn = null;
		Writer out = null;

		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(createUser));

		try
		{
			showHtmlIn = (isShowForEdit ? IOUtil.getReader(showHtml) : null);
			String responseEncoding = dashboardWidget.getTemplateEncoding();

			if (StringUtil.isEmpty(responseEncoding))
				responseEncoding = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager()
						.getDefaultEncoding();

			response.setCharacterEncoding(responseEncoding);
			response.setContentType(CONTENT_TYPE_HTML);
			out = IOUtil.getBufferedWriter(response.getWriter());

			List<HtmlTplDashboardImport> importList = (isShowForEdit ? buildHtmlTplDashboardImportsForEdit(request)
					: buildHtmlTplDashboardImportsForShow(request));

			HtmlTitleHandler htmlTitleHandler = getShowDashboardHtmlTitleHandler(request, response, currentUser,
					dashboardWidget);
			HtmlTplDashboardRenderContext renderContext = createRenderContext(request, response, template, out,
					createWebContext(request), importList, htmlTitleHandler);

			// 移除参数中的模板内容，一是它不应该传入页面，二是它可能包含"</script>"子串，传回浏览器端时会导致页面解析出错
			renderContext.removeAttribute(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT);

			if (isShowForEdit)
				renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO, editHtmlInfo);

			if (showHtmlIn != null)
			{
				renderContext.setTemplateReader(showHtmlIn);
				renderContext.setTemplateLastModified(HtmlTplDashboardRenderContext.TEMPLATE_LAST_MODIFIED_NONE);
			}

			HtmlTplDashboard dashboard = dashboardWidget.render(renderContext);
			getSessionDashboardInfoSupport().setDashboardInfo(request, new DashboardInfo(dashboard, isShowForEdit));
		}
		finally
		{
			IOUtil.close(showHtmlIn);
			IOUtil.close(out);
			ChartWidgetSourceContext.remove();
		}
	}

	protected HtmlTitleHandler getShowDashboardHtmlTitleHandler(HttpServletRequest request,
			HttpServletResponse response, User user, HtmlTplDashboardWidgetEntity dashboardWidget) throws Exception
	{
		return new DefaultHtmlTitleHandler(
				getMessage(request, "dashboard.show.htmlTitleSuffix", getMessage(request, "app.name")),
				getMessage(request, "dashboard.show.htmlTitleSuffixForEmpty", dashboardWidget.getName(),
						getMessage(request, "app.name")));
	}

	/**
	 * 获取看板展示时使用的看板部件。
	 * 
	 * @param request
	 * @param user
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 *             没有权限时
	 * @throws RecordNotFoundException
	 *             记录不存在时
	 */
	protected HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidgetEntityForShow(HttpServletRequest request, User user,
			String id) throws PermissionDeniedException, RecordNotFoundException
	{
		if (StringUtil.isEmpty(id))
			throw new IllegalInputException();

		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, id);

		if (dashboardWidget == null)
				throw new RecordNotFoundException();

		return dashboardWidget;
	}

	protected String buildShowForEditShowHtml(String templateContent) throws IOException
	{
		String showHtml = "";

		if (templateContent != null)
		{
			// 加一个"s"前缀与前端生成的区分
			String staticIdPrefix = Global.PRODUCT_NAME_EN_LC + "s" + Long.toHexString(System.currentTimeMillis());

			StringReader in = null;
			StringWriter out = null;

			try
			{
				in = IOUtil.getReader(templateContent);
				out = new StringWriter(templateContent.length());

				ShowHtmlFilterHandler fh = new ShowHtmlFilterHandler(out, staticIdPrefix);
				this.htmlFilter.filter(in, fh);

				showHtml = out.toString();
			}
			finally
			{
				IOUtil.close(in);
				IOUtil.close(out);
			}
		}

		return showHtml;
	}

	protected EditHtmlInfo buildShowForEditEditHtmlInfo(String showHtml) throws IOException
	{
		String beforeBodyHtml = "";
		String bodyHtml = "";
		String afterBodyHtml = "";
		Map<String, String> placeholderSources = Collections.emptyMap();

		if (showHtml != null)
		{
			StringReader in = null;
			EditHtmlInfoFilterHandler fh = null;

			try
			{
				in = IOUtil.getReader(showHtml);
				fh = new EditHtmlInfoFilterHandler();

				this.htmlFilter.filter(in, fh);
			}
			finally
			{
				IOUtil.close(in);
			}

			beforeBodyHtml = fh.getBeforeBodyHtml();
			bodyHtml = fh.getBodyHtml();
			afterBodyHtml = fh.getAfterBodyHtml();
			placeholderSources = fh.getPlaceholderSources();
		}

		return new EditHtmlInfo(escapeDashboardRenderContextAttrValue(beforeBodyHtml),
				escapeDashboardRenderContextAttrValue(bodyHtml), escapeDashboardRenderContextAttrValue(afterBodyHtml),
				escapeDashboardRenderContextAttrValue(placeholderSources));
	}

	protected boolean isDashboardShowForEditParam(HttpServletRequest request)
	{
		String editTemplate = request.getParameter(DASHBOARD_SHOW_PARAM_EDIT_TEMPLATE);
		// 有编辑模板请求参数
		return StringUtil.toBoolean(editTemplate);
	}

	protected boolean isDashboardShowForEditRequest(HttpServletRequest request,
			HtmlTplDashboardWidgetEntity dashboardWidget)
	{
		// 有编辑模板请求参数、且有编辑权限
		return (isDashboardShowForEditParam(request) && Authorization.canEdit(dashboardWidget.getDataPermission()));
	}

	/**
	 * 看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param form
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/data", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		// 此处获取ChartWidget不再需要权限控制，应显式移除线程变量
		ChartWidgetSourceContext.remove();

		DashboardResult dashboardResult = getDashboardResult(request, response, form,
				this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer());

		return new ErrorMessageDashboardResult(dashboardResult, true);
	}

	/**
	 * 加载多个看板图表的JSON对象数组。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param dashboardId
	 * @param chartWidgetIds
	 * @throws Throwable
	 */
	@RequestMapping(value = "/loadChart", produces = CONTENT_TYPE_JSON)
	public void loadChart(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam(LOAD_CHART_PARAM_DASHBOARD_ID) String dashboardId,
			@RequestParam(LOAD_CHART_PARAM_CHART_WIDGET_ID) String[] chartWidgetIds,
			@RequestParam(value = LOAD_CHART_FOR_EDITOR_PARAM, required = false) String loadChartForEditorStr)
			throws Throwable
	{
		User user = getCurrentUser();

		DashboardInfo dashboardInfo = getSessionDashboardInfoSupport().getDashboardInfo(request, dashboardId);

		if (dashboardInfo == null)
			throw new RecordNotFoundException();

		boolean loadChartForEditor = StringUtil.toBoolean(loadChartForEditorStr);
		loadChartForEditor = (dashboardInfo.isShowForEdit() && loadChartForEditor);

		HtmlTplDashboardWidgetEntity dashboardWidget = getHtmlTplDashboardWidgetEntityForShow(request, user,
				dashboardInfo.getDashboardWidgetId());

		HtmlChartWidget[] chartWidgets = new HtmlChartWidget[chartWidgetIds.length];

		HtmlTplDashboardWidgetRenderer dashboardWidgetRenderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();

		try
		{
			handleLoadChartPattern(user, dashboardInfo, dashboardWidget, chartWidgetIds, chartWidgets,
					dashboardWidgetRenderer, loadChartForEditor);

			for (int i = 0; i < chartWidgetIds.length; i++)
			{
				if (chartWidgets[i] == null)
					chartWidgets[i] = dashboardWidgetRenderer.getHtmlChartWidget(chartWidgetIds[i]);
			}

			// 不缓存
			response.setContentType(CONTENT_TYPE_JSON);
			PrintWriter out = response.getWriter();

			HtmlChart[] charts = this.htmlChartWidgetJsonRenderer.render(out, chartWidgets);

			Map<String, String> chartIdToChartWidgetIds = new HashMap<String, String>();
			for (int i = 0; i < chartWidgets.length; i++)
				chartIdToChartWidgetIds.put(charts[i].getId(), chartWidgets[i].getId());

			getSessionDashboardInfoSupport().addDashboardInfoCharts(request, dashboardInfo, chartIdToChartWidgetIds);
		}
		finally
		{
			ChartWidgetSourceContext.remove();
		}
	}

	protected void handleLoadChartPattern(User currentUser, DashboardInfo dashboardInfo,
			HtmlTplDashboardWidgetEntity dashboardWidget, String[] chartWidgetIds, HtmlChartWidget[] chartWidgets,
			HtmlTplDashboardWidgetRenderer renderer, boolean loadChartForEditor) throws Throwable
	{
		LoadableChartWidgets lcws = null;

		// 看板可视编辑模式时，插入图表操作不应受看板内定义的异步加载权限控制
		if (loadChartForEditor)
		{
			// 看板展示时是根据创建用户权限加载图表的，这里也应保持一致
			if (Authorization.canEdit(dashboardWidget.getDataPermission()))
				lcws = LoadableChartWidgets.all();
			else
				lcws = LoadableChartWidgets.none();
		}
		else
		{
			lcws = dashboardInfo.getLoadableChartWidgets();

			// 默认应设为permitted，防止用户在看板内异步加载任意图表部件
			if (lcws == null)
				lcws = LoadableChartWidgets.permitted();
		}

		// 可异步加载看板创建者有权限的所有图表
		if (lcws.isPatternAll())
		{
			// 使用看板创建用户
			ChartWidgetSourceContext.set(new ChartWidgetSourceContext(dashboardWidget.getCreateUser()));
		}
		// 不可异步加载任何图表
		else if (lcws.isPatternNone())
		{
			for (int i = 0; i < chartWidgetIds.length; i++)
			{
				String chartWidgetId = chartWidgetIds[i];

				PermissionDeniedException e = new PermissionDeniedException("Permission denied");
				chartWidgets[i] = renderer.getHtmlChartWidgetForException(chartWidgetId, e);
			}
		}
		// 仅可异步加载当前用户有权限的图表
		else if (lcws.isPatternPermitted())
		{
			// 使用当前用户
			ChartWidgetSourceContext.set(new ChartWidgetSourceContext(currentUser));
		}
		// 仅可异步加载看板创建者有权限的、且在指定列表内的图表
		else if (lcws.isPatternList())
		{
			for (int i = 0; i < chartWidgetIds.length; i++)
			{
				String chartWidgetId = chartWidgetIds[i];

				if (!lcws.inList(chartWidgetId))
				{
					PermissionDeniedException e = new PermissionDeniedException("Permission denied");
					chartWidgets[i] = renderer.getHtmlChartWidgetForException(chartWidgetId, e);
				}
			}

			// 使用看板创建用户
			ChartWidgetSourceContext.set(new ChartWidgetSourceContext(dashboardWidget.getCreateUser()));
		}
		// 未知模式
		else
		{
			for (int i = 0; i < chartWidgetIds.length; i++)
			{
				String chartWidgetId = chartWidgetIds[i];

				PermissionDeniedException e = new PermissionDeniedException(
						"Permission denied for unknown pattern '" + lcws.getPattern() + "'");
				chartWidgets[i] = renderer.getHtmlChartWidgetForException(chartWidgetId, e);
			}
		}
	}

	/**
	 * 看板心跳。
	 * <p>
	 * 看板页面有停留较长时间再操作的场景，此时可能会因为会话超时导致操作失败，所以这里添加心跳请求，避免会话超时。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = HEARTBEAT_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(HEARTBEAT_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.handleHeartbeat(request, response, dashboardId);
	}

	/**
	 * 看板卸载。
	 * <p>
	 * 看板页面关闭后，应卸载后台看板数据。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = UNLOAD_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> unloadDashboard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(UNLOAD_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.handleUnloadDashboard(request, response, dashboardId);
	}

	protected WebContext createWebContext(HttpServletRequest request)
	{
		WebContext webContext = createInitWebContext(request);

		addUpdateDataValue(request, webContext, resolveDataPath(request));
		addLoadChartValue(request, webContext, resolveLoadChartPath(request));
		addHeartBeatValue(request, webContext, resolveHeartbeatPath(request));
		addUnloadValue(request, webContext, resolveUnloadPath(request));

		return webContext;
	}

	/**
	 * 解析展示路径。
	 * 
	 * @param request
	 * @param dashboardId
	 * @return
	 */
	protected String resolveShowPath(HttpServletRequest request, String dashboardId)
	{
		return PATH_PREFIX + "/" + dashboardId + "/";
	}

	/**
	 * 解析加载数据路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveDataPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/data";
	}

	/**
	 * 解析加载图表路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveLoadChartPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/loadChart";
	}

	/**
	 * 解析心跳路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveHeartbeatPath(HttpServletRequest request)
	{
		return PATH_PREFIX + HEARTBEAT_TAIL_URL;
	}

	/**
	 * 解析卸载看板路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveUnloadPath(HttpServletRequest request)
	{
		return PATH_PREFIX + UNLOAD_TAIL_URL;
	}

	/**
	 * 解析全局资源路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveGlobalResPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/" + this.applicationProperties.getDashboardGlobalResUrlPrefixName() + "/";
	}

	/**
	 * 解析认证路径。
	 * 
	 * @param request
	 * @param dashboardId
	 * @return
	 */
	protected String resolveAuthPath(HttpServletRequest request, String dashboardId)
	{
		return PATH_PREFIX + "/auth/" + dashboardId + "/";
	}

	/**
	 * 解析认证校验路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveAuthSubmitPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/authcheck";
	}

	/**
	 * 看板可视编辑支持类。
	 * <p>
	 * 看板可视编辑的基本思路是：
	 * </p>
	 * <ol>
	 * <li>
	 * 读取原始看板HTML模板，为其<code>&lt;body&gt;&lt;/body&gt;</code>中的所有元素添加<code>dg-visual-edit-id</code>属性，
	 * 生成【展示HTML】（参考{@linkplain ShowHtmlFilterHandler}）。</li>
	 * <li>读取【展示HTML】，生成【编辑HTML信息】（参考{@linkplain EditHtmlInfoFilterHandler}），
	 * 【编辑HTML信息】由四部分组成：<br>
	 * 【body前】、【body体】、【body后】、【占位标签源映射表】，<br>
	 * 其中：<br>
	 * 【body前】是【展示HTML】的<code>&lt;body&gt;</code>前片段；<br>
	 * 【body体】是【展示HTML】的<code>&lt;body&gt;&lt;/body&gt;</code>体片段，且是已进行【占位标签替换】的；<br>
	 * 【body后】是【展示HTML】的<code>&lt;/body&gt;</code>后片段；<br>
	 * 【占位标签源映射表】存储【body体】中占位标签的原始标签信息。<br>
	 * 【占位标签替换】是将【展示HTML】的<code>&lt;body&gt;&lt;/body&gt;</code>体中的在渲染时会影响DOM结构的标签（<code>link, style, script</code>）替换为
	 * <code>&lt;--dg-placeholder-i--&gt;</code>注释标签。</li>
	 * <li>
	 * 在【展示HTML】中插入【可视编辑JS库】、在其{@linkplain RenderContext}插入【编辑HTML信息】，渲染为【可视编辑效果页面】。
	 * </li>
	 * <li>在【可视编辑效果页面】中，【可视编辑JS库】将【编辑HTML信息】的【body体】渲染至一个隔离iframe中。</li>
	 * <li>【可视编辑JS库】将所有【可视编辑效果页面】中的可视编辑操作同步至隔离iframe中的相同<code>dg-visual-edit-id</code>的HTML元素。</li>
	 * <li>【可视编辑JS库】读取隔离iframe中的<code>&lt;body&gt;&lt;/body&gt;</code>体HTML，使用【编辑HTML信息】中的【占位标签源映射表】进行还原，
	 * 使用【body前】、【body后】进行拼接，生成可视编辑结果HTML。</li>
	 * </ol>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DashboardShowForEdit
	{
		/**
		 * 看板可视编辑时的内元素可视编辑ID属性名。
		 */
		public static final String ELEMENT_ATTR_VISUAL_EDIT_ID = HtmlTplDashboardWidgetRenderer.DASHBOARD_ELEMENT_ATTR_PREFIX
				+ "visual-edit-id";

		/**
		 * 【展示HTML】过滤处理器。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class ShowHtmlFilterHandler extends HeadBodyAwareFilterHandler
		{
			private final String staticIdPrefix;

			private int staticIdSequence = 0;

			public ShowHtmlFilterHandler(Writer out, String staticIdPrefix)
			{
				super(out);
				this.staticIdPrefix = staticIdPrefix;
			}

			public String getStaticIdPrefix()
			{
				return staticIdPrefix;
			}

			@Override
			public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
					throws IOException
			{
				if (this.isInBodyTag() && !isCloseTagName(tagName))
				{
					write(" " + ELEMENT_ATTR_VISUAL_EDIT_ID + "=\"" + this.staticIdPrefix + (this.staticIdSequence++)
							+ "\" ");
				}
			}
		}

		/**
		 * 【编辑HTML信息】过滤处理器。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfoFilterHandler extends DefaultFilterHandler
		{
			private static final String PLACEHOLDER_PREFIX = HtmlTplDashboardWidgetRenderer.DASHBOARD_ELEMENT_ATTR_PREFIX
					+ "placeholder-";

			private final Map<String, String> placeholderSources = new HashMap<String, String>();

			private int placeholderSequence = 0;

			private String currentPlaceholderKey;

			public EditHtmlInfoFilterHandler()
			{
				super(new EditHtmlInfoWriter(new StringWriter(), new StringWriter(), new StringWriter(),
						new StringWriter(), false));
			}

			public String getBeforeBodyHtml()
			{
				return getEditHtmlWriter().getBeforeBodyOut().toString();
			}

			public String getBodyHtml()
			{
				return getEditHtmlWriter().getBodyOut().toString();
			}

			public String getAfterBodyHtml()
			{
				return getEditHtmlWriter().getAfterBodyOut().toString();
			}

			public Map<String, String> getPlaceholderSources()
			{
				return placeholderSources;
			}

			@Override
			public void beforeWriteTagStart(Reader in, String tagName) throws IOException
			{
				EditHtmlInfoWriter out = getEditHtmlWriter();

				// 设置<body> 写入out.getBodyOut()
				if (out.isOutStatusBeforeBody() && "body".equalsIgnoreCase(tagName))
					out.setOutStatus(EditHtmlInfoWriter.OUT_STATUS_BODY);

				if (out.isOutStatusBody() && !out.isRedirect())
				{
					// 将这些标签写入变向输出流
					if ("link".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName)
							|| "script".equalsIgnoreCase(tagName))
					{
						this.currentPlaceholderKey = nextPlaceholderKey();
						out.write(this.currentPlaceholderKey);

						out.setRedirect(true);
					}
				}
			}

			@Override
			public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
					throws IOException
			{
				EditHtmlInfoWriter out = getEditHtmlWriter();

				if (out.isOutStatusBody() && out.isRedirect())
				{
					// 从变向输出流中读取占位内容
					if ("/link".equalsIgnoreCase(tagName) || "/style".equalsIgnoreCase(tagName)
							|| "/script".equalsIgnoreCase(tagName)
							|| (isSelfCloseTagEnd(tagEnd) && ("link".equalsIgnoreCase(tagName)
									|| "style".equalsIgnoreCase(tagName) || "script".equalsIgnoreCase(tagName))))
					{
						String placeholderValue = ((StringWriter) out.getRedirectOut()).toString();
						this.placeholderSources.put(this.currentPlaceholderKey, placeholderValue);

						out.setRedirectOut(new StringWriter());
						out.setRedirect(false);
					}
				}

				// 设置</body>后写入out.getAfterBodyOut()
				if (out.isOutStatusBody() && ("/body".equalsIgnoreCase(tagName)
						|| "body".equalsIgnoreCase(tagName) && isSelfCloseTagEnd(tagEnd)))
					out.setOutStatus(EditHtmlInfoWriter.OUT_STATUS_AFTER_BODY);
			}

			protected String nextPlaceholderKey()
			{
				return "<!--" + PLACEHOLDER_PREFIX + (this.placeholderSequence++) + "-->";
			}

			protected EditHtmlInfoWriter getEditHtmlWriter()
			{
				return (EditHtmlInfoWriter) super.getOut();
			}
		}

		/**
		 * 【编辑HTML信息】输出流。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfoWriter extends RedirectWriter
		{
			/** 输出至{@linkplain #getBeforeBodyOut()} */
			public static final int OUT_STATUS_BEFORE_BODY = 0;

			/** 输出至{@linkplain #getBodyOut()} */
			public static final int OUT_STATUS_BODY = OUT_STATUS_BEFORE_BODY + 1;

			/** 输出至{@linkplain #getAfterBodyOut()} */
			public static final int OUT_STATUS_AFTER_BODY = OUT_STATUS_BODY + 1;

			private Writer bodyOut;

			private Writer afterBodyOut;

			private int outStatus = OUT_STATUS_BEFORE_BODY - 1;

			public EditHtmlInfoWriter()
			{
				super();
			}

			public EditHtmlInfoWriter(Writer beforeBodyOut, Writer bodyOut, Writer afterBodyOut, Writer redirectOut,
					boolean redirect)
			{
				super(beforeBodyOut, redirectOut, redirect);
				this.bodyOut = bodyOut;
				this.afterBodyOut = afterBodyOut;
			}

			public EditHtmlInfoWriter(Writer beforeBodyOut, Writer bodyOut, Writer afterBodyOut, Writer redirectOut,
					boolean redirect, Object lock)
			{
				super(beforeBodyOut, redirectOut, redirect, lock);
				this.bodyOut = bodyOut;
				this.afterBodyOut = afterBodyOut;
			}

			public Writer getBeforeBodyOut()
			{
				return super.getOut();
			}

			public void setBeforeBodyOut(Writer beforeBodyOut)
			{
				super.setOut(beforeBodyOut);
			}

			public Writer getBodyOut()
			{
				return bodyOut;
			}

			public void setBodyOut(Writer bodyOut)
			{
				this.bodyOut = bodyOut;
			}

			public Writer getAfterBodyOut()
			{
				return afterBodyOut;
			}

			public void setAfterBodyOut(Writer afterBodyOut)
			{
				this.afterBodyOut = afterBodyOut;
			}

			public int getOutStatus()
			{
				return outStatus;
			}

			public void setOutStatus(int outStatus)
			{
				this.outStatus = outStatus;
			}

			public boolean isOutStatusBeforeBody()
			{
				return (OUT_STATUS_BEFORE_BODY >= this.outStatus);
			}

			public boolean isOutStatusBody()
			{
				return (OUT_STATUS_BODY >= this.outStatus && this.outStatus > OUT_STATUS_BEFORE_BODY);
			}

			public boolean isOutStatusAfterBody()
			{
				return (OUT_STATUS_AFTER_BODY >= this.outStatus && this.outStatus > OUT_STATUS_BODY);
			}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{
				if (isRedirect())
					getRedirectOut().write(cbuf, off, len);
				else if (isOutStatusBeforeBody())
					getBeforeBodyOut().write(cbuf, off, len);
				else if (isOutStatusBody())
					getBodyOut().write(cbuf, off, len);
				else if (isOutStatusAfterBody())
					getAfterBodyOut().write(cbuf, off, len);
				else
					throw new IllegalStateException();
			}

			@Override
			public void flush() throws IOException
			{
				super.flush();
				this.bodyOut.flush();
				this.afterBodyOut.flush();
			}

			@Override
			public void close() throws IOException
			{
				super.close();
				this.bodyOut.close();
				this.afterBodyOut.close();
			}
		}

		/**
		 * 【编辑HTML信息】。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfo implements Serializable
		{
			private static final long serialVersionUID = 1L;

			/**
			 * 【body前】HTML
			 */
			private String beforeBodyHtml = "";

			/**
			 * 【body体】HTML
			 */
			private String bodyHtml = "";

			/**
			 * 【body后】HTML
			 */
			private String afterBodyHtml = "";

			/** 【占位标签源映射表】 */
			private Map<String, String> placeholderSources;

			public EditHtmlInfo(String beforeBodyHtml, String bodyHtml, String afterBodyHtml,
					Map<String, String> placeholderSources)
			{
				super();
				this.beforeBodyHtml = beforeBodyHtml;
				this.bodyHtml = bodyHtml;
				this.afterBodyHtml = afterBodyHtml;
				this.placeholderSources = placeholderSources;
			}

			public String getBeforeBodyHtml()
			{
				return beforeBodyHtml;
			}

			public void setBeforeBodyHtml(String beforeBodyHtml)
			{
				this.beforeBodyHtml = beforeBodyHtml;
			}

			public String getBodyHtml()
			{
				return bodyHtml;
			}

			public void setBodyHtml(String bodyHtml)
			{
				this.bodyHtml = bodyHtml;
			}

			public String getAfterBodyHtml()
			{
				return afterBodyHtml;
			}

			public void setAfterBodyHtml(String afterBodyHtml)
			{
				this.afterBodyHtml = afterBodyHtml;
			}

			public Map<String, String> getPlaceholderSources()
			{
				return placeholderSources;
			}

			public void setPlaceholderSources(Map<String, String> placeholderSources)
			{
				this.placeholderSources = placeholderSources;
			}
		}
	}

	public static class ShowAuthCheckForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String id;

		private String password;

		public ShowAuthCheckForm()
		{
			super();
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}
	}

	public static class ShowAuthCheckResponse implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 验证通过 */
		public static final String TYPE_SUCCESS = "success";

		/** 验证未通过 */
		public static final String TYPE_FAIL = "fail";

		/** 验证拒绝 */
		public static final String TYPE_DENY = "deny";

		/** 校验结果类型 */
		private String type;

		private int authFailThreshold = -1;

		/** 验证剩余次数 */
		private int authRemain = -1;

		public ShowAuthCheckResponse(String type)
		{
			super();
			this.type = type;
		}

		public ShowAuthCheckResponse(String type, int authFailThreshold, int authRemain)
		{
			super();
			this.type = type;
			this.authFailThreshold = authFailThreshold;
			this.authRemain = authRemain;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public int getAuthFailThreshold()
		{
			return authFailThreshold;
		}

		public void setAuthFailThreshold(int authFailThreshold)
		{
			this.authFailThreshold = authFailThreshold;
		}

		public int getAuthRemain()
		{
			return authRemain;
		}

		public void setAuthRemain(int authRemain)
		{
			this.authRemain = authRemain;
		}

		public static ShowAuthCheckResponse valueOf(String type)
		{
			return new ShowAuthCheckResponse(type);
		}

		public static ShowAuthCheckResponse valueOf(String type, int authFailThreshold, int authRemain)
		{
			return new ShowAuthCheckResponse(type, authFailThreshold, authRemain);
		}
	}

	public static class DashboardShowAuthCheckManager implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 失败验证次数，-1表示不限 */
		private int authFailThreshold = -1;

		/** 失败验证次数过去毫秒内 */
		private long authFailPastMs = 2 * 60 * 60 * 1000;

		private final Map<String, AuthCheckInfo> authCheckInfos = new HashMap<>();

		public DashboardShowAuthCheckManager()
		{
			super();
		}

		public DashboardShowAuthCheckManager(int authFailThreshold, long authFailPastMs)
		{
			super();
			this.authFailThreshold = authFailThreshold;
			this.authFailPastMs = authFailPastMs;
		}

		public int getAuthFailThreshold()
		{
			return authFailThreshold;
		}

		public void setAuthFailThreshold(int authFailThreshold)
		{
			this.authFailThreshold = authFailThreshold;
		}

		public long getAuthFailPastMs()
		{
			return authFailPastMs;
		}

		public void setAuthFailPastMs(long authFailPastMs)
		{
			this.authFailPastMs = authFailPastMs;
		}

		public synchronized boolean isAuthed(String dashboardId)
		{
			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);
			return (aci == null ? false : aci.isAuthed());
		}

		public synchronized void setAuthed(String dashboardId, boolean authed)
		{
			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);
			if (aci == null)
			{
				aci = new AuthCheckInfo();
				this.authCheckInfos.put(dashboardId, aci);
			}

			if (authed)
			{
				aci.setAuthed(true);
				aci.clearFailedDate();
			}
			else
			{
				aci.offerFailedDate();
				aci.pollFailedDate(this.authFailThreshold);
			}
		}

		public synchronized boolean isAuthDenied(String dashboardId)
		{
			if (this.authFailThreshold < 0)
				return false;

			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);

			if (aci == null)
				return false;

			int failedDateCount = aci.failedDateCount(this.authFailPastMs);
			return ((this.authFailThreshold - failedDateCount) <= 0);
		}

		public synchronized int authRemain(String dashboardId)
		{
			if (this.authFailThreshold < 0)
				return -1;

			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);

			int remain = this.authFailThreshold;

			if (aci != null)
				remain = this.authFailThreshold - aci.failedDateCount(this.authFailPastMs);

			return (remain < 0 ? 0 : remain);
		}

		public static class AuthCheckInfo implements Serializable
		{
			private static final long serialVersionUID = 1L;

			private boolean authed = false;

			private Queue<Long> failedDates = new LinkedList<Long>();

			public AuthCheckInfo()
			{
				super();
			}

			public boolean isAuthed()
			{
				return authed;
			}

			public void setAuthed(boolean authed)
			{
				this.authed = authed;
			}

			public Queue<Long> getFailedDates()
			{
				return failedDates;
			}

			public void setFailedDates(Queue<Long> failedDates)
			{
				this.failedDates = failedDates;
			}

			public void offerFailedDate()
			{
				this.failedDates.offer(System.currentTimeMillis());
			}

			public void offerFailedDate(long date)
			{
				this.failedDates.offer(date);
			}

			public void pollFailedDate(int keepCount)
			{
				while (this.failedDates.size() > keepCount)
					this.failedDates.poll();
			}

			public void clearFailedDate()
			{
				this.failedDates.clear();
			}

			public int failedDateCount(long pastMs)
			{
				int count = 0;

				long startDate = System.currentTimeMillis() - pastMs;
				for (Long ele : this.failedDates)
				{
					if (ele >= startDate)
						count++;
				}

				return count;
			}
		}
	}
}
