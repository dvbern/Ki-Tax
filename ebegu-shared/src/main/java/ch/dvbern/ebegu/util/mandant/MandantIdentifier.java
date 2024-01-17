/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util.mandant;

import java.util.List;

public enum MandantIdentifier {

	BERN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitBern();
		}

		@Override
		public String getUrlCode() {
			return "be";
		}
	},
	LUZERN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitLuzern();
		}

		@Override
		public String getUrlCode() {
			return "stadtluzern";
		}
	},
	SOLOTHURN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitSolothurn();
		}

		@Override
		public String getUrlCode() {
			return "so";
		}
	},
	APPENZELL_AUSSERRHODEN {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitAppenzellAusserrhoden();
		}

		@Override
		public String getUrlCode() {
			return "ar";
		}
	},
	SCHWYZ {
		@Override
		public <T> T accept(MandantVisitor<T> visitor) {
			return visitor.visitSchwyz();
		}

		@Override
		public String getUrlCode() {
			return "schwyz";
		}
	};

	public abstract <T> T accept(MandantVisitor<T> visitor);
	public abstract String getUrlCode();

	public static List<MandantIdentifier> getAll() {
		return List.of(MandantIdentifier.values());
	}
}

