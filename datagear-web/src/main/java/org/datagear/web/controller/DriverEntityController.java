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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.connection.DriverEntityManagerException;
import org.datagear.connection.DriverLibraryInfo;
import org.datagear.connection.XmlDriverEntityManager;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.web.util.DriverInfo;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据库驱动程序信息控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/driverEntity")
public class DriverEntityController extends AbstractController
{
	protected static final String TEMP_IMPORT_FILE_NAME = "import.zip";

	@Autowired
	private DriverEntityManager driverEntityManager;

	@Autowired
	private File tempDirectory;

	private List<String> commonDriverClassNames = Collections
			.unmodifiableList(DriverInfo.getDriverClassNames(DriverInfo.getCommonInDriverInfos()));

	public DriverEntityController()
	{
		super();
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public List<String> getCommonDriverClassNames()
	{
		return commonDriverClassNames;
	}

	public void setCommonDriverClassNames(List<String> commonDriverClassNames)
	{
		this.commonDriverClassNames = commonDriverClassNames;
	}

	@ExceptionHandler(IllegalImportDriverEntityFileFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleIllegalImportDriverEntityFileFormatException(HttpServletRequest request,
			HttpServletResponse response, IllegalImportDriverEntityFileFormatException exception)
	{
		String code = buildMessageCode("import." + IllegalImportDriverEntityFileFormatException.class.getSimpleName());

		setOperationMessageForThrowable(request, code, exception, false);

		return ERROR_PAGE_URL;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		DriverEntity driverEntity = new DriverEntity();
		driverEntity.setId(IDUtil.randomIdOnTime20());

		model.addAttribute("driverEntity", driverEntity);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.addDriverEntity");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DriverEntitySaveForm form)
			throws Exception
	{
		DriverEntity driverEntity = form.getDriverEntity();
		String[] driverLibraryFileNames = form.getDriverLibraryFileNames();

		if (isBlank(driverEntity.getId()) || isBlank(driverEntity.getDriverClassName()))
			throw new IllegalInputException();

		this.driverEntityManager.add(driverEntity);

		if (driverLibraryFileNames != null)
		{
			File directory = getTempDriverLibraryDirectoryNotNull(driverEntity.getId());

			for (String driverLibraryFileName : driverLibraryFileNames)
			{
				File driverLibraryFile = getTempDriverLibraryFile(directory, driverLibraryFileName);

				if (driverLibraryFile.exists())
				{
					InputStream in = IOUtil.getInputStream(driverLibraryFile);

					try
					{
						this.driverEntityManager.addDriverLibrary(driverEntity, driverLibraryFileName, in);
					}
					finally
					{
						IOUtil.close(in);
					}
				}
			}
		}

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/import")
	public String importDriverEntity(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("importId", IDUtil.uuid());

		return "/driverEntity/driverEntity_import";
	}

	@RequestMapping(value = "/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DriverEntity> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("importId") String importId, @RequestParam("file") MultipartFile multipartFile)
			throws Exception
	{
		File directory = getTempImportDirectory(importId, true);

		FileUtil.clearDirectory(directory);

		File importFile = FileUtil.getFile(directory, TEMP_IMPORT_FILE_NAME);

		InputStream in = null;
		OutputStream importFileOut = null;
		try
		{
			in = multipartFile.getInputStream();
			importFileOut = IOUtil.getOutputStream(importFile);
			IOUtil.write(in, importFileOut);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(importFileOut);
		}

		ZipInputStream importFileIn = IOUtil.getZipInputStream(importFile);

		XmlDriverEntityManager driverEntityManager = new XmlDriverEntityManager(directory);

		try
		{
			driverEntityManager.init();

			return driverEntityManager.readDriverEntitiesFromZip(importFileIn);
		}
		catch (DriverEntityManagerException e)
		{
			throw new IllegalImportDriverEntityFileFormatException(e);
		}
		finally
		{
			IOUtil.close(importFileIn);
			driverEntityManager.releaseAll();
		}
	}

	@RequestMapping(value = "/saveImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveImport(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DriverEntitySaveImportForm form)
			throws Exception
	{
		String importId = form.getImportId();
		String[] driverEntityIds = form.getDriverEntityIds();

		if (isEmpty(importId) || isNull(driverEntityIds))
			throw new IllegalInputException();

		File directory = getTempImportDirectory(importId, false);
		File importFile = FileUtil.getFile(directory, TEMP_IMPORT_FILE_NAME);

		if (!importFile.exists())
			throw new IllegalInputException("import file for [" + importId + "] not exists");

		ZipInputStream in = IOUtil.getZipInputStream(importFile);

		this.driverEntityManager.importFromZip(in, driverEntityIds);

		return buildOperationMessageSuccessResponseEntity(request, buildMessageCode("import.success"));
	}

	@RequestMapping(value = "/export")
	public void export(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "id", required = false) String[] driverEntityIds) throws Exception
	{
		response.addHeader("Content-Disposition",
				"attachment;filename=" + toResponseAttachmentFileName(request, response, "drivers.zip"));
		response.setContentType("application/octet-stream");

		ZipOutputStream zout = IOUtil.getZipOutputStream(response.getOutputStream());

		try
		{
			this.driverEntityManager.exportToZip(zout, driverEntityIds);
		}
		finally
		{
			zout.flush();
			zout.close();
		}
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		DriverEntity driverEntity = this.driverEntityManager.get(id);

		model.addAttribute("driverEntity", driverEntity);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.editDriverEntity");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DriverEntitySaveForm form)
	{
		DriverEntity driverEntity = form.getDriverEntity();

		if (isBlank(driverEntity.getId()) || isBlank(driverEntity.getDriverClassName()))
			throw new IllegalInputException();

		this.driverEntityManager.update(driverEntity);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		DriverEntity driverEntity = this.driverEntityManager.get(id);

		model.addAttribute("driverEntity", driverEntity);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.viewDriverEntity");
		model.addAttribute(KEY_READONLY, true);

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.driverEntityManager.delete(ids);

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/query")
	public String query(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.manageDriverEntity");

		return "/driverEntity/driverEntity_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.selectDriverEntity");
		model.addAttribute(KEY_SELECT_OPERATION, true);

		return "/driverEntity/driverEntity_grid";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DriverEntity> queryData(HttpServletRequest request,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<DriverEntity> driverEntities = this.driverEntityManager.getAll();

		driverEntities = findByKeyword(driverEntities, pagingQuery.getKeyword());

		return driverEntities;
	}

	@RequestMapping(value = "/uploadDriverFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadDriverFile(HttpServletRequest request, @RequestParam("id") String id,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		FileInfo[] fileInfos;
		List<String> driverClassNames = new ArrayList<>();

		String originalFilename = multipartFile.getOriginalFilename();

		DriverEntity driverEntity = this.driverEntityManager.get(id);

		if (driverEntity != null)
		{
			InputStream in = multipartFile.getInputStream();

			try
			{
				this.driverEntityManager.addDriverLibrary(driverEntity, originalFilename, in);
			}
			finally
			{
				IOUtil.close(in);
			}

			List<DriverLibraryInfo> driverLibraryInfos = this.driverEntityManager.getDriverLibraryInfos(driverEntity);
			fileInfos = toFileInfos(driverLibraryInfos);
		}
		else
		{
			File directory = getTempDriverLibraryDirectoryNotNull(id);
			File tempFile = getTempDriverLibraryFile(directory, originalFilename);

			multipartFile.transferTo(tempFile);

			resolveDriverClassNames(tempFile, driverClassNames);

			fileInfos = FileUtil.getFileInfos(directory);
		}

		Map<String, Object> map = new HashMap<>();
		map.put("fileInfos", fileInfos);
		map.put("driverClassNames", driverClassNames);

		return map;
	}

	@RequestMapping(value = "/downloadDriverFile")
	public void downloadDriverFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("file") String fileName) throws Exception
	{
		DriverEntity driverEntity = this.driverEntityManager.get(id);

		response.setCharacterEncoding(IOUtil.CHARSET_UTF_8);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, fileName));
		OutputStream out = response.getOutputStream();

		if (driverEntity != null)
		{
			this.driverEntityManager.readDriverLibrary(driverEntity, fileName, out);
		}
		else
		{
			File directory = getTempDriverLibraryDirectoryNotNull(id);
			File tempFile = getTempDriverLibraryFile(directory, fileName);

			// 即使文件不存在也不抛出异常了，会导致浏览器跳转到新的错误提示页面
			if (tempFile.exists())
				IOUtil.write(tempFile, out);
		}
	}

	@RequestMapping(value = "/deleteDriverFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteDriverFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("file") String fileName) throws Exception
	{
		boolean deleted = false;
		FileInfo[] fileInfos = null;

		DriverEntity driverEntity = this.driverEntityManager.get(id);

		if (driverEntity != null)
		{
			deleted = this.driverEntityManager.deleteDriverLibrary(driverEntity, fileName)[0];

			if (deleted)
			{
				List<DriverLibraryInfo> driverLibraryInfos = this.driverEntityManager
						.getDriverLibraryInfos(driverEntity);
				fileInfos = toFileInfos(driverLibraryInfos);
			}
		}
		else
		{
			File directory = getTempDriverLibraryDirectoryNotNull(id);
			File tempFile = getTempDriverLibraryFile(directory, fileName);

			deleted = FileUtil.deleteFile(tempFile);

			if (deleted)
				fileInfos = FileUtil.getFileInfos(directory);
		}

		ResponseEntity<OperationMessage> responseEntity = null;

		if (!deleted)
		{
			responseEntity = buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("deleteDriverFileFail"));
		}
		else
		{
			responseEntity = buildOperationMessageDeleteSuccessResponseEntity(request);
			responseEntity.getBody().setData(fileInfos);
		}

		return responseEntity;
	}

	@RequestMapping(value = "/listDriverFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo[] listDriverFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String id) throws Exception
	{
		FileInfo[] fileInfos;

		DriverEntity driverEntity = this.driverEntityManager.get(id);

		if (driverEntity != null)
		{
			List<DriverLibraryInfo> driverLibraryInfos = this.driverEntityManager.getDriverLibraryInfos(driverEntity);
			fileInfos = toFileInfos(driverLibraryInfos);
		}
		else
		{
			File directory = getTempDriverLibraryDirectoryNotNull(id);
			fileInfos = FileUtil.getFileInfos(directory);
		}

		return fileInfos;
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("driverEntity", code);
	}

	protected void resolveDriverClassNames(File file, List<String> driverClassNames)
	{
		if (!FileUtil.isExtension(file, "jar"))
			return;

		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

			while (enumeration.hasMoreElements())
			{
				String name = enumeration.nextElement().getName();
				if (FileUtil.isExtension(name, DriverInfo.DRIVER_CLASS_FILE_SUFFIX))
				{
					String className = DriverInfo.toDriverClassName(name);

					if (this.commonDriverClassNames.contains(className))
						driverClassNames.add(className);
				}
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			IOUtil.close(zipFile);
		}
	}

	protected FileInfo[] toFileInfos(List<DriverLibraryInfo> driverLibraryInfos)
	{
		FileInfo[] fileInfos = new FileInfo[driverLibraryInfos.size()];

		for (int i = 0; i < fileInfos.length; i++)
		{
			DriverLibraryInfo driverLibraryInfo = driverLibraryInfos.get(i);

			FileInfo fileInfo = new FileInfo(driverLibraryInfo.getName(), driverLibraryInfo.getSize());

			fileInfos[i] = fileInfo;
		}

		return fileInfos;
	}

	protected File getTempDriverLibraryFile(File tempDriverLibraryDirectory, String fileName)
	{
		File tempFile = FileUtil.getFile(tempDriverLibraryDirectory, fileName);
		return tempFile;
	}

	protected File getTempDriverLibraryDirectoryNotNull(String driverEntityId)
	{
		File directory = FileUtil.getDirectory(getDriverEntityTmpDirectory(), driverEntityId, true);
		return directory;
	}

	protected File getTempImportDirectory(String importId, boolean notNull)
	{
		File directory = FileUtil.getDirectory(getDriverEntityTmpDirectory(), importId, notNull);
		return directory;
	}

	protected File getDriverEntityTmpDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "driverEntity", true);
	}

	/**
	 * 根据表名称关键字查询{@linkplain TableInfo}列表。
	 * 
	 * @param driverEntities
	 * @param tableNameKeyword
	 * @return
	 */
	protected List<DriverEntity> findByKeyword(List<DriverEntity> driverEntities, String keyword)
	{
		return KeywordMatcher.<DriverEntity> match(driverEntities, keyword,
				new KeywordMatcher.MatchValue<DriverEntity>()
				{
					@Override
					public String[] get(DriverEntity t)
					{
						return new String[] { t.getDisplayName(), t.getDriverClassName(), t.getDisplayDesc() };
					}
				});
	}

	public static class DriverEntitySaveForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private DriverEntity driverEntity;

		private String[] driverLibraryFileNames = null;

		public DriverEntitySaveForm()
		{
			super();
		}

		public DriverEntity getDriverEntity()
		{
			return driverEntity;
		}

		public void setDriverEntity(DriverEntity driverEntity)
		{
			this.driverEntity = driverEntity;
		}

		public String[] getDriverLibraryFileNames()
		{
			return driverLibraryFileNames;
		}

		public void setDriverLibraryFileNames(String[] driverLibraryFileNames)
		{
			this.driverLibraryFileNames = driverLibraryFileNames;
		}
	}

	public static class DriverEntitySaveImportForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String importId;
		private String[] driverEntityIds;

		public DriverEntitySaveImportForm()
		{
			super();
		}

		public String getImportId()
		{
			return importId;
		}

		public void setImportId(String importId)
		{
			this.importId = importId;
		}

		public String[] getDriverEntityIds()
		{
			return driverEntityIds;
		}

		public void setDriverEntityIds(String[] driverEntityIds)
		{
			this.driverEntityIds = driverEntityIds;
		}
	}
}
