package se.sundsvall.disturbance.apptest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.apptest.support.WireMockLifecycleManager;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;

/**
 * Create feedback application tests
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class CreateFeedbackTest extends AbstractAppTest {

	@Inject
	FeedbackRepository feedbackRepository;

	@Test
	void test1_createFeedback() throws Exception {

		final var partyId = "a1967da0-4c42-11ec-81d3-0242ac130003";

		assertThat(feedbackRepository.findByPartyIdOptional(partyId)).isNotPresent();

		setupCall()
			.withServicePath("/feedback")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(Status.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(feedbackRepository.findByPartyIdOptional(partyId)).isPresent();
	}
}
