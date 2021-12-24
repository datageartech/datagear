/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlTplDashboard}渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardRenderAttr extends HtmlRenderAttr
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@linkplain HtmlTplDashboardRenderAttr}的渲染上下文属性名。
	 */
	public static final String ATTR_NAME = HtmlTplDashboardRenderAttr.class.getName();

	/** 属性名：导入项列表 */
	private String importListName = "importList";

	/** 属性名：Web上下文 */
	private String webContextName = "webContext";

	/** 属性名：看板主题 */
	private String dashboardThemeName = "dashboardTheme";

	/** 属性名：HTML标题处理器 */
	private String htmlTitleHandlerName = "htmlTitleHandler";

	public HtmlTplDashboardRenderAttr()
	{
		super();
	}

	public String getImportListName()
	{
		return importListName;
	}

	public void setImportListName(String importListName)
	{
		this.importListName = importListName;
	}

	public String getWebContextName()
	{
		return webContextName;
	}

	public void setWebContextName(String webContextName)
	{
		this.webContextName = webContextName;
	}

	public String getDashboardThemeName()
	{
		return dashboardThemeName;
	}

	public void setDashboardThemeName(String dashboardThemeName)
	{
		this.dashboardThemeName = dashboardThemeName;
	}

	public String getHtmlTitleHandlerName()
	{
		return htmlTitleHandlerName;
	}

	public void setHtmlTitleHandlerName(String htmlTitleHandlerName)
	{
		this.htmlTitleHandlerName = htmlTitleHandlerName;
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardImport}列表。
	 * 
	 * @param renderContext
	 * @return
	 */
	public List<HtmlTplDashboardImport> getImportListNonNull(RenderContext renderContext)
	{
		List<HtmlTplDashboardImport> importList = getImportList(renderContext);

		if (importList == null)
			throw new RenderException("The [" + this.importListName + "] attribute must be set");

		return importList;
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardImport}列表，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public List<HtmlTplDashboardImport> getImportList(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.importListName);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardImport}列表。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public void setImportList(RenderContext renderContext, List<HtmlTplDashboardImport> webContext)
	{
		renderContext.setAttribute(this.importListName, webContext);
	}

	/**
	 * 移除{@linkplain HtmlTplDashboardImport}列表。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public List<HtmlTplDashboardImport> removeImportList(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.importListName);
	}

	/**
	 * 获取{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public WebContext getWebContextNonNull(RenderContext renderContext)
	{
		WebContext webContext = getWebContext(renderContext);

		if (webContext == null)
			throw new RenderException("The [" + this.webContextName + "] attribute must be set");

		return webContext;
	}

	/**
	 * 获取{@linkplain WebContext}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public WebContext getWebContext(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.webContextName);
	}

	/**
	 * 设置{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @param renderStyle
	 */
	public void setWebContext(RenderContext renderContext, WebContext webContext)
	{
		renderContext.setAttribute(this.webContextName, webContext);
	}

	/**
	 * 移除{@linkplain WebContext}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public WebContext removeWebContext(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.webContextName);
	}

	/**
	 * 获取{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public DashboardTheme getDashboardThemeNonNull(RenderContext renderContext)
	{
		DashboardTheme dashboardTheme = renderContext.getAttribute(this.dashboardThemeName);

		if (dashboardTheme == null)
			throw new RenderException("The [" + this.dashboardThemeName + "] attribute must be set");

		return dashboardTheme;
	}

	/**
	 * 获取{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public DashboardTheme getDashboardTheme(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.dashboardThemeName);
	}

	/**
	 * 设置{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @param dashboardTheme
	 */
	public void setDashboardTheme(RenderContext renderContext, DashboardTheme dashboardTheme)
	{
		renderContext.setAttribute(this.dashboardThemeName, dashboardTheme);
	}

	/**
	 * 移除{@linkplain DashboardTheme}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public DashboardTheme removeDashboardTheme(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.dashboardThemeName);
	}

	/**
	 * 获取{@linkplain HtmlTitleHandler}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public HtmlTitleHandler getHtmlTitleHandler(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.htmlTitleHandlerName);
	}

	/**
	 * 设置{@linkplain HtmlTitleHandler}。
	 * 
	 * @param renderContext
	 * @param htmlTitleHandler
	 */
	public void setHtmlTitleHandler(RenderContext renderContext, HtmlTitleHandler htmlTitleHandler)
	{
		renderContext.setAttribute(this.htmlTitleHandlerName, htmlTitleHandler);
	}

	/**
	 * 移除{@linkplain HtmlTitleHandler}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public HtmlTitleHandler removeHtmlTitleHandler(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.htmlTitleHandlerName);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardWidget#render(RenderContext)}必须的上下文属性值。
	 * 
	 * @param renderContext
	 * @param htmlWriter
	 * @param importList
	 * @param webContext
	 * @param dashboardTheme
	 */
	public void inflate(RenderContext renderContext, Writer htmlWriter, List<HtmlTplDashboardImport> importList,
			WebContext webContext, DashboardTheme dashboardTheme)
	{
		HtmlTplDashboardRenderAttr.set(renderContext, this);
		setImportList(renderContext, importList);
		setHtmlWriter(renderContext, htmlWriter);
		setWebContext(renderContext, webContext);
		setDashboardTheme(renderContext, dashboardTheme);
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr getNonNull(RenderContext renderContext)
	{
		HtmlTplDashboardRenderAttr renderAttr = get(renderContext);

		if (renderAttr == null)
			throw new RenderException("The [" + ATTR_NAME + "] attribute must be set");

		return renderAttr;
	}

	/**
	 * 获取{@linkplain HtmlTplDashboardRenderAttr}对象，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr get(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_NAME);
	}

	/**
	 * 设置{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 */
	public static void set(RenderContext renderContext, HtmlTplDashboardRenderAttr renderAttr)
	{
		renderContext.setAttribute(ATTR_NAME, renderAttr);
	}

	/**
	 * 移除{@linkplain HtmlTplDashboardRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlTplDashboardRenderAttr remove(RenderContext renderContext)
	{
		return renderContext.removeAttribute(ATTR_NAME);
	}

	/**
	 * Web上下文信息。
	 * <p>
	 * 这些信息将输出至客户端，提供看板交互支持。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class WebContext
	{
		/** 上下文路径 */
		private String contextPath;

		/** Web属性集 */
		private Map<String, ?> attributes = new HashMap<String, Object>();

		public WebContext()
		{
			super();
		}

		public WebContext(String contextPath)
		{
			super();
			this.contextPath = contextPath;
		}

		public String getContextPath()
		{
			return contextPath;
		}

		public void setContextPath(String contextPath)
		{
			this.contextPath = contextPath;
		}

		public Map<String, ?> getAttributes()
		{
			return attributes;
		}

		public void setAttributes(Map<String, ?> attributes)
		{
			this.attributes = attributes;
		}

		@SuppressWarnings("unchecked")
		public void addAttribute(String name, Object value)
		{
			((Map<String, Object>) this.attributes).put(name, value);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [contextPath=" + contextPath + ", attributes=" + attributes + "]";
		}
	}

	/**
	 * HTML的<code>&lt;title&gt;&lt;/title&gt;</code>处理器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface HtmlTitleHandler
	{
		/**
		 * 返回要追加的标题内容。
		 * 
		 * @param title
		 * @return
		 */
		String suffix(String title);
	}

	/**
	 * 默认{@linkplain HtmlTitleHandler}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DefaultHtmlTitleHandler implements HtmlTitleHandler
	{
		private String suffix = "";

		private String suffixForBlank = "";

		public DefaultHtmlTitleHandler()
		{
			super();
		}

		public DefaultHtmlTitleHandler(String suffix)
		{
			super();
			this.suffix = suffix;
		}

		public DefaultHtmlTitleHandler(String suffix, String suffixForBlank)
		{
			super();
			this.suffix = suffix;
			this.suffixForBlank = suffixForBlank;
		}

		public String getSuffix()
		{
			return suffix;
		}

		public void setSuffix(String suffix)
		{
			this.suffix = suffix;
		}

		public String getSuffixForBlank()
		{
			return suffixForBlank;
		}

		public void setSuffixForBlank(String suffixForBlank)
		{
			this.suffixForBlank = suffixForBlank;
		}

		@Override
		public String suffix(String title)
		{
			String suffix = this.suffix;

			if (StringUtil.isBlank(title) && !StringUtil.isEmpty(this.suffixForBlank))
				suffix = this.suffixForBlank;

			return suffix;
		}
	}
}
