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

alter table familiensituation MODIFY unterhaltsvereinbarung VARCHAR(50);
alter table familiensituation_aud MODIFY column unterhaltsvereinbarung VARCHAR(50);

alter table familiensituation add column unterhaltsvereinbarung_bemerkung TEXT;
alter table familiensituation_aud add column unterhaltsvereinbarung_bemerkung TEXT;

UPDATE familiensituation set unterhaltsvereinbarung = 'JA' where unterhaltsvereinbarung = '1';
UPDATE familiensituation set unterhaltsvereinbarung = 'NEIN' where unterhaltsvereinbarung = '0';
UPDATE familiensituation_aud set unterhaltsvereinbarung = 'JA' where unterhaltsvereinbarung = '1';
UPDATE familiensituation_aud set unterhaltsvereinbarung = 'NEIN' where unterhaltsvereinbarung = '0';
