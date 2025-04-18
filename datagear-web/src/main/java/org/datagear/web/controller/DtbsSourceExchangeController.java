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
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.dataexchange.AbstractQuery;
import org.datagear.dataexchange.BatchDataExchange;
import org.datagear.dataexchange.BatchDataExchangeResult;
import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeService;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataFormatContext;
import org.datagear.dataexchange.DataImportDependencyResolver;
import org.datagear.dataexchange.DataImportDependencyResolver.AutoDataImportDependency;
import org.datagear.dataexchange.DataImportDependencyResolver.DataImportDependency;
import org.datagear.dataexchange.DataImportOption;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.SimpleBatchDataExchange;
import org.datagear.dataexchange.SqlQuery;
import org.datagear.dataexchange.SubDataExchange;
import org.datagear.dataexchange.TableQuery;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.dataexchange.support.CsvDataExport;
import org.datagear.dataexchange.support.CsvDataImport;
import org.datagear.dataexchange.support.ExcelDataExport;
import org.datagear.dataexchange.support.ExcelDataImport;
import org.datagear.dataexchange.support.JsonDataExport;
import org.datagear.dataexchange.support.JsonDataExportOption;
import org.datagear.dataexchange.support.JsonDataFormat;
import org.datagear.dataexchange.support.JsonDataImport;
import org.datagear.dataexchange.support.JsonDataImportOption;
import org.datagear.dataexchange.support.SqlDataExport;
import org.datagear.dataexchange.support.SqlDataExportOption;
import org.datagear.dataexchange.support.SqlDataImport;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.User;
import org.datagear.meta.SimpleTable;
import org.datagear.meta.TableUtil;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PersistenceManager;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.util.StringUtil;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.DataSourceConnectionFactory;
import org.datagear.util.resource.FileOutputStreamResourceFactory;
import org.datagear.util.resource.FileReaderResourceFactory;
import org.datagear.util.resource.FileWriterResourceFactory;
import org.datagear.util.resource.ResourceFactory;
import org.datagear.util.sqlvalidator.SqlValidator;
import org.datagear.web.dataexchange.MessageBatchDataExchangeListener;
import org.datagear.web.dataexchange.MessageSubDataImportListener;
import org.datagear.web.dataexchange.MessageSubTextDataExportListener;
import org.datagear.web.dataexchange.MessageSubTextValueDataImportListener;
import org.datagear.web.util.ExpiredSessionAttrManager;
import org.datagear.web.util.ExpiredSessionAttrManager.ExpiredSessionAttr;
import org.datagear.web.util.MessageChannel;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.SessionDashboardInfoSupport.DashboardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据源数据交换控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dtbsSourceExchange")
public class DtbsSourceExchangeController extends AbstractDtbsSourceConnController
{
	public static final Pattern TABLE_NAME_QUERY_PATTERN = Pattern.compile("^\\s*\\S+\\s*$", Pattern.CASE_INSENSITIVE);

	@Autowired
	private DataExchangeService<DataExchange> dataExchangeService;

	@Autowired
	private DataImportDependencyResolver dataImportDependencyResolver;

	@Autowired
	private DBMetaResolver dbMetaResolver;

	@Autowired
	private SqlValidator dsmanagerQuerySqlValidator;

	@Autowired
	private File tempDirectory;
	
	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private MessageChannel dataExchangeMessageChannel;

	@Autowired
	private ExpiredSessionAttrManager expiredSessionAttrManager;

	private long expiredBatchDataExchangeInfoMs = 1000 * 60 * 30;

	public DtbsSourceExchangeController()
	{
		super();
	}

	public DataExchangeService<DataExchange> getDataExchangeService()
	{
		return dataExchangeService;
	}

	public void setDataExchangeService(DataExchangeService<DataExchange> dataExchangeService)
	{
		this.dataExchangeService = dataExchangeService;
	}

	public DataImportDependencyResolver getDataImportDependencyResolver()
	{
		return dataImportDependencyResolver;
	}

	public void setDataImportDependencyResolver(DataImportDependencyResolver dataImportDependencyResolver)
	{
		this.dataImportDependencyResolver = dataImportDependencyResolver;
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	public SqlValidator getDsmanagerQuerySqlValidator()
	{
		return dsmanagerQuerySqlValidator;
	}

	public void setDsmanagerQuerySqlValidator(SqlValidator dsmanagerQuerySqlValidator)
	{
		this.dsmanagerQuerySqlValidator = dsmanagerQuerySqlValidator;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	public MessageChannel getDataExchangeMessageChannel()
	{
		return dataExchangeMessageChannel;
	}

	public void setDataExchangeMessageChannel(MessageChannel dataExchangeMessageChannel)
	{
		this.dataExchangeMessageChannel = dataExchangeMessageChannel;
	}

	public ExpiredSessionAttrManager getExpiredSessionAttrManager()
	{
		return expiredSessionAttrManager;
	}

	public void setExpiredSessionAttrManager(ExpiredSessionAttrManager expiredSessionAttrManager)
	{
		this.expiredSessionAttrManager = expiredSessionAttrManager;
	}

	public long getExpiredBatchDataExchangeInfoMs()
	{
		return expiredBatchDataExchangeInfoMs;
	}

	public void setExpiredBatchDataExchangeInfoMs(long expiredBatchDataExchangeInfoMs)
	{
		this.expiredBatchDataExchangeInfoMs = expiredBatchDataExchangeInfoMs;
	}

	@RequestMapping("/{dtbsSourceId}/import")
	public String impt(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		final User user = getCurrentUser();

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		return "/dtbsSourceExchange/import";
	}

	@RequestMapping("/{dtbsSourceId}/import/csv")
	public String imptCsv(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		final User user = getCurrentUser();
		setFormAction(springModel, "import", "doImport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		ValueDataImportOption importOption = new ValueDataImportOption(ExceptionResolve.ROLLBACK, false, false, true);
		
		DefaultTextValueFileBatchDataImportForm formModel = new DefaultTextValueFileBatchDataImportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setImportOption(importOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());
		formModel.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		formModel.setDependentNumberAuto(getMessage(request, "auto"));

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/import_csv";
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/csv/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> imptCsvUploadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(value = "zipFileNameEncoding", required = false) String zipFileNameEncoding)
			throws Exception
	{
		return uploadImportFile(request, response, dtbsSourceId, dataExchangeId, multipartFile, zipFileNameEncoding,
				new CsvFileFilter());
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/csv/doImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> imptCsvDoImport(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody DefaultTextValueFileBatchDataImportForm form) throws Throwable
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		final List<TextValueFileSubDataImportForm> subDataExchangeForms = form.getSubDataExchanges();
		
		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		final Set<SubDataExchange> subDataExchanges = new HashSet<SubDataExchange>(subDataExchangeForms.size());
		final List<AutoDataImportDependency> dds = new ArrayList<>(subDataExchangeForms.size());

		for (TextValueFileSubDataImportForm subForm : subDataExchangeForms)
		{
			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Reader> readerFactory = FileReaderResourceFactory.valueOf(file,
					form.getFileEncoding());

			CsvDataImport csvDataImport = new CsvDataImport(connectionFactory, form.getDataFormat(),
					form.getImportOption(), subForm.getTableName(), readerFactory);

			MessageSubTextValueDataImportListener listener = new MessageSubTextValueDataImportListener(
					this.dataExchangeMessageChannel, dataExchangeId, getMessageSource(), locale, subForm.getId(),
					csvDataImport.getImportOption().getExceptionResolve());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), csvDataImport));
			csvDataImport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), subForm.getNumber(), csvDataImport);
			AutoDataImportDependency dd = toAutoDataImportDependency(subDataExchange, subForm,
					form.getDependentNumberAuto());

			dds.add(dd);
		}

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				Connection cn = getConnection();

				List<SubDataExchange> mySubDataExchanges = dataImportDependencyResolver.resolveAuto(dds, cn);
				subDataExchanges.addAll(mySubDataExchanges);
			}
		}.execute();

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/import/sql")
	public String imptSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		final User user = getCurrentUser();
		setFormAction(springModel, "import", "doImport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();
		
		String dataExchangeId = IDUtil.uuid();
		DataImportOption importOption = new DataImportOption(ExceptionResolve.ROLLBACK);
		
		SqlFileBatchDataImportForm formModel = new SqlFileBatchDataImportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setImportOption(importOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());
		formModel.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		
		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/import_sql";
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/sql/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> imptSqlUploadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(value = "zipFileNameEncoding", required = false) String zipFileNameEncoding) throws Exception
	{
		return uploadImportFile(request, response, dtbsSourceId, dataExchangeId, multipartFile, zipFileNameEncoding,
				new SqlFileFilter());
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/sql/doImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> imptSqlDoImport(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestBody SqlFileBatchDataImportForm form)
			throws Exception
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		final List<FileSubDataImportForm> subDataExchangeForms = form.getSubDataExchanges();
		
		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		Set<SubDataExchange> subDataExchanges = new HashSet<SubDataExchange>(subDataExchangeForms.size());
		List<DataImportDependency> dds = new ArrayList<>(subDataExchangeForms.size());

		for (FileSubDataImportForm subForm : subDataExchangeForms)
		{
			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Reader> readerFactory = FileReaderResourceFactory.valueOf(file,
					form.getFileEncoding());

			SqlDataImport sqlDataImport = new SqlDataImport(connectionFactory, form.getImportOption(),
					readerFactory);

			MessageSubDataImportListener listener = new MessageSubDataImportListener(this.dataExchangeMessageChannel,
					dataExchangeId, getMessageSource(), locale, subForm.getId(),
					form.getImportOption().getExceptionResolve());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), sqlDataImport));
			sqlDataImport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), subForm.getNumber(), sqlDataImport);
			DataImportDependency dd = toDataImportDependency(subDataExchange, subForm);

			dds.add(dd);
		}

		List<SubDataExchange> mySubDataExchanges = dataImportDependencyResolver.resolve(dds);
		subDataExchanges.addAll(mySubDataExchanges);

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/import/json")
	public String imptJson(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		final User user = getCurrentUser();
		setFormAction(springModel, "import", "doImport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();
		
		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		defaultDataFormat.setBinaryFormat("0x" + DataFormatContext.wrapToExpression(DataFormat.BINARY_FORMAT_HEX));
		JsonDataImportOption importOption = new JsonDataImportOption(ExceptionResolve.ROLLBACK, false, false, true,
				JsonDataFormat.ROW_ARRAY);
		
		JsonFileBatchDataImportForm formModel = new JsonFileBatchDataImportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setImportOption(importOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());
		formModel.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		formModel.setDependentNumberAuto(getMessage(request, "auto"));

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/import_json";
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/json/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> imptJsonUploadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(value = "zipFileNameEncoding", required = false) String zipFileNameEncoding) throws Exception
	{
		return uploadImportFile(request, response, dtbsSourceId, dataExchangeId, multipartFile, zipFileNameEncoding,
				new JsonFileFilter());
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/json/doImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> imptJsonDoImport(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody JsonFileBatchDataImportForm form) throws Throwable
	{
		form.check();

		JsonDataImportOption importOption = form.getImportOption();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		final List<TextValueFileSubDataImportForm> subDataExchangeForms = form.getSubDataExchanges();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		final Set<SubDataExchange> subDataExchanges = new HashSet<SubDataExchange>(subDataExchangeForms.size());
		final List<AutoDataImportDependency> dds = new ArrayList<>(subDataExchangeForms.size());

		for (TextValueFileSubDataImportForm subForm : subDataExchangeForms)
		{
			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Reader> readerFactory = FileReaderResourceFactory.valueOf(file,
					form.getFileEncoding());

			JsonDataImport jsonDataImport = new JsonDataImport(connectionFactory, form.getDataFormat(),
					form.getImportOption(), subForm.getTableName(), readerFactory);

			MessageSubTextValueDataImportListener listener = new MessageSubTextValueDataImportListener(
					this.dataExchangeMessageChannel, dataExchangeId, getMessageSource(), locale, subForm.getId(),
					jsonDataImport.getImportOption().getExceptionResolve());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), jsonDataImport));
			jsonDataImport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), subForm.getNumber(), jsonDataImport);
			AutoDataImportDependency dd = toAutoDataImportDependency(subDataExchange, subForm,
					form.getDependentNumberAuto());

			dds.add(dd);
		}

		if (JsonDataFormat.ROW_ARRAY.equals(importOption.getJsonDataFormat()))
		{
			new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
			{
				@Override
				protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
						DtbsSource dtbsSource) throws Throwable
				{
					Connection cn = getConnection();

					List<SubDataExchange> mySubDataExchanges = dataImportDependencyResolver.resolveAuto(dds, cn);
					subDataExchanges.addAll(mySubDataExchanges);
				}
			}.execute();
		}
		else
		{
			List<SubDataExchange> mySubDataExchanges = dataImportDependencyResolver.resolve(dds);
			subDataExchanges.addAll(mySubDataExchanges);
		}

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/import/excel")
	public String imptExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		final User user = getCurrentUser();
		setFormAction(springModel, "import", "doImport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		ValueDataImportOption importOption = new ValueDataImportOption(ExceptionResolve.ROLLBACK, false, false, true);
		
		DefaultTextValueFileBatchDataImportForm formModel = new DefaultTextValueFileBatchDataImportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setImportOption(importOption);
		formModel.setFileEncoding(IOUtil.CHARSET_UTF_8);
		formModel.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		formModel.setDependentNumberAuto(getMessage(request, "auto"));

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/import_excel";
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/excel/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataImportFileInfo> imptExcelUploadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(value = "zipFileNameEncoding", required = false) String zipFileNameEncoding) throws Exception
	{
		return uploadImportFile(request, response, dtbsSourceId, dataExchangeId, multipartFile, zipFileNameEncoding,
				new ExcelFileFilter());
	}

	@RequestMapping(value = "/{dtbsSourceId}/import/excel/doImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> imptExcelDoImport(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody DefaultTextValueFileBatchDataImportForm form) throws Throwable
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		final List<TextValueFileSubDataImportForm> subDataExchangeForms = form.getSubDataExchanges();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		final Set<SubDataExchange> subDataExchanges = new HashSet<SubDataExchange>(subDataExchangeForms.size());
		final List<AutoDataImportDependency> dds = new ArrayList<>(subDataExchangeForms.size());

		for (TextValueFileSubDataImportForm subForm : subDataExchangeForms)
		{
			File file = FileUtil.getFile(directory, subForm.getFileName());

			ExcelDataImport excelDataImport = new ExcelDataImport(connectionFactory, form.getDataFormat(),
					form.getImportOption(), file);
			excelDataImport.setUnifiedTable(subForm.getTableName());

			MessageSubTextValueDataImportListener listener = new MessageSubTextValueDataImportListener(
					this.dataExchangeMessageChannel, dataExchangeId, getMessageSource(), locale, subForm.getId(),
					excelDataImport.getImportOption().getExceptionResolve());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), excelDataImport));
			excelDataImport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), subForm.getNumber(), excelDataImport);
			AutoDataImportDependency dd = toAutoDataImportDependency(subDataExchange, subForm,
					form.getDependentNumberAuto());

			dds.add(dd);
		}

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				Connection cn = getConnection();

				List<SubDataExchange> mySubDataExchanges = dataImportDependencyResolver.resolveAuto(dds, cn);
				subDataExchanges.addAll(mySubDataExchanges);
			}
		}.execute();

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}
	
	/**
	 * 查看子数据交换日志。
	 * 
	 * @param request
	 * @param response
	 * @param dtbsSourceId
	 * @param dataExchangeId
	 * @param subDataExchangeId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/{dtbsSourceId}/viewLog")
	public String viewLog(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("subDataExchangeId") String subDataExchangeId) throws Throwable
	{
		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
			}
		}.execute();

		springModel.addAttribute("dataExchangeId", dataExchangeId);
		springModel.addAttribute("subDataExchangeId", subDataExchangeId);

		return "/dtbsSourceExchange/detail_log";
	}

	/**
	 * 查看子数据交换日志。
	 * 
	 * @param request
	 * @param response
	 * @param dtbsSourceId
	 * @param dataExchangeId
	 * @param subDataExchangeId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/{dtbsSourceId}/getLogContent")
	public void getLogContent(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
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
					MessageSubTextValueDataImportListener.LOG_FILE_CHARSET));

			IOUtil.write(reader, out);
		}
		finally
		{
			IOUtil.close(reader);
			out.flush();
		}
	}

	@RequestMapping("/{dtbsSourceId}/export")
	public String expt(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			//XXX 不能使用这里的注解参数方式，因为其中包含的','，会被拆分为数组
			//@RequestParam(value="query", required = false) String[] queries,
			@RequestParam(value="queryScript", required = false) String queryScript,
			@RequestParam(value="queryScriptDelimiter", required = false) String queryScriptDelimiter) throws Throwable
	{
		String[] queries = request.getParameterValues("query");
		queries = (queries == null ? new String[0] : queries);
		
		final User user = getCurrentUser();

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		List<String> queryList = new ArrayList<>();

		if (!isEmpty(queries))
			queryList.addAll(Arrays.asList(queries));

		if (!StringUtil.isEmpty(queryScript))
		{
			SqlScriptParser sqlScriptParser = new SqlScriptParser(new StringReader(queryScript));
			if (!isEmpty(queryScriptDelimiter))
				sqlScriptParser.setDelimiter(queryScriptDelimiter);

			List<SqlStatement> sqlStatements = sqlScriptParser.parseAll();
			if (!StringUtil.isEmpty(sqlStatements))
			{
				for (SqlStatement sqlst : sqlStatements)
					queryList.add(sqlst.getSql());
			}
		}

		addAttributeForWriteJson(springModel, "queries", queryList);

		return "/dtbsSourceExchange/export";
	}

	@RequestMapping("/{dtbsSourceId}/export/csv")
	public String exptCsv(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId
			//XXX 不能使用这里的注解参数方式，因为其中包含的','，会被拆分为数组
			//@RequestParam(value="query", required = false) String[] queries
			) throws Throwable
	{
		String[] queries = request.getParameterValues("query");
		queries = (queries == null ? new String[0] : queries);
		
		final User user = getCurrentUser();
		setFormAction(springModel, "export", "doExport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());
				springModel.addAttribute(DtbsSourceDataController.KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		TextDataExportOption exportOption = new TextDataExportOption(false);
		
		DefaultTextFileBatchDataExportForm formModel = new DefaultTextFileBatchDataExportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setExportOption(exportOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "queries", queries);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/export_csv";
	}

	@RequestMapping(value = "/{dtbsSourceId}/export/csv/doExport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> exptCsvDoExport(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestBody DefaultTextFileBatchDataExportForm form)
			throws Exception
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		List<TextFileSubDataExportForm> subDataExchangeForms = form.getSubDataExchanges();
		
		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		Set<SubDataExchange> subDataExchanges = new HashSet<>();

		for (TextFileSubDataExportForm subForm : subDataExchangeForms)
		{
			Query query = toQuery(subForm.getQuery());

			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(file,
					form.getFileEncoding());

			CsvDataExport csvDataExport = new CsvDataExport(connectionFactory, form.getDataFormat(),
					form.getExportOption(), query, writerFactory);

			MessageSubTextDataExportListener listener = new MessageSubTextDataExportListener(
					this.dataExchangeMessageChannel,
					dataExchangeId, getMessageSource(), getLocale(request), subForm.getId());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), csvDataExport));
			csvDataExport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), csvDataExport);
			subDataExchanges.add(subDataExchange);
		}

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/export/excel")
	public String exptExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId
			//XXX 不能使用这里的注解参数方式，因为其中包含的','，会被拆分为数组
			//@RequestParam(value="query", required = false) String[] queries
			) throws Throwable
	{
		String[] queries = request.getParameterValues("query");
		queries = (queries == null ? new String[0] : queries);
		
		final User user = getCurrentUser();
		setFormAction(springModel, "export", "doExport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());
				springModel.addAttribute(DtbsSourceDataController.KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		TextDataExportOption exportOption = new TextDataExportOption(false);
		
		DefaultTextFileBatchDataExportForm formModel = new DefaultTextFileBatchDataExportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setExportOption(exportOption);
		formModel.setFileEncoding(IOUtil.CHARSET_UTF_8);

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "queries", queries);
		//占位，避免页面空指针
		addAttributeForWriteJson(springModel, "availableCharsetNames", Collections.emptyList());

		return "/dtbsSourceExchange/export_excel";
	}

	@RequestMapping(value = "/{dtbsSourceId}/export/excel/doExport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> exptExcelDoExport(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestBody DefaultTextFileBatchDataExportForm form)
			throws Exception
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		List<TextFileSubDataExportForm> subDataExchangeForms = form.getSubDataExchanges();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));

		Locale locale = getLocale(request);

		Set<SubDataExchange> subDataExchanges = new HashSet<>();

		for (TextFileSubDataExportForm subForm : subDataExchangeForms)
		{
			Query query = toQuery(subForm.getQuery());

			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<OutputStream> writerFactory = FileOutputStreamResourceFactory.valueOf(file);

			ExcelDataExport excelDataExport = new ExcelDataExport(connectionFactory, form.getDataFormat(),
					form.getExportOption(), query, writerFactory);

			MessageSubTextDataExportListener listener = new MessageSubTextDataExportListener(
					this.dataExchangeMessageChannel,
					dataExchangeId, getMessageSource(), getLocale(request), subForm.getId());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), excelDataExport));
			excelDataExport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), excelDataExport);
			subDataExchanges.add(subDataExchange);
		}

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/export/sql")
	public String exptSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId
			//XXX 不能使用这里的注解参数方式，因为其中包含的','，会被拆分为数组
			//@RequestParam(value="query", required = false) String[] queries
			) throws Throwable
	{
		String[] queries = request.getParameterValues("query");
		queries = (queries == null ? new String[0] : queries);
		
		final User user = getCurrentUser();
		setFormAction(springModel, "export", "doExport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());
				springModel.addAttribute(DtbsSourceDataController.KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		defaultDataFormat.setDateFormat("'" + DataFormatContext.wrapToExpression(DataFormat.DEFAULT_DATE_FORMAT) + "'");
		defaultDataFormat.setTimeFormat("'" + DataFormatContext.wrapToExpression(DataFormat.DEFAULT_TIME_FORMAT) + "'");
		defaultDataFormat.setTimestampFormat(
				"'" + DataFormatContext.wrapToExpression(DataFormat.DEFAULT_TIMESTAMP_FORMAT) + "'");
		defaultDataFormat.setBinaryFormat("0x" + DataFormatContext.wrapToExpression(DataFormat.BINARY_FORMAT_HEX));
		SqlDataExportOption exportOption = new SqlDataExportOption(false, false);
		
		SqlFileBatchDataExportForm formModel = new SqlFileBatchDataExportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setExportOption(exportOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "queries", queries);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/export_sql";
	}

	@RequestMapping(value = "/{dtbsSourceId}/export/sql/doExport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> exptSqlDoExport(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestBody SqlFileBatchDataExportForm form)
			throws Exception
	{
		form.check();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		List<TableNameTextFileSubDataExportForm> subDataExchangeForms = form.getSubDataExchanges();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		Set<SubDataExchange> subDataExchanges = new HashSet<>();

		for (TableNameTextFileSubDataExportForm subForm : subDataExchangeForms)
		{
			Query query = toQuery(subForm.getQuery());

			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(file,
					form.getFileEncoding());

			SqlDataExport sqlDataExport = new SqlDataExport(connectionFactory, form.getDataFormat(),
					form.getExportOption(), query, subForm.getTableName(), writerFactory);

			MessageSubTextDataExportListener listener = new MessageSubTextDataExportListener(
					this.dataExchangeMessageChannel,
					dataExchangeId, getMessageSource(), getLocale(request), subForm.getId());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), sqlDataExport));
			sqlDataExport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), sqlDataExport);
			subDataExchanges.add(subDataExchange);
		}

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/{dtbsSourceId}/export/json")
	public String exptJson(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId
			//XXX 不能使用这里的注解参数方式，因为其中包含的','，会被拆分为数组
			//@RequestParam(value="query", required = false) String[] queries
			) throws Throwable
	{
		String[] queries = request.getParameterValues("query");
		queries = (queries == null ? new String[0] : queries);
		
		final User user = getCurrentUser();
		setFormAction(springModel, "export", "doExport");

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());
				springModel.addAttribute(DtbsSourceDataController.KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
			}
		}.execute();

		String dataExchangeId = IDUtil.uuid();
		DataFormat defaultDataFormat = new DataFormat();
		defaultDataFormat.setBinaryFormat("0x" + DataFormatContext.wrapToExpression(DataFormat.BINARY_FORMAT_HEX));
		JsonDataExportOption exportOption = new JsonDataExportOption(false, JsonDataFormat.ROW_ARRAY);
		
		JsonFileBatchDataExportForm formModel = new JsonFileBatchDataExportForm();
		formModel.setDataExchangeId(dataExchangeId);
		formModel.setDataFormat(defaultDataFormat);
		formModel.setExportOption(exportOption);
		formModel.setFileEncoding(Charset.defaultCharset().name());

		setFormModel(springModel, formModel);
		springModel.addAttribute("dataExchangeId", dataExchangeId);
		addAttributeForWriteJson(springModel, "queries", queries);
		addAttributeForWriteJson(springModel, "availableCharsetNames", getAvailableCharsetNames());

		return "/dtbsSourceExchange/export_json";
	}

	@RequestMapping(value = "/{dtbsSourceId}/export/json/doExport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> exptJsonDoExport(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestBody JsonFileBatchDataExportForm form)
			throws Exception
	{
		form.check();

		JsonDataExportOption exportOption = form.getExportOption();

		final User user = getCurrentUser();

		String dataExchangeId = form.getDataExchangeId();
		List<TableNameTextFileSubDataExportForm> subDataExchangeForms = form.getSubDataExchanges();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File logDirectory = getTempDataExchangeLogDirectory(dataExchangeId, true);

		DtbsSource dtbsSource = getDtbsSourceForUserNotNull(user, dtbsSourceId);

		checkDeleteTableDataPermission(dtbsSource, user);

		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(new DtbsSourceDataSource(dtbsSource));
		Locale locale = getLocale(request);

		Set<SubDataExchange> subDataExchanges = new HashSet<>();

		for (TableNameTextFileSubDataExportForm subForm : subDataExchangeForms)
		{
			Query query = toQuery(subForm.getQuery());

			File file = FileUtil.getFile(directory, subForm.getFileName());
			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(file,
					form.getFileEncoding());

			JsonDataExport csvDataExport = new JsonDataExport(connectionFactory, form.getDataFormat(),
					exportOption, query, writerFactory, subForm.getTableName());

			MessageSubTextDataExportListener listener = new MessageSubTextDataExportListener(
					this.dataExchangeMessageChannel,
					dataExchangeId, getMessageSource(), getLocale(request), subForm.getId());
			listener.setLogFile(getTempSubDataExchangeLogFile(logDirectory, subForm.getId()));
			listener.setSendExchangingMessageInterval(
					evalSendDataExchangingMessageInterval(subDataExchangeForms.size(), csvDataExport));
			csvDataExport.setListener(listener);

			SubDataExchange subDataExchange = new SubDataExchange(subForm.getId(), csvDataExport);
			subDataExchanges.add(subDataExchange);
		}

		BatchDataExchange batchDataExchange = buildBatchDataExchange(connectionFactory, subDataExchanges,
				dataExchangeId, locale);

		this.dataExchangeService.exchange(batchDataExchange);

		BatchDataExchangeInfo batchDataExchangeInfo = new BatchDataExchangeInfo(dataExchangeId, batchDataExchange);
		setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/{dtbsSourceId}/export/download")
	@ResponseBody
	public void exptDownload(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("fileName") String fileName) throws Throwable
	{
		final User user = getCurrentUser();

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		response.setCharacterEncoding(RESPONSE_ENCODING);
		setDownloadResponseHeader(request, response, fileName);

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);
		File file = FileUtil.getFile(directory, fileName);

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

	@RequestMapping(value = "/{dtbsSourceId}/export/downloadAll")
	@ResponseBody
	public void exptDownloadAll(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel,
			@PathVariable("dtbsSourceId") String dtbsSourceId, @RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam("fileName") String fileName) throws Throwable
	{
		final User user = getCurrentUser();

		new VoidDtbsSourceConnExecutor(request, response, springModel, dtbsSourceId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					DtbsSource dtbsSource) throws Throwable
			{
				checkDeleteTableDataPermission(dtbsSource, user);
			}
		}.execute();

		response.setCharacterEncoding(RESPONSE_ENCODING);
		setDownloadResponseHeader(request, response, fileName);

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

	@RequestMapping(value = "/{dtbsSourceId}/message", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Object> message(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestParam("dataExchangeId") String dataExchangeId,
			@RequestParam(value = "messageCount", required = false) Integer messageCount) throws Throwable
	{
		if (messageCount == null)
			messageCount = 50;
		if (messageCount < 1)
			messageCount = 1;

		return this.dataExchangeMessageChannel.poll(dataExchangeId, messageCount);
	}
	
	protected AutoDataImportDependency toAutoDataImportDependency(SubDataExchange subDataExchange,
			TextValueFileSubDataImportForm form, String autoFlag)
	{
		String index = form.getNumber();
		String tableName = form.getTableName();
		String dependentNumber = form.getDependentNumber();

		if (StringUtil.isEquals(autoFlag, dependentNumber))
		{
			return new AutoDataImportDependency(subDataExchange, index, tableName);
		}
		else
		{
			String[] dependIndexes = (StringUtil.isBlank(dependentNumber) ? DataImportDependency.DEPEND_INDEXES_EMPTY
					: StringUtil.split(dependentNumber, ",", true));

			return new AutoDataImportDependency(subDataExchange, index, dependIndexes);
		}
	}

	protected DataImportDependency toDataImportDependency(SubDataExchange subDataExchange, FileSubDataImportForm form)
	{
		String index = form.getNumber();
		String dependentNumber = form.getDependentNumber();
		String[] dependIndexes = (StringUtil.isBlank(dependentNumber) ? DataImportDependency.DEPEND_INDEXES_EMPTY
				: StringUtil.split(dependentNumber, ",", true));

		return new DataImportDependency(subDataExchange, index, dependIndexes);
	}

	protected List<DataImportFileInfo> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			String dtbsSourceId, String dataExchangeId, MultipartFile multipartFile, String zipFileNameEncoding,
			FileFilter fileFilter) throws Exception
	{
		List<DataImportFileInfo> fileInfos = new ArrayList<>();

		File directory = getTempDataExchangeDirectory(dataExchangeId, true);

		String rawFileName = multipartFile.getOriginalFilename();
		boolean isZipFile = isZipFile(rawFileName);
		String serverFileName = IDUtil.uuid();

		if (isZipFile)
		{
			File unzipDirectory = FileUtil.getFile(directory, serverFileName);
			FileUtil.deleteFile(unzipDirectory);
			unzipDirectory.mkdirs();

			ZipInputStream in = null;

			try
			{
				in = IOUtil.getZipInputStream(multipartFile.getInputStream(), zipFileNameEncoding);
				IOUtil.unzipCheckMalformed(in, unzipDirectory);
			}
			finally
			{
				IOUtil.close(in);
			}

			listDataImportFileInfos(unzipDirectory, fileFilter, serverFileName, rawFileName, fileInfos);
		}
		else
		{
			int extIndex = rawFileName.lastIndexOf('.');
			if (extIndex >= 0 && extIndex < rawFileName.length() - 1)
				serverFileName += "." + rawFileName.substring(extIndex + 1);

			File importFile = FileUtil.getFile(directory, serverFileName);

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

			DataImportFileInfo fileInfo = new DataImportFileInfo(serverFileName, importFile.length(), rawFileName,
					DataImportFileInfo.fileNameToTableName(rawFileName));

			fileInfos.add(fileInfo);
		}

		return fileInfos;
	}

	protected BatchDataExchange buildBatchDataExchange(ConnectionFactory connectionFactory,
			Set<SubDataExchange> subDataExchanges, String channel, Locale locale)
	{
		BatchDataExchange batchDataExchange = new SimpleBatchDataExchange(connectionFactory, subDataExchanges);

		MessageBatchDataExchangeListener listener = new MessageBatchDataExchangeListener(
				this.dataExchangeMessageChannel, channel,
				getMessageSource(), locale);
		batchDataExchange.setListener(listener);

		return batchDataExchange;
	}

	protected Query toQuery(String query)
	{
		AbstractQuery re = (isTableNameQueryString(query) ? new TableQuery(query) : new SqlQuery(query));
		re.setSqlValidator(this.dsmanagerQuerySqlValidator);
		return re;
	}

	protected boolean isTableNameQueryString(String query)
	{
		return TABLE_NAME_QUERY_PATTERN.matcher(query).matches();
	}

	@RequestMapping(value = "/{dtbsSourceId}/getAllTableNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> getAllTableNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("dtbsSourceId") String dtbsSourceId)
			throws Throwable
	{
		List<String> tableNames = new ReturnDtbsSourceConnExecutor<List<String>>(request, response, springModel,
				dtbsSourceId, true)
		{
			@Override
			protected List<String> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, DtbsSource dtbsSource) throws Throwable
			{
				Connection cn = getConnection();
				List<SimpleTable> tables = getDbMetaResolver().getEntityTables(cn);

				return TableUtil.namesOf(tables, true);
			}

		}.execute();

		return tableNames;
	}

	@RequestMapping(value = "/{dtbsSourceId}/cancel")
	@ResponseBody
	public ResponseEntity<OperationMessage> cancel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("dtbsSourceId") String dtbsSourceId,
			@RequestBody CancelDataExchangeForm form) throws Exception
	{
		if (isEmpty(form.getDataExchangeId()))
			throw new IllegalInputException();

		BatchDataExchangeInfo batchDataExchangeInfo = getSessionBatchDataExchangeInfo(request, form.getDataExchangeId());
		
		if (batchDataExchangeInfo != null && form.getSubDataExchangeIds() != null)
		{
			BatchDataExchange batchDataExchange = batchDataExchangeInfo.getBatchDataExchange();
			
			// BatchDataExchangeInfo.batchDataExchange是transient的，
			// 在可能的分布式会话方案时可能为null，需在这里校验
			if (batchDataExchange != null)
			{
				List<String> subDataExchangeIds = form.getSubDataExchangeIds();
				BatchDataExchangeResult batchDataExchangeResult = batchDataExchange.getResult();

				for (String subDataExchangeId : subDataExchangeIds)
					batchDataExchangeResult.cancel(subDataExchangeId);

				setSessionBatchDataExchangeInfo(request, batchDataExchangeInfo);
			}
		}
		
		return optSuccessResponseEntity(request);
	}
	
	/**
	 * 计算导入/导出中消息发送间隔。
	 * <p>
	 * 如果发送频率过快，当数据交换很多时会出现卡死的情况。
	 * </p>
	 * 
	 * @param total
	 * @param dataExchange
	 * @return
	 */
	protected int evalSendDataExchangingMessageInterval(int total, DataExchange dataExchange)
	{
		int interval = 500 * total;

		if (interval > 5000)
			interval = 5000;

		return interval;
	}

	/**
	 * 获取会话中的{@linkplain BatchDataExchangeInfo}。
	 * <p>
	 * 如果修改了获取的{@linkplain BatchDataExchangeInfo}的状态，应在修改之后调用{@linkplain #setSessionBatchDataExchangeInfo(HttpServletRequest, BatchDataExchangeInfo)}，
	 * 以为可能扩展的分布式会话提供支持。
	 * </p>
	 * 
	 * @param request
	 * @param dataExchangeId
	 * @return
	 */
	protected BatchDataExchangeInfo getSessionBatchDataExchangeInfo(HttpServletRequest request, String dataExchangeId)
	{
		HttpSession session = request.getSession();
		String name = toSessionNameForBatchDataExchangeInfo(request, dataExchangeId);
		return (BatchDataExchangeInfo) session.getAttribute(name);
	}

	/**
	 * 设置会话中的{@linkplain BatchDataExchangeInfo}。
	 * 
	 * @param request
	 * @param batchDataExchangeInfo
	 */
	protected void setSessionBatchDataExchangeInfo(HttpServletRequest request, BatchDataExchangeInfo batchDataExchangeInfo)
	{
		HttpSession session = request.getSession();
		String name = toSessionNameForBatchDataExchangeInfo(request, batchDataExchangeInfo.getDataExchangeId());
		session.setAttribute(name, batchDataExchangeInfo);

		removeSessionExpiredBatchDataExchangeInfo(request);
	}

	/**
	 * 移除会话中过期的{@linkplain BatchDataExchangeInfo}。
	 * <p>
	 * 目前需要获取会话中{@linkplain BatchDataExchangeInfo}的操作只有
	 * {@linkplain #cancel(HttpServletRequest, HttpServletResponse, String, CancelDataExchangeForm)}，
	 * 所以移除基本没有什么影响。
	 * </p>
	 * <p>
	 * 需定时清理，防止会话中存储过多已过期的信息
	 * </p>
	 * 
	 * @param request
	 */
	protected void removeSessionExpiredBatchDataExchangeInfo(HttpServletRequest request)
	{
		getExpiredSessionAttrManager().removeExpired(request, this.expiredBatchDataExchangeInfoMs,
				DashboardInfo.class);
	}

	protected String toSessionNameForBatchDataExchangeInfo(HttpServletRequest request, String dataExchangeId)
	{
		return "dataexchange-" + dataExchangeId;
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
				DataImportFileInfo fileInfo = new DataImportFileInfo(myPath, file.length(), myDisplayPath,
						DataImportFileInfo.fileNameToTableName(file.getName()));

				dataImportFileInfos.add(fileInfo);
			}
		}
	}

	protected boolean isZipFile(String fileName)
	{
		return FileUtil.isExtension(fileName, "zip");
	}

	protected File getTempDataExchangeDirectory(String dataExchangeId, boolean notNull)
	{
		File directory = FileUtil.getDirectory(getDataExchangeTmpDirectory(), dataExchangeId, true);
		return directory;
	}

	protected File getTempDataExchangeLogDirectory(String dataExchangeId, boolean notNull)
	{
		File directory = FileUtil.getDirectory(getDataExchangeTmpDirectory(), dataExchangeId + "_logs", true);
		return directory;
	}

	protected File getTempSubDataExchangeLogFile(File logDirectory, String subDataExchangeId)
	{
		File logFile = FileUtil.getFile(logDirectory, "log_" + subDataExchangeId + ".txt");
		return logFile;
	}

	protected File getExportFileZip(String dataExchangeId)
	{
		File file = FileUtil.getFile(getDataExchangeTmpDirectory(), dataExchangeId + ".zip");
		return file;
	}

	protected File getDataExchangeTmpDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "dataExchange", true);
	}

	public static class DataImportFileInfo extends FileInfo
	{
		private static final long serialVersionUID = 1L;

		/** 导入表名 */
		private String tableName;

		public DataImportFileInfo()
		{
			super();
		}

		public DataImportFileInfo(String name, long bytes, String displayName, String tableName)
		{
			super(name, false, bytes);
			super.setDisplayName(displayName);
			this.tableName = tableName;
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

	protected static class CsvFileFilter implements FileFilter
	{
		public CsvFileFilter()
		{
			super();
		}

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			else
				return (FileUtil.isExtension(file, "csv") ||  FileUtil.isExtension(file, "txt"));
		}
	}

	protected static class SqlFileFilter implements FileFilter
	{
		public SqlFileFilter()
		{
			super();
		}

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			else
				return (FileUtil.isExtension(file, "sql") ||  FileUtil.isExtension(file, "txt"));
		}
	}

	protected static class JsonFileFilter implements FileFilter
	{
		public JsonFileFilter()
		{
			super();
		}

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			else
				return (FileUtil.isExtension(file, "json") ||  FileUtil.isExtension(file, "txt"));
		}
	}

	protected static class ExcelFileFilter implements FileFilter
	{
		public ExcelFileFilter()
		{
			super();
		}

		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return true;
			else
				return (FileUtil.isExtension(file, "xlsx") ||  FileUtil.isExtension(file, "xls"));
		}
	}

	public static abstract class AbstractFileBatchDataExchangeForm<T extends AbstractFileSubDataExchangeForm> implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String dataExchangeId;

		private List<T> subDataExchanges;

		private String fileEncoding = "";

		private String zipFileNameEncoding = "";

		public AbstractFileBatchDataExchangeForm()
		{
			super();
		}

		public String getDataExchangeId()
		{
			return dataExchangeId;
		}

		public void setDataExchangeId(String dataExchangeId)
		{
			this.dataExchangeId = dataExchangeId;
		}

		public List<T> getSubDataExchanges()
		{
			return subDataExchanges;
		}

		public void setSubDataExchanges(List<T> subDataExchanges)
		{
			this.subDataExchanges = subDataExchanges;
		}

		public String getFileEncoding()
		{
			return fileEncoding;
		}

		public void setFileEncoding(String fileEncoding)
		{
			this.fileEncoding = fileEncoding;
		}

		public String getZipFileNameEncoding()
		{
			return zipFileNameEncoding;
		}

		public void setZipFileNameEncoding(String zipFileNameEncoding)
		{
			this.zipFileNameEncoding = zipFileNameEncoding;
		}
		
		public void check() throws IllegalInputException
		{
			if(StringUtil.isEmpty(this.dataExchangeId) || StringUtil.isEmpty(this.fileEncoding))
				throw new IllegalInputException();
			
			checkSubDataExchanges();
		}
		
		protected void checkSubDataExchanges() throws IllegalInputException
		{
			if(StringUtil.isEmpty(this.subDataExchanges))
				throw new IllegalInputException();
			
			for(T sub : this.subDataExchanges)
				checkSubDataExchange(sub);
		}
		
		protected void checkSubDataExchange(T subDataExchange) throws IllegalInputException
		{
			if(StringUtil.isEmpty(subDataExchange.getId()) || StringUtil.isEmpty(subDataExchange.getFileName()))
				throw new IllegalInputException();
		}
	}
	
	public static abstract class AbstractFileSubDataExchangeForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;
		
		private String id;
		
		private String fileName;

		public AbstractFileSubDataExchangeForm()
		{
			super();
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getFileName()
		{
			return fileName;
		}

		public void setFileName(String fileName)
		{
			this.fileName = fileName;
		}
	}

	public static abstract class AbstractFileBatchDataImportForm<T extends FileSubDataImportForm> extends AbstractFileBatchDataExchangeForm<T>
	{
		private static final long serialVersionUID = 1L;
		
		/** 自动处理导入条目依赖编号的字面值 */
		private String dependentNumberAuto = null;

		public AbstractFileBatchDataImportForm()
		{
			super();
		}
		
		public String getDependentNumberAuto()
		{
			return dependentNumberAuto;
		}

		public void setDependentNumberAuto(String dependentNumberAuto)
		{
			this.dependentNumberAuto = dependentNumberAuto;
		}

		@Override
		protected void checkSubDataExchange(T subDataExchange) throws IllegalInputException
		{
			super.checkSubDataExchange(subDataExchange);

			if(StringUtil.isEmpty(subDataExchange.getNumber()))
				throw new IllegalInputException();
		}
	}
	
	public static class FileSubDataImportForm extends AbstractFileSubDataExchangeForm
	{
		private static final long serialVersionUID = 1L;

		/** 导入条目编号 */
		private String number;

		/** 导入条目依赖编号 */
		private String dependentNumber;

		public FileSubDataImportForm()
		{
			super();
		}

		public String getNumber()
		{
			return number;
		}

		public void setNumber(String number)
		{
			this.number = number;
		}

		public String getDependentNumber()
		{
			return dependentNumber;
		}

		public void setDependentNumber(String dependentNumber)
		{
			this.dependentNumber = dependentNumber;
		}
	}

	public static abstract class AbstractTextValueFileBatchDataImportForm<T extends TextValueFileSubDataImportForm> extends AbstractFileBatchDataImportForm<T>
	{
		private static final long serialVersionUID = 1L;

		private ValueDataImportOption importOption;

		private DataFormat dataFormat;

		public AbstractTextValueFileBatchDataImportForm()
		{
			super();
		}

		public ValueDataImportOption getImportOption()
		{
			return importOption;
		}

		public void setImportOption(ValueDataImportOption importOption)
		{
			this.importOption = importOption;
		}

		public DataFormat getDataFormat()
		{
			return dataFormat;
		}

		public void setDataFormat(DataFormat dataFormat)
		{
			this.dataFormat = dataFormat;
		}

		@Override
		public void check() throws IllegalInputException
		{
			if(this.importOption == null || this.dataFormat == null)
				throw new IllegalInputException();
			
			super.check();
		}

		@Override
		protected void checkSubDataExchange(T subDataExchange) throws IllegalInputException
		{
			super.checkSubDataExchange(subDataExchange);
			checkSubDataExchangeTableName(subDataExchange);
		}
		
		protected void checkSubDataExchangeTableName(T subDataExchange) throws IllegalInputException
		{
			if(StringUtil.isEmpty(subDataExchange.getTableName()))
				throw new IllegalInputException();
		}
	}
	
	public static class TextValueFileSubDataImportForm extends FileSubDataImportForm
	{
		private static final long serialVersionUID = 1L;
		
		private String tableName;

		public TextValueFileSubDataImportForm()
		{
			super();
		}

		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName = tableName;
		}
	}

	public static class DefaultTextValueFileBatchDataImportForm extends AbstractTextValueFileBatchDataImportForm<TextValueFileSubDataImportForm>
	{
		private static final long serialVersionUID = 1L;

		public DefaultTextValueFileBatchDataImportForm()
		{
			super();
		}
	}

	public static class JsonFileBatchDataImportForm extends AbstractTextValueFileBatchDataImportForm<TextValueFileSubDataImportForm>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public JsonDataImportOption getImportOption()
		{
			return (JsonDataImportOption)super.getImportOption();
		}

		public void setImportOption(JsonDataImportOption importOption)
		{
			super.setImportOption(importOption);
		}

		@Override
		protected void checkSubDataExchangeTableName(TextValueFileSubDataImportForm subDataExchange)
				throws IllegalInputException
		{
			if(JsonDataFormat.ROW_ARRAY.equals(getImportOption().getJsonDataFormat()))
				super.checkSubDataExchangeTableName(subDataExchange);
		}
	}

	public static class SqlFileBatchDataImportForm extends AbstractFileBatchDataImportForm<FileSubDataImportForm>
	{
		private static final long serialVersionUID = 1L;

		private DataImportOption importOption;

		public SqlFileBatchDataImportForm()
		{
			super();
		}

		public DataImportOption getImportOption()
		{
			return importOption;
		}

		public void setImportOption(DataImportOption importOption)
		{
			this.importOption = importOption;
		}
	}

	public static abstract class AbstractTextFileBatchDataExportForm<T extends TextFileSubDataExportForm> extends AbstractFileBatchDataExchangeForm<T>
	{
		private static final long serialVersionUID = 1L;

		private TextDataExportOption exportOption;

		private DataFormat dataFormat;

		public AbstractTextFileBatchDataExportForm()
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

		public DataFormat getDataFormat()
		{
			return dataFormat;
		}

		public void setDataFormat(DataFormat dataFormat)
		{
			this.dataFormat = dataFormat;
		}

		@Override
		public void check() throws IllegalInputException
		{
			if(this.exportOption == null || this.dataFormat == null)
				throw new IllegalInputException();
			
			super.check();
		}

		@Override
		protected void checkSubDataExchange(T subDataExchange) throws IllegalInputException
		{
			super.checkSubDataExchange(subDataExchange);

			if(StringUtil.isEmpty(subDataExchange.getQuery()))
				throw new IllegalInputException();
		}
		
		public abstract T createTextFileSubDataExportForm();
	}

	public static class DefaultTextFileBatchDataExportForm extends AbstractTextFileBatchDataExportForm<TextFileSubDataExportForm>
	{
		private static final long serialVersionUID = 1L;

		public DefaultTextFileBatchDataExportForm()
		{
			super();
		}

		@Override
		public TextFileSubDataExportForm createTextFileSubDataExportForm()
		{
			return new TextFileSubDataExportForm();
		}
	}
	
	public static class TextFileSubDataExportForm extends AbstractFileSubDataExchangeForm
	{
		private static final long serialVersionUID = 1L;

		private String query;

		public TextFileSubDataExportForm()
		{
			super();
		}

		public String getQuery()
		{
			return query;
		}

		public void setQuery(String query)
		{
			this.query = query;
		}
	}
	
	public static class TableNameTextFileSubDataExportForm extends TextFileSubDataExportForm
	{
		private static final long serialVersionUID = 1L;
		
		private String tableName;

		public TableNameTextFileSubDataExportForm()
		{
			super();
		}

		public String getTableName()
		{
			return tableName;
		}

		public void setTableName(String tableName)
		{
			this.tableName = tableName;
		}
	}

	public static class SqlFileBatchDataExportForm extends AbstractTextFileBatchDataExportForm<TableNameTextFileSubDataExportForm>
	{
		private static final long serialVersionUID = 1L;

		public SqlFileBatchDataExportForm()
		{
			super();
		}

		@Override
		public SqlDataExportOption getExportOption()
		{
			return (SqlDataExportOption)super.getExportOption();
		}

		public void setExportOption(SqlDataExportOption exportOption)
		{
			super.setExportOption(exportOption);
		}

		@Override
		protected void checkSubDataExchange(TableNameTextFileSubDataExportForm subDataExchange) throws IllegalInputException
		{
			super.checkSubDataExchange(subDataExchange);

			if(StringUtil.isEmpty(subDataExchange.getTableName()))
				throw new IllegalInputException();
		}

		@Override
		public TableNameTextFileSubDataExportForm createTextFileSubDataExportForm()
		{
			return new TableNameTextFileSubDataExportForm();
		}
	}

	public static class JsonFileBatchDataExportForm extends AbstractTextFileBatchDataExportForm<TableNameTextFileSubDataExportForm>
	{
		private static final long serialVersionUID = 1L;

		public JsonFileBatchDataExportForm()
		{
			super();
		}

		@Override
		public JsonDataExportOption getExportOption()
		{
			return (JsonDataExportOption)super.getExportOption();
		}

		public void setExportOption(JsonDataExportOption exportOption)
		{
			super.setExportOption(exportOption);
		}

		@Override
		protected void checkSubDataExchange(TableNameTextFileSubDataExportForm subDataExchange) throws IllegalInputException
		{
			super.checkSubDataExchange(subDataExchange);

			if(JsonDataFormat.TABLE_OBJECT.equals(getExportOption().getJsonDataFormat())
					&& StringUtil.isEmpty(subDataExchange.getTableName()))
				throw new IllegalInputException();
		}

		@Override
		public TableNameTextFileSubDataExportForm createTextFileSubDataExportForm()
		{
			return new TableNameTextFileSubDataExportForm();
		}
	}
	
	public static class CancelDataExchangeForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;
		
		private String dataExchangeId;

		private List<String> subDataExchangeIds = Collections.emptyList();

		public CancelDataExchangeForm()
		{
			super();
		}

		public String getDataExchangeId()
		{
			return dataExchangeId;
		}

		public void setDataExchangeId(String dataExchangeId)
		{
			this.dataExchangeId = dataExchangeId;
		}

		public List<String> getSubDataExchangeIds()
		{
			return subDataExchangeIds;
		}

		public void setSubDataExchangeIds(List<String> subDataExchangeIds)
		{
			this.subDataExchangeIds = subDataExchangeIds;
		}
	}

	protected static class BatchDataExchangeInfo implements ExpiredSessionAttr, Serializable
	{
		private static final long serialVersionUID = 1L;

		private String dataExchangeId;

		/**
		 * 此对象包含无法序列化的信息，应添加transient修饰符
		 */
		private transient BatchDataExchange batchDataExchange;

		private volatile long lastAccessTime = 0;

		public BatchDataExchangeInfo()
		{
			super();
			this.lastAccessTime = System.currentTimeMillis();
		}

		public BatchDataExchangeInfo(String dataExchangeId, BatchDataExchange batchDataExchange)
		{
			super();
			this.dataExchangeId = dataExchangeId;
			this.batchDataExchange = batchDataExchange;
			this.lastAccessTime = System.currentTimeMillis();
		}

		public String getDataExchangeId()
		{
			return dataExchangeId;
		}

		public void setDataExchangeId(String dataExchangeId)
		{
			this.dataExchangeId = dataExchangeId;
		}

		public BatchDataExchange getBatchDataExchange()
		{
			return batchDataExchange;
		}

		public void setBatchDataExchange(BatchDataExchange batchDataExchange)
		{
			this.batchDataExchange = batchDataExchange;
		}

		@Override
		public long getLastAccessTime()
		{
			return lastAccessTime;
		}

		@Override
		public void setLastAccessTime(long lastAccessTime)
		{
			this.lastAccessTime = lastAccessTime;
		}
	}
}
