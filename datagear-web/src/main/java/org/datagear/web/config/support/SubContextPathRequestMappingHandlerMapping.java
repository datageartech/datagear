/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.config.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.datagear.util.StringUtil;
import org.datagear.web.util.WebContextPath;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 能够为所有控制器处理路径添加{@linkplain #subContextPath}前缀的{@linkplain RequestMappingHandlerMapping}。
 * 
 * @author datagear@163.com
 *
 */
public class SubContextPathRequestMappingHandlerMapping extends RequestMappingHandlerMapping
{
	private String subContextPath = "";

	public SubContextPathRequestMappingHandlerMapping()
	{
		super();
	}

	public String getSubContextPath()
	{
		return subContextPath;
	}

	public void setSubContextPath(String subContextPath)
	{
		this.subContextPath = WebContextPath.trimSubContextPath(subContextPath);
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType)
	{
		RequestMappingInfo rmi = super.getMappingForMethod(method, handlerType);

		if (rmi == null)
			return null;

		if (StringUtil.isEmpty(this.subContextPath))
			return rmi;

		PatternsRequestCondition prc = rmi.getPatternsCondition();
		Set<String> patterns = prc.getPatterns();

		List<String> myPatterns = new ArrayList<String>(patterns.size());
		for (String pattern : patterns)
		{
			if (pattern.isEmpty())
				;
			else if (!pattern.startsWith("/"))
				pattern = "/" + pattern;

			pattern = this.subContextPath + pattern;

			myPatterns.add(pattern);
		}

		PatternsRequestCondition myPrc = new PatternsRequestCondition(myPatterns.toArray(new String[myPatterns.size()]),
				getUrlPathHelper(), getPathMatcher(),
				useSuffixPatternMatch(), useTrailingSlashMatch(), getFileExtensions());

		RequestMappingInfo myRmi = new RequestMappingInfo(myPrc, rmi.getMethodsCondition(),
				rmi.getParamsCondition(), rmi.getHeadersCondition(),
				rmi.getConsumesCondition(), rmi.getProducesCondition(), rmi.getCustomCondition());

		return myRmi;
	}
}
