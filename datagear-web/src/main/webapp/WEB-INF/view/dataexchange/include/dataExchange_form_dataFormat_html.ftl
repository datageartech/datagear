<#--
DataFormat defaultDataFormat 默认数据格式，不允许为null
-->
<div class="form-item">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.dateFormat' /></div>
	<div class="form-item-value">
		<input type="text" name="dataFormat.dateFormat" value="${defaultDataFormat.dateFormat}" class="ui-widget ui-widget-content" />
	</div>
</div>
<div class="form-item">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.timeFormat' /></div>
	<div class="form-item-value">
		<input type="text" name="dataFormat.timeFormat" value="${defaultDataFormat.timeFormat}" class="ui-widget ui-widget-content" />
	</div>
</div>
<div class="form-item">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.timestampFormat' /></div>
	<div class="form-item-value">
		<input type="text" name="dataFormat.timestampFormat" value="${defaultDataFormat.timestampFormat}" class="ui-widget ui-widget-content" />
	</div>
</div>
<div class="form-item">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.numberFormat' /></div>
	<div class="form-item-value">
		<input type="text" name="dataFormat.numberFormat" value="${defaultDataFormat.numberFormat}" class="ui-widget ui-widget-content" />
	</div>
</div>
<div class="form-item form-item-binaryFormat">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.binaryFormat' /></div>
	<div class="form-item-value">
		<input type="text" name="dataFormat.binaryFormat" value="${defaultDataFormat.binaryFormat}" class="input-binaryFormat ui-widget ui-widget-content" />
		<div id="${pageId}-binaryFormat">
			<button type="button" class="binaryFormatSetButton" value="Hex"><@spring.message code='dataExchange.dataFormat.binaryFormat.HEX' /></button>
			<button type="button" class="binaryFormatSetButton" value="Base4"><@spring.message code='dataExchange.dataFormat.binaryFormat.Base64' /></button>
			<button type="button" class="binaryFormatSetButton" value="NULL"><@spring.message code='dataExchange.dataFormat.binaryFormat.NULL' /></button>
		</div>
	</div>
</div>