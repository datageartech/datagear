<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据权限JS片段。

依赖：
page_obj.ftl
-->
<#assign __podpAuthorization=statics['org.datagear.management.domain.Authorization']>
<script type="text/javascript">
(function(po)
{
	po.canRead = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_READ_START} <= dataOrPermission;
	};
	
	po.canEdit = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_EDIT_START} <= dataOrPermission;
	};
	
	po.canDelete = function(dataOrPermission)
	{
		if(dataOrPermission == null)
			return false;
		
		if(dataOrPermission.dataPermission != undefined)
			dataOrPermission = dataOrPermission.dataPermission;
		
		return ${__podpAuthorization.PERMISSION_DELETE_START} <= dataOrPermission;
	};
	
	po.canAuthorize = function(entity, user)
	{
		if(user.admin)
			return true;
		
		if(user.anonymous)
			return false;
		
		if(!po.canDelete(entity))
			return false;
		
		if(!entity.createUser)
			return false;
		
		return entity.createUser.id == user.id;
	};
	
	po.toPermissionLabel = function(dataOrPermission)
	{
		if(po.canDelete(dataOrPermission))
			return "<@spring.message code='authorization.permission.DELETE' />";
		else if(po.canEdit(dataOrPermission))
			return "<@spring.message code='authorization.permission.EDIT' />";
		else if(po.canRead(dataOrPermission))
			return "<@spring.message code='authorization.permission.READ' />";
		else
			return "<@spring.message code='authorization.permission.NONE' />";
	};
})
(${pageId});
</script>
