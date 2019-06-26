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
<div class="form-item">
	<div class="form-item-label"><@spring.message code='dataExchange.dataFormat.binaryFormat' /></div>
	<div class="form-item-value">
		<div id="${pageId}-binaryFormat">
			<label for="${pageId}-binaryFormat-0"><@spring.message code='dataExchange.dataFormat.binaryFormat.HEX' /></label>
			<input id="${pageId}-binaryFormat-0" type="radio" name="dataFormat.binaryFormat" value="HEX" />
			<label for="${pageId}-binaryFormat-1"><@spring.message code='dataExchange.dataFormat.binaryFormat.Base64' /></label>
			<input id="${pageId}-binaryFormat-1" type="radio" name="dataFormat.binaryFormat" value="BASE64" />
			<label for="${pageId}-binaryFormat-2"><@spring.message code='dataExchange.dataFormat.binaryFormat.NULL' /></label>
			<input id="${pageId}-binaryFormat-2" type="radio" name="dataFormat.binaryFormat" value="NULL" />
		</div>
	</div>
</div>