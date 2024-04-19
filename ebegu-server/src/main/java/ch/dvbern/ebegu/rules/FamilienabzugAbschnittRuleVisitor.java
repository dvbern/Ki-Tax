/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.util.Locale;
import java.util.Map;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class FamilienabzugAbschnittRuleVisitor implements MandantVisitor<AbstractFamilienabzugAbschnittRule> {

	private final Map<EinstellungKey, Einstellung> einstellungMap;
	private final DateRange validityPeriod;
	private final Locale locale;

	public FamilienabzugAbschnittRuleVisitor(Map<EinstellungKey, Einstellung> einstellungMap, DateRange validityPeriod, Locale locale) {
		this.einstellungMap = einstellungMap;
		this.validityPeriod = validityPeriod;
		this.locale = locale;
	}

	public AbstractFamilienabzugAbschnittRule getFamilienabzugAbschnittRule(@NotNull MandantIdentifier mandant) {
		return mandant.accept(this);
	}

	@Override
	public AbstractFamilienabzugAbschnittRule visitBern() {
		return new FamilienabzugAbschnittRuleBern(einstellungMap, validityPeriod, locale);
	}

	@Override
	public AbstractFamilienabzugAbschnittRule visitLuzern() {
		return visitBern();
	}

	@Override
	public AbstractFamilienabzugAbschnittRule visitSolothurn() {
		return visitBern();
	}

	@Override
	public AbstractFamilienabzugAbschnittRule visitAppenzellAusserrhoden() {
		return visitBern();
	}

	@Override
	public AbstractFamilienabzugAbschnittRule visitSchwyz() {
		return new FamilienabzugAbschnittRuleSchwyz(einstellungMap, validityPeriod, locale);
	}
}
