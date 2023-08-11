<#--
 *
 * Copyright 2018-2023 datagear.tech
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
<#--
上传文件片段。
-->
<div class="fileupload-info text-color-secondary">
	<small class="file-name">{{pm.fileuploadInfo.name}}</small>
	<small class="upload-progress ml-2">{{pm.fileuploadInfo.progress}}</small>
</div>
<script>
(function(po)
{
	po.dftUploadSuccessMsg = "<@spring.message code='uploadSuccess' />";
	
	po.vuePageModel(
	{
		fileuploadInfo: { name: "", progress: "" }
	});
	
	po.uploadFileOnSelect = function(e)
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.name = (e.files && e.files[0] ? e.files[0].name : "");
	};
	
	po.uploadFileOnProgress = function(e)
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.progress = (e.progress >= 100 ? 99 : e.progress) +"%";
	};
	
	po.uploadFileOnUploaded = function(e, tip, tipMsg)
	{
		tip = (tip == null ? false : tip);
		
		var pm = po.vuePageModel();
		pm.fileuploadInfo.progress = "100%";
		
		if(tip)
		{
			if(!tipMsg)
			{
				var response = $.getResponseJson(e.xhr);
				tipMsg = (response && response.message ? response.message : po.dftUploadSuccessMsg);
			}
			
			$.tipSuccess(tipMsg);
		}
	};
	
	po.uploadFileOnError = function(e)
	{
		var om = $.getResponseJson(e.xhr);
		var message = "Error";
		
		if(om && om.message)
			message = om.message;
		
		$.tipError(message);
	};
	
	po.clearFileuploadInfo = function()
	{
		var pm = po.vuePageModel();
		pm.fileuploadInfo.name = "";
		pm.fileuploadInfo.progress = "";
	};
	
	po.vueMethod(
	{
		uploadFileOnSelect: function(e)
		{
			po.uploadFileOnSelect(e);
		},
		
		uploadFileOnProgress: function(e)
		{
			po.uploadFileOnProgress(e);
		},
		
		uploadFileOnError: function(e)
		{
			po.uploadFileOnError(e);
		}
	});
})
(${pid});
</script>
