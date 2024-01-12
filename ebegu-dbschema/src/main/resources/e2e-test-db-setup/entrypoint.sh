#!/bin/bash
#
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
#
set -o errexit

mariadb_args=(--host="${E2E_DB_HOST}" --user="${E2E_DB_USER}" --password="${E2E_DB_PASSWORD}" --database="${E2E_DB_NAME}")
flyway_args=(-driver=org.mariadb.jdbc.Driver -table=schema_version -url="${E2E_DB_URL}" -user="${E2E_DB_USER}" -password="${E2E_DB_PASSWORD}")

echo "${flyway_args[@]}"
echo "${mariadb_args[@]}"

echo "dropping everything in schema"
flyway "${flyway_args[@]}" -cleanDisabled="false" clean

echo "importing db dump" # to avoid broken migrations, see https://support.dvbern.ch/browse/KIBON-3277
mariadb "${mariadb_args[@]}" < ebegu-dump.sql


echo "insert test data for BE"
mariadb "${mariadb_args[@]}" < testdaten/create_testdata.sql

echo "insert test data for AR"
mariadb "${mariadb_args[@]}" < testdaten/create_testdata_ar.sql

echo "insert test data for LU"
mariadb "${mariadb_args[@]}" < testdaten/create_testdata_lu.sql

echo "insert test data for SO"
mariadb "${mariadb_args[@]}" < testdaten/create_testdata_so.sql

echo "migrating schema"
flyway "${flyway_args[@]}" migrate

echo "done"
