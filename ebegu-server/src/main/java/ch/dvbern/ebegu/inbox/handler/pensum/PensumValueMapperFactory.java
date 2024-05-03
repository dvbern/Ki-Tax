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

package ch.dvbern.ebegu.inbox.handler.pensum;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;

@ApplicationScoped
@AllArgsConstructor
@NoArgsConstructor
public class PensumValueMapperFactory {

	@Inject
	private EinstellungService einstellungService;

	public PensumValueMapper createForPensum(ProcessingContext ctx) {
		BigDecimal maxTageProJahr = einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_KITA, ctx.getBetreuung());
		BigDecimal maxTageProJahrTFO = einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_TFO, ctx.getBetreuung());
		BigDecimal hoursProTag = einstellungService.getEinstellungAsBigDecimal(OEFFNUNGSSTUNDEN_TFO, ctx.getBetreuung());
		BigDecimal anzahlMonatProJahr = new BigDecimal("12.00");
		BigDecimal maxTageProMonat = MathUtil.DEFAULT.divideNullSafe(maxTageProJahr, anzahlMonatProJahr);

		BigDecimal maxTageProMonatTFO = MathUtil.DEFAULT.divideNullSafe(maxTageProJahrTFO, anzahlMonatProJahr);
		BigDecimal maxStundenProMonat = MathUtil.DEFAULT.multiplyNullSafe(maxTageProMonatTFO, hoursProTag);

		return new PensumValueMapper(maxTageProMonat, maxStundenProMonat);
	}
}
