<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Model model 模型，不允许为null
Object data 初始数据，允许null
boolean isClientPageData 初始数据是否是客户端数据，默认为false
String titleOperationMessageKey 标题操作标签I18N关键字，不允许null
String titleDisplayName 页面展示名称，默认为""
String submitAction 提交活动，po.pageParam().submit(...)未定义时，不允许为null
boolean readonly 是否只读操作，默认为false
String ignorePropertyName 忽略表单渲染和处理的属性名，默认为""
boolean batchSet 是否开启批量执行功能，默认为false
-->
<#assign isClientPageData=(isClientPageData!false)>
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign submitAction=(submitAction!'#')>
<#assign readonly=(readonly!false)>
<#assign ignorePropertyName=(ignorePropertyName!'')>
<#assign batchSet=(batchSet!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title>
	<#include "../include/html_title_app_name.ftl">
	<@spring.message code='${titleOperationMessageKey}' />
	<@spring.message code='titleSeparator' />
	${titleDisplayName?html}
</title>
</head>
<body>
<div id="${pageId}" class="page-form">
	<div class="head">
	</div>
	<div class="content">
		<form id="${pageId}-form" method="POST">
		</form>
	</div>
	<div class="foot">
	</div>
</div>
<#include "include/data_page_obj.ftl">
<#include "include/data_page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.data = <@writeJson var=data />;
	po.readonly = ${readonly?c};
	po.submitAction = "${submitAction?js_string}";
	po.isClientPageData = ${isClientPageData?c};
	po.batchSet = ${batchSet?c};
	
	if(!po.isClientPageData && po.data == null)
		po.isClientPageData = true;
	
	po.onTable(function(table)
	{
		po.form().tableform(
		{
			table : table,
			data : po.data,
			readonly : po.readonly,
			submit : function()
			{
				var formData = $(this).tableform("data");
				var formParam = $(this).tableform("param");
				
				var close = true;
				
				//父页面定义了submit回调函数，则优先执行
				if(po.pageParam("submit"))
				{
					close = (po.pageParamCall("submit", formData, formParam) != false);
					
					if(close && !$(this).tableform("isDialogPinned"))
						po.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = $.extend(formParam, {"data" : formData, "originalData" : po.data});
					
					po.ajaxSubmitForHandleDuplication(po.submitAction, param, "<@spring.message code='save.continueIgnoreDuplicationTemplate' />",
					{
						beforeSend : function()
						{
							$(thisForm).tableform("disableOperation");
						},
						success : function(operationMessage)
						{
							var $form = $(thisForm);
							var batchSubmit = $form.tableform("isBatchSubmit");
							var isDialogPinned = $form.tableform("isDialogPinned");
							
							$form.tableform("enableOperation");
							
							po.refreshParent();
							
							if(batchSubmit)
								;
							else
							{
								//如果有初值，则更新为后台已保存值（编辑时）；如果没有初值，则不更新（添加时）
								if(po.data)
									po.data = operationMessage.data;
								
								close = (po.pageParamCall("afterSave", operationMessage.data) != false);
								
								if(close && !isDialogPinned)
									po.close();
							}
						},
						error : function()
						{
							var $form = $(thisForm);
							var batchSubmit = $form.tableform("isBatchSubmit");
							
							$form.tableform("enableOperation");
							
							if(batchSubmit)
								po.refreshParent();
						}
					});
				}
				
				return false;
			},
			addSinglePropertyValue : function(property)
			{
				po.addSinglePropertyValue(property);
			},
			editSinglePropertyValue : function(property, propertyValue)
			{
				po.editSinglePropertyValue(property, propertyValue);
			},
			deleteSinglePropertyValue : function(property, propertyValue)
			{
				po.deleteSinglePropertyValue(property, propertyValue);
			},
			selectSinglePropertyValue : function(property, propertyValue)
			{
				po.selectSinglePropertyValue(property, propertyValue);
			},
			viewSinglePropertyValue : function(property, propertyValue)
			{
				po.viewSinglePropertyValue(property, propertyValue);
			},
			editMultiplePropertyValue : function(property, propertyValue)
			{
				po.editMultiplePropertyValue(property, propertyValue);
			},
			viewMultiplePropertyValue : function(property, propertyValue)
			{
				po.viewMultiplePropertyValue(property, propertyValue);
			},
			filePropertyUploadURL : "${contextPath}/data/file/upload",
			filePropertyDeleteURL : "${contextPath}/data/file/delete",
			columnValueDownloadUrl : function(property)
			{
				po.columnValueDownloadUrl(property);
			},
			validationRequiredAsAdd : ("saveAdd" == po.submitAction),
			batchSet : po.batchSet,
			labels : po.formLabels,
			dateFormat : "${sqlDateFormat}",
			timestampFormat : "${sqlTimestampFormat}",
			timeFormat : "${sqlTimeFormat}",
			lobValuePlaceholders:
			{
				blobPlaceholder: "${formDefaultLOBRowMapper.blobPlaceholder}",
				clobPlaceholder: "${formDefaultLOBRowMapper.clobPlaceholder}",
				sqlXmlPlaceholder: "${formDefaultLOBRowMapper.sqlXmlPlaceholder}"
			}
		});
	});
})
(${pageId});
</script>
</body>
</html>
