/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.util.Collections;
import java.util.List;

/**
 * 简单{@linkplain HtmlTplDashboardImport}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleHtmlTplDashboardImport implements HtmlTplDashboardImport
{
	private List<ImportItem> importItems = Collections.emptyList();

	public SimpleHtmlTplDashboardImport()
	{
	}

	@SuppressWarnings("unchecked")
	public SimpleHtmlTplDashboardImport(List<? extends ImportItem> importItems)
	{
		super();
		this.importItems = ((List<ImportItem>) importItems);
	}

	@Override
	public List<ImportItem> getImportItems()
	{
		return importItems;
	}

	@SuppressWarnings("unchecked")
	public void setImportItems(List<? extends ImportItem> importItems)
	{
		this.importItems = ((List<ImportItem>) importItems);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [importItems=" + importItems + "]";
	}
}
