/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.StringReader;

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
	private HtmlTplDashboardWidgetRenderer<HtmlRenderContext> renderer = new TestHtmlTplDashboardWidgetRenderer<HtmlRenderContext>();

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

	private class TestHtmlTplDashboardWidgetRenderer<T extends HtmlRenderContext> extends HtmlTplDashboardWidgetRenderer<T>
	{
		@Override
		public String simpleTemplateContent(String htmlCharset, String... chartWidgetId)
		{
			return null;
		}

		@Override
		protected void renderHtmlDashboard(T renderContext, HtmlDashboard dashboard) throws Throwable
		{
		}
	}
}
