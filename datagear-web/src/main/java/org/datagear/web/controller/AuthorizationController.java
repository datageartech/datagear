/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.impl.AuthorizationQueryContext;
import org.datagear.management.service.impl.EnumValueLabel;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.controller.AuthorizationResourceMetas.PermissionMeta;
import org.datagear.web.controller.AuthorizationResourceMetas.ResourceMeta;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	@RequestMapping("/{resourceType}/{resource}/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		Authorization authorization = new Authorization();
		inflateResourceInfo(authorization, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);

		model.addAttribute("resourceType", resourceType);
		model.addAttribute("resource", resource);
		model.addAttribute("authorization", authorization);
		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthAddAuthorizationLabel());
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			Authorization authorization)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		inflateResourceInfo(authorization, resourceType, resource);
		checkInput(authorization);
		setResourceMetaAttribute(model, resourceType);

		authorization.setId(IDUtil.randomIdOnTime20());

		this.authorizationService.add(authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/{resource}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization authorization = this.authorizationService.getById(id);

		model.addAttribute("resourceType", resourceType);
		model.addAttribute("resource", resource);
		model.addAttribute("authorization", authorization);
		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthEditAuthorizationLabel());
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			Authorization authorization)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		inflateResourceInfo(authorization, resourceType, resource);

		if (isEmpty(authorization.getId()))
			throw new IllegalInputException();
		checkInput(authorization);

		setResourceMetaAttribute(model, resourceType);

		this.authorizationService.update(authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/{resource}/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization authorization = this.authorizationService.getById(id);

		if (authorization == null)
			throw new RecordNotFoundException();

		model.addAttribute("resourceType", resourceType);
		model.addAttribute("resource", resource);
		model.addAttribute("authorization", authorization);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthViewAuthorizationLabel());
		model.addAttribute(KEY_READONLY, true);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource, @RequestBody String[] ids)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		setResourceMetaAttribute(model, resourceType);
		this.authorizationService.deleteByIds(resourceType, resource, ids);

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/{resourceType}/{resource}/query")
	public String query(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = WebUtils.getUser(request, response);

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);

		model.addAttribute("resourceType", resourceType);
		model.addAttribute("resource", resource);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthManageAuthorizationLabel());

		return "/authorization/authorization_grid";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Authorization> queryData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		return this.authorizationService.query(pagingQuery);
	}

	protected void checkIsAllowAuthorization(User user, String resourceType, String resource)
			throws PermissionDeniedException
	{
		if (!this.authorizationService.isAllowAuthorization(user, resourceType, resource))
			throw new PermissionDeniedException();
	}

	protected void inflateResourceInfo(Authorization auth, String resourceType, String resource)
	{
		auth.setResourceType(resourceType);
		auth.setResource(resource);
	}

	protected ResourceMeta setResourceMetaAttribute(org.springframework.ui.Model model, String resourceType)
	{
		ResourceMeta resourceMeta = AuthorizationResourceMetas.get(resourceType);

		if (resourceMeta == null)
			throw new IllegalInputException();

		model.addAttribute("resourceMeta", resourceMeta);

		return resourceMeta;
	}

	protected void setAuthorizationQueryContext(HttpServletRequest request, ResourceMeta resourceMeta, String resource)
	{
		AuthorizationQueryContext context = new AuthorizationQueryContext();
		context.setPrincipalAllLabel(getMessage(request, "authorization.principalType.ALL"));
		context.setPrincipalAnonymousLabel(getMessage(request, "authorization.principalType.ANONYMOUS"));
		context.setResourceType(resourceMeta.getResourceType());
		context.setResource(resource);

		PermissionMeta[] permissionMetas = resourceMeta.getPermissionMetas();
		@SuppressWarnings("unchecked")
		EnumValueLabel<Integer>[] permissionLabels = new EnumValueLabel[permissionMetas.length];
		for (int i = 0; i < permissionMetas.length; i++)
		{
			PermissionMeta permissionMeta = permissionMetas[i];

			permissionLabels[i] = new EnumValueLabel<>(permissionMeta.getPermission(),
					getMessage(request, permissionMeta.getPermissionLabel()));
		}
		context.setPermissionLabels(permissionLabels);

		AuthorizationQueryContext.set(context);
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
