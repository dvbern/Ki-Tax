/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class EinschulungstypBgStundenFaktorVisitor implements MandantVisitor<Double> {
	private final EinschulungTyp einschulungTyp;

	private static final double MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE_AR = 2400;
	private static final double MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT_AR = 1900;

	public EinschulungstypBgStundenFaktorVisitor(EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	public double getFaktor(Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}
	@Override
	public Double visitBern() {
		return 1.0;
	}

	@Override
	public Double visitLuzern() {
		return 1.0;
	}

	@Override
	public Double visitSolothurn() {
		return 1.0;
	}

	@Override
	public Double visitAppenzellAusserrhoden() {
		return (einschulungTyp.isEingeschultAppenzell() ?
			MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT_AR :
			MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE_AR) / 12 / 100;
	}
}
