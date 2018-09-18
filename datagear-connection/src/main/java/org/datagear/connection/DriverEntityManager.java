/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.util.List;

/**
 * {@linkplain DriverEntity}管理器。
 * 
 * @author datagear@163.com
 *
 */
public interface DriverEntityManager
{
	/**
	 * 添加{@linkplain DriverEntity}。
	 * 
	 * @param driverEntities
	 * @throws DriverEntityManagerException
	 */
	void add(DriverEntity... driverEntities) throws DriverEntityManagerException;

	/**
	 * 更新{@linkplain DriverEntity}。
	 * 
	 * @param driverEntities
	 * @return
	 * @throws DriverEntityManagerException
	 */
	boolean[] update(DriverEntity... driverEntities) throws DriverEntityManagerException;

	/**
	 * 获取指定路径的{@linkplain DriverEntity}。
	 * 
	 * @param id
	 * @return
	 * @throws DriverEntityManagerException
	 */
	DriverEntity get(String id) throws DriverEntityManagerException;

	/**
	 * 删除指定ID的{@linkplain DriverEntity}。
	 * 
	 * @param ids
	 * @return
	 * @throws DriverEntityManagerException
	 */
	void delete(String... ids) throws DriverEntityManagerException;

	/**
	 * 获取所有{@linkplain DriverEntity}。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	List<DriverEntity> getAll() throws DriverEntityManagerException;

	/**
	 * 获取上次变更时间。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	long getLastModified() throws DriverEntityManagerException;

	/**
	 * 添加驱动程序库。
	 * 
	 * @param driverEntity
	 * @param libraryName
	 * @param in
	 * @throws DriverEntityManagerException
	 */
	void addDriverLibrary(DriverEntity driverEntity, String libraryName, InputStream in)
			throws DriverEntityManagerException;

	/**
	 * 删除驱动程序库。
	 * 
	 * @param driverEntity
	 * @param libraryName
	 * @throws DriverEntityManagerException
	 */
	void deleteDriverLibrary(DriverEntity driverEntity, String... libraryName) throws DriverEntityManagerException;

	/**
	 * 删除驱动程序所有库。
	 * 
	 * @param driverEntity
	 * @throws DriverEntityManagerException
	 */
	void deleteDriverLibrary(DriverEntity driverEntity) throws DriverEntityManagerException;

	/**
	 * 获取驱动程序库输出流。
	 * 
	 * @param driverEntity
	 * @param libraryName
	 * @return
	 * @throws DriverEntityManagerException
	 */
	InputStream getDriverLibrary(DriverEntity driverEntity, String libraryName) throws DriverEntityManagerException;

	/**
	 * 读取驱动程序库。
	 * 
	 * @param driverEntity
	 * @param libraryName
	 * @param out
	 * @throws DriverEntityManagerException
	 */
	void readDriverLibrary(DriverEntity driverEntity, String libraryName, OutputStream out)
			throws DriverEntityManagerException;

	/**
	 * 获取{@linkplain DriverLibraryInfo}列表。
	 * 
	 * @param driverEntity
	 * @return
	 * @throws DriverEntityManagerException
	 */
	List<DriverLibraryInfo> getDriverLibraryInfos(DriverEntity driverEntity) throws DriverEntityManagerException;

	/**
	 * 获取驱动程序。
	 * 
	 * @param driverEntity
	 * @return
	 * @throws DriverEntityManagerException
	 */
	Driver getDriver(DriverEntity driverEntity) throws DriverEntityManagerException;

	/**
	 * 释放指定{@linkplain DriverEntity}的资源。
	 * 
	 * @param driverEntity
	 * @throws DriverEntityManagerException
	 */
	void release(DriverEntity driverEntity) throws DriverEntityManagerException;

	/**
	 * 释放所有资源。
	 */
	void releaseAll();
}
