package ch.dvbern.ebegu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

import ch.dvbern.ebegu.util.Constants;
import org.apache.xmlbeans.impl.util.Base64;

/**
 * This is a helper that is able to replace an uuid in a db unit script with its equivalent base 64 encoded binary value
 * Usefull for migrating dbunit datasets to a binary uuid id column
 * */
@SuppressWarnings("PMD.SystemPrintln")
public class UUIDToBase64TestHelper {

	private UUIDToBase64TestHelper() {
		//util
	}

	private static List<String> lineList;

	public static void main(String[] args) throws IOException {

		String path = "/home/homa/java/ideaprojects/jugendamt/ebegu-test/ebegu/ebegu-testutils/src/main/resources/datasets/massenversand-dataset.xml";
		if(args.length > 0){
			path = args[0];
		}
		replaceUUIDSInfile(path);
	}

	private static void replaceUUIDSInfile(String path) throws IOException {
		String testsimple = "0621fb5d-a187-5a91-abaf-8a813c4d2aaa";
		String testLine = "<gesuchsperiode id=\"0621fb5d-a187-5a91-abaf-8a813c4d2aaa\"";
		final boolean simpleMatch = testsimple.matches(Constants.REGEX_UUID);
		System.out.println("simple match " + simpleMatch);
		final boolean complexMatch = testLine.matches(Constants.REGEX_UUID);
		System.out.println("complex match " + complexMatch);

		lineList = Files.lines(Paths.get(path)).collect(Collectors.toList());

		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(Constants.REGEX_UUID);
		for (String s : lineList) {

			Matcher matcher = p.matcher(s);

			while (matcher.find()) {

				String uuidMatch = matcher.group(2);
				String base64EncodedUUID = uuidToBase64(uuidMatch);
				matcher.appendReplacement(sb, "$1" + base64EncodedUUID + "$3");
			}
			matcher.appendTail(sb);
			sb.append("\n");

		}
		System.out.println(sb);
	}

	@Nonnull
	private static String uuidToBase64(String uuidMatch) {
		final String changedUUID = uuidMatch.replaceAll("-", "");
		final byte[] bytes = DatatypeConverter.parseHexBinary(changedUUID);
		return new String(Base64.encode(bytes));
	}

}
