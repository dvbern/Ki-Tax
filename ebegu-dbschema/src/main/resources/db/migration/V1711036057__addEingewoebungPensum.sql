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
CREATE TABLE eingewoehnung_pauschale (
     id                 BINARY(16)   NOT NULL,
     timestamp_erstellt DATETIME     NOT NULL,
     timestamp_mutiert  DATETIME     NOT NULL,
     user_erstellt      VARCHAR(255) NOT NULL,
     user_mutiert       VARCHAR(255) NOT NULL,
     version            BIGINT       NOT NULL,
     vorgaenger_id      VARCHAR(36),
     gueltig_ab         DATE         NOT NULL,
     gueltig_bis        DATE         NOT NULL,
     pauschale          decimal(19,2) not null,
     PRIMARY KEY (id)
);

CREATE TABLE eingewoehnung_pauschale_aud (
      id                 BINARY(16)   NOT NULL,
      rev                INTEGER      NOT NULL,
      revtype            TINYINT,
      timestamp_erstellt DATETIME,
      timestamp_mutiert  DATETIME,
      user_erstellt      VARCHAR(255),
      user_mutiert       VARCHAR(255),
      version            BIGINT,
      vorgaenger_id      VARCHAR(36),
      gueltig_ab         DATE,
      gueltig_bis        DATE,
      pauschale          decimal(19,2),
      PRIMARY KEY (id, rev)
);

alter table betreuungspensum add column eingewoehnung_pauschale_id binary(16);
alter table betreuungspensum_aud add column eingewoehnung_pauschale_id binary(16);

alter table betreuungspensum
    add constraint FK_betreuungspensum_eingewoehnung_pauschale_id
        foreign key (eingewoehnung_pauschale_id)
            references eingewoehnung_pauschale(id);
