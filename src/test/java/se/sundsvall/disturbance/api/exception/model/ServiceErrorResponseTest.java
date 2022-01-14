package se.sundsvall.disturbance.api.exception.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class ServiceErrorResponseTest {

	@Test
	void testBean() {
		assertThat(ServiceErrorResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var httpCode = 404;
		final var message = "Nothing was found here";
		final var technicalDetails = TechnicalDetails.create();

		final var serviceErrorResponse = ServiceErrorResponse.create()
			.withHttpCode(httpCode)
			.withMessage(message)
			.withTechnicalDetails(technicalDetails);

		assertThat(serviceErrorResponse).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(serviceErrorResponse.getHttpCode()).isEqualTo(httpCode);
		assertThat(serviceErrorResponse.getMessage()).isEqualTo(message);
		assertThat(serviceErrorResponse.getTechnicalDetails()).isEqualTo(technicalDetails);
	}
}
