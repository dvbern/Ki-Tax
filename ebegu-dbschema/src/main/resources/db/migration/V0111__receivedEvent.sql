CREATE TABLE received_event (
	id                 	BINARY(16)   NOT NULL,
	timestamp_erstellt 	datetime not null,
	timestamp_mutiert 	datetime not null,
	user_erstellt 		varchar(255) not null,
	user_mutiert 		varchar(255) not null,
	version 			bigint not null,
	event_id			VARCHAR(255) NOT NULL,
	event_key      		VARCHAR(255) NOT NULL,
	event_type      	VARCHAR(255) NOT NULL,
	event_timestamp       	DATETIME NOT NULL,
	event_dto			longblob NOT NULL,
	PRIMARY KEY (id)
);
