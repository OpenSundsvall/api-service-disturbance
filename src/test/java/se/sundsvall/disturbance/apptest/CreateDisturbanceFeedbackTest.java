package se.sundsvall.disturbance.apptest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.apptest.support.WireMockLifecycleManager;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;

/**
 * Create disturbance feedback application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class CreateDisturbanceFeedbackTest extends AbstractAppTest {

	@Inject
	DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Test
	void test1_createDisturbanceFeedback() throws Exception {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-11";
		final var partyId = "3807839a-3bab-11ec-8d3d-0242ac130003";

		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyIdOptional(category, disturbanceId, partyId)).isNotPresent();

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId + "/feedback")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(Status.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyIdOptional(category, disturbanceId, partyId)).isPresent();
	}
}
