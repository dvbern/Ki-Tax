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

package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BIGDECIMAL_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATETIME_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

/**
 * Merger fuer Statistik fuer Benutzer
 */
public enum MergeFieldFerienbetreuung implements MergeFieldProvider {

	repeatRow(new RepeatRowMergeField("repeatRow")),

	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	status(new SimpleMergeField<>("status", STRING_CONVERTER)),
	timestampMutiert(new SimpleMergeField<>("timestampMutiert", DATETIME_CONVERTER)),
	traegerschaft(new SimpleMergeField<>("traegerschaft", STRING_CONVERTER)),
	weitereGemeinden(new SimpleMergeField<>("weitereGemeinden", STRING_CONVERTER)),
	seitWannFerienbetreuungen(new SimpleMergeField<>("seitWannFerienbetreuungen", DATE_CONVERTER)),
	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	bfsNummerGemeinde(new SimpleMergeField<>("bfsNummerGemeinde", LONG_CONVERTER)),
	gemeindeAnschrift(new SimpleMergeField<>("gemeindeAnschrift", STRING_CONVERTER)),
	gemeindeStrasse(new SimpleMergeField<>("gemeindeStrasse", STRING_CONVERTER)),
	gemeindeHausnummer(new SimpleMergeField<>("gemeindeHausnummer", STRING_CONVERTER)),
	gemeindeZusatz(new SimpleMergeField<>("gemeindeZusatz", STRING_CONVERTER)),
	gemeindePlz(new SimpleMergeField<>("gemeindePlz", STRING_CONVERTER)),
	gemeindeOrt(new SimpleMergeField<>("gemeindeOrt", STRING_CONVERTER)),
	stammdatenKontaktpersonVorname(new SimpleMergeField<>("stammdatenKontaktpersonVorname", STRING_CONVERTER)),
	stammdatenKontaktpersonName(new SimpleMergeField<>("stammdatenKontaktpersonName", STRING_CONVERTER)),
	stammdatenKontaktpersonFunktion(new SimpleMergeField<>("stammdatenKontaktpersonFunktion", STRING_CONVERTER)),
	stammdatenKontaktpersonTelefon(new SimpleMergeField<>("stammdatenKontaktpersonTelefon", STRING_CONVERTER)),
	stammdatenKontaktpersonEmail(new SimpleMergeField<>("stammdatenKontaktpersonEmail", STRING_CONVERTER)),
	kontoInhaber(new SimpleMergeField<>("kontoInhaber", STRING_CONVERTER)),
	kontoStrasse(new SimpleMergeField<>("kontoStrasse", STRING_CONVERTER)),
	kontoHausnummer(new SimpleMergeField<>("kontoHausnummer", STRING_CONVERTER)),
	kontoZusatz(new SimpleMergeField<>("kontoZusatz", STRING_CONVERTER)),
	kontoPlz(new SimpleMergeField<>("kontoPlz", STRING_CONVERTER)),
	kontoOrt(new SimpleMergeField<>("kontoOrt", STRING_CONVERTER)),
	iban(new SimpleMergeField<>("iban", STRING_CONVERTER)),
	kontoVermerk(new SimpleMergeField<>("kontoVermerk", STRING_CONVERTER)),
	angebot(new SimpleMergeField<>("angebot", STRING_CONVERTER)),
	angebotKontaktpersonVorname(new SimpleMergeField<>("angebotKontaktpersonVorname", STRING_CONVERTER)),
	angebotKontaktpersonNachname(new SimpleMergeField<>("angebotKontaktpersonNachname", STRING_CONVERTER)),
	angebotKontaktpersonStrasse(new SimpleMergeField<>("angebotKontaktpersonStrasse", STRING_CONVERTER)),
	angebotKontaktpersonHausnummer(new SimpleMergeField<>("angebotKontaktpersonHausnummer", STRING_CONVERTER)),
	angebotKontaktpersonZusatz(new SimpleMergeField<>("angebotKontaktpersonZusatz", STRING_CONVERTER)),
	angebotKontaktpersonPlz(new SimpleMergeField<>("angebotKontaktpersonPlz", STRING_CONVERTER)),
	angebotKontaktpersonOrt(new SimpleMergeField<>("angebotKontaktpersonOrt", STRING_CONVERTER)),
	anzahlFerienwochenHerbstferien(new SimpleMergeField<>("anzahlFerienwochenHerbstferien", BIGDECIMAL_CONVERTER)),
	anzahlFerienwochenWinterferien(new SimpleMergeField<>("anzahlFerienwochenWinterferien", BIGDECIMAL_CONVERTER)),
	anzahlFerienwochenSportferien(new SimpleMergeField<>("anzahlFerienwochenSportferien", BIGDECIMAL_CONVERTER)),
	anzahlFerienwochenFruehlingsferien(new SimpleMergeField<>("anzahlFerienwochenFruehlingsferien", BIGDECIMAL_CONVERTER)),
	anzahlFerienwochenSommerferien(new SimpleMergeField<>("anzahlFerienwochenSommerferien", BIGDECIMAL_CONVERTER)),
	anzahlTageGesamt(new SimpleMergeField<>("anzahlTageGesamt", BIGDECIMAL_CONVERTER)),
	bemerkungAnzahlFerienwochen(new SimpleMergeField<>("bemerkungAnzahlFerienwochen", STRING_CONVERTER)),
	anzahlStundenProBetreuungstag(new SimpleMergeField<>("anzahlStundenProBetreuungstag", BIGDECIMAL_CONVERTER)),
	betreuungErfolgtTagsueber(new SimpleMergeField<>("betreuungErfolgtTagsueber", BOOLEAN_X_CONVERTER)),
	bemerkungOeffnungszeiten(new SimpleMergeField<>("bemerkungOeffnungszeiten", STRING_CONVERTER)),
	finanziellBeteiligteGemeinden(new SimpleMergeField<>("finanziellBeteiligteGemeinden", STRING_CONVERTER)),
	gemeindeFuehrtAngebotSelber(new SimpleMergeField<>("gemeindeFuehrtAngebotSelber", BOOLEAN_X_CONVERTER)),
	gemeindeFuehrtAngebotInKooperation(new SimpleMergeField<>("gemeindeFuehrtAngebotInKooperation", BOOLEAN_X_CONVERTER)),
	gemeindeBeauftragtExterneAnbieter(new SimpleMergeField<>("gemeindeBeauftragtExterneAnbieter", BOOLEAN_X_CONVERTER)),
	angebotVereineUndPrivateIntegriert(new SimpleMergeField<>("angebotVereineUndPrivateIntegriert", BOOLEAN_X_CONVERTER)),
	bemerkungenKooperation(new SimpleMergeField<>("bemerkungenKooperation", STRING_CONVERTER)),
	leitungDurchPersonMitAusbildung(new SimpleMergeField<>("leitungDurchPersonMitAusbildung", BOOLEAN_X_CONVERTER)),
	betreuungDurchPersonenMitErfahrung(new SimpleMergeField<>("betreuungDurchPersonenMitErfahrung", BOOLEAN_X_CONVERTER)),
	anzahlKinderAngemessen(new SimpleMergeField<>("anzahlKinderAngemessen", BOOLEAN_X_CONVERTER)),
	betreuungsschluessel(new SimpleMergeField<>("betreuungsschluessel", STRING_CONVERTER)),
	bemerkungenPersonal(new SimpleMergeField<>("bemerkungenPersonal", STRING_CONVERTER)),
	fixerTarifKinderDerGemeinde(new SimpleMergeField<>("fixerTarifKinderDerGemeinde", BOOLEAN_X_CONVERTER)),
	einkommensabhaengigerTarifKinderDerGemeinde(new SimpleMergeField<>("einkommensabhaengigerTarifKinderDerGemeinde", BOOLEAN_X_CONVERTER)),
	tagesschuleTarifGiltFuerFerienbetreuung(new SimpleMergeField<>("tagesschuleTarifGiltFuerFerienbetreuung", BOOLEAN_X_CONVERTER)),
	ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(new SimpleMergeField<>("ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet", BOOLEAN_X_CONVERTER)),
	kinderAusAnderenGemeindenZahlenAnderenTarif(new SimpleMergeField<>("kinderAusAnderenGemeindenZahlenAnderenTarif", STRING_CONVERTER)),
	bemerkungenTarifsystem(new SimpleMergeField<>("bemerkungenTarifsystem", STRING_CONVERTER)),
	anzahlBetreuungstageKinderBern(new SimpleMergeField<>("anzahlBetreuungstageKinderBern", BIGDECIMAL_CONVERTER)),
	betreuungstageKinderDieserGemeinde(new SimpleMergeField<>("betreuungstageKinderDieserGemeinde", BIGDECIMAL_CONVERTER)),
	betreuungstageKinderDieserGemeindeSonderschueler(new SimpleMergeField<>("betreuungstageKinderDieserGemeindeSonderschueler", BIGDECIMAL_CONVERTER)),
	davonBetreuungstageKinderAndererGemeinden(new SimpleMergeField<>("davonBetreuungstageKinderAndererGemeinden", BIGDECIMAL_CONVERTER)),
	davonBetreuungstageKinderAndererGemeindenSonderschueler(new SimpleMergeField<>("davonBetreuungstageKinderAndererGemeindenSonderschueler", BIGDECIMAL_CONVERTER)),
	anzahlBetreuteKinder(new SimpleMergeField<>("anzahlBetreuteKinder", BIGDECIMAL_CONVERTER)),
	anzahlBetreuteKinderSonderschueler(new SimpleMergeField<>("anzahlBetreuteKinderSonderschueler", BIGDECIMAL_CONVERTER)),
	anzahlBetreuteKinder1Zyklus(new SimpleMergeField<>("anzahlBetreuteKinder1Zyklus", BIGDECIMAL_CONVERTER)),
	anzahlBetreuteKinder2Zyklus(new SimpleMergeField<>("anzahlBetreuteKinder2Zyklus", BIGDECIMAL_CONVERTER)),
	anzahlBetreuteKinder3Zyklus(new SimpleMergeField<>("anzahlBetreuteKinder3Zyklus", BIGDECIMAL_CONVERTER)),
	personalkosten(new SimpleMergeField<>("personalkosten", BIGDECIMAL_CONVERTER)),
	personalkostenLeitungAdmin(new SimpleMergeField<>("personalkostenLeitungAdmin", BIGDECIMAL_CONVERTER)),
	sachkosten(new SimpleMergeField<>("sachkosten", BIGDECIMAL_CONVERTER)),
	verpflegungskosten(new SimpleMergeField<>("verpflegungskosten", BIGDECIMAL_CONVERTER)),
	weitereKosten(new SimpleMergeField<>("weitereKosten", BIGDECIMAL_CONVERTER)),
	bemerkungenKosten(new SimpleMergeField<>("bemerkungenKosten", STRING_CONVERTER)),
	elterngebuehren(new SimpleMergeField<>("elterngebuehren", BIGDECIMAL_CONVERTER)),
	weitereEinnahmen(new SimpleMergeField<>("weitereEinnahmen", BIGDECIMAL_CONVERTER)),
	totalKantonsbeitrag(new SimpleMergeField<>("totalKantonsbeitrag", BIGDECIMAL_CONVERTER)),
	beitragKinderAnbietendenGemeinde(new SimpleMergeField<>("beitragKinderAnbietendenGemeinde", BIGDECIMAL_CONVERTER)),
	beteiligungAnbietendenGemeinde(new SimpleMergeField<>("beteiligungAnbietendenGemeinde", BIGDECIMAL_CONVERTER)),
	kommentar(new SimpleMergeField<>("kommentar", STRING_CONVERTER)),
	datumGeneriert(new SimpleMergeField<>("datumGeneriert", DATE_CONVERTER));


	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldFerienbetreuung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
