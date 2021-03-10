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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
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

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Override
	@Nonnull
	public List<GemeindeAntrag> createAllGemeindeAntraege(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GemeindeAntragTyp typ) {
		switch (typ) {
		case LASTENAUSGLEICH_TAGESSCHULEN:
			return new ArrayList<>(lastenausgleichTagesschuleAngabenGemeindeService.createLastenausgleichTagesschuleGemeinde(
				gesuchsperiode));
		case FERIENBETREUUNG:
			throw new NotImplementedException("Masseninitialisierung für Ferienbetreuungen wird nicht umgesetzt");
		}
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public GemeindeAntrag createGemeindeAntrag(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GemeindeAntragTyp gemeindeAntragTyp
	) {
		switch (gemeindeAntragTyp) {
		case FERIENBETREUUNG:
			return ferienbetreuungService.createFerienbetreuungAntrag(gemeinde, gesuchsperiode);
		default:
			throw new NotImplementedException("createGemeindeAntrag für andere Antragtypen noch nicht implementiert");
		}
	}

	@Nonnull
	@Override
	public List<? extends GemeindeAntrag> getGemeindeAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String typ,
		@Nullable String status) {

		if (typ != null) {
			switch (typ) {
			case "LASTENAUSGLEICH_TAGESSCHULEN": {
				return lastenausgleichTagesschuleAngabenGemeindeService.getLastenausgleicheTagesschulen(
					gemeinde, periode, status
				);
			}
			case "FERIENBETREUUNG": {
				return ferienbetreuungService.getFerienbetreuungAntraege(gemeinde, periode, status);
			}
			default:
				throw new NotImplementedException("getGemeindeAntraege Typ: " + typ + " wurde noch nicht implementiert");
			}
		}
		return getGemeindeAntraege(gemeinde, periode, status);

	}

	@Nonnull
	@Override
	public List<GemeindeAntrag> getGemeindeAntraege(
		@Nullable String gemeindeId,
		@Nullable String periodeId,
		@Nullable String status) {

		List<LastenausgleichTagesschuleAngabenGemeindeContainer> latsAntraege = lastenausgleichTagesschuleAngabenGemeindeService.getLastenausgleicheTagesschulen(
			gemeindeId, periodeId, status
		);

		List<FerienbetreuungAngabenContainer> ferienbetreuungAntraege = ferienbetreuungService.getFerienbetreuungAntraege(
			gemeindeId, periodeId, status
		);

		List<GemeindeAntrag> antraege = new ArrayList<>();
		antraege.addAll(latsAntraege);
		antraege.addAll(ferienbetreuungAntraege);
		return antraege;
	}

	@Nonnull
	public Optional<? extends GemeindeAntrag> findGemeindeAntrag(@Nonnull GemeindeAntragTyp typ, @Nonnull String gemeindeAntragId) {
		if (typ == GemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN) {
			return lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(gemeindeAntragId);
		}
		if (typ == GemeindeAntragTyp.FERIENBETREUUNG) {
			return ferienbetreuungService.findFerienbetreuungAngabenContainer(gemeindeAntragId);
		}

		return Optional.empty();
	}
}


