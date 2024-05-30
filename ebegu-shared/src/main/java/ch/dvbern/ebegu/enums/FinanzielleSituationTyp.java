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

import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;

/**
 * Enum fuer Status der FinanziellenSituation
 */
public enum FinanzielleSituationTyp {
	BERN {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) {
			return visitor.visitFinSitBern();
		}
	},
	LUZERN {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) {
			return visitor.visitFinSitLuzern();
		}
	},
	SOLOTHURN {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) {
			return visitor.visitFinSitSolothurn();
		}
	},
	BERN_FKJV {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) {
			return visitor.visitFinSitBernFKJV();
		}
	},
	APPENZELL {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) { return visitor.visitFinSitAppenzell();}
	},
	APPENZELL_FOLGEMONAT {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) { return visitor.visitFinSitAppenzellFolgemonat();}
	},
	SCHWYZ {
		@Override
		public <T> T accept(FinanzielleSituationTypVisitor<T> visitor) { return visitor.visitFinSitSchwyz();}
	};

	public abstract <T> T accept(FinanzielleSituationTypVisitor<T> visitor);
}
