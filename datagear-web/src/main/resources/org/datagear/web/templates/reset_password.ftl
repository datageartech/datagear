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
<#assign LoginController=statics['org.datagear.web.controller.LoginController']>
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<#if step.finalStep>
<meta http-equiv="refresh" content="4;url=${contextPath}/login">
</#if>
<title>
	<@spring.message code='module.resetPassword' />
	<#include "include/html_app_name_suffix.ftl">
</title>
</head>
<body class="m-0 surface-ground">
<#include "include/page_obj.ftl">
<div id="${pid}" class="page page-form horizontal">
	<div class="flex flex-column h-screen m-0">
		<#include "include/page_main_header.ftl">
		<div class="flex-grow-1 p-0">
			<div class="grid grid-nogutter justify-content-center">
				<p-card class="col-10 md:col-8 p-card mt-6">
					<template #title><@spring.message code='module.resetPassword' /></template>
					<template #content>
					<form id="${pid}form" class="flex flex-column">
						<div class="page-form-content flex-grow-1 px-2 py-1 overflow-y-auto">
							<div class="mb-5">
								<p-steps :model="pm.stepItems" :readonly="true"></p-steps>
							</div>
							<div class="grid grid-nogutter justify-content-center pt-3">
								<div class="col-12 md:col-9">
									<#if step.step == 1>
									<div class="field grid">
										<label for="${pid}username" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='username' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<p-inputtext id="${pid}nameuser" v-model="fm.username" type="text" class="input w-full"
								        		name="username" required maxlength="50" autofocus>
								        	</p-inputtext>
								        </div>
									</div>
									
									<#elseif step.step == 2>
									<div class="field grid">
										<label for="${pid}username" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='username' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<p-inputtext id="${pid}nameuser" v-model="fm.username" type="text" class="input w-full"
								        		name="username" maxlength="50" readonly>
								        	</p-inputtext>
								        </div>
									</div>
									<div class="field grid">
										<label for="${pid}checkFile" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='checkFile' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<div id="${pid}checkFile" class="line-height-3">
								        		${step.checkFileTip?no_esc}
								        	</div>
								        	<div class="desc text-color-secondary mt-3">
								        		<small><@spring.message code='resetPassword.checkFile.desc1' /></small>
								        	</div>
								        	<div class="desc text-color-secondary">
								        		<small><@spring.message code='resetPassword.checkFile.desc2' /></small>
								        	</div>
								        </div>
									</div>
									
									<#elseif step.step == 3>
									<div class="field grid">
										<label for="${pid}username" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='username' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<p-inputtext id="${pid}nameuser" v-model="fm.username" type="text" class="input w-full"
								        		name="username" maxlength="50" readonly>
								        	</p-inputtext>
								        </div>
									</div>
									<div class="field grid">
										<label for="${pid}password" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='password' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<p-password id="${pid}password" v-model="fm.password" class="input w-full"
								        		input-class="w-full" toggle-mask :feedback="false"
								        		name="password" required maxlength="50" autocomplete="new-password" autofocus>
								        	</p-password>
								        </div>
									</div>
									<div class="field grid">
										<label for="${pid}confirmPassword" class="field-label col-12 mb-2 md:col-3 md:mb-0">
											<@spring.message code='confirmPassword' />
										</label>
								        <div class="field-input col-12 md:col-9">
								        	<p-password id="${pid}confirmPassword" v-model="fm.confirmPassword" class="input w-full"
								        		input-class="w-full" toggle-mask :feedback="false"
								        		name="confirmPassword" required maxlength="50" autocomplete="new-password">
								        	</p-password>
								        </div>
									</div>
									
									<#elseif step.finalStep>
									<div class="field grid justify-content-center">
										<div class="pb-5">
											<i class="pi pi-check-circle text-xl mr-2"></i>
											<span>
												<#assign messageArgs=['${contextPath}/login'] />
												<@spring.messageArgs code='resetPassword.step.finish.content' args=messageArgs />
											</span>
										</div>
									</div>
									</#if>
								</div>
							</div>
						</div>
						<div class="page-form-foot flex-grow-0 flex justify-content-center gap-2 pt-2">
							<p-button type="button" label="<@spring.message code='restart' />"
								class="p-button-secondary mx-2" @click="onRestart" <#if step.firstStep>disabled="disabled"</#if> >
							</p-button>
							<p-button type="submit" label="<@spring.message code='nextStep' />" class="mx-2"
								<#if step.finalStep>disabled="disabled"</#if> >
							</p-button>
						</div>
					</form>
					</template>
				</p-card>
			</div>
		</div>
	</div>
</div>
<#include "include/page_form.ftl">
<script>
(function(po)
{
	po.submitUrl = "/resetPassword/${step.action}";
	
	po.vuePageModel(
	{
		stepItems:
		[
			{
				label: "<@spring.message code='resetPassword.step.fillUserInfo' />",
				class: "step-1"
			},
			{
				label: "<@spring.message code='resetPassword.step.checkUser' />",
				class: "step-2"
			},
			{
				label: "<@spring.message code='resetPassword.step.setNewPassword' />",
				class: "step-3"
			},
			{
				label: "<@spring.message code='resetPassword.step.finish' />",
				class: "step-4"
			}
		]
	});
	
	var formModel = $.unescapeHtmlForJson(<@writeJson var=step />);
	po.setupForm(formModel,
	{
		tipSuccess: false,
		success: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/resetPassword?step";
		}
	},
	function()
	{
		var options =
		{
			rules:
			{
				<#if step.step == 3>
				"confirmPassword":
				{
					"equalTo" : po.elementOfName("password")
				}
				</#if>
			}
		};
		
		return options;
	});
	
	po.vueMethod(
	{
		onRestart: function()
		{
			(window.top ? window.top : window).location.href="${contextPath}/resetPassword";
		}
	});
	
	po.vueMounted(function()
	{
		po.element(".step-${step.step}").removeClass("p-disabled").addClass("p-highlight p-steps-current");
	});
})
(${pid});
</script>
<#include "include/page_vue_mount.ftl">
</body>
</html>