/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;

/**
 * {@linkplain HtmlChartWidgetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlChartWidgetEntityService extends DataPermissionEntityService<String, HtmlChartWidgetEntity>,
		ChartWidgetSource, CreateUserEntityService, AnalysisProjectAwareEntityService<HtmlChartWidgetEntity>
{
	/**
	 * {@linkplain ChartWidgetSource}上下文。
	 * <p>
	 * {@linkplain ChartWidgetSource#getChartWidget(String)}实现方法可以使用此类处理权限。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	class ChartWidgetSourceContext
	{
		protected static final String NAME_FOR_SERVICE_CONTEXT = ChartWidgetSourceContext.class.getName();
		
		private User user;

		public ChartWidgetSourceContext()
		{
			super();
		}

		public ChartWidgetSourceContext(User user)
		{
			super();
			this.user = user;
		}

		public boolean hasUser()
		{
			return (this.user != null);
		}

		public User getUser()
		{
			return user;
		}

		public void setUser(User user)
		{
			this.user = user;
		}

		/**
		 * 获取当前{@linkplain ChartWidgetSourceContext}线程变量，不会返回{@code null}。
		 * 
		 * @return
		 */
		public static ChartWidgetSourceContext get()
		{
			ChartWidgetSourceContext context = ServiceContext.get().getValue(NAME_FOR_SERVICE_CONTEXT);
			if (context == null)
				context = new ChartWidgetSourceContext();

			return context;
		}

		/**
		 * 设置当前{@linkplain ChartWidgetSourceContext}至线程变量。
		 * 
		 * @param context
		 */
		public static void set(ChartWidgetSourceContext context)
		{
			ServiceContext.get().setValue(NAME_FOR_SERVICE_CONTEXT, context);
		}

		/**
		 * 移除当前{@linkplain ChartWidgetSourceContext}线程变量。
		 */
		public static void remove()
		{
			ServiceContext.get().removeValue(NAME_FOR_SERVICE_CONTEXT);
		}
	}
}
