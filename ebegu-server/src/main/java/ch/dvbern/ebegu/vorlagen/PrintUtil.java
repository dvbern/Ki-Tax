/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.vorlagen;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.StreamsUtil;
import org.apache.commons.lang.StringUtils;

public final class PrintUtil {

	private PrintUtil() {
	}

	/**
	 * Gibt die Korrespondenzadresse zurueck wenn vorhanden, ansonsten die aktuelle Wohnadresse wenn vorhanden, wenn
	 * keine
	 * vorhanden dann empty
	 */
	@Nonnull
	public static Optional<GesuchstellerAdresseContainer> getGesuchstellerAdresse(
		@Nullable GesuchstellerContainer gesuchsteller) {

		if (gesuchsteller != null) {
			List<GesuchstellerAdresseContainer> adressen = gesuchsteller.getAdressen();

			// Zuerst suchen wir die Korrespondenzadresse wenn vorhanden
			final Optional<GesuchstellerAdresseContainer> korrespondenzadresse = adressen.stream()
				.filter(GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse)
				.reduce(throwExceptionIfMoreThanOneAdresse(gesuchsteller));
			if (korrespondenzadresse.isPresent() && korrespondenzadresse.get().getGesuchstellerAdresseJA() != null) {
				return korrespondenzadresse;
			}

			// Sonst suchen wir die aktuelle Wohnadresse. Die ist keine KORRESPONDENZADRESSE und das aktuelle Datum
			// liegt innerhalb ihrer Gueltigkeit
			final LocalDate now = LocalDate.now();
			for (GesuchstellerAdresseContainer gesuchstellerAdresse : adressen) {
				DateRange gueltigkeit = gesuchstellerAdresse.extractGueltigkeit();
				if (!gesuchstellerAdresse.extractIsKorrespondenzAdresse()
					// Adressen aus dem GS-Container interessieren uns nicht
					&& gesuchstellerAdresse.getGesuchstellerAdresseJA() != null
					&& gueltigkeit != null
					&& !gueltigkeit.getGueltigAb().isAfter(now)
					&& !gueltigkeit.getGueltigBis().isBefore(now)) {
					return Optional.of(gesuchstellerAdresse);
				}
			}
		}
		return Optional.empty();
	}

	@Nonnull
	private static BinaryOperator<GesuchstellerAdresseContainer> throwExceptionIfMoreThanOneAdresse(
		@Nonnull GesuchstellerContainer gesuchsteller) {

		return (element, otherElement) -> {
			throw new EbeguRuntimeException("getGesuchstellerAdresse_Korrespondenzadresse",
				ErrorCodeEnum.ERROR_TOO_MANY_RESULTS,
				gesuchsteller.getId());
		};
	}

	/**
	 * Ermittelt die Fallnummer im Form vom JJ.00xxxx. X ist die Fallnummer. Die Fallnummer wird in 6 Stellen
	 * dargestellt (mit 0 ergänzt)
	 *
	 * @param gesuch das Gesuch
	 * @return Fallnummer
	 */
	@Nonnull
	public static String createFallNummerString(@Nonnull Gesuch gesuch) {
		return gesuch.getJahrFallAndGemeindenummer();
	}

	/**
	 * @return GesuchstellerName
	 */
	@Nonnull
	public static String getGesuchstellerName(@Nonnull Gesuch gesuch) {
		StringBuilder name = new StringBuilder();

		extractGesuchsteller1(gesuch)
			.map(GesuchstellerContainer::extractFullName)
			.ifPresent(name::append);

		extractGesuchsteller2(gesuch)
			.map(GesuchstellerContainer::extractFullName)
			.ifPresent(fullName -> name.append('\n').append(fullName));

		return name.toString();
	}

	/**
	 * @return Gesuchsteller-Strasse
	 */
	@Nonnull
	public static String getGesuchstellerStrasse(@Nonnull Gesuch gesuch) {
		return extractGesuchsteller1(gesuch)
			.flatMap(PrintUtil::getGesuchstellerAdresse)
			.map(adresse -> {
				String strasse = adresse.extractStrasse();
				if (strasse != null) {
					if (adresse.extractHausnummer() != null) {
						return strasse + ' ' + adresse.extractHausnummer();
					}
					return strasse;
				}

				return StringUtils.EMPTY;
			})
			.orElse(StringUtils.EMPTY);
	}

	/**
	 * @return Gesuchsteller-PLZ Stadt
	 */
	@Nonnull
	public static String getGesuchstellerPLZStadt(@Nonnull Gesuch gesuch) {

		final Optional<GesuchstellerContainer> gesuchsteller1 = extractGesuchsteller1(gesuch);
		if (gesuchsteller1.isPresent()) {
			Optional<GesuchstellerAdresseContainer> gesuchstellerAdresse =
				getGesuchstellerAdresse(gesuchsteller1.get());
			if (gesuchstellerAdresse.isPresent()) {
				final GesuchstellerAdresseContainer gsAdresseCont = gesuchstellerAdresse.get();
				return gsAdresseCont.extractPlz() + ' ' + gsAdresseCont.extractOrt();
			}
		}
		return StringUtils.EMPTY;
	}

	@Nonnull
	private static Optional<GesuchstellerContainer> extractGesuchsteller1(@Nonnull Gesuch gesuch) {
		return Optional.ofNullable(gesuch.getGesuchsteller1());
	}

	@Nonnull
	private static Optional<GesuchstellerContainer> extractGesuchsteller2(@Nonnull Gesuch gesuch) {
		return Optional.ofNullable(gesuch.getGesuchsteller2());
	}

	/**
	 * Gibt die Organisationsbezeichnung falls sie eingegeben worden ist, sons leer. Die Organisation MUSS auf einer
	 * Korrespondenzadresse sein wenn vorhanden
	 *
	 * @return GesuchstellerName
	 */

	@Nullable
	public static String getOrganisation(@Nonnull Gesuch gesuch) {
		return extractGesuchsteller1(gesuch)
			.map(GesuchstellerContainer::getAdressen)
			.flatMap(adressen -> adressen.stream().filter(GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse)
				.reduce(StreamsUtil.toOnlyElement()))
			.map(GesuchstellerAdresseContainer::extractOrganisation)
			.orElse(null);
	}

	/**
	 * Liefer den Adresszusatz
	 */
	@Nullable
	public static String getAdresszusatz(@Nonnull Gesuch gesuch) {
		return extractGesuchsteller1(gesuch)
			.flatMap(PrintUtil::getGesuchstellerAdresse)
			.map(GesuchstellerAdresseContainer::extractZusatzzeile)
			.orElse(null);
	}

	@Nonnull
	public static String getNameAdresseFormatiert(
		@Nullable Gesuch gesuch,
		@Nullable GesuchstellerContainer gesuchsteller) {

		if (gesuch == null || gesuchsteller == null) {
			return StringUtils.EMPTY;
		}

		String newlineMSWord = "\n";

		String adresse = gesuchsteller.extractFullName();

		adresse += getGesuchstellerAdresse(gesuchsteller)
			.map(gsa -> {
				if (StringUtils.isNotEmpty(gsa.extractHausnummer())) {
					return newlineMSWord + gsa.extractStrasse() + ' ' + gsa.extractHausnummer();
				}
				return newlineMSWord + gsa.extractStrasse();
			})
			.orElse(StringUtils.EMPTY);

		String adrZusatz = getAdresszusatz(gesuch);
		if (StringUtils.isNotEmpty(adrZusatz)) {
			adresse += newlineMSWord + adrZusatz;
		}

		adresse += newlineMSWord + getGesuchstellerPLZStadt(gesuch);

		return adresse;
	}

	@Nonnull
	public static StringBuilder parseDokumentGrundDataToString(@Nonnull DokumentGrund dokumentGrund) {
		StringBuilder bemerkungenBuilder = new StringBuilder();
		if (dokumentGrund.isNeeded() && dokumentGrund.isEmpty()) {
			bemerkungenBuilder.append(ServerMessageUtil.translateEnumValue(dokumentGrund.getDokumentTyp()));
		}
		return bemerkungenBuilder;
	}
}
