/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.impl.AuthorizationQueryLabel;
import org.datagear.management.service.impl.ServiceContext;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 授权管理控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/authorization")
public class AuthorizationController extends AbstractController
{
	@Autowired
	private AuthorizationService authorizationService;

	public AuthorizationController()
	{
		super();
	}

	public AuthorizationController(AuthorizationService authorizationService)
	{
		super();
		this.authorizationService = authorizationService;
	}

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "authorization.addAuthorization");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			Authorization authorization)
	{
		checkInput(authorization);

		User user = WebUtils.getUser(request, response);

		authorization.setId(IDUtil.uuid());
		authorization.setCreateUser(user);

		this.authorizationService.add(authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		setAuthorizationQueryLabel(request);

		Authorization authorization = this.authorizationService.getById(id);

		model.addAttribute("authorization", authorization);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "authorization.editAuthorization");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			Authorization authorization)
	{
		if (isEmpty(authorization.getId()))
			throw new IllegalInputException();
		checkInput(authorization);

		this.authorizationService.update(WebUtils.getUser(request, response), authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		setAuthorizationQueryLabel(request);

		Authorization authorization = this.authorizationService.getById(id);

		if (authorization == null)
			throw new RecordNotFoundException();

		model.addAttribute("authorization", authorization);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "authorization.viewAuthorization");
		model.addAttribute(KEY_READONLY, true);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
	{
		this.authorizationService.deleteByIds(WebUtils.getUser(request, response), ids);

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/query")
	public String query(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "authorization.manageAuthorization");

		return "/authorization/authorization_grid";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Authorization> queryData(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		PagingQuery pagingQuery = getPagingQuery(request, null);

		setAuthorizationQueryLabel(request);

		List<Authorization> authorizations = this.authorizationService.query(WebUtils.getUser(request, response),
				pagingQuery);

		return authorizations;
	}

	protected void setAuthorizationQueryLabel(HttpServletRequest request)
	{
		AuthorizationQueryLabel label = new AuthorizationQueryLabel();
		label.setPrincipalAll(getMessage(request, "authorization.principalType.ALL"));
		label.setPrincipalAnonymous(getMessage(request, "authorization.principalType.ANONYMOUS"));

		ServiceContext.get().setValue(AuthorizationQueryLabel.CUSTOM_QUERY_PARAMETER_NAME, label);
	}

	protected void checkInput(Authorization authorization) throws IllegalInputException
	{
		if (isEmpty(authorization.getResource()) || isEmpty(authorization.getResourceType())
				|| isEmpty(authorization.getPrincipal()) || isEmpty(authorization.getPrincipalType())
				|| authorization.getPermission() < Authorization.PERMISSION_NONE_START
				|| authorization.getPermission() > Authorization.PERMISSION_MAX)
			throw new IllegalInputException();
	}
}
