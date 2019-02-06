package ch.dvbern.ebegu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import ch.dvbern.ebegu.util.Constants;
import org.apache.xmlbeans.impl.util.Base64;

/**
 * This is a helper that is able to replace an uuid in a db unit script with its equivalent base 64 encoded binary value
 * Usefull for migrating dbunit datasets to a binary uuid id column
 * */
public class UUIDToBase64TestHelper {

	private static List<String> lineList;

	public static void main(String[] args) throws IOException {
		String testsimple = "0621fb5d-a187-5a91-abaf-8a813c4d2aaa";
		String test = "<gesuchsperiode id=\"0621fb5d-a187-5a91-abaf-8a813c4d2aaa\"";
		final boolean simpleMatch = testsimple.matches(Constants.REGEX_UUID);
		System.out.println("simple match " + simpleMatch);
		final boolean complexMatch = test.matches(Constants.REGEX_UUID);
		System.out.println("complex match " + complexMatch);

		lineList = Files.lines(Paths.get("/home/homa/java/ideaprojects/jugendamt/ebegu-test/ebegu/ebegu-testutils/src/main/resources"
			+ "/datasets/reportTestData.xml")).collect(Collectors.toList());

		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(Constants.REGEX_UUID);
		for (String s : lineList) {

			Matcher matcher = p.matcher(s);

			while (matcher.find()) {

				String uuidMatch = matcher.group(2);
				final String changedUUID = uuidMatch.replaceAll("-", "");
				final byte[] bytes = DatatypeConverter.parseHexBinary(changedUUID);
				String base64EncodedUUID = new String(Base64.encode(bytes));
				matcher.appendReplacement(sb, "$1" + base64EncodedUUID + "$3");
			}
			matcher.appendTail(sb);
			sb.append("\n");

		}
		System.out.println(sb.toString());

	}


}
