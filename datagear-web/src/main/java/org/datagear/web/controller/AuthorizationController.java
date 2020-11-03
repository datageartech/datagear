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
import org.datagear.management.service.impl.AuthorizationQueryContext;
import org.datagear.management.service.impl.EnumValueLabel;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
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
	/**
	 * 指定授权资源参数，设置后，所有CRUD操作都只针对这一个资源。
	 */
	public static final String PARAM_ASSIGNED_RESOURCE = "assignedResource";

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

	@RequestMapping("/{resourceType}/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType)
	{
		User user = WebUtils.getUser(request, response);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAssignedResourceAttributeIf(request, model, resourceMeta);
		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthAddAuthorizationLabel());
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			Authorization authorization)
	{
		checkInput(authorization);
		setResourceMetaAttribute(model, resourceType);

		User user = WebUtils.getUser(request, response);

		authorization.setId(IDUtil.randomIdOnTime20());
		authorization.setCreateUser(user);

		this.authorizationService.add(user, authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta);

		Authorization authorization = this.authorizationService.getByIdForEdit(user, id);

		setAssignedResourceAttributeIf(request, model, resourceMeta);
		model.addAttribute("authorization", authorization);
		model.addAttribute("user", user);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthEditAuthorizationLabel());
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			Authorization authorization)
	{
		if (isEmpty(authorization.getId()))
			throw new IllegalInputException();
		checkInput(authorization);

		setResourceMetaAttribute(model, resourceType);

		User user = WebUtils.getUser(request, response);

		this.authorizationService.update(user, authorization);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/{resourceType}/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta);

		Authorization authorization = this.authorizationService.getById(user, id);

		if (authorization == null)
			throw new RecordNotFoundException();

		setAssignedResourceAttributeIf(request, model, resourceMeta);
		model.addAttribute("authorization", authorization);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthViewAuthorizationLabel());
		model.addAttribute(KEY_READONLY, true);

		return "/authorization/authorization_form";
	}

	@RequestMapping(value = "/{resourceType}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@RequestBody String[] ids)
	{
		setResourceMetaAttribute(model, resourceType);
		this.authorizationService.deleteByIds(WebUtils.getUser(request, response), ids);

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/{resourceType}/query")
	public String query(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("resourceType") String resourceType)
	{
		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAssignedResourceAttributeIf(request, model, resourceMeta);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, resourceMeta.getAuthManageAuthorizationLabel());

		return "/authorization/authorization_grid";
	}

	@RequestMapping(value = "/{resourceType}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Authorization> queryData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @PathVariable("resourceType") String resourceType,
			@RequestBody(required = false) PagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		ResourceMeta resourceMeta = setResourceMetaAttribute(model, resourceType);
		setAuthorizationQueryContext(request, resourceMeta);
		String assignedResource = getAssignedResource(request);

		List<Authorization> authorizations = null;

		if (!isEmpty(assignedResource))
			authorizations = this.authorizationService.queryForAssignedResource(user, assignedResource, pagingQuery);
		else
			authorizations = this.authorizationService.query(user, pagingQuery);

		return authorizations;
	}

	protected void setAssignedResourceAttributeIf(HttpServletRequest request, org.springframework.ui.Model model,
			ResourceMeta resourceMeta)
	{
		String ap = getAssignedResource(request);

		if (resourceMeta.mustAssignResource() && StringUtil.isEmpty(ap))
			throw new IllegalInputException();

		if (!StringUtil.isEmpty(ap))
			model.addAttribute("assignedResource", ap);
	}

	protected ResourceMeta setResourceMetaAttribute(org.springframework.ui.Model model, String resourceType)
	{
		ResourceMeta resourceMeta = AuthorizationResourceMetas.get(resourceType);

		if (resourceMeta == null)
			throw new IllegalInputException();

		model.addAttribute("resourceMeta", resourceMeta);

		return resourceMeta;
	}

	protected String getAssignedResource(HttpServletRequest request)
	{
		return request.getParameter(PARAM_ASSIGNED_RESOURCE);
	}

	protected void setAuthorizationQueryContext(HttpServletRequest request, ResourceMeta resourceMeta)
	{
		AuthorizationQueryContext context = new AuthorizationQueryContext();
		context.setPrincipalAllLabel(getMessage(request, "authorization.principalType.ALL"));
		context.setPrincipalAnonymousLabel(getMessage(request, "authorization.principalType.ANONYMOUS"));
		context.setResourceType(resourceMeta.getResourceType());

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
