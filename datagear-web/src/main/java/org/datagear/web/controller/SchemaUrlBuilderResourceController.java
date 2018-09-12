/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

/**
 * 模式JDBC连接URL构建器JS资源控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/schemaUrlBuilder")
public class SchemaUrlBuilderResourceController extends AbstractSchemaModelController
{
	@Autowired
	@Qualifier("schemaUrlBuilderResourceDirectory")
	private File schemaUrlBuilderResourceDirectory;

	public SchemaUrlBuilderResourceController()
	{
		super();
	}

	public SchemaUrlBuilderResourceController(File schemaUrlBuilderResourceDirectory)
	{
		super();
		this.schemaUrlBuilderResourceDirectory = schemaUrlBuilderResourceDirectory;
	}

	public File getSchemaUrlBuilderResourceDirectory()
	{
		return schemaUrlBuilderResourceDirectory;
	}

	public void setSchemaUrlBuilderResourceDirectory(File schemaUrlBuilderResourceDirectory)
	{
		this.schemaUrlBuilderResourceDirectory = schemaUrlBuilderResourceDirectory;
	}

	@RequestMapping("buildUrl")
	public String buildSchemaUrl(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel)
	{
		String[] jsFileNames = listJsResourceFileNamesNoSuffix();

		springModel.addAttribute("schemaUrlBuilderResources", jsFileNames);

		return "/schema/schema_build_url";
	}

	@RequestMapping("script/{name}")
	public void getScript(WebRequest request, HttpServletResponse response, @PathVariable("name") String scriptFileName)
			throws IOException
	{
		File file = new File(this.schemaUrlBuilderResourceDirectory, scriptFileName + ".js");

		if (!file.exists())
			file = new File(this.schemaUrlBuilderResourceDirectory, scriptFileName + ".JS");

		if (!file.exists())
			file = new File(this.schemaUrlBuilderResourceDirectory, scriptFileName + ".jS");

		if (!file.exists())
			file = new File(this.schemaUrlBuilderResourceDirectory, scriptFileName + ".Js");

		if (!file.exists())
			return;

		if (!isJsFile(file))
			return;

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/javascript;charset=UTF-8");

		Writer out = response.getWriter();

		String line = null;

		try
		{
			while ((line = reader.readLine()) != null)
			{
				out.write(line);
				out.write("\r\n");
			}
		}
		finally
		{
			reader.close();
		}
	}

	protected String[] listJsResourceFileNamesNoSuffix()
	{
		File[] files = this.schemaUrlBuilderResourceDirectory.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				if (pathname.isDirectory())
					return false;

				return isJsFile(pathname);
			}
		});

		String[] names = new String[files.length];

		for (int i = 0; i < files.length; i++)
		{
			String name = files[i].getName();
			name = name.substring(0, name.lastIndexOf('.'));

			names[i] = name;
		}

		return names;
	}

	protected boolean isJsFile(File file)
	{
		return file.getName().toLowerCase().endsWith(".js");
	}
}
