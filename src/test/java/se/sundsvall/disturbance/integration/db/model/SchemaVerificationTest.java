package se.sundsvall.disturbance.integration.db.model;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SchemaVerificationTest {

	private static final String STORED_SCHEMA_FILE = "db/schema.sql";

	@ConfigProperty(name = "quarkus.hibernate-orm.scripts.generation.create-target")
	String generatedSchemaFile;

	@Test
	void verifySchemaUpdates() throws IOException {
		final var storedSchema = getResourceFile(STORED_SCHEMA_FILE);
		final var generatedSchema = getFile(generatedSchemaFile);

		removeInsertStatements(generatedSchema);

		assertThat(storedSchema).as(String.format("Please reflect modifications to entities in file: %s", STORED_SCHEMA_FILE))
			.hasSameTextualContentAs(generatedSchema);
	}

	private File getResourceFile(String fileName) {
		return new File(getClass().getClassLoader().getResource(fileName).getFile());
	}

	private File getFile(String fileName) {
		return new File(fileName);
	}

	private void removeInsertStatements(File file) throws IOException {
		final var lines = readLines(file, defaultCharset());
		final var updatedLines = lines.stream().filter(s -> !s.contains("INSERT")).collect(toList());
		writeLines(file, updatedLines, false);
	}
}
