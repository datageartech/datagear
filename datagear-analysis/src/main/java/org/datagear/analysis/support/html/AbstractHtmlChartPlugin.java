/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.util.i18n.Label;

/**
 * 抽象HTML图表插件。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractHtmlChartPlugin<T extends HtmlRenderContext> extends AbstractChartPlugin<T>
{
	public AbstractHtmlChartPlugin()
	{
		super();
	}

	public AbstractHtmlChartPlugin(String id, Label nameLabel)
	{
		super(id, nameLabel);
	}
}
