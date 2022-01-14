package se.sundsvall.disturbance.api;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.service.FeedbackService;

@QuarkusTest
class FeedbackResourceFailuresTest {

	@ConfigProperty(name = "quarkus.application.name")
	String applicationName;

	@InjectMock
	FeedbackService feedbackServiceMock;

	/**
	 * Create feedback tests:
	 */

	@Test
	void createContinuousFeedbackMissingBody() {

		final var response = given()
			.contentType(APPLICATION_JSON)
			.when()
			.post("/feedback")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request validation failed!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withRootCause("Constraint violation")
			.withServiceId(applicationName)
			.withDetails(List.of(
				"body: must not be null",
				"Request: /feedback")));

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackMissingPartyId() {

		// Parameter values
		final var body = FeedbackCreateRequest.create() // Missing partyId
			.withPartyId(null);

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/feedback")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request validation failed!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withRootCause("Constraint violation")
			.withServiceId(applicationName)
			.withDetails(List.of(
				"body.partyId: must not be null",
				"body.partyId: not a valid UUID",
				"Request: /feedback")));

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadBodyFormat() {

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body("badformat")
			.when()
			.post("/feedback")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Bad request format!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withRootCause("Unrecognized token 'badformat': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')")
			.withServiceId(applicationName)
			.withDetails(List.of("Request: /feedback")));

		verifyNoInteractions(feedbackServiceMock);
	}

	@Test
	void createContinuousFeedbackBadPartyId() {

		// Parameter values
		final var body = FeedbackCreateRequest.create() // Bad partyId
			.withPartyId("bad-partyId-id");

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/feedback")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request validation failed!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withRootCause("Constraint violation")
			.withServiceId(applicationName)
			.withDetails(List.of(
				"body.partyId: not a valid UUID",
				"Request: /feedback")));

		verifyNoInteractions(feedbackServiceMock);
	}

	/**
	 * Delete feedback tests:
	 */

	@Test
	void deleteContinuousFeedbackBadPartyId() {

		// Parameter values
		final var partyId = "not-an-uuid";

		final var response = given()
			.pathParam("partyId", partyId)
			.contentType(APPLICATION_JSON)
			.when()
			.delete("/feedback/{partyId}")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request validation failed!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withRootCause("Constraint violation")
			.withServiceId(applicationName)
			.withDetails(List.of(
				"partyId: not a valid UUID",
				"Request: /feedback/" + partyId)));

		verifyNoInteractions(feedbackServiceMock);
	}
}
