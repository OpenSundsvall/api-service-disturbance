package se.sundsvall.disturbance.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

@ExtendWith(MockitoExtension.class)
class DisturbanceMapperTest {

	@Test
	void toDisturbanceSuccess() {

		final var affectedEntity1 = new AffectedEntity();
		affectedEntity1.setPartyId("partyId-1");
		affectedEntity1.setReference("reference-1");

		final var affectedEntity2 = new AffectedEntity();
		affectedEntity2.setPartyId("partyId-2");
		affectedEntity2.setReference("reference-2");

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setDisturbanceId("disturbanceId");
		disturbanceEntity.setCategory("COMMUNICATION");
		disturbanceEntity.setDescription("description");
		disturbanceEntity.setTitle("title");
		disturbanceEntity.setStatus("OPEN");
		disturbanceEntity.setPlannedStartDate(now().plusDays(1));
		disturbanceEntity.setPlannedStopDate(now().plusDays(2));
		disturbanceEntity.setCreated(now());
		disturbanceEntity.setAffectedEntities(List.of(affectedEntity1, affectedEntity2));

		final var disturbance = DisturbanceMapper.toDisturbance(disturbanceEntity);

		assertThat(disturbance.getCategory()).isEqualTo(Category.COMMUNICATION);
		assertThat(disturbance.getId()).isEqualTo("disturbanceId");
		assertThat(disturbance.getDescription()).isEqualTo("description");
		assertThat(disturbance.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN);
		assertThat(disturbance.getPlannedStartDate()).isCloseTo(now().plusDays(1), within(2, SECONDS));
		assertThat(disturbance.getPlannedStopDate()).isCloseTo(now().plusDays(2), within(2, SECONDS));
		assertThat(disturbance.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(disturbance.getAffecteds().size()).isEqualTo(2);
		assertThat(disturbance.getAffecteds().get(0).getPartyId()).isEqualTo("partyId-1");
		assertThat(disturbance.getAffecteds().get(0).getReference()).isEqualTo("reference-1");
		assertThat(disturbance.getAffecteds().get(1).getPartyId()).isEqualTo("partyId-2");
		assertThat(disturbance.getAffecteds().get(1).getReference()).isEqualTo("reference-2");
	}

	@Test
	void toDisturbanceEntityFromDisturbanceCreateRequest() {

		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withDescription("Description")
			.withId("id")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")))
			.withPlannedStartDate(now())
			.withPlannedStopDate(now().plusDays(1))
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("Title");

		final var disturbanceEntity = DisturbanceMapper.toDisturbanceEntity(disturbanceCreateRequest);

		assertThat(disturbanceEntity.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntity.getCategory()).isEqualTo(Category.COMMUNICATION.toString());
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo("id");
		assertThat(disturbanceEntity.getPlannedStartDate()).isCloseTo(now(), within(2, SECONDS));
		assertThat(disturbanceEntity.getPlannedStopDate()).isCloseTo(now().plusDays(1), within(2, SECONDS));
		assertThat(disturbanceEntity.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
		assertThat(disturbanceEntity.getTitle()).isEqualTo("Title");
	}

	@Test
	void toDisturbanceEntityFromDisturbanceUpdateRequest() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbanceId";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withDescription("Description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")))
			.withPlannedStartDate(now())
			.withPlannedStopDate(now().plusDays(1))
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN);

		final var disturbanceEntity = DisturbanceMapper.toDisturbanceEntity(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(disturbanceEntity.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntity.getCategory()).isEqualTo(category.toString());
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntity.getPlannedStartDate()).isCloseTo(now(), within(2, SECONDS));
		assertThat(disturbanceEntity.getPlannedStopDate()).isCloseTo(now().plusDays(1), within(2, SECONDS));
		assertThat(disturbanceEntity.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
		assertThat(disturbanceEntity.getTitle()).isNull();
	}

	@Test
	void toMergedDisturbanceEntityAllNewValuesSet() {

		/**
		 * Set up old entity.
		 */
		final var oldAffected1 = new AffectedEntity();
		oldAffected1.setPartyId("oldpartyId-1");
		oldAffected1.setReference("oldReference-1");

		final var oldAffected2 = new AffectedEntity();
		oldAffected2.setPartyId("oldPartyId-2");
		oldAffected2.setReference("oldReference-2");

		final var oldAffected3 = new AffectedEntity();
		oldAffected3.setPartyId("oldPartyId-3");
		oldAffected3.setReference("oldReference-3");

		final var oldEntity = new DisturbanceEntity();
		oldEntity.setId(1L);
		oldEntity.setDisturbanceId("oldDisturbanceId");
		oldEntity.setCategory("oldStatus");
		oldEntity.setDescription("oldDescription");
		oldEntity.setTitle("oldTitle");
		oldEntity.setStatus("oldStatus");
		oldEntity.setPlannedStartDate(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setPlannedStopDate(now().plusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setCreated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setUpdated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setAffectedEntities(new ArrayList<>(List.of(oldAffected1, oldAffected2, oldAffected3)));

		/**
		 * Set up new entity.
		 */
		final var newAffected1 = new AffectedEntity();
		newAffected1.setPartyId("newPartyId-1");
		newAffected1.setReference("newReference-1");

		final var newAffected2 = new AffectedEntity();
		newAffected2.setPartyId("newPartyId-2");
		newAffected2.setReference("newReference-2");

		final var newEntity = new DisturbanceEntity();
		newEntity.setId(0L);
		newEntity.setDisturbanceId("newDisturbanceId");
		newEntity.setCategory("newStatus");
		newEntity.setDescription("newDescription");
		newEntity.setTitle("newTitle");
		newEntity.setStatus("newStatus");
		newEntity.setPlannedStartDate(now().minusDays(RandomUtils.nextInt(1, 1000)));
		newEntity.setPlannedStopDate(now().plusDays(RandomUtils.nextInt(1, 1000)));
		newEntity.setCreated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		newEntity.setUpdated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		newEntity.setAffectedEntities(new ArrayList<>(List.of(newAffected1, newAffected2)));

		final var mergedDisturbanceEntity = DisturbanceMapper.toMergedDisturbanceEntity(oldEntity, newEntity);

		assertThat(mergedDisturbanceEntity).isNotNull();
		assertThat(mergedDisturbanceEntity.getAffectedEntities()).isEqualTo(List.of(newAffected1, newAffected2));
		assertThat(mergedDisturbanceEntity.getCategory()).isEqualTo(oldEntity.getCategory());
		assertThat(mergedDisturbanceEntity.getCreated()).isEqualTo(oldEntity.getCreated());
		assertThat(mergedDisturbanceEntity.getDescription()).isEqualTo(newEntity.getDescription());
		assertThat(mergedDisturbanceEntity.getDisturbanceId()).isEqualTo(oldEntity.getDisturbanceId());
		assertThat(mergedDisturbanceEntity.getId()).isEqualTo(oldEntity.getId());
		assertThat(mergedDisturbanceEntity.getPlannedStartDate()).isEqualTo(newEntity.getPlannedStartDate());
		assertThat(mergedDisturbanceEntity.getPlannedStopDate()).isEqualTo(newEntity.getPlannedStopDate());
		assertThat(mergedDisturbanceEntity.getStatus()).isEqualTo(newEntity.getStatus());
		assertThat(mergedDisturbanceEntity.getTitle()).isEqualTo(newEntity.getTitle());
		assertThat(mergedDisturbanceEntity.getUpdated()).isEqualTo(oldEntity.getUpdated());
	}

	@Test
	void toMergedDisturbanceEntityNoNewValuesSet() {

		/**
		 * Set up old entity.
		 */
		final var oldAffected1 = new AffectedEntity();
		oldAffected1.setPartyId("oldPartyId-1");
		oldAffected1.setReference("oldReference-1");

		final var oldAffected2 = new AffectedEntity();
		oldAffected2.setPartyId("oldPartyId-2");
		oldAffected2.setReference("oldReference-2");

		final var oldAffected3 = new AffectedEntity();
		oldAffected3.setPartyId("oldPartyId-3");
		oldAffected3.setReference("oldReference-3");

		final var oldEntity = new DisturbanceEntity();
		oldEntity.setId(1L);
		oldEntity.setDisturbanceId("oldDisturbanceId");
		oldEntity.setCategory("oldStatus");
		oldEntity.setDescription("oldDescription");
		oldEntity.setTitle("oldTitle");
		oldEntity.setStatus("oldStatus");
		oldEntity.setPlannedStartDate(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setPlannedStopDate(now().plusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setCreated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setUpdated(now().minusDays(RandomUtils.nextInt(1, 1000)));
		oldEntity.setAffectedEntities(new ArrayList<>(List.of(oldAffected1, oldAffected2, oldAffected3)));

		final var mergedDisturbanceEntity = DisturbanceMapper.toMergedDisturbanceEntity(oldEntity, new DisturbanceEntity());

		assertThat(mergedDisturbanceEntity)
			.isNotNull()
			.isEqualTo(oldEntity);
	}
}
