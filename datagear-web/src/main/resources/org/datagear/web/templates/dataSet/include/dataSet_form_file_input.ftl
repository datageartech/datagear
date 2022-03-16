<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
数据集表单页：文件类数据集片段
-->
<#assign dsffiDirectoryFileDataSetEntity=statics['org.datagear.management.domain.DirectoryFileDataSetEntity']>
<div class="ds-file-input-wrapper">
	<div class="fileSourceType-wrapper row-wrapper">
		<span class="fileSourceType-radios">
			<label for="${pageId}-fileSourceType_0">
				<@spring.message code='dataSet.FILE_SOURCE_TYPE_UPLOAD' />
			</label>
		 	<input type="radio" id="${pageId}-fileSourceType_0" name="fileSourceType" value="${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}" />
		 	
			<label for="${pageId}-fileSourceType_1">
				<@spring.message code='dataSet.FILE_SOURCE_TYPE_SERVER' />
			</label>
		 	<input type="radio" id="${pageId}-fileSourceType_1" name="fileSourceType" value="${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}"  />
		</span>
	</div>
	<div class="upload-file-input-wrapper form-item-value error-newline">
		<input type="hidden" id="${pageId}-originalFileName" value="${(dataSet.fileName)!''}" />
		
		<input type="hidden" name="fileName" value="${(dataSet.fileName)!''}" />
		
		<div class="row-wrapper">
			<input type="text" name="displayName" value="${(dataSet.displayName)!''}" class="file-display-name ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
			<a id="${pageId}-uploadDownloadLink" href="${contextPath}/dataSet/downloadFile?id=${(dataSet.id)!''}" target="_blank" class="link"><@spring.message code='download' /></a>
		</div>
		
		<#if !readonly>
		<div class="fileinput-wrapper row-wrapper">
			<div class="ui-widget ui-corner-all ui-button fileinput-button"><@spring.message code='upload' /><input type="file"></div>
			<div class="upload-file-info"></div>
		</div>
		</#if>
	</div>
	<div class="server-file-input-wrapper">
		<input type="hidden" id="${pageId}-originalServerDirectoryId" value="${(dataSet.dataSetResDirectory.id)!''}" />
		<input type="hidden" id="${pageId}-originalServerFileName" value="${(dataSet.dataSetResFileName)!''}" />
		
		<div class="row-wrapper form-item-value error-newline">
			<input type="hidden" name="dataSetResDirectory.id" value="${(dataSet.dataSetResDirectory.id)!''}" />
			<div class="label">
				<label title="<@spring.message code='dataSet.serverDirectory.desc' />" class="tip-label"><@spring.message code='dataSet.serverDirectory' /></label>
			</div>
			<input type="text" name="dataSetResDirectory.directory" value="${(dataSet.dataSetResDirectory.directory)!''}" class="ui-widget ui-widget-content ui-corner-all" readonly="readonly" />
			<#if !readonly>
			<button type="button" class="selectServerDirectoryBtn"><@spring.message code='select' /></button>
			</#if>
		</div>
		<div class="row-wrapper form-item-value error-newline form-item-value-server-file">
			<div class="label">
				<label title="<@spring.message code='dataSet.dataSetResFileName.desc' />" class="tip-label"><@spring.message code='dataSet.fileInDirectory' /></label>
			</div>
			<input type="text" name="dataSetResFileName" value="${(dataSet.dataSetResFileName)!''}" class="ui-widget ui-widget-content ui-corner-all" />
			<#if !readonly>
			<button type="button" auto-close-prevent="server-file-list-panel" class="selectServerFileBtn ui-button-icon-only">
				<span class="ui-button-icon ui-icon ui-icon-triangle-1-s"></span><span class="ui-button-icon-space"> </span>
				<@spring.message code='select' />
			</button>
			<div class="server-file-list-panel auto-close-panel ui-widget ui-widget-content ui-corner-all ui-widget-shadow ui-front">
				<div class="server-file-list-content"></div>
			</div>
			</#if>
			<#--
			服务端文件允许参数化文件名，所以没法下载
			<a id="${pageId}-serverDownloadLink" href="${contextPath}/dataSet/downloadFile?id=${(dataSet.id)!''}" target="_blank" class="link"><@spring.message code='download' /></a>
			-->
		</div>
	</div>
</div>
<script type="text/javascript">
(function(po)
{
	po.fileUploadInfo = function(){ return this.element(".upload-file-info"); };
	
	po.fileSourceTypeValue = function()
	{
		return po.element("input[name='fileSourceType']:checked").val();
	};
	
	po.isFileSourceTypeUpload = function(fileSourceType)
	{
		var value = (fileSourceType === undefined ? po.element("input[name='fileSourceType']:checked").val() : fileSourceType);
		return value == "${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}";
	};
	
	po.isFileSourceTypeServer = function(fileSourceType)
	{
		var value = (fileSourceType === undefined ? po.element("input[name='fileSourceType']:checked").val() : fileSourceType);
		return value == "${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_SERVER}";
	};
	
	po.initDataSetFileInput = function(uploadURL, fileSourceType, isAddOperation)
	{
		if(!fileSourceType)
			fileSourceType = "${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}";
		
		po.element(".fileSourceType-radios").controlgroup();
		
		po.element("input[name='fileSourceType']").on("change", function()
		{
			var radioVal = $(this).val();
			var $upload = po.element(".upload-file-input-wrapper");
			var $server = po.element(".server-file-input-wrapper");
			
			if(radioVal == "${dsffiDirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD}")
			{
				$upload.show();
				$server.hide();
				po.disableDataSetParamOperation(true);
			}
			else
			{
				$upload.hide();
				$server.show();
				po.disableDataSetParamOperation(false);
			}
		});
		
		po.element("input[name='fileSourceType']").each(function()
		{
			if($(this).val() == fileSourceType)
				$(this).attr("checked", "checked").change();
		});
		
		if(isAddOperation || po.element("#${pageId}-originalFileName").val() == "")
			po.element("#${pageId}-uploadDownloadLink").hide();
		
		po.element(".fileinput-button").fileupload(
		{
			url : uploadURL,
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
		
		po.element(".selectServerDirectoryBtn").click(function()
		{
			var options =
			{
				pageParam :
				{
					select : function(dataSetResDirectory)
					{
						po.element("input[name='dataSetResDirectory.id']").val(dataSetResDirectory.id);
						po.element("input[name='dataSetResDirectory.directory']").val(dataSetResDirectory.directory);
					}
				}
			};
			
			$.setGridPageHeightOption(options);
			
			po.open("${contextPath}/dataSetResDirectory/select", options);
		});
		
		po.element(".selectServerFileBtn").click(function()
		{
			var panel = po.element(".server-file-list-panel");
			
			if(panel.is(":hidden"))
			{
				var directoryId = po.element("input[name='dataSetResDirectory.id']").val();
				
				if(!directoryId)
					return;
				
				var content = $(".server-file-list-content", panel);
				
				content.empty();
				panel.show();
				
				$.getJSON("${contextPath}/dataSetResDirectory/listFiles", {"id" : directoryId}, function(fileInfos)
				{
					for(var i=0; i<fileInfos.length; i++)
					{
						$("<div class='server-file-item'></div>").attr("file-name", fileInfos[i].name)
							.html(fileInfos[i].name).appendTo(content);
					}
				});
			}
			else
				panel.hide();
		});
		
		po.element(".server-file-list-panel")
		.on("click", ".server-file-item", function()
		{
			var name = $(this).attr("file-name");
			po.element("input[name='dataSetResFileName']").val(name);
			po.element(".server-file-list-panel").hide();
		})
		.on("mouseenter", ".server-file-item", function()
		{
			$(this).addClass("ui-state-active");
		})
		.on("mouseleave", ".server-file-item", function()
		{
			$(this).removeClass("ui-state-active");
		});
		
		var height = po.element(".fileSourceType-wrapper").outerHeight(true);
		height += Math.max(po.element(".upload-file-input-wrapper").outerHeight(true), po.element(".server-file-input-wrapper").outerHeight(true));
		po.element(".ds-file-input-wrapper").height(height);
	};
	
	po.isPreviewDataFileValid = function(data)
	{
		var pd = data.dataSet;
		
		if(po.isFileSourceTypeUpload(pd.fileSourceType))
		{
			if(!pd.fileName)
				return false;
		}
		else if(po.isFileSourceTypeServer(pd.fileSourceType))
		{
			if(!pd.dataSetResDirectory || !pd.dataSetResDirectory.id)
				return false;
			
			if(!pd.dataSetResFileName)
				return false;
		}
	};
	
	$.validator.addMethod("dataSetUploadFileNameRequired", function(value, element)
	{
		if(!po.isFileSourceTypeUpload())
			return true;
		
		return (value && value != "");
	});
	
	$.validator.addMethod("dataSetServerDirectoryRequired", function(value, element)
	{
		if(!po.isFileSourceTypeServer())
			return true;
		
		return (value && value != "");
	});
	
	$.validator.addMethod("dataSetServerFileNameRequired", function(value, element)
	{
		if(!po.isFileSourceTypeServer())
			return true;
		
		return (value && value != "");
	});
	
	$.validator.addMethod("dataSetUploadFilePreviewRequired", function(value, element)
	{
		if(!po.isFileSourceTypeUpload())
			return true;
		
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
	
	$.validator.addMethod("dataSetServerFilePreviewRequired", function(value, element)
	{
		if(!po.isFileSourceTypeServer())
			return true;
		
		return !po.isPreviewValueModified() && po.previewSuccess();
	});
})
(${pageId});
</script>