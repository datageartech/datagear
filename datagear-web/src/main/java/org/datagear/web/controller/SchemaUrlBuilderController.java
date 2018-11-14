/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.IOUtil;
import org.datagear.web.OperationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

/**
 * 模式JDBC连接URL构建器控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/schemaUrlBuilder")
public class SchemaUrlBuilderController extends AbstractController implements ServletContextAware
{
	protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String BUILT_IN_DB_URL_BUILDER_PATH = "/WEB-INF/builtInDbUrlBuilder.js";

	public static final String DB_URL_BUILDER_ENCODING = "UTF-8";

	private ServletContext servletContext;

	private File schemaUrlBuilderScriptFile;

	private volatile String _builtInDbUrlBuilderScript = null;

	public SchemaUrlBuilderController()
	{
		super();
	}

	public SchemaUrlBuilderController(ServletContext servletContext, File schemaUrlBuilderScriptFile)
	{
		super();
		this.servletContext = servletContext;
		this.schemaUrlBuilderScriptFile = schemaUrlBuilderScriptFile;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	public File getSchemaUrlBuilderScriptFile()
	{
		return schemaUrlBuilderScriptFile;
	}

	public void setSchemaUrlBuilderScriptFile(File schemaUrlBuilderScriptFile)
	{
		this.schemaUrlBuilderScriptFile = schemaUrlBuilderScriptFile;
	}

	@Value("${schemaUrlBuilderScriptFile}")
	public void setSchemaUrlBuilderScriptFileString(String schemaUrlBuilderScriptFile)
	{
		this.schemaUrlBuilderScriptFile = IOUtil.getFile(schemaUrlBuilderScriptFile);
	}

	@RequestMapping("/editScriptCode")
	public String editScriptCode(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		request.setAttribute("scriptCode", getUrlBuilderScript());

		return "/schema_url_builder";
	}

	@RequestMapping(value = "/saveScriptCode", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveScriptCode(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "scriptCode", required = false) String scriptCode) throws IOException
	{
		saveCustomScript(scriptCode);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/previewScriptCode")
	public String previewScriptCode(HttpServletRequest request,
			@RequestParam(value = "scriptCode", required = false) String scriptCode) throws IOException
	{
		request.setAttribute("scriptCode", scriptCode);
		request.setAttribute("preview", "1");

		return "/schema/schema_build_url";
	}

	@RequestMapping("/buildUrl")
	public String buildSchemaUrl(HttpServletRequest request, @RequestParam(value = "url", required = false) String url)
			throws IOException
	{
		request.setAttribute("scriptCode", getUrlBuilderScript());
		request.setAttribute("url", url);

		return "/schema/schema_build_url";
	}

	protected void saveCustomScript(String scriptCode) throws IOException
	{
		if (scriptCode == null)
			scriptCode = "";

		scriptCode = scriptCode.trim();

		if (scriptCode.endsWith(","))
			scriptCode = scriptCode.substring(0, scriptCode.length() - 1);

		Writer out = IOUtil.getWriter(this.schemaUrlBuilderScriptFile, DB_URL_BUILDER_ENCODING);

		try
		{
			out.write(scriptCode);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	protected String getUrlBuilderScript() throws IOException
	{
		String script = getCustomUrlBuilderScript();

		if (script == null || script.isEmpty())
			script = getBuiltInUrlBuilderScript();

		return script;
	}

	/**
	 * 获取自定义脚本。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String getCustomUrlBuilderScript() throws IOException
	{
		if (this.schemaUrlBuilderScriptFile == null || !this.schemaUrlBuilderScriptFile.exists())
			return "";

		Reader reader = IOUtil.getReader(this.schemaUrlBuilderScriptFile, DB_URL_BUILDER_ENCODING);

		BufferedReader bufferedReader = null;

		if (reader instanceof BufferedReader)
			bufferedReader = (BufferedReader) reader;
		else
			bufferedReader = new BufferedReader(reader);

		try
		{
			return getUrlBuilderScript(bufferedReader);
		}
		finally
		{
			IOUtil.close(bufferedReader);
		}
	}

	/**
	 * 获取内置脚本。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String getBuiltInUrlBuilderScript() throws IOException
	{
		if (this._builtInDbUrlBuilderScript == null)
		{
			ServletContextResource resource = new ServletContextResource(this.servletContext,
					BUILT_IN_DB_URL_BUILDER_PATH);

			InputStream in = resource.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, DB_URL_BUILDER_ENCODING));

			try
			{
				this._builtInDbUrlBuilderScript = getUrlBuilderScript(bufferedReader);
			}
			finally
			{
				IOUtil.close(bufferedReader);
			}
		}

		return this._builtInDbUrlBuilderScript;
	}

	protected String getUrlBuilderScript(BufferedReader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		String line = null;

		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append(LINE_SEPARATOR);
		}

		return sb.toString().trim();
	}
}
