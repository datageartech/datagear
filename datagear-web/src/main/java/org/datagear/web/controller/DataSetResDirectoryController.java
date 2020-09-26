/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.User;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
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
	public String handleDataSetResDirectoryNotFoundException(HttpServletRequest request,
			HttpServletResponse response, DataSetResDirectoryNotFoundException exception)
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

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSetResDirectory);
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

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSetResDirectory);
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

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSetResDirectory.manageDataSetResDirectory");

		return "/dataSetResDirectory/dataSetResDirectory_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSetResDirectory.selectDataSetResDirectory");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/dataSetResDirectory/dataSetResDirectory_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DataSetResDirectory> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		PagingData<DataSetResDirectory> pagingData = this.dataSetResDirectoryService.pagingQuery(user, pagingQuery);

		return pagingData;
	}

	protected void checkSaveEntity(DataSetResDirectory dataSetResDirectory)
	{
		if (isBlank(dataSetResDirectory.getDirectory()))
			throw new IllegalInputException();

		File directory = FileUtil.getDirectory(dataSetResDirectory.getDirectory(), false);

		if (!directory.exists())
			throw new DataSetResDirectoryNotFoundException(dataSetResDirectory.getDirectory());
	}
}
