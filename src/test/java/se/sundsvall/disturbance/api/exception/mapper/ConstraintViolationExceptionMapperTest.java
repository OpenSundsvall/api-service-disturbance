package se.sundsvall.disturbance.api.exception.mapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
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
class ConstraintViolationExceptionMapperTest {

	private static final String REQUEST_PATH = "http://localhost:1234/path";
	private static final String APPLICATION_NAME = "The-app";

	@Mock
	private ConstraintViolation<?> constraintViolationMock;

	@Mock
	private ConstraintViolationException constraintViolationExceptionMock;

	@Mock
	private Path pathMock;

	@Mock
	private UriInfo uriInfoMock;

	@Mock
	private Config configMock;

	@InjectMocks
	private ConstraintViolationExceptionMapper exceptionMapper;

	@BeforeEach
	void setup() {
		when(uriInfoMock.getPath()).thenReturn(REQUEST_PATH);
		when(pathMock.toString()).thenReturn("object.fieldX");
		when(constraintViolationMock.getMessage()).thenReturn("is not a valid property");
		when(constraintViolationMock.getPropertyPath()).thenReturn(pathMock);
		when(constraintViolationExceptionMock.getConstraintViolations()).thenReturn(Set.of(constraintViolationMock));
		when(configMock.getOptionalValue("quarkus.application.name", String.class)).thenReturn(Optional.of(APPLICATION_NAME));
	}

	@Test
	void constraintValidationException() {

		final var response = exceptionMapper.toResponse(constraintViolationExceptionMock).readEntity(ServiceErrorResponse.class);

		assertThat(response).isEqualTo(ServiceErrorResponse.create()
			.withMessage("Request validation failed!")
			.withHttpCode(BAD_REQUEST.getStatusCode())
			.withTechnicalDetails(TechnicalDetails.create()
				.withRootCode(BAD_REQUEST.getStatusCode())
				.withRootCause("Constraint violation")
				.withServiceId(APPLICATION_NAME)
				.withDetails(List.of(
					"fieldX: is not a valid property",
					"Request: ".concat(REQUEST_PATH)))));
	}
}
