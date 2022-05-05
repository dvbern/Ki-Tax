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

create table belegung_ferieninsel_morgen_belegung_ferieninsel_tag_aud (
	rev integer not null,
	tage_id binary(16) not null,
	revtype tinyint,
	belegung_ferieninsel_morgen_id binary(16) not null,
	primary key (rev, belegung_ferieninsel_morgen_id, tage_id)
);

create table belegung_ferieninsel_morgen_belegung_ferieninsel_tag (
	belegung_ferieninsel_morgen_id binary(16) not null,
	tage_id binary(16) not null
);

alter table belegung_ferieninsel_morgen_belegung_ferieninsel_tag
	add constraint FK_belegung_ferieninsel_belegung_morgen_ferieninsel_id
		foreign key (belegung_ferieninsel_morgen_id)
			references belegung_ferieninsel (id);

create index IX_belegung_ferieninsel_belegung_morgen_ferieninsel_id on belegung_ferieninsel_morgen_belegung_ferieninsel_tag (belegung_ferieninsel_morgen_id);
create index IX_belegung_morgen_ferieninsel_tage_id on belegung_ferieninsel_morgen_belegung_ferieninsel_tag (tage_id);
