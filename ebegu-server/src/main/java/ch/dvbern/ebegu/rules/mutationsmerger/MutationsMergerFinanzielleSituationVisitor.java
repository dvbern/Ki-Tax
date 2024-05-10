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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.util.Locale;

import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;
import com.sun.istack.NotNull;

public class MutationsMergerFinanzielleSituationVisitor implements FinanzielleSituationTypVisitor<AbstractMutationsMergerFinanzielleSituation> {

	private final Locale locale;

	public MutationsMergerFinanzielleSituationVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerFinanzielleSituation getMutationsMergerFinanzielleSituation(@NotNull
		FinanzielleSituationTyp finanzielleSituationTyp) {
		return finanzielleSituationTyp.accept(this);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBern() {
		return new MutationsMergerFinanzielleSituationBern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBernFKJV() {
		return new MutationsMergerFinanzielleSituationBernFKJV(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitLuzern() {
		return new MutationsMergerFinanzielleSituationLuzern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitSolothurn() {
		return new MutationsMergerFinanzielleSituationBern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitAppenzell() {
		return new MutationsMergerFinanzielleSituationBern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitSchwyz() {
		return new MutationsMergerFinanzielleSituationSchwyz(locale);
	}
}
