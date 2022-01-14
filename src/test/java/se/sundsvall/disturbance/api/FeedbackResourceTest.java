package se.sundsvall.disturbance.api;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.service.FeedbackService;

@QuarkusTest
class FeedbackResourceTest {

	@InjectMock
	FeedbackService feedbackServiceMock;

	@Test
	void createFeedback() throws ServiceException {

		final var body = FeedbackCreateRequest.create()
			.withPartyId(UUID.randomUUID().toString());

		given()
			.contentType(APPLICATION_JSON)
			.body(body)
			.when()
			.post("/feedback")
			.then().assertThat()
			.statusCode(NO_CONTENT.getStatusCode())
			.contentType(is(emptyOrNullString()));

		verify(feedbackServiceMock).createFeedback(body);
		verifyNoMoreInteractions(feedbackServiceMock);
	}

	@Test
	void deleteFeedback() throws ServiceException {

		final var partyId = UUID.randomUUID().toString();

		given()
			.pathParam("partyId", partyId)
			.contentType(APPLICATION_JSON)
			.when()
			.delete("/feedback/{partyId}")
			.then().assertThat()
			.statusCode(NO_CONTENT.getStatusCode())
			.contentType(is(emptyOrNullString()));

		verify(feedbackServiceMock).deleteFeedback(partyId);
		verifyNoMoreInteractions(feedbackServiceMock);
	}
}
