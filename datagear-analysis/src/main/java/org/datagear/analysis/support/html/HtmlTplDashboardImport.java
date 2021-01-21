/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Serializable;
import java.util.List;

/**
 * HTML模板看板导入项。
 * <p>
 * 这些导入项通常被插入至HTML看板的{@code <head></head>}之间，作为页面依赖资源（JS、CSS等）加载。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlTplDashboardImport
{
	/**
	 * 获取导入项列表。
	 * <p>
	 * {@linkplain ImportItem#getContent()}可以包含{@linkplain HtmlTplDashboardWidgetRenderer#getContextPathPlaceholder()}、
	 * {@linkplain HtmlTplDashboardWidgetRenderer#getVersionPlaceholder()}占位符。
	 * </p>
	 * 
	 * @return 返回{@code null}或空列表，表明无导入项
	 */
	List<ImportItem> getImportItems();

	/**
	 * 导入项。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ImportItem implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 名称 */
		private String name;

		/** 内容 */
		private String content;

		public ImportItem()
		{
			super();
		}

		public ImportItem(String name, String content)
		{
			super();
			this.name = name;
			this.content = content;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", content=" + content + "]";
		}
	}
}
