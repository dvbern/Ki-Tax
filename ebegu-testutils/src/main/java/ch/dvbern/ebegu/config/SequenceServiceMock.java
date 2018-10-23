/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.config;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.SequenceType;
import ch.dvbern.ebegu.services.SequenceService;

@Alternative
@Priority(1) // Somehow WELD only loads this as an alternative when it has a higher priority
@Dependent
public class SequenceServiceMock implements SequenceService {

	@Nonnull
	private final AtomicLong sequence = new AtomicLong();

	@Nonnull
	@Override
	public Long createNumberTransactional(@Nonnull SequenceType seq, @Nonnull Mandant mandant) {
		return sequence.incrementAndGet();
	}
}
