/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.fmk;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.core.CommonTemplateMarkupOutputModel;

/**
 * JSON输出模型。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateJsonOutputModel extends CommonTemplateMarkupOutputModel<TemplateJsonOutputModel>
{
	public TemplateJsonOutputModel(String plainTextContent, String markupContent)
	{
		super(plainTextContent, markupContent);
	}

	@Override
	public CommonMarkupOutputFormat<TemplateJsonOutputModel> getOutputFormat()
	{
		return JsonOutputFormat.INSTANCE;
	}
}
