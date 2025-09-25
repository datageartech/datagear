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

package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartDefinition;
import org.datagear.util.i18n.Label;

/**
 * 专门用于渲染异常消息的{@linkplain AttributeValueHtmlChartPlugin}。
 * 
 * @author datagear@163.com
 *
 */
public class ExceptionMsgHtmlChartPlugin extends AttributeValueHtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	public static final ExceptionMsgHtmlChartPlugin INSTANCE = new ExceptionMsgHtmlChartPlugin();

	/**
	 * 默认插件ID。
	 */
	public static final String DEFAULT_ID = "org.datagear.exceptionMsg";

	/**
	 * 存储在{@linkplain ChartDefinition#getAttrValues()}中的异常消息属性名。
	 */
	public static final String DEFAULT_ATTR_NAME = ChartDefinition.BUILTIN_ATTR_PREFIX + "EXCEPTION_MESSAGE";

	public ExceptionMsgHtmlChartPlugin()
	{
		super(DEFAULT_ID, new Label(ExceptionMsgHtmlChartPlugin.class.getSimpleName()), DEFAULT_ATTR_NAME);
	}
}
