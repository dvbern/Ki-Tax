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

alter table download_file
add COLUMN if not EXISTS benutzer_id BINARY(16);

update download_file
inner join benutzer
on SUBSTRING_INDEX(download_file.user_erstellt, ':', 1) = benutzer.username
set download_file.benutzer_id = benutzer.id;


ALTER TABLE download_file
ADD CONSTRAINT FK_download_file_benutzer_id
	FOREIGN KEY (benutzer_id)
		REFERENCES benutzer(id);

alter table download_file
	modify benutzer_id BINARY(16) NOT NULL;