/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;
import java.util.Map;

/**
 * 数据集。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSet extends Identifiable
{
	/**
	 * 获取名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取属性列表。
	 * <p>
	 * 属性列表描述{@linkplain #getResult(Map)}返回的{@linkplain DataSetResult#getData()}的对象结构。
	 * </p>
	 * <p>
	 * 属性列表并不一定与{@linkplain DataSetResult#getData()}的对象结构严格一致，
	 * 通常是{@linkplain DataSetResult#getData()}对象包含相同、或者更多的属性。
	 * </p>
	 * 
	 * @return
	 */
	List<DataSetProperty> getProperties();

	/**
	 * 获取指定名称的属性，没有则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSetProperty getProperty(String name);

	/**
	 * 获取参数列表。
	 * <p>
	 * 返回{@code null}或空列表，表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<DataSetParam> getParams();

	/**
	 * 获取指定名称的参数，没有则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSetParam getParam(String name);

	/**
	 * 给定的参数映射表是否可用于{@linkplain #getResult(Map)}。
	 * 
	 * @param paramValues
	 * @return
	 */
	boolean isReady(Map<String, ?> paramValues);

	/**
	 * 获取{@linkplain DataSetResult}。
	 * <p>
	 * 返回结果中的数据项应已转换为与{@linkplain #getProperties()}的{@linkplain DataSetProperty#getType()}类型一致。
	 * </p>
	 * 
	 * @param paramValues
	 *            由{@linkplain #getParams()}所描述的参数值映射表，其关键字是{@linkplain DataSetParam#getName()}
	 * @return
	 * @throws DataSetException
	 */
	DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException;
}
