package se.sundsvall.disturbance.integration.db;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

@ApplicationScoped
public class FeedbackRepository implements PanacheRepository<FeedbackEntity> {

	public Optional<FeedbackEntity> findByPartyIdOptional(String partyId) {
		return find("partyId", partyId).firstResultOptional();
	}
}
