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

package org.datagear.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件工具类。
 * 
 * @author datagear@163.com
 *
 */
public class FileUtil
{
	public static final String PATH_SEPARATOR = File.separator;

	/**
	 * 斜杠（/）路径分隔符
	 */
	public static final String PATH_SEPARATOR_SLASH = "/";

	/**
	 * 反斜杠（\）路径分隔符
	 */
	public static final String PATH_SEPARATOR_BACK_SLASH = "\\";

	private FileUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 获取子孙文件相对于祖先文件的路径。
	 * <p>
	 * 相对路径不会以{@linkplain #PATH_SEPARATOR}开头。
	 * </p>
	 * 
	 * @param ancestor
	 * @param descendent
	 * @return
	 */
	public static String getRelativePath(File ancestor, File descendent)
	{
		String ap = ancestor.getAbsolutePath();
		String dp = descendent.getAbsolutePath();

		int index = dp.indexOf(ap);

		if (index != 0)
			throw new IllegalArgumentException(
					"File [" + descendent + "] is not descendent of File [" + ancestor + "]");

		String rp = dp.substring(index + ap.length());

		if (rp.startsWith(PATH_SEPARATOR))
			rp = rp.substring(PATH_SEPARATOR.length());

		return rp;
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param file
	 * @return
	 */
	public static File getFile(String file)
	{
		return getFileNullable(null, file, false);
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param file
	 * @param createDirectory
	 *            是否创建自身目录和上级目录，如果{@code file}以{@code '/'}或者{@code '\'}结尾将被认为是创建目录
	 * @return
	 */
	public static File getFile(String file, boolean createDirectory)
	{
		return getFileNullable(null, file, createDirectory);
	}

	/**
	 * 获取指定目录下的文件对象。
	 * 
	 * @param parent
	 * @param file
	 * @return
	 */
	public static File getFile(File parent, String file)
	{
		return getFile(parent, file, false);
	}

	/**
	 * 获取指定目录下的文件对象。
	 * 
	 * @param parent
	 * @param file 文件
	 * @param createDirectory
	 *            是否创建自身目录和上级目录，如果{@code file}以{@code '/'}或者{@code '\'}结尾将被认为是创建目录
	 * @return
	 */
	public static File getFile(File parent, String file, boolean createDirectory)
	{
		if (parent == null)
			throw new IllegalArgumentException("[parent] must not be null");

		return getFileNullable(parent, file, createDirectory);
	}

	/**
	 * 获取目录对象。
	 * <p>
	 * 如果目录不存在，此方法会自动创建。
	 * </p>
	 * 
	 * @param file
	 * @return
	 */
	public static File getDirectory(String file)
	{
		return getDirectory(file, true);
	}

	/**
	 * 获取目录对象。
	 * 
	 * @param file
	 * @param create
	 *            是否自动创建
	 * @return
	 */
	public static File getDirectory(String file, boolean create)
	{
		return getDirectoryNullable(null, file, create);
	}

	/**
	 * 获取指定目录下的子目录。
	 * <p>
	 * 如果目录不存在，此方法会自动创建。
	 * </p>
	 * 
	 * @param parent
	 * @param file
	 * @return
	 */
	public static File getDirectory(File parent, String file)
	{
		return getDirectory(parent, file, true);
	}

	/**
	 * 获取指定目录下的子目录。
	 * 
	 * @param parent
	 * @param file
	 * @param create
	 *            是否自动创建
	 * @return
	 */
	public static File getDirectory(File parent, String file, boolean create)
	{
		if (parent == null)
			throw new IllegalArgumentException("[parent] must not be null");

		return getDirectoryNullable(parent, file, create);
	}

	/**
	 * 获取指定目录下的子目录。
	 * 
	 * @param parent
	 *            允许为{@code null}
	 * @param file
	 * @param create
	 *            是否自动创建
	 * @return
	 */
	protected static File getDirectoryNullable(File parent, String file, boolean create)
	{
		file = trimPath(file);

		if (!file.endsWith(PATH_SEPARATOR))
			file += PATH_SEPARATOR;

		return getFileNullable(parent, file, create);
	}

	/**
	 * 获取文件对象。
	 * 
	 * @param parent
	 *            允许为{@code null}
	 * @param file
	 *            文件
	 * @param createDirectory
	 *            是否创建自身目录和上级目录，如果{@code file}以{@code '/'}或者{@code '\'}结尾将被认为是创建目录
	 * @return
	 */
	protected static File getFileNullable(File parent, String file, boolean createDirectory)
	{
		if (StringUtil.isEmpty(file))
			throw new IllegalArgumentException("[file] must not be empty");
		
		file = trimPath(file);

		// 只有限定了父级目录，才需要校验
		if (parent != null)
			checkBackwardPathNoTrim(file);
		
		File reFile = (parent == null ? new File(file) : new File(parent, file));
		
		if (createDirectory)
		{
			createParentIfNone(reFile);
			
			if(!reFile.exists() && (file.endsWith(PATH_SEPARATOR) || file.endsWith(PATH_SEPARATOR_BACK_SLASH)
					|| file.endsWith(PATH_SEPARATOR_SLASH)))
			{
				reFile.mkdir();
			}
		}

		return reFile;
	}
	
	/**
	 * 如果没有，则创建指定文件的父目录。
	 * 
	 * @param file
	 */
	public static void createParentIfNone(File file)
	{
		if(file.exists())
			return;
		
		File parent = file.getParentFile();
		
		if (parent != null && !parent.exists())
			parent.mkdirs();
	}
	
	/**
	 * 删除文件。
	 * 
	 * @param file
	 * @return
	 */
	public static boolean deleteFile(File file)
	{
		if (!file.exists())
			return true;

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			for (File child : children)
				deleteFile(child);
		}

		return file.delete();
	}

	/**
	 * 清空目录，保留目录本身。
	 * 
	 * @param directory
	 * @return
	 */
	public static boolean clearDirectory(File directory)
	{
		if (!directory.exists())
			return true;

		boolean clear = true;

		if (directory.isDirectory())
		{
			File[] children = directory.listFiles();

			for (File child : children)
			{
				boolean deleted = deleteFile(child);

				if (!deleted && clear)
					clear = false;
			}
		}

		return clear;
	}

	/**
	 * 删除所有空子目录。
	 * 
	 * @param file
	 * @param deleteSelf
	 *            如果{@code file}为空，是否删除
	 */
	public static void deleteEmptySubDirectory(File file, boolean deleteSelf)
	{
		if (!file.exists() || !file.isDirectory())
			return;

		File[] children = file.listFiles();

		if (children != null)
		{
			for (File child : children)
			{
				if (child.isDirectory())
					deleteEmptySubDirectory(child, true);
			}
		}

		children = file.listFiles();

		if (children == null || children.length == 0)
		{
			if (deleteSelf)
				file.delete();
		}
	}

	/**
	 * 在指定目录下生成一个文件。
	 * 
	 * @param parent
	 * @return
	 */
	public static File generateUniqueFile(File parent)
	{
		return new File(parent, IDUtil.uuid());
	}

	/**
	 * 在指定目录下生成一个文件。
	 * 
	 * @param parent
	 * @param extension
	 *            为{@code null}时不生成后缀
	 * @return
	 */
	public static File generateUniqueFile(File parent, String extension)
	{
		checkBackwardPath(extension);

		String name = (StringUtil.isEmpty(extension) ? IDUtil.uuid() : IDUtil.uuid() + "." + extension);
		return new File(parent, name);
	}

	/**
	 * 在指定目录下生成一个子文件夹。
	 * 
	 * @param parent
	 * @return
	 */
	public static File generateUniqueDirectory(File parent)
	{
		File file = new File(parent, IDUtil.uuid());

		if (!file.exists())
			file.mkdirs();

		return file;
	}

	/**
	 * 列出指定目录下的文件名称。
	 * 
	 * @param directory
	 * @return
	 */
	public static String[] listFileNames(File directory)
	{
		if (!directory.exists())
			return new String[0];

		return directory.list();
	}

	/**
	 * 获取指定文件的{@linkplain FileInfo}。
	 * 
	 * @param file
	 * @return
	 */
	public static FileInfo getFileInfo(File file)
	{
		FileInfo fileInfo = new FileInfo(file.getName(), file.isDirectory(), file.length());
		return fileInfo;
	}

	/**
	 * 获取指定目录下文件的{@linkplain FileInfo}。
	 * 
	 * @param directory
	 * @return
	 */
	public static FileInfo[] getFileInfos(File directory)
	{
		if (!directory.exists() || !directory.isDirectory())
			return new FileInfo[0];

		File[] files = directory.listFiles();

		FileInfo[] fileInfos = new FileInfo[files.length];

		for (int i = 0; i < files.length; i++)
			fileInfos[i] = getFileInfo(files[i]);
		
		Arrays.sort(fileInfos);

		return fileInfos;
	}

	/**
	 * 获取{@linkplain File}的{@linkplain URL}。
	 * 
	 * @param file
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static URL toURL(File file) throws IllegalArgumentException
	{
		try
		{
			return file.toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Illegal [" + file.toString() + "] to URL");
		}
	}

	/**
	 * 获取文件名后缀。
	 * <p>
	 * 如果文件名没有后缀，将返回{@code null}
	 * </p>
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(File file)
	{
		if (file.isDirectory())
			throw new IllegalArgumentException("[file] must not be directory");

		String name = file.getName();

		return getExtension(name);
	}

	/**
	 * 获取文件名后缀。
	 * <p>
	 * 如果文件名没有后缀，将返回{@code null}
	 * </p>
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName)
	{
		if (fileName == null)
			return null;

		int dotIdx = fileName.lastIndexOf('.');

		if (dotIdx > 0 && dotIdx < fileName.length() - 1)
			return fileName.substring(dotIdx + 1);
		else
			return null;
	}

	/**
	 * 是否为指定后缀的文件名。
	 * 
	 * @param fileName
	 * @param extension
	 *            扩展名，可以带“.”前缀，也可不带
	 * @return
	 */
	public static boolean isExtension(String fileName, String extension)
	{
		if (StringUtil.isEmpty(fileName))
			return false;

		if (!extension.startsWith("."))
			extension = "." + extension;

		return fileName.toLowerCase().endsWith(extension.toLowerCase());
	}

	/**
	 * 是否为指定后缀的文件名。
	 * 
	 * @param file
	 * @param extension
	 *            扩展名，可以带“.”前缀，也可不带
	 * @return
	 */
	public static boolean isExtension(File file, String extension)
	{
		return isExtension(file.getName(), extension);
	}

	/**
	 * 删除文件名后缀。
	 * 
	 * @param fileName
	 * @return
	 */
	public static String deleteExtension(String fileName)
	{
		if (StringUtil.isEmpty(fileName))
			return fileName;

		int idx = fileName.lastIndexOf('.');

		if (idx < 0)
			return fileName;

		return fileName.substring(0, idx);
	}

	/**
	 * 连接路径。
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public static String concatPath(String parent, String child)
	{
		return concatPath(parent, child, PATH_SEPARATOR);
	}

	/**
	 * 连接路径。
	 * 
	 * @param parent
	 * @param child
	 * @param separator
	 * @return
	 */
	public static String concatPath(String parent, String child, String separator)
	{
		return concatPath(parent, child, separator, true);
	}

	/**
	 * 连接路径。
	 * 
	 * @param parent
	 * @param child
	 * @param separator
	 * @param checkBackwardPath
	 * @return
	 */
	public static String concatPath(String parent, String child, String separator, boolean checkBackwardPath)
	{
		if (checkBackwardPath)
		{
			checkBackwardPath(parent);
			checkBackwardPath(child);
		}

		parent = trimPath(parent, separator);
		child = trimPath(child, separator);

		boolean parentEndsWith = parent.endsWith(separator);
		boolean childStartsWith = child.startsWith(separator);

		if (parentEndsWith && childStartsWith)
			return parent + child.substring(separator.length());
		else if (parentEndsWith || childStartsWith)
			return parent + child;
		else
			return parent + separator + child;
	}

	/**
	 * 整理路径。
	 * <p>
	 * 此方法将路径中的{@code "/"}、{@code "\"}统一替换为指定的{@linkplain #PATH_SEPARATOR}。
	 * </p>
	 * 
	 * @param path
	 * @param separator
	 * @return
	 */
	public static String trimPath(String path)
	{
		return trimPath(path, PATH_SEPARATOR);
	}

	/**
	 * 整理路径。
	 * <p>
	 * 此方法将路径中的{@code "/"}、{@code "\"}统一替换为指定的{@code separator}。
	 * </p>
	 * 
	 * @param path
	 * @param separator
	 * @return
	 */
	public static String trimPath(String path, String separator)
	{
		if (path == null)
			return null;

		if (separator.equals("\\"))
			return path.replace("/", separator);
		else
			return path.replace("\\", separator);
	}

	/**
	 * 删除开头的路径分隔符。
	 * 
	 * @param path
	 * @return
	 */
	public static String deletePathSeparatorHead(String path)
	{
		return deletePathSeparatorHead(path, PATH_SEPARATOR);
	}

	/**
	 * 删除开头的路径分隔符。
	 * 
	 * @param path
	 * @param separator
	 * @return
	 */
	public static String deletePathSeparatorHead(String path, String separator)
	{
		if (path == null)
			return null;

		if (path.indexOf(separator) == 0)
			path = path.substring(separator.length());

		return path;
	}

	/**
	 * 是否包含上行路径（{@code ../}、{@code ..\}）。
	 * 
	 * @param path
	 * @return
	 */
	public static boolean containsBackwardPath(String path)
	{
		if (path == null)
			return false;

		return containsBackwardPathNoTrim(trimPath(path));
	}

	protected static boolean containsBackwardPathNoTrim(String path)
	{
		if (path == null)
			return false;

		return (path.indexOf(".." + PATH_SEPARATOR) > -1 || path.indexOf(PATH_SEPARATOR + "..") > -1);
	}

	/**
	 * 确保{@code path}中不包含上行路径。
	 * 
	 * @param path
	 * @throws IllegalArgumentException
	 */
	public static void checkBackwardPath(String path) throws IllegalArgumentException
	{
		if (path == null)
			return;

		checkBackwardPathNoTrim(trimPath(path));
	}

	protected static void checkBackwardPathNoTrim(String path) throws IllegalArgumentException
	{
		if (containsBackwardPathNoTrim(path))
			throw new IllegalArgumentException("[../] and [..\\] is not allowed in path [" + path + "]");
	}

	/**
	 * 创建临时文件夹。
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File createTempDirectory() throws IOException
	{
		String prefix = Global.PRODUCT_NAME_EN_UC + "_TMP_DIR";
		Path path = Files.createTempDirectory(prefix);
		return path.toFile();
	}

	/**
	 * 创建临时文件。
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File createTempFile() throws IOException
	{
		String prefix = Global.PRODUCT_NAME_EN_UC + "_TMP_FILE";
		Path path = Files.createTempFile(prefix, null);
		return path.toFile();
	}

	/**
	 * 创建临时文件。
	 * 
	 * @param extension
	 * @return
	 * @throws IOException
	 */
	public static File createTempFile(String extension) throws IOException
	{
		String prefix = Global.PRODUCT_NAME_EN_UC + "_TMP_FILE";
		Path path = Files.createTempFile(prefix, extension);
		return path.toFile();
	}
	
	/**
	 * 是否是目录名（以{@code '/'}或{@code '\'结尾}）。
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isDirectoryName(String name)
	{
		if(name == null)
			return false;
		
		return (name.endsWith(PATH_SEPARATOR_SLASH) || name.endsWith(PATH_SEPARATOR_BACK_SLASH));
	}
	
	/**
	 * 判断{@code sub}是否是{@code parent}或其子目录。
	 * 
	 * @param parent
	 * @param sub
	 * @return
	 */
	public static boolean contains(File parent, File sub)
	{
		if(parent == null || sub == null)
			return false;
		
		File myParent = sub;
		
		while(myParent != null)
		{
			if(myParent.equals(parent))
				return true;
			
			myParent = myParent.getParentFile();
		}
		
		return false;
	}
	
	/**
	 * 重命名文件路径。
	 * 
	 * @param path 原文件路径
	 * @param newPath 新文件路径，可以是原文件路径的上级路径、下级路径
	 * @return 重命名清单，结构为：<code>新文件 -&gt; 原文件</code>
	 * @throws IOException
	 */
	public static Map<File, File> renameTracked(File path, File newPath) throws IOException
	{
		Map<File, File> tracks = new HashMap<File, File>();
		renameTracked(tracks, path, newPath);
		
		return tracks;
	}
	
	protected static void renameTracked(Map<File, File> handledFiles, File path, File newPath) throws IOException
	{
		if(!path.exists() ||  path.equals(newPath))
			return;
		
		if(path.isDirectory())
		{
			//在创建newPath前获取子文件，这样当newPath是需新建的path子目录时，可以将其忽略
			File[] children = path.listFiles();
			
			if(!newPath.exists())
				newPath.mkdirs();
			
			if(!newPath.isDirectory())
				throw new IllegalArgumentException("Target file must be directory");
			
			handledFiles.put(newPath, path);
			
			for(File child : children)
			{
				if(!child.exists() || handledFiles.containsKey(child))
					continue;
				
				File childNewFile = null;
				
				if(child.isDirectory())
				{
					childNewFile = getDirectory(newPath, child.getName(), true);
				}
				else
				{
					childNewFile = getFile(newPath, child.getName());
				}
				
				renameTracked(handledFiles, child, childNewFile);
			}
			
			if(path.listFiles().length == 0)
				deleteFile(path);
		}
		else if(newPath.exists() && newPath.isDirectory())
		{
			newPath = getFile(newPath, path.getName());
			renameTracked(handledFiles, path, newPath);
		}
		else
		{
			createParentIfNone(newPath);
			
			handledFiles.put(newPath, path);
			
			newPath = IOUtil.copy(path, newPath);
			deleteFile(path);
		}
	}

	/**
	 * 获取文件自身的上次修改时间。
	 * 
	 * @param file
	 * @return
	 */
	public static long lastModified(File file)
	{
		return file.lastModified();
	}

	/**
	 * 获取上次修改时间。
	 * <p>
	 * 如果是目录，则取其中最新文件的上次修改时间。
	 * </p>
	 * 
	 * @param file
	 * @return
	 */
	public static long lastModifiedOfPath(File file)
	{
		long lastModified = file.lastModified();

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			if (children != null)
			{
				for (File child : children)
				{
					long childLastModified = lastModified(child);

					if (childLastModified > lastModified)
						lastModified = childLastModified;
				}
			}
		}

		return lastModified;
	}
}
