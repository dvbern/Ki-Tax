/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.ws.sts;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ch.dvbern.ebegu.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse soll die Signatur berechnen
 * Dazu nimmt sie den uebergebenen PrivateKey und die fuer die Signatur benoetigten Angaben und berechnet die Signatur.
*/
public class STSWebServiceSignatureGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(STSWebServiceSignatureGenerator.class.getSimpleName());
	private final String securityPrefix;

	public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	private final DateTimeFormatter dateTimeFormatter;
	private final PrivateKey privateKey;

	public STSWebServiceSignatureGenerator(String securityPrefix, String dateFormatPattern, PrivateKey privateSTSKey) {
		this.securityPrefix = securityPrefix;
		try {
			dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern, Constants.DEFAULT_LOCALE);
		} catch (IllegalArgumentException ex){
			LOGGER.error("Invalid dateFormatPattern passed to Signature Generator {}", dateFormatPattern );
			throw ex;
		}

		this.privateKey = privateSTSKey;
	}

	public  String getRequestProof(String appName, LocalDateTime requestTime) {
		String formattedZeit = this.dateTimeFormatter.format(requestTime);
		return this.securityPrefix + '-' + appName + ' ' + formattedZeit;
	}


	public byte[] getSignatureValue(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
			Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
		LOGGER.trace("String that will be signed: '{}'", message);

			sig.initSign(privateKey);
			sig.update(message.getBytes(StandardCharsets.UTF_8));

			return sig.sign();

	}
}
