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
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

/**
 * Delete disturbance application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class DeleteDisturbanceTest extends AbstractAppTest {

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Test
	void test1_deleteDisturbanceWithStatusOpen() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-9";

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isPresent();

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.DELETE)
			.withExpectedResponseStatus(Status.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isNotPresent();
	}

	@Test
	void test2_deleteDisturbanceWithStatusClosed() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-10";

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isPresent();

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.DELETE)
			.withExpectedResponseStatus(Status.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).isNotPresent();
	}
}
