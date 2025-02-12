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

package org.datagear.web.util.packager;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * PrimeVue打包工具类。
 * <p>
 * 此类将由<code>npm install -g primevue</code>下载的PrimeVue资源打包为本项目所需的传统JS、CSS资源。
 * </p>
 * <p>
 * 打包配置参考：{@linkplain #getPkgComponents(File)}、{@linkplain #getPkgThemes(File)}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PrimeVuePackager extends AbstractPackager
{
	private String srcEncoding = IOUtil.CHARSET_UTF_8;

	private int pkgFileSizeThreshold = 500;

	public PrimeVuePackager()
	{
		super();
	}

	public String getSrcEncoding()
	{
		return srcEncoding;
	}

	public void setSrcEncoding(String srcEncoding)
	{
		this.srcEncoding = srcEncoding;
	}

	public int getPkgFileSizeThreshold()
	{
		return pkgFileSizeThreshold;
	}

	public void setPkgFileSizeThreshold(int pkgFileSizeThreshold)
	{
		this.pkgFileSizeThreshold = pkgFileSizeThreshold;
	}

	/**
	 * 执行打包。
	 * 
	 * @param primeVueDir
	 *            由npm安装的PrimeVue目录
	 * @param target
	 *            打包目标存储目录
	 */
	public void pkg(String primeVueDir, String target) throws IOException
	{
		pkg(new File(primeVueDir), new File(target));
	}

	/**
	 * 执行打包。
	 * 
	 * @param primeVueDir
	 *            由npm安装的PrimeVue目录
	 * @param target
	 *            打包目标存储目录
	 */
	public void pkg(File primeVueDir, File target) throws IOException
	{
		if (!primeVueDir.exists() || !primeVueDir.isDirectory())
			throw new IllegalArgumentException("[src] illegal");

		File pkgInfo = FileUtil.getFile(primeVueDir, "package.json");

		if (!pkgInfo.exists())
			throw new IllegalArgumentException("[src] illegal");

		if (!target.exists())
			FileUtil.mkdirsIfNot(target);

		if (!target.isDirectory())
			throw new IllegalArgumentException("[target] illegal");

		String version = readVersion(pkgInfo);

		if (StringUtil.isBlank(version))
			throw new IllegalArgumentException("[package.json] file illegal");

		target = createTargetPkgFolder(target, version);

		copyReadme(primeVueDir, target);
		copyLicense(primeVueDir, target);
		pkgComponent(primeVueDir, target);
		pkgResources(primeVueDir, target);
		writePkgInfo(target);
	}

	/**
	 * 打包<span>resources/</span>资源。
	 * 
	 * @param primeVueDir
	 * @param target
	 * @throws IOException
	 */
	protected void pkgResources(File primeVueDir, File target) throws IOException
	{
		primeVueDir = FileUtil.getFile(primeVueDir, "resources");
		target = FileUtil.getDirectory(target, primeVueDir.getName(), true);

		if (isPkgPrimevueMinCss())
		{
			File file = FileUtil.getFile(primeVueDir, "primevue.min.css");
			IOUtil.copy(file, FileUtil.getFile(target, file.getName()));
		}

		File srcThemes = FileUtil.getDirectory(primeVueDir, "themes", true);
		File targetThemes = FileUtil.getDirectory(target, srcThemes.getName(), true);

		List<String> themes = getPkgThemes(primeVueDir);
		for (String theme : themes)
		{
			IOUtil.copy(FileUtil.getDirectory(srcThemes, theme, false), FileUtil.getDirectory(targetThemes, theme));
		}
	}

	/**
	 * 打包JS组件。
	 * 
	 * @param primeVueDir
	 * @param target
	 * @throws IOException
	 */
	protected void pkgComponent(File primeVueDir, File target) throws IOException
	{
		List<String> cmps = getPkgComponents(primeVueDir);
		List<String> fullCmps = resolveDependencyNames(primeVueDir, cmps);

		List<File> files = new ArrayList<>();

		println();
		println("------------------------------");
		println("Package components :");
		println("------------------------------");

		for (int i = 0, len = fullCmps.size(); i < len; i++)
		{
			String name = fullCmps.get(i);
			String path = componentNameToFilePath(name, true);
			File file = FileUtil.getFile(primeVueDir, path);

			if (!file.exists())
				throw new IllegalArgumentException("Component file not found : " + path);

			files.add(file);

			println("[" + (i + 1) + "] Package component : " + name + " , file : " + path);
		}

		mergeFile(files, target, getPkgFileSizeThreshold(), (total, index) ->
		{
			return "primevue.min." + index + ".js";
		});
	}

	/**
	 * 是否打包<code>resources/primevue.min.css</code>文件。
	 * 
	 * @return
	 */
	protected boolean isPkgPrimevueMinCss()
	{
		return true;
	}

	/**
	 * 获取打包主题名。
	 * 
	 * @param primeVueDir
	 * @return
	 */
	protected List<String> getPkgThemes(File primeVueDir)
	{
		return Arrays.asList("saga-blue", "vela-blue");
	}

	/**
	 * 获取打包组件名。
	 * <p>
	 * 这里只需要返回最终要使用的组件名即可，依赖组件会自动识别和打包。
	 * </p>
	 * 
	 * @param primeVueDir
	 * @return
	 */
	protected List<String> getPkgComponents(File primeVueDir)
	{
		List<String> re = new ArrayList<>();

		println();
		println("------------------------------");
		println("Components :");
		println("------------------------------");

		Set<String> excludes = getPkgExcludeComponents(primeVueDir);
		File[] files = primeVueDir.listFiles();

		for (File file : files)
		{
			String name = file.getName();
			boolean isComponent = false;

			if (!file.isDirectory())
			{
				isComponent = false;
			}
			else
			{
				String cmpPath = componentNameToFilePath(name, true);
				File cmpFile = FileUtil.getFile(primeVueDir, cmpPath);

				if (cmpFile.exists())
				{
					isComponent = true;
				}
				else
				{
					isComponent = false;
				}
			}

			if (!isComponent)
			{
				println("[WARN] : [" + name + "] is ignored for resolving component");
			}

			if (isComponent && !excludes.contains(name))
			{
				re.add(name);
			}
		}

		for (int i = 0, len = re.size(); i < len; i++)
		{
			String n = re.get(i);
			println("[" + (i + 1) + "] " + n);
		}

		return re;
	}

	/**
	 * 获取打包排除组件。
	 * 
	 * @param primeVueDir
	 * @return
	 */
	protected Set<String> getPkgExcludeComponents(File primeVueDir)
	{
		return Collections.emptySet();
	}

	/**
	 * 解析组件及其所有依赖组件名，并按照依赖顺序排序（被依赖项靠前）。
	 * 
	 * @param primeVueDir
	 * @param names
	 *            组件名，比如：{@code "button"}、{@code "toast.style"}、{@code "icons.check"}
	 * @return
	 */
	protected List<String> resolveDependencyNames(File primeVueDir, List<String> names)
	{
		List<String> re = new ArrayList<>();

		Map<String, List<String>> dependencyMap = new HashMap<>();
		resolveDependencyNames(primeVueDir, names, dependencyMap);
		re.addAll(dependencyMap.keySet());

		println();
		println("------------------------------");
		println("Component dependencies :");
		println("------------------------------");

		int index = 0;
		for (String n : dependencyMap.keySet())
		{
			println("[" + (index + 1) + "] " + n + " : " + dependencyMap.get(n));
			index++;
		}

		int len = re.size();
		for (int i = 0; i < len - 1; i++)
		{
			for (int j = 0; j < len - 1 - i; j++)
			{
				String a = re.get(j);
				String b = re.get(j + 1);
				List<String> ads = dependencyMap.get(a);
				List<String> bds = dependencyMap.get(b);

				if (ads == null)
					ads = Collections.emptyList();
				if (bds == null)
					bds = Collections.emptyList();

				int compare = 0;

				if (ads.isEmpty() && bds.isEmpty())
				{
					compare = 0;
				}
				else if (ads.isEmpty())
				{
					compare = -1;
				}
				else if (bds.isEmpty())
				{
					compare = 1;
				}
				else
				{
					boolean adb = isDependency(dependencyMap, a, b);
					boolean bda = isDependency(dependencyMap, b, a);

					if (!adb && !bda)
					{
						compare = 0;
					}
					else if (adb)
					{
						compare = 1;
					}
					else if (bda)
					{
						compare = -1;
					}
					else
					{
						compare = 0;
					}
				}

				// 只要不是小于0，都应往后移
				if (compare >= 0)
				{
					re.set(j + 1, a);
					re.set(j, b);
				}
			}
		}

		return re;
	}

	/**
	 * 解析组件及其所有依赖组件名，写入{@code dependencyMap}映射表。
	 * 
	 * @param primeVueDir
	 * @param names
	 *            组件名，比如：{@code "button"}、{@code "toast.style"}、{@code "icons.check"}
	 * @param dependencyMap
	 */
	protected void resolveDependencyNames(File primeVueDir, List<String> names, Map<String, List<String>> dependencyMap)
	{
		for (String name : names)
		{
			if (dependencyMap.containsKey(name))
				continue;

			List<String> myDependencyNames = resolveDependencyNames(primeVueDir, name);
			dependencyMap.put(name, myDependencyNames);

			resolveDependencyNames(primeVueDir, myDependencyNames, dependencyMap);
		}
	}

	/**
	 * 判断组件是否依赖另一个组件。
	 * 
	 * @param dependencyMap
	 * @param name
	 *            组件名
	 * @param dependency
	 *            被依赖组件名
	 * @return
	 */
	protected boolean isDependency(Map<String, List<String>> dependencyMap, String name, String dependency)
	{
		List<String> dependencies = dependencyMap.get(name);

		if (dependencies == null || dependencies.isEmpty())
			return false;

		if (dependencies.contains(dependency))
			return true;

		for (String n : dependencies)
		{
			if (isDependency(dependencyMap, n, dependency))
				return true;
		}

		return false;
	}

	/**
	 * 解析指定组件的依赖组件。
	 * 
	 * @param primeVueDir
	 * @param name
	 * @return
	 */
	protected List<String> resolveDependencyNames(File primeVueDir, String name)
	{
		// 这里不使用"*.min.js"文件，因为这些文件里有可能没有定义依赖参数
		String path = componentNameToFilePath(name, false);
		File file = FileUtil.getFile(primeVueDir, path);

		StringBuilder functionCallParamStr = new StringBuilder();

		Reader in = null;
		try
		{
			in = IOUtil.getReader(file, getSrcEncoding());
			int c = -1;
			while ((c = in.read()) != -1)
			{
				// 只需要读取组件JS文件最后的函数调用参数即可
				if (c == '(')
				{
					functionCallParamStr.delete(0, functionCallParamStr.length());
				}

				functionCallParamStr.appendCodePoint(c);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtil.close(in);
		}

		String fcpstr = functionCallParamStr.toString();
		int lb = fcpstr.indexOf('(');
		int rb = (lb < 0 ? -1 : fcpstr.indexOf(')', lb + 1));

		if (lb < 0 || rb < 0)
			throw new IllegalArgumentException("Invalid component file : " + path);

		fcpstr = fcpstr.substring(lb + 1, rb);
		String[] params = StringUtil.split(fcpstr, ",", true);

		List<String> re = new ArrayList<>(params.length);

		for (String p : params)
		{
			// 仅处理PrimeVue依赖
			if (p.startsWith("primevue."))
			{
				p = p.substring("primevue.".length());
				re.add(p);
			}
		}

		return re;
	}

	/**
	 * 组件名转换至组件文件路径。
	 * 
	 * @param name
	 *            组件名，比如：{@code "button"}、{@code "toast.style"}、{@code "icons.check"}
	 * @param min
	 *            是否转换为{@code "*.min.js"}
	 * @return
	 */
	protected String componentNameToFilePath(String name, boolean min)
	{
		String path = null;

		String[] nodes = StringUtil.split(name, ".", true);

		if (nodes.length == 0)
		{
			path = null;
		}
		else if (nodes.length == 1)
		{
			path = nodes[0] + "/" + nodes[0] + (min ? ".min.js" : ".js");
		}
		else if (nodes.length == 2)
		{
			// icons.xxx
			if ("icons".equals(nodes[0]))
			{
				path = "icons/" + nodes[1] + "/index" + (min ? ".min.js" : ".js");
			}
			// xxx.style
			else if ("style".equals(nodes[1]))
			{
				path = nodes[0] + "/style/" + nodes[0] + nodes[1] + (min ? ".min.js" : ".js");
			}
		}

		if (StringUtil.isEmpty(path))
			throw new UnsupportedOperationException("Unsupported component name to file path for : " + name);

		return path;
	}

	protected void copyReadme(File src, File target) throws IOException
	{
		File srcFile = FileUtil.getFile(src, "README.md");
		File tgtFile = FileUtil.getFile(target, srcFile.getName());

		IOUtil.copy(srcFile, tgtFile);
	}

	protected void copyLicense(File src, File target) throws IOException
	{
		File srcFile = FileUtil.getFile(src, "LICENSE.md");
		File tgtFile = FileUtil.getFile(target, srcFile.getName());

		IOUtil.copy(srcFile, tgtFile);
	}

	protected void writePkgInfo(File target) throws IOException
	{
		File file = FileUtil.getFile(target, "readme-pkg.txt");
		Writer out = null;
		try
		{
			out = IOUtil.getWriter(file, IOUtil.CHARSET_UTF_8);
			out.write("Package by : " + getClass().getName());
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	protected File createTargetPkgFolder(File target, String version)
	{
		File folder = FileUtil.getDirectory(target, "primevue@" + version);
		FileUtil.clearDirectory(folder);

		return folder;
	}

	protected String readVersion(File pkgInfo) throws IOException
	{
		Reader in = null;

		try
		{
			in = IOUtil.getReader(pkgInfo, IOUtil.CHARSET_UTF_8);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = JsonSupport.parseNonStardand(in, Map.class);

			return (String) map.get("version");
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	public static void main(String[] args) throws Exception
	{
		println("*****************************************");
		println("PrimeVue 打包工具");
		println("要下载PrimeVue，先安装node（https://nodejs.org/），然后执行命令：npm install -g primevue");
		println("*****************************************");
		println();

		String dftSrc = "D:/node/node_modules/primevue";
		String dftTarget = "target/primevuepkg";
		String src = null;
		String target = null;

		Scanner scanner = new Scanner(System.in);

		println("请输入PrimeVue原始目录：");
		println("要使用默认目录（" + dftSrc + "），请输入：d");

		while (scanner.hasNextLine())
		{
			String input = scanner.nextLine().trim();

			if (input.isEmpty())
				;
			else if ("exit".equalsIgnoreCase(input))
			{
				println("Bye!");
				scanner.close();
				System.exit(0);
			}
			else if (StringUtil.isEmpty(src))
			{
				src = input;
				
				if ("d".equalsIgnoreCase(src))
					src = dftSrc;
				
				println("请输入PrimeVue打包目录：");
				println("要使用默认打包目录（" + dftTarget + "），请输入：d");
			}
			else if(StringUtil.isEmpty(target))
			{
				target = input;
				
				if ("d".equalsIgnoreCase(target))
					target = dftTarget;

				break;
			}
		}

		scanner.close();

		PrimeVuePackager pk = new PrimeVuePackager();
		pk.pkg(src, target);
	}
}
