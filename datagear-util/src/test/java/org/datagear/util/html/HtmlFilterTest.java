/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@linkplain HtmlFilter}单元测试用例。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlFilterTest
{
	private HtmlFilter htmlFilter = new HtmlFilter();

	@Test
	public void filterTest_Reader_FilterHandler() throws IOException
	{
		{
			String html = "<html lang='zh'>"
					+ "\n"
					+ "<head>"
					+ "\n"
					+ "<title>title</title>"
					+ "\n"
					+ "<link href=\"...\">"
					+ "\n"
					+ "<script type='text/javascript' href=\"v\"></script>"
					+ "\n"
					+ "<script type='text/javascript'>"
					+ "\n"
					+ "\n"
					+ "  // line-comment <div></div> <script> </script>"
					+ "\n"
					+ "  var str='<div></div> </script>'; "
					+ "  /* block comment "
					+ "  <div></div> <script> </script>"
					+ "\n"
					+ "  <div></div> <script> </script>"
					+ "  */"
					+ "\n"
					+ "</script>"
					+ "\n"
					+ "<style type=\"text/css\">"
					+ "\n"
					+ ".a{ color: 'red' }"
					+ "\n"
					+ "// line- comment <div></div> <style> </style>"
					+ "\n"
					+ "  /* block comment "
					+ "  <div></div> <style> </style>"
					+ "\n"
					+ "  <div></div> <style> </style>"
					+ "  */"
					+ "\n"
					+ "</style>"
					+ "\n"
					+ "</head>"
					+ "\n"
					+ "<body p0 p1 = v1 p2='v2' p3=\"v3\" p4 p5 = v5 >"
					+ "\n"
					+ "<!---->"
					+ "\n"
					+ "<!--comment-->"
					+ "\n"
					+ "<!-- comment -->"
					+ "\n"
					+ "<!--"
					+ "\n"
					+ " comment "
					+ "\n"
					+ " <div>comment</div>"
					+ " -->"
					+ "\n"
					+ "<input name='name' value=\"value\">"
					+ "\n"
					+ "<input name='name' value=\"value\"/>"
					+ "\n"
					+ "<input name='name' value=\"value\" />"
					+ "\n"
					+ "<div p0=v0 p1 p2 = v2>sdf</div>"
					+ "\n"
					+ "<script/>"
					+ "\n"
					+ "<script p0=v0/>"
					+ "\n"
					+ "<script />"
					+ "\n"
					+ "<script p0=v0 />"
					+ "\n"
					+ "<>"
					+ "\n"
					+ "</>"
					+ "\n"
					+ "< />"
					+ "\n"
					+ "</ >"
					+ "\n"
					+ "</body>"
					+ "\n"
					+ "</html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filter(in, newTestFilterHandler(out));

			String expected = "[bts]<html lang='zh'[bte]>[ate]"
					+ "\n"
					+ "[bts]<head[bte]>[ate]"
					+ "\n"
					+ "[bts]<title[bte]>[ate]title[bts]</title[bte]>[ate]"
					+ "\n"
					+ "[bts]<link href=\"...\"[bte]>[ate]"
					+ "\n"
					+ "[bts]<script type='text/javascript' href=\"v\"[bte]>[ate][bts]</script[bte]>[ate]"
					+ "\n"
					+ "[bts]<script type='text/javascript'[bte]>[ate]"
					+ "\n"
					+ "\n"
					+ "  // line-comment <div></div> <script> </script>"
					+ "\n"
					+ "  var str='<div></div> </script>'; "
					+ "  /* block comment "
					+ "  <div></div> <script> </script>"
					+ "\n"
					+ "  <div></div> <script> </script>"
					+ "  */"
					+ "\n"
					+ "[bts]</script[bte]>[ate]"
					+ "\n"
					+ "[bts]<style type=\"text/css\"[bte]>[ate]"
					+ "\n"
					+ ".a{ color: 'red' }"
					+ "\n"
					+ "// line- comment <div></div> <style> </style>"
					+ "\n"
					+ "  /* block comment "
					+ "  <div></div> <style> </style>"
					+ "\n"
					+ "  <div></div> <style> </style>"
					+ "  */"
					+ "\n"
					+ "[bts]</style[bte]>[ate]"
					+ "\n"
					+ "[bts]</head[bte]>[ate]"
					+ "\n"
					+ "[bts]<body p0 p1 = v1 p2='v2' p3=\"v3\" p4 p5 = v5 [bte]>[ate]"
					+ "\n"
					+ "<!---->"
					+ "\n"
					+ "<!--comment-->"
					+ "\n"
					+ "<!-- comment -->"
					+ "\n"
					+ "<!--"
					+ "\n"
					+ " comment "
					+ "\n"
					+ " <div>comment</div>"
					+ " -->"
					+ "\n"
					+ "[bts]<input name='name' value=\"value\"[bte]>[ate]"
					+ "\n"
					+ "[bts]<input name='name' value=\"value\"[bte]/>[ate]"
					+ "\n"
					+ "[bts]<input name='name' value=\"value\" [bte]/>[ate]"
					+ "\n"
					+ "[bts]<div p0=v0 p1 p2 = v2[bte]>[ate]sdf[bts]</div[bte]>[ate]"
					+ "\n"
					+ "[bts]<script[bte]/>[ate]"
					+ "\n"
					+ "[bts]<script p0=v0[bte]/>[ate]"
					+ "\n"
					+ "[bts]<script [bte]/>[ate]"
					+ "\n"
					+ "[bts]<script p0=v0 [bte]/>[ate]"
					+ "\n"
					+ "[bts]<[bte]>[ate]"
					+ "\n"
					+ "[bts]</[bte]>[ate]"
					+ "\n"
					+ "[bts]< [bte]/>[ate]"
					+ "\n"
					+ "[bts]</ [bte]>[ate]"
					+ "\n"
					+ "[bts]</body[bte]>[ate]"
					+ "\n"
					+ "[bts]</html[bte]>[ate]";

			assertEquals(expected, out.toString());
		}
	}

	@Test
	public void filterTest_Reader_CharsetFilterHandler() throws IOException
	{
		{
			String html = "<html><head><meta charset='UTF-8'></head><body></body></html>";
			CharsetFilterHandler handler = new CharsetFilterHandler();
			StringReader in = new StringReader(html);
			htmlFilter.filter(in, handler);

			assertEquals("UTF-8", handler.getCharset());
		}
	}

	@Test
	public void filterTest_Reader_TestBeforeAfterWriteFilterHandler() throws IOException
	{
		{
			String html = "<html><head><></head><body></body></html>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			TestBeforeAfterWriteFilterHandler handler = new TestBeforeAfterWriteFilterHandler(out);
			htmlFilter.filter(in, handler);

			assertEquals("[b]<html><head><></head><body></body></html>[a]", out.toString());
		}
	}

	@Test
	public void filterTest_aborted() throws IOException
	{
		{
			String html = "<html><head><meta charset=\"UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler handler = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, handler);

			assertEquals("<html><head><meta charset=\"UTF-8\">", out.toString());
			assertEquals("UTF-8", handler.getCharset());
		}
		{
			String html = "<html><head><meta content=\"text/html; charset=UTF-8\"></head><body></body></html>";

			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			CharsetFilterHandler handler = new CharsetFilterHandler(out, true);
			htmlFilter.filter(in, handler);

			assertEquals("<html><head><meta content=\"text/html; charset=UTF-8\">", out.toString());
			assertEquals("UTF-8", handler.getCharset());
		}
	}

	@Test
	public void readTagNameTest() throws IOException
	{
		{
			StringReader in = new StringReader("div ");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("div", tagName.toString());
			assertEquals(" ", nameAfter);
		}

		{
			StringReader in = new StringReader("div>");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("div", tagName.toString());
			assertEquals(">", nameAfter);
		}

		{
			StringReader in = new StringReader("div");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("div", tagName.toString());
			assertNull(nameAfter);
		}

		{
			StringReader in = new StringReader("div/>");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("div", tagName.toString());
			assertEquals("/>", nameAfter);
		}

		{
			StringReader in = new StringReader("/div ");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("/div", tagName.toString());
			assertEquals(" ", nameAfter);
		}

		{
			StringReader in = new StringReader(">");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("", tagName.toString());
			assertEquals(">", nameAfter);
		}

		{
			StringReader in = new StringReader(" ");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("", tagName.toString());
			assertEquals(" ", nameAfter);
		}

		{
			StringReader in = new StringReader("/>");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("/", tagName.toString());
			assertEquals(">", nameAfter);
		}

		{
			StringReader in = new StringReader(" />");
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			assertEquals("", tagName.toString());
			assertEquals(" ", nameAfter);
		}
	}

	@Test
	public void filterAfterHtmlCommentTest() throws IOException
	{
		{
			String html = "!---->";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			htmlFilter.filterAfterHtmlComment(in, newFilterContext(out), tagName.toString(), nameAfter);
			assertEquals("<!---->", out.toString());
		}

		{
			String html = "!-- comment -->";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			htmlFilter.filterAfterHtmlComment(in, newFilterContext(out), tagName.toString(), nameAfter);
			assertEquals("<!-- comment -->", out.toString());
		}

		{
			String html = "!-- <div></div> -->";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			htmlFilter.filterAfterHtmlComment(in, newFilterContext(out), tagName.toString(), nameAfter);
			assertEquals("<!-- <div></div> -->", out.toString());
		}

		{
			String html = "!----><after>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			htmlFilter.filterAfterHtmlComment(in, newFilterContext(out), tagName.toString(), nameAfter);
			assertEquals("<!---->", out.toString());
		}

		{
			String html = "!----<after>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			StringBuilder tagName = new StringBuilder();
			String nameAfter = htmlFilter.readTagName(in, tagName);

			htmlFilter.filterAfterHtmlComment(in, newFilterContext(out), tagName.toString(), nameAfter);
			assertEquals("<!----<after>", out.toString());
		}
	}

	@Test
	public void filterAfterTagTest() throws IOException
	{
		{
			{
				String html = "div";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newFilterContext(out), tagName.toString(), nameAfter);
				assertEquals("<div", out.toString());
			}

			{
				String html = "div>after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newFilterContext(out), tagName.toString(), nameAfter);
				assertEquals("<div>", out.toString());
			}

			{
				String html = "div/>after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newFilterContext(out), tagName.toString(), nameAfter);
				assertEquals("<div/>", out.toString());
			}

			{
				String html = "div p0=v0 >after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newFilterContext(out), tagName.toString(), nameAfter);
				assertEquals("<div p0=v0 >", out.toString());
			}
		}

		{
			{
				String html = "div";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newTestFilterHandler(out), tagName.toString(), nameAfter);
				assertEquals("[bts]<div[bte]", out.toString());
			}

			{
				String html = "div>after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newTestFilterHandler(out), tagName.toString(), nameAfter);
				assertEquals("[bts]<div[bte]>[ate]", out.toString());
			}

			{
				String html = "div/>after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newTestFilterHandler(out), tagName.toString(), nameAfter);
				assertEquals("[bts]<div[bte]/>[ate]", out.toString());
			}

			{
				String html = "div p0=v0 >after";
				StringReader in = new StringReader(html);
				StringWriter out = new StringWriter();
				StringBuilder tagName = new StringBuilder();
				String nameAfter = htmlFilter.readTagName(in, tagName);

				htmlFilter.filterAfterTag(in, newTestFilterHandler(out), tagName.toString(), nameAfter);
				assertEquals("[bts]<div p0=v0 [bte]>[ate]", out.toString());
			}
		}
	}

	@Test
	public void filterUntilTagEndTest() throws IOException
	{
		{
			String html = ">";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals("", out.toString());
			assertEquals(">", tagEnd);
		}
		{
			String html = "p0='v0' p1='v1>' p2=\"p2>\">";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals("p0='v0' p1='v1>' p2=\"p2>\"", out.toString());
			assertEquals(">", tagEnd);
		}
		{
			String html = "p0='>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals("p0='>", out.toString());
			assertNull(tagEnd);
		}
		{
			String html = "p0=\">";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals("p0=\">", out.toString());
			assertNull(tagEnd);
		}
		{
			String html = "/>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals("", out.toString());
			assertEquals("/>", tagEnd);
		}
		{
			String html = " />";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "");

			assertEquals(" ", out.toString());
			assertEquals("/>", tagEnd);
		}
	}

	@Test
	public void filterUntilTagEndTest_tagAttrs() throws IOException
	{
		{
			String html = ">";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals("", out.toString());
			assertEquals(">", tagEnd);
			assertTrue(tagAttrs.isEmpty());
		}
		{
			String html = "p0=v0 p1='v1>' p2=\"v2>\" p3 p4 = v4 p5 = 'v5' >";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals("p0=v0 p1='v1>' p2=\"v2>\" p3 p4 = v4 p5 = 'v5' ", out.toString());
			assertEquals(">", tagEnd);
			assertEquals(6, tagAttrs.size());
			assertEquals("v0", tagAttrs.get("p0"));
			assertEquals("v1>", tagAttrs.get("p1"));
			assertEquals("v2>", tagAttrs.get("p2"));
			assertNull(tagAttrs.get("p3"));
			assertEquals("v4", tagAttrs.get("p4"));
			assertEquals("v5", tagAttrs.get("p5"));
		}
		{
			String html = "p0='>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals("p0='>", out.toString());
			assertNull(tagEnd);
			assertEquals(1, tagAttrs.size());
			assertEquals("'>", tagAttrs.get("p0"));
		}
		{
			String html = "p0=\">";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals("p0=\">", out.toString());
			assertNull(tagEnd);
			assertEquals(1, tagAttrs.size());
			assertEquals("\">", tagAttrs.get("p0"));
		}
		{
			String html = "/>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals("", out.toString());
			assertEquals("/>", tagEnd);
			assertTrue(tagAttrs.isEmpty());
		}
		{
			String html = " />";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();
			Map<String, String> tagAttrs = new HashMap<String, String>();
			String tagEnd = htmlFilter.filterUntilTagEnd(in, newFilterContext(out), "", tagAttrs);

			assertEquals(" ", out.toString());
			assertEquals("/>", tagEnd);
			assertTrue(tagAttrs.isEmpty());
		}
	}

	@Test
	public void filterAfterScriptCloseTagTest() throws IOException
	{
		{
			String html = "</script>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("</script>", out.toString());
		}
		{
			String html = "</script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("</script>", out.toString());
		}
		{
			String html = "'</script>' </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("'</script>' </script>", out.toString());
		}
		{
			String html = "\"</script>\" </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("\"</script>\" </script>", out.toString());
		}
		{
			String html = "`</script>` </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("`</script>` </script>", out.toString());
		}
		{
			String html = "//comment </script> \n </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("//comment </script> \n </script>", out.toString());
		}
		{
			String html = "/*comment </script> \n </script> \n comment*/ </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("/*comment </script> \n </script> \n comment*/ </script>", out.toString());
		}
		{
			String html = "<div> <span></span> </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newFilterContext(out));
			assertEquals("<div> <span></span> </script>", out.toString());
		}
		{
			String html = "<div> <span></span> </script>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterScriptCloseTag(in, newTestFilterHandler(out));
			assertEquals("<div> <span></span> [bts]</script[bte]>[ate]", out.toString());
		}
	}

	@Test
	public void filterAfterStyleCloseTagTest() throws IOException
	{
		{
			String html = "</style>";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("</style>", out.toString());
		}
		{
			String html = "</style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("</style>", out.toString());
		}
		{
			String html = "'</style>' </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("'</style>' </style>", out.toString());
		}
		{
			String html = "\"</style>\" </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("\"</style>\" </style>", out.toString());
		}
		{
			String html = "//comment </style> \n </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("//comment </style> \n </style>", out.toString());
		}
		{
			String html = "/*comment </style> \n </style> \n comment*/ </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("/*comment </style> \n </style> \n comment*/ </style>", out.toString());
		}
		{
			String html = "<div> <span></span> </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newFilterContext(out));
			assertEquals("<div> <span></span> </style>", out.toString());
		}
		{
			String html = "<div> <span></span> </style>after";
			StringReader in = new StringReader(html);
			StringWriter out = new StringWriter();

			htmlFilter.filterAfterStyleCloseTag(in, newTestFilterHandler(out));
			assertEquals("<div> <span></span> [bts]</style[bte]>[ate]", out.toString());
		}
	}

	protected FilterHandler newFilterContext(Writer out)
	{
		return new DefaultFilterHandler(out);
	}

	protected FilterHandler newTestFilterHandler(Writer out)
	{
		return new TestFilterHandler(out);
	}
	
	protected static class TestFilterHandler extends DefaultFilterHandler
	{
		public TestFilterHandler(Writer out)
		{
			super(out);
		}

		@Override
		public void beforeWriteTagStart(Reader in, String tagName) throws IOException
		{
			write("[bts]");
		}

		@Override
		public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd,
				Map<String, String> attrs) throws IOException
		{
			write("[bte]");
		}

		@Override
		public void afterWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs) throws IOException
		{
			write("[ate]");
		}	
	};

	protected static class TestBeforeAfterWriteFilterHandler extends DefaultFilterHandler
	{
		public TestBeforeAfterWriteFilterHandler(Writer out)
		{
			super(out);
		}

		@Override
		public void beforeWrite(Reader in) throws IOException
		{
			write("[b]");
		}

		@Override
		public void afterWrite(Reader in) throws IOException
		{
			write("[a]");
		}
	};
}
