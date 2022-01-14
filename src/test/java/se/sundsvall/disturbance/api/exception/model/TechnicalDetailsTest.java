package se.sundsvall.disturbance.api.exception.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class TechnicalDetailsTest {

	@Test
	void testBean() {
		assertThat(TechnicalDetails.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var detailsList = List.of("error1", "error2");
		final var rootCode = 418;
		final var rootCause = "The rootCause";
		final var serviceId = "The serviceId";

		final var technicalDetails = TechnicalDetails.create()
			.withDetails(detailsList)
			.withRootCause(rootCause)
			.withRootCode(rootCode)
			.withServiceId(serviceId);

		assertThat(technicalDetails).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(technicalDetails.getDetails()).isEqualTo(detailsList);
		assertThat(technicalDetails.getRootCode()).isEqualTo(rootCode);
		assertThat(technicalDetails.getRootCause()).isEqualTo(rootCause);
		assertThat(technicalDetails.getServiceId()).isEqualTo(serviceId);
	}
}
