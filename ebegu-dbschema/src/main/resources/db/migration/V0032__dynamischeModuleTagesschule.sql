ALTER TABLE modul_tagesschule ADD COLUMN gesuchsperiode_id BINARY(16) NOT NULL;
ALTER TABLE modul_tagesschule_aud ADD COLUMN gesuchsperiode_id BINARY(16);

ALTER TABLE modul_tagesschule ADD COLUMN bezeichnung varchar(255) not null;
ALTER TABLE modul_tagesschule_aud ADD COLUMN bezeichnung varchar(255);

ALTER TABLE modul_tagesschule ADD COLUMN intervall varchar(255) not null;
ALTER TABLE modul_tagesschule_aud ADD COLUMN intervall varchar(255);

ALTER TABLE modul_tagesschule ADD COLUMN verpflegungskosten decimal(19,2);
ALTER TABLE modul_tagesschule_aud ADD COLUMN verpflegungskosten decimal(19,2);

ALTER TABLE modul_tagesschule ADD COLUMN wird_paedagogisch_betreut bit not null;
ALTER TABLE modul_tagesschule_aud ADD COLUMN wird_paedagogisch_betreut bit;

ALTER TABLE modul_tagesschule
	ADD CONSTRAINT FK_modul_tagesschule_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode (id);