package se.sundsvall.disturbance.apptest;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.apptest.support.WireMockLifecycleManager;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

/**
 * Update disturbance application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class UpdateDisturbanceTest extends AbstractAppTest {

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Test
	void test1_updateDisturbanceContent() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-5";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getTitle()).isEqualTo("Planerat avbrott i eget nät");
		assertThat(updatedDisturbance.get().getDescription()).isEqualTo("Vi felsöker strömavbrottet.");
		assertThat(updatedDisturbance.get().getPlannedStopDate()).isEqualTo(LocalDateTime.of(2022, 01, 04, 18, 00, 20, 0).atOffset(now().getOffset()));
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}

	@Test
	void test2_updateDisturbanceRemoveAffecteds() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-6";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(1);
	}

	@Test
	void test3_updateDisturbanceAddAffecteds() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-7";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(4);
	}

	@Test
	void test4_updateDisturbanceChangeStatusToClosed() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-8";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getStatus()).isEqualTo(Status.CLOSED.toString());
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}

	@Test
	void test5_updateDisturbanceChangeStatusFromPlannedToOpen() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-12";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(HttpMethod.PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getStatus()).isEqualTo(Status.OPEN.toString());
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}
}
