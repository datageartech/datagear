/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.support.DataSetSourceParseException;
import org.datagear.analysis.support.SqlDataSetSqlExecutionException;
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
import org.datagear.persistence.support.UnsupportedDialectException;
import org.datagear.web.OperationMessage;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
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
		setOperationMessageForThrowable(request, buildMessageCode(MissingServletRequestParameterException.class),
				exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerBindException(HttpServletRequest request, HttpServletResponse response,
			BindException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(BindException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMethodArgumentNotValidException(HttpServletRequest request,
			HttpServletResponse response, MethodArgumentNotValidException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(MethodArgumentNotValidException.class), exception,
				false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerHttpMessageNotReadableException(HttpServletRequest request,
			HttpServletResponse response, HttpMessageNotReadableException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(HttpMessageNotReadableException.class), exception,
				false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(IllegalInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalInputException(HttpServletRequest request, HttpServletResponse response,
			IllegalInputException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(IllegalInputException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleControllerIllegalArgumentException(HttpServletRequest request, HttpServletResponse response,
			IllegalArgumentException exception)
	{
		setOperationMessageForInternalServerError(request, buildMessageCode(IllegalArgumentException.class), exception);

		return getErrorView(request, response);
	}

	@ExceptionHandler(RecordNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerRecordNotFoundException(HttpServletRequest request, HttpServletResponse response,
			RecordNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(RecordNotFoundException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerSchemaNotFoundException(HttpServletRequest request, HttpServletResponse response,
			SchemaNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SchemaNotFoundException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(ConversionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerConversionException(HttpServletRequest request, HttpServletResponse response,
			ConversionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(ConversionException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerFileNotFoundException(HttpServletRequest request, HttpServletResponse response,
			FileNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(FileNotFoundException.class), exception, false,
				exception.getFileName());

		return getErrorView(request, response);
	}

	@ExceptionHandler(DuplicateRecordException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerDuplicateRecordException(HttpServletRequest request, HttpServletResponse response,
			DuplicateRecordException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DuplicateRecordException.class), exception, false,
				exception.getExpectedCount(), exception.getActualCount());

		return getErrorView(request, response);
	}

	@ExceptionHandler(UserSQLException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerUserSQLException(HttpServletRequest request, HttpServletResponse response,
			UserSQLException exception)
	{
		String message = (exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
		setOperationMessageForThrowable(request, buildMessageCode(UserSQLException.class), exception, false, message);

		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueVariableExpressionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceSqlParamValueVariableExpressionException(HttpServletRequest request,
			HttpServletResponse response, SqlParamValueVariableExpressionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlParamValueVariableExpressionException.class),
				exception.getCause(), false, exception.getExpression());

		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueSqlExpressionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceSqlParamValueSqlExpressionException(HttpServletRequest request,
			HttpServletResponse response, SqlParamValueSqlExpressionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlParamValueSqlExpressionException.class),
				exception.getCause(), true, exception.getExpression());

		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlParamValueMapperException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNonUniqueResultException(HttpServletRequest request, HttpServletResponse response,
			SqlParamValueMapperException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlParamValueMapperException.class), exception, true,
				exception.getColumn().getName());

		return getErrorView(request, response);
	}

	@ExceptionHandler(NonUniqueResultException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNonUniqueResultException(HttpServletRequest request, HttpServletResponse response,
			NonUniqueResultException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(NonUniqueResultException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(NoColumnDefinedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNoColumnDefinedException(HttpServletRequest request, HttpServletResponse response,
			NoColumnDefinedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(NoColumnDefinedException.class), exception, false,
				exception.getTableName());

		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedDialectException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceUnsupportedDialectException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedDialectException exception)
	{
		setOperationMessageForInternalServerError(request, buildMessageCode(UnsupportedDialectException.class),
				exception);

		return getErrorView(request, response);
	}

	@ExceptionHandler(PersistenceException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistencePersistenceException(HttpServletRequest request, HttpServletResponse response,
			PersistenceException exception)
	{
		if (exception.getCause() instanceof SQLException)
			setOperationMessageForThrowable(request, buildMessageCode(PersistenceException.class), exception.getCause(),
					true);
		else
			setOperationMessageForInternalServerError(request, buildMessageCode(PersistenceException.class), exception);

		return getErrorView(request, response);
	}

	@ExceptionHandler(TableNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleMetaTableNotFoundException(HttpServletRequest request, HttpServletResponse response,
			TableNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(TableNotFoundException.class), exception, false,
				exception.getTableName());

		return getErrorView(request, response);
	}

	@ExceptionHandler(DBMetaResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleMetaDBMetaResolverException(HttpServletRequest request, HttpServletResponse response,
			DBMetaResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DBMetaResolverException.class), exception, true);

		return getErrorView(request, response);
	}

	@ExceptionHandler(ConnectionSourceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionConnectionSourceException(HttpServletRequest request, HttpServletResponse response,
			ConnectionSourceException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(ConnectionSourceException.class), exception, true);

		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverEntityManagerException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverEntityManagerException(HttpServletRequest request, HttpServletResponse response,
			DriverEntityManagerException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverEntityManagerException.class), exception, true,
				exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(PathDriverFactoryException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionPathDriverFactoryException(HttpServletRequest request, HttpServletResponse response,
			PathDriverFactoryException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(PathDriverFactoryException.class), exception, true,
				exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DriverNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverNotFoundException.class), exception, false,
				exception.getDriverClassName());

		return getErrorView(request, response);
	}

	@ExceptionHandler(DriverClassFormatErrorException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverClassFormatErrorException(HttpServletRequest request,
			HttpServletResponse response, DriverClassFormatErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverClassFormatErrorException.class), exception,
				false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(URLNotAcceptedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionURLNotAcceptedException(HttpServletRequest request, HttpServletResponse response,
			URLNotAcceptedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(URLNotAcceptedException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedGetConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionUnsupportedGetConnectionException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedGetConnectionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedGetConnectionException.class), exception,
				false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(EstablishConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionEstablishConnectionException(HttpServletRequest request, HttpServletResponse response,
			EstablishConnectionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(EstablishConnectionException.class),
				exception.getCause(), true);

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

		setOperationMessageForThrowable(request, buildMessageCode(SqlDataSetUnsupportedSqlTypeException.class),
				exception, false, msgArg);
		return getErrorView(request, response);
	}

	@ExceptionHandler(TemplateResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisTemplateResolverException(HttpServletRequest request, HttpServletResponse response,
			TemplateResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(TemplateResolverException.class), exception, false,
				exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedResultDataException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisUnsupportedResultDataException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedResultDataException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedResultDataException.class), exception,
				false, exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedJsonResultDataException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisUnsupportedJsonResultDataException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedJsonResultDataException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedJsonResultDataException.class), exception,
				false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(DataSetSourceParseException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisDataSetSourceParseException(HttpServletRequest request, HttpServletResponse response,
			DataSetSourceParseException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DataSetSourceParseException.class), exception, false,
				exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlDataSetSqlExecutionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAnalysisSqlDataSetSqlExecutionException(HttpServletRequest request,
			HttpServletResponse response, SqlDataSetSqlExecutionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlDataSetSqlExecutionException.class), exception,
				false, exception.getMessage());

		return getErrorView(request, response);
	}

	@ExceptionHandler(SaveSchemaUrlPermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleServicePermissionDeniedException(HttpServletRequest request, HttpServletResponse response,
			SaveSchemaUrlPermissionDeniedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SaveSchemaUrlPermissionDeniedException.class),
				exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(PermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleServicePermissionDeniedException(HttpServletRequest request, HttpServletResponse response,
			PermissionDeniedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(PermissionDeniedException.class), exception, false);

		return getErrorView(request, response);
	}

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleThrowable(HttpServletRequest request, HttpServletResponse response, Throwable t)
	{
		setOperationMessageForInternalServerError(request, buildMessageCode(Throwable.class), t);

		return getErrorView(request, response);
	}

	protected void setOperationMessageForInternalServerError(HttpServletRequest request, String messageCode,
			Throwable t)
	{
		setOperationMessageForThrowable(request, messageCode, t, false, t.getMessage());
		LOGGER.error("", t);
	}

	/**
	 * 获取错误信息视图。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected String getErrorView(HttpServletRequest request, HttpServletResponse response)
	{
		setAttributeIfIsJsonResponse(request, response);
		return ERROR_PAGE_URL;
	}

	/**
	 * 设置JSON响应的错误页面属性。
	 * 
	 * @param request
	 * @param response
	 */
	protected void setAttributeIfIsJsonResponse(HttpServletRequest request, HttpServletResponse response)
	{
		String expectedContentType = DeliverContentTypeExceptionHandlerExceptionResolver.getHandlerContentType(request);
		if (expectedContentType != null && !expectedContentType.isEmpty())
			response.setContentType(expectedContentType);

		boolean isJsonResponse = WebUtils.isJsonResponse(response);

		request.setAttribute("isJsonResponse", isJsonResponse);

		if (isJsonResponse)
		{
			OperationMessage operationMessage = getOperationMessageForHttpError(request, response);

			request.setAttribute(WebUtils.KEY_OPERATION_MESSAGE,
					WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(operationMessage));

			response.setContentType(CONTENT_TYPE_JSON);
		}
	}

	protected String buildMessageCode(Class<? extends Throwable> clazz)
	{
		return buildMessageCode(clazz.getSimpleName());
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("error", code);
	}
}
