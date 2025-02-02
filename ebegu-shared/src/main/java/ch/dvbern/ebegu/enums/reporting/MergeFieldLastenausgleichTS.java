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

public enum MergeFieldLastenausgleichTS implements MergeFieldProvider {
	// param
	datumGeneriert(new SimpleMergeField<>("datumGeneriert", DATE_CONVERTER)),

	// repeat row
	rowGemeindenRepeat(new RepeatRowMergeField("rowGemeindenRepeat")),
	rowTagesschulenRepeat(new RepeatRowMergeField("rowTagesschulenRepeat")),

	// Gemeinden
	nameGemeinde(new SimpleMergeField<>("nameGemeinde", STRING_CONVERTER)),
	bfsNummer(new SimpleMergeField<>("bfsNummer", LONG_CONVERTER)),
	gemeindeFallNummer(new SimpleMergeField<>("gemeindeFallNummer", STRING_CONVERTER)),
	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	status(new SimpleMergeField<>("status", STRING_CONVERTER)),
	timestampMutiert(new SimpleMergeField<>("timestampMutiert", DATETIME_CONVERTER)),
	alleAnmeldungenKibon(new SimpleMergeField<>("alleAnmeldungenKibon", BOOLEAN_X_CONVERTER)),
	bedarfAbgeklaert(new SimpleMergeField<>("bedarfAbgeklaert", BOOLEAN_X_CONVERTER)),
	ferienbetreuung(new SimpleMergeField<>("ferienbetreuung", BOOLEAN_X_CONVERTER)),
	zugangAlle(new SimpleMergeField<>("zugangAlle", BOOLEAN_X_CONVERTER)),
	grundZugangEingeschraenkt(new SimpleMergeField<>("grundZugangEingeschraenkt", STRING_CONVERTER)),
	betreuungsstundenFaktor1(new SimpleMergeField<>("betreuungsstundenFaktor1", BIGDECIMAL_CONVERTER)),
	betreuungsstundenFaktor15(new SimpleMergeField<>("betreuungsstundenFaktor15", BIGDECIMAL_CONVERTER)),
	betreuungsstundenFaktor3(new SimpleMergeField<>("betreuungsstundenFaktor3", BIGDECIMAL_CONVERTER)),
	betreuungsstundenPaed(new SimpleMergeField<>("betreuungsstundenPaed", BIGDECIMAL_CONVERTER)),
	betreuungsstundenNichtPaed(new SimpleMergeField<>("betreuungsstundenNichtPaed", BIGDECIMAL_CONVERTER)),
	elterngebuehrenBetreuung(new SimpleMergeField<>("elterngebuehrenBetreuung", BIGDECIMAL_CONVERTER)),
	elterngebuehrenVolksschulangebot(new SimpleMergeField<>("elterngebuehrenVolksschulangebot", BIGDECIMAL_CONVERTER)),
	schliessungCovid(new SimpleMergeField<>("schliessungCovid", BOOLEAN_X_CONVERTER)),
	elterngebuehrenCovid(new SimpleMergeField<>("elterngebuehrenCovid", BIGDECIMAL_CONVERTER)),
	ersteRate(new SimpleMergeField<>("ersteRate", BIGDECIMAL_CONVERTER)),
	gesamtkosten(new SimpleMergeField<>("gesamtkosten", BIGDECIMAL_CONVERTER)),
	elterngebuehrenVerpflegung(new SimpleMergeField<>("elterngebuehrenVerpflegung", BIGDECIMAL_CONVERTER)),
	einnahmenDritte(new SimpleMergeField<>("einnahmenDritte", BIGDECIMAL_CONVERTER)),
	ueberschussVorjahr(new SimpleMergeField<>("ueberschussVorjahr", BOOLEAN_X_CONVERTER)),
	ueberschussVerwendung(new SimpleMergeField<>("ueberschussVerwendung", STRING_CONVERTER)),
	bemerkungenKosten(new SimpleMergeField<>("bemerkungenKosten", STRING_CONVERTER)),
	betreuungsstundenDokumentiert(new SimpleMergeField<>("betreuungsstundenDokumentiert", BOOLEAN_X_CONVERTER)),
	ElterngebuehrenTSV(new SimpleMergeField<>("ElterngebuehrenTSV", BOOLEAN_X_CONVERTER)),
	elterngebuehrenBelege(new SimpleMergeField<>("elterngebuehrenBelege", BOOLEAN_X_CONVERTER)),
	elterngebuehrenMaximaltarif(new SimpleMergeField<>("elterngebuehrenMaximaltarif", BOOLEAN_X_CONVERTER)),
	betreuungPaedagogisch(new SimpleMergeField<>("betreuungPaedagogisch", BOOLEAN_X_CONVERTER)),
	ausbildungBelegt(new SimpleMergeField<>("ausbildungBelegt", BOOLEAN_X_CONVERTER)),
	bemerkungenGemeinde(new SimpleMergeField<>("bemerkungenGemeinde", STRING_CONVERTER)),
	bemerkungStarkeVeraenderung(new SimpleMergeField<>("bemerkungStarkeVeraenderung", STRING_CONVERTER)),
	betreuungsstundenPrognose(new SimpleMergeField<>("betreuungsstundenPrognose", BIGDECIMAL_CONVERTER)),
	betreuungsstundenPrognoseKibon(new SimpleMergeField<>("betreuungsstundenPrognoseKibon", STRING_CONVERTER)),
	betreuungsstundenPrognoseBemerkungen(new SimpleMergeField<>("betreuungsstundenPrognoseBemerkungen", STRING_CONVERTER)),
	bemerkungMindestens50ProzentAusgebildet(new SimpleMergeField<>("bemerkungMindestensFuenfzigProzentAusgebildet", STRING_CONVERTER)),

	// Tagesschulen
	gemeindeFallnummerTS(new SimpleMergeField<>("gemeindeFallnummerTS", STRING_CONVERTER)),
	tagesschuleName(new SimpleMergeField<>("tagesschuleName", STRING_CONVERTER)),
	tagesschuleID(new SimpleMergeField<>("tagesschuleID", STRING_CONVERTER)),
	lehrbetrieb(new SimpleMergeField<>("lehrbetrieb", BOOLEAN_X_CONVERTER)),
	kinderTotal(new SimpleMergeField<>("kinderTotal", BIGDECIMAL_CONVERTER)),
	kinderKindergarten(new SimpleMergeField<>("kinderKindergarten", BIGDECIMAL_CONVERTER)),
	kinderPrimar(new SimpleMergeField<>("kinderPrimar", BIGDECIMAL_CONVERTER)),
	kinderSek(new SimpleMergeField<>("kinderSek", BIGDECIMAL_CONVERTER)),
	kinderFaktor15(new SimpleMergeField<>("kinderFaktor15", BIGDECIMAL_CONVERTER)),
	kinderFaktor3(new SimpleMergeField<>("kinderFaktor3", BIGDECIMAL_CONVERTER)),
	kinderFrueh(new SimpleMergeField<>("kinderFrueh", BIGDECIMAL_CONVERTER)),
	kinderMittag(new SimpleMergeField<>("kinderMittag", BIGDECIMAL_CONVERTER)),
	kinderNachmittag1(new SimpleMergeField<>("kinderNachmittag1", BIGDECIMAL_CONVERTER)),
	kinderNachmittag2(new SimpleMergeField<>("kinderNachmittag2", BIGDECIMAL_CONVERTER)),
	kinderBasisstufe(new SimpleMergeField<>("kinderBasisstufe", BIGDECIMAL_CONVERTER)),
	betreuungsstundenTagesschule(new SimpleMergeField<>("betreuungsstundenTagesschule", BIGDECIMAL_CONVERTER)),
	konzeptOrganisatorisch(new SimpleMergeField<>("konzeptOrganisatorisch", BOOLEAN_X_CONVERTER)),
	konzeptPaedagogisch(new SimpleMergeField<>("konzeptPaedagogisch", BOOLEAN_X_CONVERTER)),
	raeumeGeeignet(new SimpleMergeField<>("raeumeGeeignet", BOOLEAN_X_CONVERTER)),
	betreuungsVerhaeltnis(new SimpleMergeField<>("betreuungsVerhaeltnis", BOOLEAN_X_CONVERTER)),
	ernaehrung(new SimpleMergeField<>("ernaehrung", BOOLEAN_X_CONVERTER)),
	bemerkungenTagesschule(new SimpleMergeField<>("bemerkungenTagesschule", STRING_CONVERTER)),

	fruehBetMo(new SimpleMergeField<>("fruehBetMo", BOOLEAN_X_CONVERTER)),
	fruehBetDi(new SimpleMergeField<>("fruehBetDi", BOOLEAN_X_CONVERTER)),
	fruehBetMi(new SimpleMergeField<>("fruehBetMi", BOOLEAN_X_CONVERTER)),
	fruehBetDo(new SimpleMergeField<>("fruehBetDo", BOOLEAN_X_CONVERTER)),
	fruehBetFr(new SimpleMergeField<>("fruehBetFr", BOOLEAN_X_CONVERTER)),
	mittagsBetMo(new SimpleMergeField<>("mittagsBetMo", BOOLEAN_X_CONVERTER)),
	mittagsBetDi(new SimpleMergeField<>("mittagsBetDi", BOOLEAN_X_CONVERTER)),
	mittagsBetMi(new SimpleMergeField<>("mittagsBetMi", BOOLEAN_X_CONVERTER)),
	mittagsBetDo(new SimpleMergeField<>("mittagsBetDo", BOOLEAN_X_CONVERTER)),
	mittagsBetFr(new SimpleMergeField<>("mittagsBetFr", BOOLEAN_X_CONVERTER)),
	nachmittags1BetMo(new SimpleMergeField<>("nachmittags1BetMo", BOOLEAN_X_CONVERTER)),
	nachmittags1BetDi(new SimpleMergeField<>("nachmittags1BetDi", BOOLEAN_X_CONVERTER)),
	nachmittags1BetMi(new SimpleMergeField<>("nachmittags1BetMi", BOOLEAN_X_CONVERTER)),
	nachmittags1BetDo(new SimpleMergeField<>("nachmittags1BetDo", BOOLEAN_X_CONVERTER)),
	nachmittags1BetFr(new SimpleMergeField<>("nachmittags1BetFr", BOOLEAN_X_CONVERTER)),
	nachmittags2BetMo(new SimpleMergeField<>("nachmittags2BetMo", BOOLEAN_X_CONVERTER)),
	nachmittags2BetDi(new SimpleMergeField<>("nachmittags2BetDi", BOOLEAN_X_CONVERTER)),
	nachmittags2BetMi(new SimpleMergeField<>("nachmittags2BetMi", BOOLEAN_X_CONVERTER)),
	nachmittags2BetDo(new SimpleMergeField<>("nachmittags2BetDo", BOOLEAN_X_CONVERTER)),
	nachmittags2BetFr(new SimpleMergeField<>("nachmittags2BetFr", BOOLEAN_X_CONVERTER)),
	;

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldLastenausgleichTS(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
