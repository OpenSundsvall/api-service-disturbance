package se.sundsvall.disturbance.api.exception.mapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

@Provider
public class InvalidFormatExceptionMapper extends AbstractExceptionMapper<InvalidFormatException> {

	private static final Logger LOGGER = getLogger(InvalidFormatExceptionMapper.class);

	@Override
	public Response toResponse(InvalidFormatException e) {

		LOGGER.info("Mapping exception into ServiceErrorResponse", e);

		final var serviceErrorResponse = ServiceErrorResponse.create()
			.withMessage("Request validation failed!").withHttpCode(BAD_REQUEST.getStatusCode())
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCause(e.getOriginalMessage())
				.withRootCode(BAD_REQUEST.getStatusCode())
				.withServiceId(getApplicationName())
				.withDetails(List.of("Request: " + uriInfo.getPath())));

		return wrapServiceErrorResponse(serviceErrorResponse);
	}
}
