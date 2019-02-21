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
import org.datagear.persistence.support.SqlExpressionErrorException;
import org.datagear.persistence.support.VariableExpressionErrorException;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.IllegalSourceValueException;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.DeliverContentTypeExceptionHandlerExceptionResolver;
import org.datagear.web.util.WebUtils;
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

	@ExceptionHandler(AuthenticationFailedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleAuthenticationFailedException(HttpServletRequest request, HttpServletResponse response,
			AuthenticationFailedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(AuthenticationFailedException.class), exception,
				true);

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
		setOperationMessageForThrowable(request, buildMessageCode(IllegalArgumentException.class), exception, false);

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

	@ExceptionHandler(RecordNotFoundOrPermissionDeniedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerRecordNotFoundOrNoPermissionException(HttpServletRequest request,
			HttpServletResponse response, RecordNotFoundOrPermissionDeniedException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(RecordNotFoundOrPermissionDeniedException.class),
				exception, false);

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

	@ExceptionHandler(IllegalSourceValueException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleControllerIllegalSourceValueException(HttpServletRequest request, HttpServletResponse response,
			IllegalSourceValueException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(IllegalSourceValueException.class), exception, false,
				exception.getSourceValue(), exception.getTargetType().getName());

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

	@ExceptionHandler(VariableExpressionErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceVariableExpressionErrorException(HttpServletRequest request,
			HttpServletResponse response, VariableExpressionErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(VariableExpressionErrorException.class),
				exception.getCause(), false, exception.getExpression().getContent());

		return getErrorView(request, response);
	}

	@ExceptionHandler(SqlExpressionErrorException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceSqlExpressionErrorException(HttpServletRequest request, HttpServletResponse response,
			SqlExpressionErrorException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(SqlExpressionErrorException.class),
				exception.getCause(), true, exception.getExpression().getContent());

		return getErrorView(request, response);
	}

	@ExceptionHandler(UnsupportedDialectException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handlePersistenceUnsupportedDialectException(HttpServletRequest request, HttpServletResponse response,
			UnsupportedDialectException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(UnsupportedDialectException.class), exception, false);

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
			setOperationMessageForThrowable(request, buildMessageCode(PersistenceException.class), exception, true);

		return getErrorView(request, response);
	}

	@ExceptionHandler(DatabaseInfoResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseInfoResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseInfoResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DatabaseInfoResolverException.class), exception,
				true);

		return getErrorView(request, response);
	}

	@ExceptionHandler(DatabaseModelResolverException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleDbmodelDatabaseModelResolverException(HttpServletRequest request, HttpServletResponse response,
			DatabaseModelResolverException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(DatabaseModelResolverException.class), exception,
				true);

		return getErrorView(request, response);
	}

	@ExceptionHandler(TableNotExistsException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleDbmodelTableNotExistsException(HttpServletRequest request, HttpServletResponse response,
			TableNotExistsException exception)
	{
		setOperationMessageForThrowable(request, buildMessageCode(TableNotExistsException.class), exception, false,
				exception.getTableName());

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
		setOperationMessageForThrowable(request, buildMessageCode(DriverEntityManagerException.class), exception, true);

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
