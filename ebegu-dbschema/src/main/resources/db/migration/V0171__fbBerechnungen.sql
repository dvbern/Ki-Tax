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

CREATE TABLE ferienbetreuung_berechnungen(
    id binary(16) not null,
    timestamp_erstellt datetime not null,
    timestamp_mutiert datetime not null,
    user_erstellt varchar(255) not null,
    user_mutiert varchar(255) not null,
    version bigint not null,
    total_kosten DECIMAL(19,2),
    betreuungstage_kinder_dieser_gemeinde_minus_sonderschueler DECIMAL(19,2),
    betreuungstage_kinder_anderer_gemeinde_minus_sonderschueler DECIMAL(19,2),
    total_kantonsbeitrag DECIMAL(19,2),
    total_einnahmen DECIMAL(19,2),
    beitrag_kinder_anbietenden_gemeinde DECIMAL(19,2),
    beteiligung_anbietenden_gemeinde DECIMAL(19,2),
    beteiligung_zu_tief BIT(1),
    PRIMARY KEY (id)
);

CREATE TABLE ferienbetreuung_berechnungen_aud(
    id binary(16) not null,
    rev integer not null,
    revtype tinyint,
    timestamp_erstellt datetime,
    timestamp_mutiert datetime,
    user_erstellt varchar(255),
    user_mutiert varchar(255),
    total_kosten DECIMAL(19,2),
    betreuungstage_kinder_dieser_gemeinde_minus_sonderschueler DECIMAL(19,2),
    betreuungstage_kinder_anderer_gemeinde_minus_sonderschueler DECIMAL(19,2),
    total_kantonsbeitrag DECIMAL(19,2),
    total_einnahmen DECIMAL(19,2),
    beitrag_kinder_anbietenden_gemeinde DECIMAL(19,2),
    beteiligung_anbietenden_gemeinde DECIMAL(19,2),
    beteiligung_zu_tief BIT(1),
    PRIMARY KEY (id)
);

ALTER TABLE ferienbetreuung_angaben ADD IF NOT EXISTS ferienbetreuung_berechnungen_id BINARY(16);
ALTER TABLE ferienbetreuung_angaben_aud ADD IF NOT EXISTS ferienbetreuung_berechnungen_id BINARY(16);

alter table ferienbetreuung_angaben
    add constraint FK_ferienbetreuung_berechnungen_ferienbetreuung
        foreign key (ferienbetreuung_berechnungen_id)
            references ferienbetreuung_berechnungen(id);

ALTER TABLE ferienbetreuung_berechnungen_aud
    ADD CONSTRAINT FK_ferienbetreuung_berechnungen_aud
        FOREIGN key (rev) REFERENCES revinfo(rev);