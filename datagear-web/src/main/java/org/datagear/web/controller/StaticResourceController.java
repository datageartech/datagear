package org.datagear.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * 静态资源控制器。
 * <p>
 * 此控制器负责将的<code>/static/*</code>请求映射至{@code org/datagear/web/webapp/staticResource}类路径内的资源。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
@Controller
public class StaticResourceController extends ResourceHttpRequestHandler
{
	public static final String STATIC_RESOURCE_CLASSPATH_ROOT = "org/datagear/web/webapp/staticResource/";

	public StaticResourceController()
	{
		super();

		List<Resource> locations = new ArrayList<Resource>(1);
		locations.add(new ClassPathResource(STATIC_RESOURCE_CLASSPATH_ROOT));
		setLocations(locations);
	}

	@RequestMapping("/static/**/*")
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		handleRequest(request, response);
	}
}
