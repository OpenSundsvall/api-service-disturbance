package se.sundsvall.disturbance.api.exception.mapper;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;

public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionMapper.class);

	@Context
	protected UriInfo uriInfo;

	protected Config config = ConfigProvider.getConfig();

	protected Response wrapServiceErrorResponse(final ServiceErrorResponse serviceErrorResponse) {
		LOGGER.debug("Returning error response to client: '{}'", serviceErrorResponse);
		return Response.status(serviceErrorResponse.getHttpCode()).entity(serviceErrorResponse).build();
	}

	protected String getApplicationName() {
		return config.getOptionalValue("quarkus.application.name", String.class).orElse("Unknown");
	}
}
