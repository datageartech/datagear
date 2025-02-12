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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.util.FileUtil;

/**
 * CodeMirror打包工具类。
 * <p>
 * 此类将<code>src/main/resources/org/datagear/web/static/lib/codemirror-5.64.0/</code>内
 * <code>addon/*</code>、<code>mode/*</code>中本项目所需的资源合并打包。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CodeMirrorPackager extends AbstractPackager
{
	private String baseDir = "src/main/resources/org/datagear/web/static/lib/codemirror-5.64.0";

	/** 要合并的css文件 */
	private List<String> cssPaths = Collections.emptyList();

	/** 要合并的js文件 */
	private List<String> jsPaths = Collections.emptyList();

	public CodeMirrorPackager()
	{
		super();
		this.init();
	}

	protected void init()
	{
		this.cssPaths = new ArrayList<>();
		this.jsPaths = new ArrayList<>();
		initCssPaths(cssPaths);
		initJsPaths(jsPaths);
	}

	protected void initCssPaths(List<String> cssPaths)
	{
		cssPaths.add("addon/hint/show-hint.css");
		cssPaths.add("addon/fold/foldgutter.css");
	}

	protected void initJsPaths(List<String> jsPaths)
	{
		jsPaths.add("addon/hint/show-hint.js");
		jsPaths.add("addon/search/searchcursor.js");
		jsPaths.add("addon/fold/xml-fold.js");
		jsPaths.add("addon/fold/foldcode.js");
		jsPaths.add("addon/fold/foldgutter.js");
		jsPaths.add("addon/edit/matchbrackets.js");
		jsPaths.add("addon/edit/matchtags.js");
		jsPaths.add("addon/edit/closetag.js");
		jsPaths.add("addon/edit/closebrackets.js");
		jsPaths.add("mode/xml/xml.js");
		jsPaths.add("mode/css/css.js");
		jsPaths.add("mode/htmlmixed/htmlmixed.js");
		jsPaths.add("mode/javascript/javascript.js");
		jsPaths.add("mode/sql/sql.js");
	}

	public String getBaseDir()
	{
		return baseDir;
	}

	public void setBaseDir(String baseDir)
	{
		this.baseDir = baseDir;
	}

	public List<String> getCssPaths()
	{
		return cssPaths;
	}

	public void setCssPaths(List<String> cssPaths)
	{
		this.cssPaths = cssPaths;
	}

	public List<String> getJsPaths()
	{
		return jsPaths;
	}

	public void setJsPaths(List<String> jsPaths)
	{
		this.jsPaths = jsPaths;
	}

	/**
	 * 执行打包。
	 * 
	 * @param target
	 *            打包目标存储目录
	 */
	public void pkg(File targetDir) throws IOException
	{
		pkgCss(targetDir);
		pkgJs(targetDir);
	}

	protected void pkgCss(File targetDir) throws IOException
	{
		List<File> files = toFiles(baseDir, this.cssPaths);
		mergeFile(files, targetDir, -1, (size, index) ->
		{
			return "codemirror-bundle.css";
		});
	}

	protected void pkgJs(File targetDir) throws IOException
	{
		List<File> files = toFiles(baseDir, this.jsPaths);
		mergeFile(files, targetDir, -1, (size, index) ->
		{
			return "codemirror-bundle.js";
		});
	}

	public static void main(String[] args) throws Exception
	{
		println("*****************************************");
		println("CodeMirror 打包工具");
		println("*****************************************");
		println();

		File targetDir = FileUtil.getDirectory("target/codemirrorpkg");

		CodeMirrorPackager pk = new CodeMirrorPackager();
		pk.pkg(targetDir);
	}
}
