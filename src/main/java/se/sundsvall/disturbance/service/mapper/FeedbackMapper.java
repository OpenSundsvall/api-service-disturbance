package se.sundsvall.disturbance.service.mapper;

import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

public class FeedbackMapper {

	private FeedbackMapper() {}

	public static FeedbackEntity toFeedbackEntity(FeedbackCreateRequest request) {
		final var entity = new FeedbackEntity();
		entity.setPartyId(request.getPartyId());
		return entity;
	}
}
