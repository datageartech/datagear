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
<#--
数据格式片段
-->
<#assign DataFormat=statics['org.datagear.dataexchange.DataFormat']>
<div class="field grid">
	<label for="${pid}dateFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='dateFormat' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}dateFormat" v-model="fm.dataFormat.dateFormat" type="text" class="input w-full"
			name="dateFormat" required maxlength="100" autofocus>
		</p-inputtext>
	</div>
</div>
<div class="field grid">
	<label for="${pid}timeFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='timeFormat' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}timeFormat" v-model="fm.dataFormat.timeFormat" type="text" class="input w-full"
			name="timeFormat" required maxlength="100">
		</p-inputtext>
	</div>
</div>
<div class="field grid">
	<label for="${pid}timestampFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='timestampFormat' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}timestampFormat" v-model="fm.dataFormat.timestampFormat" type="text" class="input w-full"
			name="timestampFormat" required maxlength="100">
		</p-inputtext>
	</div>
</div>
<div class="field grid">
	<label for="${pid}numberFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='numberFormat' />
	</label>
	<div class="field-input col-12 md:col-9">
		<p-inputtext id="${pid}numberFormat" v-model="fm.dataFormat.numberFormat" type="text" class="input w-full"
			name="numberFormat" required maxlength="100">
		</p-inputtext>
	</div>
</div>
<div class="field grid">
	<label for="${pid}binaryFormat" class="field-label col-12 mb-2 md:col-3 md:mb-0">
		<@spring.message code='binaryFormat' />
	</label>
	<div class="field-input col-12 md:col-9">
		<div class="p-inputgroup">
			<p-inputtext id="${pid}binaryFormat" v-model="fm.dataFormat.binaryFormat" type="text" class="input"
				name="binaryFormat" required maxlength="100">
			</p-inputtext>
			<p-button type="button" label="<@spring.message code='binaryFormat.HEX' />"
				@click="onSetBinaryFormat('${DataFormat.BINARY_FORMAT_HEX}')" class="p-button-secondary">
			</p-button>
			<p-button type="button" label="<@spring.message code='binaryFormat.Base64' />"
				@click="onSetBinaryFormat('${DataFormat.BINARY_FORMAT_BASE64}')" class="p-button-secondary">
			</p-button>
			<p-button type="button" label="<@spring.message code='binaryFormat.NULL' />"
				@click="onSetBinaryFormat('${DataFormat.BINARY_FORMAT_NULL}')" class="p-button-secondary">
			</p-button>
		</div>
	</div>
</div>
<script>
(function(po)
{
	po.vuePageModel(
	{
		binaryFormatOptions:
		[
			{name: "<@spring.message code='binaryFormat.HEX' />", value: "${DataFormat.BINARY_FORMAT_HEX}"},
			{name: "<@spring.message code='binaryFormat.Base64' />", value: "${DataFormat.BINARY_FORMAT_BASE64}"},
			{name: "<@spring.message code='binaryFormat.NULL' />", value: "${DataFormat.BINARY_FORMAT_NULL}"}
		]
	});
	
	po.vueMethod(
	{
		onSetBinaryFormat: function(value)
		{
			var fm = po.vueFormModel();
			fm.dataFormat.binaryFormat = value;
		}
	});
})
(${pid});
</script>