/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldMahlzeitenverguenstigung implements MergeFieldProvider {

	mahlzeitenverguenstigungTitle(new SimpleMergeField<>("mahlzeitenverguenstigungTitle", STRING_CONVERTER)),
	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	vonTitle(new SimpleMergeField<>("vonTitle", STRING_CONVERTER)),
	bisTitle(new SimpleMergeField<>("bisTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	traegerschaftTitle(new SimpleMergeField<>("traegerschaftTitle", STRING_CONVERTER)),
	angebotTitle(new SimpleMergeField<>("angebotTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	gesuchsteller1Title(new SimpleMergeField<>("gesuchsteller1Title", STRING_CONVERTER)),
	gesuchsteller2Title(new SimpleMergeField<>("gesuchsteller2Title", STRING_CONVERTER)),
	bgNummerTitle(new SimpleMergeField<>("bgNummerTitle", STRING_CONVERTER)),
	betreuungTitle(new SimpleMergeField<>("betreuungTitle", STRING_CONVERTER)),
	betreuungVonTitle(new SimpleMergeField<>("betreuungVonTitle", STRING_CONVERTER)),
	betreuungBisTitle(new SimpleMergeField<>("betreuungBisTitle", STRING_CONVERTER)),
	mahlzeitenTitle(new SimpleMergeField<>("mahlzeitenTitle", STRING_CONVERTER)),
	anzahlHauptmahlzeitenTitle(new SimpleMergeField<>("anzahlHauptmahlzeitenTitle", STRING_CONVERTER)),
	anzahlNebenmahlzeitenTitle(new SimpleMergeField<>("anzahlNebenmahlzeitenTitle", STRING_CONVERTER)),
	kostenHauptmahlzeitenTitle(new SimpleMergeField<>("kostenHauptmahlzeitenTitle", STRING_CONVERTER)),
	kostenNebenmahlzeitenTitle(new SimpleMergeField<>("kostenNebenmahlzeitenTitle", STRING_CONVERTER)),
	berechneteMahlzeitenverguenstigungTitle(new SimpleMergeField<>("berechneteMahlzeitenverguenstigungTitle", STRING_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow")),

	auswertungVon(new SimpleMergeField<>("auswertungVon", DATE_CONVERTER)),
	auswertungBis(new SimpleMergeField<>("auswertungBis", DATE_CONVERTER)),

	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	traegerschaft(new SimpleMergeField<>("traegerschaft", STRING_CONVERTER)),
	angebot(new SimpleMergeField<>("angebot", STRING_CONVERTER)),
	kindName(new SimpleMergeField<>("kindName", STRING_CONVERTER)),
	kindVorname(new SimpleMergeField<>("kindVorname", STRING_CONVERTER)),
	kindGeburtsdatum(new SimpleMergeField<>("kindGeburtsdatum", DATE_CONVERTER)),
	gs1Name(new SimpleMergeField<>("gs1Name", STRING_CONVERTER)),
	gs1Vorname(new SimpleMergeField<>("gs1Vorname", STRING_CONVERTER)),
	gs2Name(new SimpleMergeField<>("gs2Name", STRING_CONVERTER)),
	gs2Vorname(new SimpleMergeField<>("gs2Vorname", STRING_CONVERTER)),
	sozialhilfebezuegerTitle(new SimpleMergeField<>("sozialhilfebezuegerTitle", STRING_CONVERTER)),
	ibanTitle(new SimpleMergeField<>("ibanTitle", STRING_CONVERTER)),
	massgebendesEinkommenTitle(new SimpleMergeField<>("massgebendesEinkommenTitle", STRING_CONVERTER)),
	massgebendesEinkommenVorFamAbzugTitle(new SimpleMergeField<>("massgebendesEinkommenVorFamAbzugTitle", STRING_CONVERTER)),
	famGroesseTitle(new SimpleMergeField<>("famGroesseTitle", STRING_CONVERTER)),
	massgebendesEinkommenNachFamAbzugTitle(new SimpleMergeField<>("massgebendesEinkommenNachFamAbzugTitle", STRING_CONVERTER)),
	bgNummer(new SimpleMergeField<>("bgNummer", STRING_CONVERTER)),
	zeitabschnittVon(new SimpleMergeField<>("zeitabschnittVon", DATE_CONVERTER)),
	zeitabschnittBis(new SimpleMergeField<>("zeitabschnittBis", DATE_CONVERTER)),
	anzahlHauptmahlzeiten(new SimpleMergeField<>("anzahlHauptmahlzeiten", BIGDECIMAL_CONVERTER)),
	anzahlNebenmahlzeiten(new SimpleMergeField<>("anzahlNebenmahlzeiten", BIGDECIMAL_CONVERTER)),
	kostenHauptmahlzeiten(new SimpleMergeField<>("kostenHauptmahlzeiten", BIGDECIMAL_CONVERTER)),
	kostenNebenmahlzeiten(new SimpleMergeField<>("kostenNebenmahlzeiten", BIGDECIMAL_CONVERTER)),
	berechneteMahlzeitenverguenstigung(new SimpleMergeField<>("berechneteMahlzeitenverguenstigung", BIGDECIMAL_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldMahlzeitenverguenstigung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
