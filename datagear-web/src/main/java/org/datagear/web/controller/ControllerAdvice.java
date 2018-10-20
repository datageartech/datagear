/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.mail.AuthenticationFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverClassFormatErrorException;
import org.datagear.connection.DriverEntityManagerException;
import org.datagear.connection.DriverNotFoundException;
import org.datagear.connection.EstablishConnectionException;
import org.datagear.connection.URLNotAcceptedException;
import org.datagear.connection.UnsupportedGetConnectionException;
import org.datagear.dbinfo.DatabaseInfoResolverException;
import org.datagear.dbinfo.TableNotExistsException;
import org.datagear.dbmodel.DatabaseModelResolverException;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.UnsupportedDialectException;
import org.datagear.persistence.support.SqlExpressionErrorException;
import org.datagear.web.convert.IllegalSourceValueException;
import org.springframework.http.HttpStatus;
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
	public ControllerAdvice()
	{
		super();
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMissingServletRequestParameterException(HttpServletRequest request,
			HttpServletResponse response, MissingServletRequestParameterException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerBindException(HttpServletRequest request, HttpServletResponse response,
			BindException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMethodArgumentNotValidException(HttpServletRequest request,
			HttpServletResponse response, MethodArgumentNotValidException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAuthenticationFailedException(HttpServletRequest request, HttpServletResponse response,
			AuthenticationFailedException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(IllegalInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalInputException(HttpServletRequest request, HttpServletResponse response,
			IllegalInputException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(RecordNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerRecordNotFoundException(HttpServletRequest request, HttpServletResponse response,
			RecordNotFoundException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(RecordNotFoundOrPermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerRecordNotFoundOrNoPermissionException(HttpServletRequest request,
			HttpServletResponse response, RecordNotFoundOrPermissionDeniedException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerSchemaNotFoundException(HttpServletRequest request, HttpServletResponse response,
			SchemaNotFoundException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(IllegalSourceValueException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalSourceValueException(HttpServletRequest request, HttpServletResponse response,
			IllegalSourceValueException exception)
	{
		setOperationMessageForException(request, exception, false, exception.getSourceValue(),
				exception.getTargetType().getName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerFileNotFoundException(HttpServletRequest request, HttpServletResponse response,
			FileNotFoundException exception)
	{
		setOperationMessageForException(request, exception, false, exception.getFileName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(SqlExpressionErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceSqlExpressionErrorException(HttpServletRequest request, HttpServletResponse response,
			SqlExpressionErrorException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(UnsupportedDialectException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceUnsupportedDialectException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedDialectException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(PersistenceException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistencePersistenceException(HttpServletRequest request, HttpServletResponse response,
			PersistenceException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DatabaseInfoResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseInfoResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseInfoResolverException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DatabaseModelResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseModelResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseModelResolverException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(TableNotExistsException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleDbmodelTableNotExistsException(HttpServletRequest request, HttpServletResponse response,
			TableNotExistsException exception)
	{
		setOperationMessageForException(request, exception, false, exception.getTableName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(ConnectionSourceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionConnectionSourceException(HttpServletRequest request, HttpServletResponse response,
			ConnectionSourceException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverEntityManagerException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverEntityManagerException(HttpServletRequest request, HttpServletResponse response,
			DriverEntityManagerException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DriverNotFoundException exception)
	{
		setOperationMessageForException(request, exception, false, exception.getDriverClassName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverClassFormatErrorException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverClassFormatErrorException(HttpServletRequest request,
			HttpServletResponse response, DriverClassFormatErrorException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(URLNotAcceptedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionURLNotAcceptedException(HttpServletRequest request, HttpServletResponse response,
			URLNotAcceptedException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(UnsupportedGetConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionUnsupportedGetConnectionException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedGetConnectionException exception)
	{
		setOperationMessageForException(request, exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(EstablishConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionEstablishConnectionException(HttpServletRequest request, HttpServletResponse response,
			EstablishConnectionException exception)
	{
		setOperationMessageForException(request, exception, true);

		return ERROR_PAGE_URL;
	}

	protected void setOperationMessageForException(HttpServletRequest request, Exception exception,
			boolean traceException, Object... messageArgs)
	{
		String code = buildMessageCode(exception.getClass().getSimpleName());

		setOperationMessageForException(request, code, exception, traceException, messageArgs);
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return buildMessageCode("error", code);
	}
}
