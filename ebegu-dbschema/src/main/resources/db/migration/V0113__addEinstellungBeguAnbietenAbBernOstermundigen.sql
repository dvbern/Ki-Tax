# set einstellung BEGU_ANBIETEN_AB for both Bern and Otermundigen
# default timestamp and value 2016-01-01 and default user flyway

# Ostermundigen
INSERT INTO einstellung (
	id,
	timestamp_erstellt,
	timestamp_mutiert,
	user_erstellt,
	user_mutiert,
	version,
	einstellung_key,
	value,
	gemeinde_id,
	gesuchsperiode_id,
	mandant_id
)
VALUES (
	UUID(),
	'2016-01-01 00:00:00',
	'2016-01-01 00:00:00',
	'flyway',
	'flyway',
	0,
	'BEGU_ANBIETEN_AB',
	'2016-01-01',
	'80a8e496-b73c-4a4a-a163-a0b2caf76487',
	'0621fb5d-a187-5a91-abaf-8a813c4d263a',
	'e3736eb8-6eef-40ef-9e52-96ab48d8f220'
);

# Bern
INSERT INTO einstellung (
	id,
	timestamp_erstellt,
	timestamp_mutiert,
	user_erstellt,
	user_mutiert,
	version,
	einstellung_key,
	value,
	gemeinde_id,
	gesuchsperiode_id,
	mandant_id
)
VALUES (
	UUID(),
	'2016-01-01 00:00:00',
	'2016-01-01 00:00:00',
	'flyway',
	'flyway',
	0,
	'BEGU_ANBIETEN_AB',
	'2016-01-01',
	'ea02b313-e7c3-4b26-9ef7-e413f4046db2',
	'0621fb5d-a187-5a91-abaf-8a813c4d263a',
	'e3736eb8-6eef-40ef-9e52-96ab48d8f220'
);
