/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.connection.ConnectionSource;
import org.datagear.connection.IOUtil;
import org.datagear.dataexchange.BatchDataExchange;
import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeService;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataSourceConnectionFactory;
import org.datagear.dataexchange.FileReaderResourceFactory;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.SimpleBatchDataExchange;
import org.datagear.dataexchange.TextDataImportOption;
import org.datagear.dataexchange.support.CsvDataImport;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.vo.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据交换控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dataexchange")
public class DataExchangeController extends AbstractSchemaConnController
{
	protected static final String KEY_SESSION_BatchDataExchangeFutureInfoMap = DataExchangeController.class.getName()
			+ ".BatchDataExchangeFutureInfoMap";

	@Autowired
	private DataExchangeService<DataExchange> dataExchangeService;

	@Autowired
	@Qualifier("tempDataImportRootDirectory")
	private File tempDataImportRootDirectory;

	public DataExchangeController()
	{
		super();
	}

	public DataExchangeController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource,
			DataExchangeService<DataExchange> dataExchangeService, File tempDataImportRootDirectory)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.dataExchangeService = dataExchangeService;
		this.tempDataImportRootDirectory = tempDataImportRootDirectory;
	}

	public DataExchangeService<DataExchange> getDataExchangeService()
	{
		return dataExchangeService;
	}

	public void setDataExchangeService(DataExchangeService<DataExchange> dataExchangeService)
	{
		this.dataExchangeService = dataExchangeService;
	}

	public File getTempDataImportRootDirectory()
	{
		return tempDataImportRootDirectory;
	}

	public void setTempDataImportRootDirectory(File tempDataImportRootDirectory)
	{
		this.tempDataImportRootDirectory = tempDataImportRootDirectory;
	}

	@RequestMapping("/{schemaId}/import")
	public String impt(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		return "/dataexchange/import";
	}

	@RequestMapping("/{schemaId}/import/csv")
	public String imptCsv(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		DataFormat defaultDataFormat = new DataFormat();

		springModel.addAttribute("defaultDataFormat", defaultDataFormat);
		springModel.addAttribute("importId", UUID.gen());

		return "/dataexchange/import_csv";
	}

	@RequestMapping("/{schemaId}/import/db")
	public String imptDb(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		springModel.addAttribute("importId", UUID.gen());

		return "/dataexchange/import_db";
	}

	@RequestMapping(value = "/{schemaId}/import/uploadDataFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("importId") String importId,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		List<DataImportFileInfo> fileInfos = new ArrayList<DataImportFileInfo>();

		File directory = getTempDataImportDirectory(importId, true);

		String rawFileName = multipartFile.getOriginalFilename();
		boolean isZipFile = isZipFile(rawFileName);
		String serverFileName = UUID.gen();

		if (isZipFile)
		{
			File unzipDirectory = new File(directory, serverFileName);
			IOUtil.deleteFile(unzipDirectory);
			unzipDirectory.mkdirs();

			ZipInputStream in = null;

			try
			{
				in = new ZipInputStream(multipartFile.getInputStream());
				// TODO ZIP中存在中文名文件时解压会报错
				IOUtil.unzip(in, unzipDirectory);
			}
			finally
			{
				IOUtil.close(in);
			}

			listDataImportFileInfos(unzipDirectory, new CsvFileFilger(), serverFileName, rawFileName, fileInfos);
		}
		else
		{
			File importFile = IOUtil.getFile(directory, serverFileName);

			InputStream in = null;
			OutputStream importFileOut = null;
			try
			{
				in = multipartFile.getInputStream();
				importFileOut = IOUtil.getOutputStream(importFile);
				IOUtil.write(in, importFileOut);
			}
			finally
			{
				IOUtil.close(in);
				IOUtil.close(importFileOut);
			}

			DataImportFileInfo fileInfo = new DataImportFileInfo(UUID.gen(), serverFileName, importFile.length(),
					rawFileName, DataImportFileInfo.fileNameToTableName(rawFileName));

			fileInfos.add(fileInfo);
		}

		return fileInfos;
	}

	@RequestMapping(value = "/{schemaId}/import/csv/doImport")
	@ResponseBody
	public ResponseEntity<OperationMessage> doImportCsv(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("importId") String importId,
			TextDataImportForm dataImportForm) throws Exception
	{
		if (dataImportForm == null || isEmpty(dataImportForm.getDataFormat())
				|| isEmpty(dataImportForm.getImportOption()) || isEmpty(dataImportForm.getFileEncoding())
				|| isEmpty(dataImportForm.getFileIds()) || isEmpty(dataImportForm.getFileNames())
				|| isEmpty(dataImportForm.getTableNames())
				|| dataImportForm.getFileIds().length != dataImportForm.getFileNames().length
				|| dataImportForm.getFileNames().length != dataImportForm.getTableNames().length)
			throw new IllegalInputException();

		String[] fileIds = dataImportForm.getFileIds();
		String[] fileNames = dataImportForm.getFileNames();
		String[] tableNames = dataImportForm.getTableNames();

		File directory = getTempDataImportDirectory(importId, true);

		List<ResourceFactory<Reader>> readerFactories = toReaderResourceFactories(directory,
				dataImportForm.getFileEncoding(), fileNames);

		List<String> importTables = new ArrayList<String>(tableNames.length);
		Collections.addAll(importTables, tableNames);

		Schema schema = getSchemaNotNull(request, response, schemaId);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new SchemaDataSource(schema));

		List<CsvDataImport> csvDataImports = CsvDataImport.valuesOf(connectionFactory, dataImportForm.getDataFormat(),
				dataImportForm.getImportOption(), importTables, readerFactories);

		BatchDataExchange<CsvDataImport> batchDataExchange = new SimpleBatchDataExchange<CsvDataImport>(connectionFactory,
				csvDataImports);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeFutureInfo<CsvDataImport> futureInfo = new BatchDataExchangeFutureInfo<CsvDataImport>(
				importId, batchDataExchange, fileIds);
		storeBatchDataExchangeFutureInfo(request, futureInfo);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping("/{schemaId}/export")
	public String expt(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		return "/dataexchange/export";
	}

	@RequestMapping(value = "/{schemaId}/cancel")
	@ResponseBody
	public ResponseEntity<OperationMessage> cancel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			TextDataImportForm dataImportForm) throws Exception
	{
		String[] subDataExchangeIds = request.getParameterValues("subDataExchangeIds");

		if (subDataExchangeIds == null)
			throw new IllegalInputException();

		BatchDataExchangeFutureInfo<?> futureInfo = retrieveBatchDataExchangeFutureInfo(request, dataExchangeId);

		if (futureInfo == null)
			throw new IllegalInputException();

		boolean[] cancels = futureInfo.cancel(subDataExchangeIds);

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSuccessEmptyResponseEntity();
		responseEntity.getBody().setData(cancels);

		return responseEntity;
	}

	/**
	 * 将{@linkplain BatchDataExchangeFutureInfo}存储至session中。
	 * 
	 * @param request
	 * @param batchDataExchangeFutureInfo
	 */
	@SuppressWarnings("unchecked")
	protected void storeBatchDataExchangeFutureInfo(HttpServletRequest request,
			BatchDataExchangeFutureInfo<?> batchDataExchangeFutureInfo)
	{
		HttpSession session = request.getSession();

		Map<String, BatchDataExchangeFutureInfo<?>> map = null;

		synchronized (session)
		{
			map = (Map<String, BatchDataExchangeFutureInfo<?>>) session
					.getAttribute(KEY_SESSION_BatchDataExchangeFutureInfoMap);

			if (map == null)
			{
				map = new Hashtable<String, BatchDataExchangeFutureInfo<?>>();
				session.setAttribute(KEY_SESSION_BatchDataExchangeFutureInfoMap, map);
			}
		}

		map.put(batchDataExchangeFutureInfo.getDataExchangeId(), batchDataExchangeFutureInfo);
	}

	/**
	 * 从session中取回{@linkplain BatchDataExchangeFutureInfo}。
	 * 
	 * @param request
	 * @param dataExchangeId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected BatchDataExchangeFutureInfo<?> retrieveBatchDataExchangeFutureInfo(HttpServletRequest request,
			String dataExchangeId)
	{
		HttpSession session = request.getSession();

		Map<String, BatchDataExchangeFutureInfo<?>> map = (Map<String, BatchDataExchangeFutureInfo<?>>) session
				.getAttribute(KEY_SESSION_BatchDataExchangeFutureInfoMap);

		if (map == null)
			return null;

		return map.get(dataExchangeId);
	}

	protected List<ResourceFactory<Reader>> toReaderResourceFactories(File directory, String charset,
			String... fileNames)
	{
		List<ResourceFactory<Reader>> readerFactories = new ArrayList<ResourceFactory<Reader>>(fileNames.length);
		for (String fileName : fileNames)
		{
			File file = new File(directory, fileName);
			readerFactories.add(FileReaderResourceFactory.valueOf(file, charset));
		}

		return readerFactories;
	}

	protected void listDataImportFileInfos(File directory, FileFilter fileFilter, String parentPath,
			String displayParentPath, List<DataImportFileInfo> dataImportFileInfos)
	{
		if (parentPath == null)
			parentPath = "";
		else if (parentPath.isEmpty())
			;
		else
		{
			if (!parentPath.endsWith(File.separator))
				parentPath += File.separator;
		}

		if (displayParentPath == null)
			displayParentPath = "";
		else if (displayParentPath.isEmpty())
			;
		else
		{
			if (!displayParentPath.endsWith(File.separator))
				displayParentPath += File.separator;
		}

		File[] files = directory.listFiles(fileFilter);

		for (File file : files)
		{
			String myPath = parentPath + file.getName();
			String myDisplayPath = displayParentPath + file.getName();

			if (file.isDirectory())
				listDataImportFileInfos(file, fileFilter, myPath, myDisplayPath, dataImportFileInfos);
			else
			{
				DataImportFileInfo fileInfo = new DataImportFileInfo(UUID.gen(), myPath, file.length(), myDisplayPath,
						DataImportFileInfo.fileNameToTableName(file.getName()));

				dataImportFileInfos.add(fileInfo);
			}
		}
	}

	protected boolean isZipFile(String fileName)
	{
		if (fileName == null || fileName.isEmpty())
			return false;

		return fileName.toLowerCase().endsWith(".zip");
	}

	protected File getTempDataImportDirectory(String importId, boolean notNull)
	{
		File directory = new File(this.tempDataImportRootDirectory, importId);

		if (notNull && !directory.exists())
			directory.mkdirs();

		return directory;
	}

	public static class DataImportFileInfo extends FileInfo
	{
		private static final long serialVersionUID = 1L;

		private String id;

		/** 导入表名 */
		private String tableName;

		public DataImportFileInfo()
		{
			super();
		}

		public DataImportFileInfo(String id, String name, long bytes, String displayName, String tableName)
		{
			super(name, bytes);
			this.id = id;
			super.setDisplayName(displayName);
			this.tableName = tableName;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName = tableName;
		}

		/**
		 * 文件名转换为表名。
		 * 
		 * @param fileName
		 * @return
		 */
		public static String fileNameToTableName(String fileName)
		{
			if (fileName == null || fileName.isEmpty())
				return "";

			int slashIndex = fileName.indexOf('/');
			if (slashIndex < 0)
				slashIndex = fileName.indexOf('\\');

			String tableName = fileName.substring(slashIndex + 1);

			int dotIndex = tableName.indexOf('.');

			tableName = tableName.substring(0, dotIndex);

			return tableName;
		}
	}

	protected static class CsvFileFilger implements FileFilter
	{
		public CsvFileFilger()
		{
			super();
		}

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			else
				return file.getName().toLowerCase().endsWith(".csv");
		}
	}

	/**
	 * 文本导入表单。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextDataImportForm implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private DataFormat dataFormat;

		private TextDataImportOption importOption;

		private String fileEncoding;

		private String[] fileIds;

		private String[] fileNames;

		private String[] tableNames;

		public TextDataImportForm()
		{
			super();
		}

		public DataFormat getDataFormat()
		{
			return dataFormat;
		}

		public void setDataFormat(DataFormat dataFormat)
		{
			this.dataFormat = dataFormat;
		}

		public TextDataImportOption getImportOption()
		{
			return importOption;
		}

		public void setImportOption(TextDataImportOption importOption)
		{
			this.importOption = importOption;
		}

		public String getFileEncoding()
		{
			return fileEncoding;
		}

		public void setFileEncoding(String fileEncoding)
		{
			this.fileEncoding = fileEncoding;
		}

		public String[] getFileIds()
		{
			return fileIds;
		}

		public void setFileIds(String[] fileIds)
		{
			this.fileIds = fileIds;
		}

		public String[] getFileNames()
		{
			return fileNames;
		}

		public void setFileNames(String[] fileNames)
		{
			this.fileNames = fileNames;
		}

		public String[] getTableNames()
		{
			return tableNames;
		}

		public void setTableNames(String[] tableNames)
		{
			this.tableNames = tableNames;
		}
	}

	protected static class BatchDataExchangeFutureInfo<T extends DataExchange> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String dataExchangeId;

		private transient BatchDataExchange<T> batchDataExchange;

		private String[] subDataExchangeIds;

		private transient List<Future<T>> _subDataExchangeFutures;

		public BatchDataExchangeFutureInfo()
		{
			super();
		}

		public BatchDataExchangeFutureInfo(String dataExchangeId, BatchDataExchange<T> batchDataExchange,
				String[] subDataExchangeIds)
		{
			super();
			this.dataExchangeId = dataExchangeId;
			this.batchDataExchange = batchDataExchange;
			this.subDataExchangeIds = subDataExchangeIds;
			this._subDataExchangeFutures = batchDataExchange.getResults();
		}

		public String getDataExchangeId()
		{
			return dataExchangeId;
		}

		public void setDataExchangeId(String dataExchangeId)
		{
			this.dataExchangeId = dataExchangeId;
		}

		public BatchDataExchange<T> getBatchDataExchange()
		{
			return batchDataExchange;
		}

		public void setBatchDataExchange(BatchDataExchange<T> batchDataExchange)
		{
			this.batchDataExchange = batchDataExchange;
			this._subDataExchangeFutures = batchDataExchange.getResults();
		}

		public String[] getSubDataExchangeIds()
		{
			return subDataExchangeIds;
		}

		public void setSubDataExchangeIds(String[] subDataExchangeIds)
		{
			this.subDataExchangeIds = subDataExchangeIds;
		}

		/**
		 * 取消指定的子数据交换。
		 * 
		 * @param subDataExchangeId
		 * @return
		 */
		public boolean[] cancel(String... subDataExchangeIds)
		{
			boolean[] cancels = new boolean[subDataExchangeIds.length];

			for (int i = 0; i < subDataExchangeIds.length; i++)
			{
				boolean cancel = false;

				int index = getSubDataExchangeIndex(subDataExchangeIds[i]);

				if (index < 0)
					cancel = false;
				else
					cancel = this._subDataExchangeFutures.get(index).cancel(false);

				cancels[i] = cancel;
			}

			return cancels;
		}

		protected int getSubDataExchangeIndex(String subDataExchangeId)
		{
			if (this.subDataExchangeIds == null)
				return -1;

			for (int i = 0; i < this.subDataExchangeIds.length; i++)
			{
				if (this.subDataExchangeIds[i].equals(subDataExchangeId))
					return i;
			}

			return -1;
		}
	}
}
