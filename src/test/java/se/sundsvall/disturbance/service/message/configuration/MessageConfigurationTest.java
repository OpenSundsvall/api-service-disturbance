package se.sundsvall.disturbance.service.message.configuration;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.quarkus.test.junit.QuarkusTest;
import se.sundsvall.disturbance.api.model.Category;

@QuarkusTest
class MessageConfigurationTest {

	@Inject
	MessageConfiguration messageConfiguration;

	@ParameterizedTest
	@EnumSource(Category.class) // Passing all categories
	void configExistsForAllCategories(Category category) {

		final var categoryConfig = messageConfiguration.getCategoryConfig(category);

		assertThat(categoryConfig)
			.describedAs("Missing one or more properties for config group: 'message.template.%s'", lowerCase(category.name()))
			.isNotNull()
			.hasNoNullFieldsOrProperties();
	}
}
