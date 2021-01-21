/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 操作消息。
 * 
 * @author datagear@163.com
 *
 */
public class OperationMessage
{
	/** 消息类型 */
	private MessageType type = MessageType.SUCCESS;

	/** 消息码 */
	private String code;

	/** 消息内容 */
	private String message;

	/** 消息详细内容 */
	private String detail;

	/** 是否是Throwable详细内容 */
	private boolean throwableDetail;

	/** 操作返回数据 */
	private Object data;

	public OperationMessage()
	{
		super();
	}

	public OperationMessage(MessageType type, String code, String message)
	{
		super();
		this.type = type;
		this.code = code;
		this.message = message;
	}

	/**
	 * 操作是否成功。
	 * 
	 * @return
	 */
	public boolean isSuccess()
	{
		return MessageType.SUCCESS.equals(this.type);
	}

	/**
	 * 操作是否失败。
	 * 
	 * @return
	 */
	public boolean isFail()
	{
		return MessageType.FAIL.equals(this.type);
	}

	public MessageType getType()
	{
		return type;
	}

	public void setType(MessageType type)
	{
		this.type = type;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean hasDetail()
	{
		return this.detail != null && !this.detail.isEmpty();
	}

	public String getDetail()
	{
		return detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	public boolean isThrowableDetail()
	{
		return throwableDetail;
	}

	public void setThrowableDetail(boolean throwableDetail)
	{
		this.throwableDetail = throwableDetail;
	}

	public boolean hasData()
	{
		return (this.data != null);
	}

	public Object getData()
	{
		return data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	public void setThrowable(Throwable throwable)
	{
		this.detail = printThrowableTrace(throwable);
		this.throwableDetail = true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + ", code=" + code + ", message=" + message + "]";
	}

	/**
	 * 将{@linkplain Throwable}转换为字符串。
	 * 
	 * @param t
	 * @return
	 */
	public static String printThrowableTrace(Throwable t)
	{
		if (t == null)
			return "";

		StringWriter out = new StringWriter();
		PrintWriter pout = new PrintWriter(out);

		try
		{
			t.printStackTrace(pout);
		}
		finally
		{
			if (pout != null)
				pout.close();
		}

		return out.toString();
	}

	/**
	 * 操作消息类型。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum MessageType
	{
		/** 成功 */
		SUCCESS,

		/** 失败 */
		FAIL
	}

	/**
	 * 构建操作成功消息。
	 * 
	 * @param code
	 * @param message
	 * @return
	 */
	public static OperationMessage valueOfSuccess(String code, String message)
	{
		OperationMessage om = new OperationMessage(MessageType.SUCCESS, code, message);

		return om;
	}

	/**
	 * 构建操作成功消息。
	 * 
	 * @param code
	 * @param message
	 * @param data
	 * @return
	 */
	public static OperationMessage valueOfSuccess(String code, String message, Object data)
	{
		OperationMessage om = new OperationMessage(MessageType.SUCCESS, code, message);
		om.setData(data);

		return om;
	}

	/**
	 * 构建操作失败消息。
	 * 
	 * @param code
	 * @param message
	 * @return
	 */
	public static OperationMessage valueOfFail(String code, String message)
	{
		OperationMessage om = new OperationMessage(MessageType.FAIL, code, message);

		return om;
	}

	/**
	 * 构建操作失败消息。
	 * 
	 * @param code
	 * @param message
	 * @param data
	 * @return
	 */
	public static OperationMessage valueOfFail(String code, String message, Object data)
	{
		OperationMessage om = new OperationMessage(MessageType.FAIL, code, message);
		om.setData(data);

		return om;
	}

	/**
	 * 构建Throwable操作失败消息。
	 * 
	 * @param code
	 * @param message
	 * @param throwable
	 * @return
	 */
	public static OperationMessage valueOfThrowableFail(String code, String message, Throwable throwable)
	{
		OperationMessage om = new OperationMessage(MessageType.FAIL, code, message);

		om.setThrowable(throwable);

		return om;
	}

	/**
	 * 构建Throwable操作失败消息。
	 * 
	 * @param code
	 * @param message
	 * @param data
	 * @param throwable
	 * @return
	 */
	public static OperationMessage valueOfThrowableFail(String code, String message, Object data, Throwable throwable)
	{
		OperationMessage om = new OperationMessage(MessageType.FAIL, code, message);
		om.setThrowable(throwable);
		om.setData(data);

		return om;
	}
}
