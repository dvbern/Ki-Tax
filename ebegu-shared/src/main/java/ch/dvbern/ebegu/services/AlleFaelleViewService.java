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

package ch.dvbern.ebegu.services;

import java.util.List;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;

public interface AlleFaelleViewService {

	boolean isNeueAlleFaelleViewActivated();

	void createViewForFullGesuch(Gesuch gesuch);

	void updateViewForGesuch(Gesuch gesuch);

	void removeViewForGesuch(Gesuch gesuch);

	void createKindInView(Kind kind, Gesuch gesuch);

	void updateKindInView(Kind kind);

	void removeKindInView(Kind kind);

	Long countAllGesuch();

	List<String> searchAllGesuchIds(int start, int size);
}
