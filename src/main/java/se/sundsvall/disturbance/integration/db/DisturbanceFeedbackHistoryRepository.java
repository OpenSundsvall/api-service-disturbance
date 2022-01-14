package se.sundsvall.disturbance.integration.db;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackHistoryEntity;

@ApplicationScoped
public class DisturbanceFeedbackHistoryRepository implements PanacheRepository<DisturbanceFeedbackHistoryEntity> {

	static final String STATUS_SENT = "SENT";

	public void persistWithStatusSent(DisturbanceFeedbackEntity orderFeedbackEntity) {
		final var orderFeedbackHistory = new DisturbanceFeedbackHistoryEntity();
		orderFeedbackHistory.setPartyId(orderFeedbackEntity.getPartyId());
		orderFeedbackHistory.setDisturbanceId(orderFeedbackEntity.getDisturbanceId());
		orderFeedbackHistory.setCategory(orderFeedbackEntity.getCategory());
		orderFeedbackHistory.setStatus(STATUS_SENT);

		persist(orderFeedbackHistory);
	}
}
