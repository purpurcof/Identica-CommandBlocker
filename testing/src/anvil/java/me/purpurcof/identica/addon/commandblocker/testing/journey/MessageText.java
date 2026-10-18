package me.purpurcof.identica.addon.commandblocker.testing.journey;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A configured message as the player reads it: without formatting tags and blank lines. A placeholder matches any
 * text, so a message matches whoever it addresses.
 */
final class MessageText {

	private static final Pattern TAG = Pattern.compile("</?[a-zA-Z_#!?][^<>]*>");
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{[a-zA-Z_]+}");

	private final List<String> lines;

	/**
	 * @param configured a message as configured: one line or a list of lines
	 */
	MessageText(Object configured) {
		List<String> plain = new ArrayList<>();
		for (Object line : configured instanceof List<?> list ? list : List.of(configured)) {
			String text = TAG.matcher(String.valueOf(line)).replaceAll("").strip();
			if (!PLACEHOLDER.matcher(text).replaceAll("").isBlank()) plain.add(text);
		}

		if (plain.isEmpty()) throw new IllegalArgumentException("Message has no text: " + configured);

		lines = List.copyOf(plain);
	}

	/**
	 * Returns whether a received text contains every line of this message.
	 */
	boolean in(String received) {
		return lines.stream().allMatch(line -> pattern(line).matcher(received).find());
	}

	@Override
	public String toString() {
		return lines.toString();
	}

	private static Pattern pattern(String line) {
		StringBuilder expression = new StringBuilder();
		for (String literal : PLACEHOLDER.split(line, -1)) {
			if (!expression.isEmpty()) expression.append(".*");
			expression.append(Pattern.quote(literal));
		}

		return Pattern.compile(expression.toString(), Pattern.DOTALL);
	}
}