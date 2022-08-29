/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.util.IOUtil;
import org.datagear.web.util.DriverInfo;
import org.datagear.web.util.DriverInfo.DefaultValue;
import org.datagear.web.util.DriverInfo.UrlTemplate;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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
	public static final String DB_URL_BUILDER_ENCODING = "UTF-8";

	private ServletContext servletContext;

	@Autowired
	private File schemaUrlBuilderScriptFile;

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

	@RequestMapping("/set")
	public String set(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model) throws IOException
	{
		SaveScriptCodeForm form = new SaveScriptCodeForm();
		form.setCode(getUrlBuilderScript());
		
		setFormModel(model, form, "set", "saveSet");

		return "/schemaUrlBuilder/schemaUrlBuilder_set";
	}

	@RequestMapping(value = "/saveSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SaveScriptCodeForm form) throws IOException
	{
		String scriptCode = form.getCode();

		saveCustomScript(scriptCode);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/preview")
	public String previewScriptCode(HttpServletRequest request, org.springframework.ui.Model model,
			@RequestParam(value = "scriptCode", required = false) String scriptCode) throws IOException
	{
		model.addAttribute("scriptCode", scriptCode);
		model.addAttribute("builtInBuildersJson", getBuiltInUrlBuildersJson());
		setFormAction(model, "preview", SUBMIT_ACTION_NONE);
		
		return "/schemaUrlBuilder/schemaUrlBuilder_build";
	}

	@RequestMapping("/build")
	public String buildSchemaUrl(HttpServletRequest request, org.springframework.ui.Model model,
			@RequestParam(value = "url", required = false) String url) throws IOException
	{
		model.addAttribute("scriptCode", getUrlBuilderScript());
		model.addAttribute("builtInBuildersJson", getBuiltInUrlBuildersJson());
		model.addAttribute("url", url);
		setFormAction(model, "build", SUBMIT_ACTION_NONE);
		
		return "/schemaUrlBuilder/schemaUrlBuilder_build";
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

	protected String getBuiltInUrlBuildersJson()
	{
		List<DbTypeUrlTemplate> builtInNameUrlTemplates = getBuiltInDbTypeUrlTemplates();
		return JsonSupport.generate(builtInNameUrlTemplates, "[]");
	}

	protected List<DbTypeUrlTemplate> getBuiltInDbTypeUrlTemplates()
	{
		List<DbTypeUrlTemplate> dbTypeUrlTemplates = new ArrayList<>();

		List<DriverInfo> driverInfos = DriverInfo.getCommonInDriverInfos();
		if (driverInfos != null)
		{
			for (DriverInfo driverInfo : driverInfos)
			{
				UrlTemplate urlTemplate = driverInfo.getUrlTemplate();

				if (isEmpty(driverInfo.getName()) || isEmpty(urlTemplate) || isEmpty(urlTemplate.getTemplate()))
					continue;

				DbTypeUrlTemplate nt = new DbTypeUrlTemplate(driverInfo.getName(), urlTemplate.getTemplate());

				if (urlTemplate.getDefaultValue() != null)
					nt.setDefaultValue(new DefaultValue(urlTemplate.getDefaultValue()));

				dbTypeUrlTemplates.add(nt);
			}
		}

		return dbTypeUrlTemplates;
	}

	public static class DbTypeUrlTemplate extends UrlTemplate
	{
		private static final long serialVersionUID = 1L;

		private String dbType = "";

		public DbTypeUrlTemplate()
		{
			super();
		}

		public DbTypeUrlTemplate(String dbType, String template)
		{
			super(template);
			this.dbType = dbType;
		}

		public String getDbType()
		{
			return dbType;
		}

		public void setDbType(String dbType)
		{
			this.dbType = dbType;
		}
	}

	public static class SaveScriptCodeForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String code;

		public SaveScriptCodeForm()
		{
			super();
		}

		public String getCode()
		{
			return code;
		}

		public void setCode(String code)
		{
			this.code = code;
		}
	}
}
