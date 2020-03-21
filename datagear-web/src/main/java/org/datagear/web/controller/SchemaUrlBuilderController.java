/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.web.OperationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

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
	public static final String BUILT_IN_DB_URL_BUILDER_CLASS_PATH = "org/datagear/web/builtInDbUrlBuilder.js";

	public static final String DB_URL_BUILDER_ENCODING = "UTF-8";

	private ServletContext servletContext;

	private File schemaUrlBuilderScriptFile;

	private volatile String _builtInDbUrlBuilderScript = null;

	public SchemaUrlBuilderController()
	{
		super();
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
		this.schemaUrlBuilderScriptFile = FileUtil.getFile(schemaUrlBuilderScriptFile);
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
		request.setAttribute("preview", true);

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

		return IOUtil.readString(reader, true);
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
			InputStream in = getClass().getClassLoader().getResourceAsStream(BUILT_IN_DB_URL_BUILDER_CLASS_PATH);
			this._builtInDbUrlBuilderScript = IOUtil.readString(in, DB_URL_BUILDER_ENCODING, true);
		}

		return this._builtInDbUrlBuilderScript;
	}
}
