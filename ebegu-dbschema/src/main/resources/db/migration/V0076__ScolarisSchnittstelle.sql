CREATE TABLE gemeinde_stammdaten_external_client (
	gemeinde_stammdaten_id     BINARY(16) NOT NULL,
	external_client_id BINARY(16) NOT NULL,
	PRIMARY KEY (gemeinde_stammdaten_id, external_client_id)
);

CREATE TABLE gemeinde_stammdaten_external_client_aud (
	rev                INTEGER    NOT NULL,
	gemeinde_stammdaten_id     BINARY(16) NOT NULL,
	external_client_id BINARY(16) NOT NULL,
	revtype            TINYINT,
	PRIMARY KEY (rev, gemeinde_stammdaten_id, external_client_id)
);

ALTER TABLE gemeinde_stammdaten_external_client_aud
	ADD CONSTRAINT FK_gemeinde_stammdaten_external_client_aud_revinfo
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);

ALTER TABLE gemeinde_stammdaten_external_client
	ADD CONSTRAINT FK_gemeinde_stammdaten_external_clients_external_client_id
		FOREIGN KEY (external_client_id)
			REFERENCES external_client(id);

ALTER TABLE gemeinde_stammdaten_external_client
	ADD CONSTRAINT FK_gemeinde_stammdaten_external_clients_gemeinde_stammdaten_id
		FOREIGN KEY (gemeinde_stammdaten_id)
			REFERENCES gemeinde_stammdaten(id);


INSERT INTO external_client (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							 client_name, type)
VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')), now(), now(), 'flyway', 'flyway', 0, 'scolaris',
		'GEMEINDE_SCOLARIS_SERVICE');

ALTER TABLE gemeinde_stammdaten ADD username_scolaris VARCHAR(255);

ALTER TABLE gemeinde_stammdaten_aud ADD username_scolaris VARCHAR(255);
