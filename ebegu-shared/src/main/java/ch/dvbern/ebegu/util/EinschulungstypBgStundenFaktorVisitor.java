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

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class EinschulungstypBgStundenFaktorVisitor implements MandantVisitor<BigDecimal> {
	private final EinschulungTyp einschulungTyp;

	private static final BigDecimal MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE_AR = BigDecimal.valueOf(2400);
	private static final BigDecimal MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT_AR =  BigDecimal.valueOf(1900);

	public EinschulungstypBgStundenFaktorVisitor(EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	public BigDecimal getFaktor(Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}
	@Override
	public BigDecimal visitBern() {
		return BigDecimal.ONE;
	}

	@Override
	public BigDecimal visitLuzern() {
		return BigDecimal.ONE;
	}

	@Override
	public BigDecimal visitSolothurn() {
		return BigDecimal.ONE;
	}

	@Override
	public BigDecimal visitAppenzellAusserrhoden() {
		BigDecimal bgStundenProJahr =  (einschulungTyp.isEingeschultAppenzell() ?
			MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT_AR :
			MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE_AR);

		return MathUtil.EXACT.divide(MathUtil.EXACT.divide(bgStundenProJahr, BigDecimal.valueOf(12)), BigDecimal.valueOf(100));
	}
}
