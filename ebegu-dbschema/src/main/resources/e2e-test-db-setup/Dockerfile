# Copyright (C) 2023 DV Bern AG, Switzerland
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

FROM docker-registry.dvbern.ch/dockerhub/flyway/flyway:10-alpine

RUN apk update --no-cache && apk add mariadb-client

ADD db/migration/ /flyway/sql

WORKDIR /opt/e2e-test-db-setup

ADD db/testdaten testdaten/

ADD e2e-test-db-setup/entrypoint.sh .

COPY --from=docker-registry.dvbern.ch/kibon/kibon-e2e-db-dump /ebegu-dump.sql .

ENTRYPOINT ["./entrypoint.sh"]
