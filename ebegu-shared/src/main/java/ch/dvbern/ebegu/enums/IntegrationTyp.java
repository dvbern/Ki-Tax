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

package ch.dvbern.ebegu.enums;

import java.util.Locale;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * Integrationstyp for Fachstellen
 */
public enum IntegrationTyp {
	SOZIALE_INTEGRATION {
		@Override
		public String getIndikationMessage(Locale locale, Mandant mandant) {
			return ServerMessageUtil.getMessage("Sozialen_Indikation", locale, mandant);
		}
	},
	SPRACHLICHE_INTEGRATION {
		@Override
		public String getIndikationMessage(Locale locale, Mandant mandant) {
			return ServerMessageUtil.getMessage("Sprachlichen_Indikation", locale, mandant);
		}
	},
	ZUSATZLEISTUNG_INTEGRATION {
		@Override
		public String getIndikationMessage(Locale locale, Mandant mandant) {
			return ServerMessageUtil.getMessage("Zusatzleistung_Indikation", locale, mandant);
		}
	};

	public abstract String getIndikationMessage(Locale locale, Mandant mandant);
}
