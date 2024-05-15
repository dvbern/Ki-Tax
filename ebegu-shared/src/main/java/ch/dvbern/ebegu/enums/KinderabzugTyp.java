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

import ch.dvbern.ebegu.util.KinderabzugTypVisitor;

/**
 * Enum fuer Typ des Kinderabzuges
 */
public enum KinderabzugTyp {
	ASIV {
		@Override
		public <T> T accept(KinderabzugTypVisitor<T> visitor) {
			return visitor.visitASIV();
		}
	},
	FKJV {
		@Override
		public <T> T accept(KinderabzugTypVisitor<T> visitor) {
			return visitor.visitFKJV();
		}
	},
	FKJV_2 {
		@Override
		public <T> T accept(KinderabzugTypVisitor<T> visitor) {
			return visitor.visitFKJV2();
		}
	},
	SCHWYZ {
		@Override
		public <T> T accept(KinderabzugTypVisitor<T> visitor) {
			return visitor.visitSchwyz();
		}
	},
	KEINE {
		@Override
		public <T> T accept(KinderabzugTypVisitor<T> visitor) {
			return visitor.visitKeine();
		}
	};

	public abstract <T> T accept(KinderabzugTypVisitor<T> visitor);
	public boolean isFKJV() {
		return this == FKJV || this == FKJV_2;
	}
}
