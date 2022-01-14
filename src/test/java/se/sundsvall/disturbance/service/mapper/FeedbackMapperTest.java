package se.sundsvall.disturbance.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;

class FeedbackMapperTest {

	@Test
	void testToFeedbackEntity() {

		final var body = FeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		final var result = FeedbackMapper.toFeedbackEntity(body);

		assertThat(result).isNotNull();
		assertThat(result.getPartyId()).isEqualTo(body.getPartyId());
	}
}
