package se.sundsvall.disturbance.integration.db;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.disturbance.api.model.Category.COMMUNICATION;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;

/**
 * Disturbance repository tests.
 * 
 * @see src/test/resources/db/testdata.sql for data setup.
 */
@QuarkusTest
@TestTransaction
class DisturbanceRepositoryTest {

	private static final String DISTURBANCE_ID_2 = "disturbance-2";
	private static final String PARTY_ID_1 = "0d64beb2-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".
	private static final String PARTY_ID_2 = "0d64c132-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".
	private static final String PARTY_ID_3 = "0d64c42a-3aea-11ec-8d3d-0242ac130003"; // Exists in "disturbance-2".

	@Inject
	DisturbanceRepository disturbanceRepository;

	@Test
	void findByDisturbanceIdAndCategory() {
		final var disturbanceOptional = disturbanceRepository.findByCategoryAndDisturbanceIdOptional(COMMUNICATION, DISTURBANCE_ID_2);

		assertThat(disturbanceOptional).isPresent();
		assertAsDisturbanceEntity2(disturbanceOptional.get());
	}

	@Test
	void persistAndFetch() {

		final var disturbanceEntity = setupNewDisturbanceEntity("persistAndFetch-disturbanceId");

		final var disturbance = disturbanceRepository.persistAndFetch(disturbanceEntity);
		assertThat(disturbance.getId()).isPositive();
		assertThat(disturbance.getAffectedEntities()).hasSize(1);
		assertThat(disturbance.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1");
		assertThat(disturbance.getDisturbanceId()).isEqualTo("persistAndFetch-disturbanceId");
		assertThat(disturbance.getCategory()).isEqualTo(COMMUNICATION.toString());
		assertThat(disturbance.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(disturbance.getDeleted()).isFalse();
		assertThat(disturbance.getDescription()).isEqualTo("description");
		assertThat(disturbance.getPlannedStartDate()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(disturbance.getPlannedStopDate()).isCloseTo(OffsetDateTime.now().plusDays(6), within(2, SECONDS));
		assertThat(disturbance.getStatus()).isEqualTo(OPEN.toString());
	}

	@Test
	void persistAndFetchReplaceAffected() {

		// Create new entity.
		var disturbanceEntity = setupNewDisturbanceEntity("persistAndFetchReplaceAffected-disturbanceId");
		disturbanceEntity = disturbanceRepository.persistAndFetch(disturbanceEntity);

		// Assert affected.
		assertThat(disturbanceEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1");

		// Replace with new affectedEntities.
		final var affectedNew1 = new AffectedEntity();
		affectedNew1.setPartyId("new-partyId-1");
		final var affectedNew2 = new AffectedEntity();
		affectedNew2.setPartyId("new-partyId-2");
		disturbanceEntity.replaceAffectedEntities(Arrays.asList(affectedNew1, affectedNew2));
		disturbanceEntity = disturbanceRepository.persistAndFetch(disturbanceEntity);

		// Assert that everything was persisted correct.
		assertThat(disturbanceEntity.getAffectedEntities())
			.hasSize(2)
			.extracting(AffectedEntity::getPartyId)
			.containsExactlyInAnyOrder("new-partyId-1", "new-partyId-2");
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithCategoryFilterAndStatusFilter() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_1, List.of(COMMUNICATION), List.of(OPEN));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithEmptyCategoryAndEmptyStatus() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_2, emptyList(), emptyList());
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithMultipleCategoriesAndMultipleStatuses() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_2, List.of(COMMUNICATION, ELECTRICITY), List.of(OPEN, CLOSED));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithCategoryFilter() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_1, List.of(COMMUNICATION), null);
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithStatusFilter() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_2, null, List.of(OPEN));
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	@Test
	void findByPartyIdFilterByCategoryAndStatusWithNoStatusFilterAndNoCategoryFilter() {
		final var disturbances = disturbanceRepository.findByPartyIdFilterByCategoryAndStatus(PARTY_ID_2, null, null);
		assertThat(disturbances)
			.isNotEmpty()
			.hasSize(1)
			.allSatisfy(this::assertAsDisturbanceEntity2);
	}

	private void assertAsDisturbanceEntity2(DisturbanceEntity disturbanceEntity) {

		assertThat(disturbanceEntity.getId()).isEqualTo(2);
		assertThat(disturbanceEntity.getCategory()).isEqualTo(COMMUNICATION.toString());
		assertThat(disturbanceEntity.getCreated()).isEqualTo(getOffsetDateTime(2021, 9, 23, 9, 05, 48, 198000000));
		assertThat(disturbanceEntity.getDescription()).isEqualTo("Description");
		assertThat(disturbanceEntity.getDisturbanceId()).isEqualTo("disturbance-2");
		assertThat(disturbanceEntity.getPlannedStartDate()).isEqualTo(getOffsetDateTime(2021, 12, 31, 11, 30, 45, 0));
		assertThat(disturbanceEntity.getPlannedStopDate()).isEqualTo(getOffsetDateTime(2022, 01, 11, 11, 30, 45, 0));

		assertThat(disturbanceEntity.getStatus()).isEqualTo(OPEN.toString());
		assertThat(disturbanceEntity.getTitle()).isEqualTo("Title");
		assertThat(disturbanceEntity.getUpdated()).isNull();
		assertThat(disturbanceEntity.getAffectedEntities()).hasSize(3);
		// Affected 1
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getPartyId()).isEqualTo(PARTY_ID_1);
		assertThat(disturbanceEntity.getAffectedEntities().get(0).getReference()).isEqualTo("Streetname 11");
		// Affected 2
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getPartyId()).isEqualTo(PARTY_ID_2);
		assertThat(disturbanceEntity.getAffectedEntities().get(1).getReference()).isEqualTo("Streetname 22");
		// Affected 3
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getPartyId()).isEqualTo(PARTY_ID_3);
		assertThat(disturbanceEntity.getAffectedEntities().get(2).getReference()).isEqualTo("Streetname 33");
	}

	private static OffsetDateTime getOffsetDateTime(int year, int month, int day, int hour, int minute, int second, int nanoOfSecond) {
		var utcOffsetDateTime = OffsetDateTime.of(year, month, day, hour, minute, second, nanoOfSecond, ZoneOffset.UTC);
		ZoneOffset currentOffset = ZoneId.systemDefault().getRules().getOffset(utcOffsetDateTime.toInstant());

		return OffsetDateTime.of(year, month, day, hour, minute, second, nanoOfSecond, currentOffset);
	}

	private DisturbanceEntity setupNewDisturbanceEntity(String disturbanceId) {
		final var affectedEntity = new AffectedEntity();
		affectedEntity.setPartyId("partyId-1");

		final var entity = new DisturbanceEntity();
		entity.setDisturbanceId(disturbanceId);
		entity.setCategory(COMMUNICATION.toString());
		entity.setTitle("title");
		entity.setDescription("description");
		entity.setStatus(OPEN.toString());
		entity.setPlannedStartDate(OffsetDateTime.now());
		entity.setPlannedStopDate(OffsetDateTime.now().plusDays(6));
		entity.addAffectedEntities(Arrays.asList(affectedEntity));

		return entity;
	}
}
