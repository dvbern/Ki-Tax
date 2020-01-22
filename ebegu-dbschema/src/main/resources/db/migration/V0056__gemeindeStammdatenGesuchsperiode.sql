CREATE TABLE gemeinde_stammdaten_gesuchsperiode (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	gemeinde_id        BINARY(16)   NOT NULL,
	gesuchsperiode_id  BINARY(16)   NOT NULL,
	merkblatt_anmeldung_tagesschule_de    longblob,
	merkblatt_anmeldung_tagesschule_fr    longblob,
	PRIMARY KEY (id)
);

CREATE TABLE gemeinde_stammdaten_gesuchsperiode_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	gemeinde_id        BINARY(16),
	gesuchsperiode_id  BINARY(16),
	merkblatt_anmeldung_tagesschule_de    longblob,
	merkblatt_anmeldung_tagesschule_fr    longblob,
	PRIMARY KEY (id, rev)
);


ALTER TABLE gemeinde_stammdaten_gesuchsperiode
	ADD CONSTRAINT FK_gemeinde_stammdaten_gesuchsperiode_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde(id);

ALTER TABLE gemeinde_stammdaten_gesuchsperiode
	ADD CONSTRAINT FK_gemeinde_stammdaten_gesuchsperiode_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode(id);

alter table gemeinde_stammdaten_gesuchsperiode_aud
	add constraint FK_gemeinde_stammdaten_gesuchsperiode_aud_revinfo
foreign key (rev)
references revinfo (rev);