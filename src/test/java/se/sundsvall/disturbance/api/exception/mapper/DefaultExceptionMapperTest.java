package se.sundsvall.disturbance.api.exception.mapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

@ExtendWith(MockitoExtension.class)
class DefaultExceptionMapperTest {

	private static final String REQUEST_PATH = "http://localhost:1234/path";
	private static final String APPLICATION_NAME = "The-app";
	private static final String EXCEPTION_MESSAGE = "an error occured";

	private NullPointerException exception;

	@Mock
	private UriInfo uriInfoMock;

	@Mock
	private Config configMock;

	@InjectMocks
	private DefaultExceptionMapper exceptionMapper;

	@BeforeEach
	void setup() {

		exception = new NullPointerException(EXCEPTION_MESSAGE);

		when(configMock.getOptionalValue("quarkus.application.name", String.class)).thenReturn(Optional.of(APPLICATION_NAME));
		when(uriInfoMock.getPath()).thenReturn(REQUEST_PATH);
	}

	@Test
	void defaultException() {

		final var response = exceptionMapper.toResponse(exception).readEntity(ServiceErrorResponse.class);

		assertThat(response).isEqualTo(ServiceErrorResponse.create()
			.withMessage("Service error!")
			.withHttpCode(INTERNAL_SERVER_ERROR.getStatusCode())
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCode(INTERNAL_SERVER_ERROR.getStatusCode())
				.withRootCause(EXCEPTION_MESSAGE)
				.withServiceId(APPLICATION_NAME)
				.withDetails(List.of(
					"Type: NullPointerException",
					"Request: ".concat(REQUEST_PATH)))));
	}
}
