ALTER TABLE modul_tagesschule ADD COLUMN gesuchsperiode_id BINARY(16) NOT NULL;
ALTER TABLE modul_tagesschule_aud ADD COLUMN gesuchsperiode_id BINARY(16);

ALTER TABLE modul_tagesschule
	ADD CONSTRAINT FK_modul_tagesschule_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode (id);