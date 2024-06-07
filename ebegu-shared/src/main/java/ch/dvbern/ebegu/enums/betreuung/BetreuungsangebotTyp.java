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

package ch.dvbern.ebegu.enums.betreuung;

import ch.dvbern.ebegu.util.BetreuungsangebotTypVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * Enum fuers Feld betreuungsangebotTyp in Institution.
 */
public enum BetreuungsangebotTyp {
	KITA {
		@Override
		public <T> T accept(BetreuungsangebotTypVisitor<T> visitor) {
			return visitor.visitKita();
		}
	},
	TAGESSCHULE {
		@Override
		public <T> T accept(BetreuungsangebotTypVisitor<T> visitor) {
			return visitor.visitTagesschule();
		}
	},
	MITTAGSTISCH {
		@Override
		public <T> T accept(BetreuungsangebotTypVisitor<T> visitor) {
			return visitor.visitMittagtisch();
		}
	},
	TAGESFAMILIEN {
		@Override
		public <T> T accept(BetreuungsangebotTypVisitor<T> visitor) {
			return visitor.visitTagesfamilien();
		}
	},
	FERIENINSEL {
		@Override
		public <T> T accept(BetreuungsangebotTypVisitor<T> visitor) {
			return visitor.visitFerieninsel();
		}
	};

	public boolean isKita() {
		return KITA == this;
	}

	public boolean isTagesschule() {
		return TAGESSCHULE == this;
	}

	public boolean isTagesfamilien() { return TAGESFAMILIEN == this; }

	public boolean isMittagstisch() {
		return MITTAGSTISCH == this;
	}

	public boolean isFerieninsel() {
		return FERIENINSEL == this;
	}

	public boolean isSchulamt() {
		return TAGESSCHULE == this || FERIENINSEL == this;
	}

	public boolean isAngebotJugendamtKleinkind() { return KITA == this || TAGESFAMILIEN == this; }

	public boolean isJugendamt() {
		return !isSchulamt();
	}

	public static List<BetreuungsangebotTyp> getSchulamtTypes() {
		return Arrays.asList(TAGESSCHULE, FERIENINSEL);
	}

	public static List<BetreuungsangebotTyp> getBetreuungsgutscheinTypes() {
		return List.of(KITA, TAGESFAMILIEN, MITTAGSTISCH);
	}

	public static List<BetreuungsangebotTyp> getBerechnetesAngebotTypes() {
		return List.of(KITA, TAGESFAMILIEN, MITTAGSTISCH, TAGESSCHULE);
	}

	public boolean isBerechnetesAngebot() {
		return getBerechnetesAngebotTypes().contains(this);
	}

	public boolean isBetreuungsgutscheinAngebot() {
		return getBetreuungsgutscheinTypes().contains(this);
	}

	public abstract <T> T accept(BetreuungsangebotTypVisitor<T> visitor);
}
