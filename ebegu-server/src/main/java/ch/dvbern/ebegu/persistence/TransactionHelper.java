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

package ch.dvbern.ebegu.persistence;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

/**
 * Darf in dieser Form nur als CDI-Bean genutzt werden, sonst gibts Probleme im Transaction-Handling
 * (Exceptions in EJBs haben immer einen Rollback der globalen Transaktion zur Folge)
 */
@Dependent
public class TransactionHelper {

	@Transactional(TxType.REQUIRES_NEW)
	public void runInNewTransaction(@Nonnull Runnable r) {
		r.run();
	}
}
