/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;

/**
 * 看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DashboardWidget extends Identifiable, Serializable
{
	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	Dashboard render(RenderContext renderContext) throws RenderException;
}
