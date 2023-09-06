/*
 * Copyright 2018-2023 datagear.tech
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

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
	 * <p>
	 * 通常是不可修改的列表。
	 * </p>
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
	 * 获取驱动库上次变更时间。
	 * 
	 * @return {@code -1}表示没有
	 * @throws DriverEntityManagerException
	 */
	long getLastModified(DriverEntity driverEntity) throws DriverEntityManagerException;

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
	 * @return
	 * @throws DriverEntityManagerException
	 */
	boolean[] deleteDriverLibrary(DriverEntity driverEntity, String... libraryName) throws DriverEntityManagerException;

	/**
	 * 删除驱动程序所有库。
	 * 
	 * @param driverEntity
	 * @return
	 * @throws DriverEntityManagerException
	 */
	boolean deleteDriverLibrary(DriverEntity driverEntity) throws DriverEntityManagerException;

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
	 * 释放指定{@linkplain DriverEntity}的资源，但不删除{@linkplain DriverEntity}。
	 * 
	 * @param driverEntity
	 * @throws DriverEntityManagerException
	 */
	void release(DriverEntity driverEntity) throws DriverEntityManagerException;

	/**
	 * 释放所有{@linkplain DriverEntity}的资源，但不删除任何{@linkplain DriverEntity}。
	 */
	void releaseAll();

	/**
	 * 导出至ZIP。
	 * 
	 * @param out
	 * @param ids
	 *            要筛选导出的{@linkplain DriverEntity#getId()}，如果为空，表示全部导出。
	 * @throws DriverEntityManagerException
	 */
	void exportToZip(ZipOutputStream out, String... ids) throws DriverEntityManagerException;

	/**
	 * 由ZIP导入。
	 * 
	 * @param in
	 * @param ids
	 *            要筛选导入的{@linkplain DriverEntity#getId()}，如果为空，表示全部导入。
	 * @throws DriverEntityManagerException
	 */
	void importFromZip(ZipInputStream in, String... ids) throws DriverEntityManagerException;

	/**
	 * 从ZIP输入流中读取{@linkplain DriverEntity}列表。
	 * <p>
	 * 如果输入流中不包含{@linkplain #driverEntityInfoFileName}的文件，返回空列表。
	 * </p>
	 * 
	 * @param in
	 * @return
	 * @throws DriverEntityManagerException
	 */
	List<DriverEntity> readDriverEntitiesFromZip(ZipInputStream in) throws DriverEntityManagerException;
}
