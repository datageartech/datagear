/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.sql.SQLException;

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
import org.datagear.persistence.support.NotUniqueRecordException;
import org.datagear.persistence.support.SqlExpressionErrorException;
import org.datagear.persistence.support.VariableExpressionErrorException;
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
		setOperationMessageForThrowable(request, buildMessageCode(MissingServletRequestParameterException.class),
				exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerBindException(HttpServletRequest request, HttpServletResponse response,
			BindException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(BindException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerMethodArgumentNotValidException(HttpServletRequest request,
			HttpServletResponse response, MethodArgumentNotValidException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(MethodArgumentNotValidException.class), exception,
				false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAuthenticationFailedException(HttpServletRequest request, HttpServletResponse response,
			AuthenticationFailedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(AuthenticationFailedException.class), exception,
				true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(IllegalInputException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalInputException(HttpServletRequest request, HttpServletResponse response,
			IllegalInputException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(IllegalInputException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(RecordNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerRecordNotFoundException(HttpServletRequest request, HttpServletResponse response,
			RecordNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(RecordNotFoundException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(RecordNotFoundOrPermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerRecordNotFoundOrNoPermissionException(HttpServletRequest request,
			HttpServletResponse response, RecordNotFoundOrPermissionDeniedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(RecordNotFoundOrPermissionDeniedException.class),
				exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(SchemaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerSchemaNotFoundException(HttpServletRequest request, HttpServletResponse response,
			SchemaNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SchemaNotFoundException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(IllegalSourceValueException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalSourceValueException(HttpServletRequest request, HttpServletResponse response,
			IllegalSourceValueException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(IllegalSourceValueException.class), exception, false,
				exception.getSourceValue(), exception.getTargetType().getName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleControllerFileNotFoundException(HttpServletRequest request, HttpServletResponse response,
			FileNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(FileNotFoundException.class), exception, false,
				exception.getFileName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(VariableExpressionErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceVariableExpressionErrorException(HttpServletRequest request,
			HttpServletResponse response, VariableExpressionErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(VariableExpressionErrorException.class),
				exception.getCause(), false, exception.getExpression().getContent());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(SqlExpressionErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceSqlExpressionErrorException(HttpServletRequest request, HttpServletResponse response,
			SqlExpressionErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlExpressionErrorException.class),
				exception.getCause(), true, exception.getExpression().getContent());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(UnsupportedDialectException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceUnsupportedDialectException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedDialectException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedDialectException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(NotUniqueRecordException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handlePersistenceNotUniqueRecordException(HttpServletRequest request, HttpServletResponse response,
			NotUniqueRecordException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(NotUniqueRecordException.class), exception, false);

		return ERROR_PAGE_URL;
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
			setOperationMessageForThrowable(request, buildMessageCode(PersistenceException.class), exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DatabaseInfoResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseInfoResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseInfoResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DatabaseInfoResolverException.class), exception,
				true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DatabaseModelResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseModelResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseModelResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DatabaseModelResolverException.class), exception,
				true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(TableNotExistsException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleDbmodelTableNotExistsException(HttpServletRequest request, HttpServletResponse response,
			TableNotExistsException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(TableNotExistsException.class), exception, false,
				exception.getTableName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(ConnectionSourceException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionConnectionSourceException(HttpServletRequest request, HttpServletResponse response,
			ConnectionSourceException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(ConnectionSourceException.class), exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverEntityManagerException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverEntityManagerException(HttpServletRequest request, HttpServletResponse response,
			DriverEntityManagerException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverEntityManagerException.class), exception, true);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverNotFoundException(HttpServletRequest request, HttpServletResponse response,
			DriverNotFoundException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverNotFoundException.class), exception, false,
				exception.getDriverClassName());

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(DriverClassFormatErrorException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionDriverClassFormatErrorException(HttpServletRequest request,
			HttpServletResponse response, DriverClassFormatErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DriverClassFormatErrorException.class), exception,
				false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(URLNotAcceptedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionURLNotAcceptedException(HttpServletRequest request, HttpServletResponse response,
			URLNotAcceptedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(URLNotAcceptedException.class), exception, false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(UnsupportedGetConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionUnsupportedGetConnectionException(HttpServletRequest request,
			HttpServletResponse response, UnsupportedGetConnectionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedGetConnectionException.class), exception,
				false);

		return ERROR_PAGE_URL;
	}

	@ExceptionHandler(EstablishConnectionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleConnectionEstablishConnectionException(HttpServletRequest request, HttpServletResponse response,
			EstablishConnectionException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(EstablishConnectionException.class),
				exception.getCause(), true);

		return ERROR_PAGE_URL;
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
