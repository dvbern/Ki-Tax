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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;

/**
 * Exception die geworfen wird, wenn fuer einen Einstellungs-Key keine Einstellung gefunden wird: Weder auf Stufe Gemeinde, Mandant noch System.
 */
public class NoEinstellungFoundException extends EbeguRuntimeException {

	private EinstellungKey key;
	private Gemeinde gemeinde;
	private Gesuchsperiode gesuchsperiode;

	private static final long serialVersionUID = 7990451269130155438L;

	public NoEinstellungFoundException(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
	super(null, ErrorCodeEnum.ERROR_EINSTELLUNG_NOT_FOUND, key.name(), gemeinde.getId());
		this.key = key;
		this.gemeinde = gemeinde;
		this.gesuchsperiode = gesuchsperiode;
	}

	@Override
	public String getMessage() {
		return "Einstellung " + key + " not found for Gemeinde " + gemeinde.getName() + " and Gesuchsperiode " + gesuchsperiode.getGesuchsperiodeString();
	}
}
