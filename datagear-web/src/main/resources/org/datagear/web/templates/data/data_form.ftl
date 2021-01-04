<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Table table 表，不允许为null
Object data 初始数据，允许null
boolean dataIsClient 初始数据是否是客户端数据，默认为false
String titleOperationMessageKey 标题操作标签I18N关键字，不允许null
String titleDisplayName 页面展示名称，默认为""
String titleDisplayDesc 页面展示描述，默认为""
String submitAction 提交活动，po.pageParam().submit(...)未定义时，不允许为null
boolean readonly 是否只读操作，默认为false
String ignorePropertyName 忽略表单渲染和处理的属性名，默认为""
boolean batchSet 是否开启批量执行功能，默认为false
-->
<#assign dataIsClient=(dataIsClient!false)>
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign titleDisplayDesc=(titleDisplayDesc!'')>
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
	${titleDisplayName}
	<#if titleDisplayDesc != ''>
	<@spring.message code='bracketLeft' />
	${titleDisplayDesc}
	<@spring.message code='bracketRight' />
	</#if>
	<@spring.message code='bracketLeft' />
	${schema.title}
	<@spring.message code='bracketRight' />
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
	po.dataIsClient = ${dataIsClient?c};
	po.readonly = ${readonly?c};
	po.submitAction = "${submitAction?js_string}";
	po.batchSet = ${batchSet?c};
	
	if(!po.dataIsClient && po.data == null)
		po.dataIsClient = true;
	
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
				var batchParam = $(this).tableform("batchParam");
				
				var thisForm = this;
				var url = po.url(po.submitAction);
				if(batchParam && batchParam.batchCount)
				{
					url = $.addParam(url, "batchCount", batchParam.batchCount);
					if(batchParam.batchHandleErrorMode)
						url = $.addParam(url, "batchHandleErrorMode", batchParam.batchHandleErrorMode);
				}
				var param = (po.dataIsClient ? formData : {"data" : formData, "originalData" : po.data});
				
				po.ajaxSubmitForHandleDuplication(url, param, "<@spring.message code='save.continueIgnoreDuplicationTemplate' />",
				{
					beforeSend : function()
					{
						$(thisForm).tableform("disableOperation");
					},
					success : function(operationMessage)
					{
						var $form = $(thisForm);
						var batchSubmit = $form.tableform("isBatchSubmit");
						
						$form.tableform("enableOperation");
						
						if(batchSubmit)
							po.pageParamCallAfterSave(true);
						else
						{
							//更新操作成功后要更新页面初始数据，确保再次提交正确
							if(!po.dataIsClient)
								po.data = operationMessage.data;
							
							po.pageParamCallAfterSave(true, operationMessage.data);
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
				
				return false;
			},
			selectColumnValue : po.selectColumnValue,
			viewColumnValue: po.viewColumnValue,
			downloadColumnValue: po.downloadColumnValue,
			fileUploadUrl : "${contextPath}/data/uploadFile",
			fileDeleteUrl : "${contextPath}/data/deleteFile",
			validationRequiredAsAdd : ("saveAdd" == po.submitAction),
			batchSet : po.batchSet,
			labels : po.formLabels,
			dateFormat : "${sqlDateFormat}",
			timestampFormat : "${sqlTimestampFormat}",
			timeFormat : "${sqlTimeFormat}"
		});
	});
})
(${pageId});
</script>
</body>
</html>
