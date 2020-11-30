/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.services.AbstractBaseService;
import org.apache.commons.lang.NotImplementedException;

/**
 * Service fuer Gemeindeantraege
 */
@Stateless
@Local(GemeindeAntragService.class)
public class GemeindeAntragServiceBean extends AbstractBaseService implements GemeindeAntragService {

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService lastenausgleichTagesschuleAngabenGemeindeService;


	@Override
	@Nonnull
	public List<GemeindeAntrag> createGemeindeAntrag(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp typ) {
		switch (typ) {
		case LASTENAUSGLEICH_TAGESSCHULEN:
			return new ArrayList<>(lastenausgleichTagesschuleAngabenGemeindeService.createLastenausgleichTagesschuleGemeinde(gesuchsperiode));
		case FERIENBETREUUNG:
			throw new NotImplementedException("Ferienbetreuung ist noch nicht umgesetzt");
		}
		return Collections.emptyList();
	}
}


