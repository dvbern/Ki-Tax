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
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public final class PrintUtil {

	private static final int FALLNUMMER_MAXLAENGE = 6;

	private PrintUtil() {
	}

	/**
	 * Gibt die Korrespondenzadresse zurueck wenn vorhanden, ansonsten die aktuelle Wohnadresse wenn vorhanden, wenn keine
	 * vorhanden dann empty
	 */
	@Nonnull
	public static Optional<GesuchstellerAdresseContainer> getGesuchstellerAdresse(@Nullable GesuchstellerContainer gesuchsteller) {

		if (gesuchsteller != null) {
			List<GesuchstellerAdresseContainer> adressen = gesuchsteller.getAdressen();

			// Zuerst suchen wir die Korrespondenzadresse wenn vorhanden
			final Optional<GesuchstellerAdresseContainer> korrespondenzadresse = adressen.stream().filter(GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse)
				.reduce(throwExceptionIfMoreThanOneAdresse(gesuchsteller));
			if (korrespondenzadresse.isPresent() && korrespondenzadresse.get().getGesuchstellerAdresseJA() != null) {
				return korrespondenzadresse;
			}

			// Sonst suchen wir die aktuelle Wohnadresse. Die ist keine KORRESPONDENZADRESSE und das aktuelle Datum liegt innerhalb ihrer Gueltigkeit
			final LocalDate now = LocalDate.now();
			for (GesuchstellerAdresseContainer gesuchstellerAdresse : adressen) {
				DateRange gueltigkeit = gesuchstellerAdresse.extractGueltigkeit();
				if (!gesuchstellerAdresse.extractIsKorrespondenzAdresse()
					&& gesuchstellerAdresse.getGesuchstellerAdresseJA() != null // Adressen aus dem GS-Container interessieren uns nicht
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
	private static BinaryOperator<GesuchstellerAdresseContainer> throwExceptionIfMoreThanOneAdresse(@Nonnull GesuchstellerContainer gesuchsteller) {
		return (element, otherElement) -> {
			throw new EbeguRuntimeException("getGesuchstellerAdresse_Korrespondenzadresse", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, gesuchsteller.getId());
		};
	}

	/**
	 * Ermittelt die Fallnummer im Form vom JJ.00xxxx. X ist die Fallnummer. Die Fallnummer wird in 6 Stellen
	 * dargestellt (mit 0 ergänzt)
	 *
	 * @param gesuch das Gesuch
	 * @return Fallnummer
	 */
	public static String createFallNummerString(Gesuch gesuch) {

		return Integer.toString(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear()).substring(2, 4) + "."
			+ getPaddedFallnummer(gesuch.getFall().getFallNummer());
	}

	public static String getPaddedFallnummer(long fallNummer) {
		return Strings.padStart(Long.toString(fallNummer), FALLNUMMER_MAXLAENGE, '0');
	}

	/**
	 * @return GesuchstellerName
	 */
	public static String getGesuchstellerName(Gesuch gesuch) {
		StringBuilder name = new StringBuilder();
		Optional<GesuchstellerContainer> gesuchsteller = extractGesuchsteller1(gesuch);
		gesuchsteller.ifPresent(gesuchstellerContainer -> name.append(gesuchstellerContainer.extractFullName()));
		if (gesuch.getGesuchsteller2() != null) {
			Optional<GesuchstellerContainer> gesuchsteller2 = extractGesuchsteller2(gesuch);
			if (gesuchsteller2.isPresent()) {
				name.append('\n');
				name.append(gesuchsteller2.get().extractFullName());
			}
		}
		return name.toString();
	}

	/**
	 * @return Gesuchsteller-Strasse
	 */

	public static String getGesuchstellerStrasse(Gesuch gesuch) {
		final Optional<GesuchstellerContainer> gesuchsteller1 = extractGesuchsteller1(gesuch);
		if (gesuchsteller1.isPresent()) {
			Optional<GesuchstellerAdresseContainer> gesuchstellerAdresse = getGesuchstellerAdresse(gesuchsteller1.get());
			if (gesuchstellerAdresse.isPresent()) {
				final GesuchstellerAdresseContainer gsAdresseCont = gesuchstellerAdresse.get();
				String strasse = gsAdresseCont.extractStrasse();
				if (strasse != null) {
					if (gsAdresseCont.extractHausnummer() != null) {
						return strasse + ' ' + gsAdresseCont.extractHausnummer();
					} else {
						return strasse;
					}
				}
			}
		}
		return "";
	}

	/**
	 * @return Gesuchsteller-PLZ Stadt
	 */

	public static String getGesuchstellerPLZStadt(Gesuch gesuch) {

		final Optional<GesuchstellerContainer> gesuchsteller1 = extractGesuchsteller1(gesuch);
		if (gesuchsteller1.isPresent()) {
			Optional<GesuchstellerAdresseContainer> gesuchstellerAdresse = getGesuchstellerAdresse(gesuchsteller1.get());
			if (gesuchstellerAdresse.isPresent()) {
				final GesuchstellerAdresseContainer gsAdresseCont = gesuchstellerAdresse.get();
				return gsAdresseCont.extractPlz() + " " + gsAdresseCont.extractOrt();
			}
		}
		return "";
	}

	@Nonnull
	private static Optional<GesuchstellerContainer> extractGesuchsteller1(Gesuch gesuch) {

		GesuchstellerContainer gs1 = gesuch.getGesuchsteller1();
		if (gs1 != null) {
			return Optional.of(gs1);
		}
		return Optional.empty();
	}

	@Nonnull
	private static Optional<GesuchstellerContainer> extractGesuchsteller2(Gesuch gesuch) {

		GesuchstellerContainer gs2 = gesuch.getGesuchsteller2();
		if (gs2 != null) {
			return Optional.of(gs2);
		}
		return Optional.empty();
	}

	/**
	 * Gibt die Organisationsbezeichnung falls sie eingegeben worden ist, sons leer. Die Organisation MUSS auf einer
	 * Korrespondenzadresse sein wenn vorhanden
	 *
	 * @return GesuchstellerName
	 */

	@Nullable
	public static String getOrganisation(Gesuch gesuch) {
		Optional<GesuchstellerContainer> gesuchsteller = extractGesuchsteller1(gesuch);
		if (gesuchsteller.isPresent()) {
			final List<GesuchstellerAdresseContainer> adressen = gesuchsteller.get().getAdressen();
			Optional<GesuchstellerAdresseContainer> korrespondezaddrOpt = adressen.stream().
				filter(GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse)
				.reduce(StreamsUtil.toOnlyElement());
			if (korrespondezaddrOpt.isPresent()) {
				return korrespondezaddrOpt.get().extractOrganisation();
			}
		}
		return null;
	}

	/**
	 * Liefer den Adresszusatz
	 */
	@Nullable
	public static String getAdresszusatz(Gesuch gesuch) {
		if (extractGesuchsteller1(gesuch).isPresent()) {
			Optional<GesuchstellerAdresseContainer> gesuchstellerAdresse = getGesuchstellerAdresse(extractGesuchsteller1(gesuch).get());
			if (gesuchstellerAdresse.isPresent()) {
				return gesuchstellerAdresse.get().extractZusatzzeile();
			}
		}
		return null;
	}

	public static String getNameAdresseFormatiert(Gesuch gesuch, GesuchstellerContainer gesuchsteller) {

		if (gesuch != null && gesuchsteller != null) {
			String newlineMSWord = "\n";
			String adresse = StringUtils.EMPTY;

			adresse += gesuchsteller.extractFullName();

			Optional<GesuchstellerAdresseContainer> gsa = getGesuchstellerAdresse(gesuchsteller);
			if (gsa.isPresent()) {
				if (StringUtils.isNotEmpty(gsa.get().extractHausnummer())) {
					adresse += newlineMSWord + gsa.get().extractStrasse() + " " + gsa.get().extractHausnummer();
				} else {
					adresse += newlineMSWord + gsa.get().extractStrasse();
				}
			}

			String adrZusatz = getAdresszusatz(gesuch);
			if (StringUtils.isNotEmpty(adrZusatz)) {
				adresse += newlineMSWord + adrZusatz;
			}

			adresse += newlineMSWord + getGesuchstellerPLZStadt(gesuch);

			return adresse;
		} else {
			return StringUtils.EMPTY;
		}

	}

	@Nonnull
	public static StringBuilder parseDokumentGrundDataToString(DokumentGrund dokumentGrund) {
		StringBuilder bemerkungenBuilder = new StringBuilder();
		if (dokumentGrund.isNeeded() && dokumentGrund.isEmpty()) {
			bemerkungenBuilder.append(ServerMessageUtil.translateEnumValue(dokumentGrund.getDokumentTyp()));
			if (StringUtils.isNotEmpty(dokumentGrund.getFullName())) {
				bemerkungenBuilder.append(" (");
				bemerkungenBuilder.append(dokumentGrund.getFullName());

				if (dokumentGrund.getTag() != null) {
					bemerkungenBuilder.append(" / ").append(dokumentGrund.getTag());
				}
				bemerkungenBuilder.append(")");
			}
		}
		return bemerkungenBuilder;
	}
}
