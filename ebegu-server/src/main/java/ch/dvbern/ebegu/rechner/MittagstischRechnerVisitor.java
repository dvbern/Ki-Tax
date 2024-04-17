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
 *
 */

package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class MittagstischRechnerVisitor implements MandantVisitor<AbstractRechner> {

	@Override
	public AbstractRechner visitBern() {
		throw new UnsupportedOperationException("No MittagstischRechner implemented for Mandant Bern");
	}

	@Override
	public AbstractRechner visitLuzern() {
		throw new UnsupportedOperationException("No MittagstischRechner implemented for Mandant Luzern");
	}

	@Override
	public AbstractRechner visitSolothurn() {
		throw new UnsupportedOperationException("No MittagstischRechner implemented for Mandant Solothurn");
	}

	@Override
	public AbstractRechner visitAppenzellAusserrhoden() {
		throw new UnsupportedOperationException("No MittagstischRechner implemented for Mandant AppenzellAusserrhoden");
	}

	@Override
	public AbstractRechner visitSchwyz() {
		return new MittagstischSchwyzRechner();
	}
}
