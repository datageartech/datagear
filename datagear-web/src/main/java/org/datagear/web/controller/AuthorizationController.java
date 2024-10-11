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
import org.springframework.ui.Model;
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
	public String add(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		ResourceMeta resourceMeta = getResourceMetaNonNull(request, resourceType);

		checkIsAllowAuthorization(user, resourceType, resource);

		Authorization entity = createAdd(request, model, resource, resourceType, resourceMeta);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity, resourceMeta);

		return "/authorization/authorization_form";
	}

	protected Authorization createAdd(HttpServletRequest request, Model model, String resource, String resourceType,
			ResourceMeta resourceMeta)
	{
		Authorization entity = createInstance();

		entity.setId("");
		entity.setResource(resource);
		entity.setResourceType(resourceType);
		entity.setPrincipal("");
		entity.setPrincipalType(Authorization.PRINCIPAL_TYPE_USER);
		entity.setPermission(resourceMeta.getPermissionMetas()[0].getPermission());

		return entity;
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource, @RequestBody Authorization entity)
	{
		User user = getCurrentUser();

		checkResourceType(request, resourceType);
		checkIsAllowAuthorization(user, resourceType, resource);

		entity.setId(IDUtil.randomIdOnTime20());
		inflateSaveEntity(request, entity);
		inflateResourceInfo(entity, resourceType, resource);

		checkSaveEntity(entity);

		this.authorizationService.add(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/{resourceType}/{resource}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		ResourceMeta resourceMeta = getResourceMetaNonNull(request, resourceType);

		checkIsAllowAuthorization(user, resourceType, resource);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization entity = getByIdForEdit(this.authorizationService, id);

		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity, resourceMeta);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource, @RequestBody Authorization entity)
	{
		User user = getCurrentUser();

		checkResourceType(request, resourceType);
		checkIsAllowAuthorization(user, resourceType, resource);

		inflateSaveEntity(request, entity);
		inflateResourceInfo(entity, resourceType, resource);

		checkSaveEntity(entity);

		this.authorizationService.update(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/{resourceType}/{resource}/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		ResourceMeta resourceMeta = getResourceMetaNonNull(request, resourceType);

		checkIsAllowAuthorization(user, resourceType, resource);

		setAuthorizationQueryContext(request, resourceMeta, resource);

		Authorization entity = getByIdForView(this.authorizationService, id);

		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity, resourceMeta);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource, @RequestBody String[] ids)
	{
		User user = getCurrentUser();

		checkResourceType(request, resourceType);
		checkIsAllowAuthorization(user, resourceType, resource);

		this.authorizationService.deleteByIds(resourceType, resource, ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/{resourceType}/{resource}/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("resourceType") String resourceType, @PathVariable("resource") String resource)
	{
		User user = getCurrentUser();

		ResourceMeta resourceMeta = getResourceMetaNonNull(request, resourceType);

		checkIsAllowAuthorization(user, resourceType, resource);

		setResourceMetaAttribute(request, model, resourceMeta);
		model.addAttribute("resource", resource);
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model, false);

		return "/authorization/authorization_table";
	}

	@RequestMapping(value = "/{resourceType}/{resource}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Authorization> queryData(HttpServletRequest request, HttpServletResponse response,
			Model model, @PathVariable("resourceType") String resourceType,
			@PathVariable("resource") String resource,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		ResourceMeta resourceMeta = getResourceMetaNonNull(request, resourceType);

		checkIsAllowAuthorization(user, resourceType, resource);
		setAuthorizationQueryContext(request, resourceMeta, resource);

		List<Authorization> list = this.authorizationService.query(pagingQuery);

		toQueryResponseData(request, list);

		return list;
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

	protected ResourceMeta getResourceMetaNonNull(HttpServletRequest request, String resourceType)
	{
		ResourceMeta rm = this.authorizationResMetaManager.get(resourceType);

		if (rm == null)
			throw new IllegalInputException();

		return rm;
	}

	protected void checkResourceType(HttpServletRequest request, String resourceType)
	{
		getResourceMetaNonNull(request, resourceType);
	}

	protected void setResourceMetaAttribute(HttpServletRequest request, Model model, ResourceMeta rm)
	{
		model.addAttribute("resourceMeta", rm);
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

	protected void checkSaveEntity(Authorization entity) throws IllegalInputException
	{
		if (isEmpty(entity.getId()) || isEmpty(entity.getResource())
				|| isEmpty(entity.getResourceType())
				|| isEmpty(entity.getPrincipal()) || isEmpty(entity.getPrincipalType())
				|| entity.getPermission() < Authorization.PERMISSION_MIN
				|| entity.getPermission() > Authorization.PERMISSION_MAX)
		{
			throw new IllegalInputException();
		}
	}

	protected void setFormPageAttr(HttpServletRequest request, Model model, Authorization entity,
			ResourceMeta resourceMeta)
	{
		setFormModel(model, entity);
		setResourceMetaAttribute(request, model, resourceMeta);
		model.addAttribute("permissionMetas", resourceMeta.getPermissionMetas());
	}

	protected void inflateSaveEntity(HttpServletRequest request, Authorization entity)
	{
	}

	protected void toFormResponseData(HttpServletRequest request, Authorization entity)
	{
	}

	protected void toQueryResponseData(HttpServletRequest request, List<Authorization> items)
	{
	}

	protected Authorization createInstance()
	{
		return new Authorization();
	}
}
