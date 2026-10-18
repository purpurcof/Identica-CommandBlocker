package me.purpurcof.identica.addon.commandblocker.testing.journey;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.whereareiam.identica.model.config.type.DateTimePattern;

import java.io.IOException;

/**
 * Reads the one value type of Identica's messages that is not a plain value. The plugin registers its own module for
 * it, which is not part of the published API.
 */
final class DateTimePatternModule extends SimpleModule {
	DateTimePatternModule() {
		addDeserializer(DateTimePattern.class, new StdScalarDeserializer<>(DateTimePattern.class) {
			@Override
			public DateTimePattern deserialize(JsonParser parser, DeserializationContext context) throws IOException {
				String value = parser.getValueAsString();
				return value == null || value.isBlank() ? null : new DateTimePattern(value.trim());
			}
		});
	}
}