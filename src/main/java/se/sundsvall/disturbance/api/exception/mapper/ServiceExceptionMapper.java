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

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;

@Provider
public class ServiceExceptionMapper extends AbstractExceptionMapper<ServiceException> {

	private static final Logger LOGGER = getLogger(ServiceExceptionMapper.class);

	@Override
	public Response toResponse(final ServiceException e) {

		LOGGER.info("Mapping exception into ServiceErrorResponse", e);

		final var serviceErrorResponse = ServiceErrorResponse.createFrom(e)
			.withTechnicalDetails(
				e.getTechnicalDetails()
					.withServiceId(e.getTechnicalDetails().getServiceId() != null ? e.getTechnicalDetails().getServiceId() : getApplicationName())
					.withDetails(mergeLists(
						e.getTechnicalDetails().getDetails(),
						List.of("Request: " + uriInfo.getPath()))));

		return wrapServiceErrorResponse(serviceErrorResponse);
	}

	@SafeVarargs
	private List<String> mergeLists(List<String>... lists) {
		return Stream.of(lists).filter(Objects::nonNull).flatMap(Collection::stream).collect(toList());
	}
}
