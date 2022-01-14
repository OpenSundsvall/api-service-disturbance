package se.sundsvall.disturbance.integration.db;

import static java.lang.String.valueOf;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

@ApplicationScoped
public class DisturbanceFeedbackRepository implements PanacheRepository<DisturbanceFeedbackEntity> {

	private static final String CATEGORY_PARAM = "category";
	private static final String DISTURBANCE_ID_PARAM = "disturbanceId";
	private static final String PARTY_ID_PARAM = "partyId";

	public List<DisturbanceFeedbackEntity> findByCategoryAndDisturbanceId(Category category, String disturbanceId) {
		return list("disturbanceId = :disturbanceId and category = :category",
			Parameters.with(DISTURBANCE_ID_PARAM, disturbanceId).and(CATEGORY_PARAM, valueOf(category)));
	}

	public Optional<DisturbanceFeedbackEntity> findByCategoryAndDisturbanceIdAndPartyIdOptional(Category category, String disturbanceId, String partyId) {
		return find("disturbanceId = :disturbanceId and category = :category and partyId = :partyId",
			Parameters.with(DISTURBANCE_ID_PARAM, disturbanceId).and(CATEGORY_PARAM, valueOf(category)).and(PARTY_ID_PARAM, partyId)).firstResultOptional();
	}

	public List<DisturbanceFeedbackEntity> findByPartyId(String partyId) {
		return list(PARTY_ID_PARAM, partyId);
	}

	public long deleteByCategoryAndDisturbanceId(Category category, String disturbanceId) {
		return delete("disturbanceId = :disturbanceId and category = :category",
			Parameters.with(DISTURBANCE_ID_PARAM, disturbanceId).and(CATEGORY_PARAM, valueOf(category)));
	}
}
