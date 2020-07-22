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

public enum MergeFieldTagesschuleRechnungsstellung implements MergeFieldProvider {

	tagesschuleRechungsstellungTitle(new SimpleMergeField<>("tagesschuleRechungsstellungTitle", STRING_CONVERTER)),
	datumErstelltTitle(new SimpleMergeField<>("datumErstelltTitle", STRING_CONVERTER)),
	tagesschuleTitle(new SimpleMergeField<>("tagesschuleTitle", STRING_CONVERTER)),
	kindTitle(new SimpleMergeField<>("kindTitle", STRING_CONVERTER)),
	nachnameKindTitle(new SimpleMergeField<>("nachnameKindTitle", STRING_CONVERTER)),
	vornameKindTitle(new SimpleMergeField<>("vornameKindTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	referenznummerTitle(new SimpleMergeField<>("referenznummerTitle", STRING_CONVERTER)),
	rechungsadresseTitle(new SimpleMergeField<>("rechungsadresseTitle", STRING_CONVERTER)),
	rechnungsadresseVornameTitle(new SimpleMergeField<>("rechnungsadresseVornameTitle", STRING_CONVERTER)),
	rechnungsadresseNachnameTitle(new SimpleMergeField<>("rechnungsadresseNachnameTitle", STRING_CONVERTER)),
	rechnungsadresseStrasseTitle(new SimpleMergeField<>("rechnungsadresseStrasseTitle", STRING_CONVERTER)),
	rechnungsadresseHausnummerTitle(new SimpleMergeField<>("rechnungsadresseHausnummerTitle", STRING_CONVERTER)),
	rechnungsadressePlzTitle(new SimpleMergeField<>("rechnungsadressePlzTitle", STRING_CONVERTER)),
	rechnungsadresseOrtTitle(new SimpleMergeField<>("rechnungsadresseOrtTitle", STRING_CONVERTER)),
	monatTitle(new SimpleMergeField<>("monatTitle", STRING_CONVERTER)),
	massgebendesEinkommenVorFamAbzugTitle(new SimpleMergeField<>("massgebendesEinkommenVorFamAbzugTitle", STRING_CONVERTER)),
	famGroesseTitle(new SimpleMergeField<>("famGroesseTitle", STRING_CONVERTER)),
	massgebendesEinkommenNachFamAbzugTitle(new SimpleMergeField<>("massgebendesEinkommenNachFamAbzugTitle", STRING_CONVERTER)),
	eintrittsdatumTitle(new SimpleMergeField<>("eintrittsdatumTitle", STRING_CONVERTER)),
	gebuehrProStundeMitBetreuungTitle(new SimpleMergeField<>("gebuehrProStundeMitBetreuungTitle", STRING_CONVERTER)),
	gebuehrProStundeOhneBetreuungTitle(new SimpleMergeField<>("gebuehrProStundeOhneBetreuungTitle", STRING_CONVERTER)),

	datumErstellt(new SimpleMergeField<>("datumErstellt", DATE_CONVERTER)),
	tagesschule(new SimpleMergeField<>("tagesschule", STRING_CONVERTER)),
	nachnameKind(new SimpleMergeField<>("nachnameKind", STRING_CONVERTER)),
	vornameKind(new SimpleMergeField<>("vornameKind", STRING_CONVERTER)),
	geburtsdatumKind(new SimpleMergeField<>("geburtsdatumKind", DATE_CONVERTER)),
	referenznummer(new SimpleMergeField<>("referenznummer", STRING_CONVERTER)),
	rechnungsadresseVorname(new SimpleMergeField<>("rechnungsadresseVorname", STRING_CONVERTER)),
	rechnungsadresseNachname(new SimpleMergeField<>("rechnungsadresseNachname", STRING_CONVERTER)),
	rechnungsadresseStrasse(new SimpleMergeField<>("rechnungsadresseStrasse", STRING_CONVERTER)),
	rechnungsadresseHausnummer(new SimpleMergeField<>("rechnungsadresseHausnummer", STRING_CONVERTER)),
	rechnungsadressePlz(new SimpleMergeField<>("rechnungsadressePlz", STRING_CONVERTER)),
	rechnungsadresseOrt(new SimpleMergeField<>("rechnungsadresseOrt", STRING_CONVERTER)),
	monat(new SimpleMergeField<>("monat", DATE_CONVERTER)),
	massgebendesEinkommenVorFamAbzug(new SimpleMergeField<>("massgebendesEinkommenVorFamAbzug", BIGDECIMAL_CONVERTER)),
	famGroesse(new SimpleMergeField<>("famGroesse", BIGDECIMAL_CONVERTER)),
	massgebendesEinkommenNachFamAbzug(new SimpleMergeField<>("massgebendesEinkommenNachFamAbzug", BIGDECIMAL_CONVERTER)),
	eintrittsdatum(new SimpleMergeField<>("eintrittsdatum", DATE_CONVERTER)),
	gebuehrProStundeMitBetreuung(new SimpleMergeField<>("gebuehrProStundeMitBetreuung", BIGDECIMAL_CONVERTER)),
	gebuehrProStundeOhneBetreuung(new SimpleMergeField<>("gebuehrProStundeOhneBetreuung", BIGDECIMAL_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldTagesschuleRechnungsstellung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
