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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldGemeinden implements MergeFieldProvider {

	gemeindenTitle(new SimpleMergeField<>("gemeindenTitle", STRING_CONVERTER)),
	mandant(new SimpleMergeField<>("mandant", STRING_CONVERTER)),

	rowGemeindeInfoRepeat(new RepeatRowMergeField("rowGemeindeInfoRepeat")),
	rowGemeindenZahlenRepeat(new RepeatRowMergeField("rowGemeindenZahlenRepeat")),

	bfsNummerTitle(new SimpleMergeField<>("bfsNummerTitle", STRING_CONVERTER)),
	nameGemeindeTitle(new SimpleMergeField<>("nameGemeindeTitle", STRING_CONVERTER)),
	gutscheinausgabestelleTitle(new SimpleMergeField<>("gutscheinausgabestelleTitle", STRING_CONVERTER)),
	korrespondenzspracheGemeindeTitle(new SimpleMergeField<>("korrespondenzspracheGemeindeTitle", STRING_CONVERTER)),
	angebotBGTitle(new SimpleMergeField<>("angebotBGTitle", STRING_CONVERTER)),
	angebotTSTitle(new SimpleMergeField<>("angebotTSTitle", STRING_CONVERTER)),
	startdatumBGTitle(new SimpleMergeField<>("startdatumBGTitle", STRING_CONVERTER)),

	gesuchsperiodeTitle(new SimpleMergeField<>("gesuchsperiodeTitle", STRING_CONVERTER)),
	limitierungKitaTitle(new SimpleMergeField<>("limitierungKitaTitle", STRING_CONVERTER)),
	kontingentierungTitle(new SimpleMergeField<>("kontingentierungTitle", STRING_CONVERTER)),
	nachfrageErfuelltTitle(new SimpleMergeField<>("nachfrageErfuelltTitle", STRING_CONVERTER)),
	nachfrageAnzahlTitle(new SimpleMergeField<>("nachfrageAnzahlTitle", STRING_CONVERTER)),
	nachfrageDauerTitle(new SimpleMergeField<>("nachfrageDauerTitle", STRING_CONVERTER)),
	kostenlenkungAndereTitle(new SimpleMergeField<>("kostenlenkungAndereTitle", STRING_CONVERTER)),
	welcheKostenlenkungsmassnahmenTitle(new SimpleMergeField<>("welcheKostenlenkungsmassnahmenTitle", STRING_CONVERTER)),
	erwerbspensumZuschlagTitle(new SimpleMergeField<>("erwerbspensumZuschlagTitle", STRING_CONVERTER)),

	bfsNummer(new SimpleMergeField<>("bfsNummer", LONG_CONVERTER)),
	nameGemeinde(new SimpleMergeField<>("nameGemeinde", STRING_CONVERTER)),
	gutscheinausgabestelle(new SimpleMergeField<>("gutscheinausgabestelle", STRING_CONVERTER)),
	korrespondenzspracheGemeinde(new SimpleMergeField<>("korrespondenzspracheGemeinde", STRING_CONVERTER)),
	angebotBG(new SimpleMergeField<>("angebotBG", BOOLEAN_X_CONVERTER)),
	angebotTS(new SimpleMergeField<>("angebotTS", BOOLEAN_X_CONVERTER)),
	startdatumBG(new SimpleMergeField<>("startdatumBG", DATE_CONVERTER)),

	gesuchsperiode(new SimpleMergeField<>("gesuchsperiode", STRING_CONVERTER)),
	limitierungKita(new SimpleMergeField<>("limitierungKita", STRING_CONVERTER)),
	kontingentierung(new SimpleMergeField<>("kontingentierung", BOOLEAN_X_CONVERTER)),
	nachfrageErfuellt(new SimpleMergeField<>("nachfrageErfuellt", STRING_CONVERTER)),
	nachfrageAnzahl(new SimpleMergeField<>("nachfrageAnzahl", BIGDECIMAL_CONVERTER)),
	nachfrageDauer(new SimpleMergeField<>("nachfrageDauer", BIGDECIMAL_CONVERTER)),
	kostenlenkungAndere(new SimpleMergeField<>("kostenlenkungAndere", BOOLEAN_X_CONVERTER)),
	welcheKostenlenkungsmassnahmen(new SimpleMergeField<>("welcheKostenlenkungsmassnahmen", STRING_CONVERTER)),
	erwerbspensumZuschlag(new SimpleMergeField<>("erwerbspensumZuschlag", STRING_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldGemeinden(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Nonnull
	@Override
	public <V> MergeField<V> getMergeField() {
		//noinspect unchecked
		return (MergeField<V>) mergeField;
	}
}
