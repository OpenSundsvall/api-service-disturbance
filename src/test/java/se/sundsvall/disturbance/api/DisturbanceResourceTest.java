package se.sundsvall.disturbance.api;

import static io.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@QuarkusTest
class DisturbanceResourceTest {

	@InjectMock
	DisturbanceService disturbanceServiceMock;

	@InjectMock
	DisturbanceFeedbackService disturbanceFeedbackServiceMock;

	@Test
	void getDisturbancesByPartyId() throws ServiceException {

		// Parameters
		final var partyId = UUID.randomUUID().toString();

		final var response = given()
			.pathParam("partyId", partyId)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/affecteds/{partyId}")
			.then().assertThat()
			.statusCode(OK.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract()
			.as(Disturbance[].class);

		assertThat(response).isNotNull();
		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, emptyList(), emptyList());
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbancesByPartyIdAndFilterParameters() throws ServiceException {

		// Parameters
		final var partyId = UUID.randomUUID().toString();
		final var categoryFilter = List.of(Category.COMMUNICATION, Category.ELECTRICITY);
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.PLANNED, se.sundsvall.disturbance.api.model.Status.OPEN);

		final var response = given()
			.pathParam("partyId", partyId)
			.queryParam("category", categoryFilter)
			.queryParam("status", statusFilter)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/affecteds/{partyId}")
			.then().assertThat()
			.statusCode(OK.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(Disturbance[].class);

		assertThat(response).isNotNull();
		verify(disturbanceServiceMock).findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}

	@Test
	void getDisturbance() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceServiceMock.findByCategoryAndDisturbanceId(category, disturbanceId)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.get("/disturbances/{category}/{disturbanceId}")
			.then().assertThat()
			.statusCode(OK.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(Disturbance.class);

		assertThat(response).isNotNull();
		verify(disturbanceServiceMock).findByCategoryAndDisturbanceId(category, disturbanceId);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void updateDisturbance() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var description = "Updated description";
		final var body = DisturbanceUpdateRequest.create()
			.withDescription(description);

		when(disturbanceServiceMock.updateDisturbance(category, disturbanceId, body)).thenReturn(Disturbance.create()
			.withCategory(category)
			.withId(disturbanceId));

		final var response = given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.patch("/disturbances/{category}/{disturbanceId}")
			.then().assertThat()
			.statusCode(OK.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.extract().as(Disturbance.class);

		assertThat(response).isNotNull();
		verify(disturbanceServiceMock).updateDisturbance(category, disturbanceId, body);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void deleteDisturbance() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		doNothing().when(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);

		given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.when()
			.delete("/disturbances/{category}/{disturbanceId}")
			.then().assertThat()
			.statusCode(NO_CONTENT.getStatusCode())
			.contentType(is(emptyOrNullString()));

		verify(disturbanceServiceMock).deleteDisturbance(category, disturbanceId);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbance() throws ServiceException {

		final var body = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description");

		when(disturbanceServiceMock.createDisturbance(body)).thenReturn(Disturbance.create());

		final var response = given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/")
			.then().assertThat()
			.statusCode(CREATED.getStatusCode())
			.contentType(equalTo(APPLICATION_JSON))
			.header(LOCATION, "http://localhost:8081/disturbances/COMMUNICATION/id")
			.extract().as(Disturbance.class);

		assertThat(response).isNotNull();
		verify(disturbanceServiceMock).createDisturbance(body);
		verifyNoInteractions(disturbanceFeedbackServiceMock);
	}

	@Test
	void createDisturbanceFeedback() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var body = DisturbanceFeedbackCreateRequest.create()
			.withPartyId(UUID.randomUUID().toString());

		given()
			.pathParam("category", category)
			.pathParam("disturbanceId", disturbanceId)
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/disturbances/{category}/{disturbanceId}/feedback")
			.then().assertThat()
			.statusCode(NO_CONTENT.getStatusCode())
			.contentType(is(emptyOrNullString()));

		verify(disturbanceFeedbackServiceMock).createDisturbanceFeedback(category, disturbanceId, body);
		verifyNoMoreInteractions(disturbanceServiceMock, disturbanceFeedbackServiceMock);
	}
}
