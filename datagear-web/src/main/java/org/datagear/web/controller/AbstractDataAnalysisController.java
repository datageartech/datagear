/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.RenderStyle;
import org.datagear.util.StringUtil;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.context.MessageSource;

/**
 * 抽象数据分析控制器。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractDataAnalysisController extends AbstractController
{
	public AbstractDataAnalysisController()
	{
		super();
	}

	public AbstractDataAnalysisController(MessageSource messageSource, ClassDataConverter classDataConverter)
	{
		super(messageSource, classDataConverter);
	}

	protected RenderStyle resolveRenderStyle(HttpServletRequest request)
	{
		String theme = WebUtils.getTheme(request);

		if (StringUtil.isEmpty(theme))
			return RenderStyle.LIGHT;

		theme = theme.toLowerCase();

		if (theme.indexOf("dark") > -1)
			return RenderStyle.DARK;

		return RenderStyle.LIGHT;
	}
}
