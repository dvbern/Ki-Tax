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
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class FinanzielleSituationVisitor implements MandantVisitor<AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>> {

	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> getFinanzielleSituationDokumenteForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitBern() {
		return new BernFinanzielleSituationDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitLuzern() {
		return new LuzernFinanzielleSituationDokumente();
	}

	@Override
	public AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> visitSolothurn() {
		return new SolothurnFinanzielleSituationDokumente();
	}
}
