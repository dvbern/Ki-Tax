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

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.SchulungConstants;

public class SchulungConstantsVisitor implements MandantVisitor<SchulungConstants> {

	public SchulungConstants process(final Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public SchulungConstants visitBern() {
		return new SchulungConstants(
				"11111111-1111-4444-4444-111111111111",
				"11111111-1111-4444-4444-111111111112",
				"11111111-1111-1111-1111-111111111111",
				"22222222-1111-1111-1111-111111111111",
				"22222222-1111-1111-1111-222222222222",
				"22222222-1111-1111-1111-333333333333",
				"22222222-1111-1111-1111-444444444444",
				"33333333-1111-1111-1111-111111111111",
				"33333333-1111-1111-2222-111111111111",
				"33333333-1111-1111-1111-222222222222",
				"33333333-1111-1111-1111-444444444444",
				"9a0eb656-b6b7-4613-8f55-4e0e4720455e",
				"44444444-1111-1111-1111-1111111111XX"
		);
	}

	@Override
	public SchulungConstants visitLuzern() {
		return new SchulungConstants(
				"11111112-1112-4444-4444-111111111111",
				"11111111-1112-4444-4444-111111111112",
				"11111111-1112-1111-1111-111111111111",
				"22222222-1112-1111-1111-111111111111",
				"22222222-1112-1111-1111-222222222222",
				"22222222-1112-1111-1111-333333333333",
				"22222222-1112-1111-1111-444444444444",
				"33333333-1112-1111-1111-111111111111",
				"33333333-1112-1111-2222-111111111111",
				"33333333-1112-1111-1111-222222222222",
				"33333333-1112-1111-1111-444444444444",
				"6d6afdb2-3261-11ec-a17e-b89a2ae4a038",
				"44444444-1112-1111-1111-1111111111XX"
		);
	}

	@Override
	public SchulungConstants visitSolothurn() {
		return new SchulungConstants(
				"11111111-1113-4444-4444-111111111111",
				"11111111-1113-4444-4444-111111111112",
				"11111111-1113-1111-1111-111111111111",
				"22222222-1113-1111-1111-111111111111",
				"22222222-1113-1111-1111-222222222222",
				"22222222-1113-1111-1111-333333333333",
				"22222222-1113-1111-1111-444444444444",
				"33333333-1113-1111-1111-111111111111",
				"33333333-1113-1111-2222-111111111111",
				"33333333-1113-1111-1111-222222222222",
				"33333333-1113-1111-1111-444444444444",
				"6aa08c20-537f-11ec-98e8-f4390979fa3e",
				"44444444-1113-1111-1111-1111111111XX"
		);
	}
}
