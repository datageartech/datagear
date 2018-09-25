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

	public static final String ELEMENT_NAME_JRE_VERSION = "jre-version";

	public static final String ELEMENT_NAME_DATABASE_NAME = "database-name";

	public static final String ELEMENT_NAME_DATABASE_VERSIONS = "database-versions";

	public static final String ELEMENT_NAME_DATABASE_VERSION = "database-version";

	public XmlDriverEntityManager()
	{
		super();
		setDriverEntityInfoFileName(DEFAULT_DRIVER_ENTITY_INFO_FILE_NAME);
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
					else if (ELEMENT_NAME_JRE_VERSION.equalsIgnoreCase(nodeName))
						driverEntity.setJreVersion(nodeContent);
					else if (ELEMENT_NAME_DATABASE_NAME.equalsIgnoreCase(nodeName))
						driverEntity.setDatabaseName(nodeContent);
					else if (ELEMENT_NAME_DATABASE_VERSIONS.equalsIgnoreCase(nodeName))
					{
						NodeList dbVersionChildren = child.getChildNodes();
						int dbVersionLength = dbVersionChildren.getLength();

						if (dbVersionLength > 0)
						{
							List<String> databaseVersions = new ArrayList<String>(dbVersionLength);

							for (int k = 0; k < dbVersionLength; k++)
							{
								Node dbVersionNode = dbVersionChildren.item(k);

								if (!ELEMENT_NAME_DATABASE_VERSION.equalsIgnoreCase(dbVersionNode.getNodeName()))
									continue;

								String dbVersionNodeContent = dbVersionNode.getTextContent();
								if (dbVersionNodeContent != null)
									dbVersionNodeContent = dbVersionNodeContent.trim();

								if (!dbVersionNodeContent.isEmpty())
									databaseVersions.add(dbVersionNodeContent);
							}

							if (!databaseVersions.isEmpty())
								driverEntity.setDatabaseVersions(databaseVersions);
						}
					}
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

					driverEntityEle.appendChild(idEle);

					Element driverClassNameEle = document.createElement(ELEMENT_NAME_DRIVER_CLASS_NAME);
					driverClassNameEle.setTextContent(driverEntity.getDriverClassName());

					driverEntityEle.appendChild(driverClassNameEle);

					if (driverEntity.hasDisplayName())
					{
						Element displayNameEle = document.createElement(ELEMENT_NAME_DISPLAY_NAME);
						displayNameEle.setTextContent(driverEntity.getDisplayName());

						driverEntityEle.appendChild(displayNameEle);
					}

					if (driverEntity.hasDisplayDesc())
					{
						Element displayDescEle = document.createElement(ELEMENT_NAME_DISPLAY_DESC);
						displayDescEle.setTextContent(driverEntity.getDisplayDesc());

						driverEntityEle.appendChild(displayDescEle);
					}

					if (driverEntity.hasJreVersion())
					{
						Element jreVersionEle = document.createElement(ELEMENT_NAME_JRE_VERSION);
						jreVersionEle.setTextContent(driverEntity.getJreVersion());

						driverEntityEle.appendChild(jreVersionEle);
					}

					if (driverEntity.hasDatabaseName())
					{
						Element databaseNameEle = document.createElement(ELEMENT_NAME_DATABASE_NAME);
						databaseNameEle.setTextContent(driverEntity.getDatabaseName());

						driverEntityEle.appendChild(databaseNameEle);
					}

					if (driverEntity.hasDatabaseVersions())
					{
						Element databaseVersionsEle = document.createElement(ELEMENT_NAME_DATABASE_VERSIONS);

						List<String> databaseVersions = driverEntity.getDatabaseVersions();

						for (String databaseVersion : databaseVersions)
						{
							Element databaseVersionEle = document.createElement(ELEMENT_NAME_DATABASE_VERSION);
							databaseVersionEle.setTextContent(databaseVersion);

							databaseVersionsEle.appendChild(databaseVersionEle);
						}

						driverEntityEle.appendChild(databaseVersionsEle);
					}

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
