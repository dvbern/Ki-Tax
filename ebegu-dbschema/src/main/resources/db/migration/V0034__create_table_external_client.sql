CREATE TABLE external_client_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	client_name        VARCHAR(255),
	type               VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE external_client (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	client_name        VARCHAR(255) NOT NULL,
	type               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE institution_external_client_aud (
	rev                INTEGER    NOT NULL,
	institution_id     BINARY(16) NOT NULL,
	external_client_id BINARY(16) NOT NULL,
	revtype            TINYINT,
	PRIMARY KEY (rev, institution_id, external_client_id)
);

CREATE TABLE institution_external_client (
	institution_id     BINARY(16) NOT NULL,
	external_client_id BINARY(16) NOT NULL,
	PRIMARY KEY (institution_id, external_client_id)
);

ALTER TABLE external_client
	ADD CONSTRAINT UK_external_client UNIQUE (client_name, type);

CREATE INDEX IX_institution_external_clients_institution_id ON institution_external_client(institution_id);
CREATE INDEX IX_institution_external_clients_external_client_id ON institution_external_client(external_client_id);

ALTER TABLE external_client_aud
	ADD CONSTRAINT FKbc90oqbkj73b3780uq5we8tq
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);

ALTER TABLE institution_external_client_aud
	ADD CONSTRAINT FKo8vfhtjnae4f903lr1oudujrp
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);

ALTER TABLE institution_external_client
	ADD CONSTRAINT FK_institution_external_clients_external_client_id
		FOREIGN KEY (external_client_id)
			REFERENCES external_client(id);

ALTER TABLE institution_external_client
	ADD CONSTRAINT FK_institution_external_clients_institution_id
		FOREIGN KEY (institution_id)
			REFERENCES institution(id);

INSERT INTO external_client (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							 client_name, type)
VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')), now(), now(), 'flyway', 'flyway', 0, 'kitAdmin',
		'EXCHANGE_SERVICE_USER');

