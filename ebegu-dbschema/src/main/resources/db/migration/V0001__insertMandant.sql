INSERT INTO mandant
VALUES (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Bern');

INSERT INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
	VALUES (
	UNHEX(REPLACE('06c1e5d5-48c0-4f2d-af25-251b15de8ceb', '-', '')), # id
					'2018-01-01 00:00:00', # timestamp_erstellt
					'2018-01-01 00:00:00', # timestamp_mutiert
					'flyway', # user_erstellt
					'flyway', # user_mutiert
					0, # version
					'FALL_NUMMER', # sequence_type
					100, # current_value
					 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));

INSERT INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
	VALUES (
	UNHEX(REPLACE('44673705-b510-4618-8c14-8ce6e2d7d961', '-', '')), # id
				   '2018-01-01 00:00:00', # timestamp_erstellt
				   '2018-01-01 00:00:00', # timestamp_mutiert
				   'flyway', # user_erstellt
				   'flyway', # user_mutiert
				   0, # version
				   'GEMEINDE_NUMMER', # sequence_type
				   1, # current_value
				   UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));