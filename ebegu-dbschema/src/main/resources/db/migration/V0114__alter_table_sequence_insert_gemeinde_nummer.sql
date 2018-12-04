INSERT INTO sequence(id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 current_value,
					 sequence_type,
					 mandant_id)
SELECT UUID(),
	   now(),
	   now(),
	   'flyway',
	   'flyway',
	   0,
	   NULL,
	   m.next_number_gemeinde,
	   'GEMEINDE_NUMMER',
	   m.id
FROM mandant m;

ALTER TABLE mandant
	DROP COLUMN next_number_gemeinde;
