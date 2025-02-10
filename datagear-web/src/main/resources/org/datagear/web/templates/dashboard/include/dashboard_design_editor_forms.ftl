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
看板编辑器表单面板HTML
注意：
这些HTML不能直接写在dashboard_design_editor.ftl内，
因为会出现嵌套form，这里面板里的form元素会被vue解析剔除

依赖：
page_boolean_options.ftl
page_palette.ftl
-->
<form id="${pid}visualEditorLoadForm" action="#" method="POST" style="display:none;">
	<input type="hidden" name="DG_EDIT_TEMPLATE" value="true" />
	<textarea name="DG_TEMPLATE_CONTENT"></textarea>
</form>

<p-dialog :header="pm.vepts.gridLayout" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.gridLayoutShown" @show="onVeGridLayoutPanelShow">
	<div class="page page-form">
		<form id="${pid}veGridLayoutForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veGridLayoutRows" class="field-label col-12 mb-2">
						<@spring.message code='rowCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutRows" v-model="pm.vepms.gridLayout.rows" type="text"
							class="help-target input w-full" name="rows" required maxlength="10" autofocus>
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="1">
								<@spring.message code='dashboard.veditor.gridLayout.rows.1r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2">
								<@spring.message code='dashboard.veditor.gridLayout.rows.2r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3">
								<@spring.message code='dashboard.veditor.gridLayout.rows.3r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="4">
								<@spring.message code='dashboard.veditor.gridLayout.rows.4r' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5">
								<@spring.message code='dashboard.veditor.gridLayout.rows.5r' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutRowHeightDivide" class="field-label col-12 mb-2">
						<@spring.message code='rowHeightDivide' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veGridLayoutRowHeightDivide">
							<p-selectbutton id="${pid}veGridLayoutRowHeightDivideSel" v-model="pm.vepms.gridLayout.rowHeightDivide"
								:options="pm.gridLayoutDivideOptions" option-label="name" option-value="value" class="input w-full">
							</p-selectbutton>
							<div v-if="pm.vepms.gridLayout.rowHeightDivide == 'custom'" class="mt-2">
								<div v-for="rnum in pm.vepms.gridLayout.rowHeights.length" class="field-input mb-2">
									<div class="flex align-items-center">
										<div class="px-3"><@spring.message code='row' /> {{rnum}}</div>
										<div class="flex-grow-1">
											<p-inputtext v-model="pm.vepms.gridLayout.rowHeights[rnum-1]" type="text"
												:name="'rowHeights['+(rnum-1)+']'"  class="help-target input w-full">
											</p-inputtext>
											<div class="p-buttonset mt-1 text-sm">
												<p-button type="button" class="help-src p-button-secondary" help-value="2em">
													2em
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="3em">
													3em
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="4em">
													4em
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="5em">
													5em
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="auto">
													auto
												</p-button>
											</div>
											<div class="p-buttonset text-sm" style="margin-top:1px;">
												<p-button type="button" class="help-src p-button-secondary" help-value="20%">
													20%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="30%">
													30%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="40%">
													40%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="50%">
													50%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="1fr">
													1fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="2fr">
													2fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="3fr">
													3fr
												</p-button>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutColumns" class="field-label col-12 mb-2">
						<@spring.message code='columnCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutColumns" v-model="pm.vepms.gridLayout.columns" type="text"
							class="help-target input w-full" name="columns" required maxlength="10">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="1">
								<@spring.message code='dashboard.veditor.gridLayout.columns.1c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2">
								<@spring.message code='dashboard.veditor.gridLayout.columns.2c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3">
								<@spring.message code='dashboard.veditor.gridLayout.columns.3c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="4">
								<@spring.message code='dashboard.veditor.gridLayout.columns.4c' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5">
								<@spring.message code='dashboard.veditor.gridLayout.columns.5c' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutColWidthDivide" class="field-label col-12 mb-2">
						<@spring.message code='columnWidthDivide' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veGridLayoutColWidthDivide">
							<p-selectbutton id="${pid}veGridLayoutColWidthDivideSel" v-model="pm.vepms.gridLayout.colWidthDivide"
								:options="pm.gridLayoutDivideOptions" option-label="name" option-value="value" class="input w-full">
							</p-selectbutton>
							<div v-if="pm.vepms.gridLayout.colWidthDivide == 'custom'" class="mt-2">
								<div v-for="rnum in pm.vepms.gridLayout.colWidths.length" class="field-input mb-2">
									<div class="flex align-items-center">
										<div class="px-3"><@spring.message code='column' /> {{rnum}}</div>
										<div class="flex-grow-1">
											<p-inputtext v-model="pm.vepms.gridLayout.colWidths[rnum-1]" type="text"
												:name="'colWidths['+(rnum-1)+']'" class="help-target input w-full">
											</p-inputtext>
											<div class="p-buttonset mt-1 text-sm">
												<p-button type="button" class="help-src p-button-secondary" help-value="20%">
													20%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="30%">
													30%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="40%">
													40%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="50%">
													50%
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="auto">
													auto
												</p-button>
											</div>
											<div class="p-buttonset text-sm" style="margin-top:1px;">
												<p-button type="button" class="help-src p-button-secondary" help-value="1fr">
													1fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="2fr">
													2fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="3fr">
													3fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="4fr">
													4fr
												</p-button>
												<p-button type="button" class="help-src p-button-secondary" help-value="5fr">
													5fr
												</p-button>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutRowGap" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.style.rowGap.desc' />">
						<@spring.message code='dashboard.veditor.style.rowGap' />
						<span class="text-color-secondary text-sm ml-1">row-gap</span>
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutRowGap" v-model="pm.vepms.gridLayout.rowGap" type="text"
							class="help-target input w-full" name="rowGap">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="3px">
								3px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5px">
								5px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="10px">
								10px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
								1rem
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
								2rem
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
								3rem
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veGridLayoutColumnGap" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.style.columnGap.desc' />">
						<@spring.message code='dashboard.veditor.style.columnGap' />
						<span class="text-color-secondary text-sm ml-1">column-gap</span>
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutColumnGap" v-model="pm.vepms.gridLayout.columnGap" type="text"
							class="help-target input w-full" name="columnGap">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="3px">
								3px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5px">
								5px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="10px">
								10px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
								1rem
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
								2rem
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
								3rem
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid" v-if="pm.veGridLayoutPanelShowFillParent">
					<label for="${pid}veGridLayoutFillParent" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.gridLayout.fillPage.desc' />">
						<@spring.message code='fillPage' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veGridLayoutFillParent" v-model="pm.vepms.gridLayout.fillParent" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.flexLayout" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.flexLayoutShown" @show="onVeFlexLayoutPanelShow">
	<div class="page page-form">
		<form id="${pid}veFlexLayoutForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veFlexLayoutItems" class="field-label col-12 mb-2">
						<@spring.message code='itemCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veFlexLayoutItems" v-model="pm.vepms.flexLayout.items" type="text"
							class="help-target input w-full" name="items" required maxlength="10" autofocus>
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="1">
								<@spring.message code='dashboard.veditor.flexLayout.items.1' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="2">
								<@spring.message code='dashboard.veditor.flexLayout.items.2' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="3">
								<@spring.message code='dashboard.veditor.flexLayout.items.3' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="4">
								<@spring.message code='dashboard.veditor.flexLayout.items.4' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="5">
								<@spring.message code='dashboard.veditor.flexLayout.items.5' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veFlexLayoutDirection" class="field-label col-12 mb-2">
						<@spring.message code='dashboard.veditor.style.flexDirection' />
						<span class="text-color-secondary text-sm ml-1">flex-direction</span>
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veFlexLayoutDirection" v-model="pm.vepms.flexLayout.direction" type="text"
							class="help-target input w-full" name="direction">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="row">
								<@spring.message code='dashboard.veditor.style.flexDirection.row' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="column">
								<@spring.message code='dashboard.veditor.style.flexDirection.column' />
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid" v-if="pm.veFlexLayoutPanelShowFillParent">
					<label for="${pid}veFlexLayoutFillParent" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.flexLayout.fillPage.desc' />">
						<@spring.message code='fillPage' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veFlexLayoutFillParent" v-model="pm.vepms.flexLayout.fillParent" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.hxtitle" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.hxtitleShown" @show="onVeHxtitlePanelShow">
	<div class="page page-form">
		<form id="${pid}veHxtitleForm" class="flex flex-column">
			<div class="page-form-content size-30vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veHxtitleType" class="field-label col-12 mb-2">
						<@spring.message code='type' />
					</label>
					<div class="field-input col-12">
						<p-dropdown id="${pid}veHxtitleType" v-model="pm.vepms.hxtitle.type" :options="pm.hxtitleTypeOptions"
							option-label="name" option-value="value" class="input w-full">
							<template #option="slotProps">
								<div v-html="formatHxtitleOptionLabel(slotProps.option)"></div>
							</template>
						</p-dropdown>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHxtitleContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}veHxtitleContent" v-model="pm.vepms.hxtitle.content"
							class="input w-full size-10vh" name="content" autofocus>
						</p-textarea>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHxtitleTextAlign" class="field-label col-12 mb-2">
						<@spring.message code='dashboard.veditor.style.textAlign' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHxtitleTextAlign" v-model="pm.vepms.hxtitle.textAlign" type="text"
							class="help-target input w-full" name="textAlign">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="left">
								<@spring.message code='dashboard.veditor.style.textAlign.left' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="center">
								<@spring.message code='dashboard.veditor.style.textAlign.center' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="right">
							<@spring.message code='dashboard.veditor.style.textAlign.right' />
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.textElement" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.textElementShown" @show="onVeTextElementPanelShow">
	<div class="page page-form">
		<form id="${pid}veTextElementForm" class="flex flex-column">
			<div class="page-form-content size-30vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veTextElementContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}veTextElementContent" v-model="pm.vepms.textElement.content"
							class="input w-full size-20vh" name="content" autofocus>
						</p-textarea>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.image" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.imageShown" @show="onVeImagePanelShow">
	<div class="page page-form">
		<form id="${pid}veImageForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veImageSrc" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.src.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageSrc" v-model="pm.vepms.image.src" type="text"
							class="input w-full" name="src" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veImageWdith" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.width.desc' />">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageWdith" v-model="pm.vepms.image.width" type="text"
							class="help-target input w-full" name="width">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="auto">
								auto
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veImageHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageHeight" v-model="pm.vepms.image.height" type="text"
							class="help-target input w-full" name="height">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="auto">
								auto
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.hyperlink" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.hyperlinkShown" @show="onVeHyperlinkPanelShow">
	<div class="page page-form">
		<form id="${pid}veHyperlinkForm" class="flex flex-column">
			<div class="page-form-content size-30vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veHyperlinkHref" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.hyperlink.href.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkHref" v-model="pm.vepms.hyperlink.href" type="text"
							class="input w-full" name="href" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHyperlinkContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkContent" v-model="pm.vepms.hyperlink.content" type="text"
							class="input w-full" name="content">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veHyperlinkTarget" class="field-label col-12 mb-2">
						<@spring.message code='target' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veHyperlinkTarget" v-model="pm.vepms.hyperlink.target" type="text"
							class="help-target input w-full" name="target">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="_blank">
								<@spring.message code='dashboard.veditor.hyperlink.target._blank' />
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="_self">
								<@spring.message code='dashboard.veditor.hyperlink.target._self' />
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.video" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.videoShown" @show="onVeVideoPanelShow">
	<div class="page page-form">
		<form id="${pid}veVideoForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veVideoSrc" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.src.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoSrc" v-model="pm.vepms.video.src" type="text"
							class="input w-full" name="src" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veVideoWdith" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.width.desc' />">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoWdith" v-model="pm.vepms.video.width" type="text"
							class="help-target input w-full" name="width">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veVideoHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoHeight" v-model="pm.vepms.video.height" type="text"
							class="help-target input w-full" name="height">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.dashboardSize" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.dashboardSizeShown" @show="onVeDashboardSizePanelShow">
	<div class="page page-form">
		<form id="${pid}veDashboardSizeForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veDashboardSizeWdith" class="field-label col-12 mb-2">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<div class="p-inputgroup">
							<p-inputtext id="${pid}veDashboardSizeWdith" v-model="pm.vepms.dashboardSize.width" type="text"
								class="input w-full" name="width">
							</p-inputtext>
							<span class="p-inputgroup-addon">px</span>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veDashboardSizeHeight" class="field-label col-12 mb-2">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<div class="p-inputgroup">
							<p-inputtext id="${pid}veDashboardSizeHeight" v-model="pm.vepms.dashboardSize.height" type="text"
								class="input w-full" name="height">
							</p-inputtext>
							<span class="p-inputgroup-addon">px</span>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veDashboardSizeScale" class="field-label col-12 mb-2">
						<@spring.message code='scale' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veDashboardSizeScale" v-model="pm.vepms.dashboardSize.scale" :options="pm.dashboardSizeScaleOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				<p-button type="button" label="<@spring.message code='resetToDefault' />"
					class="p-button-secondary" @click="onVeDashboardSizeResetToDft">
				</p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartAttrValues" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.chartAttrValuesShown" @show="onVeChartAttrValuesPanelShow">
	<div class="page page-form dashboard-form-chart-attr-values">
		<#include "../../chart/include/chart_attr_values_form.ftl">
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartOptions" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.chartOptionsShown" @show="onVeChartOptionsPanelShow">
	<div class="page page-form">
		<form id="${pid}veChartOptionsForm" class="flex flex-column">
			<div class="page-form-content size-50vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<div class="field-input col-12">
						<div id="${pid}veChartOptionsContent" class="code-editor-wrapper size-30vh input p-component p-inputtext w-full">
							<div id="${pid}veChartOptionsCodeEditor" class="code-editor"></div>
						</div>
			        	<div class="desc text-color-secondary">
			        		<small><@spring.message code='chartOptions.formatDesc' /></small>
			        	</div>
			        	<div class="desc text-color-secondary" v-if="!pm.veChartOptionsPanelForGlobal">
			        		<small><@spring.message code='dashboard.veditor.chartOptions.desc' /></small>
			        	</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				<p-button type="button" class="p-button-secondary" label="<@spring.message code='viewOriginalOptions' />"
					aria:haspopup="true" aria-controls="${pid}optionsOriginPanel" @click="onShowOptionsOriginPanel"
					v-if="!pm.veChartOptionsPanelForGlobal">
				</p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartTheme" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.chartThemeShown" @show="onVeChartThemePanelShow">
	<div class="page page-form">
		<form id="${pid}veChartThemeForm" class="flex flex-column">
			<div class="page-form-content size-30vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veChartThemeFgColor" class="field-label col-12 mb-2">
						<@spring.message code='fgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex">
							<p-inputtext id="${pid}veChartThemeFgColor" v-model="pm.vepms.chartTheme.color" type="text"
								class="input flex-grow-1 mr-1" name="color" autofocus>
							</p-inputtext>
							<p-button type="button" :style="{'background-color': pm.vepms.chartTheme.color}" class="palette-btn surface-border"
								@click="showPalettePanel($event, pm.vepms.chartTheme, 'color')"></p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeBgColor" class="field-label col-12 mb-2">
						<@spring.message code='bgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex">
							<p-inputtext id="${pid}veChartThemeBgColor" v-model="pm.vepms.chartTheme.backgroundColor" type="text"
								class="input flex-grow-1 mr-1" name="backgroundColor">
							</p-inputtext>
							<p-button type="button" :style="{'background-color': pm.vepms.chartTheme.backgroundColor}" class="palette-btn surface-border"
								@click="showPalettePanel($event, pm.vepms.chartTheme, 'backgroundColor')"></p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeActualBgColor" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.chartTheme.actualBgColor.desc' />">
						<@spring.message code='actualBgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex">
							<p-inputtext id="${pid}veChartThemeActualBgColor" v-model="pm.vepms.chartTheme.actualBackgroundColor" type="text"
								class="input flex-grow-1 mr-1" name="actualBackgroundColor">
							</p-inputtext>
							<p-button type="button" :style="{'background-color': pm.vepms.chartTheme.actualBackgroundColor}" class="palette-btn surface-border"
								@click="showPalettePanel($event, pm.vepms.chartTheme, 'actualBackgroundColor')"></p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeFontSize" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.chartTheme.fontSize.desc' />">
						<@spring.message code='fontSize' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veChartThemeFontSize" v-model="pm.vepms.chartTheme.fontSize" type="text"
							class="input w-full" name="fontSize">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeGraphColors" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.chartTheme.graphColors.desc' />">
						<@spring.message code='dashboard.veditor.chartTheme.graphColors' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veChartThemeGraphColors" class="">
							<div v-for="(gc, gcIdx) in pm.vepms.chartTheme.graphColors" :key="gcIdx">
								<div class="flex mb-1 gap-2">
									<div class="flex-grow-1 flex">
										<p-inputtext v-model="pm.vepms.chartTheme.graphColors[gcIdx]" type="text"
											class="input flex-grow-1 mr-1" name="graphColors">
										</p-inputtext>
										<p-button type="button" :style="{'background-color': pm.vepms.chartTheme.graphColors[gcIdx]}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.vepms.chartTheme.graphColors, gcIdx)"></p-button>
									</div>
									<div class="flex gap-1">
										<p-button type="button" icon="pi pi-plus" severity="secondary"
											@click="onVeChartThemeInsertGraphColor($event, gcIdx)">
										</p-button>
										<p-button type="button" icon="pi pi-minus" severity="danger"
											@click="onVeChartThemeRemoveGraphColor($event, gcIdx)">
										</p-button>
									</div>
								</div>
							</div>
						</div>
						<div class="mt-1">
							<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onVeChartThemeAddGraphColor"></p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeGraphRangeColors" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.chartTheme.graphRangeColors.desc' />">
						<@spring.message code='dashboard.veditor.chartTheme.graphRangeColors' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veChartThemeGraphRangeColors" class="">
							<div v-for="(gc, gcIdx) in pm.vepms.chartTheme.graphRangeColors" :key="gcIdx">
								<div class="flex mb-1 gap-2">
									<div class="flex-grow-1 flex">
										<p-inputtext v-model="pm.vepms.chartTheme.graphRangeColors[gcIdx]" type="text"
											class="input flex-grow-1 mr-1" name="graphRangeColors">
										</p-inputtext>
										<p-button type="button" :style="{'background-color': pm.vepms.chartTheme.graphRangeColors[gcIdx]}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.vepms.chartTheme.graphRangeColors, gcIdx)"></p-button>
									</div>
									<div class="flex gap-1">
										<p-button type="button" icon="pi pi-plus" severity="secondary"
											@click="onVeChartThemeInsertGraphRangeColor($event, gcIdx)">
										</p-button>
										<p-button type="button" icon="pi pi-minus" severity="danger"
											@click="onVeChartThemeRemoveGraphRangeColor($event, gcIdx)">
										</p-button>
									</div>
								</div>
							</div>
						</div>
						<div class="mt-1">
							<p-button type="button" icon="pi pi-plus" severity="secondary" @click="onVeChartThemeAddGraphRangeColor"></p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.style" append-to="body"
	position="center" :modal="true"  class="dashboard-ve-style-panel"
	v-model:visible="pm.vepss.styleShown" @show="onVeStylePanelShow">
	<div class="page page-form">
		<form id="${pid}veStyleForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<p-tabview v-model:active-index="pm.veStyleTabviewActiveIndex" class="light-tabview">
					<p-tabpanel header="<@spring.message code='color' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleColor" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.color.desc' />">
									<@spring.message code='dashboard.veditor.style.color' />
									<span class="text-color-secondary text-sm ml-1">color</span>
								</label>
								<div class="field-input col-12">
									<div class="flex">
										<p-inputtext id="${pid}veStyleColor" v-model="pm.vepms.style.color" type="text"
											class="input flex-grow-1 mr-1" name="color" autofocus>
										</p-inputtext>
										<p-button type="button" :style="{'background-color': pm.vepms.style.color}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.vepms.style, 'color')"></p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgColor" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.bgColor.desc' />">
									<@spring.message code='dashboard.veditor.style.bgColor' />
									<span class="text-color-secondary text-sm ml-1">background-color</span>
								</label>
								<div class="field-input col-12">
									<div class="flex">
										<p-inputtext id="${pid}veStyleBgColor" v-model="pm.vepms.style['background-color']" type="text"
											class="input flex-grow-1 mr-1" name="background-color">
										</p-inputtext>
										<p-button type="button" :style="{'background-color':pm.vepms.style['background-color']}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.vepms.style, 'background-color')"></p-button>
									</div>
								</div>
							</div>
							<p-divider align="center">
								<label class="text-lg font-bold"><@spring.message code='bgImage' /></label>
							</p-divider>
							<div class="field grid">
								<label for="${pid}veStyleBgImage" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.bgImage.desc' />">
									<@spring.message code='dashboard.veditor.style.bgImage' />
									<span class="text-color-secondary text-sm ml-1">background-image</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBgImage" v-model="pm.vepms.style['background-image']" type="text"
										class="input w-full" name="background-image">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgSize" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.bgSize' />
									<span class="text-color-secondary text-sm ml-1">background-size</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBgSize" v-model="pm.vepms.style['background-size']" type="text"
										class="help-target input w-full" name="background-size">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="100% 100%">
											<@spring.message code='dashboard.veditor.style.bgSize.fill' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="100% auto">
											<@spring.message code='dashboard.veditor.style.bgSize.fill-x' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="auto 100%">
											<@spring.message code='dashboard.veditor.style.bgSize.fill-y' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="auto auto">
											<@spring.message code='dashboard.veditor.style.bgSize.oirgin' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgRepeat" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.bgRepeat' />
									<span class="text-color-secondary text-sm ml-1">background-repeat</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBgRepeat" v-model="pm.vepms.style['background-repeat']" type="text"
										class="help-target input w-full" name="background-repeat">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="no-repeat">
											<@spring.message code='dashboard.veditor.style.bgRepeat.no-repeat' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat">
											<@spring.message code='dashboard.veditor.style.bgRepeat.repeat' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat-x">
											<@spring.message code='dashboard.veditor.style.bgRepeat.repeat-x' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat-y">
											<@spring.message code='dashboard.veditor.style.bgRepeat.repeat-y' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgPosition" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.bgPosition' />
									<span class="text-color-secondary text-sm ml-1">background-position</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBgPosition" v-model="pm.vepms.style['background-position']" type="text"
										class="help-target input w-full" name="background-position">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="center center">
											<@spring.message code='dashboard.veditor.style.bgPosition.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="left top">
											<@spring.message code='dashboard.veditor.style.bgPosition.leftTop' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="right top">
											<@spring.message code='dashboard.veditor.style.bgPosition.rightTop' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="left bottom">
											<@spring.message code='dashboard.veditor.style.bgPosition.leftBottom' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="right bottom">
											<@spring.message code='dashboard.veditor.style.bgPosition.rightBottom' />
										</p-button>
									</div>
								</div>
							</div>
							<p-divider align="center">
								<label class="text-lg font-bold"><@spring.message code='border' /></label>
							</p-divider>
							<div class="field grid">
								<label for="${pid}veStyleBorderWidth" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.borderWidth.desc' />">
									<@spring.message code='dashboard.veditor.style.borderWidth' />
									<span class="text-color-secondary text-sm ml-1">border-width</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBorderWidth" v-model="pm.vepms.style['border-width']" type="text"
										class="help-target input w-full" name="border-width">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="1px">
										<@spring.message code='dashboard.veditor.style.borderWidth.1px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2px">
										<@spring.message code='dashboard.veditor.style.borderWidth.2px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
										<@spring.message code='dashboard.veditor.style.borderWidth.3px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="4px">
										<@spring.message code='dashboard.veditor.style.borderWidth.4px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
										<@spring.message code='dashboard.veditor.style.borderWidth.5px' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBorderColor" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.borderColor.desc' />">
									<@spring.message code='dashboard.veditor.style.borderColor' />
									<span class="text-color-secondary text-sm ml-1">border-color</span>
								</label>
								<div class="field-input col-12">
									<div class="flex">
										<p-inputtext id="${pid}veStyleBorderColor" v-model="pm.vepms.style['border-color']" type="text"
											class="input flex-grow-1 mr-1" name="border-color">
										</p-inputtext>
										<p-button type="button" :style="{'background-color': pm.vepms.style['border-color']}" class="palette-btn surface-border"
											@click="showPalettePanel($event, pm.vepms.style, 'border-color')"></p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBorderStyle" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.borderStyle' />
									<span class="text-color-secondary text-sm ml-1">border-style</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBorderStyle" v-model="pm.vepms.style['border-style']" type="text"
										class="help-target input w-full" name="border-style">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="solid">
											<@spring.message code='dashboard.veditor.style.borderStyle.solid' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="dotted">
											<@spring.message code='dashboard.veditor.style.borderStyle.dotted' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="dashed">
											<@spring.message code='dashboard.veditor.style.borderStyle.dashed' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBorderRadius" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.borderRadius.desc' />">
									<@spring.message code='dashboard.veditor.style.borderRadius' />
									<span class="text-color-secondary text-sm ml-1">border-radius</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBorderRadius" v-model="pm.vepms.style['border-radius']" type="text"
										class="help-target input w-full" name="border-radius">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											<@spring.message code='dashboard.veditor.style.borderRadius.3px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											<@spring.message code='dashboard.veditor.style.borderRadius.5px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											<@spring.message code='dashboard.veditor.style.borderRadius.10px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="30px">
											<@spring.message code='dashboard.veditor.style.borderRadius.30px' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="50px">
											<@spring.message code='dashboard.veditor.style.borderRadius.50px' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBoxShadow" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.boxShadow.desc' />">
									<@spring.message code='dashboard.veditor.style.boxShadow' />
									<span class="text-color-secondary text-sm ml-1">box-shadow</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBoxShadow" v-model="pm.vepms.style['box-shadow']" type="text"
										class="help-target input w-full" name="box-shadow">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="0px 0px 6px 4px #666">
											<@spring.message code='dashboard.veditor.style.boxShadow.around' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="-4px -4px 6px 4px #666">
											<@spring.message code='dashboard.veditor.style.boxShadow.lt' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="4px -4px 6px 4px #666">
											<@spring.message code='dashboard.veditor.style.boxShadow.rt' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="4px 4px 6px 4px #666">
											<@spring.message code='dashboard.veditor.style.boxShadow.rb' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="-4px 4px 6px 4px #666">
											<@spring.message code='dashboard.veditor.style.boxShadow.lb' />
										</p-button>
									</div>
								</div>
							</div>
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='size' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleDisplay" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.display' />
									<span class="text-color-secondary text-sm ml-1">display</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleDisplay" v-model="pm.vepms.style['display']" type="text"
										class="help-target input w-full" name="display">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="grid">
											<@spring.message code='dashboard.veditor.style.display.grid' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="inline-grid">
											<@spring.message code='dashboard.veditor.style.display.inline-grid' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="flex">
											<@spring.message code='dashboard.veditor.style.display.flex' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="inline-flex">
											<@spring.message code='dashboard.veditor.style.display.inline-flex' />
										</p-button>
									</div>
									<div class="p-buttonset text-sm" style="margin-top:1px;">
										<p-button type="button" class="help-src p-button-secondary" help-value="block">
											<@spring.message code='dashboard.veditor.style.display.block' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="inline-block">
											<@spring.message code='dashboard.veditor.style.display.inline-block' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="inline">
											<@spring.message code='dashboard.veditor.style.display.inline' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleWdith" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.width.desc' />">
									<@spring.message code='dashboard.veditor.style.width' />
									<span class="text-color-secondary text-sm ml-1">width</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleWdith" v-model="pm.vepms.style['width']" type="text"
										class="help-target input w-full" name="width">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="25%">
											25%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="50%">
											50%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="75%">
											75%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="100%">
											100%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="auto">
											auto
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleHeight" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.height.desc' />">
									<@spring.message code='dashboard.veditor.style.height' />
									<span class="text-color-secondary text-sm ml-1">height</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleHeight" v-model="pm.vepms.style['height']" type="text"
										class="help-target input w-full" name="height">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="50px">
											50px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="100px">
											100px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="300px">
											300px
										</p-button>
									</div>
									<div class="p-buttonset text-sm" style="margin-top:1px;">
										<p-button type="button" class="help-src p-button-secondary" help-value="25%">
											25%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="50%">
											50%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="75%">
											75%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="100%">
											100%
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="auto">
											auto
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStylePadding" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.padding.desc' />">
									<@spring.message code='dashboard.veditor.style.padding' />
									<span class="text-color-secondary text-sm ml-1">padding</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStylePadding" v-model="pm.vepms.style['padding']" type="text"
										class="help-target input w-full" name="padding">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="0 1rem">
											0 1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem 0">
											1rem 0
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleMargin" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.margin.desc' />">
									<@spring.message code='dashboard.veditor.style.margin' />
									<span class="text-color-secondary text-sm ml-1">margin</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleMargin" v-model="pm.vepms.style['margin']" type="text"
										class="help-target input w-full" name="margin">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="0 1rem">
											0 1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem 0">
											1rem 0
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleOverflow" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.overflow.desc' />">
									<@spring.message code='dashboard.veditor.style.overflow' />
									<span class="text-color-secondary text-sm ml-1">overflow</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleOverflow" v-model="pm.vepms.style['overflow']" type="text"
										class="help-target input w-full" name="overflow">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="auto">
											<@spring.message code='dashboard.veditor.style.overflow.auto' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="hidden">
											<@spring.message code='dashboard.veditor.style.overflow.hidden' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="visible">
											<@spring.message code='dashboard.veditor.style.overflow.visible' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBoxSizing" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.boxSizing.desc' />">
									<@spring.message code='dashboard.veditor.style.boxSizing' />
									<span class="text-color-secondary text-sm ml-1">box-sizing</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBoxSizing" v-model="pm.vepms.style['box-sizing']" type="text"
										class="help-target input w-full" name="box-sizing">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="border-box">
											<@spring.message code='dashboard.veditor.style.boxSizing.border-box' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="content-box">
											<@spring.message code='dashboard.veditor.style.boxSizing.content-box' />
										</p-button>
									</div>
								</div>
							</div>
							<p-divider align="center">
								<label class="text-lg font-bold"><@spring.message code='position' /></label>
							</p-divider>
							<div class="field grid">
								<label for="${pid}veStylePosition" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.position' />
									<span class="text-color-secondary text-sm ml-1">position</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStylePosition" v-model="pm.vepms.style['position']" type="text"
										class="help-target input w-full" name="position">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="absolute">
											<@spring.message code='dashboard.veditor.style.position.absolute' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="relative">
											<@spring.message code='dashboard.veditor.style.position.relative' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="fixed">
											<@spring.message code='dashboard.veditor.style.position.fixed' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="static">
											<@spring.message code='dashboard.veditor.style.position.static' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleLeft" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.left.desc' />">
									<@spring.message code='dashboard.veditor.style.left' />
									<span class="text-color-secondary text-sm ml-1">left</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleLeft" v-model="pm.vepms.style['left']" type="text"
										class="input w-full" name="left">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleTop" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.top.desc' />">
									<@spring.message code='dashboard.veditor.style.top' />
									<span class="text-color-secondary text-sm ml-1">top</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleTop" v-model="pm.vepms.style['top']" type="text"
										class="input w-full" name="top">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleRight" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.right.desc' />">
									<@spring.message code='dashboard.veditor.style.right' />
									<span class="text-color-secondary text-sm ml-1">right</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleRight" v-model="pm.vepms.style['right']" type="text"
										class="input w-full" name="right">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBottom" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.bottom.desc' />">
									<@spring.message code='dashboard.veditor.style.bottom' />
									<span class="text-color-secondary text-sm ml-1">bottom</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleBottom" v-model="pm.vepms.style['bottom']" type="text"
										class="input w-full" name="bottom">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleZindex" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.zindex.desc' />">
									<@spring.message code='dashboard.veditor.style.zindex' />
									<span class="text-color-secondary text-sm ml-1">z-index</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleZindex" v-model="pm.vepms.style['z-index']" type="text"
										class="input w-full" name=z-index>
									</p-inputtext>
								</div>
							</div>
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='gridLayout' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<p-divider align="center" class="mb-1">
								<label class="text-lg font-bold">
									<@spring.message code='gridContainer' />
								</label>
							</p-divider>
							<div class="text-color-secondary text-sm text-center mb-3">
								<@spring.message code='dashboard.veditor.styleSubType.gridContainer.desc' />
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridTemplateRows" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridTemplateRows.desc' />">
									<@spring.message code='dashboard.veditor.style.gridTemplateRows' />
									<span class="text-color-secondary text-sm ml-1">grid-template-rows</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridTemplateRows" v-model="pm.vepms.style['grid-template-rows']" type="text"
										class="help-target input w-full" name="grid-template-rows">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(1, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.1r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(2, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.2r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(3, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.3r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(4, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.4r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(5, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.5r' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridTemplateColumns" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridTemplateColumns.desc' />">
									<@spring.message code='dashboard.veditor.style.gridTemplateColumns' />
									<span class="text-color-secondary text-sm ml-1">grid-template-columns</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridTemplateColumns" v-model="pm.vepms.style['grid-template-columns']" type="text"
										class="help-target input w-full" name="grid-template-columns">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(1, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.1c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(2, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.2c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(3, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.3c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(4, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.4c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(5, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.5c' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridRowGap" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.rowGap.desc' />">
									<@spring.message code='dashboard.veditor.style.rowGap' />
									<span class="text-color-secondary text-sm ml-1">row-gap</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridRowGap" v-model="pm.vepms.style['row-gap']" type="text"
										class="help-target input w-full" name="row-gap">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridColumnGap" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.columnGap.desc' />">
									<@spring.message code='dashboard.veditor.style.columnGap' />
									<span class="text-color-secondary text-sm ml-1">column-gap</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridColumnGap" v-model="pm.vepms.style['column-gap']" type="text"
										class="help-target input w-full" name="column-gap">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridTemplateAreas" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridTemplateAreas.desc' />">
									<@spring.message code='dashboard.veditor.style.gridTemplateAreas' />
									<span class="text-color-secondary text-sm ml-1">grid-template-areas</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridTemplateAreas" v-model="pm.vepms.style['grid-template-areas']" type="text"
										class="input w-full" name="grid-template-areas">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAutoFlow" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.gridAutoFlow' />
									<span class="text-color-secondary text-sm ml-1">grid-auto-flow</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAutoFlow" v-model="pm.vepms.style['grid-auto-flow']" type="text"
										class="help-target input w-full" name="grid-auto-flow">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="row">
											<@spring.message code='dashboard.veditor.style.gridAutoFlow.row' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="column">
											<@spring.message code='dashboard.veditor.style.gridAutoFlow.column' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="row dense">
											<@spring.message code='dashboard.veditor.style.gridAutoFlow.rowDense' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="column dense">
											<@spring.message code='dashboard.veditor.style.gridAutoFlow.columnDense' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleJustifyItems" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.justifyItems' />
									<span class="text-color-secondary text-sm ml-1">justify-items</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleJustifyItems" v-model="pm.vepms.style['justify-items']" type="text"
										class="help-target input w-full" name="justify-items">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAlignItems" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.gridAlignItems' />
									<span class="text-color-secondary text-sm ml-1">align-items</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAlignItems" v-model="pm.vepms.style['align-items']" type="text"
										class="help-target input w-full" name="align-items">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridJustifyContent" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.gridJustifyContent' />
									<span class="text-color-secondary text-sm ml-1">justify-content</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridJustifyContent" v-model="pm.vepms.style['justify-content']" type="text"
										class="help-target input w-full" name="justify-content">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
									<div class="p-buttonset text-sm" style="margin-top:1px;">
										<p-button type="button" class="help-src p-button-secondary" help-value="space-around">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-around' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-between">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-between' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-evenly">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-evenly' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAlignContent" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.gridAlignContent' />
									<span class="text-color-secondary text-sm ml-1">align-content</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAlignContent" v-model="pm.vepms.style['align-content']" type="text"
										class="help-target input w-full" name="align-content">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
									<div class="p-buttonset text-sm" style="margin-top:1px;">
										<p-button type="button" class="help-src p-button-secondary" help-value="space-around">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-around' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-between">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-between' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-evenly">
											<@spring.message code='dashboard.veditor.style.gridAligns.space-evenly' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAutoRows" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridTemplateColumns.desc' />">
									<@spring.message code='dashboard.veditor.style.gridAutoRows' />
									<span class="text-color-secondary text-sm ml-1">grid-auto-rows</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAutoRows" v-model="pm.vepms.style['grid-auto-rows']" type="text"
										class="help-target input w-full" name="grid-auto-rows">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(1, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.1r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(2, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.2r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(3, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.3r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(4, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.4r' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(5, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateRows.5r' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAutoColumns" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridTemplateColumns.desc' />">
									<@spring.message code='dashboard.veditor.style.gridAutoColumns' />
									<span class="text-color-secondary text-sm ml-1">grid-auto-columns</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAutoColumns" v-model="pm.vepms.style['grid-auto-columns']" type="text"
										class="help-target input w-full" name="grid-auto-columns">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(1, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.1c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(2, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.2c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(3, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.3c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(4, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.4c' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="repeat(5, 1fr)">
											<@spring.message code='dashboard.veditor.style.gridTemplateColumns.5c' />
										</p-button>
									</div>
								</div>
							</div>
							<p-divider align="center" class="mb-1">
								<label class="text-lg font-bold">
									<@spring.message code='gridItem' />
								</label>
							</p-divider>
							<div class="text-color-secondary text-sm text-center mb-3">
								<@spring.message code='dashboard.veditor.styleSubType.gridItem.desc' />
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridRowStart" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridRowStart.desc' />">
									<@spring.message code='dashboard.veditor.style.gridRowStart' />
									<span class="text-color-secondary text-sm ml-1">grid-row-start</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridRowStart" v-model="pm.vepms.style['grid-row-start']" type="text"
										class="input w-full" name="grid-row-start">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridRowEnd" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridRowEnd.desc' />">
									<@spring.message code='dashboard.veditor.style.gridRowEnd' />
									<span class="text-color-secondary text-sm ml-1">grid-row-end</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridRowEnd" v-model="pm.vepms.style['grid-row-end']" type="text"
										class="input w-full" name="grid-row-end">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridColumnStart" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridColumnStart.desc' />">
									<@spring.message code='dashboard.veditor.style.gridColumnStart' />
									<span class="text-color-secondary text-sm ml-1">grid-column-start</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridColumnStart" v-model="pm.vepms.style['grid-column-start']" type="text"
										class="input w-full" name="grid-column-start">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridColumnEnd" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridColumnEnd.desc' />">
									<@spring.message code='dashboard.veditor.style.gridColumnEnd' />
									<span class="text-color-secondary text-sm ml-1">grid-column-end</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridColumnEnd" v-model="pm.vepms.style['grid-column-end']" type="text"
										class="input w-full" name="grid-column-end">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridArea" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.gridArea.desc' />">
									<@spring.message code='dashboard.veditor.style.gridArea' />
									<span class="text-color-secondary text-sm ml-1">grid-area</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridArea" v-model="pm.vepms.style['grid-area']" type="text"
										class="input w-full" name="grid-area">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleJustifySelf" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.justifySelf' />
									<span class="text-color-secondary text-sm ml-1">justify-self</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleJustifySelf" v-model="pm.vepms.style['justify-self']" type="text"
										class="help-target input w-full" name="justify-self">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleGridAlignSelf" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.gridAlignSelf' />
									<span class="text-color-secondary text-sm ml-1">align-self</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleGridAlignSelf" v-model="pm.vepms.style['align-self']" type="text"
										class="help-target input w-full" name="align-self">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="start">
											<@spring.message code='dashboard.veditor.style.gridAligns.start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="end">
											<@spring.message code='dashboard.veditor.style.gridAligns.end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.gridAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.gridAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='flexLayout' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<p-divider align="center" class="mb-1">
								<label class="text-lg font-bold">
									<@spring.message code='flexContainer' />
								</label>
							</p-divider>
							<div class="text-color-secondary text-sm text-center mb-3">
								<@spring.message code='dashboard.veditor.styleSubType.flexContainer.desc' />
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexDirection" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexDirection' />
									<span class="text-color-secondary text-sm ml-1">flex-direction</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexDirection" v-model="pm.vepms.style['flex-direction']" type="text"
										class="help-target input w-full" name="flex-direction">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="row">
											<@spring.message code='dashboard.veditor.style.flexDirection.row' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="row-reverse">
											<@spring.message code='dashboard.veditor.style.flexDirection.row-reverse' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="column">
											<@spring.message code='dashboard.veditor.style.flexDirection.column' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="column-reverse">
											<@spring.message code='dashboard.veditor.style.flexDirection.column-reverse' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexWrap" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexWrap' />
									<span class="text-color-secondary text-sm ml-1">flex-wrap</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexWrap" v-model="pm.vepms.style['flex-wrap']" type="text"
										class="help-target input w-full" name="flex-wrap">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="nowrap">
											<@spring.message code='dashboard.veditor.style.flexWrap.nowrap' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="wrap">
											<@spring.message code='dashboard.veditor.style.flexWrap.wrap' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="wrap-reverse">
											<@spring.message code='dashboard.veditor.style.flexWrap.wrap-reverse' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexJustifyContent" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexJustifyContent' />
									<span class="text-color-secondary text-sm ml-1">justify-content</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexJustifyContent" v-model="pm.vepms.style['justify-content']" type="text"
										class="help-target input w-full" name="justify-content">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-start">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-end">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.flexAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-between">
											<@spring.message code='dashboard.veditor.style.flexAligns.space-between' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-around">
											<@spring.message code='dashboard.veditor.style.flexAligns.space-around' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexAlignItems" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexAlignItems' />
									<span class="text-color-secondary text-sm ml-1">align-items</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexAlignItems" v-model="pm.vepms.style['align-items']" type="text"
										class="help-target input w-full" name="align-items">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-start">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-end">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.flexAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="baseline">
											<@spring.message code='dashboard.veditor.style.flexAligns.baseline' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.flexAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexAlignContent" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexAlignContent' />
									<span class="text-color-secondary text-sm ml-1">align-content</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexAlignContent" v-model="pm.vepms.style['align-content']" type="text"
										class="help-target input w-full" name="align-content">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-start">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-end">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.flexAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-between">
											<@spring.message code='dashboard.veditor.style.flexAligns.space-between' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="space-around">
											<@spring.message code='dashboard.veditor.style.flexAligns.space-around' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.flexAligns.stretch' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexRowGap" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.rowGap.desc' />">
									<@spring.message code='dashboard.veditor.style.rowGap' />
									<span class="text-color-secondary text-sm ml-1">row-gap</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexRowGap" v-model="pm.vepms.style['row-gap']" type="text"
										class="help-target input w-full" name="row-gap">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexColumnGap" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.columnGap.desc' />">
									<@spring.message code='dashboard.veditor.style.columnGap' />
									<span class="text-color-secondary text-sm ml-1">column-gap</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexColumnGap" v-model="pm.vepms.style['column-gap']" type="text"
										class="help-target input w-full" name="column-gap">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="3px">
											3px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="5px">
											5px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="10px">
											10px
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
									</div>
								</div>
							</div>
							<p-divider align="center" class="mb-1">
								<label class="text-lg font-bold">
									<@spring.message code='flexItem' />
								</label>
							</p-divider>
							<div class="text-color-secondary text-sm text-center mb-3">
								<@spring.message code='dashboard.veditor.styleSubType.flexItem.desc' />
							</div>
							<div class="field grid">
								<label for="${pid}veStyleOrder" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.order.desc' />">
									<@spring.message code='dashboard.veditor.style.order' />
									<span class="text-color-secondary text-sm ml-1">order</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleOrder" v-model="pm.vepms.style['order']" type="text"
										class="input w-full" name="order">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexGrow" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.flexGrow.desc' />">
									<@spring.message code='dashboard.veditor.style.flexGrow' />
									<span class="text-color-secondary text-sm ml-1">flex-grow</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexGrow" v-model="pm.vepms.style['flex-grow']" type="text"
										class="input w-full" name="flex-grow">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexShrink" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.flexShrink.desc' />">
									<@spring.message code='dashboard.veditor.style.flexShrink' />
									<span class="text-color-secondary text-sm ml-1">flex-shrink</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexShrink" v-model="pm.vepms.style['flex-shrink']" type="text"
										class="input w-full" name="flex-shrink">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexBasis" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.flexBasis.desc' />">
									<@spring.message code='dashboard.veditor.style.flexBasis' />
									<span class="text-color-secondary text-sm ml-1">flex-basis</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexBasis" v-model="pm.vepms.style['flex-basis']" type="text"
										class="input w-full" name="flex-basis">
									</p-inputtext>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFlexAlignSelf" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.flexAlignSelf' />
									<span class="text-color-secondary text-sm ml-1">align-self</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFlexAlignSelf" v-model="pm.vepms.style['align-self']" type="text"
										class="help-target input w-full" name="align-self">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-start">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-start' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="flex-end">
											<@spring.message code='dashboard.veditor.style.flexAligns.flex-end' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.flexAligns.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="baseline">
											<@spring.message code='dashboard.veditor.style.flexAligns.baseline' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="stretch">
											<@spring.message code='dashboard.veditor.style.flexAligns.stretch' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="auto">
											<@spring.message code='dashboard.veditor.style.flexAligns.auto' />
										</p-button>
									</div>
								</div>
							</div>
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='font' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleFontSize" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.fontSize.desc' />">
									<@spring.message code='dashboard.veditor.style.fontSize' />
									<span class="text-color-secondary text-sm ml-1">font-size</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFontSize" v-model="pm.vepms.style['font-size']" type="text"
										class="help-target input w-full" name="font-size">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="0.75rem">
											0.75rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="0.875rem">
											0.875rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1rem">
											1rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1.25rem">
											1.25rem
										</p-button>
									</div>
									<div class="p-buttonset text-sm" style="margin-top:1px;">
										<p-button type="button" class="help-src p-button-secondary" help-value="1.5rem">
											1.5rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1.75rem">
											1.75rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2rem">
											2rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2.5rem">
											2.5rem
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3rem">
											3rem
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleTextAlign" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.textAlign' />
									<span class="text-color-secondary text-sm ml-1">text-align</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleTextAlign" v-model="pm.vepms.style['text-align']" type="text"
										class="help-target input w-full" name="text-align">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="left">
											<@spring.message code='dashboard.veditor.style.textAlign.left' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="center">
											<@spring.message code='dashboard.veditor.style.textAlign.center' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="right">
										<@spring.message code='dashboard.veditor.style.textAlign.right' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFontWeight" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.fontWeight' />
									<span class="text-color-secondary text-sm ml-1">font-weight</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFontWeight" v-model="pm.vepms.style['font-weight']" type="text"
										class="help-target input w-full" name="font-weight">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="normal">
											<@spring.message code='dashboard.veditor.style.fontWeight.normal' />
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="bold">
											<@spring.message code='dashboard.veditor.style.fontWeight.bold' />
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFontWeight" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.lineHeight' />
									<span class="text-color-secondary text-sm ml-1">line-height</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFontWeight" v-model="pm.vepms.style['line-height']" type="text"
										class="help-target input w-full" name="line-height">
									</p-inputtext>
									<div class="p-buttonset mt-1 text-sm">
										<p-button type="button" class="help-src p-button-secondary" help-value="normal">
											normal
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="1.5em">
											1.5em
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2em">
											2em
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="2.5em">
											2.5em
										</p-button>
										<p-button type="button" class="help-src p-button-secondary" help-value="3em">
											3em
										</p-button>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleFontFamily" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.fontFamily' />
									<span class="text-color-secondary text-sm ml-1">font-family</span>
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleFontFamily" v-model="pm.vepms.style['font-family']" type="text"
										class="input w-full" name="font-family">
									</p-inputtext>
								</div>
							</div>
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='other' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleClassName" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.className' />
								</label>
								<div class="field-input col-12">
									<p-inputtext id="${pid}veStyleClassName" v-model="pm.vepms.style['className']" type="text"
										class="input w-full" name="className">
									</p-inputtext>
								</div>
							</div>
						</div>
					</p-tabpanel>
				</p-tabview>
			</div>
			<div class="page-form-foot flex-grow-0 pt-2">
				<div class="flex">
					<div class="flex-shink-1"  style="flex-basis:50%;">
					</div>
					<div class="flex-grow-0 flex-shink-0 flex justify-content-center px-3">
						<p-button type="submit" label="<@spring.message code='confirm' />" class="white-space-nowrap"></p-button>
					</div>
					<div class="flex-shink-1 flex justify-content-start align-items-center" style="flex-basis:50%;"><p-checkbox input-id="${pid}styleSyncChartTheme" v-model="pm.vepms.style['syncChartTheme']" :binary="true"></p-checkbox>
						<label for="${pid}styleSyncChartTheme" class="ml-1" title="<@spring.message code='dashboard.veditor.style.syncChartTheme.desc' />">
							<@spring.message code='dashboard.veditor.style.syncChartTheme' />
						</label>
					</div>
				</div>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.eleId" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.eleIdShown" @show="onVeEleIdPanelShow">
	<div class="page page-form">
		<form id="${pid}veEleIdForm" class="flex flex-column">
			<div class="page-form-content size-20vw flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veEleIdValue" class="field-label col-12 mb-2">
						<@spring.message code='id' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veEleIdValue" v-model="pm.vepms.eleId.value" type="text"
							class="input w-full" name="value" autofocus>
						</p-inputtext>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.iframe" append-to="body"
	position="center" :modal="true" v-model:visible="pm.vepss.iframeShown" @show="onVeIframePanelShow">
	<div class="page page-form">
		<form id="${pid}veIframeForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veIframeSrc" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.iframe.src.desc' />">
						<@spring.message code='url' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veIframeSrc" v-model="pm.vepms.iframe.src" type="text"
							class="input w-full" name="src" autofocus>
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veIframeWdith" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.iframe.width.desc' />">
						<@spring.message code='width' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veIframeWdith" v-model="pm.vepms.iframe.width" type="text"
							class="help-target input w-full" name="width">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veIframeHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.iframe.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veIframeHeight" v-model="pm.vepms.iframe.height" type="text"
							class="help-target input w-full" name="height">
						</p-inputtext>
						<div class="p-buttonset mt-1 text-sm">
							<p-button type="button" class="help-src p-button-secondary" help-value="100px">
								100px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="300px">
								300px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="500px">
								500px
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="30%">
								30%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="50%">
								50%
							</p-button>
							<p-button type="button" class="help-src p-button-secondary" help-value="100%">
								100%
							</p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-overlaypanel ref="${pid}optionsOriginPanelEle" append-to="body" id="${pid}optionsOriginPanel" @show="onOptionsOriginPanelShow">
	<div class="pb-2">
		<label class="text-lg font-bold">
			<@spring.message code='chartOptionsOrigin' />
		</label>
	</div>
	<div class="page page-form">
		<form id="${pid}optionsOriginForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<div class="field-input col-12">
						<div id="${pid}optionsContent" class="code-editor-wrapper input p-component p-inputtext panel-content-size-xxs">
							<div id="${pid}optionsContentCodeEditor" class="code-editor"></div>
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>
</p-overlaypanel>

<script>
(function(po)
{
	po.veDftChartThemeModel = function()
	{
		var re = { graphColors: [], graphRangeColors: [] };
		return re;
	};
	
	po.veDftGridLayoutModel = function()
	{
		var re =
		{
			fillParent: false, rowHeightDivide: "avg", rowHeights: [], rowGap: "5px",
			colWidthDivide: "avg", colWidths: [], columnGap: "5px"
		};
		return re;
	};
	
	po.veDftVideoModel = function()
	{
		var re = {};
		return re;
	};
	
	po.veDftIframeModel = function()
	{
		var re = {};
		return re;
	};
	
	po.initVePanelHelperSrc = function(form, formModel, helpValueHandler, helpTargetHandler)
	{
		$(form).on("click", ".help-src", function()
		{
			var $this = $(this);
			var helpValue = ($this.attr("help-value") || "");
			var helpTarget = po.element(".help-target", $this.closest(".field-input"));
			var targetName = helpTarget.attr("name");
			
			if(helpValueHandler != null)
			{
				var newHelpValue = helpValueHandler(form, formModel, helpValue, targetName);
				if(newHelpValue !== false)
				{
					helpValue = newHelpValue;
				}
			}
			
			if(helpTargetHandler != null)
			{
				helpTargetHandler(form, formModel, helpValue, targetName);
			}
			else
			{
				if(targetName)
					$.propPathValue(formModel, targetName, helpValue);
				else
					helpTarget.val(helpValue);
			}
			
			//不自动聚焦了，总会激活浏览器自动补全框，影响操作
			//helpTarget.focus();
		});
	};
	
	po.cssColorToHexStrChartTheme = function(chartTheme)
	{
		var re = $.extend(true, {}, chartTheme);
		re.color = po.cssColorToHexStr(re.color);
		re.backgroundColor = po.cssColorToHexStr(re.backgroundColor);
		re.actualBackgroundColor = po.cssColorToHexStr(re.actualBackgroundColor);
		re.graphColors = po.cssColorsToHexStrs(re.graphColors);
		re.graphRangeColors = po.cssColorsToHexStrs(re.graphRangeColors);
		
		return re;
	};

	po.cssColorToHexStrStyle = function(style)
	{
		var re = $.extend(true, {}, style);
		re.color = po.cssColorToHexStr(re.color);
		re['background-color'] = po.cssColorToHexStr(re['background-color']);
		re['border-color'] = po.cssColorToHexStr(re['border-color']);
		
		return re;
	};
	
	//重写chart_attr_values_form.ftl中的函数
	po.getChartPluginAttributeInputOptionsForMap = function(asTree)
	{
		var re = [];
		
		var dashboardEditor = po.visualDashboardEditorByTab();
		if(dashboardEditor)
			re = dashboardEditor.getChartPluginAttributeInputOptionsForMap(asTree);
		
		return re;
	};
	
	po.showVeGridLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veGridLayoutPanelShowFillParent = showFillParent;
		pm.vepms.gridLayout.fillParent = showFillParent;
		pm.vepss.gridLayoutShown = true;
	};

	po.showVeFlexLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veFlexLayoutPanelShowFillParent = showFillParent;
		pm.vepms.flexLayout.fillParent = showFillParent;
		pm.vepss.flexLayoutShown = true;
	};
	
	po.showVeHxtitlePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.hxtitle = submitHandler;
		pm.vepms.hxtitle = $.extend(true, { type: "h1" }, model);
		pm.vepss.hxtitleShown = true;
	};
	
	po.showVeTextElementPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.textElement = submitHandler;
		pm.vepms.textElement = $.extend(true, {}, model);
		pm.vepss.textElementShown = true;
	};
	
	po.showVeImagePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.image = submitHandler;
		pm.vepms.image = $.extend(true, {}, model);
		pm.vepss.imageShown = true;
	};
	
	po.showVeHyperlinkPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.hyperlink = submitHandler;
		pm.vepms.hyperlink = $.extend(true, {}, model);
		pm.vepss.hyperlinkShown = true;
	};
	
	po.showVeVideoPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.video = submitHandler;
		pm.vepms.video = $.extend(true, po.veDftVideoModel(), model);
		pm.vepss.videoShown = true;
	};
	
	po.showVeDashboardSizePanel = function(model)
	{
		var pm = po.vuePageModel();
		pm.vepms.dashboardSize = $.extend(true, {}, model);
		pm.vepss.dashboardSizeShown = true;
	};

	po.showVeChartAttrValuesPanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartAttrValues = submitHandler;
		if(title)
			pm.vepts.chartAttrValues = title; 
		pm.vepss.chartAttrValuesShown = true;
	};
	
	po.showVeChartOptionsPanel = function(submitHandler, model, title, global)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartOptions = submitHandler;
		pm.vepms.chartOptions = $.extend(true, {}, model);
		if(title)
			pm.vepts.chartOptions = title;
		pm.veChartOptionsPanelForGlobal = global;
		pm.vepss.chartOptionsShown = true;
	};
	
	po.showVeChartThemePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartTheme = submitHandler;
		pm.vepms.chartTheme = $.extend(true, po.veDftChartThemeModel(), model);
		if(title)
			pm.vepts.chartTheme = title;
		pm.vepss.chartThemeShown = true;
	};
	
	po.showVeStylePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.style = submitHandler;
		pm.vepms.style = $.extend(true, {}, model);
		if(title)
			pm.vepts.style = title;
		pm.vepss.styleShown = true;
	};

	po.showVeEleIdPanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.eleId = submitHandler;
		pm.vepms.eleId = $.extend(true, {}, model);
		pm.vepss.eleIdShown = true;
	};
	
	po.showVeIframePanel = function(submitHandler, model)
	{
		var pm = po.vuePageModel();
		pm.veshs.iframe = submitHandler;
		pm.vepms.iframe = $.extend(true, po.veDftIframeModel(), model);
		pm.vepss.iframeShown = true;
	};
	
	po.setupResourceEditorForms = function()
	{
		po.vueRef("${pid}optionsOriginPanelEle", null);
		
		po.vuePageModel(
		{
			//可视编辑操作对话框是否显示
			vepss:
			{
				gridLayoutShown: false,
				flexLayoutShown: false,
				hxtitleShown: false,
				textElementShown: false,
				imageShown: false,
				hyperlinkShown: false,
				videoShown: false,
				dashboardSizeShown: false,
				chartOptionsShown: false,
				chartAttrValuesShown: false,
				chartThemeShown: false,
				styleShown: false,
				eleIdShown: false,
				iframeShown: false
			},
			//可视编辑操作对话框标题
			vepts:
			{
				gridLayout: "<@spring.message code='gridLayout' />",
				flexLayout: "<@spring.message code='flexLayout' />",
				hxtitle: "<@spring.message code='titleElement' />",
				textElement: "<@spring.message code='textElement' />",
				image: "<@spring.message code='image' />",
				hyperlink: "<@spring.message code='hyperlink' />",
				video: "<@spring.message code='video' />",
				dashboardSize: "<@spring.message code='dashboardSize' />",
				chartOptions: "<@spring.message code='chartOptions' />",
				chartAttrValues: "<@spring.message code='chartAttribute' /> dg-chart-attr-values",
				chartTheme: "<@spring.message code='chartTheme' />",
				style: "<@spring.message code='style' />",
				eleId: "<@spring.message code='elementId' />",
				iframe: "<@spring.message code='iframe' />"
			},
			//可视编辑操作对话框表单模型
			vepms:
			{
				gridLayout: po.veDftGridLayoutModel(),
				flexLayout: { fillParent: false },
				hxtitle: { type: "h1", content: "" },
				textElement: { content: "" },
				image: {},
				hyperlink: {},
				video: po.veDftVideoModel(),
				dashboardSize: { scale: "auto" },
				chartOptions: { value: "" },
				chartAttrValues: {},
				chartTheme: po.veDftChartThemeModel(),
				style: {},
				eleId: {},
				iframe: po.veDftIframeModel()
			},
			//可视编辑操作对话框提交处理函数
			veshs:
			{
				hxtitle: function(model){},
				textElement: function(model){},
				image: function(model){},
				hyperlink: function(model){},
				video: function(model){},
				chartOptions: function(model){},
				chartAttrValues: function(model){},
				chartTheme: function(model){},
				style: function(model){},
				eleId: function(model){},
				iframe: function(model){}
			},
			veGridLayoutPanelShowFillParent: false,
			veFlexLayoutPanelShowFillParent: false,
			dashboardSizeScaleOptions:
			[
				{ name: "<@spring.message code='auto' />", value: "auto" },
				{ name: "100%", value: 100 },
				{ name: "75%", value: 75 },
				{ name: "50%", value: 50 },
				{ name: "25%", value: 25 }
			],
			veStyleTabviewActiveIndex: 0,
			hxtitleTypeOptions:
			[
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h1' />", value: "h1" },
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h2' />", value: "h2" },
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h3' />", value: "h3" },
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h4' />", value: "h4" },
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h5' />", value: "h5" },
				{ name: "<@spring.message code='dashboard.veditor.hxtitle.type.h6' />", value: "h6" }
			],
			veChartOptionsPanelForGlobal: false,
			gridLayoutDivideOptions:
			[
				{ name: "<@spring.message code='dashboard.veditor.gridLayout.divide.avg' />", value: "avg" },
				{ name: "<@spring.message code='dashboard.veditor.gridLayout.divide.custom' />", value: "custom" }
			]
		});
		
		var pm = po.vuePageModel();
		
		po.vueMethod(
		{
			formatHxtitleOptionLabel: function(option)
			{
				return "<"+option.value+">"+option.name+"</"+option.value+">";
			},
			onVeGridLayoutPanelShow: function()
			{
				var form = po.elementOfId("${pid}veGridLayoutForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.gridLayout);
				
				po.setupSimpleForm(form, pm.vepms.gridLayout, function()
				{
					if(po.insertVeGridLayout(pm.vepms.gridLayout) !== false)
					{
						pm.vepms.gridLayout = po.veDftGridLayoutModel();
						pm.vepss.gridLayoutShown = false;
					}
				});
			},
			
			onVeFlexLayoutPanelShow: function()
			{
				var form = po.elementOfId("${pid}veFlexLayoutForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.flexLayout);
				
				po.setupSimpleForm(form, pm.vepms.flexLayout, function()
				{
					if(po.insertVeFlexLayout(pm.vepms.flexLayout) !== false)
					{
						pm.vepms.flexLayout = { fillParent: false };
						pm.vepss.flexLayoutShown = false;
					}
				});
			},
			
			onVeHxtitlePanelShow: function()
			{
				var form = po.elementOfId("${pid}veHxtitleForm", document.body);
				po.initVePanelHelperSrc(form, pm.vepms.hxtitle);
				
				po.setupSimpleForm(form, pm.vepms.hxtitle, function()
				{
					if(pm.veshs.hxtitle(pm.vepms.hxtitle) !== false)
					{
						pm.vepms.hxtitle = { type: "h1", content: "" };
						pm.vepss.hxtitleShown = false;
					}
				});
			},
			
			onVeTextElementPanelShow: function()
			{
				var form = po.elementOfId("${pid}veTextElementForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.textElement, function()
				{
					if(pm.veshs.textElement(pm.vepms.textElement) !== false)
					{
						pm.vepms.textElement = {};
						pm.vepss.textElementShown = false;
					}
				});
			},
			
			onVeImagePanelShow: function()
			{
				var form = po.elementOfId("${pid}veImageForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.image);
				
				po.setupSimpleForm(form, pm.vepms.image, function()
				{
					if(pm.veshs.image(pm.vepms.image) !== false)
					{
						pm.vepms.image = {};
						pm.vepss.imageShown = false;
					}
				});
			},
			
			onVeHyperlinkPanelShow: function()
			{
				var form = po.elementOfId("${pid}veHyperlinkForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.hyperlink);
				
				po.setupSimpleForm(form, pm.vepms.hyperlink, function()
				{
					if(pm.veshs.hyperlink(pm.vepms.hyperlink) !== false)
					{
						pm.vepms.hyperlink = {};
						pm.vepss.hyperlinkShown = false;
					}
				});
			},
			
			onVeVideoPanelShow: function()
			{
				var form = po.elementOfId("${pid}veVideoForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.video);
				
				po.setupSimpleForm(form, pm.vepms.video, function()
				{
					if(pm.veshs.video(pm.vepms.video) !== false)
					{
						pm.vepms.video = po.veDftVideoModel();
						pm.vepss.videoShown = false;
					}
				});
			},
			
			onVeDashboardSizePanelShow: function()
			{
				var form = po.elementOfId("${pid}veDashboardSizeForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.dashboardSize, function()
				{
					if(po.setVeDashboardSize(null, pm.vepms.dashboardSize) !== false)
					{
						pm.vepss.dashboardSizeShown = false;
					}
				});
			},
			
			onVeDashboardSizeResetToDft: function()
			{
				if(po.setVeDashboardSize(null, {}) !== false)
				{
					pm.vepss.dashboardSizeShown = false;
				}
			},
			
			onVeChartAttrValuesPanelShow: function()
			{
				var dashboardEditor = po.visualDashboardEditorByTab();
				var cpas = [];
				var attrValues = [];
				
				if(dashboardEditor)
				{
					cpas = (dashboardEditor.getElementChartPluginAttrs() || []);
					attrValues = (dashboardEditor.getElementChartAttrValues() || {});
				}
				
				po.setupChartAttrValuesForm(cpas, attrValues,
				{
					submitHandler: function(avs)
					{
						if(pm.veshs.chartAttrValues(avs) !== false)
						{
							pm.vepss.chartAttrValuesShown = false;
						}
					},
					buttons:
					[
						{
							name: "<@spring.message code='resetToOrigin' />",
							clickHandler: function()
							{
								var attrValuesReset = {};
								
								if(dashboardEditor)
									attrValuesReset = (dashboardEditor.getElementChartAttrValuesForReset() || {});
								
								po.setChartAttrValuesFormAttrValues(attrValuesReset);
							}
						}
					]
				});
			},
			
			onVeChartOptionsPanelShow: function()
			{
				var form = po.elementOfId("${pid}veChartOptionsForm", document.body);
				var codeEditorEle = po.elementOfId("${pid}veChartOptionsCodeEditor", form);
				
				var editorOptions =
				{
					value: "",
					matchBrackets: true,
					autoCloseBrackets: true,
					mode: {name: "javascript", json: true}
				};
				
				codeEditorEle.empty();
				var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
				po.setCodeTextTimeout(codeEditor, (pm.vepms.chartOptions.value || ""), true);
				
				po.setupSimpleForm(form, pm.vepms.chartOptions, function()
				{
					pm.vepms.chartOptions.value = po.getCodeText(codeEditor);
					if(pm.veshs.chartOptions(pm.vepms.chartOptions) !== false)
					{
						pm.vepms.chartOptions = {};
						pm.vepss.chartOptionsShown = false;
					}
				});
			},
			
			onVeChartThemePanelShow: function()
			{
				var form = po.elementOfId("${pid}veChartThemeForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.chartTheme, function()
				{
					if(pm.veshs.chartTheme(pm.vepms.chartTheme) !== false)
					{
						pm.vepms.chartTheme = po.veDftChartThemeModel();
						pm.vepss.chartThemeShown = false;
					}
				});
			},
			
			onVeChartThemeAddGraphColor: function()
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphColors.push("");
			},
			
			onVeChartThemeAddGraphRangeColor: function()
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphRangeColors.push("");
			},
			
			onVeChartThemeInsertGraphColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				//不在idx+1位置插入，这样无法在第一个之前插入
				chartTheme.graphColors.splice(idx, 0, "");
			},
			
			onVeChartThemeRemoveGraphColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphColors.splice(idx, 1);
			},

			onVeChartThemeInsertGraphRangeColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				//不在idx+1位置插入，这样无法在第一个之前插入
				chartTheme.graphRangeColors.splice(idx, 0, "");
			},
			
			onVeChartThemeRemoveGraphRangeColor: function(e, idx)
			{
				var chartTheme = pm.vepms.chartTheme;
				chartTheme.graphRangeColors.splice(idx, 1);
			},
			
			onVeStylePanelShow: function()
			{
				var form = po.elementOfId("${pid}veStyleForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.style);
				
				po.setupSimpleForm(form, pm.vepms.style, function()
				{
					if(pm.veshs.style(pm.vepms.style) !== false)
					{
						pm.vepms.style = {};
						pm.vepss.styleShown = false;
					}
				});
			},
			
			onShowOptionsOriginPanel: function(e)
			{
				po.vueUnref("${pid}optionsOriginPanelEle").toggle(e);
			},
			
			onOptionsOriginPanelShow: function()
			{
				var optionsOrigin = "";
				
				var dashboardEditor = po.visualDashboardEditorByTab();
				if(dashboardEditor)
					optionsOrigin = dashboardEditor.getElementChartOptionsOrigin();
				
				var form = po.elementOfId("${pid}optionsOriginForm", document.body);
				var codeEditorEle = po.elementOfId("${pid}optionsContentCodeEditor", form);
				
				var editorOptions =
				{
					value: "",
					matchBrackets: true,
					autoCloseBrackets: true,
					mode: {name: "javascript", json: true},
					readOnly: true
				};
				
				codeEditorEle.empty();
				var codeEditor = po.createCodeEditor(codeEditorEle, editorOptions);
				po.setCodeTextTimeout(codeEditor, (optionsOrigin || ""), true);
				
				po.setupSimpleForm(form, {}, function(){});
			},
			
			onVeEleIdPanelShow: function()
			{
				var form = po.elementOfId("${pid}veEleIdForm", document.body);
				
				po.setupSimpleForm(form, pm.vepms.eleId, function()
				{
					if(pm.veshs.eleId(pm.vepms.eleId) !== false)
					{
						pm.vepms.eleId = {};
						pm.vepss.eleIdShown = false;
					}
				});
			},

			onVeIframePanelShow: function()
			{
				var form = po.elementOfId("${pid}veIframeForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.iframe);
				
				po.setupSimpleForm(form, pm.vepms.iframe, function()
				{
					if(pm.veshs.iframe(pm.vepms.iframe) !== false)
					{
						pm.vepms.iframe = po.veDftIframeModel();
						pm.vepss.iframeShown = false;
					}
				});
			}
		});
		
		po.vueWatch(function()
		{
			return pm.vepms.gridLayout.rows;
		},
		function(newVal, oldVal)
		{
			//默认不使用"auto"，内部插入元素后会导致尺寸变化
			var dftValue = "1fr";
			$.trimArrayLen(pm.vepms.gridLayout.rowHeights, pm.vepms.gridLayout.rows, dftValue);
		});
		
		po.vueWatch(function()
		{
			return pm.vepms.gridLayout.columns;
		},
		function(newVal, oldVal)
		{
			//默认不使用"auto"，内部插入元素后会导致尺寸变化
			var dftValue = "1fr";
			$.trimArrayLen(pm.vepms.gridLayout.colWidths, pm.vepms.gridLayout.columns, dftValue);
		});
	};
})
(${pid});
</script>