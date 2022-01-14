package se.sundsvall.disturbance.service.mapper;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static se.sundsvall.disturbance.service.util.DateUtils.toOffsetDateTimeWithLocalOffset;

import java.util.List;
import java.util.Objects;

import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

public class DisturbanceMapper {

	private DisturbanceMapper() {}

	public static Disturbance toDisturbance(DisturbanceEntity disturbanceEntity) {
		return Disturbance.create()
			.withCategory(disturbanceEntity.getCategory() != null ? Category.valueOf(disturbanceEntity.getCategory()) : null)
			.withTitle(disturbanceEntity.getTitle())
			.withDescription(disturbanceEntity.getDescription())
			.withId(disturbanceEntity.getDisturbanceId())
			.withDescription(disturbanceEntity.getDescription())
			.withAffecteds(toAffecteds(disturbanceEntity.getAffectedEntities()))
			.withStatus(disturbanceEntity.getStatus() != null ? Status.valueOf(disturbanceEntity.getStatus()) : null)
			.withCreated(disturbanceEntity.getCreated())
			.withPlannedStartDate(disturbanceEntity.getPlannedStartDate())
			.withPlannedStopDate(disturbanceEntity.getPlannedStopDate())
			.withUpdated(disturbanceEntity.getUpdated());
	}

	public static DisturbanceEntity toDisturbanceEntity(DisturbanceCreateRequest disturbanceCreateRequest) {
		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.replaceAffectedEntities(toAffectedEntities(disturbanceCreateRequest.getAffecteds()));
		disturbanceEntity.setCategory(String.valueOf(disturbanceCreateRequest.getCategory()));
		disturbanceEntity.setDescription(disturbanceCreateRequest.getDescription());
		disturbanceEntity.setDisturbanceId(disturbanceCreateRequest.getId());
		disturbanceEntity.setPlannedStartDate(toOffsetDateTimeWithLocalOffset(disturbanceCreateRequest.getPlannedStartDate()));
		disturbanceEntity.setPlannedStopDate(toOffsetDateTimeWithLocalOffset(disturbanceCreateRequest.getPlannedStopDate()));
		disturbanceEntity.setStatus(String.valueOf(disturbanceCreateRequest.getStatus()));
		disturbanceEntity.setTitle(disturbanceCreateRequest.getTitle());

		return disturbanceEntity;
	}

	public static DisturbanceEntity toDisturbanceEntity(Category category, String disturbanceId, DisturbanceUpdateRequest disturbanceUpdateRequest) {
		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.replaceAffectedEntities(toAffectedEntities(disturbanceUpdateRequest.getAffecteds()));
		disturbanceEntity.setCategory(String.valueOf(category));
		disturbanceEntity.setDescription(disturbanceUpdateRequest.getDescription());
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setPlannedStartDate(toOffsetDateTimeWithLocalOffset(disturbanceUpdateRequest.getPlannedStartDate()));
		disturbanceEntity.setPlannedStopDate(toOffsetDateTimeWithLocalOffset(disturbanceUpdateRequest.getPlannedStopDate()));
		disturbanceEntity.setStatus(disturbanceUpdateRequest.getStatus() != null ? String.valueOf(disturbanceUpdateRequest.getStatus()) : null);
		disturbanceEntity.setTitle(disturbanceUpdateRequest.getTitle());

		return disturbanceEntity;
	}

	/**
	 * Merge all new values from "newEntity" the the "oldEntity". Values are only used (copied) if they are not null.
	 * 
	 * @param oldEntity
	 * @param newEntity
	 * @return the old entity with available (non-null) values from the new entity.
	 */
	public static DisturbanceEntity toMergedDisturbanceEntity(DisturbanceEntity oldEntity, DisturbanceEntity newEntity) {
		ofNullable(newEntity.getAffectedEntities()).ifPresent(oldEntity::replaceAffectedEntities);
		ofNullable(newEntity.getDescription()).ifPresent(oldEntity::setDescription);
		ofNullable(newEntity.getPlannedStartDate()).ifPresent(oldEntity::setPlannedStartDate);
		ofNullable(newEntity.getPlannedStopDate()).ifPresent(oldEntity::setPlannedStopDate);
		ofNullable(newEntity.getStatus()).ifPresent(oldEntity::setStatus);
		ofNullable(newEntity.getTitle()).ifPresent(oldEntity::setTitle);

		return oldEntity;
	}

	private static List<AffectedEntity> toAffectedEntities(List<Affected> affecteds) {
		if (isNull(affecteds)) {
			return null;
		}

		return affecteds.stream()
			.filter(Objects::nonNull)
			.distinct() // Remove duplicates
			.map(DisturbanceMapper::toAffectedEntity)
			.collect(toList());
	}

	private static AffectedEntity toAffectedEntity(Affected affected) {
		final var affectedEntity = new AffectedEntity();
		affectedEntity.setPartyId(affected.getPartyId());
		affectedEntity.setReference(affected.getReference());

		return affectedEntity;
	}

	private static List<Affected> toAffecteds(List<AffectedEntity> affectedEntityList) {
		if (isNull(affectedEntityList)) {
			return null;
		}

		return affectedEntityList.stream()
			.filter(Objects::nonNull)
			.map(DisturbanceMapper::toAffected)
			.collect(toList());
	}

	private static Affected toAffected(AffectedEntity affectedEntity) {
		return Affected.create()
			.withPartyId(affectedEntity.getPartyId())
			.withReference(affectedEntity.getReference());
	}

	public static List<Disturbance> toDisturbances(List<DisturbanceEntity> disturbanceEntities) {
		return disturbanceEntities.stream()
			.filter(Objects::nonNull)
			.map(DisturbanceMapper::toDisturbance)
			.collect(toList());
	}
}
