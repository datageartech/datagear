<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
编辑表格功能HTML片段。
--%>
<div class="edit-grid">
	<div class="edit-grid-switch-wrapper">
		<label for="${pageId}-editGridSwitch">编辑表格</label>
		<input id="${pageId}-editGridSwitch" type="checkbox" value="1" />
	</div>
	<div class="edit-grid-operation">
		<button id="${pageId}-editGridButtonCancel" type="button" class="button-cancel" style="display: none;">恢复</button>
		<button id="${pageId}-editGridButtonCancelAll" type="button" class="button-cancel-all" style="display: none;">全部恢复</button>
		<button id="${pageId}-editGridButtonSave" type="button" class="button-save recommended" style="display: none;">保存</button>
	</div>
</div>