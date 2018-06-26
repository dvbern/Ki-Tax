CREATE TABLE berechtigung_gemeinde (
	berechtigung_id  VARCHAR(36) NOT NULL,
	gemeinde_list_id VARCHAR(36) NOT NULL,
	PRIMARY KEY (berechtigung_id, gemeinde_list_id)
);

CREATE TABLE berechtigung_gemeinde_aud (
	rev              INTEGER     NOT NULL,
	berechtigung_id  VARCHAR(36) NOT NULL,
	gemeinde_list_id VARCHAR(36) NOT NULL,
	revtype          TINYINT,
	PRIMARY KEY (rev, berechtigung_id, gemeinde_list_id)
);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_gemeinde_list_id
FOREIGN KEY (gemeinde_list_id)
REFERENCES gemeinde (id);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_berechtigung_id
FOREIGN KEY (berechtigung_id)
REFERENCES berechtigung (id);

ALTER TABLE berechtigung_gemeinde_aud
	ADD CONSTRAINT FK_berechtigung_gemeinde_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);