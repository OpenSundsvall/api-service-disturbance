package se.sundsvall.disturbance.api.exception.mapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

@Provider
public class ClientErrorExceptionMapper extends AbstractExceptionMapper<ClientErrorException> {

	private static final Logger LOGGER = getLogger(ClientErrorExceptionMapper.class);

	@Override
	public Response toResponse(ClientErrorException e) {

		LOGGER.info("Mapping exception into ServiceErrorResponse", e);

		final var serviceErrorResponse = ServiceErrorResponse.create()
			.withMessage("Request not valid!").withHttpCode(BAD_REQUEST.getStatusCode())
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCode(BAD_REQUEST.getStatusCode())
				.withServiceId(getApplicationName())
				.withDetails(List.of(e.getMessage(), "Request: " + uriInfo.getPath())));

		return wrapServiceErrorResponse(serviceErrorResponse);
	}
}
