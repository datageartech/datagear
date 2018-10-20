/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.io.Serializable;

/**
 * 键策略。
 * 
 * @author datagear@163.com
 *
 */
public class KeyRule implements Serializable
{
	private static final long serialVersionUID = 1L;

	private RuleType ruleType = RuleType.NO_ACTION;

	/** 是否由程序维护级联规则，而不是数据库本身 */
	private boolean manually = false;

	public KeyRule()
	{
		super();
	}

	public KeyRule(RuleType ruleType)
	{
		this(ruleType, false);
	}

	public KeyRule(RuleType ruleType, boolean manually)
	{
		super();
		this.ruleType = ruleType;
		this.manually = manually;
	}

	public RuleType getRuleType()
	{
		return ruleType;
	}

	public void setRuleType(RuleType ruleType)
	{
		this.ruleType = ruleType;
	}

	public boolean isManually()
	{
		return manually;
	}

	public void setManually(boolean manually)
	{
		this.manually = manually;
	}

	/**
	 * 构建{@linkplain KeyRule}。
	 * 
	 * @param ruleType
	 * @param manually
	 * @return
	 */
	public static KeyRule valueOf(RuleType ruleType, boolean manually)
	{
		return new KeyRule(ruleType, manually);
	}

	/**
	 * 规则类型，参考{@linkplain DatabaseMetaData}的{@code importedKey*}常量。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum RuleType
	{
		NO_ACTION,

		CASCADE,

		SET_NULL,

		SET_DEFAUL,

		RESTRICT
	}
}
