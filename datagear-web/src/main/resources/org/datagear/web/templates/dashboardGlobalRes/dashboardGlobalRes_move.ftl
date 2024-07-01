<#--
 *
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
 *
-->
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dashboardGlobalRes' />
	-
	<@spring.message code='move' />
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<form id="${pid}form" class="flex flex-column" :class="{readonly: pm.isReadonlyAction}">
		<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
			<div class="field grid">
				<label for="${pid}directory" class="field-label col-12 mb-2 md:col-3 md:mb-0"
					title="<@spring.message code='dashboardGlobalRes.move.directory.desc' />">
					<@spring.message code='targetDirectory' />
				</label>
		        <div class="field-input col-12 md:col-9">
		        	<div class="p-inputgroup">
		        		<p-inputtext id="${pid}directory" v-model="fm.directory" type="text" class="input"
			        		name="directory" required maxlength="300" autofocus>
			        	</p-inputtext>
			        	<p-button type="button" label="<@spring.message code='select' />"
			        		@click="onSelectDir" v-if="!pm.isReadonlyAction">
			        	</p-button>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
			<p-button type="submit" label="<@spring.message code='save' />"></p-button>
		</div>
	</form>
</div>
<#include "../include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/dashboardGlobalRes/"+po.submitAction;

	var formModel = $.unescapeHtmlForJson(<@writeJson var=formModel />);
	po.setupForm(formModel);
	
	po.vueMethod(
	{
		onSelectDir: function()
		{
			po.handleOpenSelectAction("/dashboardGlobalRes/select?onlyDirectory=true", function(fileInfo)
			{
				var fm = po.vueFormModel();
				fm.directory = fileInfo.path;
			});
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>