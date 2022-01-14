package se.sundsvall.disturbance.api.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.exception.ServiceRuntimeException;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

@ExtendWith(MockitoExtension.class)
class ServiceRuntimeExceptionMapperTest {

	private static final String REQUEST_PATH = "http://localhost:1234/path";
	private static final String SERVICE_ID = "Some service-ID";
	private static final String EXCEPTION_MESSAGE = "Service error";
	private static final String ROOT_CAUSE = "Gateway timeout during call";
	private static final int ROOT_CODE = Status.GATEWAY_TIMEOUT.getStatusCode();

	private ServiceRuntimeException serviceRuntimeException;

	@Mock
	private UriInfo uriInfoMock;

	@InjectMocks
	private ServiceRuntimeExceptionMapper exceptionMapper;

	@BeforeEach
	void setup() {

		serviceRuntimeException = ServiceException.create(EXCEPTION_MESSAGE)
			.withStatus(Status.GATEWAY_TIMEOUT)
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCause(ROOT_CAUSE)
				.withRootCode(ROOT_CODE)
				.withServiceId(SERVICE_ID)
				.withDetails(List.of("Special detail")))
			.asRuntimeException();

		when(uriInfoMock.getPath()).thenReturn(REQUEST_PATH);
	}

	@Test
	void constraintValidationException() {

		final var response = exceptionMapper.toResponse(serviceRuntimeException).readEntity(ServiceErrorResponse.class);

		assertThat(response).isEqualTo(ServiceErrorResponse.create()
			.withMessage(EXCEPTION_MESSAGE)
			.withHttpCode(ROOT_CODE)
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCode(ROOT_CODE)
				.withRootCause(ROOT_CAUSE)
				.withServiceId(SERVICE_ID)
				.withDetails(List.of("Special detail", "Request: ".concat(REQUEST_PATH)))));
	}
}
