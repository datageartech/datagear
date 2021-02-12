/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.config.CoreConfig;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 看板全局资源控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dashboardGlobalRes")
public class DashboardGlobalResController extends AbstractController
{
	@Autowired
	@Qualifier(CoreConfig.NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	private File dashboardGlobalResRootDirectory;

	@Autowired
	private File tempDirectory;

	public DashboardGlobalResController()
	{
		super();
	}

	public File getDashboardGlobalResRootDirectory()
	{
		return dashboardGlobalResRootDirectory;
	}

	public void setDashboardGlobalResRootDirectory(File dashboardGlobalResRootDirectory)
	{
		this.dashboardGlobalResRootDirectory = dashboardGlobalResRootDirectory;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboardGlobalRes.addDashboardGlobalRes");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/dashboardGlobalRes/dashboardGlobalRes_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DashboardGlobalResAddForm form) throws Exception
	{
		if (isEmpty(form.getFilePath()))
			throw new IllegalInputException();

		File file = FileUtil.getFile(this.tempDirectory, form.getFilePath());
		String savePath = form.getSavePath();

		if (form.isAutoUnzip() && FileUtil.isExtension(file, "zip"))
		{
			File parent = this.dashboardGlobalResRootDirectory;
			if (!StringUtil.isEmpty(savePath))
				parent = FileUtil.getDirectory(this.dashboardGlobalResRootDirectory, savePath);

			ZipInputStream in = null;

			try
			{
				in = IOUtil.getZipInputStream(file);
				IOUtil.unzip(in, parent);
			}
			finally
			{
				IOUtil.close(in);
			}
		}
		else
		{
			if (isEmpty(form.getSavePath()))
				throw new IllegalInputException();

			File resFile = FileUtil.getFile(this.dashboardGlobalResRootDirectory, savePath);
			IOUtil.copy(file, resFile, false);
		}

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/upload", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> upload(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		String fileName = multipartFile.getOriginalFilename();
		File file = FileUtil.getFile(tmpDirectory, fileName);

		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = multipartFile.getInputStream();
			out = IOUtil.getOutputStream(file);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}

		String uploadFilePath = FileUtil.getRelativePath(this.tempDirectory, file);

		Map<String, Object> results = new HashMap<>();
		results.put("filePath", uploadFilePath);
		results.put("fileName", fileName);

		return results;
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("path") String path)
	{
		File file = FileUtil.getFile(this.dashboardGlobalResRootDirectory, path);

		if (!file.exists())
			throw new RecordNotFoundException();

		DashboardGlobalResItem dashboardGlobalResItem = toDashboardGlobalResItem(file);

		model.addAttribute("dashboardGlobalResItem", dashboardGlobalResItem);

		return "/dashboardGlobalRes/dashboardGlobalRes_detail";
	}

	@RequestMapping(value = "/download")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("path") String path)
			throws Exception
	{
		File file = FileUtil.getFile(this.dashboardGlobalResRootDirectory, path);

		if (!file.exists())
			throw new RecordNotFoundException();

		String responseFileName = toResponseAttachmentFileName(request, response, file.getName());

		if (file.isDirectory())
		{
			if (!FileUtil.isExtension(responseFileName, "zip"))
				responseFileName += ".zip";
		}

		response.setCharacterEncoding(IOUtil.CHARSET_UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=" + responseFileName);
		OutputStream out = response.getOutputStream();

		if (file.isDirectory())
		{
			ZipOutputStream zout = null;

			try
			{
				zout = IOUtil.getZipOutputStream(out);
				IOUtil.writeFileToZipOutputStream(zout, file, file.getName());
			}
			finally
			{
				IOUtil.flush(zout);
				IOUtil.close(zout);
			}
		}
		else
		{
			IOUtil.write(file, out);
		}
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] paths)
	{
		for (int i = 0; i < paths.length; i++)
		{
			File file = FileUtil.getFile(this.dashboardGlobalResRootDirectory, paths[i]);
			FileUtil.deleteFile(file);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/query")
	public String query(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboardGlobalRes.manageDashboardGlobalRes");

		return "/dashboardGlobalRes/dashboardGlobalRes_grid";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DashboardGlobalResItem> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		return findDashboardGlobalResItems(pagingQuery.getKeyword());
	}

	protected List<DashboardGlobalResItem> findDashboardGlobalResItems(String keyword)
	{
		List<File> files = new ArrayList<File>();
		listAllDescendentFiles(this.dashboardGlobalResRootDirectory, files);

		List<DashboardGlobalResItem> resItems = new ArrayList<>(files.size());

		for (File file : files)
		{
			DashboardGlobalResItem item = toDashboardGlobalResItem(file);
			resItems.add(item);
		}

		if (StringUtil.isEmpty(keyword))
			return resItems;

		return KeywordMatcher.<DashboardGlobalResItem>match(resItems, keyword,
				new KeywordMatcher.MatchValue<DashboardGlobalResItem>()
				{
					@Override
					public String[] get(DashboardGlobalResItem t)
					{
						return new String[] { t.getPath() };
					}
				});
	}

	protected DashboardGlobalResItem toDashboardGlobalResItem(File file)
	{
		String path = FileUtil.trimPath(FileUtil.getRelativePath(this.dashboardGlobalResRootDirectory, file));

		if (file.isDirectory() && !path.endsWith(FileUtil.PATH_SEPARATOR_SLASH))
			path += FileUtil.PATH_SEPARATOR_SLASH;

		DashboardGlobalResItem item = new DashboardGlobalResItem(path);

		return item;
	}

	/**
	 * 列出所有嵌套目录、文件夹。
	 * 
	 * @param directory
	 * @param files
	 */
	protected void listAllDescendentFiles(File directory, List<File> files)
	{
		if (!directory.exists())
			return;

		File[] children = directory.listFiles();

		Arrays.sort(children, new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File child : children)
		{
			files.add(child);

			if (child.isDirectory())
				listAllDescendentFiles(child, files);
		}
	}

	public static class DashboardGlobalResAddForm implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private String filePath;

		private String fileName;

		/** 是否自动解压zip文件 */
		private boolean autoUnzip = false;

		/** 存储路径 */
		private String savePath = "";

		public DashboardGlobalResAddForm()
		{
			super();
		}

		public DashboardGlobalResAddForm(String filePath, String fileName)
		{
			super();
			this.filePath = filePath;
			this.fileName = fileName;
		}

		public String getFilePath()
		{
			return filePath;
		}

		public void setFilePath(String filePath)
		{
			this.filePath = filePath;
		}

		public String getFileName()
		{
			return fileName;
		}

		public void setFileName(String fileName)
		{
			this.fileName = fileName;
		}

		public boolean isAutoUnzip()
		{
			return autoUnzip;
		}

		public void setAutoUnzip(boolean autoUnzip)
		{
			this.autoUnzip = autoUnzip;
		}

		public String getSavePath()
		{
			return savePath;
		}

		public void setSavePath(String savePath)
		{
			this.savePath = savePath;
		}
	}

	public static class DashboardGlobalResItem implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 相对路径 */
		private String path;

		public DashboardGlobalResItem()
		{
			super();
		}

		public DashboardGlobalResItem(String path)
		{
			super();
			this.path = path;
		}

		public String getPath()
		{
			return path;
		}

		public void setPath(String path)
		{
			this.path = path;
		}
	}
}
