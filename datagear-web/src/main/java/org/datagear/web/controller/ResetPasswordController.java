/*
 * Copyright 2018-2024 datagear.tech
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
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.beans.BeanUtils;
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
	public static final String STEP_CHECK_USER_INFO = "checkUserInfo";
	public static final String STEP_SET_NEW_PASSWORD = "setNewPassword";
	public static final String STEP_FINISH = "finish";

	@Autowired
	private UserService userService;

	@Autowired
	private File resetPasswordCheckFileDirectory;

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
		ResetPasswordStep resetPasswordStep = getSessionResetPasswordStep(request);

		if (resetPasswordStep == null || request.getParameter("step") == null)
		{
			resetPasswordStep = createInitResetPasswordStep(request, response);
			setSessionResetPasswordStep(request, resetPasswordStep);
		}

		model.addAttribute("step", toResetPasswordStepView(request, response, resetPasswordStep));
		WebUtils.setEnableDetectNewVersionRequest(request);

		return "/reset_password";
	}

	@RequestMapping(value = "/" + STEP_FILL_USER_INFO, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> fillUserInfo(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestBody FillUserInfoForm form)
	{
		String username = form.getUsername();

		if (isEmpty(username))
			throw new IllegalInputException();

		ResetPasswordStep resetPasswordStep = getSessionResetPasswordStep(request);

		if (isEmpty(resetPasswordStep))
			return optStepNotInSessionResponseEntity(request);

		User user = this.userService.getByNameSimple(username);

		if (user == null)
			return optFailResponseEntity(request, "usernameNotExists", username);

		resetPasswordStep.setUsername(username);
		resetPasswordStep.setCheckFileName(IDUtil.uuid());
		resetPasswordStep.setCheckFileTip(
				getMessage(request, "resetPassword.pleaseCreateCheckFile", resetPasswordStep.getCheckFileName()));
		resetPasswordStep.setStep(2, STEP_CHECK_USER_INFO);

		setSessionResetPasswordStep(request, resetPasswordStep);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/" + STEP_CHECK_USER_INFO, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> checkUserInfo(HttpServletRequest request, HttpServletResponse response,
			Model model)
	{
		ResetPasswordStep resetPasswordStep = getSessionResetPasswordStep(request);

		if (isEmpty(resetPasswordStep) || isEmpty(resetPasswordStep.getUsername())
				|| isEmpty(resetPasswordStep.getCheckFileName()))
			return optStepNotInSessionResponseEntity(request);

		File checkFile = FileUtil.getFile(this.resetPasswordCheckFileDirectory, resetPasswordStep.getCheckFileName(),
				false);

		if (!checkFile.exists())
			return optFailResponseEntity(request, "resetPassword.checkFileNotExists");

		resetPasswordStep.setCheckOk(true);
		resetPasswordStep.setStep(3, STEP_SET_NEW_PASSWORD);

		setSessionResetPasswordStep(request, resetPasswordStep);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/" + STEP_SET_NEW_PASSWORD, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> setNewPassword(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestBody SetNewPasswordForm form)
	{
		String password = form.getPassword();

		if (isEmpty(password))
			throw new IllegalInputException();

		ResetPasswordStep resetPasswordStep = getSessionResetPasswordStep(request);

		if (isEmpty(resetPasswordStep) || isEmpty(resetPasswordStep.getUsername()) || !resetPasswordStep.isCheckOk())
			return optStepNotInSessionResponseEntity(request);

		String username = resetPasswordStep.getUsername();
		User user = this.userService.getByNameSimple(username);

		if (user == null)
			return optFailResponseEntity(request, "usernameNotExists", username);

		this.userService.updatePasswordById(user.getId(), password, true);
		resetPasswordStep.setStep(4, STEP_FINISH);
		this.usernameLoginLatch.clear(user.getName());

		setSessionResetPasswordStep(request, resetPasswordStep);

		return optSuccessResponseEntity(request);
	}

	protected ResponseEntity<OperationMessage> optStepNotInSessionResponseEntity(HttpServletRequest request)
	{
		return optFailDataResponseEntity(request, "resetPassword.stepNotInSession");
	}

	/**
	 * 获取会话中的{@linkplain ResetPasswordStep}。
	 * <p>
	 * 如果修改了获取的{@linkplain ResetPasswordStep}的状态，应在修改之后调用{@linkplain #setSessionResetPasswordStep(HttpServletRequest, ResetPasswordStep)}，
	 * 以为可能扩展的分布式会话提供支持。
	 * </p>
	 * 
	 * @param request
	 * @return 没有则返回{@code null}
	 */
	protected ResetPasswordStep getSessionResetPasswordStep(HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		ResetPasswordStep resetPasswordStep = (ResetPasswordStep) session.getAttribute(KEY_STEP);

		return resetPasswordStep;
	}

	/**
	 * 设置会话中的{@linkplain ResetPasswordStep}。
	 * 
	 * @param request
	 * @param step
	 */
	protected void setSessionResetPasswordStep(HttpServletRequest request, ResetPasswordStep step)
	{
		HttpSession session = request.getSession();
		session.setAttribute(KEY_STEP, step);
	}

	/**
	 * 创建初始{@linkplain ResetPasswordStep}。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected ResetPasswordStep createInitResetPasswordStep(HttpServletRequest request, HttpServletResponse response)
	{
		int totalStep = 4;

		ResetPasswordStep step = new ResetPasswordStep(totalStep);
		step.setStep(1, STEP_FILL_USER_INFO);

		return step;
	}

	/**
	 * 转换为页面展示信息。
	 * 
	 * @param request
	 * @param response
	 * @param step
	 * @return
	 */
	protected ResetPasswordStep toResetPasswordStepView(HttpServletRequest request, HttpServletResponse response,
			ResetPasswordStep step)
	{
		ResetPasswordStep re = new ResetPasswordStep(step.getStep());
		BeanUtils.copyProperties(step, re);

		re.setPassword(null);

		return re;
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
	}
}
