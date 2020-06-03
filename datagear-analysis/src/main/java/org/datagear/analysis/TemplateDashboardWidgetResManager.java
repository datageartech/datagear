package org.datagear.analysis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

/**
 * {@linkplain TemplateDashboardWidget}资源管理器。
 * <p>
 * 此类通过{@linkplain DashboardWidget#getId()}来管理{@linkplain DashboardWidget}资源。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface TemplateDashboardWidgetResManager
{
	/**
	 * 获取默认资源编码。
	 * 
	 * @return
	 */
	String getDefaultEncoding();

	/**
	 * 获取指定模板的输入流。
	 * 
	 * @param widget
	 * @param template
	 * @return
	 * @throws IOException
	 */
	Reader getTemplateReader(TemplateDashboardWidget widget, String template) throws IOException;

	/**
	 * 获取指定模板的输出流。
	 * 
	 * @param widget
	 * @param template
	 * @return
	 * @throws IOException
	 */
	Writer getTemplateWriter(TemplateDashboardWidget widget, String template) throws IOException;

	/**
	 * 获取指定名称资源的输入流。
	 * 
	 * @param id
	 *            {@linkplain TemplateDashboardWidget#getId()}
	 * @param name
	 *            资源名称
	 * @param encoding
	 *            资源编码，为{@code null}或空则使用默认编码
	 * @return
	 * @throws IOException
	 */
	Reader getResourceReader(String id, String name, String encoding) throws IOException;

	/**
	 * 获取指定名称资源的输出流。
	 * 
	 * @param id
	 *            {@linkplain TemplateDashboardWidget#getId()}
	 * @param name
	 *            资源名称
	 * @param encoding
	 *            资源编码，为{@code null}或空则使用默认编码
	 * @return
	 * @throws IOException
	 */
	Writer getResourceWriter(String id, String name, String encoding) throws IOException;

	/**
	 * 获取指定名称资源的输入流。
	 * 
	 * @param id
	 *            {@linkplain TemplateDashboardWidget#getId()}
	 * @param name
	 *            资源名称
	 * @return
	 * @throws IOException
	 */
	InputStream getResourceInputStream(String id, String name) throws IOException;

	/**
	 * 获取指定名称资源的输出流。
	 * 
	 * @param id
	 *            {@linkplain TemplateDashboardWidget#getId()}
	 * @param name
	 *            资源名称
	 * @return
	 * @throws IOException
	 */
	OutputStream getResourceOutputStream(String id, String name) throws IOException;

	/**
	 * 将指定目录下的所有文件作为资源拷入。
	 * <p>
	 * 拷入后，目录下所有子文件的相对路径名（比如：<code>some-file.txt</code>、<code>some-directory/some-file.png</code>），即可作为此类的资源名称使用。
	 * </p>
	 * 
	 * @param id
	 *            {@linkplain Dashboard#getId()}
	 * @param directory
	 * @throws IOException
	 */
	void copyFrom(String id, File directory) throws IOException;

	/**
	 * 是否包含指定名称的资源。
	 * 
	 * @param id
	 *            {@linkplain Dashboard#getId()}
	 * @param name
	 *            资源名称
	 * @return
	 */
	boolean containsResource(String id, String name);

	/**
	 * 获取指定资源上次修改时间。
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	long lastModifiedResource(String id, String name);

	/**
	 * 列出所有资源。
	 * 
	 * @param id
	 * @return
	 */
	List<String> listResources(String id);

	/**
	 * 删除指定ID的所有资源。
	 * 
	 * @param id
	 */
	void delete(String id);

	/**
	 * 删除指定资源。
	 * 
	 * @param id
	 * @param name
	 */
	void delete(String id, String name);
}
