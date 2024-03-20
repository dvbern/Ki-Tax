/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;
import com.sun.istack.NotNull;
import org.apache.commons.lang.NotImplementedException;

public class EinkommenVerschlechterungDokumenteVisitor implements FinanzielleSituationTypVisitor<AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>> {

	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> getEinkommenVerschlechterungDokumenteForFinSitTyp(@NotNull
		FinanzielleSituationTyp finanzielleSituationTyp) {
		return finanzielleSituationTyp.accept(this);
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitBern() {
		return new BernEinkommensverschlechterungDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitBernFKJV() {
		return new BernEinkommensverschlechterungDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitLuzern() {
		return new LuzernEinkommensverschlechterungDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitSolothurn() {
		return new SolothurnEinkommensverschlechterungDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitAppenzell() {
		return new AppenzellEinkommensverschlechterungDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitFinSitSchwyz() {
		throw new NotImplementedException();
	}
}
