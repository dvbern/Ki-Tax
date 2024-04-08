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

package ch.dvbern.ebegu.entities.containers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import lombok.Value;

@Value
public class BetreuungAbweichung implements BetreuungAndPensumContainer {

	private final Betreuung betreuung;
	private final Set<BetreuungspensumAbweichung> abweichungen;

	@Nonnull
	@Override
	public List<? extends AbstractMahlzeitenPensum> getBetreuungenGS() {
		return List.of();
	}

	@Nonnull
	@Override
	public List<? extends AbstractMahlzeitenPensum> getBetreuungenJA() {
		return List.copyOf(abweichungen);
	}

	@Nonnull
	@Override
	public Optional<Betreuung> findBetreuung() {
		return Optional.of(betreuung);
	}
}
