/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web;

import java.io.Serializable;
import java.util.Date;

import org.datagear.web.util.WebUtils;

/**
 * 重设密码配置。
 * 
 * @author datagear@163.com
 *
 */
public class ResetPasswordRequestConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 处理时延小时数 */
	private float handleDelayHours = 24;

	/** 历时保留天数 */
	private int historyRetainDays = 365;

	public ResetPasswordRequestConfig()
	{
		super();
	}

	public float getHandleDelayHours()
	{
		return handleDelayHours;
	}

	public void setHandleDelayHours(float handleDelayHours)
	{
		this.handleDelayHours = handleDelayHours;
	}

	public int getHistoryRetainDays()
	{
		return historyRetainDays;
	}

	public void setHistoryRetainDays(int historyRetainDays)
	{
		this.historyRetainDays = historyRetainDays;
	}

	/**
	 * 获取时延秒数。
	 * 
	 * @return
	 */
	public int getHandleDelaySeconds()
	{
		return (int) (this.handleDelayHours * 60 * 60);
	}

	/**
	 * 获取请求生效时间。
	 * 
	 * @param requestTime
	 * @return
	 */
	public Date getEffectiveTime(Date requestTime)
	{
		return WebUtils.addSeconds(requestTime, getHandleDelaySeconds());
	}

	/**
	 * 获取历史保留日期。
	 * 
	 * @param historyDate
	 * @return
	 */
	public Date getHistoryRetainDate()
	{
		int seconds = 0 - this.historyRetainDays * 24 * 60 * 60;

		return WebUtils.addSeconds(new Date(), seconds);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [handleDelayHours=" + handleDelayHours + ", historyRetainDays="
				+ historyRetainDays + "]";
	}

}
