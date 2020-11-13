/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SqlHistory;
import org.datagear.management.domain.User;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.support.DefaultLOBRowMapper;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.persistence.support.SqlSelectResult;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.util.StringUtil;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.sqlpad.SqlpadExecutionService.CommitMode;
import org.datagear.web.sqlpad.SqlpadExecutionService.ExceptionHandleMode;
import org.datagear.web.sqlpad.SqlpadExecutionService.SqlCommand;
import org.datagear.web.sqlpad.SqlpadExecutionSubmit;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * SQL工作台控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/sqlpad")
public class SqlpadController extends AbstractSchemaConnController
{
	public static final int DEFAULT_SQL_RESULTSET_FETCH_SIZE = 20;

	@Autowired
	private SqlSelectManager sqlSelectManager;

	@Autowired
	private SqlpadExecutionService sqlpadExecutionService;

	@Autowired
	private SqlHistoryService sqlHistoryService;

	@Autowired
	private File tempDirectory;

	private int sqlResultReadActualLobRows = 3;

	public SqlpadController()
	{
		super();
	}

	public SqlSelectManager getSqlSelectManager()
	{
		return sqlSelectManager;
	}

	public void setSqlSelectManager(SqlSelectManager sqlSelectManager)
	{
		this.sqlSelectManager = sqlSelectManager;
	}

	public SqlpadExecutionService getSqlpadExecutionService()
	{
		return sqlpadExecutionService;
	}

	public void setSqlpadExecutionService(SqlpadExecutionService sqlpadExecutionService)
	{
		this.sqlpadExecutionService = sqlpadExecutionService;
	}

	public SqlHistoryService getSqlHistoryService()
	{
		return sqlHistoryService;
	}

	public void setSqlHistoryService(SqlHistoryService sqlHistoryService)
	{
		this.sqlHistoryService = sqlHistoryService;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public int getSqlResultReadActualLobRows()
	{
		return sqlResultReadActualLobRows;
	}

	public void setSqlResultReadActualLobRows(int sqlResultReadActualLobRows)
	{
		this.sqlResultReadActualLobRows = sqlResultReadActualLobRows;
	}

	@RequestMapping("/{schemaId}")
	public String index(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);
			}
		}.execute();

		String initSql = request.getParameter("initSql");
		if (initSql == null)
			initSql = "";

		String sqlpadId = generateSqlpadId(request, response);

		springModel.addAttribute("sqlpadId", sqlpadId);
		springModel.addAttribute("sqlResultRowMapper", buildDefaultLOBRowMapper());
		springModel.addAttribute("initSql", initSql);

		return "/sqlpad/sqlpad";
	}

	@RequestMapping(value = "/{schemaId}/execute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> executeSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("sql") String sql,
			@RequestParam(value = "sqlDelimiter", required = false) String sqlDelimiter,
			@RequestParam(value = "sqlStartRow", required = false) Integer sqlStartRow,
			@RequestParam(value = "sqlStartColumn", required = false) Integer sqlStartColumn,
			@RequestParam(value = "commitMode", required = false) CommitMode commitMode,
			@RequestParam(value = "exceptionHandleMode", required = false) ExceptionHandleMode exceptionHandleMode,
			@RequestParam(value = "overTimeThreashold", required = false) Integer overTimeThreashold,
			@RequestParam(value = "resultsetFetchSize", required = false) Integer resultsetFetchSize) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		Schema schema = getSchemaForUserNotNull(user, schemaId);

		checkReadTableDataPermission(schema, user);

		SqlScriptParser sqlScriptParser = new SqlScriptParser(new StringReader(sql));
		if (!isEmpty(sqlDelimiter))
			sqlScriptParser.setDelimiter(sqlDelimiter);
		if (sqlStartRow != null)
			sqlScriptParser.setContextStartRow(sqlStartRow);
		if (sqlStartColumn != null)
			sqlScriptParser.setContextStartColumn(sqlStartColumn);

		if (commitMode == null)
			commitMode = CommitMode.AUTO;

		if (exceptionHandleMode == null)
			exceptionHandleMode = ExceptionHandleMode.ABORT;

		if (resultsetFetchSize == null)
			resultsetFetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;

		List<SqlStatement> sqlStatements = sqlScriptParser.parseAll();

		SqlpadExecutionSubmit submit = new SqlpadExecutionSubmit(user, schema, sqlpadId,
				FileUtil.getDirectory(getSqlpadTmpDirectory(), sqlpadId), sqlStatements, commitMode,
				exceptionHandleMode, overTimeThreashold, resultsetFetchSize, buildDefaultLOBRowMapper(),
				WebUtils.getLocale(request));

		this.sqlpadExecutionService.submit(submit);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "/{schemaId}/command", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> command(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("command") SqlCommand sqlCommand) throws Throwable
	{
		this.sqlpadExecutionService.command(sqlpadId, sqlCommand);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "/{schemaId}/message", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Object> message(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId,
			@RequestParam(value = "messageCount", required = false) Integer messageCount) throws Throwable
	{
		if (messageCount == null)
			messageCount = 50;
		if (messageCount < 1)
			messageCount = 1;

		return this.sqlpadExecutionService.message(sqlpadId, messageCount);
	}

	@RequestMapping(value = "/{schemaId}/select", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public SqlSelectResult select(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("sql") final String sql,
			@RequestParam(value = "startRow", required = false) Integer startRow,
			@RequestParam(value = "fetchSize", required = false) Integer fetchSize,
			@RequestParam(value = "returnMeta", required = false) Boolean returnMeta) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		if (startRow == null)
			startRow = 1;
		if (fetchSize == null)
			fetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;
		if (returnMeta == null)
			returnMeta = false;

		if (fetchSize < 1)
			fetchSize = 1;
		if (fetchSize > 1000)
			fetchSize = 1000;

		final int startRowFinal = startRow;
		final int fetchSizeFinal = fetchSize;

		SqlSelectResult result = new ReturnSchemaConnExecutor<SqlSelectResult>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected SqlSelectResult execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				SqlSelectResult result = getSqlSelectManager().select(getConnection(), sql, startRowFinal,
						fetchSizeFinal, buildDefaultLOBRowMapper());

				return result;
			}
		}.execute();

		if (!Boolean.TRUE.equals(returnMeta))
			result.setTable(null);

		return result;
	}

	@RequestMapping("/{schemaId}/downloadResultField")
	public void downloadResultField(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("value") String value) throws Throwable
	{
		File blobFile = buildDefaultLOBRowMapper().getBlobFile(value);

		if (!blobFile.exists())
			throw new FileNotFoundException(value);

		response.setCharacterEncoding(IOUtil.CHARSET_UTF_8);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, "BLOB"));

		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = new FileInputStream(blobFile);

			out = response.getOutputStream();

			byte[] buffer = new byte[1024];

			int readLen = 0;
			while ((readLen = in.read(buffer)) > 0)
				out.write(buffer, 0, readLen);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/{schemaId}/sqlHistoryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SqlHistory> pagingQueryTable(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			PagingQuery pagingQueryParam) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		return this.sqlHistoryService.pagingQueryByUserId(schemaId, user.getId(), pagingQuery);
	}

	@RequestMapping(value = "/{schemaId}/uploadInsertFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo fileUpload(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("file") MultipartFile multipartFile)
			throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		Schema schema = getSchemaForUserNotNull(user, schemaId);

		checkDeleteTableDataPermission(schema, user);

		SqlpadFileDirectory directory = SqlpadFileDirectory.valueOf(getSqlpadTmpDirectory(), sqlpadId);
		File file = directory.createFileFor(multipartFile.getOriginalFilename());
		multipartFile.transferTo(file);

		FileInfo fileInfo = new FileInfo(file.getName(), file.length());

		return fileInfo;
	}

	protected DefaultLOBRowMapper buildDefaultLOBRowMapper()
	{
		DefaultLOBRowMapper rowMapper = new DefaultLOBRowMapper();
		rowMapper.setReadActualClobRows(this.sqlResultReadActualLobRows);
		rowMapper.setReadActualBinaryRows(this.sqlResultReadActualLobRows);
		rowMapper.setBinaryEncoder(DefaultLOBRowMapper.BINARY_ENCODER_HEX);
		rowMapper.setBinaryDirectory(getSqlpadBinaryTmpDirectory());

		return rowMapper;
	}

	protected File getSqlpadTmpDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "sqlpad", true);
	}

	protected File getSqlpadBinaryTmpDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "sqlpadbinary", true);
	}

	protected String generateSqlpadId(HttpServletRequest request, HttpServletResponse response)
	{
		return IDUtil.uuid();
	}

	/**
	 * SQL工作台文件目录。
	 * <p>
	 * 此类非线程安全。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SqlpadFileDirectory
	{
		public static final String NAME_PREFIX = "SQLPAD";

		private File directory;

		private Map<String, String> _absolutePathMap = null;

		public SqlpadFileDirectory(File directory)
		{
			super();
			this.directory = directory;
		}

		public File getDirectory()
		{
			return directory;
		}

		public void setDirectory(File directory)
		{
			this.directory = directory;
		}

		/**
		 * 将字符串中的文件名替换为绝对路径。
		 * 
		 * @param str
		 * @return
		 */
		public String replaceNameToAbsolutePath(String str)
		{
			Map<String, String> absolutePathMap = getAbsolutePathMap();

			if (absolutePathMap == null || absolutePathMap.isEmpty())
				return str;

			String re = str;
			int index = 0;

			while ((index = re.indexOf(NAME_PREFIX, index)) >= 0)
			{
				int length = re.length();
				int myStart = index + NAME_PREFIX.length();
				int nextIndex = myStart;

				if (myStart >= length)
					break;

				char tailLenLenChar = re.substring(myStart, myStart + 1).charAt(0);

				if (tailLenLenChar > '1' && tailLenLenChar < '9')
				{
					int tailLenLen = Integer.parseInt(Character.toString(tailLenLenChar));
					myStart += 1;

					if (myStart + tailLenLen <= length)
					{
						String tailLenStr = re.substring(myStart, myStart + tailLenLen);

						int fileNameEndIdx = -1;

						try
						{
							fileNameEndIdx = myStart + tailLenLen + Integer.parseInt(tailLenStr);
						}
						catch (Throwable t)
						{
						}

						if (fileNameEndIdx > -1)
						{
							if (fileNameEndIdx <= length)
							{
								String fileName = re.substring(index, fileNameEndIdx);
								String absolutePath = absolutePathMap.get(fileName);

								if (!StringUtil.isEmpty(absolutePath))
								{
									StringBuilder sb = new StringBuilder();

									if (index > 0)
										sb.append(re.substring(0, index));

									sb.append(absolutePath);

									nextIndex = sb.length();

									if (fileNameEndIdx < length)
										sb.append(re.substring(fileNameEndIdx));

									re = sb.toString();
								}
							}
						}
					}
				}

				index = nextIndex;
			}

			return re;
		}

		public Map<String, String> getAbsolutePathMap()
		{
			if (this._absolutePathMap == null)
			{
				this._absolutePathMap = new HashMap<>();

				File[] children = this.directory.listFiles();

				for (File child : children)
				{
					if (child.isDirectory())
						continue;

					String key = child.getName();
					String value = child.getAbsolutePath();

					this._absolutePathMap.put(key, value);
				}
			}

			return this._absolutePathMap;
		}

		/**
		 * 是否包含指定名称的文件。
		 * 
		 * @param name
		 * @return
		 */
		public boolean containsFile(String name)
		{
			File[] children = this.directory.listFiles();

			for (File child : children)
			{
				if (child.getName().equals(name))
					return true;
			}

			return false;
		}

		/**
		 * 获取指定名称文件的绝对路径。
		 * <p>
		 * 如果文件不存在，将返回{@code null}。
		 * </p>
		 * 
		 * @param name
		 * @return
		 */
		public String getAbsolutePath(String name)
		{
			File[] children = this.directory.listFiles();

			for (File child : children)
			{
				if (child.getName().equals(name))
					return child.getAbsolutePath();
			}

			return null;
		}

		public File createFileFor(String rawFileName)
		{
			if (this._absolutePathMap != null)
				this._absolutePathMap = null;

			String ext = FileUtil.getExtension(rawFileName);

			String tail = IDUtil.uuid();

			if (!StringUtil.isEmpty(ext))
				tail = tail + "." + ext;

			String tailLen = Integer.toString(tail.length());

			// NAME_PREFIX<一个数字表明后面几个字符是尾段长度><尾段长度><尾段>
			String fileName = NAME_PREFIX + tailLen.length() + tailLen + tail;

			return FileUtil.getFile(this.directory, fileName);
		}

		public static SqlpadFileDirectory valueOf(File directory)
		{
			return new SqlpadFileDirectory(directory);
		}

		public static SqlpadFileDirectory valueOf(File parent, String sqlpadId)
		{
			File directory = FileUtil.getDirectory(parent, sqlpadId);
			return new SqlpadFileDirectory(directory);
		}
	}
}
