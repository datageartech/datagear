<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign disableRoles=(disableRoles!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='${titleMessageKey}' /></title>
</head>
<body>
<#include "../include/page_obj.ftl" >
<div id="${pageId}" class="page-form page-form-user">
	<form id="${pageId}form" action="${contextPath}/user/${formAction}" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="${(user.id)!''}" />
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="${(user.name)!''}" required="required" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="off" />
				</div>
			</div>
			<#if !readonly>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="" <#if isAdd>required="required"</#if> maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.confirmPassword' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="confirmPassword" value="" <#if isAdd>required="required"</#if> maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="new-password" />
				</div>
			</div>
			</#if>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.realName' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="realName" value="${(user.realName)!''}" maxlength="50" class="ui-widget ui-widget-content ui-corner-all" autocomplete="off" />
				</div>
			</div>
			<#if !disableRoles>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.roles' /></label>
				</div>
				<div class="form-item-value">
					<div class="user-roles ui-widget ui-widget-content ui-corner-all input minor-list deletable-list">
					</div>
					<#if !readonly>
						<button class="selectUserRoleBtn" type="button"><@spring.message code='select' /></button>
					</#if>
				</div>
			</div>
			</#if>
			<#--
			禁用新建管理员账号功能
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='user.admin' /></label>
				</div>
				<div class="form-item-value">
					<div class="user-admin-radios">
					<label for="${pageId}-userAdminYes"><@spring.message code='yes' /></label>
		   			<input type="radio" id="${pageId}-userAdminYes" name="admin" value="1" <#if (user.admin)!false>checked="checked"</#if> />
					<label for="${pageId}-userAdminNo"><@spring.message code='no' /></label>
		   			<input type="radio" id="${pageId}-userAdminNo" name="admin" value="0" <#if !((user.admin)!false)>checked="checked"</#if> />
		   			</div>
				</div>
			</div>
			-->
		</div>
		<div class="form-foot">
			<#if !readonly>
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
			</#if>
		</div>
	</form>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.userRoles = <@writeJson var=userRoles />;
	
	po.initFormBtns();
	
	<#--
	禁用新建管理员账号功能
	po.element(".user-admin-radios").checkboxradiogroup();
	-->
	
	po.url = function(action)
	{
		return "${contextPath}/user/" + action;
	};
	
	po.element(".user-roles").on("click", ".delete-icon", function()
	{
		$(this).closest(".minor-list-item").remove();
	});
	
	po.renderRoles = function(roles)
	{
		roles = (roles || []);
		var $parent = po.element(".user-roles");
		
		for(var i=0; i<roles.length; i++)
			po.renderRole($parent, roles[i]);
	}
	
	po.renderRole = function($parent, role)
	{
		var exists = po.elementOfName("roleIds[]", $parent);
		for(var i=0; i<exists.length; i++)
		{
			if($(exists[i]).val() == role.id)
				return;
		}
		
		var $item = $("<div class='minor-list-item ui-widget ui-widget-content ui-corner-all' />").appendTo($parent);
		$("<input type='hidden' name='roleIds[]' />").attr("value", role.id).appendTo($item);
		
		<#if !readonly>
		$("<span class='delete-icon ui-icon ui-icon-close' title='<@spring.message code='delete' />' />").appendTo($item);
		</#if>
		
		$("<div class='item-content' />").text(role.name).appendTo($item);
	};
	
	po.element(".selectUserRoleBtn").click(function()
	{
		var options =
		{
			pageParam :
			{
				select : function(roles)
				{
					po.renderRoles(roles);
				}
			}
		};
		
		$.setGridPageHeightOption(options);
		
		po.open("${contextPath}/role/select?multiple", options);
	});
	
	po.validateAjaxJsonForm(
	{
		rules :
		{
			confirmPassword :
			{
				"equalTo" : po.elementOfName("password")
			}
		}
	},
	{
		handleData: function(data)
		{
			var confirmPassword = data.confirmPassword;
			data.confirmPassword = undefined;
			
			var roleIds = (data.roleIds || []);
			var roles = [];
			for(var i=0; i<roleIds.length; i++)
			{
				roles[i] = { "id": roleIds[i] };
			}
			data.roles = roles;
			data.roleIds = undefined;
			
			return { "user": data, "confirmPassword": confirmPassword };
		}
	});
	
	<#if !disableRoles>
	po.renderRoles(po.userRoles);
	</#if>
})
(${pageId});
</script>
</body>
</html>