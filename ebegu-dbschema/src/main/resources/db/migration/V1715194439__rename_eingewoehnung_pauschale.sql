/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

ALTER TABLE eingewoehnung_pauschale RENAME TO eingewoehnung;
ALTER TABLE eingewoehnung_pauschale_aud RENAME TO eingewoehnung_aud;
ALTER TABLE eingewoehnung
	CHANGE pauschale kosten DECIMAL(19, 2) NOT NULL;
ALTER TABLE eingewoehnung_aud
	CHANGE pauschale kosten DECIMAL(19, 2) NULL;

# region betreuungsmitteilung_pensum
ALTER TABLE betreuungsmitteilung_pensum
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;
ALTER TABLE betreuungsmitteilung_pensum_aud
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;

ALTER TABLE betreuungsmitteilung_pensum
	ADD CONSTRAINT FK_betreuungsmitteilung_pensum_eingewoehnung_id
		FOREIGN KEY (eingewoehnung_id)
			REFERENCES eingewoehnung(id),
	DROP CONSTRAINT FK_betreuungsmitteilung_pensum_eingewoehnung_pauschale_id;
# endregion

# region betreuungspensum
ALTER TABLE betreuungspensum
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;
ALTER TABLE betreuungspensum_aud
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;

ALTER TABLE betreuungspensum
	ADD CONSTRAINT FK_betreuungspensum_pensum_eingewoehnung_id
		FOREIGN KEY (eingewoehnung_id)
			REFERENCES eingewoehnung(id),
	DROP CONSTRAINT FK_betreuungspensum_eingewoehnung_pauschale_id;
# endregion

# region betreuungspensum_abweichung
ALTER TABLE betreuungspensum_abweichung
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;
ALTER TABLE betreuungspensum_abweichung_aud
	CHANGE eingewoehnung_pauschale_id eingewoehnung_id BINARY(16) NULL;

ALTER TABLE betreuungspensum_abweichung
	ADD CONSTRAINT FK_betreuungspensum_abweichung_eingewoehnung_id
		FOREIGN KEY (eingewoehnung_id)
			REFERENCES eingewoehnung(id),
	DROP CONSTRAINT FK_betreuungspensum_abweichung_eingewoehnung_pauschale_id;
# endregion
