/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 基于XML文件的{@linkplain DriverEntityManager}。
 * 
 * @author datagear@163.com
 *
 */
public class XmlDriverEntityManager extends AbstractFileDriverEntityManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlDriverEntityManager.class);

	public static final String DEFAULT_DRIVER_ENTITY_INFO_FILE_NAME = "driverEntityInfo.xml";

	public static final String ELEMENT_NAME_ROOT = "driver-entities";

	public static final String ELEMENT_NAME_DRIVER_ENTITY = "driver-entity";

	public static final String ELEMENT_NAME_ID = "id";

	public static final String ELEMENT_NAME_DRIVER_CLASS_NAME = "driver-class-name";

	public static final String ELEMENT_NAME_DISPLAY_NAME = "display-name";

	public static final String ELEMENT_NAME_DISPLAY_DESC = "display-desc";

	public XmlDriverEntityManager()
	{
		super();
	}

	public XmlDriverEntityManager(String rootDirectory)
	{
		super(rootDirectory, DEFAULT_DRIVER_ENTITY_INFO_FILE_NAME);
	}

	public XmlDriverEntityManager(File rootDirectory)
	{
		super(rootDirectory, DEFAULT_DRIVER_ENTITY_INFO_FILE_NAME);
	}

	@Override
	protected List<DriverEntity> readDriverEntities(File driverEntityInfoFile) throws DriverEntityManagerException
	{
		List<DriverEntity> driverEntities = new ArrayList<DriverEntity>();

		DocumentBuilderFactory documentBuilderFactory;
		DocumentBuilder documentBuilder;
		Document document;
		Reader reader = getDriverEntityInfoFileReader();

		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(new InputSource(reader));

			NodeList nodeList = document.getElementsByTagName(ELEMENT_NAME_DRIVER_ENTITY);

			for (int i = 0; i < nodeList.getLength(); i++)
			{
				DriverEntity driverEntity = new DriverEntity();

				Node node = nodeList.item(i);
				NodeList children = node.getChildNodes();

				for (int j = 0; j < children.getLength(); j++)
				{
					Node child = children.item(j);

					String nodeName = child.getNodeName();
					String nodeContent = child.getTextContent();
					if (nodeContent != null)
						nodeContent = nodeContent.trim();

					if (ELEMENT_NAME_ID.equalsIgnoreCase(nodeName))
						driverEntity.setId(nodeContent);
					else if (ELEMENT_NAME_DRIVER_CLASS_NAME.equalsIgnoreCase(nodeName))
						driverEntity.setDriverClassName(nodeContent);
					else if (ELEMENT_NAME_DISPLAY_NAME.equalsIgnoreCase(nodeName))
						driverEntity.setDisplayName(nodeContent);
					else if (ELEMENT_NAME_DISPLAY_DESC.equalsIgnoreCase(nodeName))
						driverEntity.setDisplayDesc(nodeContent);
				}

				if (driverEntity.getId() != null && !driverEntity.getId().isEmpty()
						&& driverEntity.getDriverClassName() != null && !driverEntity.getDriverClassName().isEmpty())
				{
					removeExists(driverEntities, driverEntity.getId());
					driverEntities.add(driverEntity);

					if (LOGGER.isDebugEnabled())
						LOGGER.debug(
								"Read a [" + driverEntity + "] from file [" + driverEntityInfoFile.getPath() + "]");
				}
			}
		}
		catch (Exception e)
		{
			throw new DriverEntityManagerException(e);
		}
		finally
		{
			close(reader);
		}

		return driverEntities;
	}

	@Override
	protected void writeDriverEntities(List<DriverEntity> driverEntities, File driverEntityInfoFile)
			throws DriverEntityManagerException
	{

		Writer writer = getDriverEntityInfoFileWriter();

		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			document.setXmlStandalone(true);

			Element root = document.createElement(ELEMENT_NAME_ROOT);

			if (driverEntities != null)
			{
				for (DriverEntity driverEntity : driverEntities)
				{
					Element driverEntityEle = document.createElement(ELEMENT_NAME_DRIVER_ENTITY);

					Element idEle = document.createElement(ELEMENT_NAME_ID);
					idEle.setTextContent(driverEntity.getId());

					Element driverClassNameEle = document.createElement(ELEMENT_NAME_DRIVER_CLASS_NAME);
					driverClassNameEle.setTextContent(driverEntity.getDriverClassName());

					Element displayNameEle = document.createElement(ELEMENT_NAME_DISPLAY_NAME);
					displayNameEle.setTextContent(driverEntity.getDisplayName());

					Element displayDescEle = document.createElement(ELEMENT_NAME_DISPLAY_DESC);
					displayDescEle.setTextContent(driverEntity.getDisplayDesc());

					driverEntityEle.appendChild(idEle);
					driverEntityEle.appendChild(driverClassNameEle);
					driverEntityEle.appendChild(displayNameEle);
					driverEntityEle.appendChild(displayDescEle);

					root.appendChild(driverEntityEle);
				}
			}

			document.appendChild(root);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		}
		catch (Exception e)
		{
			throw new DriverEntityManagerException(e);
		}
		finally
		{
			close(writer);
		}
	}
}
