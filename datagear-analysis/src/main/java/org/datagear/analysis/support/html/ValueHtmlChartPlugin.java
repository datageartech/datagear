/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;

import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 仅渲染指定内容值的{@linkplain HtmlChartPlugin}。
 * <p>
 * 它从{@linkplain HtmlRenderContext}中获取{@linkplain #getValueAttributeName()}的属性值，并将其作为图表内容渲染。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ValueHtmlChartPlugin<T extends HtmlRenderContext> extends HtmlChartPlugin<T>
{
	private String valueAttributeName;

	public ValueHtmlChartPlugin()
	{
		super();
	}

	public ValueHtmlChartPlugin(String id, String valueAttributeName)
	{
		super(id, new Label("ValueHtmlChartPlugin"), new StringScriptContent(""));
		this.valueAttributeName = valueAttributeName;
	}

	public String getValueAttributeName()
	{
		return valueAttributeName;
	}

	public void setValueAttributeName(String valueAttributeName)
	{
		this.valueAttributeName = valueAttributeName;
	}

	@Override
	protected void writeChartScriptContent(T renderContext, HtmlChart chart) throws IOException
	{
		String value = "";

		Object valueObj = renderContext.getAttribute(this.valueAttributeName);
		if (valueObj == null)
			;
		else if (valueObj instanceof String)
			value = (String) valueObj;
		else
			value = valueObj.toString();

		value = StringUtil.escapeJavaScriptStringValue(value);

		Writer out = renderContext.getWriter();
		String chartVarName = chart.getVarName();

		out.write("(function(chart)");
		writeNewLine(out);
		out.write("{");
		writeNewLine(out);
		out.write("  chart." + SCRIPT_RENDER_FUNCTION_NAME
				+ " = function(){ var element = document.getElementById(this.elementId); element.innerHTML=\"" + value
				+ "\"; };");
		writeNewLine(out);
		out.write("  chart." + SCRIPT_UPDATE_FUNCTION_NAME + " = function(){};");
		writeNewLine(out);
		out.write("})");
		writeNewLine(out);
		out.write("(" + chartVarName + ");");
	}
}
