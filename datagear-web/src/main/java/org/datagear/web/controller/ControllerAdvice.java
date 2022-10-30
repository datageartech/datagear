/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.support.DataSetSourceParseException;
import org.datagear.analysis.support.DataValueConvertionException;
import org.datagear.analysis.support.HeaderContentNotNameValueObjArrayJsonException;
import org.datagear.analysis.support.ReadJsonDataPathException;
import org.datagear.analysis.support.RequestContentNotNameValueObjArrayJsonException;
import org.datagear.analysis.support.SqlDataSetConnectionException;
import org.datagear.analysis.support.SqlDataSetSqlExecutionException;
import org.datagear.analysis.support.SqlDataSetSqlValidationException;
import org.datagear.analysis.support.SqlDataSetUnsupportedSqlTypeException;
import org.datagear.analysis.support.TemplateResolverException;
import org.datagear.analysis.support.UnsupportedJsonResultDataException;
import org.datagear.analysis.support.UnsupportedResultDataException;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverClassFormatErrorException;
import org.datagear.connection.DriverEntityManagerException;
import org.datagear.connection.DriverNotFoundException;
import org.datagear.connection.EstablishConnectionException;
import org.datagear.connection.PathDriverFactoryException;
import org.datagear.connection.URLNotAcceptedException;
import org.datagear.connection.UnsupportedGetConnectionException;
import org.datagear.management.service.DeleteBuiltinRoleDeniedException;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.impl.SaveSchemaUrlPermissionDeniedException;
import org.datagear.meta.resolver.DBMetaResolverException;
import org.datagear.meta.resolver.TableNotFoundException;
import org.datagear.persistence.NonUniqueResultException;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.persistence.support.NoColumnDefinedException;
import org.datagear.persistence.support.SqlParamValueSqlExpressionException;
import org.datagear.persistence.support.SqlParamValueVariableExpressionException;
import org.datagear.persistence.support.SqlValidationException;
import org.datagear.persistence.support.UnsupportedDialectException;
import org.datagear.util.MalformedZipException;
import org.datagear.web.util.OperationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * mvc控制器Advice。
 * 
 * @author datagear@163.com
 *
 */
@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAdvice.class);

	public ControllerAdvice()
	{
		super();
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMissingServletRequestParameterException(HttpServletRequest request,
			HttpServletResponse response, MissingServletRequestParameterException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerBindException(HttpServletRequest request, HttpServletResponse response,
			BindException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMethodArgumentNotValidException(HttpServletRequest request,
			HttpServletResponse response, MethodArgumentNotValidException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerHttpMessageNotReadableException(HttpServletRequest request,
			HttpServletResponse response, HttpMessageNotReadableException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(IllegalInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalInputException(HttpServletRequest request, HttpServletResponse response,
			IllegalInputException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleControllerIllegalArgumentException(HttpServletRequest request, HttpServletResponse response,
			IllegalArgumentException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(RecordNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerRecordNotFoundException(HttpServletRequest request, HttpServletResponse response,
			RecordNotFoundException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerSchemaNotFoundException(HttpServletRequest request, HttpServletResponse response,
			SchemaNotFoundException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(ConversionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerConversionException(HttpServletRequest request, HttpServletResponse response,
			ConversionException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerFileNotFoundException(HttpServletRequest request, HttpServletResponse response,
			FileNotFoundException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getFileName());
		return getErrorView(request, response);
	}

	@ExceptionHandler(java.io.FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerJavaIoFileNotFoundException(HttpServletRequest request, HttpServletResponse response,
			java.io.FileNotFoundException exception)
	{
		setOptMsgForThrowableMsgCode(request, exception, buildExceptionMsgCode(java.io.FileNotFoundException.class, true));
		return getErrorView(request, response);
	}

	@ExceptionHandler(DuplicateRecordException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerDuplicateRecordException(HttpServletRequest request, HttpServletResponse response,
			DuplicateRecordException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getExpectedCount(), exception.getActualCount());
		return getErrorView(request, response);
	}

	@ExceptionHandler(UserSQLException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerUserSQLException(HttpServletRequest request, HttpServletResponse response,
			UserSQLException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(IllegalImportDriverEntityFileFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalImportDriverEntityFileFormatException(HttpServletRequest request,
			HttpServletResponse response, IllegalImportDriverEntityFileFormatException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DataSetResDirectoryNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDataSetResDirectoryNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DataSetResDirectoryNotFoundException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getDirectory());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceSqlValidationException(HttpServletRequest request,
			HttpServletResponse response, SqlValidationException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getSqlValidation().getInvalidValue());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueVariableExpressionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceSqlParamValueVariableExpressionException(HttpServletRequest request,
			HttpServletResponse response, SqlParamValueVariableExpressionException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getExpression());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueSqlExpressionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceSqlParamValueSqlExpressionException(HttpServletRequest request,
			HttpServletResponse response, SqlParamValueSqlExpressionException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getExpression());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueMapperException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNonUniqueResultException(HttpServletRequest request, HttpServletResponse response,
			SqlParamValueMapperException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getColumn().getName());
		return getErrorView(request, response);
	}

	@ExceptionHandler(NonUniqueResultException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNonUniqueResultException(HttpServletRequest request, HttpServletResponse response,
			NonUniqueResultException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(NoColumnDefinedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNoColumnDefinedException(HttpServletRequest request, HttpServletResponse response,
			NoColumnDefinedException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getTableName());
		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedDialectException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceUnsupportedDialectException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedDialectException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(PersistenceException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistencePersistenceException(HttpServletRequest request, HttpServletResponse response,
			PersistenceException exception)
	{
		if (exception.getCause() instanceof SQLException)
			setOptMsgForThrowableMsgCodeLog(request, exception, buildExceptionMsgCode(SQLException.class));
		else
			setOptMsgForThrowableLog(request, exception);

		return getErrorView(request, response);
	}

	@ExceptionHandler(TableNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleMetaTableNotFoundException(HttpServletRequest request, HttpServletResponse response,
			TableNotFoundException exception)
	{
		setOptMsgForThrowable(request, exception, exception.getTableName());
		return getErrorView(request, response);
	}

	@ExceptionHandler(DBMetaResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleMetaDBMetaResolverException(HttpServletRequest request, HttpServletResponse response,
			DBMetaResolverException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(ConnectionSourceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionConnectionSourceException(HttpServletRequest request, HttpServletResponse response,
			ConnectionSourceException exception)
	{
		setOptMsgForThrowableLog(request, exception, true);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverEntityManagerException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverEntityManagerException(HttpServletRequest request, HttpServletResponse response,
			DriverEntityManagerException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(PathDriverFactoryException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionPathDriverFactoryException(HttpServletRequest request, HttpServletResponse response,
			PathDriverFactoryException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DriverNotFoundException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverClassFormatErrorException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverClassFormatErrorException(HttpServletRequest request,
			HttpServletResponse response, DriverClassFormatErrorException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(URLNotAcceptedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionURLNotAcceptedException(HttpServletRequest request, HttpServletResponse response,
			URLNotAcceptedException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedGetConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionUnsupportedGetConnectionException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedGetConnectionException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(EstablishConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionEstablishConnectionException(HttpServletRequest request, HttpServletResponse response,
			EstablishConnectionException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlDataSetUnsupportedSqlTypeException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisSqlDataSetUnsupportedSqlTypeException(HttpServletRequest request,
			HttpServletResponse response, SqlDataSetUnsupportedSqlTypeException exception)
	{
		String msgArg = exception.getSqlType().getTypeName();
		if (exception.hasColumnName())
			msgArg = msgArg + " (" + exception.getColumnName() + ")";

		setOptMsgForThrowable(request, exception, msgArg);
		return getErrorView(request, response);
	}

	@ExceptionHandler(TemplateResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisTemplateResolverException(HttpServletRequest request, HttpServletResponse response,
			TemplateResolverException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedResultDataException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisUnsupportedResultDataException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedResultDataException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedJsonResultDataException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisUnsupportedJsonResultDataException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedJsonResultDataException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(ReadJsonDataPathException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisReadJsonDataPathException(HttpServletRequest request, HttpServletResponse response,
			ReadJsonDataPathException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DataSetSourceParseException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisDataSetSourceParseException(HttpServletRequest request, HttpServletResponse response,
			DataSetSourceParseException exception)
	{
		OperationMessage om = setOptMsgForThrowable(request, exception, getRootMessage(exception), exception.getSource());
		om.setData(exception.getSource());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlDataSetSqlExecutionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisSqlDataSetSqlExecutionException(HttpServletRequest request,
			HttpServletResponse response, SqlDataSetSqlExecutionException exception)
	{
		OperationMessage om = setOptMsgForThrowable(request, exception, getRootMessage(exception));
		om.setData(exception.getSql());
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlDataSetConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisSqlDataSetConnectionException(HttpServletRequest request, HttpServletResponse response,
			SqlDataSetConnectionException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlDataSetSqlValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisSqlDataSetSqlValidationException(HttpServletRequest request,
			HttpServletResponse response, SqlDataSetSqlValidationException exception)
	{
		OperationMessage om = setOptMsgForThrowable(request, exception, exception.getSqlValidation().getInvalidValue());
		om.setData(exception.getSql());
		return getErrorView(request, response);
	}

	@ExceptionHandler(RequestContentNotNameValueObjArrayJsonException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisRequestContentNotNameValueObjArrayJsonException(HttpServletRequest request,
			HttpServletResponse response, RequestContentNotNameValueObjArrayJsonException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(HeaderContentNotNameValueObjArrayJsonException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisHeaderContentNotNameValueObjArrayJsonException(HttpServletRequest request,
			HttpServletResponse response, HeaderContentNotNameValueObjArrayJsonException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DataSetException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisDataSetException(HttpServletRequest request, HttpServletResponse response,
			DataSetException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}
	
	@ExceptionHandler(DataValueConvertionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisDataValueConvertionException(HttpServletRequest request, HttpServletResponse response,
			DataValueConvertionException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(SaveSchemaUrlPermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleServiceSaveSchemaUrlPermissionDeniedException(HttpServletRequest request,
			HttpServletResponse response, SaveSchemaUrlPermissionDeniedException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(PermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleServicePermissionDeniedException(HttpServletRequest request, HttpServletResponse response,
			PermissionDeniedException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DeleteBuiltinRoleDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleServiceDeleteBuiltinRoleDeniedException(HttpServletRequest request,
			HttpServletResponse response, DeleteBuiltinRoleDeniedException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(MalformedZipException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleMalformedZipException(HttpServletRequest request, HttpServletResponse response,
			MalformedZipException exception)
	{
		setOptMsgForThrowable(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDataIntegrityViolationException(HttpServletRequest request, HttpServletResponse response,
			DataIntegrityViolationException exception)
	{
		setOptMsgForThrowableLog(request, exception);
		return getErrorView(request, response);
	}

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable t)
	{
		setOptMsgForThrowableMsgCodeLog(request, t, buildExceptionMsgCode(Throwable.class));
		return getErrorView(request, response);
	}
	
	protected OperationMessage setOptMsgForThrowable(HttpServletRequest request, Throwable t, Object... msgArgs)
	{
		OperationMessage om = super.setOptMsgForThrowable(request, t, msgArgs);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Operation error: ", t);
		
		return om;
	}
	
	protected OperationMessage setOptMsgForThrowableLog(HttpServletRequest request, Throwable t, Object... msgArgs)
	{
		OperationMessage om = super.setOptMsgForThrowable(request, t, msgArgs);
		
		LOGGER.error("Operation error: ", t);
		
		return om;
	}
	
	protected OperationMessage setOptMsgForThrowableMsgCode(HttpServletRequest request, Throwable t, String msgCode, Object... msgArgs)
	{
		OperationMessage om = super.setOptMsgForThrowableMsgCode(request, t, msgCode, msgArgs);
		
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Operation error: ", t);
		
		return om;
	}
	
	protected OperationMessage setOptMsgForThrowableMsgCodeLog(HttpServletRequest request, Throwable t, String msgCode, Object... msgArgs)
	{
		OperationMessage om = super.setOptMsgForThrowableMsgCode(request, t, msgCode, msgArgs);
		
		LOGGER.error("Operation error: ", t);
		
		return om;
	}
}
