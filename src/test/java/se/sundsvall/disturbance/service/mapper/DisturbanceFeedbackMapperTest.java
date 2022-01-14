package se.sundsvall.disturbance.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;

class DisturbanceFeedbackMapperTest {

	@Test
	void testToDisturbanceFeedbackEntity() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "1337";
		final var body = DisturbanceFeedbackCreateRequest.create().withPartyId(UUID.randomUUID().toString());

		final var result = DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity(category, disturbanceId, body);

		assertThat(result).isNotNull();
		assertThat(result.getCategory()).isEqualTo(String.valueOf(Category.COMMUNICATION));
		assertThat(result.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(result.getPartyId()).isEqualTo(body.getPartyId());
	}
}
