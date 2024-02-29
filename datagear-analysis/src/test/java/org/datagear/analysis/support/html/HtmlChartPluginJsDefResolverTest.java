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
			String text = "{a: 'a', b : 'b', renderer:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', renderer:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', renderer:{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', renderer:{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{renderer:{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{renderer:{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 单引号
		{
			String text = "{a: 'a', b : 'b', 'renderer':{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', 'renderer':{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', 'renderer':{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', 'renderer':{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{'renderer':{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{'renderer':{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 双引号
		{
			String text = "{a: 'a', b : 'b', \"renderer\":{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', \"renderer\":{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', \"renderer\":{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', \"renderer\":{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{\"renderer\":{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{\"renderer\":{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 带注释
		{
			String text = "{a: 'a', b : 'b', /*sdf*/renderer/*sdf*/:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/renderer/*sdf*/:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', /*sdf*/ renderer /*sdf*/ :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/ renderer /*sdf*/ :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', //sdf\nrenderer//sdf\n:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', //sdf\nrenderer//sdf\n:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b',  //sdf\n renderer //sdf\n :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n renderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 复杂内容

		{
			String text = "{a: 'a', b : 'b',  //sdf\n renderer //sdf\n :{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n renderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}",
					content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', renderer: {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', renderer:{}}", content.getPluginJson());
			Assert.assertEquals(
					" {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}",
					content.getPluginRenderer());
		}
	}
	
	@Test
	public void resolveTestForOldVersion() throws IOException
	{
		// 无引号
		{
			String text = "{a: 'a', b : 'b', chartRenderer:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', chartRenderer:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', chartRenderer:{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', chartRenderer:{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{chartRenderer:{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{chartRenderer:{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 单引号
		{
			String text = "{a: 'a', b : 'b', 'chartRenderer':{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', 'chartRenderer':{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', 'chartRenderer':{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', 'chartRenderer':{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{'chartRenderer':{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{'chartRenderer':{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 双引号
		{
			String text = "{a: 'a', b : 'b', \"chartRenderer\":{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', \"chartRenderer\":{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', \"chartRenderer\":{render:function() {}}, b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', \"chartRenderer\":{}, b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{\"chartRenderer\":{render:function() {}}, a: 'a', b : 'b'}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{\"chartRenderer\":{}, a: 'a', b : 'b'}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 带注释
		{
			String text = "{a: 'a', b : 'b', /*sdf*/chartRenderer/*sdf*/:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/chartRenderer/*sdf*/:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', /*sdf*/ chartRenderer /*sdf*/ :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', /*sdf*/ chartRenderer /*sdf*/ :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', //sdf\nchartRenderer//sdf\n:{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', //sdf\nchartRenderer//sdf\n:{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{render:function() {}}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() {}}", content.getPluginRenderer());
		}

		// 复杂内容

		{
			String text = "{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b',  //sdf\n chartRenderer //sdf\n :{}}", content.getPluginJson());
			Assert.assertEquals("{render:function() { if(a==b) {}; /*sdf*/  \n\n //sdf\n var a= {};   }}",
					content.getPluginRenderer());
		}
		{
			String text = "{a: 'a', b : 'b', chartRenderer: {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}}";

			JsDefContent content = resolver.resolve(text);

			Assert.assertEquals("{a: 'a', b : 'b', chartRenderer:{}}", content.getPluginJson());
			Assert.assertEquals(
					" {render:function() { var options = { tooltip: {formatter: \"{a} <br/>{b} : {c}%\"} }; }}",
					content.getPluginRenderer());
		}
	}
}
