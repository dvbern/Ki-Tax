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

package ch.dvbern.ebegu.errors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;

public class EntityExistsException extends EbeguRuntimeException {

	private static final long serialVersionUID = -7412913095961290661L;

	public <T extends AbstractEntity> EntityExistsException(
		@Nonnull Class<T> entityClass,
		@Nonnull String constraintName,
		@Nonnull String duplicateValue) {

		super(null, ErrorCodeEnum.ERROR_ENTITY_EXISTS, entityClass.getSimpleName(), constraintName, duplicateValue);
	}

	public <T extends AbstractEntity> EntityExistsException(
		@Nonnull Class<T> entityClass,
		@Nonnull String constraintName,
		@Nonnull String duplicateValue,
		@Nonnull ErrorCodeEnum errorCodeEnum) {

		super(null, errorCodeEnum, entityClass.getSimpleName(), constraintName, duplicateValue);
	}
}
