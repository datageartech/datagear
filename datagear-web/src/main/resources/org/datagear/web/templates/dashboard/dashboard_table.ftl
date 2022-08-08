<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#assign HtmlTplDashboardWidgetEntity=statics['org.datagear.management.domain.HtmlTplDashboardWidgetEntity']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_app_name_prefix.ftl">
	<@spring.message code='module.dashboard' />
	<#include "../include/html_request_action_suffix.ftl">
</title>
</head>
<body class="p-card no-border">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table">
	<div class="page-header grid align-items-center">
		<div class="col-12 md:col-3">
			<#include "../include/page_search_form.ftl">
		</div>
		<div class="h-opts col-12 md:col-9 text-right">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-splitbutton label="<@spring.message code='add' />" @click="onAdd" :model="pm.addBtnItems" v-if="!pm.isSelectAction"></p-splitbutton>
			<p-splitbutton label="<@spring.message code='edit' />" @click="onEdit" :model="pm.editBtnItems" v-if="!pm.isSelectAction"></p-splitbutton>
			<p-splitbutton label="<@spring.message code='show' />" @click="onShow" :model="pm.showBtnItems" v-if="!pm.isSelectAction"></p-splitbutton>
			<p-button label="<@spring.message code='view' />" @click="onView" :class="{'p-button-secondary': pm.isSelectAction}"></p-button>
			<p-button label="<@spring.message code='share' />" @click="onShare" v-if="!pm.isSelectAction"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isSelectAction"></p-button>
		</div>
	</div>
	<div class="page-content">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:paginator="pm.paginator" :paginator-template="pm.paginatorTemplate"
			:rows="pm.rowsPerPage" :current-page-report-template="pm.pageReportTemplate"
			:rows-per-page-options="pm.rowsPerPageOptions" :loading="pm.loading"
			:lazy="true" :total-records="pm.totalRecords" @page="onPaginator($event)"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" dataKey="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" class="col-id"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<p-column field="analysisProject.name" header="<@spring.message code='ownerProject' />" :sortable="true" class="col-owner-project"></p-column>
			<p-column field="createUser.realName" header="<@spring.message code='createUser' />" :sortable="true" class="col-user"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
	</div>
	<#include "../include/page_copy_to_clipboard.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.serverURL = "${serverURL}";
	
	po.buildShowURL = function(id)
	{
		return po.concatContextPath("/dashboard/show/"+encodeURIComponent(id)+"/");
	};
	
	po.setupAjaxTable("/dashboard/pagingQueryData",
	{
		multiSortMeta: [ {field: "createTime", order: -1} ]
	});

	po.vuePageModel(
	{
		addBtnItems:
		[
			{
				label: "<@spring.message code='addInNewWindow' />",
				command: function()
				{
					po.open(po.concatContextPath("/dashboard/add"), {target: "_blank"});
				}
			},
			{
				label: "<@spring.message code='copy' />",
				command: function()
				{
					
				}
			},
			{
				label: "<@spring.message code='copyInNewWindow' />",
				command: function()
				{
					
				}
			},
			{
				label: "<@spring.message code='import' />",
				command: function()
				{
					
				}
			}
		],
		editBtnItems:
		[
			{
				label: "<@spring.message code='editInNewWindow' />",
				command: function()
				{
					po.handleOpenOfAction("/dashboard/edit", {target: "_blank"});
				}
			}
		],
		showBtnItems:
		[
			{
				label: "<@spring.message code='copyShowUrl' />",
				command: function()
				{
					po.executeOnSelect(function(entity)
	 				{
						po.copyToClipboard(po.serverURL + po.buildShowURL(entity.id));
	 				});
				}
			}
		]
	});
	
	po.vueMethod(
	{
		onAdd: function()
		{
			po.handleAddAction("/dashboard/add", {width: "90vw"});
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/dashboard/edit", {width: "90vw"});
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/dashboard/view", {width: "90vw"});
		},

		onShare: function()
		{
			po.executeOnSelect(function(entity)
			{
				po.openTableDialog("/authorization/${HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(entity.id)+"/query");
			});
		},
		
		onDelete: function()
		{
			po.handleDeleteAction("/dashboard/delete");
		},
		
		onSelect: function()
		{
			po.handleSelectAction();
		},
		
		onShow: function(e)
		{
			po.executeOnSelect(function(entity)
			{
				window.open(po.buildShowURL(entity.id), entity.id);
			});
		}
	});
	
	po.vueMount();
})
(${pid});
</script>
</body>
</html>