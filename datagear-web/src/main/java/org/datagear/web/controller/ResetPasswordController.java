/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.User;
import org.datagear.management.service.UserService;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	
	public static final String STEP_FILL_USER_INFO = "fillUserInfo";
	public static final String STEP_CHECK_USER = "checkUser";
	public static final String STEP_SET_NEW_PASSWORD = "setNewPassword";
	public static final String STEP_FINISH = "finish";

	@Autowired
	private UserService userService;

	@Autowired
	private File resetPasswordCheckFileDirectory;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private UsernameLoginLatch usernameLoginLatch;

	public ResetPasswordController()
	{
		super();
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public File getResetPasswordCheckFileDirectory()
	{
		return resetPasswordCheckFileDirectory;
	}

	public void setResetPasswordCheckFileDirectory(File resetPasswordCheckFileDirectory)
	{
		this.resetPasswordCheckFileDirectory = resetPasswordCheckFileDirectory;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	public UsernameLoginLatch getUsernameLoginLatch()
	{
		return usernameLoginLatch;
	}

	public void setUsernameLoginLatch(UsernameLoginLatch usernameLoginLatch)
	{
		this.usernameLoginLatch = usernameLoginLatch;
	}

	@RequestMapping
	public String resetPassword(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		HttpSession session = request.getSession();

		ResetPasswordStep resetPasswordStep = (ResetPasswordStep) session.getAttribute(KEY_STEP);

		if (resetPasswordStep == null || request.getParameter("step") == null)
		{
			resetPasswordStep = new ResetPasswordStep(4);
			resetPasswordStep.setStep(1, STEP_FILL_USER_INFO);

			session.setAttribute(KEY_STEP, resetPasswordStep);
		}

		model.addAttribute("step", resetPasswordStep);
		setDetectNewVersionScriptAttr(request, response, this.applicationProperties.isDisableDetectNewVersion());

		return "/reset_password";
	}

	@RequestMapping(value = "/fillUserInfo", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> fillUserInfo(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestBody FillUserInfoForm form)
	{
		String username = form.getUsername();

		if (isEmpty(username))
			throw new IllegalInputException();

		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (isEmpty(resetPasswordStep))
			return optStepNotInSessionResponseEntity(request);

		User user = this.userService.getByNameSimple(username);

		if (user == null)
			return optFailResponseEntity(request, "usernameNotExists", username);

		resetPasswordStep.setUsername(username);
		resetPasswordStep.setCheckFileName(IDUtil.uuid());
		resetPasswordStep.setCheckFileTip(getMessage(request, "resetPassword.pleaseCreateCheckFile",
				this.resetPasswordCheckFileDirectory.getAbsolutePath(), resetPasswordStep.getCheckFileName()));
		resetPasswordStep.setStep(2, STEP_CHECK_USER);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/checkUser", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> checkUser(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (isEmpty(resetPasswordStep) || isEmpty(resetPasswordStep.getUsername())
				|| isEmpty(resetPasswordStep.getCheckFileName()))
			return optStepNotInSessionResponseEntity(request);

		File checkFile = FileUtil.getFile(this.resetPasswordCheckFileDirectory, resetPasswordStep.getCheckFileName(),
				false);

		if (!checkFile.exists())
			return optFailDataResponseEntity(request, "resetPassword.checkFileNotExists");

		resetPasswordStep.setCheckOk(true);
		resetPasswordStep.setStep(3, STEP_SET_NEW_PASSWORD);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/setNewPassword", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> setNewPassword(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestBody SetNewPasswordForm form)
	{
		String password = form.getPassword();

		if (isEmpty(password))
			throw new IllegalInputException();

		ResetPasswordStep resetPasswordStep = getResetPasswordStep(request);

		if (isEmpty(resetPasswordStep) || isEmpty(resetPasswordStep.getUsername())
				|| isEmpty(resetPasswordStep.getCheckFileName()) || !resetPasswordStep.isCheckOk())
			return optStepNotInSessionResponseEntity(request);

		String username = resetPasswordStep.getUsername();
		User user = this.userService.getByNameSimple(username);

		if (user == null)
			return optFailResponseEntity(request, "usernameNotExists", username);

		this.userService.updatePasswordById(user.getId(), password, true);
		resetPasswordStep.setStep(4, STEP_FINISH);
		this.usernameLoginLatch.clear(user.getName());

		return optSuccessResponseEntity(request);
	}

	protected ResponseEntity<OperationMessage> optStepNotInSessionResponseEntity(HttpServletRequest request)
	{
		return optFailDataResponseEntity(request, "resetPassword.stepNotInSession");
	}

	protected ResetPasswordStep getResetPasswordStep(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		ResetPasswordStep resetPasswordStep = (ResetPasswordStep) session.getAttribute(KEY_STEP);

		return resetPasswordStep;
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

		/**用户名*/
		private String username;
		
		/** 用于校验的服务端文件名 */
		private String checkFileName;

		/** 新建校验文件提示信息 */
		private String checkFileTip = "";

		/** 校验是否通过 */
		private boolean checkOk = false;

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

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public String getCheckFileName()
		{
			return checkFileName;
		}

		public void setCheckFileName(String checkFileName)
		{
			this.checkFileName = checkFileName;
		}

		public String getCheckFileTip()
		{
			return checkFileTip;
		}

		public void setCheckFileTip(String checkFileTip)
		{
			this.checkFileTip = checkFileTip;
		}

		public boolean isCheckOk()
		{
			return checkOk;
		}

		public void setCheckOk(boolean checkOk)
		{
			this.checkOk = checkOk;
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

	public static class FillUserInfoForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String username;

		public FillUserInfoForm()
		{
			super();
		}

		public String getUsername()
		{
			return username;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}
	}

	public static class SetNewPasswordForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String password;

		private String confirmPassword;

		public SetNewPasswordForm()
		{
			super();
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}

		public String getConfirmPassword()
		{
			return confirmPassword;
		}

		public void setConfirmPassword(String confirmPassword)
		{
			this.confirmPassword = confirmPassword;
		}
	}
}
