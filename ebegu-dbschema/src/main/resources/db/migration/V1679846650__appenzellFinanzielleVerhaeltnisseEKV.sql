/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

ALTER TABLE einkommensverschlechterung
        ADD COLUMN fin_sit_zusatzangaben_appenzell_id BINARY(16);

ALTER TABLE einkommensverschlechterung_aud
    ADD COLUMN fin_sit_zusatzangaben_appenzell_id BINARY(16);

ALTER TABLE einkommensverschlechterung
    ADD CONSTRAINT FK_einkommensverschlechterung_fin_sit_zusatzangaben_appenzell_id
        FOREIGN KEY (fin_sit_zusatzangaben_appenzell_id)
            REFERENCES fin_sit_zusatzangaben_appenzell(id);
