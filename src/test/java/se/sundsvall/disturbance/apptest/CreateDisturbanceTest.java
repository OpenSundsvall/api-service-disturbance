package se.sundsvall.disturbance.apptest;

import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.apptest.support.WireMockLifecycleManager;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

/**
 * Create disturbance application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class CreateDisturbanceTest extends AbstractAppTest {

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Test
	void test1_createDisturbance() throws Exception {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-1";

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isNotPresent();

		setupCall()
			.withServicePath("/disturbances")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(Status.CREATED)
			.withExpectedResponse("response.json")
			.withExpectedResponseHeader(LOCATION, List.of("http://localhost:8081/disturbances/" + category + "/" + disturbanceId))
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isPresent();
	}

	@Test
	void test2_createDisturbanceWhenFeedbackExists() throws Exception {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-with-feedback-1";

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isNotPresent();

		setupCall()
			.withServicePath("/disturbances")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(Status.CREATED)
			.withExpectedResponse("response.json")
			.withExpectedResponseHeader(LOCATION, List.of("http://localhost:8081/disturbances/" + category + "/" + disturbanceId))
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isPresent();
	}
}
