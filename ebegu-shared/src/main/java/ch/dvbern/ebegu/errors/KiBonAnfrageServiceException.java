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

package ch.dvbern.ebegu.errors;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;

public class KiBonAnfrageServiceException extends EbeguException {

	private static final long serialVersionUID = 5438097529958118878L;

	public KiBonAnfrageServiceException(final String methodname, final String message) {
		super(methodname, message, ErrorCodeEnum.ERROR_KIBON_ANFRAGE_TECHNICAL);
	}

	public KiBonAnfrageServiceException(final String methodname, final String message, final Throwable cause) {
		super(methodname, message, ErrorCodeEnum.ERROR_KIBON_ANFRAGE_TECHNICAL, cause);
	}

	public KiBonAnfrageServiceException(final String methodname, final String message, final String code, final String text) {
		super(methodname, message, ErrorCodeEnum.ERROR_KIBON_ANFRAGE_TECHNICAL, code, text);
	}

}
