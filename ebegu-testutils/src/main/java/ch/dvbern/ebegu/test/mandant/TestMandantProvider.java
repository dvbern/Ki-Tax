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

package ch.dvbern.ebegu.test.mandant;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class TestMandantProvider implements MandantVisitor<Mandant> {

	@Override
	public Mandant visitBern() {
		return TestDataUtil.getMandantKantonBern();
	}

	@Override
	public Mandant visitLuzern() {
		return TestDataUtil.getMandantLuzern();
	}

	@Override
	public Mandant visitSolothurn() {
		Mandant mandant = TestDataUtil.createDefaultMandant();
		mandant.setMandantIdentifier(MandantIdentifier.SOLOTHURN);
		mandant.setName("Solothurn");

		return mandant;
	}

	@Override
	public Mandant visitAppenzellAusserrhoden() {
		Mandant mandant = TestDataUtil.createDefaultMandant();
		mandant.setMandantIdentifier(MandantIdentifier.APPENZELL_AUSSERRHODEN);
		mandant.setName("Appenzell Ausserrhoden");

		return mandant;
	}

	@Override
	public Mandant visitSchwyz() {
		return TestDataUtil.getMandantSchwyz();
	}
}
