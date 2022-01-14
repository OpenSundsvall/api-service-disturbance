package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.disturbance.integration.db.DisturbanceFeedbackHistoryRepository.STATUS_SENT;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

/**
 * Disturbance feedback history repository tests.
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@TestTransaction
class DisturbanceFeedbackHistoryRepositoryTest {

	@Inject
	DisturbanceFeedbackHistoryRepository disturbanceFeedbackHistoryRepository;

	private static final String CATEGORY = "category";
	private static final String DISTURBANCE_ID = "disturbanceId";
	private static final String PARTY_ID = "partyId";

	@Test
	void persistWithStatusSent() {
		final var entity = new DisturbanceFeedbackEntity();
		entity.setCategory(CATEGORY);
		entity.setDisturbanceId(DISTURBANCE_ID);
		entity.setPartyId(PARTY_ID);

		disturbanceFeedbackHistoryRepository.persistWithStatusSent(entity);

		final var list = disturbanceFeedbackHistoryRepository.list("partyId", PARTY_ID);

		assertThat(list)
			.hasSize(1)
			.allSatisfy(history -> {
				assertThat(history.getDisturbanceId()).isEqualTo(DISTURBANCE_ID);
				assertThat(history.getCategory()).isEqualTo(CATEGORY);
				assertThat(history.getStatus()).isEqualTo(STATUS_SENT);
			});
	}
}
