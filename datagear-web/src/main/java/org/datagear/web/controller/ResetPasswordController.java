/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.GlobalSetting;
import org.datagear.management.domain.ResetPasswordRequest;
import org.datagear.management.domain.User;
import org.datagear.management.service.GlobalSettingService;
import org.datagear.management.service.ResetPasswordRequestService;
import org.datagear.management.service.UserService;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.ResetPasswordRequestConfig;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.util.MailUtils;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 重设密码控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/resetPassword")
public class ResetPasswordController extends AbstractController
{
	public static final String KEY_STEP = ResetPasswordStep.class.getSimpleName();

	public static final String MESSAGE_KEY_BASENAME = "resetPassword";

	@Autowired
	private UserService userService;

	@Autowired
	private GlobalSettingService globalSettingService;

	@Autowired
	private ResetPasswordRequestService resetPasswordRequestService;

	@Autowired
	private ResetPasswordRequestConfig resetPasswordRequestConfig;

	public ResetPasswordController()
	{
		super();
	}

	public ResetPasswordController(UserService userService, GlobalSettingService globalSettingService,
			float resetPasswordAdminSkipCheckDelayHours, ResetPasswordRequestService resetPasswordRequestService,
			ResetPasswordRequestConfig resetPasswordRequestConfig)
	{
		super();
		this.userService = userService;
		this.globalSettingService = globalSettingService;
		this.resetPasswordRequestService = resetPasswordRequestService;
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public GlobalSettingService getGlobalSettingService()
	{
		return globalSettingService;
	}

	public void setGlobalSettingService(GlobalSettingService globalSettingService)
	{
		this.globalSettingService = globalSettingService;
	}

	public ResetPasswordRequestConfig getResetPasswordRequestConfig()
	{
		return resetPasswordRequestConfig;
	}

	public void setResetPasswordRequestConfig(ResetPasswordRequestConfig resetPasswordRequestConfig)
	{
		this.resetPasswordRequestConfig = resetPasswordRequestConfig;
	}

	public ResetPasswordRequestService getResetPasswordRequestService()
	{
		return resetPasswordRequestService;
	}

	public void setResetPasswordRequestService(ResetPasswordRequestService resetPasswordRequestService)
	{
		this.resetPasswordRequestService = resetPasswordRequestService;
	}

	@RequestMapping
	public String resetPassword(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		ResetPasswordStep resetPasswordStep = (ResetPasswordStep) session.getAttribute(KEY_STEP);

		if (resetPasswordStep == null || request.getParameter("step") == null)
		{
			resetPasswordStep = new ResetPasswordStep(4);
			resetPasswordStep.setStep(1, "fillUserInfo");

			session.setAttribute(KEY_STEP, resetPasswordStep);
		}

		request.setAttribute("step", resetPasswordStep);

		return "/reset_password";
	}

	@RequestMapping(value = "fillUserInfo", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> fillUserInfo(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("username") String username)
	{
		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (resetPasswordStep == null)
			return buildResetPasswordStepNotInSessionResponseEntity(request);

		User user = this.userService.getByName(username);

		if (user == null)
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("userNotExists"), username);

		if (isBlank(user.getEmail()))
		{
			if (User.isAdminUser(user))
			{
				resetPasswordStep.setSkipCheckUserAdmin(true);
				resetPasswordStep.setSkipPasswordDelayHours(this.resetPasswordRequestConfig.getHandleDelayHours());
				resetPasswordStep
						.setSkipReason(getMessage(request, buildMessageCode("admin.emailNotSet"), user.getName()));
			}
			else
			{
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("emailNotSet"), username);
			}
		}

		GlobalSetting globalSetting = this.globalSettingService.get();

		if (globalSetting == null || !globalSetting.hasSmtpSetting())
		{
			if (User.isAdminUser(user))
			{
				resetPasswordStep.setSkipCheckUserAdmin(true);
				resetPasswordStep.setSkipPasswordDelayHours(this.resetPasswordRequestConfig.getHandleDelayHours());
				resetPasswordStep
						.setSkipReason(getMessage(request, buildMessageCode("fillUserInfo.admin.smtpSettingNotSet")));
			}
			else
			{
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("smtpSettingNotSet"));
			}
		}

		resetPasswordStep.setUser(user);

		if (resetPasswordStep.isSkipCheckUserAdmin())
			resetPasswordStep.setStep(3, "setNewPassword");
		else
			resetPasswordStep.setStep(2, "checkUser");

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "sendCheckCode", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> sendCheckCode(HttpServletRequest request, HttpServletResponse response)
	{
		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (resetPasswordStep == null || resetPasswordStep.getUser() == null)
			return buildResetPasswordStepNotInSessionResponseEntity(request);

		User user = resetPasswordStep.getUser();

		GlobalSetting globalSetting = this.globalSettingService.get();

		if (globalSetting == null || !globalSetting.hasSmtpSetting())
		{
			String code = (User.isAdminUser(user) ? buildMessageCode("sendCheckCode.admin.smtpSettingNotSet")
					: buildMessageCode("smtpSettingNotSet"));

			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST, code);
		}

		String checkCode = generateCheckCode();

		String subject = getMessage(request, "resetPassword.sendCheckCode.emailSubject") + "-"
				+ getMessage(request, "app.name");
		String content = getMessage(request, "resetPassword.sendCheckCode.emailContent", checkCode);

		try
		{
			MailUtils.send(globalSetting.getSmtpSetting(), user.getEmail(), subject, content);
		}
		catch (MessagingException e)
		{
			String code = (User.isAdminUser(user) ? buildMessageCode("sendCheckCode.admin.MessagingException")
					: buildMessageCode("sendCheckCode.MessagingException"));

			ResponseEntity<OperationMessage> responseEntity = buildOperationMessageFailResponseEntity(request,
					HttpStatus.INTERNAL_SERVER_ERROR, code);
			responseEntity.getBody().setThrowable(e);

			return responseEntity;
		}

		resetPasswordStep.setEmailCheckCode(checkCode);
		resetPasswordStep.setEmailCheckCodeTime(new Date());

		return buildOperationMessageSuccessResponseEntity(request, buildMessageCode("sendCheckCode.success"));
	}

	@RequestMapping(value = "checkUser", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> checkUser(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "checkCode", required = false) String checkCode)
	{
		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (resetPasswordStep == null || resetPasswordStep.getUser() == null)
			return buildResetPasswordStepNotInSessionResponseEntity(request);

		User user = resetPasswordStep.getUser();

		if (User.isAdminUser(user))
		{
			if (isEmpty(checkCode) && isEmpty(resetPasswordStep.getEmailCheckCode()))
			{
				resetPasswordStep.setSkipCheckUserAdmin(true);
				resetPasswordStep.setSkipPasswordDelayHours(this.resetPasswordRequestConfig.getHandleDelayHours());
				resetPasswordStep.setSkipReason(null);// 原因已在sendCheckCode提示过
			}
			else
			{
				if (isEmpty(resetPasswordStep.getEmailCheckCode()))
					return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
							buildMessageCode("emailCheckCodeNotSent"));

				if (!checkCode.equals(resetPasswordStep.getEmailCheckCode()))
					return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
							buildMessageCode("inputCheckCodeError"));

				if (isEmailCheckCodeOvertime(resetPasswordStep))
					return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
							buildMessageCode("checkCodeOvertime"));

				resetPasswordStep.setInputCheckCode(checkCode);
			}
		}
		else
		{
			if (isEmpty(resetPasswordStep.getEmailCheckCode()))
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("emailCheckCodeNotSent"));

			if (isEmpty(checkCode))
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("inputCheckCodeRequired"));

			if (!checkCode.equals(resetPasswordStep.getEmailCheckCode()))
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("inputCheckCodeError"));

			if (isEmailCheckCodeOvertime(resetPasswordStep))
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("checkCodeOvertime"));

			resetPasswordStep.setInputCheckCode(checkCode);
		}

		resetPasswordStep.setStep(3, "setNewPassword");

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "setNewPassword", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> setNewPassword(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword)
	{
		if (!password.equals(confirmPassword))
			return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
					buildMessageCode("confirmPasswordError"));

		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (resetPasswordStep == null || resetPasswordStep.getUser() == null)
			return buildResetPasswordStepNotInSessionResponseEntity(request);

		User user = resetPasswordStep.getUser();

		if (resetPasswordStep.isSkipCheckUserAdmin())
			;
		else
		{
			if (isBlank(resetPasswordStep.getEmailCheckCode()) || isBlank(resetPasswordStep.getInputCheckCode()))
				return buildResetPasswordStepNotInSessionResponseEntity(request);
		}

		if (resetPasswordStep.isSkipCheckUserAdmin())
		{
			Date currentTime = new Date();
			SqlTimestampFormatter dateFormatter = new SqlTimestampFormatter();

			ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(UUID.gen(), user, password,
					currentTime, request.getRemoteHost());

			resetPasswordRequest = this.resetPasswordRequestService.addIfNone(resetPasswordRequest);

			if (resetPasswordRequest != null)
			{
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						buildMessageCode("setNewPassword.resetPasswordRequestExists"), user.getId(),
						resetPasswordRequest.getPrincipal(),
						dateFormatter.print(new java.sql.Timestamp(resetPasswordRequest.getTime().getTime()),
								WebUtils.getLocale(request)));
			}

			Date effectiveTime = this.resetPasswordRequestConfig.getEffectiveTime(currentTime);
			resetPasswordStep.setSkipPasswordEffectiveTime(
					dateFormatter.print(new java.sql.Timestamp(effectiveTime.getTime()), WebUtils.getLocale(request)));
		}
		else
			this.userService.updatePasswordById(user.getId(), password, true);

		resetPasswordStep.setStep(4, "finish");

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	protected boolean isEmailCheckCodeOvertime(ResetPasswordStep resetPasswordStep)
	{
		boolean checkCodeOvertime = false;

		if (resetPasswordStep.getEmailCheckCodeTime() == null)
			checkCodeOvertime = true;
		else
		{
			long interval = new Date().getTime() - resetPasswordStep.getEmailCheckCodeTime().getTime();
			interval = interval / 1000 / 60;// 分钟数

			if (interval > 10)
				checkCodeOvertime = true;
		}

		return checkCodeOvertime;
	}

	protected ResponseEntity<OperationMessage> buildResetPasswordStepNotInSessionResponseEntity(
			HttpServletRequest request)
	{
		String code = buildMessageCode("resetPasswordStepNotInSession");
		return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST, code);
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode(MESSAGE_KEY_BASENAME, code);
	}

	protected ResetPasswordStep getResetPasswordStep(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		ResetPasswordStep resetPasswordStep = (ResetPasswordStep) session.getAttribute(KEY_STEP);

		return resetPasswordStep;
	}

	protected String generateCheckCode()
	{
		StringBuilder sb = new StringBuilder();

		Random random = new Random();

		for (int i = 0; i < 4; i++)
		{
			int code = random.nextInt(10);
			sb.append(Integer.toString(code));
		}

		return sb.toString();
	}

	/**
	 * 重设密码步骤信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ResetPasswordStep implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 总步骤数 */
		private int total;

		/** 当前步骤 */
		private int step;

		/** 当前步骤活动 */
		private String action;

		/** 重置密码的用户 */
		private User user;

		/** 邮箱校验码 */
		private String emailCheckCode;

		/** 邮箱校验码时间 */
		private Date emailCheckCodeTime;

		/** 用户输入校验码 */
		private String inputCheckCode;

		/** 是否跳过了管理员验证身份步骤 */
		private boolean skipCheckUserAdmin;

		/** 跳过原因 */
		private String skipReason;

		/** 新密码生效延迟 */
		private float skipPasswordDelayHours;

		/** 新密码生效时间 */
		private String skipPasswordEffectiveTime;

		/** 重设新密码 */
		private String password;

		public ResetPasswordStep(int total)
		{
			this.total = total;
		}

		public ResetPasswordStep(int total, int step, String action)
		{
			super();
			this.total = total;
			this.step = step;
			this.action = action;
		}

		public int getTotal()
		{
			return total;
		}

		public void setTotal(int total)
		{
			this.total = total;
		}

		public int getStep()
		{
			return step;
		}

		public void setStep(int step)
		{
			this.step = step;
		}

		public String getAction()
		{
			return action;
		}

		public void setAction(String action)
		{
			this.action = action;
		}

		public User getUser()
		{
			return user;
		}

		public void setUser(User user)
		{
			this.user = user;
		}

		public String getBlurryEmail()
		{
			if (this.user == null || this.user.getEmail() == null)
				return "";

			String email = this.user.getEmail();

			StringBuilder sb = new StringBuilder();

			int atIndex = email.indexOf('@');

			char[] cs = email.toCharArray();

			for (int i = 0; i < cs.length; i++)
			{
				if (i < 2 || i >= atIndex - 2)
					sb.append(cs[i]);
				else
					sb.append('*');
			}

			return sb.toString();
		}

		public String getEmailCheckCode()
		{
			return emailCheckCode;
		}

		public void setEmailCheckCode(String emailCheckCode)
		{
			this.emailCheckCode = emailCheckCode;
		}

		public Date getEmailCheckCodeTime()
		{
			return emailCheckCodeTime;
		}

		public void setEmailCheckCodeTime(Date emailCheckCodeTime)
		{
			this.emailCheckCodeTime = emailCheckCodeTime;
		}

		public String getInputCheckCode()
		{
			return inputCheckCode;
		}

		public void setInputCheckCode(String inputCheckCode)
		{
			this.inputCheckCode = inputCheckCode;
		}

		public boolean isSkipCheckUserAdmin()
		{
			return skipCheckUserAdmin;
		}

		public void setSkipCheckUserAdmin(boolean skipCheckUserAdmin)
		{
			this.skipCheckUserAdmin = skipCheckUserAdmin;
		}

		public String getSkipReason()
		{
			return skipReason;
		}

		public void setSkipReason(String skipReason)
		{
			this.skipReason = skipReason;
		}

		public float getSkipPasswordDelayHours()
		{
			return skipPasswordDelayHours;
		}

		public void setSkipPasswordDelayHours(float skipPasswordDelayHours)
		{
			this.skipPasswordDelayHours = skipPasswordDelayHours;
		}

		public String getSkipPasswordEffectiveTime()
		{
			return skipPasswordEffectiveTime;
		}

		public void setSkipPasswordEffectiveTime(String skipPasswordEffectiveTime)
		{
			this.skipPasswordEffectiveTime = skipPasswordEffectiveTime;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		/**
		 * 设置到步骤。
		 * 
		 * @param step
		 * @param action
		 */
		public void setStep(int step, String action)
		{
			this.step = step;
			this.action = action;
		}

		/**
		 * 是否是第一个步骤。
		 * 
		 * @return
		 */
		public boolean isFirstStep()
		{
			return this.step == 1;
		}

		/**
		 * 当前是否是指定步骤。
		 * 
		 * @param step
		 * @return
		 */
		public boolean isStep(int step)
		{
			return this.step == step;
		}

		/**
		 * 是否在指定步骤之前。
		 * 
		 * @param step
		 * @return
		 */
		public boolean isBefore(int step)
		{
			return this.step < step;
		}

		/**
		 * 是否在指定步骤之后。
		 * 
		 * @param step
		 * @return
		 */
		public boolean isAfter(int step)
		{
			return this.step > step;
		}

		/**
		 * 是否是最终步骤。
		 * 
		 * @return
		 */
		public boolean isFinalStep()
		{
			return this.step == total;
		}
	}

	public static abstract class AbstractResetPasswordException extends ControllerException
	{
		private static final long serialVersionUID = 1L;

		private User user;

		public AbstractResetPasswordException(User user)
		{
			super();
			this.user = user;
		}

		public AbstractResetPasswordException(User user, String message)
		{
			super(message);
			this.user = user;
		}

		public AbstractResetPasswordException(User user, Throwable cause)
		{
			super(cause);
			this.user = user;
		}

		public AbstractResetPasswordException(User user, String message, Throwable cause)
		{
			super(message, cause);
			this.user = user;
		}

		public User getUser()
		{
			return user;
		}
	}

	/**
	 * SMTP未设置。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SmtpSettingNotSetException extends AbstractResetPasswordException
	{
		private static final long serialVersionUID = 1L;

		public SmtpSettingNotSetException(User user)
		{
			super(user);
		}
	}

	/**
	 * 用户email未设置异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class UserEmailNotSetException extends AbstractResetPasswordException
	{
		private static final long serialVersionUID = 1L;

		public UserEmailNotSetException(User user)
		{
			super(user);
		}
	}

	/**
	 * 发送校验码异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SendCheckCodeEmailException extends AbstractResetPasswordException
	{
		private static final long serialVersionUID = 1L;

		public SendCheckCodeEmailException(User user, Throwable cause)
		{
			super(user, cause);
		}
	}

	/**
	 * 校验码错误异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class CheckCodeErrorException extends AbstractResetPasswordException
	{
		private static final long serialVersionUID = 1L;

		public CheckCodeErrorException(User user)
		{
			super(user);
		}
	}

	/**
	 * 校验码超时异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class CheckCodeOvertimeException extends AbstractResetPasswordException
	{
		private static final long serialVersionUID = 1L;

		public CheckCodeOvertimeException(User user)
		{
			super(user);
		}
	}
}
