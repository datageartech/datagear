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
import org.datagear.web.util.AuthorizationResMetaManager;
import org.datagear.web.util.AuthorizationResMetaManager.PermissionMeta;
import org.datagear.web.util.AuthorizationResMetaManager.ResourceMeta;
import org.datagear.web.util.OperationMessage;
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

	@Autowired
	private AuthorizationResMetaManager authorizationResMetaManager;

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

	public AuthorizationResMetaManager getAuthorizationResMetaManager()
	{
		return authorizationResMetaManager;
	}

	public void setAuthorizationResMetaManager(AuthorizationResMetaManager authorizationResMetaManager)
	{
		this.authorizationResMetaManager = authorizationResMetaManager;
	}

	@RequestMapping("/{resourceType}/{resource}/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		Authorization authorization =new Authorization("", resource, resourceType, "",
				Authorization.PRINCIPAL_TYPE_USER, resourceMeta.getPermissionMetas()[0].getPermission());

		model.addAttribute("permissionMetas", resourceMeta.getPermissionMetas());
		setFormModel(model, authorization, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			@RequestBody Authorization authorization)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		inflateResourceInfo(authorization, resourceType, resource);
		checkInput(authorization);
		setResourceMetaAttribute(model, resourceType);

		authorization.setId(IDUtil.randomIdOnTime20());

		this.authorizationService.add(authorization);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/{resource}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization authorization = getByIdForEdit(this.authorizationService, id);

		model.addAttribute("permissionMetas", resourceMeta.getPermissionMetas());
		setFormModel(model, authorization, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			@RequestBody Authorization authorization)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		inflateResourceInfo(authorization, resourceType, resource);

		if (isEmpty(authorization.getId()))
			throw new IllegalInputException();
		
		checkInput(authorization);

		setResourceMetaAttribute(model, resourceType);

		this.authorizationService.update(authorization);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/{resource}/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization authorization = getByIdForView(this.authorizationService, id);

		model.addAttribute("permissionMetas", resourceMeta.getPermissionMetas());
		setFormModel(model, authorization, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource, @RequestBody String[] ids)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		setResourceMetaAttribute(model, resourceType);
		this.authorizationService.deleteByIds(resourceType, resource, ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/{resourceType}/{resource}/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = getCurrentUser();

		checkIsAllowAuthorization(user, resourceType, resource);

		setResourceMetaAttribute(model, resourceType);
		model.addAttribute("resource", resource);
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model, false);

		return "/authorization/authorization_table";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Authorization> queryData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
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
		ResourceMeta resourceMeta = this.authorizationResMetaManager.get(resourceType);

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
				|| authorization.getPermission() < Authorization.PERMISSION_MIN
				|| authorization.getPermission() > Authorization.PERMISSION_MAX)
			throw new IllegalInputException();
	}
}
