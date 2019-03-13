/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.List;
import java.util.Map;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.Extension;
import org.cometd.server.BayeuxServerImpl;

/**
 * {@linkplain BayeuxServer}工厂类。
 * 
 * @author datagear@163.com
 *
 */
public class BayeuxServerFactory
{
	private Map<String, Object> options;

	private List<Extension> extensions;

	public BayeuxServerFactory()
	{
		super();
	}

	public Map<String, Object> getOptions()
	{
		return options;
	}

	public void setOptions(Map<String, Object> options)
	{
		this.options = options;
	}

	public List<Extension> getExtensions()
	{
		return extensions;
	}

	public void setExtensions(List<Extension> extensions)
	{
		this.extensions = extensions;
	}

	public BayeuxServer getBayeuxServer()
	{
		BayeuxServerImpl bayeuxServerImpl = new BayeuxServerImpl();

		if (this.options != null)
			bayeuxServerImpl.setOptions(this.options);

		if (this.extensions != null)
		{
			for (Extension extension : this.extensions)
				bayeuxServerImpl.addExtension(extension);
		}

		return bayeuxServerImpl;
	}
}
