/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;

import org.datagear.analysis.support.html.HtmlChartPluginJsDefResolver.JsDefContent;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain HtmlChartPluginJsDefResolver}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginJsDefResolverTest
{
	private HtmlChartPluginJsDefResolver resolver = new HtmlChartPluginJsDefResolver();

	@Test
	public void resolveTest() throws IOException
	{
		// 无引号
		{
			String text = "{a: 'a', b : 'b', chartRenderer:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', chartRenderer:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', chartRenderer:{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', chartRenderer:{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{chartRenderer:{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{chartRenderer:{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}

		// 单引号
		{
			String text = "{a: 'a', b : 'b', 'chartRenderer':{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', 'chartRenderer':{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', 'chartRenderer':{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', 'chartRenderer':{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{'chartRenderer':{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{'chartRenderer':{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}

		// 双引号
		{
			String text = "{a: 'a', b : 'b', \"chartRenderer\":{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', \"chartRenderer\":{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', \"chartRenderer\":{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', \"chartRenderer\":{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{\"chartRenderer\":{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{\"chartRenderer\":{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}

		// 带注释
		{
			String text = "{a: 'a', b : 'b', /*sdf*/chartRenderer/*sdf*/:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/chartRenderer/*sdf*/:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', /*sdf*/ chartRenderer /*sdf*/ :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/ chartRenderer /*sdf*/ :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', //sdf\nchartRenderer//sdf\n:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', //sdf\nchartRenderer//sdf\n:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRenderer());
		}

		// 复杂内容

		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}",
					content.getPluginChartRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', chartRenderer: {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', chartRenderer:{}}", content.getPluginJson());
			Assert.assertEquals(
					" {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}",
					content.getPluginChartRenderer());
		}
	}
}
