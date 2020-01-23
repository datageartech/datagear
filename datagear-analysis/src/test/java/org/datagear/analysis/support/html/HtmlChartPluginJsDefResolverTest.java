/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
			String text = "{a: 'a', b : 'b', chartRender:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', chartRender:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', chartRender:{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', chartRender:{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{chartRender:{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{chartRender:{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}

		// 单引号
		{
			String text = "{a: 'a', b : 'b', 'chartRender':{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', 'chartRender':{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', 'chartRender':{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', 'chartRender':{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{'chartRender':{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{'chartRender':{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}

		// 双引号
		{
			String text = "{a: 'a', b : 'b', \"chartRender\":{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', \"chartRender\":{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', \"chartRender\":{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', \"chartRender\":{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{\"chartRender\":{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{\"chartRender\":{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}

		// 带注释
		{
			String text = "{a: 'a', b : 'b', /*sdf*/chartRender/*sdf*/:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/chartRender/*sdf*/:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', b : 'b', /*sdf*/ chartRender /*sdf*/ :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/ chartRender /*sdf*/ :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', b : 'b', //sdf\nchartRender//sdf\n:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', //sdf\nchartRender//sdf\n:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}
		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRender //sdf\n :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRender //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginChartRender());
		}

		// 复杂内容

		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRender //sdf\n :{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRender //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}",
					content.getPluginChartRender());
		}
	}
}
