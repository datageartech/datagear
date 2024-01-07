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

package org.datagear.web.util.packager;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
 * 打包配置参考：{@linkplain #getPkgComponents()}、{@linkplain #isPkgPrimevueMinCss()}、{@linkplain #getPkgThemes()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PrimeVuePackager extends AbstractPackager
{
	private int pkgFileSizeThreshold = 500;

	public PrimeVuePackager()
	{
		super();
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
	 * @param src
	 *            由npm安装的PrimeVue资源目录
	 * @param target
	 *            生成传统JS、CSS资源的存储目录
	 */
	public void pkg(String src, String target) throws IOException
	{
		pkg(new File(src), new File(target));
	}

	/**
	 * 执行打包。
	 * 
	 * @param src
	 *            由npm安装的PrimeVue资源目录
	 * @param target
	 *            生成传统JS、CSS资源的存储目录
	 */
	public void pkg(File src, File target) throws IOException
	{
		if (!src.exists() || !src.isDirectory())
			throw new IllegalArgumentException("[src] illegal");

		File pkgInfo = FileUtil.getFile(src, "package.json");

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

		copyReadme(src, target);
		copyLicense(src, target);
		pkgComponent(src, target);
		pkgResources(src, target);
		writePkgInfo(target);
	}

	/**
	 * 打包<span>resources/</span>资源。
	 * 
	 * @param src
	 * @param target
	 * @throws IOException
	 */
	protected void pkgResources(File src, File target) throws IOException
	{
		src = FileUtil.getFile(src, "resources");
		target = FileUtil.getDirectory(target, src.getName(), true);

		if (isPkgPrimevueMinCss())
		{
			File file = FileUtil.getFile(src, "primevue.min.css");
			IOUtil.copy(file, FileUtil.getFile(target, file.getName()));
		}

		File srcThemes = FileUtil.getDirectory(src, "themes", true);
		File targetThemes = FileUtil.getDirectory(target, srcThemes.getName(), true);

		List<String> themes = getPkgThemes();
		for (String theme : themes)
		{
			IOUtil.copy(FileUtil.getDirectory(srcThemes, theme, false), FileUtil.getDirectory(targetThemes, theme));
		}
	}

	/**
	 * 打包JS组件。
	 * 
	 * @param src
	 * @param target
	 * @throws IOException
	 */
	protected void pkgComponent(File src, File target) throws IOException
	{
		List<String> cmps = getPkgComponents();

		List<File> files = new ArrayList<>();

		for (String name : cmps)
		{
			File cmpFolder = FileUtil.getDirectory(src, name);
			File cmpFile = FileUtil.getFile(cmpFolder, name + ".min.js");
			files.add(cmpFile);
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
	 * @return
	 */
	protected List<String> getPkgThemes()
	{
		return Arrays.asList("saga-blue", "vela-blue");
	}

	protected List<String> getPkgComponents()
	{
		return Arrays.asList(
				//
				"core", "tabmenu", "card", "datatable", "column", //
				"contextmenu", "dialog", "checkbox", "textarea", "toast", //
				"toastservice", "password", "divider", "selectbutton", "confirmdialog", //
				"confirmationservice", "togglebutton", "splitbutton", "tabview", "tabpanel", //
				"menu", "menubar", "chip", "fileupload", "inlinemessage", //
				"steps", "dataview", "overlaypanel", "panel", "fieldset", //
				"listbox", "tooltip", "colorpicker", "tieredmenu", "splitter", //
				"splitterpanel", "radiobutton", "progressbar", "multiselect", "treeselect" //
		//
		);
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

	protected static void print(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.print(str);
	}

	protected static void println(Object o)
	{
		String str = "NULL";

		if (o == null)
			;
		else if (o instanceof String)
			str = (String) o;
		else
			str = o.toString();

		System.out.println(str);
	}

	protected static void println()
	{
		System.out.println();
	}

	public static void main(String[] args) throws Exception
	{
		println("*****************************************");
		println("PrimeVue 打包工具");
		println("*****************************************");
		println();

		String dftTarget = "target/primevuepkg";
		String src = null;
		String target = null;

		Scanner scanner = new Scanner(System.in);

		println("请输入PrimeVue原始目录：");

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
