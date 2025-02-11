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
调色板
-->
<p-overlaypanel ref="${pid}palettePanelEle" append-to="body" id="${pid}palettePanel" @show="onPalettePanelShow">
	<div class="panel-content-size-mh30vh overflow-auto pr-2">
		<div class="flex flex-column" style="gap:1px;">
			<div v-for="(groupColors, groupIndex) in pm.palette.colors"
				class="flex flex-wrap" style="gap:1px;">
				<div v-for="(color, colorIndex) in groupColors.colors" :title="color"
					class="border-1 border-round-sm cursor-pointer" style="padding:1px;"
					:class="(pm.palette.value == color ? 'border-700' : 'surface-border hover:border-500')"
					@click="onSelectPaletteColor(color, index)">
					<div :style="{background: color}" style="width:1.2rem;height:1.2rem;" class="border-round-sm">
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="flex justify-content-between align-items-center gap-1 pt-2">
		<div class="flex align-items-center" style="gap:1px;">
			<div v-for="(color, colorIndex) in pm.palette.pureColors" :title="color"
				class="border-1 border-round-sm cursor-pointer" style="padding:1px;"
				:class="(pm.palette.value == color ? 'border-700' : 'surface-border hover:border-500')"
				@click="onSelectPaletteColor(color, index)">
				<div :style="{background: color}" style="width:1.2rem;height:1.2rem;" class="border-round-sm">
				</div>
			</div>
		</div>
		<div class="flex align-items-center">
			<div class="text-color-secondary">
				<small><@spring.message code='moreOfColon' /></small>
			</div>
			<div>
				<p-colorpicker v-model="pm.palette.pickerValue" :pt="{input:{'style':'padding:0.2rem;'}}"
					default-color="FFFFFF" class="flex-grow-0 preview-h-full"
					@change="onSelectPaletteColorPicker($event)">
				</p-colorpicker>
			</div>
		</div>
	</div>
</p-overlaypanel>
<script>
(function(po)
{
	//page.js
	$.inflatePagePalette(po);
})
(${pid});
</script>
