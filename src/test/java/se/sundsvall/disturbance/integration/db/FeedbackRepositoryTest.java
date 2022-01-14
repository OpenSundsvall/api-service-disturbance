package se.sundsvall.disturbance.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

/**
 * Feedback repository tests.
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@TestTransaction
class FeedbackRepositoryTest {

	private static final String PARTY_ID = "49a974ea-9137-419b-bcb9-ad74c81a1d7f";

	@Inject
	FeedbackRepository feedbackRepository;

	@Test
	void findByPartyId() {
		final var optionalFeedback = feedbackRepository.findByPartyIdOptional(PARTY_ID);

		assertThat(optionalFeedback).isPresent();
		assertThat(optionalFeedback.get().getPartyId()).isEqualTo(PARTY_ID);
		assertThat(optionalFeedback.get().getId()).isPositive();
		assertThat(optionalFeedback.get().getCreated().toString()).hasToString("2021-12-28T12:20:41.298+01:00");
	}

	@Test
	void findByPartyIdEmptyResult() {
		final var optionalFeedback = feedbackRepository.findByPartyIdOptional("not a party id");

		assertThat(optionalFeedback).isNotPresent();
	}

	@Test()
	void persistWithNullValues() {
		assertThatThrownBy(() -> feedbackRepository.persist(new FeedbackEntity())).hasCauseInstanceOf(ConstraintViolationException.class);
	}
}
