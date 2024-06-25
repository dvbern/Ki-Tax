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

ALTER TABLE fin_sit_zusatzangaben_appenzell
	ADD COLUMN IF NOT EXISTS zusatzangaben_partner_id BINARY(16) NULL;
ALTER TABLE fin_sit_zusatzangaben_appenzell_aud
	ADD COLUMN IF NOT EXISTS zusatzangaben_partner_id BINARY(16) NULL;

ALTER TABLE fin_sit_zusatzangaben_appenzell
	ADD COLUMN IF NOT EXISTS steuerbares_vermoegen DECIMAL(19,2) NULL;
ALTER TABLE fin_sit_zusatzangaben_appenzell_aud
	ADD COLUMN IF NOT EXISTS steuerbares_vermoegen DECIMAL(19,2) NULL;

ALTER TABLE fin_sit_zusatzangaben_appenzell
	ADD COLUMN IF NOT EXISTS steuerbares_einkommen DECIMAL(19,2) NULL;
ALTER TABLE fin_sit_zusatzangaben_appenzell_aud
	ADD COLUMN IF NOT EXISTS steuerbares_einkommen DECIMAL(19,2) NULL;

ALTER TABLE fin_sit_zusatzangaben_appenzell
	ADD CONSTRAINT FK_fin_sit_zusatzangaben_appenzell_partner
		FOREIGN KEY IF NOT EXISTS (zusatzangaben_partner_id)
			REFERENCES fin_sit_zusatzangaben_appenzell(id);