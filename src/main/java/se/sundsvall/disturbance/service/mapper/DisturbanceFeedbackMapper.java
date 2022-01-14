package se.sundsvall.disturbance.service.mapper;

import static java.lang.String.valueOf;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

public class DisturbanceFeedbackMapper {

	private DisturbanceFeedbackMapper() {}

	public static DisturbanceFeedbackEntity toDisturbanceFeedbackEntity(Category category, String disturbanceId, DisturbanceFeedbackCreateRequest request) {
		final var entity = new DisturbanceFeedbackEntity();
		entity.setCategory(valueOf(category));
		entity.setDisturbanceId(disturbanceId);
		entity.setPartyId(request.getPartyId());
		return entity;
	}
}
