package se.sundsvall.disturbance.api.exception.mapper;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import se.sundsvall.disturbance.api.exception.ServiceRuntimeException;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;

@Provider
public class ServiceRuntimeExceptionMapper extends AbstractExceptionMapper<ServiceRuntimeException> {

	private static final Logger LOGGER = getLogger(ServiceRuntimeExceptionMapper.class);

	@Override
	public Response toResponse(final ServiceRuntimeException e) {

		LOGGER.info("Mapping exception into ServiceErrorResponse", e);

		final var se = e.getTypedCause();
		final var serviceErrorResponse = ServiceErrorResponse.createFrom(se)
			.withTechnicalDetails(
				se.getTechnicalDetails()
					.withServiceId(se.getTechnicalDetails().getServiceId() != null ? se.getTechnicalDetails().getServiceId() : getApplicationName())
					.withDetails(mergeLists(
						se.getTechnicalDetails().getDetails(),
						List.of("Request: " + uriInfo.getPath()))));

		return wrapServiceErrorResponse(serviceErrorResponse);
	}

	@SafeVarargs
	private List<String> mergeLists(List<String>... lists) {
		return Stream.of(lists).filter(Objects::nonNull).flatMap(Collection::stream).collect(toList());
	}
}
