package se.sundsvall.disturbance.api.exception.mapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

@Provider
public class DefaultExceptionMapper extends AbstractExceptionMapper<Exception> {

	private static final Logger LOGGER = getLogger(DefaultExceptionMapper.class);

	@Override
	public Response toResponse(Exception e) {

		LOGGER.info("Mapping exception into ServiceErrorResponse", e);

		final var serviceErrorResponse = ServiceErrorResponse.create()
			.withMessage("Service error!").withHttpCode(INTERNAL_SERVER_ERROR.getStatusCode())
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCode(INTERNAL_SERVER_ERROR.getStatusCode())
				.withRootCause(extractMessage(e))
				.withServiceId(getApplicationName())
				.withDetails(List.of("Type: " + e.getClass().getSimpleName(), "Request: " + uriInfo.getPath())));

		return wrapServiceErrorResponse(serviceErrorResponse);
	}

	private String extractMessage(Exception e) {
		return Optional.ofNullable(e.getMessage()).orElse(String.valueOf(e));
	}
}
