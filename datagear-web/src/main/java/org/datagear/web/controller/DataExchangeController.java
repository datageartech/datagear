/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.IOUtil;
import org.datagear.dataexchange.BatchDataExchange;
import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeService;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataSourceConnectionFactory;
import org.datagear.dataexchange.FileReaderResourceFactory;
import org.datagear.dataexchange.FileWriterResourceFactory;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.SimpleBatchDataExchange;
import org.datagear.dataexchange.SqlQuery;
import org.datagear.dataexchange.TableQuery;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dataexchange.TextDataImportOption;
import org.datagear.dataexchange.support.CsvDataExport;
import org.datagear.dataexchange.support.CsvDataImport;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;
import org.datagear.dbinfo.TableType;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.cometd.dataexchange.CometdBatchDataExchangeListener;
import org.datagear.web.cometd.dataexchange.CometdSubTextDataExportListener;
import org.datagear.web.cometd.dataexchange.CometdSubTextDataImportListener;
import org.datagear.web.cometd.dataexchange.DataExchangeCometdService;
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
	public static final Pattern TABLE_NAME_QUERY_PATTERN = Pattern.compile("^\\s*\\S+\\s*$", Pattern.CASE_INSENSITIVE);

	protected static final String KEY_SESSION_BatchDataExchangeFutureInfoMap = DataExchangeController.class.getName()
			+ ".BatchDataExchangeFutureInfoMap";

	@Autowired
	private DataExchangeService<DataExchange> dataExchangeService;

	@Autowired
	@Qualifier("tempDataExchangeRootDirectory")
	private File tempDataExchangeRootDirectory;

	@Autowired
	private DataExchangeCometdService dataExchangeCometdService;

	@Autowired
	private DatabaseInfoResolver databaseInfoResolver;

	public DataExchangeController()
	{
		super();
	}

	public DataExchangeController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource,
			DataExchangeService<DataExchange> dataExchangeService, File tempDataExchangeRootDirectory,
			DataExchangeCometdService dataExchangeCometdService, DatabaseInfoResolver databaseInfoResolver)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.dataExchangeService = dataExchangeService;
		this.tempDataExchangeRootDirectory = tempDataExchangeRootDirectory;
		this.dataExchangeCometdService = dataExchangeCometdService;
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DataExchangeService<DataExchange> getDataExchangeService()
	{
		return dataExchangeService;
	}

	public void setDataExchangeService(DataExchangeService<DataExchange> dataExchangeService)
	{
		this.dataExchangeService = dataExchangeService;
	}

	public File getTempDataExchangeRootDirectory()
	{
		return tempDataExchangeRootDirectory;
	}

	public void setTempDataExchangeRootDirectory(File tempDataExchangeRootDirectory)
	{
		this.tempDataExchangeRootDirectory = tempDataExchangeRootDirectory;
	}

	public DataExchangeCometdService getDataExchangeCometdService()
	{
		return dataExchangeCometdService;
	}

	public void setDataExchangeCometdService(DataExchangeCometdService dataExchangeCometdService)
	{
		this.dataExchangeCometdService = dataExchangeCometdService;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
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

		String dataExchangeId = UUID.gen();

		springModel.addAttribute("defaultDataFormat", defaultDataFormat);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		springModel.addAttribute("dataExchangeChannelId", getDataExchangeChannelId(dataExchangeId));
		springModel.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		springModel.addAttribute("defaultCharsetName", Charset.defaultCharset().name());

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

		springModel.addAttribute("dataExchangeId", UUID.gen());

		return "/dataexchange/import_db";
	}

	@RequestMapping(value = "/{schemaId}/import/uploadDataFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		List<DataImportFileInfo> fileInfos = new ArrayList<DataImportFileInfo>();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);

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

	@RequestMapping(value = "/{schemaId}/import/csv/doImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> doImportCsv(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			TextDataImportForm dataImportForm) throws Exception
	{
		if (dataImportForm == null || isEmpty(dataImportForm.getDataFormat())
				|| isEmpty(dataImportForm.getImportOption()) || isEmpty(dataImportForm.getFileEncoding())
				|| isEmpty(dataImportForm.getSubDataExchangeIds()) || isEmpty(dataImportForm.getFileNames())
				|| isEmpty(dataImportForm.getTableNames())
				|| dataImportForm.getSubDataExchangeIds().length != dataImportForm.getFileNames().length
				|| dataImportForm.getFileNames().length != dataImportForm.getTableNames().length)
			throw new IllegalInputException();

		String[] subDataExchangeIds = dataImportForm.getSubDataExchangeIds();
		String[] fileNames = dataImportForm.getFileNames();
		String[] tableNames = dataImportForm.getTableNames();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		List<ResourceFactory<Reader>> readerFactories = toReaderResourceFactories(directory,
				dataImportForm.getFileEncoding(), fileNames);

		List<String> importTables = new ArrayList<String>(tableNames.length);
		Collections.addAll(importTables, tableNames);

		Schema schema = getSchemaNotNull(request, response, schemaId);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new SchemaDataSource(schema));

		String importChannelId = getDataExchangeChannelId(dataExchangeId);
		ServerChannel importServerChannel = this.dataExchangeCometdService.getChannelWithCreation(importChannelId);

		List<CsvDataImport> csvDataImports = CsvDataImport.valuesOf(connectionFactory, dataImportForm.getDataFormat(),
				dataImportForm.getImportOption(), importTables, readerFactories);

		for (int i = 0; i < csvDataImports.size(); i++)
		{
			CsvDataImport csvDataImport = csvDataImports.get(i);

			CometdSubTextDataImportListener listener = new CometdSubTextDataImportListener(
					this.dataExchangeCometdService, importServerChannel, getMessageSource(), getLocale(request),
					subDataExchangeIds[i], csvDataImport.getImportOption().getExceptionResolve());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subDataExchangeIds[i]));
			listener.setSendExchangingMessageInterval(
					evalSendImportingMessageInterval(csvDataImports, csvDataImport, i));
			csvDataImport.setListener(listener);
		}

		BatchDataExchange<CsvDataImport> batchDataExchange = new SimpleBatchDataExchange<CsvDataImport>(
				connectionFactory, csvDataImports);

		CometdBatchDataExchangeListener<CsvDataImport> listener = new CometdBatchDataExchangeListener<CsvDataImport>(
				this.dataExchangeCometdService, importServerChannel, getMessageSource(), getLocale(request),
				subDataExchangeIds);

		batchDataExchange.setListener(listener);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeFutureInfo<CsvDataImport> futureInfo = new BatchDataExchangeFutureInfo<CsvDataImport>(
				dataExchangeId, batchDataExchange, subDataExchangeIds);
		storeBatchDataExchangeFutureInfo(request, futureInfo);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	/**
	 * 查看子数据交换日志。
	 * 
	 * @param request
	 * @param response
	 * @param schemaId
	 * @param dataExchangeId
	 * @param subDataExchangeId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/{schemaId}/viewLog")
	public String viewLog(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("subDataExchangeId") String subDataExchangeId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		springModel.addAttribute("dataExchangeId", dataExchangeId);
		springModel.addAttribute("subDataExchangeId", subDataExchangeId);

		return "/dataexchange/view_log";
	}

	/**
	 * 查看子数据交换日志。
	 * 
	 * @param request
	 * @param response
	 * @param schemaId
	 * @param dataExchangeId
	 * @param subDataExchangeId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/{schemaId}/getLogContent")
	public void getLogContent(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("subDataExchangeId") String subDataExchangeId) throws Throwable
	{
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);
		File logFile = getTempSubDataExchangeLogFile(logDirectory, subDataExchangeId);

		if (!logFile.exists())
			throw new IllegalInputException();

		response.setCharacterEncoding(RESPONSE_ENCODING);
		response.setContentType(CONTENT_TYPE_HTML);

		PrintWriter out = response.getWriter();

		Reader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile),
					CometdSubTextDataImportListener.LOG_FILE_CHARSET));

			IOUtil.write(reader, out);
		}
		finally
		{
			IOUtil.close(reader);
			out.flush();
		}
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

	@RequestMapping("/{schemaId}/export/csv")
	public String exptCsv(HttpServletRequest request, HttpServletResponse response,
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

		String dataExchangeId = UUID.gen();

		springModel.addAttribute("defaultDataFormat", defaultDataFormat);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		springModel.addAttribute("dataExchangeChannelId", getDataExchangeChannelId(dataExchangeId));
		springModel.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		springModel.addAttribute("defaultCharsetName", Charset.defaultCharset().name());

		return "/dataexchange/export_csv";
	}

	@RequestMapping(value = "/{schemaId}/export/csv/doExport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> doExportCsv(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			TextDataExportForm exportForm) throws Exception
	{
		if (exportForm == null || isEmpty(exportForm.getDataFormat()) || isEmpty(exportForm.getExportOption())
				|| isEmpty(exportForm.getFileEncoding()) || isEmpty(exportForm.getSubDataExchangeIds())
				|| isEmpty(exportForm.getQueries()) || isEmpty(exportForm.getFileNames())
				|| exportForm.getSubDataExchangeIds().length != exportForm.getQueries().length
				|| exportForm.getSubDataExchangeIds().length != exportForm.getFileNames().length)
			throw new IllegalInputException();

		String[] subDataExchangeIds = exportForm.getSubDataExchangeIds();
		String[] queries = exportForm.getQueries();
		String[] fileNames = exportForm.getFileNames();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		List<Query> queryList = toQueries(queries);

		List<ResourceFactory<Writer>> writerFactories = toWriterResourceFactories(directory,
				exportForm.getFileEncoding(), fileNames);

		Schema schema = getSchemaNotNull(request, response, schemaId);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new SchemaDataSource(schema));

		String exportChannelId = getDataExchangeChannelId(dataExchangeId);
		ServerChannel exportServerChannel = this.dataExchangeCometdService.getChannelWithCreation(exportChannelId);

		List<CsvDataExport> csvDataExports = CsvDataExport.valuesOf(connectionFactory, exportForm.getDataFormat(),
				exportForm.getExportOption(), queryList, writerFactories);

		for (int i = 0; i < csvDataExports.size(); i++)
		{
			CsvDataExport csvDataExport = csvDataExports.get(i);

			CometdSubTextDataExportListener listener = new CometdSubTextDataExportListener(
					this.dataExchangeCometdService, exportServerChannel, getMessageSource(), getLocale(request),
					subDataExchangeIds[i]);
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subDataExchangeIds[i]));
			listener.setSendExchangingMessageInterval(
					evalSendExportingMessageInterval(csvDataExports, csvDataExport, i));
			csvDataExport.setListener(listener);
		}

		BatchDataExchange<CsvDataExport> batchDataExchange = new SimpleBatchDataExchange<CsvDataExport>(
				connectionFactory, csvDataExports);

		CometdBatchDataExchangeListener<CsvDataExport> listener = new CometdBatchDataExchangeListener<CsvDataExport>(
				this.dataExchangeCometdService, exportServerChannel, getMessageSource(), getLocale(request),
				subDataExchangeIds);

		batchDataExchange.setListener(listener);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeFutureInfo<CsvDataExport> futureInfo = new BatchDataExchangeFutureInfo<CsvDataExport>(
				dataExchangeId, batchDataExchange, subDataExchangeIds);
		storeBatchDataExchangeFutureInfo(request, futureInfo);

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSuccessEmptyResponseEntity();

		Map<String, String> subDataExchangeFileNameMap = buildSubDataExchangeFileNameMap(subDataExchangeIds, fileNames);
		responseEntity.getBody().setData(subDataExchangeFileNameMap);

		return responseEntity;
	}

	@RequestMapping(value = "/{schemaId}/export/download")
	@ResponseBody
	public void exptDownload(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("fileName") String fileName) throws Exception
	{
		response.setCharacterEncoding(RESPONSE_ENCODING);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + new String(fileName.getBytes(RESPONSE_ENCODING), "iso-8859-1") + "");

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File file = new File(directory, fileName);

		OutputStream out = null;

		try
		{
			out = response.getOutputStream();
			IOUtil.write(file, out);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/{schemaId}/export/downloadAll")
	@ResponseBody
	public void exptDownloadAll(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId)
			throws Exception
	{
		String fileName = "export_csv.zip";

		response.setCharacterEncoding(RESPONSE_ENCODING);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + new String(fileName.getBytes(RESPONSE_ENCODING), "iso-8859-1") + "");

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);

		ZipOutputStream out = null;
		try
		{
			out = new ZipOutputStream(response.getOutputStream());
			IOUtil.writeFileToZipOutputStream(out, directory, null);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	protected Map<String, String> buildSubDataExchangeFileNameMap(String[] subDataExchangeIds, String[] fileNames)
	{
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < subDataExchangeIds.length; i++)
			map.put(subDataExchangeIds[i], fileNames[i]);

		return map;
	}

	protected List<Query> toQueries(String[] queries)
	{
		int size = queries.length;

		List<Query> list = new ArrayList<Query>(size);

		for (int i = 0; i < size; i++)
		{
			String query = queries[i];

			if (isTableNameQueryString(query))
				list.add(new TableQuery(query));
			else
				list.add(new SqlQuery(query));
		}

		return list;
	}

	protected boolean isTableNameQueryString(String query)
	{
		return TABLE_NAME_QUERY_PATTERN.matcher(query).matches();
	}

	protected List<ResourceFactory<Writer>> toWriterResourceFactories(File directory, String charset,
			String[] fileNames)
	{
		List<ResourceFactory<Writer>> writerFactories = new ArrayList<ResourceFactory<Writer>>(fileNames.length);
		for (String fileName : fileNames)
		{
			File file = new File(directory, fileName);
			writerFactories.add(FileWriterResourceFactory.valueOf(file, charset));
		}

		return writerFactories;
	}

	@RequestMapping(value = "/{schemaId}/getAllTableNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> getAllTableNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		TableInfo[] tableInfos = new ReturnSchemaConnExecutor<TableInfo[]>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected TableInfo[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				return getDatabaseInfoResolver().getTableInfos(getConnection());
			}

		}.execute();

		List<String> tableNames = excludeViewNames(tableInfos);
		Collections.sort(tableNames);

		return tableNames;
	}

	@RequestMapping(value = "/{schemaId}/cancel")
	@ResponseBody
	public ResponseEntity<OperationMessage> cancel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("schemaId") String schemaId, @RequestParam("dataExchangeId") String dataExchangeId)
			throws Exception
	{
		String[] subDataExchangeIds = request.getParameterValues("subDataExchangeId");

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

	protected List<String> excludeViewNames(TableInfo[] tableInfos)
	{
		List<String> list = new ArrayList<String>(tableInfos.length);

		for (TableInfo tableInfo : tableInfos)
		{
			if (TableType.TABLE.equals(tableInfo.getType()))
				list.add(tableInfo.getName());
		}

		return list;
	}

	/**
	 * 计算导出中消息发送间隔。
	 * <p>
	 * 如果发送频率过快，当导出文件很多时会出现cometd卡死的情况。
	 * </p>
	 * 
	 * @param csvDataExports
	 * @param csvDataExport
	 * @param index
	 * @return
	 */
	protected int evalSendExportingMessageInterval(List<CsvDataExport> csvDataExports, CsvDataExport csvDataExport,
			int index)
	{
		int interval = 500 * csvDataExports.size();

		if (interval > 5000)
			interval = 5000;

		return interval;
	}

	/**
	 * 计算导入中消息发送间隔。
	 * <p>
	 * 如果发送频率过快，当导入文件很多时会出现cometd卡死的情况。
	 * </p>
	 * 
	 * @param csvDataImports
	 * @param csvDataImport
	 * @param index
	 * @return
	 */
	protected int evalSendImportingMessageInterval(List<CsvDataImport> csvDataImports, CsvDataImport csvDataImport,
			int index)
	{
		int interval = 500 * csvDataImports.size();

		if (interval > 5000)
			interval = 5000;

		return interval;
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

	protected File getTempDataExchangeDirectory(String dataExchangeId, boolean notNull)
	{
		File directory = new File(this.tempDataExchangeRootDirectory, dataExchangeId);

		if (notNull && !directory.exists())
			directory.mkdirs();

		return directory;
	}

	protected File getTempDataExchangeLogDirectory(String dataExchangeId, boolean notNull)
	{
		File directory = new File(this.tempDataExchangeRootDirectory, dataExchangeId + "_logs");

		if (notNull && !directory.exists())
			directory.mkdirs();

		return directory;
	}

	protected File getTempSubDataExchangeLogFile(File logDirectory, String subDataExchangeId)
	{
		File logFile = new File(logDirectory, "log_" + subDataExchangeId + ".txt");
		return logFile;
	}

	protected File getExportFileZip(String dataExchangeId)
	{
		File file = new File(this.tempDataExchangeRootDirectory, dataExchangeId + ".zip");
		return file;
	}

	/**
	 * 获取指定数据交换操作ID对应的cometd通道ID。
	 * 
	 * @param dataExchangeId
	 * @return
	 */
	protected String getDataExchangeChannelId(String dataExchangeId)
	{
		return "/dataexchange/channel/" + dataExchangeId;
	}

	/**
	 * 获取可用字符集名称。
	 * 
	 * @return
	 */
	protected List<String> getAvailableCharsetNames()
	{
		List<String> charsetNames = new ArrayList<String>();

		Map<String, Charset> map = Charset.availableCharsets();
		Set<String> names = map.keySet();
		for (String name : names)
		{
			// 排除未在IANA注册的字符集
			if (name.startsWith("x-") || name.startsWith("X-"))
				continue;

			charsetNames.add(name);
		}

		return charsetNames;
	}

	public static class DataImportFileInfo extends FileInfo
	{
		private static final long serialVersionUID = 1L;

		private String subDataExchangeId;

		/** 导入表名 */
		private String tableName;

		public DataImportFileInfo()
		{
			super();
		}

		public DataImportFileInfo(String subDataExchangeId, String name, long bytes, String displayName,
				String tableName)
		{
			super(name, bytes);
			this.subDataExchangeId = subDataExchangeId;
			super.setDisplayName(displayName);
			this.tableName = tableName;
		}

		public String getSubDataExchangeId()
		{
			return subDataExchangeId;
		}

		public void setSubDataExchangeId(String subDataExchangeId)
		{
			this.subDataExchangeId = subDataExchangeId;
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

	public static class AbstractTextDataExchangeForm implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private DataFormat dataFormat;

		private String fileEncoding;

		private String[] subDataExchangeIds;

		public AbstractTextDataExchangeForm()
		{
			super();
		}

		public AbstractTextDataExchangeForm(DataFormat dataFormat, String fileEncoding, String[] subDataExchangeIds)
		{
			super();
			this.dataFormat = dataFormat;
			this.fileEncoding = fileEncoding;
			this.subDataExchangeIds = subDataExchangeIds;
		}

		public DataFormat getDataFormat()
		{
			return dataFormat;
		}

		public void setDataFormat(DataFormat dataFormat)
		{
			this.dataFormat = dataFormat;
		}

		public String getFileEncoding()
		{
			return fileEncoding;
		}

		public void setFileEncoding(String fileEncoding)
		{
			this.fileEncoding = fileEncoding;
		}

		public String[] getSubDataExchangeIds()
		{
			return subDataExchangeIds;
		}

		public void setSubDataExchangeIds(String[] subDataExchangeIds)
		{
			this.subDataExchangeIds = subDataExchangeIds;
		}
	}

	/**
	 * 文本导入表单。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextDataImportForm extends AbstractTextDataExchangeForm implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private TextDataImportOption importOption;

		private String[] fileNames;

		private String[] tableNames;

		public TextDataImportForm()
		{
			super();
		}

		public TextDataImportOption getImportOption()
		{
			return importOption;
		}

		public void setImportOption(TextDataImportOption importOption)
		{
			this.importOption = importOption;
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

	/**
	 * 文本导出表单。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextDataExportForm extends AbstractTextDataExchangeForm implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private TextDataExportOption exportOption;

		private String[] queries;

		private String[] fileNames;

		public TextDataExportForm()
		{
			super();
		}

		public TextDataExportOption getExportOption()
		{
			return exportOption;
		}

		public void setExportOption(TextDataExportOption exportOption)
		{
			this.exportOption = exportOption;
		}

		public String[] getQueries()
		{
			return queries;
		}

		public void setQueries(String[] queries)
		{
			this.queries = queries;
		}

		public String[] getFileNames()
		{
			return fileNames;
		}

		public void setFileNames(String[] fileNames)
		{
			this.fileNames = fileNames;
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
