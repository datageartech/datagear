<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
导入选项片段

依赖：
page_boolean_options.ftl
dataexchange_js.ftl
import_js.ftl
-->
<div class="field grid">
	<label for="${pid}ignoreInexistentColumn" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='dataImport.ignoreInexistentColumn' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-selectbutton id="${pid}ignoreInexistentColumn" v-model="fm.importOption.ignoreInexistentColumn"
			:options="pm.booleanOptions" option-label="name" option-value="value" class="input w-full">
       	</p-selectbutton>
	</div>
</div>
<div class="field grid">
	<label for="${pid}nullForIllegalColumnValue" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='dataImport.nullForIllegalColumnValue' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-selectbutton id="${pid}nullForIllegalColumnValue" v-model="fm.importOption.nullForIllegalColumnValue"
			:options="pm.booleanOptions" option-label="name" option-value="value" class="input w-full">
       	</p-selectbutton>
	</div>
</div>
<div class="field grid">
	<label for="${pid}exceptionResolve" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='dataExchange.exceptionResolve' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-selectbutton id="${pid}exceptionResolve" v-model="fm.importOption.exceptionResolve"
			:options="pm.exceptionResolveOptions" option-label="name" option-value="value" class="input w-full">
       	</p-selectbutton>
	</div>
</div>