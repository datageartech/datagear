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