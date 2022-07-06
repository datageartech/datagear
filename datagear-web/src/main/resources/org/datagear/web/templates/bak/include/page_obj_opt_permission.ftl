<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
操作权限JS片段。

依赖：
page_obj.ftl
-->
<script type="text/javascript">
(function(po)
{
	po.handlePermissionElement = function(currentUser, parentElement)
	{
		if(!currentUser)
			currentUser = po.currentUser;
		
		if(!parentElement)
			parentElement = null;
		
		if(!po.currentUser)
			throw new Error("[currentUser] must be set");
		
		var roles = (currentUser.roles || []);
		
		var showAnyEle = po.element("[show-any-role]", parentElement);
		showAnyEle.each(function()
		{
			var $this = $(this);
			var roleIds = po.attrValueRoles($this, "show-any-role");
			
			if(po.containsRoleId(roles, roleIds) || currentUser.admin)
				$this.show();
			else
				$this.hide();
		});

		var visibleAnyEle = po.element("[visible-any-role]", parentElement);
		visibleAnyEle.each(function()
		{
			var $this = $(this);
			var roleIds = po.attrValueRoles($this, "visible-any-role");
			
			if(po.containsRoleId(roles, roleIds) || currentUser.admin)
				$this.css("visibility", "");
			else
				$this.css("visibility", "hidden");
		});
		
		var showAllEle = po.element("[show-all-role]", parentElement);
		showAllEle.each(function()
		{
			var $this = $(this);
			var roleIds = po.attrValueRoles($this, "show-all-role");
			
			if(po.containsRoleId(roles, roleIds, true) || currentUser.admin)
				$this.show();
			else
				$this.hide();
		});
		
		var visibleAllEle = po.element("[visible-all-role]", parentElement);
		visibleAllEle.each(function()
		{
			var $this = $(this);
			var roleIds = po.attrValueRoles($this, "visible-all-role");
			
			if(po.containsRoleId(roles, roleIds, true) || currentUser.admin)
				$this.css("visibility", "");
			else
				$this.css("visibility", "hidden");
		});
	};
	
	po.attrValueRoles = function($ele, attrName)
	{
		var roles = $ele.attr(attrName);
		roles = (roles ? roles.split(",") : []);
		for(var i=0; i<roles.length; i++)
			roles[i] = $.trim(roles[i]);
		
		return roles;
	};
	
	//角色数组是否包含任意（默认）、全部roleIds
	po.containsRoleId = function(roles, roleIds, all)
	{
		roleIds = ($.isArray(roleIds) ? roleIds : [ roleIds ]);
		var containsCount = 0;
		
		for(var i=0; i<roleIds.length; i++)
		{
			var ri = roleIds[i];
			
			for(var j=0; j<roles.length; j++)
			{
				if(roles[j].id == ri)
				{
					containsCount++;
					break;
				}
			}
			
			if(all && containsCount <= i)
				return false;
		}
		
		return (all ? containsCount > 0 && containsCount == roleIds.length : containsCount > 0);
	};
})
(${pageId});
</script>
