package se.sundsvall.disturbance.service.message.configuration;

import java.util.Map;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "message")
public interface MessageConfigurationMapping {

	Map<String, CategoryConfig> template();

	public interface CategoryConfig {

		boolean active();

		String subjectClose();

		String subjectNew();

		String subjectUpdate();

		String messageClose();

		String messageNew();

		String messageUpdate();

		String senderEmailName();

		String senderEmailAddress();

		String senderSmsName();
	}
}
