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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE finanzielle_situation_selbstdeklaration ADD COLUMN IF NOT EXISTS abzug_schuldzinsen DECIMAL(19,2) NULL;
ALTER TABLE finanzielle_situation_selbstdeklaration_aud ADD COLUMN IF NOT EXISTS abzug_schuldzinsen DECIMAL(19,2) NULL;

UPDATE finanzielle_situation_selbstdeklaration s
    INNER JOIN finanzielle_situation fs on s.id = fs.selbstdeklaration_id
SET s.abzug_schuldzinsen = fs.abzug_schuldzinsen
WHERE fs.abzug_schuldzinsen IS NOT NULL;

UPDATE finanzielle_situation_selbstdeklaration s
	INNER JOIN einkommensverschlechterung e on s.id = e.selbstdeklaration_id
SET s.abzug_schuldzinsen = e.abzug_schuldzinsen
WHERE e.abzug_schuldzinsen IS NOT NULL;

