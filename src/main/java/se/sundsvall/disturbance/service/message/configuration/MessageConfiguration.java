package se.sundsvall.disturbance.service.message.configuration;

import static org.apache.commons.lang3.StringUtils.lowerCase;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.service.message.configuration.MessageConfigurationMapping.CategoryConfig;

@ApplicationScoped
public class MessageConfiguration {

	@Inject
	MessageConfigurationMapping messageConfigurationMapping;

	public CategoryConfig getCategoryConfig(Category category) {
		return messageConfigurationMapping.template().get(lowerCase(category.name()));
	}
}
