<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#--
看板编辑器表单面板HTML
注意：
这些HTML不能直接写在dashboard_form_editor.ftl内，
因为会出现嵌套form，这里面板里的form元素会被vue解析剔除

依赖：
page_boolean_options.ftl

-->
<form id="${pid}visualEditorLoadForm" action="#" method="POST" style="display:none;">
	<input type="hidden" name="DG_EDIT_TEMPLATE" value="true" />
	<textarea name="DG_TEMPLATE_CONTENT"></textarea>
</form>

<p-dialog :header="pm.vepts.gridLayout" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.gridLayoutShown" @show="onVeGridLayoutPanelShow">
	<div class="page page-form">
		<form id="${pid}veGridLayoutForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veGridLayoutRows" class="field-label col-12 mb-2">
						<@spring.message code='rowCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutRows" v-model="pm.vepms.gridLayout.rows" type="text"
							class="help-target input w-full" name="rows" maxlength="10" autofocus>
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
					<label for="${pid}veGridLayoutColumns" class="field-label col-12 mb-2">
						<@spring.message code='columnCount' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veGridLayoutColumns" v-model="pm.vepms.gridLayout.columns" type="text"
							class="help-target input w-full" name="columns" maxlength="10">
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
				<div class="field grid" v-if="pm.veGridLayoutPanelShowFillParent">
					<label for="${pid}veGridLayoutFillParent" class="field-label col-12 mb-2"
						title="<@spring.message code='dashboard.veditor.gridLayout.fillParent.desc' />">
						<@spring.message code='dashboard.veditor.gridLayout.fillParent' />
					</label>
					<div class="field-input col-12">
						<p-selectbutton id="${pid}veGridLayoutFillParent" v-model="pm.vepms.gridLayout.fillParent" :options="pm.booleanOptions"
							option-label="name" option-value="value" class="input w-full">
						</p-selectbutton>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.textElement" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.textElementShown" @show="onVeTextElementPanelShow">
	<div class="page page-form">
		<form id="${pid}veTextElementForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veTextElementContent" class="field-label col-12 mb-2">
						<@spring.message code='textContent' />
					</label>
					<div class="field-input col-12">
						<p-textarea id="${pid}veTextElementContent" v-model="pm.vepms.textElement.content"
							class="input w-full" name="content" autofocus>
						</p-textarea>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.image" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.imageShown" @show="onVeImagePanelShow">
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
							class="input w-full" name="width">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veImageHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.image.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veImageHeight" v-model="pm.vepms.image.height" type="text"
							class="input w-full" name="height">
						</p-inputtext>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.hyperlink" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.hyperlinkShown" @show="onVeHyperlinkPanelShow">
	<div class="page page-form">
		<form id="${pid}veHyperlinkForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
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
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.video" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.videoShown" @show="onVeVideoPanelShow">
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
							class="input w-full" name="width">
						</p-inputtext>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veVideoHeight" class="field-label col-12 mb-2"
						 title="<@spring.message code='dashboard.veditor.video.height.desc' />">
						<@spring.message code='height' />
					</label>
					<div class="field-input col-12">
						<p-inputtext id="${pid}veVideoHeight" v-model="pm.vepms.video.height" type="text"
							class="input w-full" name="height">
						</p-inputtext>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.dashboardSize" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.dashboardSizeShown" @show="onVeDashboardSizePanelShow">
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
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
				<p-button type="button" label="<@spring.message code='resetToDefault' />" class="p-button-secondary" @click="onVeDashboardSizeResetToDft"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartOptions" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.chartOptionsShown" @show="onVeChartOptionsPanelShow">
	<div class="page page-form">
		<form id="${pid}veChartOptionsForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto" style="min-width:40vw;">
				<div class="field grid">
					<label for="${pid}veChartOptionsContent" class="field-label col-12 mb-2">
						<@spring.message code='chartOptions' />
					</label>
					<div class="field-input col-12">
						<div id="${pid}veChartOptionsContent" class="code-editor-wrapper input p-component p-inputtext w-full" style="height:30vh;">
							<div id="${pid}veChartOptionsCodeEditor" class="code-editor"></div>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.chartTheme" append-to="body"
	position="center" :dismissable-mask="true"
	v-model:visible="pm.vepss.chartThemeShown" @show="onVeChartThemePanelShow">
	<div class="page page-form">
		<form id="${pid}veChartThemeForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<div class="field grid">
					<label for="${pid}veChartThemeFgColor" class="field-label col-12 mb-2">
						<@spring.message code='fgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex justify-content-center">
							<p-inputtext id="${pid}veChartThemeFgColor" v-model="pm.vepms.chartTheme.color" type="text"
								class="input flex-grow-1 mr-1" name="color" autofocus>
							</p-inputtext>
							<p-colorpicker v-model="pm.vepmChartThemeProxy.color"
								default-color="FFFFFF" class="flex-grow-0 preview-h-full"
								@change="onVeChartThemeColorPickerChange($event, 'color')">
							</p-colorpicker>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeBgColor" class="field-label col-12 mb-2">
						<@spring.message code='bgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex justify-content-center">
							<p-inputtext id="${pid}veChartThemeBgColor" v-model="pm.vepms.chartTheme.backgroundColor" type="text"
								class="input flex-grow-1 mr-1" name="backgroundColor">
							</p-inputtext>
							<p-colorpicker v-model="pm.vepmChartThemeProxy.backgroundColor"
								default-color="FFFFFF" class="flex-grow-0 preview-h-full"
								@change="onVeChartThemeColorPickerChange($event, 'backgroundColor')">
							</p-colorpicker>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeActualBgColor" class="field-label col-12 mb-2">
						<@spring.message code='actualBgColor' />
					</label>
					<div class="field-input col-12">
						<div class="flex justify-content-center">
							<p-inputtext id="${pid}veChartThemeActualBgColor" v-model="pm.vepms.chartTheme.actualBackgroundColor" type="text"
								class="input flex-grow-1 mr-1" name="actualBackgroundColor">
							</p-inputtext>
							<p-colorpicker v-model="pm.vepmChartThemeProxy.actualBackgroundColor"
								default-color="FFFFFF" class="flex-grow-0 preview-h-full"
								@change="onVeChartThemeColorPickerChange($event, 'actualBackgroundColor')">
							</p-colorpicker>
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
					<label for="${pid}veChartThemeGraphColors" class="field-label col-12 mb-2">
						<@spring.message code='dashboard.veditor.chartTheme.graphColors' />
					</label>
					<div class="field-input col-12">
						<div v-for="(gc, gcIdx) in pm.vepmChartThemeProxy.graphColors" :key="gcIdx">
							<div class="flex justify-content-center mb-1">
								<p-inputtext id="${pid}veChartThemeGraphColors" v-model="pm.vepms.chartTheme.graphColors[gcIdx]" type="text"
									class="input flex-grow-1 mr-1" name="graphColors">
								</p-inputtext>
								<p-colorpicker v-model="pm.vepmChartThemeProxy.graphColors[gcIdx]"
									default-color="FFFFFF" class="flex-grow-0 preview-h-full mr-3"
									@change="onVeChartThemeColorPickerChange($event, 'graphColors', gcIdx)">
								</p-colorpicker>
								<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger"
									@click="onVeChartThemeRemoveGraphColor($event, gcIdx)">
								</p-button>
							</div>
						</div>
						<div class="mt-1">
							<p-button type="button" icon="pi pi-plus" @click="onVeChartThemeAddGraphColor"></p-button>
						</div>
					</div>
				</div>
				<div class="field grid">
					<label for="${pid}veChartThemeGraphRangeColors" class="field-label col-12 mb-2">
						<@spring.message code='dashboard.veditor.chartTheme.graphRangeColors' />
					</label>
					<div class="field-input col-12">
						<div v-for="(gc, gcIdx) in pm.vepmChartThemeProxy.graphRangeColors" :key="gcIdx">
							<div class="flex justify-content-center mb-1">
								<p-inputtext id="${pid}veChartThemeGraphRangeColors" v-model="pm.vepms.chartTheme.graphRangeColors[gcIdx]" type="text"
									class="input flex-grow-1 mr-1" name="graphRangeColors">
								</p-inputtext>
								<p-colorpicker v-model="pm.vepmChartThemeProxy.graphRangeColors[gcIdx]"
									default-color="FFFFFF" class="flex-grow-0 preview-h-full mr-3"
									@change="onVeChartThemeColorPickerChange($event, 'graphRangeColors', gcIdx)">
								</p-colorpicker>
								<p-button type="button" label="<@spring.message code='delete' />" class="p-button-danger"
									@click="onVeChartThemeRemoveGraphRangeColor($event, gcIdx)">
								</p-button>
							</div>
						</div>
						<div class="mt-1">
							<p-button type="button" icon="pi pi-plus" @click="onVeChartThemeAddGraphRangeColor"></p-button>
						</div>
					</div>
				</div>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<p-dialog :header="pm.vepts.style" append-to="body"
	position="center" :dismissable-mask="true" class="dashboard-ve-style-panel"
	v-model:visible="pm.vepss.styleShown" @show="onVeStylePanelShow">
	<div class="page page-form">
		<form id="${pid}veStyleForm" class="flex flex-column">
			<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
				<p-tabview class="light-tabview">
					<p-tabpanel header="<@spring.message code='color' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleColor" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.color.desc' />">
									<@spring.message code='dashboard.veditor.style.color' />
								</label>
								<div class="field-input col-12">
									<div class="flex justify-content-center">
										<p-inputtext id="${pid}veStyleColor" v-model="pm.vepms.style.color" type="text"
											class="input flex-grow-1 mr-1" name="color" autofocus>
										</p-inputtext>
										<p-colorpicker v-model="pm.vepmStyleProxy.color"
											default-color="FFFFFF" class="flex-grow-0 preview-h-full"
											@change="onVeStyleColorPickerChange($event, 'color')">
										</p-colorpicker>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgColor" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.bgColor.desc' />">
									<@spring.message code='dashboard.veditor.style.bgColor' />
								</label>
								<div class="field-input col-12">
									<div class="flex justify-content-center">
										<p-inputtext id="${pid}veStyleBgColor" v-model="pm.vepms.style['background-color']" type="text"
											class="input flex-grow-1 mr-1" name="background-color">
										</p-inputtext>
										<p-colorpicker v-model="pm.vepmStyleProxy['background-color']"
											default-color="FFFFFF" class="flex-grow-0 preview-h-full"
											@change="onVeStyleColorPickerChange($event, 'background-color')">
										</p-colorpicker>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBgImage" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.bgImage.desc' />">
									<@spring.message code='dashboard.veditor.style.bgImage' />
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
						</div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='border' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto">
							<div class="field grid">
								<label for="${pid}veStyleBorderWidth" class="field-label col-12 mb-2"
									title="<@spring.message code='dashboard.veditor.style.borderWidth.desc' />">
									<@spring.message code='dashboard.veditor.style.borderWidth' />
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
								</label>
								<div class="field-input col-12">
									<div class="flex justify-content-center">
										<p-inputtext id="${pid}veStyleBorderColor" v-model="pm.vepms.style['border-color']" type="text"
											class="input flex-grow-1 mr-1" name="border-color">
										</p-inputtext>
										<p-colorpicker v-model="pm.vepmStyleProxy['border-color']"
											default-color="FFFFFF" class="flex-grow-0 preview-h-full"
											@change="onVeStyleColorPickerChange($event, 'border-color')">
										</p-colorpicker>
									</div>
								</div>
							</div>
							<div class="field grid">
								<label for="${pid}veStyleBorderStyle" class="field-label col-12 mb-2">
									<@spring.message code='dashboard.veditor.style.borderStyle' />
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
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto"></div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='position' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto"></div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='font' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto"></div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='gridLayout' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto"></div>
					</p-tabpanel>
					<p-tabpanel header="<@spring.message code='flexLayout' />">
						<div class="ve-style-tabpanel-content px-2 overflow-y-auto"></div>
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
							<div class="field grid mb-0">
								<label for="${pid}veStyleSyncChartTheme" class="field-label col-12 mb-2"
									 title="<@spring.message code='dashboard.veditor.style.syncChartTheme.desc' />">
									<@spring.message code='dashboard.veditor.style.syncChartTheme' />
								</label>
								<div class="field-input col-12">
									<p-selectbutton v-model="pm.vepms.style['syncChartTheme']" :options="pm.booleanOptions"
										option-label="name" option-value="value" class="input">
									</p-selectbutton>
								</div>
							</div>
						</div>
					</p-tabpanel>
				</p-tabview>
			</div>
			<div class="page-form-foot flex-grow-0 pt-3 text-center h-opts">
				<p-button type="submit" label="<@spring.message code='confirm' />"></p-button>
			</div>
		</form>
	</div>
</p-dialog>

<script>
(function(po)
{
	po.veDftChartThemeModel = function()
	{
		var re = { graphColors: [], graphRangeColors: [] };
		return re;
	};
	
	po.initVePanelHelperSrc = function(form, formModel)
	{
		po.element(".help-src", form).click(function()
		{
			var $this = $(this);
			var helpValue = ($this.attr("help-value") || "");
			var helpTarget = po.element(".help-target", $this.closest(".field-input"));
			var targetName = helpTarget.attr("name");
			
			if(targetName)
				formModel[targetName] = helpValue;
			
			helpTarget.focus();
		});
	};

	po.prettyChartOptionsStr = function(chartOptionsStr)
	{
		if(chartOptionsStr && /^\s*[\{\[]/.test(chartOptionsStr))
		{
			var obj = chartFactory.evalSilently(chartOptionsStr, chartOptionsStr);
			
			if(!chartFactory.isString(obj))
				chartOptionsStr = JSON.stringify(obj, null, '\t');
		}
		
		return (chartOptionsStr || "");
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
	
	po.cssColorsToHexStrs = function(cssColors)
	{
		if(!cssColors)
			return [];
		
		var re = [];
		
		$.each(cssColors, function(i, cssColor)
		{
			re.push(po.cssColorToHexStr(cssColor));
		});
		
		return re;
	};
	
	po.cssColorToHexStr = function(cssColor)
	{
		if(!cssColor)
			return "";
		else if(cssColor.charAt(0) == '#')
			return cssColor.substr(1);
		else
			return cssColor;
	};
	
	po.hexStrToCssColor = function(hexStr, dftCssColor)
	{
		if(!hexStr)
			return dftCssColor;
		else if(hexStr.charAt(0) != '#')
			return "#" + hexStr;
		else
			return hexStr;
	};
	
	po.showVeGridLayoutPanel = function(showFillParent)
	{
		showFillParent = (showFillParent == null ? false : showFillParent);
		
		var pm = po.vuePageModel();
		
		pm.veGridLayoutPanelShowFillParent = showFillParent;
		pm.vepms.gridLayout.fillParent = showFillParent;
		pm.vepss.gridLayoutShown = true;
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
		pm.vepms.video = $.extend(true, {}, model);
		pm.vepss.videoShown = true;
	};
	
	po.showVeDashboardSizePanel = function(model)
	{
		var pm = po.vuePageModel();
		pm.vepms.dashboardSize = $.extend(true, {}, model);
		pm.vepss.dashboardSizeShown = true;
	};
	
	po.showVeChartOptionsPanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartOptions = submitHandler;
		pm.vepms.chartOptions = $.extend(true, {}, model);
		if(title)
			pm.vepts.chartOptions = title; 
		pm.vepss.chartOptionsShown = true;
	};
	
	po.showVeChartThemePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.chartTheme = submitHandler;
		pm.vepms.chartTheme = $.extend(true, po.veDftChartThemeModel(), model);
		pm.vepmChartThemeProxy = $.extend(true, po.veDftChartThemeModel(), po.cssColorToHexStrChartTheme(model));
		if(title)
			pm.vepts.chartTheme = title;
		pm.vepss.chartThemeShown = true;
	};
	
	po.showVeStylePanel = function(submitHandler, model, title)
	{
		var pm = po.vuePageModel();
		pm.veshs.style = submitHandler;
		pm.vepms.style = $.extend(true, {}, model);
		pm.vepmStyleProxy = $.extend(true, {}, po.cssColorToHexStrStyle(model));
		if(title)
			pm.vepts.style = title;
		pm.vepss.styleShown = true;
	};
	
	po.setupResourceEditorForms = function()
	{
		po.vuePageModel(
		{
			//可视编辑操作对话框是否显示
			vepss:
			{
				gridLayoutShown: false,
				textElementShown: false,
				imageShown: false,
				hyperlinkShown: false,
				videoShown: false,
				dashboardSizeShown: false,
				chartOptionsShown: false,
				chartThemeShown: false,
				styleShown: false,
			},
			//可视编辑操作对话框标题
			vepts:
			{
				gridLayout: "<@spring.message code='gridLayout' />",
				textElement: "<@spring.message code='textElement' />",
				image: "<@spring.message code='image' />",
				hyperlink: "<@spring.message code='hyperlink' />",
				video: "<@spring.message code='video' />",
				dashboardSize: "<@spring.message code='dashboardSize' />",
				chartOptions: "<@spring.message code='chartOptions' />",
				chartTheme: "<@spring.message code='chartTheme' />",
				style: "<@spring.message code='style' />"
			},
			//可视编辑操作对话框表单模型
			vepms:
			{
				gridLayout: { fillParent: false },
				textElement: { content: "" },
				image: {},
				hyperlink: {},
				video: {},
				dashboardSize: { scale: "auto" },
				chartOptions: { value: "" },
				chartTheme: po.veDftChartThemeModel(),
				style: {}
			},
			//可视编辑操作对话框提交处理函数
			veshs:
			{
				textElement: function(model){},
				image: function(model){},
				hyperlink: function(model){},
				video: function(model){},
				chartOptions: function(model){},
				chartTheme: function(model){},
				style: function(model){}
			},
			veGridLayoutPanelShowFillParent: false,
			dashboardSizeScaleOptions:
			[
				{ name: "<@spring.message code='auto' />", value: "auto" },
				{ name: "100%", value: 100 },
				{ name: "75%", value: 75 },
				{ name: "50%", value: 50 },
				{ name: "25%", value: 25 }
			],
			vepmChartThemeProxy: po.veDftChartThemeModel(),
			vepmStyleProxy: {},
		});
		
		var pm = po.vuePageModel();
		
		po.vueMethod(
		{
			onVeGridLayoutPanelShow: function()
			{
				var form = po.elementOfId("${pid}veGridLayoutForm", document.body);
				
				po.initVePanelHelperSrc(form, pm.vepms.gridLayout);
				
				po.setupSimpleForm(form, pm.vepms.gridLayout, function()
				{
					if(po.insertVeGridLayout(pm.vepms.gridLayout) !== false)
					{
						pm.vepms.gridLayout = { fillParent: false };
						pm.vepss.gridLayoutShown = false;
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
				
				po.setupSimpleForm(form, pm.vepms.video, function()
				{
					if(pm.veshs.video(pm.vepms.video) !== false)
					{
						pm.vepms.video = {};
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
				po.setCodeTextTimeout(codeEditor, po.prettyChartOptionsStr(pm.vepms.chartOptions.value), true);
				
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
						pm.vepmChartThemeProxy = po.veDftChartThemeModel();
						pm.vepss.chartThemeShown = false;
					}
				});
			},
			
			onVeChartThemeColorPickerChange: function(e, propName, idx)
			{
				var proxy = pm.vepmChartThemeProxy;
				var chartTheme = pm.vepms.chartTheme;
				
				//XXX 使用e.value在第一次时返回的值不是新值！？
				var pickColor = (idx != null ? proxy[propName][idx] : proxy[propName]);
				
				if(idx != null)
					chartTheme[propName][idx] = po.hexStrToCssColor(pickColor, chartTheme[propName][idx]);
				else
					chartTheme[propName] = po.hexStrToCssColor(pickColor, chartTheme[propName]);
			},
			
			onVeChartThemeAddGraphColor: function()
			{
				var proxy = pm.vepmChartThemeProxy;
				var chartTheme = pm.vepms.chartTheme;
				
				proxy.graphColors.push("");
				chartTheme.graphColors.push("");
			},
			
			onVeChartThemeAddGraphRangeColor: function()
			{
				var proxy = pm.vepmChartThemeProxy;
				var chartTheme = pm.vepms.chartTheme;
				
				proxy.graphRangeColors.push("");
				chartTheme.graphRangeColors.push("");
			},
			
			onVeChartThemeRemoveGraphColor: function(e, idx)
			{
				var proxy = pm.vepmChartThemeProxy;
				var chartTheme = pm.vepms.chartTheme;
				
				proxy.graphColors.splice(idx, 1);
				chartTheme.graphColors.splice(idx, 1);
			},
			
			onVeChartThemeRemoveGraphRangeColor: function(e, idx)
			{
				var proxy = pm.vepmChartThemeProxy;
				var chartTheme = pm.vepms.chartTheme;
				
				proxy.graphRangeColors.splice(idx, 1);
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
						pm.vepmStyleProxy = {};
						pm.vepss.styleShown = false;
					}
				});
			},
			
			onVeStyleColorPickerChange: function(e, propName)
			{
				var proxy = pm.vepmStyleProxy;
				var style = pm.vepms.style;
				
				//XXX 使用e.value在第一次时返回的值不是新值！？
				var pickColor = proxy[propName];
				
				style[propName] = po.hexStrToCssColor(pickColor, style[propName]);
			}
		});
	};
})
(${pid});
</script>