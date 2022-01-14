package se.sundsvall.disturbance.apptest;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.apptest.support.WireMockLifecycleManager;

/**
 * Read disturbance application tests
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
class ReadDisturbanceTest extends AbstractAppTest {

	@Test
	void test1_readDisturbanceById() throws Exception {

		final var disturbanceId = "disturbance-2";

		setupCall()
			.withServicePath("/disturbances/COMMUNICATION/" + disturbanceId)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_readDisturbanceByPartyId() throws Exception {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath("/disturbances/affecteds/" + partyId)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_readDisturbanceByPartyIdAndCategoryFilter() throws Exception {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath("/disturbances/affecteds/" + partyId + "?category=COMMUNICATION")
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_readDisturbanceByPartyIdAndStatusFilter() throws Exception {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath("/disturbances/affecteds/" + partyId + "?status=OPEN")
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_readDisturbanceByPartyIdWithNoResults() throws Exception {

		final var partyId = "887a45da-dbd4-4d58-98bc-4afe3c2ecc18"; // Doesn't exist in DB.

		setupCall()
			.withServicePath("/disturbances/affecteds/" + partyId)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(Response.Status.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
