package se.sundsvall.disturbance.service.util;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.List;

import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

public class DisturbanceUtils {

	private DisturbanceUtils() {}

	/**
	 * Returns all removed AffectedEntity elements from oldDisturbanceEntity.getAffectedEntities(), when comparing with
	 * newDisturbanceEntity.getAffectedEntities().
	 * 
	 * E.g.
	 * 
	 * oldDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-2>, <ELEMENT-3>
	 * newDisturbanceEntity.getAffectedEntities() contains: <ELEMENT-1>, <ELEMENT-3>
	 * 
	 * Result: This method will return [<ELEMENT-2>]
	 * 
	 * @param oldDisturbanceEntity
	 * @param newDisturbanceEntity
	 * @return Returns the difference (removed elements) from oldDisturbanceEntity.
	 */
	public static List<AffectedEntity> getRemovedAffectedEntities(DisturbanceEntity oldDisturbanceEntity, DisturbanceEntity newDisturbanceEntity) {
		// If affectedEntities in newDisturbanceEntity isn't set (i.e. is null), just return an empty list.
		if (isNull(newDisturbanceEntity.getAffectedEntities())) {
			return emptyList();
		}
		return ofNullable(oldDisturbanceEntity.getAffectedEntities()).orElse(emptyList()).stream()
			.filter(oldEntity -> !existsInList(oldEntity, newDisturbanceEntity.getAffectedEntities()))
			.collect(toList());
	}

	private static boolean existsInList(AffectedEntity objectToCheck, List<AffectedEntity> list) {
		return ofNullable(list).orElse(emptyList()).stream()
			.anyMatch(entity -> equalsIgnoreCase(entity.getPartyId(), objectToCheck.getPartyId()) && equalsIgnoreCase(entity.getReference(), objectToCheck.getReference()));
	}
}
