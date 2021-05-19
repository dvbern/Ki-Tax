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

ALTER TABLE `ferienbetreuung_angaben_angebot`
	ADD COLUMN IF NOT EXISTS `anzahl_ferienwochen_sportferien` DECIMAL(19, 2) DEFAULT NULL;

ALTER TABLE `ferienbetreuung_angaben_angebot_aud`
	ADD COLUMN IF NOT EXISTS `anzahl_ferienwochen_sportferien` DECIMAL(19, 2) DEFAULT NULL;

UPDATE `ferienbetreuung_angaben_angebot` SET anzahl_ferienwochen_sportferien=0
	WHERE anzahl_ferienwochen_sportferien IS NULL AND NOT status='IN_BEARBEITUNG_GMEINDE';

ALTER TABLE `ferienbetreuung_angaben_angebot`
	ADD COLUMN IF NOT EXISTS `gemeinde_fuehrt_angebot_in_kooperation` BIT(1) DEFAULT NULL;

ALTER TABLE `ferienbetreuung_angaben_angebot_aud`
	ADD COLUMN IF NOT EXISTS `gemeinde_fuehrt_angebot_in_kooperation` BIT(1) DEFAULT NULL;

UPDATE `ferienbetreuung_angaben_angebot` SET gemeinde_fuehrt_angebot_in_kooperation=1
 	WHERE gemeinde_fuehrt_angebot_in_kooperation IS NULL AND NOT status='IN_BEARBEITUNG_GMEINDE';

ALTER TABLE ferienbetreuung_angaben_stammdaten MODIFY COLUMN seit_wann_ferienbetreuungen DATE;

ALTER TABLE ferienbetreuung_angaben_stammdaten_aud MODIFY COLUMN seit_wann_ferienbetreuungen DATE;