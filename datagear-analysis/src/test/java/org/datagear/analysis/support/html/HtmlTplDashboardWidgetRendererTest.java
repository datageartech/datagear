/*
 * Copyright 2018-2024 datagear.tech
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

import java.io.StringReader;

import org.datagear.analysis.RenderException;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlTplDashboardWidgetRenderer}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetRendererTest
{
	private HtmlTplDashboardWidgetRenderer renderer = new TestHtmlTplDashboardWidgetRenderer();

	@Test
	public void resolveCharsetTest() throws Exception
	{
		{
			String html = "<html><head><meta charset=utf-8></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("utf-8", charset);
		}

		{
			String html = "<html><head><meta charset=\"utf-8\"></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("utf-8", charset);
		}

		{
			String html = "<html><head><meta charset='UTF-8'></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("UTF-8", charset);
		}

		{
			String html = "<html><head></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertNull(charset);
		}

		{
			String html = "<html><head><meta charset=''></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("", charset);
		}

		{
			String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("utf-8", charset);
		}

		{
			String html = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("UTF-8", charset);
		}

		{
			String html = "<html><head><meta http-equiv='Content-Type' content=''></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertNull(charset);
		}

		{
			String html = "<html><head><meta http-equiv='Content-Type' content='text/html;charset='></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("", charset);
		}

		{
			String html = "<html><head><!--<meta charset='GBK'></head>--> <!----> <meta charset='UTF-8'></head></html>";
			StringReader in = new StringReader(html);
			String charset = this.renderer.resolveCharset(in);
			Assert.assertEquals("UTF-8", charset);
		}
	}

	private class TestHtmlTplDashboardWidgetRenderer extends HtmlTplDashboardWidgetRenderer
	{
		@Override
		public String simpleTemplateContent(String htmlCharset, String... chartWidgetId)
		{
			return null;
		}

		@Override
		public HtmlTplDashboard render(HtmlTplDashboardWidget dashboardWidget, HtmlTplDashboardRenderContext renderContext)
				throws RenderException
		{
			return null;
		}
	}
}
