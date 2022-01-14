package se.sundsvall.disturbance.api;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@QuarkusTest
class DisturbanceResourceFailuresTest {

	@InjectMock
	DisturbanceFeedbackService disturbanceFeedbackServiceMock;

	@InjectMock
	DisturbanceService disturbanceServiceMock;

	@InjectMock
	DisturbanceService disturbanceService;

	@ConfigProperty(name = "quarkus.application.name")
	String applicationName;

	/**
	 * Create disturbance tests:
	 */

	@Test
	void createDisturbanceMissingBody() {

		final var response = given()
			.contentType(APPLICATION_JSON)
			.when()
			.post("/disturbances/")
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
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceEmptyBody() {

		final var body = DisturbanceCreateRequest.create(); // Empty body.

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
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
				"body.category: must not be null",
				"body.description: must not be null",
				"body.id: must not be null",
				"body.status: must not be null",
				"body.title: must not be null",
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceMissingId() {

		final var body = DisturbanceCreateRequest.create() // Body with missing id.
			.withCategory(Category.COMMUNICATION)
			.withTitle("Title")
			.withDescription("Description")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN);

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
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
				"body.id: must not be null",
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceMissingCategory() {

		final var body = DisturbanceCreateRequest.create() // Body with missing category.
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("Title")
			.withDescription("Description");

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
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
				"body.category: must not be null",
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceContainsInvalidAffected() {

		// Parameter values.
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";
		final var title = "Title";
		final var description = "Description";

		final var affecteds = List.of(
			Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
			Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
			Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2"));

		final var body = DisturbanceCreateRequest.create() // Body with invalid partyId
			.withId(disturbanceId)
			.withCategory(category)
			.withTitle(title)
			.withDescription(description)
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(affecteds);

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
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
				"body.affecteds[1].partyId: not a valid UUID",
				"body.affecteds[1].reference: must not be null",
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceToLongParameters() {

		final var affecteds = List.of(Affected.create()
			.withPartyId(UUID.randomUUID().toString())
			.withReference(repeat("*", 513)));

		final var body = DisturbanceCreateRequest.create() // Body with to long parameters.
			.withId(repeat("*", 256))
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withCategory(Category.ELECTRICITY)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(affecteds);

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
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
				"body.affecteds[0].reference: size must be between 0 and 512",
				"body.description: size must be between 0 and 8192",
				"body.id: size must be between 0 and 255",
				"body.title: size must be between 0 and 255",
				"Request: /disturbances/")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Get disturbance by partyId tests:
	 */

	@Test
	void getDisturbancesByPartyIdBadPartyId() {

		// Parameter values
		final var partyId = "this-is-not-an-uuid";

		final var response = given()
			.pathParam("partyId", partyId)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/affecteds/{partyId}")
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
				"Request: /disturbances/affecteds/this-is-not-an-uuid")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdBadCategory() {

		// Parameter values
		final var partyId = UUID.randomUUID().toString();
		final var category = "not-a-category";

		final var response = given()
			.pathParam("partyId", partyId)
			.queryParam("category", category)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/affecteds/{partyId}")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request not valid!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withServiceId(applicationName)
			.withDetails(List.of(
				"RESTEASY003870: Unable to extract parameter from http request: javax.ws.rs.QueryParam(\"category\") value is 'not-a-category'",
				"Request: /disturbances/affecteds/" + partyId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdIdBadStatus() {

		// Parameter values
		final var partyId = UUID.randomUUID().toString();
		final var status = "not-a-status";

		final var response = given()
			.pathParam("partyId", partyId)
			.queryParam("status", status)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/affecteds/{partyId}")
			.then().assertThat()
			.statusCode(BAD_REQUEST.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(ServiceErrorResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getMessage()).isEqualTo("Request not valid!");
		assertThat(response.getHttpCode()).isEqualTo(BAD_REQUEST.getStatusCode());
		assertThat(response.getTechnicalDetails()).isEqualTo(TechnicalDetails.create()
			.withRootCode(BAD_REQUEST.getStatusCode())
			.withServiceId(applicationName)
			.withDetails(List.of(
				"RESTEASY003870: Unable to extract parameter from http request: javax.ws.rs.QueryParam(\"status\") value is 'not-a-status'",
				"Request: /disturbances/affecteds/" + partyId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Get disturbance tests:
	 */

	@Test
	void getDisturbanceBadDisturebanceId() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = " ";

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/{category}/{disturbanceId}")
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
				"disturbanceId: must not be blank",
				"Request: /disturbances/" + category + "/" + disturbanceId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Update disturbance tests:
	 */

	@Test
	void updateDisturbanceMissingBody() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.patch("/disturbances/{category}/{disturbanceId}")
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
				"Request: /disturbances/COMMUNICATION/" + disturbanceId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbanceBadDisturbanceId() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = " ";
		final var body = DisturbanceUpdateRequest.create();

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.patch("/disturbances/{category}/{disturbanceId}")
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
				"disturbanceId: must not be blank",
				"Request: /disturbances/COMMUNICATION/" + disturbanceId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbanceContainsInvalidAffected() {

		// Parameter values.
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";
		final var description = "Description";

		final var affecteds = List.of(
			Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference("test1"),
			Affected.create().withPartyId("invalid-party-id"), // Invalid UUID and missing reference.
			Affected.create().withPartyId("11e9e7aa-2ce4-11ec-8d3d-0242ac130003").withReference("test2"));

		final var body = DisturbanceUpdateRequest.create() // Body with invalid partyId
			.withDescription(description)
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withAffecteds(affecteds);

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.patch("/disturbances/{category}/{disturbanceId}")
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
				"body.affecteds[1].partyId: not a valid UUID",
				"body.affecteds[1].reference: must not be null",
				"Request: /disturbances/ELECTRICITY/12345")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbanceToLongParameters() {

		// Parameter values.
		final var category = Category.ELECTRICITY;
		final var disturbanceId = "12345";

		final var affecteds = List.of(
			Affected.create().withPartyId("11e9e570-2ce4-11ec-8d3d-0242ac130003").withReference(repeat("*", 513)));

		final var body = DisturbanceUpdateRequest.create() // Body with to long parameters
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle(repeat("*", 256))
			.withDescription(repeat("*", 8193))
			.withAffecteds(affecteds);

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.patch("/disturbances/{category}/{disturbanceId}")
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
				"body.affecteds[0].reference: size must be between 0 and 512",
				"body.description: size must be between 0 and 8192",
				"body.title: size must be between 0 and 255",
				"Request: /disturbances/ELECTRICITY/12345")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Delete disturbance tests:
	 */

	@Test
	void deleteDisturbanceBadDisturebanceId() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = " ";

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.delete("/disturbances/{category}/{disturbanceId}")
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
				"disturbanceId: must not be blank",
				"Request: /disturbances/" + category + "/" + disturbanceId)));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	/**
	 * Create disturbance feedback tests:
	 */

	@Test
	void createDisturbanceFeedbackMissingBody() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.post("/disturbances/{category}/{disturbanceId}/feedback")
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
				"Request: /disturbances/" + category + "/" + disturbanceId + "/feedback")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackMissingPartyId() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var body = DisturbanceFeedbackCreateRequest.create() // Missing partyId
			.withPartyId(null);

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/{category}/{disturbanceId}/feedback")
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
				"Request: /disturbances/" + category + "/" + disturbanceId + "/feedback")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackBadBodyFormat() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body("badformat")
			.when()
			.post("/disturbances/{category}/{disturbanceId}/feedback")
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
			.withDetails(List.of("Request: /disturbances/" + category + "/" + disturbanceId + "/feedback")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedbackBadPartyId() {

		// Parameter values
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var body = DisturbanceFeedbackCreateRequest.create() // Bad partyId
			.withPartyId("bad-party-id");

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/{category}/{disturbanceId}/feedback")
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
				"Request: /disturbances/" + category + "/" + disturbanceId + "/feedback")));

		verifyNoInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}
}
