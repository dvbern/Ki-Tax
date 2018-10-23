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

package ch.dvbern.ebegu.vorlagen.verfuegung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.vorlagen.AufzaehlungPrint;
import ch.dvbern.ebegu.vorlagen.AufzaehlungPrintImpl;
import ch.dvbern.ebegu.vorlagen.BriefPrintImpl;

/**
 * Transferobjekt
 */
public class VerfuegungPrintImpl extends BriefPrintImpl implements VerfuegungPrint {

	private final Betreuung betreuung;

	//formatiert
	private final String letzteVerfuegungDatum;

	public VerfuegungPrintImpl(Betreuung betreuung, @Nullable LocalDate letzteVerfuegungDatum) {
		super(betreuung.extractGesuch());
		this.letzteVerfuegungDatum = letzteVerfuegungDatum != null ?
			Constants.DATE_FORMATTER.format(letzteVerfuegungDatum) :
			null;
		this.betreuung = betreuung;
	}

	@Override
	public String getAngebot() {

		return ServerMessageUtil.translateEnumValue(betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp());
	}

	@Override
	public String getInstitution() {

		return betreuung.getInstitutionStammdaten().getInstitution().getName();
	}

	/**
	 * @return Gesuchsteller-ReferenzNummer
	 */
	@Override
	public String getReferenznummer() {

		return betreuung.getBGNummer();
	}

	/**
	 * @return Gesuchsteller-Verfuegungsdatum
	 */
	@Override
	public String getVerfuegungsdatum() {
		return letzteVerfuegungDatum;
	}

	/**
	 * @return Name Vorname des Kindes
	 */
	@Override
	public String getKindNameVorname() {

		return extractKind().getFullName();
	}

	/**
	 * @return Geburtsdatum des Kindes
	 */
	@Override
	public String getKindGeburtsdatum() {

		return Constants.DATE_FORMATTER.format(betreuung.getKind().getKindJA().getGeburtsdatum());
	}

	/**
	 * @return Kita Name
	 */
	@Override
	public String getKitaBezeichnung() {

		return betreuung.getInstitutionStammdaten().getInstitution().getName();
	}

	/**
	 * @return AnspruchAb
	 */
	@Override
	public String getAnspruchAb() {

		return Constants.DATE_FORMATTER.format(betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb());
	}

	/**
	 * @return AnspruchBis
	 */
	@Override
	public String getAnspruchBis() {

		return Constants.DATE_FORMATTER.format(betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis());
	}

	/**
	 * @return VerfuegungZeitabschnitten
	 */
	@Override
	public List<VerfuegungZeitabschnittPrint> getVerfuegungZeitabschnitt() {

		Optional<Verfuegung> verfuegung = extractVerfuegung();
		if (!verfuegung.isPresent()) {
			return new ArrayList<>();
		}

		// first of all we get all Zeitabschnitte and create a List of VerfuegungZeitabschnittPrintImpl, then we remove
		// all Zeitabschnitte with Pensum == 0 that we find at the beginning and at the end of the list. All Zeitabschnitte
		// between two valid values will remain: 0, 0, 30, 40, 0, 30, 0, 0 ==> 30, 40, 0, 30
		List<VerfuegungZeitabschnittPrint> result = verfuegung.get().getZeitabschnitte().stream()
				.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR.reversed())
				.map(VerfuegungZeitabschnittPrintImpl::new)
				.collect(Collectors.toList());

		ListIterator<VerfuegungZeitabschnittPrint> listIteratorBeginning = result.listIterator();
		while (listIteratorBeginning.hasNext()) {
			VerfuegungZeitabschnittPrint zeitabschnitt = listIteratorBeginning.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuung())) {
				listIteratorBeginning.remove();
			} else {
				break;
			}
		}

		Collections.reverse(result);
		ListIterator<VerfuegungZeitabschnittPrint> listIteratorEnd = result.listIterator();
		while (listIteratorEnd.hasNext()) {
			VerfuegungZeitabschnittPrint zeitabschnitt = listIteratorEnd.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuung())) {
				listIteratorEnd.remove();
			} else {
				break;
			}
		}

		return result;
	}

	/**
	 * Wenn die Betreuung VERFUEGT ist -> manuelle Bemerkungen Wenn die Betreuung noch nicht VERFUEGT ist -> generated
	 * Bemerkungen
	 */
	@Override
	public List<AufzaehlungPrint> getManuelleBemerkungen() {
		return extractVerfuegung()
			.map(Verfuegung::getManuelleBemerkungen)
			.map(this::splitBemerkungen)
			.orElseGet(Collections::emptyList);
	}

	/**
	 * Zerlegt die Bemerkungen (Delimiter \n) und bereitet die in einer Liste.
	 *
	 * @return List mit Bemerkungen
	 */
	@Nonnull
	private List<AufzaehlungPrint> splitBemerkungen(@Nonnull String bemerkungen) {
		List<AufzaehlungPrint> list = new ArrayList<>();
		// Leere Zeile werden mit diese Annotation [\\r\\n]+ entfernt
		String[] splitBemerkungenNewLine = bemerkungen.split('[' + System.getProperty("line.separator") + "]+");
		for (String bemerkung : splitBemerkungenNewLine) {
			list.add(new AufzaehlungPrintImpl(bemerkung));
		}
		return list;
	}

	/**
	 * @return true falls Pensum groesser 0 ist
	 */
	@Override
	public boolean isPensumGrosser0() {
		List<VerfuegungZeitabschnittPrint> vzList = getVerfuegungZeitabschnitt();
		BigDecimal value = vzList.stream()
			.map(VerfuegungZeitabschnittPrint::getBGPensum)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		return MathUtil.isPositive(value);
	}

	@Override
	public boolean isPensumIst0() {
		return !isPensumGrosser0();
	}

	@Override
	public boolean isVorgaengerVerfuegt() {
		return letzteVerfuegungDatum != null;
	}

	@Override
	public boolean isPrintManuellebemerkung() {
		return !getManuelleBemerkungen().isEmpty();
	}

	@Nonnull
	private Kind extractKind() {
		return betreuung.getKind().getKindJA();
	}

	@Nonnull
	private Optional<Verfuegung> extractVerfuegung() {
		Verfuegung verfuegung = betreuung.getVerfuegung();
		if (verfuegung != null) {
			return Optional.of(verfuegung);
		}
		return Optional.empty();
	}

}
