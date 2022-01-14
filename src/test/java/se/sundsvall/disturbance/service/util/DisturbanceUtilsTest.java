package se.sundsvall.disturbance.service.util;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

class DisturbanceUtilsTest {

	@Test
	void getRemovedAffectedEntitiesWhenElementsRemoved() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId("partyId-3");
		affectedEntity3.setReference("reference-3");

		final var affectedEntity4 = new AffectedEntity();
		affectedEntity4.setPartyId("partyId-4");
		affectedEntity4.setReference("reference-4");

		final var affectedEntity5 = new AffectedEntity();
		affectedEntity5.setPartyId("partyId-5");
		affectedEntity5.setReference("reference-5");

		final var affectedEntity6 = new AffectedEntity();
		affectedEntity6.setPartyId("partyId-6");
		affectedEntity6.setReference("reference-6");

		final var disturbanceEntity1 = new DisturbanceEntity();
		disturbanceEntity1.setAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3, affectedEntity4, affectedEntity5, affectedEntity6));

		final var disturbanceEntity2 = new DisturbanceEntity();
		// The following elements are removed compared to disturbanceEntity1: affectedEntity2, affectedEntity4, affectedEntity5
		disturbanceEntity2.setAffectedEntities(List.of(affectedEntity1, affectedEntity3, affectedEntity6));

		final var result = DisturbanceUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(3)
			.containsExactly(affectedEntity2, affectedEntity4, affectedEntity5);
	}

	@Test
	void getRemovedAffectedEntitiesWhenNoElementsAreRemovedButCaseDiffers() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId("partyId-3");
		affectedEntity3.setReference("reference-3");

		// Same as above, but different case.
		final var affectedEntityOtherCase1 = new AffectedEntity();
		affectedEntityOtherCase1.setPartyId("PartyId-1");
		affectedEntityOtherCase1.setReference("Reference-1");

		final var affectedEntityOtherCase2 = new AffectedEntity();
		affectedEntityOtherCase2.setPartyId("PARTYID-2");
		affectedEntityOtherCase2.setReference("REFERENCE-2");

		final var affectedEntityOtherCase3 = new AffectedEntity();
		affectedEntityOtherCase3.setPartyId("partyid-3");
		affectedEntityOtherCase3.setReference("reference-3");

		final var disturbanceEntity1 = new DisturbanceEntity();
		disturbanceEntity1.setAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3));

		final var disturbanceEntity2 = new DisturbanceEntity();
		// All elements have different case, but is equal otherwise.
		disturbanceEntity2.setAffectedEntities(List.of(affectedEntityOtherCase1, affectedEntityOtherCase2, affectedEntityOtherCase3));

		final var result = DisturbanceUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenElementsAdded() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var affectedEntity3 = new AffectedEntity();
		affectedEntity3.setPartyId("partyId-3");
		affectedEntity3.setReference("reference-3");

		final var affectedEntity4 = new AffectedEntity();
		affectedEntity4.setPartyId("partyId-4");
		affectedEntity4.setReference("reference-4");

		final var affectedEntity5 = new AffectedEntity();
		affectedEntity5.setPartyId("partyId-5");
		affectedEntity5.setReference("reference-5");

		final var affectedEntity6 = new AffectedEntity();
		affectedEntity6.setPartyId("partyId-6");
		affectedEntity6.setReference("reference-6");

		final var disturbanceEntity1 = new DisturbanceEntity();
		disturbanceEntity1.setAffectedEntities(List.of(affectedEntity1, affectedEntity3, affectedEntity6));

		final var disturbanceEntity2 = new DisturbanceEntity();
		// The following elements are added compared to disturbanceEntity1: affectedEntity2, affectedEntity4, affectedEntity5
		disturbanceEntity2.setAffectedEntities(List.of(affectedEntity1, affectedEntity2, affectedEntity3, affectedEntity4, affectedEntity5, affectedEntity6));

		final var result = DisturbanceUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenNewEntitiesIsNull() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var disturbanceEntity1 = new DisturbanceEntity();
		final var disturbanceEntity2 = new DisturbanceEntity();

		disturbanceEntity1.setAffectedEntities(List.of(affectedEntity1, affectedEntity2));

		final var result = DisturbanceUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getRemovedAffectedEntitiesWhenNewEntitiesIsEmptyList() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var disturbanceEntity1 = new DisturbanceEntity();
		final var disturbanceEntity2 = new DisturbanceEntity();

		disturbanceEntity1.setAffectedEntities(List.of(affectedEntity1, affectedEntity2));
		disturbanceEntity2.setAffectedEntities(emptyList());

		final var result = DisturbanceUtils.getRemovedAffectedEntities(disturbanceEntity1, disturbanceEntity2);

		assertThat(result)
			.isNotNull()
			.hasSize(2)
			.containsExactly(affectedEntity1, affectedEntity2);
	}
}
