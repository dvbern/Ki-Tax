package ch.dvbern.ebegu.services.zahlungen.infoma;

import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
public final class AsciiHelper {

	private AsciiHelper() {
	}

	@NonNull
	public static String transformToAsciiString(@NonNull String message) {
		message = replaceKnownNonAsciiCharacters(message);
		message = replaceUnknownNonAsciiCharacters(message);
		return message;
	}

	private static boolean isValidAscii(@NonNull String javaString) {
		final int length = javaString.length();
		for (int i = 0; i < length; ++i) {
			char ch = javaString.charAt(i);
			if (!isValidAsciiChar(ch)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidAsciiChar(char ch) {
		return CharMatcher.ascii().matches(ch);
	}

	@NonNull
	private static String replaceKnownNonAsciiCharacters(@NonNull String message) {
		message = message.replace("Ä", "Ae");
		message = message.replace("Á", "A");
		message = message.replace("Ă", "A");
		message = message.replace("À", "A");
		message = message.replace("Â", "A");
		message = message.replace("ä", "ae");
		message = message.replace("â", "a");
		message = message.replace("á", "a");
		message = message.replace("â", "a");
		message = message.replace("ą", "a");
		message = message.replace("ã", "a");
		message = message.replace("ǎ", "a");
		message = message.replace("Č", "C");
		message = message.replace("Ć", "C");
		message = message.replace("ć", "c");
		message = message.replace("ç", "c");
		message = message.replace("č", "c");
		message = message.replace("Đ", "D");
		message = message.replace("Ď", "D");
		message = message.replace("đ", "d");
		message = message.replace("Ë", "E");
		message = message.replace("Ê", "E");
		message = message.replace("È", "E");
		message = message.replace("é", "e");
		message = message.replace("è", "e");
		message = message.replace("ê", "e");
		message = message.replace("ē", "e");
		message = message.replace("ě", "e");
		message = message.replace("ễ", "e");
		message = message.replace("ę", "e");
		message = message.replace("ě", "e");
		message = message.replace("ė", "e");
		message = message.replace("ë", "e");
		message = message.replace("ê", "e");
		message = message.replace("ğ", "g");
		message = message.replace("İ", "I");
		message = message.replace("ï", "i");
		message = message.replace("ı", "i");
		message = message.replace("î", "i");
		message = message.replace("Í", "i");
		message = message.replace("í", "i");
		message = message.replace("ķ", "k");
		message = message.replace("Ľ", "L");
		message = message.replace("Ł", "L");
		message = message.replace("ľ", "l");
		message = message.replace("ł", "l");
		message = message.replace("ň", "n");
		message = message.replace("ń", "n");
		message = message.replace("Ö", "Oe");
		message = message.replace("Ó", "O");
		message = message.replace("Õ", "O");
		message = message.replace("Ò", "O");
		message = message.replace("ö", "oe");
		message = message.replace("ô", "o");
		message = message.replace("ọ", "o");
		message = message.replace("ő", "o");
		message = message.replace("ō", "o");
		message = message.replace("ô", "o");
		message = message.replace("ó", "o");
		message = message.replace("õ", "o");
		message = message.replace("õ", "o");
		message = message.replace("Ř", "R");
		message = message.replace("ř", "r");
		message = message.replace("Š", "S");
		message = message.replace("Ş", "S");
		message = message.replace("š", "s");
		message = message.replace("ś", "s");
		message = message.replace("ș", "s");
		message = message.replace("ş", "s");
		message = message.replace("Ü", "Ue");
		message = message.replace("Ú", "U");
		message = message.replace("ü", "ue");
		message = message.replace("û", "u");
		message = message.replace("ú", "u");
		message = message.replace("ű", "u");
		message = message.replace("ū", "u");
		message = message.replace("ű", "u");
		message = message.replace("ý", "y");
		message = message.replace("Ž", "Z");
		message = message.replace("Ż", "Z");
		message = message.replace("Ż", "Z");
		message = message.replace("ž", "z");
		message = message.replace("ź", "z");
		message = message.replace("ż", "z");
		message = message.replace("ż", "z");
		message = message.replace("–", "-");
		message = message.replace("—", "-");
		message = message.replace("’", "'");
		message = message.replace("‘", "'");
		message = message.replace("`", "'");
		message = message.replace("´", "'");
		message = message.replace("ˈ", "'");
		message = message.replace("“", "\"");
		message = message.replace("”", "\"");
		message = message.replace("„", "\"");
		message = message.replace("‟", "\"");
		message = message.replace("\u200B", " ");
		message = message.replace("\u202A", " ");
		message = message.replace("\u202C", " ");
		message = message.replace("\u200F", " ");
		message = message.replace("\u00A0", " ");

		return message;
	}

	@NonNull
	private static String replaceUnknownNonAsciiCharacters(@NonNull String javaString) {
		// Schnelltest, wenn alles ASCII ist, lassen wir den String unveraendert
		if (isValidAscii(javaString)) {
			return javaString;
		}

		// Sonst: alle Zeichen ausserhalb ASCII durch '?' ersetzen und loggen, damit wir sie noch in die Liste von bekannten Ausnahmen aufnehmen koennen
		StringBuilder sb = new StringBuilder();

		final int length = javaString.length();
		for (int i = 0; i < length; ++i) {
			char ch = javaString.charAt(i);
			if (isValidAsciiChar(ch)) {
				sb.append(ch);
			} else {
				sb.append('?');
				LOG.warn("KIBON-INFOMA: Unknown non-ascii character found: {}", ch);
			}
		}
		return sb.toString();
	}
}
