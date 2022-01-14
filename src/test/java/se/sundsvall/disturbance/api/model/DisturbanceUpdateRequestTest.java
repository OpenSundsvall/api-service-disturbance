package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DisturbanceUpdateRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(DisturbanceUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var description = "description";
		final var affecteds = List.of(
			Affected.create().withPartyId(UUID.randomUUID().toString()),
			Affected.create().withPartyId(UUID.randomUUID().toString()));
		final var plannedStartDate = now();
		final var plannedStopDate = now().plusHours(1);
		final var status = Status.OPEN;
		final var title = "Title";

		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withDescription(description)
			.withAffecteds(affecteds)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withStatus(status)
			.withTitle(title);

		assertThat(disturbanceUpdateRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(disturbanceUpdateRequest.getDescription()).isEqualTo(description);
		assertThat(disturbanceUpdateRequest.getAffecteds()).isEqualTo(affecteds);
		assertThat(disturbanceUpdateRequest.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceUpdateRequest.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceUpdateRequest.getStatus()).isEqualTo(status);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DisturbanceUpdateRequest.create()).hasAllNullFieldsOrProperties();
	}
}
