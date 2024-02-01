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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class BetreuungDokumenteVisitor  implements MandantVisitor<AbstractDokumente<Betreuung, Object>>  {

	public AbstractDokumente<Betreuung, Object> getBetreuungDokumenteForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractDokumente<Betreuung, Object> visitBern() {
		return new BernBetreuungDokumente();
	}

	@Override
	public AbstractDokumente<Betreuung, Object> visitLuzern() {
		return new LuzernBetreuungDokumente();
	}

	@Override
	public AbstractDokumente<Betreuung, Object> visitSolothurn() {
		return new BernBetreuungDokumente();
	}

	@Override
	public AbstractDokumente<Betreuung, Object> visitAppenzellAusserrhoden() {
		return new BernBetreuungDokumente();
	}

	@Override
	public AbstractDokumente<Betreuung, Object> visitSchwyz() {
		return this.visitSolothurn();
	}
}
