package se.sundsvall.disturbance.integration.messaging.mappers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_GATEWAY;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.messaging.ServiceErrorResponse;
import generated.se.sundsvall.messaging.TechnicalDetails;

@ExtendWith(MockitoExtension.class)
class MessagingExceptionMapperTest {

	@Mock
	private Response responseMock;

	@InjectMocks
	private MessagingExceptionMapper mapper;

	@Test
	void testClientErrorResponse() {

		when(responseMock.getStatus()).thenReturn(BAD_REQUEST.getStatusCode());
		when(responseMock.getStatusInfo()).thenReturn(BAD_REQUEST);
		when(responseMock.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);
		when(responseMock.readEntity(ServiceErrorResponse.class)).thenReturn(new ServiceErrorResponse()
			.httpCode(BAD_REQUEST.getStatusCode())
			.message("Bad request")
			.technicalDetails(new TechnicalDetails()
				.rootCode(BAD_REQUEST.getStatusCode())
				.serviceId("called-service")
				.details(List.of("error1", "error2"))));

		final var result = mapper.toThrowable(responseMock);

		assertThat(result).isNotNull();
		assertThat(result.getMessage()).isEqualTo("Error calling api-messaging");
		assertThat(result.getStatus()).isEqualTo(BAD_GATEWAY);
		assertThat(result.getTechnicalDetails().getRootCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(result.getTechnicalDetails().getRootCause()).isEqualTo("Bad Request");
		assertThat(result.getTechnicalDetails().getServiceId()).isEqualTo("called-service");
		assertThat(result.getTechnicalDetails().getDetails()).containsExactly("error1", "error2");
	}

	@Test
	void testServerErrorResponse() {

		when(responseMock.getStatus()).thenReturn(INTERNAL_SERVER_ERROR.getStatusCode());
		when(responseMock.getStatusInfo()).thenReturn(INTERNAL_SERVER_ERROR);
		when(responseMock.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);
		when(responseMock.readEntity(ServiceErrorResponse.class)).thenReturn(new ServiceErrorResponse()
			.httpCode(INTERNAL_SERVER_ERROR.getStatusCode())
			.message("Service is not up and running")
			.technicalDetails(new TechnicalDetails()
				.rootCode(INTERNAL_SERVER_ERROR.getStatusCode())
				.serviceId("called-service")
				.details(List.of("error1", "error2"))));

		final var result = mapper.toThrowable(responseMock);

		assertThat(result).isNotNull();
		assertThat(result.getMessage()).isEqualTo("Error calling api-messaging");
		assertThat(result.getStatus()).isEqualTo(BAD_GATEWAY);
		assertThat(result.getTechnicalDetails().getRootCode()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
		assertThat(result.getTechnicalDetails().getRootCause()).isEqualTo("Internal Server Error");
		assertThat(result.getTechnicalDetails().getServiceId()).isEqualTo("called-service");
		assertThat(result.getTechnicalDetails().getDetails()).containsExactly("error1", "error2");
	}

	@Test
	void testServerErrorResponseWithNonJsonMediaType() {

		when(responseMock.getStatus()).thenReturn(INTERNAL_SERVER_ERROR.getStatusCode());
		when(responseMock.getStatusInfo()).thenReturn(INTERNAL_SERVER_ERROR);
		when(responseMock.getMediaType()).thenReturn(TEXT_HTML_TYPE);
		when(responseMock.readEntity(String.class)).thenReturn("<body>Something went wrong</body>");

		final var result = mapper.toThrowable(responseMock);

		assertThat(result).isNotNull();
		assertThat(result.getMessage()).isEqualTo("Error calling api-messaging");
		assertThat(result.getStatus()).isEqualTo(BAD_GATEWAY);
		assertThat(result.getTechnicalDetails().getRootCode()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
		assertThat(result.getTechnicalDetails().getRootCause()).isEqualTo("Internal Server Error");
		assertThat(result.getTechnicalDetails().getServiceId()).isEqualTo(MessagingExceptionMapper.INTEGRATION_NAME);
		assertThat(result.getTechnicalDetails().getDetails()).containsExactly("<body>Something went wrong</body>");
	}

	@Test
	void testOKResponse() {

		when(responseMock.getStatus()).thenReturn(OK.getStatusCode());
		when(responseMock.getStatusInfo()).thenReturn(OK);

		final var result = mapper.toThrowable(responseMock);

		assertThat(result).isNull();
	}
}
