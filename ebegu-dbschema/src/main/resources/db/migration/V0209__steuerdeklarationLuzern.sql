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

alter table finanzielle_situation add quellenbesteuert BIT;
alter table finanzielle_situation add gemeinsame_stek_vorjahr BIT;
alter table finanzielle_situation add alleinige_stek_vorjahr BIT;
alter table finanzielle_situation add veranlagt BIT;

alter table finanzielle_situation add steuerbares_einkommen DECIMAL(19, 2);
alter table finanzielle_situation add steuerbares_vermoegen DECIMAL(19, 2);
alter table finanzielle_situation add abzuege_liegenschaft DECIMAL(19, 2);
alter table finanzielle_situation add geschaeftsverlust DECIMAL(19, 2);
alter table finanzielle_situation add einkaeufe_vorsorge DECIMAL(19, 2);

alter table finanzielle_situation_aud add quellenbesteuert BIT;
alter table finanzielle_situation_aud add gemeinsame_stek_vorjahr BIT;
alter table finanzielle_situation_aud add alleinige_stek_vorjahr BIT;
alter table finanzielle_situation_aud add veranlagt BIT;

alter table finanzielle_situation_aud add steuerbares_einkommen DECIMAL(19, 2);
alter table finanzielle_situation_aud add steuerbares_vermoegen DECIMAL(19, 2);
alter table finanzielle_situation_aud add abzuege_liegenschaft DECIMAL(19, 2);
alter table finanzielle_situation_aud add geschaeftsverlust DECIMAL(19, 2);
alter table finanzielle_situation_aud add einkaeufe_vorsorge DECIMAL(19, 2);

alter table einkommensverschlechterung add steuerbares_einkommen DECIMAL(19, 2);
alter table einkommensverschlechterung add steuerbares_vermoegen DECIMAL(19, 2);
alter table einkommensverschlechterung add abzuege_liegenschaft DECIMAL(19, 2);
alter table einkommensverschlechterung add geschaeftsverlust DECIMAL(19, 2);
alter table einkommensverschlechterung add einkaeufe_vorsorge DECIMAL(19, 2);

alter table einkommensverschlechterung_aud add steuerbares_einkommen DECIMAL(19, 2);
alter table einkommensverschlechterung_aud add steuerbares_vermoegen DECIMAL(19, 2);
alter table einkommensverschlechterung_aud add abzuege_liegenschaft DECIMAL(19, 2);
alter table einkommensverschlechterung_aud add geschaeftsverlust DECIMAL(19, 2);
alter table einkommensverschlechterung_aud add einkaeufe_vorsorge DECIMAL(19, 2);