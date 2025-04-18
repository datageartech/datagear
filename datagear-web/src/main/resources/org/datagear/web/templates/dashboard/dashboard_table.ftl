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
<#assign HtmlTplDashboardWidgetEntity=statics['org.datagear.management.domain.HtmlTplDashboardWidgetEntity']>
<#assign AbstractDataAnalysisController=statics['org.datagear.web.controller.AbstractDataAnalysisController']>
<#include "../include/page_import.ftl">
<#include "../include/html_doctype.ftl">
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<@spring.message code='module.dashboard' />
	<#include "../include/html_request_action_suffix.ftl">
	<#include "../include/html_app_name_suffix.ftl">
</title>
</head>
<body class="p-card no-border h-screen m-0">
<#include "../include/page_obj.ftl">
<div id="${pid}" class="page page-manager page-table page-search-ap-aware h-full flex flex-column overflow-auto">
	<div class="page-header grid grid-nogutter align-items-center p-1 flex-grow-0">
		<div class="col-12 mb-1">
			<#include "../include/page_current_analysis_project.ftl">
		</div>
		<div class="col-12" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-4'">
			<#include "../include/page_search_form_filter.ftl">
		</div>
		<div class="operations col-12 flex gap-1 flex-wrap md:justify-content-end" :class="pm.isSelectAction ? 'md:col-6' : 'md:col-8'">
			<p-button label="<@spring.message code='confirm' />" @click="onSelect" v-if="pm.isSelectAction"></p-button>
			
			<p-splitbutton label="<@spring.message code='add' />" @click="onAdd" :model="pm.addBtnItems" v-if="!pm.isReadonlyAction"></p-splitbutton>
			<p-button label="<@spring.message code='edit' />" @click="onEdit" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='design' />" @click="onDesign" v-if="!pm.isReadonlyAction"></p-button>
			<div id="${pid}showBtnWrapper" class="inline-block white-space-nowrap flex-tieredmenu-wrapper">
				<p-splitbutton label="<@spring.message code='show' />" @click="onShow" :model="pm.showBtnItems" append-to="#${pid}showBtnWrapper" v-if="!pm.isSelectAction"></p-splitbutton>
			</div>
			<p-splitbutton label="<@spring.message code='share' />" @click="onShare" :model="pm.shareBtnItems" v-if="!pm.isReadonlyAction"></p-splitbutton>
			<p-button label="<@spring.message code='view' />" @click="onView" :class="{'p-button-secondary': pm.isSelectAction}"></p-button>
			<p-button label="<@spring.message code='export' />" @click="onExport" v-if="!pm.isReadonlyAction"></p-button>
			<p-button label="<@spring.message code='delete' />" @click="onDelete" class="p-button-danger" v-if="!pm.isReadonlyAction"></p-button>
		</div>
	</div>
	<div class="page-content flex-grow-1 overflow-auto">
		<p-datatable :value="pm.items" :scrollable="true" scroll-height="flex"
			:paginator="pm.paginator" :paginator-template="pm.paginatorTemplate" :first="pm.pageRecordIndex"
			:rows="pm.rowsPerPage" :current-page-report-template="pm.pageReportTemplate"
			:rows-per-page-options="pm.rowsPerPageOptions" :loading="pm.loading"
			:lazy="true" :total-records="pm.totalRecords" @page="onPaginator($event)"
			sort-mode="multiple" :multi-sort-meta="pm.multiSortMeta" @sort="onSort($event)"
			:resizable-columns="true" column-resize-mode="expand"
			v-model:selection="pm.selectedItems" :selection-mode="pm.selectionMode" data-key="id" striped-rows>
			<p-column :selection-mode="pm.selectionMode" :frozen="true" class="col-check"></p-column>
			<p-column field="id" header="<@spring.message code='id' />" class="col-id"></p-column>
			<p-column field="name" header="<@spring.message code='name' />" :sortable="true" class="col-name"></p-column>
			<!--<p-column field="version" header="<@spring.message code='version' />" :sortable="true" class="col-version"></p-column>-->
			<p-column field="analysisProject.name" header="<@spring.message code='ownerProject' />" :sortable="true" class="col-name"></p-column>
			<p-column field="createUser.realName" header="<@spring.message code='createUser' />" :sortable="true" class="col-user"></p-column>
			<p-column field="createTime" header="<@spring.message code='createTime' />" :sortable="true" class="col-datetime col-last"></p-column>
		</p-datatable>
		<#include "../include/page_copy_to_clipboard.ftl">
	</div>
	<#include "../include/page_foot.ftl">
</div>
<#include "../include/page_manager.ftl">
<#include "../include/page_table.ftl">
<script>
(function(po)
{
	po.serverURL = "${serverURL}";
	
	po.buildShowURL = function(id)
	{
		return po.concatContextPath("/dv/"+encodeURIComponent(id)+"/");
	};

	po.buildIframeNestCode = function(url)
	{
		url = $.addParam(url, "${AbstractDataAnalysisController.DASHBOARD_SHOW_PARAM_SAFE_SESSION}",
								"${AbstractDataAnalysisController.DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_1}");
		return "<iframe src=\""+ url +"\" style=\"width:100%;height:100%;border:0;\"></iframe>";
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
				label: "<@spring.message code='copy' />",
				command: function()
				{
					po.handleOpenOfAction("/dashboard/copy");
				}
			},
			{
				label: "<@spring.message code='import' />",
				command: function()
				{
					po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dashboard/import"));
				}
			}
		],
		shareBtnItems:
		[
			{
				label: "<@spring.message code='shareSet' />",
				command: function()
				{
					po.handleOpenOfAction("/dashboard/shareSet", { pageParam: { submitSuccess: function(){} } });
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
			},
			{
				label: "<@spring.message code='generateIframeNestCode' />",
				command: function()
				{
					po.executeOnSelect(function(entity)
					{
						var url = po.serverURL + po.buildShowURL(entity.id);
						var iframeCode = po.buildIframeNestCode(url);
						po.copyToClipboard(iframeCode);
					});
				}
			}
		]
	});
	
	po.vueMethod(
	{
		onAdd: function()
		{
			po.handleAddAction(po.addCurrentAnalysisProjectIdParam("/dashboard/add"));
		},
		
		onEdit: function()
		{
			po.handleOpenOfAction("/dashboard/edit");
		},
		
		onDesign: function()
		{
			po.executeOnSelect(function(entity)
			{
				window.open(po.concatContextPath("/dashboard/design/"+encodeURIComponent(entity.id)));
			});
		},
		
		onView: function()
		{
			po.handleOpenOfAction("/dashboard/view", {width: "90vw"});
		},

		onShare: function()
		{
			po.executeOnSelect(function(entity)
			{
				po.openTableDialog("/authorization/${HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE}/"+encodeURIComponent(entity.id)+"/manage");
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
		},
		
		onExport: function(e)
		{
			po.handleOpenOfAction("/dashboard/export", { target: "_blank" });
		}
	});
})
(${pid});
</script>
<#include "../include/page_vue_mount.ftl">
</body>
</html>