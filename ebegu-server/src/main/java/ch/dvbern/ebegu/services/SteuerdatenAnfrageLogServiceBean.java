/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import ch.dvbern.ebegu.entities.SteuerdatenAnfrageLog;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(SteuerdatenAnfrageLogService.class)
public class SteuerdatenAnfrageLogServiceBean extends AbstractBaseService implements SteuerdatenAnfrageLogService {

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public SteuerdatenAnfrageLog saveSteuerdatenAnfrageLog(@Nonnull SteuerdatenAnfrageLog steuerdatenAnfrageLog) {
		Objects.requireNonNull(steuerdatenAnfrageLog, "SteuerdatenAnfrageLog muss gesetzt sein");
		return persistence.persist(steuerdatenAnfrageLog);
	}

}
