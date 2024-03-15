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

package org.datagear.connection;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JDBC驱动程序工具类。
 * 
 * @author datagear@163.com
 *
 */
public class DriverTool
{
	public DriverTool()
	{
		super();
	}

	/**
	 * 获取指定名称的已注册{@linkplain Driver}。
	 * <p>
	 * 如果没有，此方法将返回{@code null}。
	 * </p>
	 * 
	 * @param driverClassName
	 * @return
	 */
	public Driver getDriver(String driverClassName)
	{
		Set<Driver> drivers = getDrivers();

		for (Driver driver : drivers)
		{
			if (isAssignableClass(driver.getClass(), driverClassName))
				return driver;
		}

		return null;
	}

	/**
	 * 获取已注册的{@linkplain Driver}。
	 * <p>
	 * 如果没有，此方法将返回空{@linkplain Set}。
	 * </p>
	 * 
	 * @return
	 */
	public Set<Driver> getDrivers()
	{
		Set<Driver> myDrivers = new HashSet<Driver>();

		ClassLoader myClassLoader = getClass().getClassLoader();

		Enumeration<Driver> drivers = DriverManager.getDrivers();

		while (drivers.hasMoreElements())
		{
			Driver driver = drivers.nextElement();

			ClassLoader classLoader = driver.getClass().getClassLoader();

			if (myClassLoader == classLoader)
				myDrivers.add(driver);
		}

		return myDrivers;
	}

	/**
	 * 撤销已注册的{@linkplain Driver}。
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<String> deregisterDrivers() throws SQLException
	{
		// 此方法内容完全拷贝自org.apache.catalina.loader.JdbcLeakPrevention.clearJdbcDriverRegistrations()方法的内容

		List<String> driverNames = new ArrayList<String>();

		/*
		 * DriverManager.getDrivers() has a nasty side-effect of registering
		 * drivers that are visible to this class loader but haven't yet been
		 * loaded. Therefore, the first call to this method a) gets the list of
		 * originally loaded drivers and b) triggers the unwanted side-effect.
		 * The second call gets the complete list of drivers ensuring that both
		 * original drivers and any loaded as a result of the side-effects are
		 * all de-registered.
		 */
		HashSet<Driver> originalDrivers = new HashSet<Driver>();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			originalDrivers.add(drivers.nextElement());
		}
		drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			Driver driver = drivers.nextElement();
			// Only unload the drivers this web app loaded
			if (driver.getClass().getClassLoader() != this.getClass().getClassLoader())
			{
				continue;
			}
			// Only report drivers that were originally registered. Skip any
			// that were registered as a side-effect of this code.
			if (originalDrivers.contains(driver))
			{
				driverNames.add(driver.getClass().getCanonicalName());
			}
			DriverManager.deregisterDriver(driver);
		}
		return driverNames;
	}

	/**
	 * 判断类是否是指定类名的类或者子类。
	 * 
	 * @param subClass
	 * @param superClassName
	 * @return
	 */
	protected boolean isAssignableClass(Class<?> subClass, String superClassName)
	{
		if (subClass == null)
			return false;

		if (subClass.getName().equals(superClassName))
			return true;

		Class<?> superClass = subClass.getSuperclass();

		if (superClass != null && isAssignableClass(superClass, superClassName))
			return true;

		Class<?>[] superInterfaces = subClass.getInterfaces();

		if (superInterfaces != null)
		{
			for (Class<?> superInterface : superInterfaces)
			{
				if (isAssignableClass(superInterface, superClassName))
					return true;
			}
		}

		return false;
	}
}
