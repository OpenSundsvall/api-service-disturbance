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
 * Delete feedback application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class DeleteFeedbackTest extends AbstractAppTest {

	@Inject
	FeedbackRepository feedbackRepository;

	@Test
	void test1_deleteFeedback() throws Exception {

		final var partyId = "3c1236ca-4c44-11ec-81d3-0242ac130003";

		assertThat(feedbackRepository.findByPartyIdOptional(partyId)).isPresent();

		setupCall()
			.withServicePath("/feedback/" + partyId)
			.withHttpMethod(HttpMethod.DELETE)
			.withExpectedResponseStatus(Status.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(feedbackRepository.findByPartyIdOptional(partyId)).isNotPresent();
	}
}
