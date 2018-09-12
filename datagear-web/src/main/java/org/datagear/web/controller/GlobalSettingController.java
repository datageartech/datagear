/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.GlobalSetting;
import org.datagear.management.domain.SmtpSetting;
import org.datagear.management.domain.User;
import org.datagear.management.domain.SmtpSetting.ConnectionType;
import org.datagear.management.service.GlobalSettingService;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.MailUtils;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局设置控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/globalSetting")
public class GlobalSettingController extends AbstractController
{
	@Autowired
	private GlobalSettingService globalSettingService;

	public GlobalSettingController()
	{
		super();
	}

	public GlobalSettingController(MessageSource messageSource, ClassDataConverter classDataConverter,
			GlobalSettingService globalSettingService)
	{
		super(messageSource, classDataConverter);
		this.globalSettingService = globalSettingService;
	}

	public GlobalSettingService getGlobalSettingService()
	{
		return globalSettingService;
	}

	public void setGlobalSettingService(GlobalSettingService globalSettingService)
	{
		this.globalSettingService = globalSettingService;
	}

	@RequestMapping
	public String globalSetting(HttpServletRequest request, org.springframework.ui.Model model)
	{
		GlobalSetting globalSetting = this.globalSettingService.get();

		if (globalSetting == null)
			globalSetting = new GlobalSetting();

		if (!globalSetting.hasSmtpSetting())
		{
			SmtpSetting smtpSetting = new SmtpSetting();
			smtpSetting.setPort(25);
			smtpSetting.setConnectionType(ConnectionType.PLAIN);

			globalSetting.setSmtpSetting(smtpSetting);
		}

		model.addAttribute("globalSetting", globalSetting);

		return "/global_setting";
	}

	@RequestMapping(value = "/save", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			GlobalSetting globalSetting)
	{
		User user = WebUtils.getUser(request, response);

		boolean save = this.globalSettingService.save(user, globalSetting);

		if (!save)
			throw new RecordNotFoundOrPermissionDeniedException();

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/testSmtp", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> testSmtp(HttpServletRequest request, HttpServletResponse response,
			GlobalSetting globalSetting, @RequestParam("testSmtpRecevierEmail") String testSmtpRecevierEmail)
			throws MessagingException
	{
		String subject = getMessage(request, "globalSetting.testSmtp.emailSubject") + "-"
				+ getMessage(request, "app.name");
		String content = getMessage(request, "globalSetting.testSmtp.emailContent");

		try
		{
			MailUtils.send(globalSetting.getSmtpSetting(), testSmtpRecevierEmail, subject, content);
		}
		catch (MessagingException exception)
		{
			ResponseEntity<OperationMessage> responseEntity = buildOperationMessageFailResponseEntity(request,
					HttpStatus.INTERNAL_SERVER_ERROR, buildMessageCode("testSmtp.sendEmail.MessagingException"));
			responseEntity.getBody().setThrowable(exception);

			return responseEntity;
		}

		return buildOperationMessageSuccessResponseEntity(request, buildMessageCode("testSmtp.sendEmail.success"));
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("globalSetting", code);
	}
}
