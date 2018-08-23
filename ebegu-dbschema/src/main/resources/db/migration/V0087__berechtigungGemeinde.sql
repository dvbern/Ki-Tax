CREATE TABLE berechtigung_gemeinde (
	berechtigung_id VARCHAR(36) NOT NULL,
	gemeinde_id     VARCHAR(36) NOT NULL,
	PRIMARY KEY (berechtigung_id, gemeinde_id)
);

CREATE TABLE berechtigung_gemeinde_aud (
	rev             INTEGER     NOT NULL,
	berechtigung_id VARCHAR(36) NOT NULL,
	gemeinde_id     VARCHAR(36) NOT NULL,
	revtype         TINYINT,
	PRIMARY KEY (rev, berechtigung_id, gemeinde_id)
);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_gemeinde_id
FOREIGN KEY (berechtigung_id)
REFERENCES berechtigung (id);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_berechtigung_id
FOREIGN KEY (berechtigung_id)
REFERENCES berechtigung (id);

ALTER TABLE berechtigung_gemeinde_aud
	ADD CONSTRAINT FK_berechtigung_gemeinde_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

CREATE INDEX IX_berechtigung_gemeinde_berechtigung_id
	ON berechtigung_gemeinde (berechtigung_id);
CREATE INDEX IX_berechtigung_gemeinde_gemeinde_id
	ON berechtigung_gemeinde (gemeinde_id);
