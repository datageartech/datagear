/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.User;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
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

/**
 * 数据集资源目录控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dataSetResDirectory")
public class DataSetResDirectoryController extends AbstractController
{
	static
	{
		AuthorizationResourceMetas.registerForShare(DataSetResDirectory.AUTHORIZATION_RESOURCE_TYPE,
				"dataSetResDirectory");
	}

	@Autowired
	private DataSetResDirectoryService dataSetResDirectoryService;

	public DataSetResDirectoryController()
	{
		super();
	}

	public DataSetResDirectoryService getDataSetResDirectoryService()
	{
		return dataSetResDirectoryService;
	}

	public void setDataSetResDirectoryService(DataSetResDirectoryService dataSetResDirectoryService)
	{
		this.dataSetResDirectoryService = dataSetResDirectoryService;
	}

	@ExceptionHandler(DataSetResDirectoryNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDataSetResDirectoryNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DataSetResDirectoryNotFoundException exception)
	{
		setOperationMessageForThrowable(request, "dataSetResDirectory.DataSetResDirectoryNotFoundException", exception,
				false, exception.getDirectory());

		return getErrorView(request, response);
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		DataSetResDirectory dataSetResDirectory = new DataSetResDirectory();

		model.addAttribute("dataSetResDirectory", dataSetResDirectory);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSetResDirectory.addDataSetResDirectory");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/dataSetResDirectory/dataSetResDirectory_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DataSetResDirectory dataSetResDirectory)
	{
		checkSaveEntity(dataSetResDirectory);

		User user = WebUtils.getUser(request, response);

		dataSetResDirectory.setId(IDUtil.randomIdOnTime20());
		dataSetResDirectory.setCreateUser(user);

		this.dataSetResDirectoryService.add(dataSetResDirectory);

		return optMsgSaveSuccessResponseEntity(request, dataSetResDirectory);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		DataSetResDirectory dataSetResDirectory = this.dataSetResDirectoryService.getByIdForEdit(user, id);

		if (dataSetResDirectory == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSetResDirectory", dataSetResDirectory);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSetResDirectory.editDataSetResDirectory");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/dataSetResDirectory/dataSetResDirectory_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DataSetResDirectory dataSetResDirectory)
	{
		checkSaveEntity(dataSetResDirectory);

		User user = WebUtils.getUser(request, response);

		this.dataSetResDirectoryService.update(user, dataSetResDirectory);

		return optMsgSaveSuccessResponseEntity(request, dataSetResDirectory);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		DataSetResDirectory dataSetResDirectory = this.dataSetResDirectoryService.getById(user, id);

		if (dataSetResDirectory == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSetResDirectory", dataSetResDirectory);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSetResDirectory.viewDataSetResDirectory");
		model.addAttribute(KEY_READONLY, true);

		return "/dataSetResDirectory/dataSetResDirectory_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.dataSetResDirectoryService.deleteById(user, id);
		}

		return optMsgDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		return "/dataSetResDirectory/dataSetResDirectory_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/dataSetResDirectory/dataSetResDirectory_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DataSetResDirectory> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		User user = WebUtils.getUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<DataSetResDirectory> pagingData = this.dataSetResDirectoryService.pagingQuery(user, pagingQuery);

		return pagingData;
	}

	@RequestMapping(value = "/listFiles", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<FileInfo> listFiles(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser();

		DataSetResDirectory dataSetResDirectory = this.dataSetResDirectoryService.getById(user, id);

		if (dataSetResDirectory == null)
			throw new RecordNotFoundException();

		List<FileInfo> fileInfos = new ArrayList<>();

		File directory = FileUtil.getDirectory(dataSetResDirectory.getDirectory(), false);
		listDataSetResDirectoryFilePaths(directory, "", fileInfos);

		return fileInfos;
	}

	protected void listDataSetResDirectoryFilePaths(File directory, String parentPath, List<FileInfo> fileInfos)
	{
		if (!directory.exists())
			return;

		if (!directory.isDirectory())
			return;

		File[] files = directory.listFiles();
		Arrays.sort(files, FILE_NAME_ASC_COMPARATOR);

		for (File file : files)
		{
			String myPath = (StringUtil.isEmpty(parentPath) ? file.getName()
					: FileUtil.concatPath(parentPath, file.getName()));

			if (file.isDirectory())
				listDataSetResDirectoryFilePaths(file, myPath, fileInfos);
			else
				fileInfos.add(new FileInfo(myPath));
		}
	}

	protected void checkSaveEntity(DataSetResDirectory dataSetResDirectory)
	{
		if (isBlank(dataSetResDirectory.getDirectory()))
			throw new IllegalInputException();

		File directory = FileUtil.getDirectory(dataSetResDirectory.getDirectory(), false);

		if (!directory.exists())
			throw new DataSetResDirectoryNotFoundException(dataSetResDirectory.getDirectory());
	}

	protected static final Comparator<File> FILE_NAME_ASC_COMPARATOR = new Comparator<File>()
	{
		@Override
		public int compare(File o1, File o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};
}
