/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.web.util;

import java.io.PrintStream;

import org.datagear.util.Global;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

/**
 * 应用启动标语。
 * 
 * @author datagear@163.com
 *
 */
public class ApplicationBanner implements Banner
{
	public ApplicationBanner()
	{
		super();
	}

	@Override
	public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out)
	{
		printLogo(environment, sourceClass, out);
		out.println("");
		printInfo(environment, sourceClass, out);
		out.println("");
	}
	
	protected void printLogo(Environment environment, Class<?> sourceClass, PrintStream out)
	{
		out.println("  ____        _         ____                 ");
		out.println(" |  _ \\  __ _| |_ __ _ / ___| ___  __ _ _ __ ");
		out.println(" | | | |/ _` | __/ _` | |  _ / _ \\/ _` | '__|");
		out.println(" | |_| | (_| | |_ (_| | |_| |  __/ (_| | |   ");
		out.println(" |____/ \\__,_|\\__\\__,_|\\____|\\___|\\__,_|_|   ");
	}
	
	protected void printInfo(Environment environment, Class<?> sourceClass, PrintStream out)
	{
		out.println("  " + Global.PRODUCT_NAME_EN + "-v" + Global.VERSION + "  " + Global.WEB_SITE);
	}
}
