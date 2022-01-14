package se.sundsvall.disturbance.integration.db;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

@ApplicationScoped
public class DisturbanceRepository implements PanacheRepository<DisturbanceEntity> {

	public Optional<DisturbanceEntity> findByCategoryAndDisturbanceIdOptional(Category category, String disturbanceId) {
		return find("disturbanceId = :disturbanceId and category = :category and deleted = false",
			Parameters.with("disturbanceId", disturbanceId).and("category", valueOf(category))).firstResultOptional();
	}

	public List<DisturbanceEntity> findByPartyIdFilterByCategoryAndStatus(String partyId, List<Category> categoryFilter, List<Status> statusFilter) {

		// Convert from List of enums to list of strings.
		final var categoryFilterStrings = toStringList(categoryFilter);
		final var statusFilterStrings = toStringList(statusFilter);

		if (isNotEmpty(statusFilterStrings) && isNotEmpty(categoryFilterStrings)) {
			return list("SELECT d FROM DisturbanceEntity d JOIN d.affectedEntities a WHERE d.category IN ?1 AND d.status IN ?2 AND a.partyId = ?3 AND d.deleted = false",
				categoryFilterStrings, statusFilterStrings, partyId);
		} else if (isNotEmpty(categoryFilterStrings)) {
			return list("SELECT d FROM DisturbanceEntity d JOIN d.affectedEntities a WHERE d.category IN ?1 AND a.partyId = ?2 AND d.deleted = false",
				categoryFilterStrings, partyId);
		} else if (isNotEmpty(statusFilterStrings)) {
			return list("SELECT d FROM DisturbanceEntity d JOIN d.affectedEntities a WHERE d.status IN ?1 AND a.partyId = ?2 AND d.deleted = false",
				statusFilterStrings, partyId);
		}
		return list("SELECT d FROM DisturbanceEntity d JOIN d.affectedEntities a WHERE a.partyId = ?1 AND d.deleted = false", partyId);
	}

	public DisturbanceEntity persistAndFetch(DisturbanceEntity disturbanceEntity) {
		this.persistAndFlush(disturbanceEntity);
		return this.findById(disturbanceEntity.getId());
	}

	private List<String> toStringList(List<? extends Enum<?>> enumList) {
		return ofNullable(enumList).orElse(emptyList()).stream().map(Enum::name).collect(toList());
	}
}
