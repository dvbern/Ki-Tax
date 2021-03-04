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

package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

/**
 * Merger fuer Statistik fuer Benutzer
 */
public enum MergeFieldBenutzer implements MergeFieldProvider {

	repeatBenutzerRow(new RepeatRowMergeField("repeatBenutzerRow")),

	reportBenutzerTitle(new SimpleMergeField<>("reportBenutzerTitle", STRING_CONVERTER)),

	usernameTitle(new SimpleMergeField<>("usernameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	emailTitle(new SimpleMergeField<>("emailTitle", STRING_CONVERTER)),
	roleTitle(new SimpleMergeField<>("roleTitle", STRING_CONVERTER)),
	roleGueltigBisTitel(new SimpleMergeField<>("roleGueltigBisTitel", STRING_CONVERTER)),
	gemeindenTitle(new SimpleMergeField<>("gemeindenTitle", STRING_CONVERTER)),
	angebotGemeindenTitle(new SimpleMergeField<>("angebotGemeindenTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	traegerschaftTitle(new SimpleMergeField<>("traegerschaftTitle", STRING_CONVERTER)),
	kitaTitel(new SimpleMergeField<>("kitaTitel", STRING_CONVERTER)),
	tagesfamilienTitle(new SimpleMergeField<>("tagesfamilienTitle", STRING_CONVERTER)),
	tagesschulenTitel(new SimpleMergeField<>("tagesschulenTitel", STRING_CONVERTER)),
	ferieninselTitle(new SimpleMergeField<>("ferieninselTitle", STRING_CONVERTER)),
	isJugendamtTitle(new SimpleMergeField<>("isJugendamtTitle", STRING_CONVERTER)),
	isSchulamtTitle(new SimpleMergeField<>("isSchulamtTitle", STRING_CONVERTER)),
	statusTitle(new SimpleMergeField<>("statusTitle", STRING_CONVERTER)),
	stichtagTitle(new SimpleMergeField<>("stichtagTitle", STRING_CONVERTER)),

	stichtag(new SimpleMergeField<>("stichtag", DATE_CONVERTER)),

	username(new SimpleMergeField<>("username", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	nachname(new SimpleMergeField<>("nachname", STRING_CONVERTER)),
	email(new SimpleMergeField<>("email", STRING_CONVERTER)),
	role(new SimpleMergeField<>("role", STRING_CONVERTER)),
	roleGueltigAb(new SimpleMergeField<>("roleGueltigAb", DATE_CONVERTER)),
	roleGueltigBis(new SimpleMergeField<>("roleGueltigBis", DATE_CONVERTER)),
	gemeinden(new SimpleMergeField<>("gemeinden", STRING_CONVERTER)),
	angebotGemeinden(new SimpleMergeField<>("angebotGemeinden", STRING_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	traegerschaft(new SimpleMergeField<>("traegerschaft", STRING_CONVERTER)),
	status(new SimpleMergeField<>("status", STRING_CONVERTER)),
	isKita(new SimpleMergeField<>("isKita", BOOLEAN_X_CONVERTER)),
	isTagesfamilien(new SimpleMergeField<>("isTagesfamilien", BOOLEAN_X_CONVERTER)),
	isTagi(new SimpleMergeField<>("isTagi", BOOLEAN_X_CONVERTER)),
	isTagesschule(new SimpleMergeField<>("isTagesschule", BOOLEAN_X_CONVERTER)),
	isFerieninsel(new SimpleMergeField<>("isFerieninsel", BOOLEAN_X_CONVERTER)),
	isJugendamt(new SimpleMergeField<>("isJugendamt", BOOLEAN_X_CONVERTER)),
	isSchulamt(new SimpleMergeField<>("isSchulamt", BOOLEAN_X_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldBenutzer(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
