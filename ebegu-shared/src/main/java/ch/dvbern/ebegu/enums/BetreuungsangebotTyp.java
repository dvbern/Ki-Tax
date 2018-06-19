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

import java.util.Arrays;
import java.util.List;

/**
 * Enum fuers Feld betreuungsangebotTyp in Institution.
 */
public enum BetreuungsangebotTyp {
	KITA,
	TAGESSCHULE,
	TAGESELTERN_KLEINKIND,
	TAGESELTERN_SCHULKIND,
	TAGI,
	FERIENINSEL;

	public boolean isKita() {
		return KITA == this;
	}

	public boolean isTagesschule() {
		return TAGESSCHULE == this;
	}

	public boolean isTageselternKleinkind() {
		return TAGESELTERN_KLEINKIND == this;
	}

	public boolean isTageselternSchulkind() {
		return TAGESELTERN_SCHULKIND == this;
	}

	public boolean isTagi() {
		return TAGI == this;
	}

	public boolean isFerieninsel() {
		return FERIENINSEL == this;
	}

	public boolean isSchulamt() {
		return TAGESSCHULE == this || FERIENINSEL == this;
	}

	public boolean isTageseltern() {
		return TAGESELTERN_KLEINKIND == this || TAGESELTERN_SCHULKIND == this;
	}

	public boolean isAngebotJugendamtKleinkind() {
		return KITA == this || TAGESELTERN_KLEINKIND == this;
	}

	public boolean isAngebotJugendamtSchulkind() {
		return TAGI == this || TAGESELTERN_SCHULKIND == this;
	}

	public boolean isJugendamt() {
		return !isSchulamt();
	}

	public static List<BetreuungsangebotTyp> getSchulamtTypes() {
		return Arrays.asList(TAGESSCHULE, FERIENINSEL);
	}
}
