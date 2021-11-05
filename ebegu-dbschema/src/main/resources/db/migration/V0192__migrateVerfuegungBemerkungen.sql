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
DROP PROCEDURE IF EXISTS PROCESS_BEMERKUNGEN;
DROP PROCEDURE IF EXISTS INSERT_BEMERKUNG;
DROP PROCEDURE IF EXISTS GET_NOT_MIGRATED_VERFUEGUNG_ZEITABSCHNITT_ID;
DROP PROCEDURE IF EXISTS MIGRATE_NOT_MIGRATED_VERFUEGUNGS_BEMERKUNG;

DELIMITER ;;

CREATE PROCEDURE INSERT_BEMERKUNG(IN bemerkungInput VARCHAR(4000), IN zeitabschnittID BINARY(16))
	BEGIN
		set @gueltigAb = (select gueltig_ab FROM verfuegung_zeitabschnitt where id = zeitabschnittID);
		set @gueltigBis = (select gueltig_bis FROM verfuegung_zeitabschnitt WHERE id = zeitabschnittID);

		INSERT INTO verfuegung_zeitabschnitt_bemerkung (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,
														user_mutiert,version,gueltig_ab,gueltig_bis, bemerkung, verfuegung_zeitabschnitt_id)
		VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci , '-', '')),current_timestamp,current_timestamp,'flyway',
				'flyway',1,@gueltigAb, @gueltigBis, bemerkungInput, zeitabschnittID);
	END ;;

CREATE PROCEDURE PROCESS_BEMERKUNGEN(IN zeitabschnittID BINARY(16))
	BEGIN
		set @bemerkungen = (select bemerkungen from verfuegung_zeitabschnitt where id = zeitabschnittID);

		/*Anzahl \n +1 = Anzahl der Bemerkungen*/
		set @anzahlBemerkungenToProcess = (select (CHAR_LENGTH(@bemerkungen)-CHAR_LENGTH(REPLACE(@bemerkungen, '\n', '')))+1);

		WHILE @anzahlBemerkungenToProcess>0 DO
			set @count = @anzahlBemerkungenToProcess*(-1);
			set @bemerkung = SUBSTRING_INDEX(@bemerkungen, '\n', @count);
			CALL INSERT_BEMERKUNG(@bemerkung, zeitabschnittID);
			set @anzahlBemerkungenToProcess = @anzahlBemerkungenToProcess-1;
		END WHILE;
	END ;;

CREATE PROCEDURE GET_NOT_MIGRATED_VERFUEGUNG_ZEITABSCHNITT_ID(IN limitOffset INT, OUT zeitAbschnittID BINARY(16))
	BEGIN
		PREPARE stmt FROM 'select vz.id from verfuegung_zeitabschnitt_bemerkung vzb
									   right join verfuegung_zeitabschnitt vz on vz.id = vzb.verfuegung_zeitabschnitt_id
									   where vzb.verfuegung_zeitabschnitt_id is null limit ?, 1 INTO @result';
		EXECUTE stmt USING limitOffset;
		DEALLOCATE PREPARE stmt;
		set zeitAbschnittID = @result;
	END ;;

CREATE PROCEDURE MIGRATE_NOT_MIGRATED_VERFUEGUNGS_BEMERKUNG()
	BEGIN
		set @anzahlZeitabschitteNotMigrated = (select Count(*) from verfuegung_zeitabschnitt_bemerkung vzb
			right join verfuegung_zeitabschnitt vz on vz.id = vzb.verfuegung_zeitabschnitt_id
			where vzb.verfuegung_zeitabschnitt_id is null);
		set @iter = 0;

		WHILE @iter<@anzahlZeitabschitteNotMigrated DO
			CALL GET_NOT_MIGRATED_VERFUEGUNG_ZEITABSCHNITT_ID(@iter, @zeitAbschnittId);
			CALL PROCESS_BEMERKUNGEN(@zeitAbschnittId);
			SET @iter = @iter+1;
		END WHILE;
	END;;

CALL MIGRATE_NOT_MIGRATED_VERFUEGUNGS_BEMERKUNG();