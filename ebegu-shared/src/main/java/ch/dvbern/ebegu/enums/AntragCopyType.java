/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.util.EnumUtil;

/**
 * Enum fuer den Status Events vom Gesuch.
 */
public enum AntragCopyType {
	MUTATION, // entspricht einer Mutation im selben Dossier/Gemeinde
	ERNEUERUNG, // Erneuerungsgesuch im selben Dossier/Gemeinde
	MUTATION_NEUES_DOSSIER, // Antrag in einem neuen Dossier/Gemeinde in der selben Gesuchsperiode
	ERNEUERUNG_NEUES_DOSSIER; // Erneuerungsgesuch in einem neuen Dossier/Gemeinde

	public boolean isNeuesDossier() {
		return EnumUtil.isOneOf(this, MUTATION_NEUES_DOSSIER, ERNEUERUNG_NEUES_DOSSIER);
	}

	public boolean isGleichesDossier() {
		return EnumUtil.isOneOf(this, MUTATION, ERNEUERUNG);
	}
}
