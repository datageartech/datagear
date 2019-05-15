<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
Schema schema 数据库，不允许为null
Model model 模型，不允许为null
Object data 初始数据，允许null
String propertyPath 属性名称，不允许null
Object propertyValue 初始属性值，可用于设置初始表单数据，允许为null
boolean isClientPageData 初始数据是否是客户端数据，默认为false
String titleOperationMessageKey 标题操作标签I18N关键字，不允许null
String titleDisplayName 页面展示名称，默认为""
String submitAction 提交活动，po.pageParam().submit(...)未定义时，不允许为null
boolean readonly 是否只读操作，默认为false
boolean batchSet 是否开启批量执行功能，默认为false
-->
<#assign isClientPageData=(isClientPageData!false)>
<#assign titleDisplayName=(titleDisplayName!'')>
<#assign submitAction=(submitAction!'#')>
<#assign readonly=(readonly!false)>
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
<div id="${pageId}" class="page-form page-form-propvalue">
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
	po.data = ($.unref(<@writeJson var=data />) || {});
	po.propertyPath = "${propertyPath?js_string}";
	po.propertyValue = ($.unref(<@writeJson var=propertyValue />) || $.model.propertyPathValue(po.data, po.propertyPath));
	po.readonly = ${readonly?c};
	po.submitAction = "${submitAction?js_string}";
	po.isClientPageData = ${isClientPageData?c};
	po.batchSet = ${batchSet?c};
	
	if(!po.isClientPageData && po.propertyValue == null)
		po.isClientPageData = true;
	
	po.superBuildPropertyActionOptions = po.buildPropertyActionOptions;
	po.buildPropertyActionOptions = function(property, propertyValue, extraRequestParams, extraPageParams)
	{
		var actionParam = po.superBuildPropertyActionOptions(property, propertyValue,
				extraRequestParams, extraPageParams);
		
		if(po.isClientPageData)
		{
			//客户端属性值数据则传递最新表单数据，因为不需要根据初始属性值数据到服务端数据库查找
			
			var data = $.deepClone(po.data);
			var formData = actionParam["data"]["data"];
			$.model.propertyPathValue(data, po.propertyPath, formData); 
			
			actionParam["data"]["data"] = data;
		}
		else
			actionParam["data"]["data"] = po.data;
		
		actionParam["data"]["propertyPath"] = $.propertyPath.concatPropertyName(po.propertyPath, property.name);
		
		return actionParam;
	};
	
	po.onModel(function(model)
	{
		var propertyInfo = $.model.getTailPropertyInfo(model, po.propertyPath);
		var property = propertyInfo.property;
		
		po.form().modelform(
		{
			model : property.model,
			ignorePropertyNames : $.model.findMappedByWith(property),
			data : $.deepClone(po.propertyValue),
			readonly : po.readonly,
			submit : function()
			{
				var propertyValue = $(this).modelform("data");
				var formParam = $(this).modelform("param");
				
				var close = true;
				
				//父页面定义了submit回调函数，则优先执行
				if(po.pageParam("submit"))
				{
					close = (po.pageParamCall("submit", propertyValue, formParam) != false);
					
					if(close && !$(this).modelform("isDialogPinned"))
						po.close();
				}
				//否则，POST至后台
				else
				{
					var thisForm = this;
					var param = $.extend(formParam, { "data" : po.data, "propertyPath" : po.propertyPath, "propertyValue" : propertyValue });
					
					po.ajaxSubmitForHandleDuplication(po.submitAction, param, "<@spring.message code='save.continueIgnoreDuplicationTemplate' />",
					{
						beforeSend : function()
						{
							$(thisForm).modelform("disableOperation");
						},
						success : function(operationMessage)
						{
							var $form = $(thisForm);
							var batchSubmit = $form.modelform("isBatchSubmit");
							var isDialogPinned = $form.modelform("isDialogPinned");
							
							$form.modelform("enableOperation");
							
							po.refreshParent();
							
							if(batchSubmit)
								;
							else
							{
								//如果有初始数据，则更新为已保存至后台的数据
								if(po.propertyValue != null)
								{
									$.model.propertyPathValue(po.data, po.propertyPath, operationMessage.data);
									po.propertyValue = operationMessage.data;
								}
								
								close = (po.pageParamCall("afterSave", operationMessage.data) != false);
								
								if(close && !isDialogPinned)
									po.close();
							}
						},
						error : function()
						{
							var $form = $(thisForm);
							var batchSubmit = $form.modelform("isBatchSubmit");

							$form.modelform("enableOperation");
							
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
			downloadSinglePropertyValueFile : function(property)
			{
				po.downloadSinglePropertyValueFile(property);
			},
			validationRequiredAsAdd : ("saveAdd" == po.submitAction),
			batchSet : po.batchSet,
			labels : po.formLabels,
			dateFormat : "${sqlDateFormat}",
			timestampFormat : "${sqlTimestampFormat}",
			timeFormat : "${sqlTimeFormat}",
			filePropertyLabelValue : "${filePropertyLabelValue}"
		});
	});
})
(${pageId});
</script>
</body>
</html>
