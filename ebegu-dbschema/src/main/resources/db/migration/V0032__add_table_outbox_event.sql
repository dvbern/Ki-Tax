CREATE TABLE outbox_event (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	aggregate_id       VARCHAR(255) NOT NULL,
	aggregate_type     VARCHAR(255) NOT NULL,
	payload            LONGBLOB     NOT NULL,
	type               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);
