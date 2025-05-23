/*
 * Copyright 2018-present datagear.tech
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
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.connection.DriverLibraryInfo;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.KeywordMatcher;
import org.datagear.util.KeywordMatcher.MatchValue;
import org.datagear.web.util.DriverInfo;
import org.datagear.web.util.OperationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

	private KeywordMatcher keywordMatcher = new KeywordMatcher();

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

	public KeywordMatcher getKeywordMatcher()
	{
		return keywordMatcher;
	}

	public void setKeywordMatcher(KeywordMatcher keywordMatcher)
	{
		this.keywordMatcher = keywordMatcher;
	}

	public List<String> getCommonDriverClassNames()
	{
		return commonDriverClassNames;
	}

	public void setCommonDriverClassNames(List<String> commonDriverClassNames)
	{
		this.commonDriverClassNames = commonDriverClassNames;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		DriverEntity entity = createAdd(request, model);

		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/driverEntity/driverEntity_form";
	}

	protected DriverEntity createAdd(HttpServletRequest request, Model model)
	{
		DriverEntity entity = createInstance();
		entity.setId(IDUtil.randomIdOnTime20());

		return entity;
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DriverEntitySaveForm form)
			throws Exception
	{
		DriverEntity entity = form.getDriverEntity();
		String[] driverLibraryFileNames = form.getDriverLibraryFileNames();

		inflateSaveEntity(request, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		this.driverEntityManager.add(entity);

		if (driverLibraryFileNames != null)
		{
			File directory = getTempDriverLibraryDirectoryNotNull(entity.getId());

			for (String driverLibraryFileName : driverLibraryFileNames)
			{
				File driverLibraryFile = getTempDriverLibraryFile(directory, driverLibraryFileName);

				if (driverLibraryFile.exists())
				{
					InputStream in = IOUtil.getInputStream(driverLibraryFile);

					try
					{
						this.driverEntityManager.addDriverLibrary(entity, driverLibraryFileName, in);
					}
					finally
					{
						IOUtil.close(in);
					}
				}
			}
		}

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping(value = "/export")
	public void export(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "id", required = false) String[] driverEntityIds) throws Exception
	{
		setDownloadResponseHeader(request, response, "drivers.zip");
		response.setContentType(CONTENT_TYPE_OCTET_STREAM);

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
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		DriverEntity entity = this.driverEntityManager.get(id);
		checkNonNullEntity(entity);
		
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DriverEntitySaveForm form)
	{
		DriverEntity entity = form.getDriverEntity();

		inflateSaveEntity(request, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, entity);

		if (re != null)
			return re;

		this.driverEntityManager.update(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		DriverEntity entity = this.driverEntityManager.get(id);
		checkNonNullEntity(entity);
		
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/driverEntity/driverEntity_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		this.driverEntityManager.delete(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/manage")
	public String manage(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/driverEntity/driverEntity_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/driverEntity/driverEntity_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DriverEntity> queryData(HttpServletRequest request,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<DriverEntity> items = this.driverEntityManager.getAll();
		items = findByKeyword(items, pagingQuery.getKeyword());
		toQueryResponseData(request, items);

		return items;
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
		setDownloadResponseHeader(request, response, fileName);
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
			responseEntity = optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "driverEntity.deleteDriverFileFail");
		}
		else
		{
			responseEntity = optSuccessResponseEntity(request);
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

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, DriverEntity entity)
	{
		if (isEmpty(entity.getId()) || isBlank(entity.getDriverClassName()))
			throw new IllegalInputException();

		return null;
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

			FileInfo fileInfo = FileInfo.valueOfFile(driverLibraryInfo.getName(), driverLibraryInfo.getSize());

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

	protected void setFormPageAttr(HttpServletRequest request, Model model, DriverEntity entity)
	{
		setFormModel(model, entity);
	}

	protected void inflateSaveEntity(HttpServletRequest request, DriverEntity entity)
	{
	}

	protected void toFormResponseData(HttpServletRequest request, DriverEntity entity)
	{
	}

	protected void toQueryResponseData(HttpServletRequest request, List<DriverEntity> items)
	{
	}

	protected DriverEntity createInstance()
	{
		return new DriverEntity();
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
		return this.keywordMatcher.match(driverEntities, keyword, new MatchValue<DriverEntity>()
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
