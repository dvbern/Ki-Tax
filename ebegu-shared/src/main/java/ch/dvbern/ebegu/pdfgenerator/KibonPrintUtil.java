/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang.StringUtils;

public final class KibonPrintUtil {

	public static final Character LINE_BREAK = '\n';

	private KibonPrintUtil() {
	}


	@Nonnull
	public static String getGesuchstellerNameAsString(@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return "";
		}
		// Name Vorname
		return gesuchstellerContainer.extractFullName();
	}

	@Nonnull
	public static String getGesuchstellerAddressAsString(@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Optional<GesuchstellerAdresseContainer> adresseOptional =
			KibonPrintUtil.getGesuchstellerAdresse(gesuchstellerContainer);
		if (adresseOptional.isPresent()) {
			GesuchstellerAdresse adresse = adresseOptional.get().getGesuchstellerAdresseJA();
			Objects.requireNonNull(adresse);
			sb.append(adresse.getAddressAsString());
		}
		return sb.toString();
	}

	@Nonnull
	public static String getGesuchstellerWithAddressAsString(@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return "";
		}
		//noinspection StringConcatenationMissingWhitespace Es gibt ein NewLine
		return getGesuchstellerNameAsString(gesuchstellerContainer)
			+ LINE_BREAK
			+ getGesuchstellerAddressAsString(gesuchstellerContainer);
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

	@Nonnull
	public static List<String> getBenoetigteDokumenteAsList(
		@Nonnull List<DokumentGrund> benoetigteUnterlagen,
		@Nonnull Gesuch gesuch,
		@Nonnull Locale locale
	) {
		List<String> dokumenteList = new ArrayList<>();
		for (DokumentGrund dokumentGrund : benoetigteUnterlagen) {
			String text = KibonPrintUtil.getDokumentAsTextIfNeeded(dokumentGrund, gesuch, locale);
			if (StringUtils.isNotEmpty(text)) {
				dokumenteList.add(text);
			}
		}
		return dokumenteList;
	}

	@Nonnull
	public static String getDokumentAsTextIfNeeded(
		@Nonnull DokumentGrund dokumentGrund,
		@Nonnull Gesuch gesuch,
		@Nonnull Locale locale
	) {
		if (dokumentGrund.isNeeded() && dokumentGrund.isEmpty()) {
			final String additionalInformation = extractAdditionalInformation(dokumentGrund, gesuch);
			return ServerMessageUtil.translateEnumValue(dokumentGrund.getDokumentTyp(), locale)
				+ additionalInformation;
		}
		return "";
	}

	/**
	 * Gets the Tag or the LinkedPerson and returns it between parenthesis
	 */
	private static String extractAdditionalInformation(@Nonnull DokumentGrund dokumentGrund, @Nonnull Gesuch gesuch) {
		List<String> additionalText = new ArrayList<>();

		if (dokumentGrund.getTag() != null) {
			additionalText.add(dokumentGrund.getTag());
		}

		if (dokumentGrund.getPersonType() != null && dokumentGrund.getPersonNumber() != null) {
			if (dokumentGrund.getPersonType() == DokumentGrundPersonType.GESUCHSTELLER) {
				if (dokumentGrund.getPersonNumber() == 2 && gesuch.getGesuchsteller2() != null) {
					additionalText.add(gesuch.getGesuchsteller2().extractFullName());
				}
				if (dokumentGrund.getPersonNumber() == 1 && gesuch.getGesuchsteller1() != null) {
					additionalText.add(gesuch.getGesuchsteller1().extractFullName());
				}
			} else if (dokumentGrund.getPersonType() == DokumentGrundPersonType.KIND) {
				final KindContainer kindContainer = gesuch.extractKindFromKindNumber(dokumentGrund.getPersonNumber());
				if (kindContainer != null && kindContainer.getKindJA() != null) {
					additionalText.add(kindContainer.getKindJA().getFullName());
				}
			}
		}
		return additionalText.isEmpty() ? "" : " (" + StringUtils.join(additionalText, ", ") + ')';
	}
}
