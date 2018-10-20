/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.connection.DriverEntityManagerException;
import org.datagear.connection.DriverLibraryInfo;
import org.datagear.connection.IOUtil;
import org.datagear.connection.XmlDriverEntityManager;
import org.datagear.dbinfo.TableInfo;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.FileUtils;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.vo.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
	@Qualifier("tempDriverLibraryRootDirectory")
	private File tempDriverLibraryRootDirectory;

	public DriverEntityController()
	{
		super();
	}

	public DriverEntityController(MessageSource messageSource, ClassDataConverter classDataConverter,
			DriverEntityManager driverEntityManager, File tempDriverLibraryRootDirectory)
	{
		super(messageSource, classDataConverter);
		this.driverEntityManager = driverEntityManager;
		this.tempDriverLibraryRootDirectory = tempDriverLibraryRootDirectory;
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public File getTempDriverLibraryRootDirectory()
	{
		return tempDriverLibraryRootDirectory;
	}

	public void setTempDriverLibraryRootDirectory(File tempDriverLibraryRootDirectory)
	{
		this.tempDriverLibraryRootDirectory = tempDriverLibraryRootDirectory;
	}

	@ExceptionHandler(IllegalImportDriverEntityFileFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleIllegalImportDriverEntityFileFormatException(HttpServletRequest request,
			HttpServletResponse response, IllegalImportDriverEntityFileFormatException exception)
	{
		String code = buildMessageCode("import." + IllegalImportDriverEntityFileFormatException.class.getSimpleName());

		setOperationMessageForException(request, code, exception, false);

		return ERROR_PAGE_URL;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		DriverEntity driverEntity = new DriverEntity();
		driverEntity.setId(UUID.gen());

		model.addAttribute("driverEntity", driverEntity);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "driverEntity.addDriverEntity");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			DriverEntity driverEntity,
			@RequestParam(value = "driverLibraryName", required = false) String[] driverLibraryFileNames)
			throws Exception
	{
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
		model.addAttribute("importId", UUID.gen());

		return "/driverEntity/driverEntity_import";
	}

	@RequestMapping(value = "/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DriverEntity> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("importId") String importId, @RequestParam("file") MultipartFile multipartFile)
			throws Exception
	{
		File directory = getTempImportDirectory(importId, true);

		IOUtil.clearDirectory(directory);

		File importFile = IOUtil.getFile(directory, TEMP_IMPORT_FILE_NAME);

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
			@RequestParam("importId") String importId, @RequestParam("driverEntity.id") String[] driverEntityIds)
			throws Exception
	{
		File directory = getTempImportDirectory(importId, false);
		File importFile = IOUtil.getFile(directory, TEMP_IMPORT_FILE_NAME);

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
		response.addHeader("Content-Disposition", "attachment;filename=drivers.zip");
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
			DriverEntity driverEntity)
	{
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
		model.addAttribute(KEY_READONLY, "true");

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
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
		model.addAttribute(KEY_SELECTONLY, "true");

		return "/driverEntity/driverEntity_grid";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DriverEntity> queryData(HttpServletRequest request) throws Exception
	{
		PagingQuery pagingQuery = getPagingQuery(request, null);

		List<DriverEntity> driverEntities = this.driverEntityManager.getAll();

		driverEntities = findByKeyword(driverEntities, pagingQuery.getKeyword());

		return driverEntities;
	}

	@RequestMapping(value = "/uploadDriverFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo[] uploadDriverFile(HttpServletRequest request, @RequestParam("id") String id,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		FileInfo[] fileInfos;

		DriverEntity driverEntity = this.driverEntityManager.get(id);

		if (driverEntity != null)
		{
			InputStream in = multipartFile.getInputStream();

			try
			{
				this.driverEntityManager.addDriverLibrary(driverEntity, multipartFile.getOriginalFilename(), in);
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
			File tempFile = getTempDriverLibraryFile(directory, multipartFile.getOriginalFilename());

			multipartFile.transferTo(tempFile);

			fileInfos = FileUtils.getFileInfos(directory);
		}

		return fileInfos;
	}

	@RequestMapping(value = "/downloadDriverFile")
	public void downloadDriverFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("file") String fileName) throws Exception
	{
		DriverEntity driverEntity = this.driverEntityManager.get(id);

		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "");
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
		FileInfo[] fileInfos;

		DriverEntity driverEntity = this.driverEntityManager.get(id);

		if (driverEntity != null)
		{
			this.driverEntityManager.deleteDriverLibrary(driverEntity, fileName);

			List<DriverLibraryInfo> driverLibraryInfos = this.driverEntityManager.getDriverLibraryInfos(driverEntity);
			fileInfos = toFileInfos(driverLibraryInfos);
		}
		else
		{
			File directory = getTempDriverLibraryDirectoryNotNull(id);
			File tempFile = getTempDriverLibraryFile(directory, fileName);

			IOUtil.deleteFile(tempFile);

			fileInfos = FileUtils.getFileInfos(directory);
		}

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageDeleteSuccessResponseEntity(request);
		responseEntity.getBody().setData(fileInfos);

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
			fileInfos = FileUtils.getFileInfos(directory);
		}

		return fileInfos;
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("driverEntity", code);
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
		File tempFile = new File(tempDriverLibraryDirectory, fileName);

		return tempFile;
	}

	protected File getTempDriverLibraryDirectoryNotNull(String driverEntityId)
	{
		File directory = new File(this.tempDriverLibraryRootDirectory, driverEntityId);

		if (!directory.exists())
			directory.mkdirs();

		return directory;
	}

	protected File getTempImportDirectory(String importId, boolean notNull)
	{
		File directory = new File(this.tempDriverLibraryRootDirectory, importId);

		if (notNull && !directory.exists())
			directory.mkdirs();

		return directory;
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
}
