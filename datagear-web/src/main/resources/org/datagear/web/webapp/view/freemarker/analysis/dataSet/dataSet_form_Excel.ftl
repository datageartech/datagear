<#include "../../include/import_global.ftl">
<#include "../../include/html_doctype.ftl">
<#--
titleMessageKey 标题标签I18N关键字，不允许null
formAction 表单提交action，允许为null
readonly 是否只读操作，允许为null
-->
<#assign formAction=(formAction!'#')>
<#assign readonly=(readonly!false)>
<#assign isAdd=(formAction == 'saveAdd')>
<html>
<head>
<#include "../../include/html_head.ftl">
<title><#include "../../include/html_title_app_name.ftl">
	<@spring.message code='${titleMessageKey}' /> - <@spring.message code='dataSet.dataSetType.Excel' />
</title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-dataSet page-form-dataSet-excel">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<#include "include/dataSet_form_html_name.ftl">
			<div class="workspace">
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excelFile' /></label>
					</div>
					<div class="form-item-value">
						<input type="hidden" id="${pageId}-originalFileName" value="${(dataSet.fileName)!''?html}" />
						<input type="hidden" name="fileName" value="${(dataSet.fileName)!''?html}" />
						<input type="text" name="displayName" value="${(dataSet.displayName)!''?html}" class="file-display-name ui-widget ui-widget-content" readonly="readonly" />
						<#if !readonly>
						<div class="fileinput-wrapper">
							<div class="ui-widget ui-corner-all ui-button fileinput-button"><@spring.message code='upload' /><input type="file"></div>
							<div class="upload-file-info"></div>
						</div>
						</#if>
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.sheetIndex.desc' />">
							<@spring.message code='dataSet.excel.sheetIndex' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="sheetIndex" value="${(dataSet.sheetIndex)!''?html}" class="ui-widget ui-widget-content" style="width:41%;" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.nameRow.desc' />">
							<@spring.message code='dataSet.excel.nameRow' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="hidden" name="nameRow" value="${(dataSet.nameRow)!''?html}" class="ui-widget ui-widget-content" />
						<span class="nameRow-radios">
							<label for="${pageId}-nameRow_0">
								<@spring.message code='dataSet.excel.nameRow.none' />
							</label>
				   			<input type="radio" id="${pageId}-nameRow_0" name="nameRowRadio" value="0" />
							<label for="${pageId}-nameRow_1">
								<@spring.message code='dataSet.excel.nameRow.assign' />
							</label>
				   			<input type="radio" id="${pageId}-nameRow_1" name="nameRowRadio" value="1"  />
						</span>
						&nbsp;
						<input type="text" name="nameRowText" value="${(dataSet.nameRow)!''?html}" class="ui-widget ui-widget-content" style="width:4.1em;" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.dataRowExp.desc' />">
							<@spring.message code='dataSet.excel.dataRowExp' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataRowExp" value="${(dataSet.dataRowExp)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label title="<@spring.message code='dataSet.excel.dataColumnExp.desc' />">
							<@spring.message code='dataSet.excel.dataColumnExp' />
						</label>
					</div>
					<div class="form-item-value">
						<input type="text" name="dataColumnExp" value="${(dataSet.dataColumnExp)!''?html}" class="ui-widget ui-widget-content" />
					</div>
				</div>
				<div class="form-item">
					<div class="form-item-label">
						<label><@spring.message code='dataSet.excel.forceXls' /></label>
					</div>
					<div class="form-item-value">
						<div id="${pageId}-forceXls">
							<label for="${pageId}-forceXls-true"><@spring.message code='yes' /></label>
							<input id="${pageId}-forceXls-true" type="radio" name="forceXls" value="true" />
							<label for="${pageId}-forceXls-false"><@spring.message code='no' /></label>
							<input id="${pageId}-forceXls-false" type="radio" name="forceXls" value="false" />
						</div>
					</div>
				</div>
				<#include "include/dataSet_form_html_wow.ftl" >
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<#if !readonly>
			<input type="submit" value="<@spring.message code='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<@spring.message code='reset' />" />
			<#else>
			<div class="form-foot-placeholder">&nbsp;</div>
			</#if>
		</div>
	</form>
	<#include "include/dataSet_form_html_preview_pvp.ftl" >
</div>
<#include "../../include/page_js_obj.ftl" >
<#include "../../include/page_obj_form.ftl">
<#include "include/dataSet_form_js.ftl">
<script type="text/javascript">
(function(po)
{
	po.dataSetProperties = <@writeJson var=dataSetProperties />;
	po.dataSetParams = <@writeJson var=dataSetParams />;
	
	$.initButtons(po.element());
	po.element(".nameRow-radios").controlgroup();
	po.element("#${pageId}-forceXls").buttonset();
	po.element("#${pageId}-forceXls-${((dataSet.forceXls)!true)?string('true', 'false')}").click();
	po.initWorkspaceHeight();
	po.initWorkspaceTabs();
	po.initDataSetPropertiesTable(po.dataSetProperties);
	po.initDataSetParamsTable(po.dataSetParams);
	po.initPreviewParamValuePanel();
	
	po.element("input[name='nameRowRadio']").on("change", function()
	{
		var radioVal = $(this).val();
		var $nameRow = po.element("input[name='nameRow']");
		var $nameRowText = po.element("input[name='nameRowText']");
		
		if(radioVal == "0")
		{
			$nameRow.val("0");
			$nameRowText.hide();
		}
		else
		{
			var myVal = parseInt($nameRowText.val());
			if(!myVal || myVal < 1)
				$nameRowText.val("1");
			
			$nameRowText.show();
		}
	});
	
	po.nameRowValue = function(value)
	{
		var $nameRow = po.element("input[name='nameRow']");
		var $nameRowText = po.element("input[name='nameRowText']");
		
		if(value === undefined)
		{
			var radioVal = po.element("input[name='nameRowRadio']:checked").val();
			
			if(radioVal == "0")
				return $nameRow.val();
			else
				return $nameRowText.val();
		}
		else
		{
			$nameRow.val(value);
			$nameRowText.val(value);
			
			po.element("input[name='nameRowRadio'][value='"+(value >= 1 ? 1 : 0)+"']").attr("checked", "checked").change();
		}
	};
	
	po.nameRowValue(${(dataSet.nameRow)!"1"});
	
	po.updatePreviewOptionsData = function()
	{
		var dataSet = po.previewOptions.data.dataSet;
		
		dataSet.fileName = po.element("input[name='fileName']").val();
		dataSet.sheetIndex = po.element("input[name='sheetIndex']").val();
		dataSet.nameRow = po.nameRowValue();
		dataSet.dataRowExp = po.element("input[name='dataRowExp']").val();
		dataSet.dataColumnExp = po.element("input[name='dataColumnExp']").val();
		dataSet.forceXls = po.element("input[name='forceXls']:checked").val();
		
		po.previewOptions.data.originalFileName = po.element("#${pageId}-originalFileName").val();
	};
	
	<#if formAction == 'saveEditForExcel'>
	//初始化预览数据，为po.isPreviewValueModified判断逻辑提供支持
	po.updatePreviewOptionsData();
	//编辑操作默认为预览成功
	po.previewSuccess(true);
	</#if>
	
	po.isPreviewValueModified = function()
	{
		var fileName = po.element("input[name='fileName']").val();
		var sheetIndex = po.element("input[name='sheetIndex']").val();
		var nameRow = po.nameRowValue();
		var dataRowExp = po.element("input[name='dataRowExp']").val();
		var dataColumnExp = po.element("input[name='dataColumnExp']").val();
		var forceXls = po.element("input[name='forceXls']:checked").val();
		
		var pd = po.previewOptions.data.dataSet;
		
		return (pd.fileName != fileName) || (pd.sheetIndex != sheetIndex)
			|| (pd.nameRow != nameRow) || (pd.dataRowExp != dataRowExp)
			|| (pd.dataColumnExp != dataColumnExp) || (pd.forceXls != forceXls);
	};
	
	po.previewOptions.url = po.url("previewExcel");
	po.previewOptions.beforePreview = function()
	{
		po.updatePreviewOptionsData();
		
		if(!this.data.dataSet.fileName)
			return false;
	};
	po.previewOptions.beforeRefresh = function()
	{
		if(!this.data.dataSet.fileName)
			return false;
	};
	
	po.initPreviewOperations();
	
	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };

	po.element(".fileinput-button").fileupload(
	{
		url : po.url("uploadFile"),
		paramName : "file",
		success : function(uploadResult, textStatus, jqXHR)
		{
			$.fileuploadsuccessHandlerForUploadInfo(po.fileUploadInfo(), false);
			po.element("input[name='fileName']").val(uploadResult.fileName);
			po.element("input[name='displayName']").val(uploadResult.displayName);
		}
	})
	.bind('fileuploadadd', function (e, data)
	{
		po.element("input[name='displayName']").val("");
		$.fileuploadaddHandlerForUploadInfo(e, data, po.fileUploadInfo());
	})
	.bind('fileuploadprogressall', function (e, data)
	{
		$.fileuploadprogressallHandlerForUploadInfo(e, data, po.fileUploadInfo());
	});
	
	$.validator.addMethod("dataSetExcelPreviewRequired", function(value, element)
	{
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	$.validator.addMethod("dataSetExcelDataRowExpRegex", function(value, element)
	{
		if(!value)
			return true;
		
		var regex = /[\d\-\,\s]$/g;
		
		return regex.test(value);
	});

	$.validator.addMethod("dataSetExcelDataColumnExpRegex", function(value, element)
	{
		if(!value)
			return true;
		
		var regex = /([A-Z]|[\-\,\s])$/g;
		
		return regex.test(value);
	});
	
	po.form().validate(
	{
		ignore : "",
		rules :
		{
			"name" : "required",
			"displayName" : {"required": true, "dataSetExcelPreviewRequired": true, "dataSetPropertiesRequired": true},
			"sheetIndex": {"required": true, "integer": true, "min": 1},
			"nameRowText": {"integer": true, "min": 1},
			"dataRowExp": {"dataSetExcelDataRowExpRegex": true},
			"dataColumnExp": {"dataSetExcelDataColumnExpRegex": true}
		},
		messages :
		{
			"name" : "<@spring.message code='validation.required' />",
			"displayName" :
			{
				"required": "<@spring.message code='validation.required' />",
				"dataSetExcelPreviewRequired": "<@spring.message code='dataSet.validation.previewRequired' />",
				"dataSetPropertiesRequired": "<@spring.message code='dataSet.validation.propertiesRequired' />"
			},
			"sheetIndex":
			{
				"required": "<@spring.message code='validation.required' />",
				"integer": "<@spring.message code='validation.integer' />",
				"min": "<@spring.message code='validation.min' />"
			},
			"nameRowText":
			{
				"integer": "<@spring.message code='validation.integer' />",
				"min": "<@spring.message code='validation.min' />"
			},
			"dataRowExp": {"dataSetExcelDataRowExpRegex": "<@spring.message code='dataSet.validation.excel.dataRowExp.regex' />"},
			"dataColumnExp": {"dataSetExcelDataColumnExpRegex": "<@spring.message code='dataSet.validation.excel.dataColumnExp.regex' />"}
		},
		submitHandler : function(form)
		{
			var formData = $.formToJson(form);
			formData["properties"] = po.getFormDataSetProperties();
			formData["params"] = po.getFormDataSetParams();
			formData["nameRow"] = po.nameRowValue();
			formData["nameRowRadio"] = undefined;
			formData["nameRowText"] = undefined;
			
			var originalFileName = po.element("#${pageId}-originalFileName").val();
			
			$.postJson("${contextPath}/analysis/dataSet/${formAction}?originalFileName="+originalFileName, formData,
			function(response)
			{
				po.pageParamCallAfterSave(true, response.data);
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
})
(${pageId});
</script>
</body>
</html>