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
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.user' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}users" class="field-label col-12 mb-2 md:col-3 md:mb-0">
					<@spring.message code='module.user' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div id="${pid}users" class="input p-inputtext w-full border-noround-right overflow-auto" style="height:6rem;">
		        		<p-chip v-for="user in fm.users" :key="user.id" :label="user.name" class="mb-2" :removable @remove="onRemoveUser($event, user.id)"></p-chip>
		        	</div>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}migrateToName" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='user.migrateToUser.desc' />">
					<@spring.message code='migrateToUser' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
			        	<p-inputtext id="${pid}migrateToName" v-model="fm.migrateToName" type="text" class="input"
			        		name="migrateToName" required maxlength="50" readonly="readonly">
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />" @click="onSelectUser"
			        		class="p-button-secondary">
			        	</p-button>
			        </div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='delete' />" class="p-button-danger"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/user/deleteDo";
	
	po.submitFormParent = po.submitForm;
	po.submitForm = function(url, options)
	{
		po.confirm(
		{
			message: "<@spring.message code='confirmDeleteUserAsk' />",
			accept: function()
			{
				po.submitFormParent(url, options);
			} 
		});
	};
	
	po.beforeSubmitForm = function(action)
	{
		var data = action.options.data;
		
		data.ids = [];
		
		var users = (data.users || []);
		for(var i=0; i<users.length; i++)
			data.ids.push(users[i].id);
		
		data.users = undefined;
	};
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	formModel = { users: formModel };
	po.setupForm(formModel);
	
	po.vueMethod(
	{
		onRemoveUser: function(e, userId)
		{
			var fm = po.vueFormModel();
			var users = (fm.users || []);
			$.removeById(users, userId);
		},
		
		onSelectUser: function()
		{
			po.handleOpenSelectAction("/user/select", function(user)
			{
				var fm = po.vueFormModel();
				fm.migrateToId = user.id;
				fm.migrateToName = user.name;
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>