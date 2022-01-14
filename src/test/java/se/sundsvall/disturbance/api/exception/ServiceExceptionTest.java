package se.sundsvall.disturbance.api.exception;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.BAD_GATEWAY;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceExceptionTest {

	private ServiceException exceptionWithoutCauseAndServiceId;
	private ServiceException exceptionWithoutCause;
	private ServiceException exceptionWithCause;

	@BeforeEach
	void setup() {
		exceptionWithoutCause = ServiceException.create(
			"This is the error message",
			"serviceId",
			Status.INTERNAL_SERVER_ERROR,
			Status.BAD_GATEWAY,
			"Error 1", "Error 2", "Error 3");

		exceptionWithCause = ServiceException.create(
			"This is the error message",
			new Exception("SubException", new Exception("SubSubException")),
			"serviceId",
			Status.INTERNAL_SERVER_ERROR,
			Status.BAD_GATEWAY,
			"Error 1", "Error 2", "Error 3");

		exceptionWithoutCauseAndServiceId = ServiceException.create(
			"This is the error message",
			Status.INTERNAL_SERVER_ERROR,
			Status.BAD_GATEWAY,
			"Error 1", "Error 2", "Error 3");
	}

	@Test
	void testException() {
		assertThat(exceptionWithoutCause).isNotNull();
		assertThat(ExceptionUtils.getThrowableCount(exceptionWithoutCause)).isEqualTo(1);
		assertThat(exceptionWithoutCause.getMessage()).isEqualTo("This is the error message");
		assertThat(exceptionWithoutCause.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(exceptionWithoutCause.getTechnicalDetails()).isNotNull();
		assertThat(exceptionWithoutCause.getTechnicalDetails().getRootCause()).isEqualTo(BAD_GATEWAY.getReasonPhrase());
		assertThat(exceptionWithoutCause.getTechnicalDetails().getRootCode()).isEqualTo(BAD_GATEWAY.getStatusCode());
		assertThat(exceptionWithoutCause.getTechnicalDetails().getServiceId()).isEqualTo("serviceId");
		assertThat(exceptionWithoutCause.getTechnicalDetails().getDetails()).containsExactly("Error 1", "Error 2", "Error 3");

		assertThat(exceptionWithCause).isNotNull();
		assertThat(ExceptionUtils.getThrowableCount(exceptionWithCause)).isEqualTo(3);
		assertThat(exceptionWithCause.getMessage()).isEqualTo("This is the error message");
		assertThat(exceptionWithCause.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(exceptionWithCause.getTechnicalDetails()).isNotNull();
		assertThat(exceptionWithCause.getTechnicalDetails().getRootCause()).isEqualTo("SubException");
		assertThat(exceptionWithCause.getTechnicalDetails().getRootCode()).isEqualTo(BAD_GATEWAY.getStatusCode());
		assertThat(exceptionWithCause.getTechnicalDetails().getServiceId()).isEqualTo("serviceId");
		assertThat(exceptionWithCause.getTechnicalDetails().getDetails()).containsExactly("Error 1", "Error 2", "Error 3");

		List<String> exceptionMessages = ExceptionUtils.getThrowableList(exceptionWithCause).stream()
			.map(Throwable::getMessage)
			.collect(toList());

		assertThat(exceptionMessages).containsSequence("This is the error message", "SubException", "SubSubException");

		assertThat(exceptionWithoutCauseAndServiceId).isNotNull();
		assertThat(ExceptionUtils.getThrowableCount(exceptionWithoutCauseAndServiceId)).isEqualTo(1);
		assertThat(exceptionWithoutCauseAndServiceId.getMessage()).isEqualTo("This is the error message");
		assertThat(exceptionWithoutCauseAndServiceId.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(exceptionWithoutCauseAndServiceId.getTechnicalDetails()).isNotNull();
		assertThat(exceptionWithoutCauseAndServiceId.getTechnicalDetails().getRootCause()).isEqualTo(BAD_GATEWAY.getReasonPhrase());
		assertThat(exceptionWithoutCauseAndServiceId.getTechnicalDetails().getRootCode()).isEqualTo(BAD_GATEWAY.getStatusCode());
		assertThat(exceptionWithoutCauseAndServiceId.getTechnicalDetails().getServiceId()).isNull();
		assertThat(exceptionWithoutCauseAndServiceId.getTechnicalDetails().getDetails())
			.containsExactly("Error 1", "Error 2", "Error 3");
	}

	@Test
	void testAsRuntimeException() {
		final var runtimeException = exceptionWithCause.asRuntimeException();

		assertThat(ExceptionUtils.indexOfThrowable(runtimeException, ServiceException.class)).isNotEqualTo(-1);
		assertThat(runtimeException).isInstanceOf(RuntimeException.class);
		assertThat(runtimeException.getMessage()).isEqualTo("This is the error message");

		// ...the other way around.
		assertThat(runtimeException.getTypedCause()).isEqualTo(exceptionWithCause);
		assertThat(runtimeException.getTypedCause()).isEqualTo(runtimeException.getCause());
	}
}
