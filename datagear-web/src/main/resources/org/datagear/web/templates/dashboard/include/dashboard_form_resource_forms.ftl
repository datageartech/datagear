<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板资源表单面板HTML
注意：
这些HTML不能直接写在dashboard_form_resource.ftl内，
因为会出现嵌套form，这里面板里的form元素会被vue解析剔除
-->
<p-overlaypanel ref="${pid}addResPanelEle" append-to="body"
	@show="onAddResPanelShow" :show-close-icon="false" id="${pid}addResPanel">
	<form id="${pid}addResForm" action="#">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='createResource' />
			</label>
		</div>
		<div class="p-2">
			<div class="field grid">
				<label for="${pid}addResName" class="field-label col-12 mb-2"
					title="<@spring.message code='dashboard.addResName.desc' />">
					<@spring.message code='resourceName' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}addResName" v-model="pm.addResModel.resName" type="text"
						class="input w-full" name="resName" required maxlength="200">
					</p-inputtext>
				</div>
			</div>
		</div>
		<div class="pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		</div>
	</form>
</p-overlaypanel>
<p-overlaypanel ref="${pid}uploadResPanelEle" append-to="body"
	@show="onUploadResPanelShow" :show-close-icon="false" id="${pid}uploadResPanel">
	<form id="${pid}uploadResForm" action="#">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='uploadResource' />
			</label>
		</div>
		<div class="p-2 panel-content-size-xs-mwh overflow-auto">
			<div class="field grid">
				<label for="${pid}uploadResFile" class="field-label col-12 mb-2">
					<@spring.message code='file' />
				</label>
				<div class="field-input col-12">
					<div id="${pid}uploadResFile" class="fileupload-wrapper flex align-items-center">
						<p-fileupload mode="basic" name="file" :url="pm.uploadResModel.url"
			        		@upload="onResUploaded" @select="uploadFileOnSelect" @progress="uploadFileOnProgress"
			        		:auto="true" choose-label="<@spring.message code='select' />" class="mr-2">
			        	</p-fileupload>
						<#include "../../include/page_fileupload.ftl">
					</div>
		        	<div class="validate-msg">
		        		<input name="filePath" required type="text" class="validate-proxy" />
		        	</div>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}uploadResName" class="field-label col-12 mb-2"
					title="<@spring.message code='dashboard.uploadResSavePath.desc' />">
					<@spring.message code='savePath' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}uploadResName" v-model="pm.uploadResModel.savePath" type="text"
						class="input w-full validate-normalizer" name="savePath" required maxlength="200">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}uploadResAutoUnzip" class="field-label col-12 mb-2"
					title="<@spring.message code='dashboard.uploadResAutoUnzip.desc' />">
					<@spring.message code='autoUnzip' />
				</label>
		        <div class="field-input col-12">
		        	<p-selectbutton id="${pid}uploadResAutoUnzip" v-model="pm.uploadResModel.autoUnzip" :options="pm.booleanOptions"
		        		option-label="name" option-value="value" class="input w-full">
		        	</p-selectbutton>
		        </div>
			</div>
			<div class="field grid">
				<label for="${pid}uploadResZipFileNameEncoding" class="field-label col-12 mb-2"
					title="<@spring.message code='zipFileEncoding.uploadAutoUzip.desc' />">
					<@spring.message code='zipFileEncoding' />
				</label>
		        <div class="field-input col-12">
		        	<p-dropdown id="${pid}uploadResZipFileNameEncoding" v-model="pm.uploadResModel.zipFileNameEncoding"
		        		:options="pm.availableCharsetNames" class="input w-full">
		        	</p-dropdown>
		        	<div class="desc text-color-secondary">
		        		<small><@spring.message code='zipFileEncoding.uploadAutoUzip.notice' /></small>
		        	</div>
		        </div>
			</div>
		</div>
		<div class="pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='confirm' />">
			</p-button>
		</div>
	</form>
</p-overlaypanel>
<p-overlaypanel ref="${pid}renameResPanelEle" append-to="body"
	@show="onRenameResPanelShow" :show-close-icon="false" id="${pid}renameResPanel">
	<form id="${pid}renameResForm" action="#">
		<div class="pb-2">
			<label class="text-lg font-bold">
				<@spring.message code='rename' />
			</label>
		</div>
		<div class="p-2">
			<div class="field grid">
				<label for="${pid}srcResName" class="field-label col-12 mb-2">
					<@spring.message code='originalName' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}srcResName" v-model="pm.renameResModel.srcName" type="text"
						class="input w-full" name="srcName" readonly="readonly">
					</p-inputtext>
				</div>
			</div>
			<div class="field grid">
				<label for="${pid}destResName" class="field-label col-12 mb-2">
					<@spring.message code='newName' />
				</label>
				<div class="field-input col-12">
					<p-inputtext id="${pid}destResName" v-model="pm.renameResModel.destName" type="text"
						class="input w-full" name="destName" required maxlength="500">
					</p-inputtext>
				</div>
			</div>
        	<div class="desc text-color-secondary">
        		<small><@spring.message code='dashboard.renameRes.destName.desc' /></small>
        	</div>
		</div>
		<div class="pt-3 text-center">
			<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
		</div>
	</form>
</p-overlaypanel>