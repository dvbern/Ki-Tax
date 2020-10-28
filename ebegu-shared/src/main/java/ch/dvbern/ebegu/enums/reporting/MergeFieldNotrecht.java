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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATETIME_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldNotrecht implements MergeFieldProvider {

	datumErstellt(new SimpleMergeField<>("datumErstellt", DATE_CONVERTER)),
	flagZahlungenAusloesen(new SimpleMergeField<>("flagZahlungenAusloesen", STRING_CONVERTER)),

	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	status(new SimpleMergeField<>("status", STRING_CONVERTER)),
	betreuungsangebotTyp(new SimpleMergeField<>("betreuungsangebotTyp", STRING_CONVERTER)),
	traegerschaft(new SimpleMergeField<>("traegerschaft", STRING_CONVERTER)),
	email(new SimpleMergeField<>("email", STRING_CONVERTER)),
	adresseOrganisation(new SimpleMergeField<>("adresseOrganisation", STRING_CONVERTER)),
	adresseStrasse(new SimpleMergeField<>("adresseStrasse", STRING_CONVERTER)),
	adresseHausnummer(new SimpleMergeField<>("adresseHausnummer", STRING_CONVERTER)),
	adressePlz(new SimpleMergeField<>("adressePlz", STRING_CONVERTER)),
	adresseOrt(new SimpleMergeField<>("adresseOrt", STRING_CONVERTER)),
	telefon(new SimpleMergeField<>("telefon", STRING_CONVERTER)),

	stufe1InstitutionKostenuebernahmeAnzahlTage(new SimpleMergeField<>("stufe1InstitutionKostenuebernahmeAnzahlTage", BIGDECIMAL_CONVERTER)),
	stufe1InstitutionKostenuebernahmeAnzahlStunden(new SimpleMergeField<>("stufe1InstitutionKostenuebernahmeAnzahlStunden", BIGDECIMAL_CONVERTER)),
	stufe1InstitutionKostenuebernahmeBetreuung(new SimpleMergeField<>("stufe1InstitutionKostenuebernahmeBetreuung", BIGDECIMAL_CONVERTER)),
	stufe1KantonKostenuebernahmeAnzahlTage(new SimpleMergeField<>("stufe1KantonKostenuebernahmeAnzahlTage", BIGDECIMAL_CONVERTER)),
	stufe1KantonKostenuebernahmeAnzahlStunden(new SimpleMergeField<>("stufe1KantonKostenuebernahmeAnzahlStunden", BIGDECIMAL_CONVERTER)),
	stufe1KantonKostenuebernahmeBetreuung(new SimpleMergeField<>("stufe1KantonKostenuebernahmeBetreuung", BIGDECIMAL_CONVERTER)),

	stufe1FreigabeBetrag(new SimpleMergeField<>("stufe1FreigabeBetrag", BIGDECIMAL_CONVERTER)),
	stufe1FreigabeDatum(new SimpleMergeField<>("stufe1FreigabeDatum", DATETIME_CONVERTER)),
	stufe1FreigabeAusbezahltAm(new SimpleMergeField<>("stufe1FreigabeAusbezahltAm", DATETIME_CONVERTER)),
	stufe1ZahlungJetztAusgeloest(new SimpleMergeField<>("stufe1ZahlungJetztAusgeloest", STRING_CONVERTER)),

	institutionTyp(new SimpleMergeField<>("institutionTyp", STRING_CONVERTER)),

	stufe2InstitutionKostenuebernahmeAnzahlTage(new SimpleMergeField<>("stufe2InstitutionKostenuebernahmeAnzahlTage", BIGDECIMAL_CONVERTER)),
	stufe2InstitutionKostenuebernahmeAnzahlStunden(new SimpleMergeField<>("stufe2InstitutionKostenuebernahmeAnzahlStunden", BIGDECIMAL_CONVERTER)),
	stufe2InstitutionKostenuebernahmeBetreuung(new SimpleMergeField<>("stufe2InstitutionKostenuebernahmeBetreuung", BIGDECIMAL_CONVERTER)),
	stufe2KantonKostenuebernahmeAnzahlTage(new SimpleMergeField<>("stufe2KantonKostenuebernahmeAnzahlTage", BIGDECIMAL_CONVERTER)),
	stufe2KantonKostenuebernahmeAnzahlStunden(new SimpleMergeField<>("stufe2KantonKostenuebernahmeAnzahlStunden", BIGDECIMAL_CONVERTER)),
	stufe2KantonKostenuebernahmeBetreuung(new SimpleMergeField<>("stufe2KantonKostenuebernahmeBetreuung", BIGDECIMAL_CONVERTER)),

	betragEntgangeneElternbeitraege(new SimpleMergeField<>("betragEntgangeneElternbeitraege", BIGDECIMAL_CONVERTER)),
	betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(new SimpleMergeField<>("betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten", BIGDECIMAL_CONVERTER)),
	rueckerstattungNichtAngeboteneBetreuungstage(new SimpleMergeField<>("rueckerstattungNichtAngeboteneBetreuungstage", BIGDECIMAL_CONVERTER)),
	kurzarbeitBetrag(new SimpleMergeField<>("kurzarbeitBetrag", BIGDECIMAL_CONVERTER)),
	coronaErwerbsersatzBetrag(new SimpleMergeField<>("coronaErwerbsersatzBetrag", BIGDECIMAL_CONVERTER)),

	stufe2VerfuegungBetrag(new SimpleMergeField<>("stufe2VerfuegungBetrag", BIGDECIMAL_CONVERTER)),
	stufe2VerfuegungDatum(new SimpleMergeField<>("stufe2VerfuegungDatum", DATETIME_CONVERTER)),
	stufe2VerfuegungAusbezahltAm(new SimpleMergeField<>("stufe2VerfuegungAusbezahltAm", DATETIME_CONVERTER)),
	stufe2ZahlungJetztAusgeloest(new SimpleMergeField<>("stufe2ZahlungJetztAusgeloest", STRING_CONVERTER)),

	iban(new SimpleMergeField<>("iban", STRING_CONVERTER)),
	kontoinhaber(new SimpleMergeField<>("kontoinhaber", STRING_CONVERTER)),
	auszahlungOrganisation(new SimpleMergeField<>("auszahlungOrganisation", STRING_CONVERTER)),
	auszahlungStrasse(new SimpleMergeField<>("auszahlungStrasse", STRING_CONVERTER)),
	auszahlungHausnummer(new SimpleMergeField<>("auszahlungHausnummer", STRING_CONVERTER)),
	auszahlungPlz(new SimpleMergeField<>("auszahlungPlz", STRING_CONVERTER)),
	auszahlungOrt(new SimpleMergeField<>("auszahlungOrt", STRING_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldNotrecht(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
