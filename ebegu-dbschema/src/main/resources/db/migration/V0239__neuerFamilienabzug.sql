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

alter table kind add is_pflegekind bit not null default 0;
alter table kind add pflege_entschaedigung_erhalten bit;
alter table kind add obhut_alternierend_ausueben bit;
alter table kind add gemeinsames_gesuch bit;
alter table kind add in_erstausbildung bit;
alter table kind add lebt_kind_alternierend bit;
alter table kind add alimente_erhalten bit;
alter table kind add alimente_bezahlen bit;

alter table kind_aud add is_pflegekind bit not null default 0;
alter table kind_aud add pflege_entschaedigung_erhalten bit;
alter table kind_aud add obhut_alternierend_ausueben bit;
alter table kind_aud add gemeinsames_gesuch bit;
alter table kind_aud add in_erstausbildung bit;
alter table kind_aud add lebt_kind_alternierend bit;
alter table kind_aud add alimente_erhalten bit;
alter table kind_aud add alimente_bezahlen bit;

