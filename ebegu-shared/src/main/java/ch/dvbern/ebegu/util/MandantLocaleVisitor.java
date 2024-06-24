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

package ch.dvbern.ebegu.util;

import java.util.Locale;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class MandantLocaleVisitor implements MandantVisitor<Locale> {

	private static final String VARIANT_BE = "be";
	private static final String VARIANT_LU = "lu";
	private static final String VARIANT_SO = "so";
	private static final String VARIANT_APPENZELL_AUSSERRHODEN = "ar";
	private static final String VARIANT_SCHWYZ = "sz";

	private final Locale locale;

	public MandantLocaleVisitor(Locale locale) {
		this.locale = locale;
	}

	public Locale process(Mandant mandant) {
		return process(mandant.getMandantIdentifier());
	}

	public Locale process(MandantIdentifier mandantIdentifier) {
		return mandantIdentifier.accept(this);
	}

	@Override
	public Locale visitBern() {
		return new Locale(locale.getLanguage(), locale.getCountry(), VARIANT_BE);
	}

	@Override
	public Locale visitLuzern() {
		return new Locale(locale.getLanguage(), locale.getCountry(), VARIANT_LU);
	}

	@Override
	public Locale visitSolothurn() {
		return new Locale(locale.getLanguage(), locale.getCountry(), VARIANT_SO);
	}

	@Override
	public Locale visitAppenzellAusserrhoden() {
		return new Locale(locale.getLanguage(), locale.getCountry(), VARIANT_APPENZELL_AUSSERRHODEN);
	}

	@Override
	public Locale visitSchwyz() {
		return new Locale(locale.getLanguage(), locale.getCountry(), VARIANT_SCHWYZ);
	}
}
