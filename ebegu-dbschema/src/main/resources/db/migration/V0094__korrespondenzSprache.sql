CREATE TABLE gesuchsteller_korrespondenz_sprachen (
	gesuchsteller_id       VARCHAR(36) NOT NULL,
	korrespondenz_sprachen VARCHAR(255)
);

CREATE TABLE gesuchsteller_korrespondenz_sprachen_aud (
	rev                    INTEGER      NOT NULL,
	gesuchsteller_id       VARCHAR(36)  NOT NULL,
	korrespondenz_sprachen VARCHAR(255) NOT NULL,
	revtype                TINYINT,
	PRIMARY KEY (rev, gesuchsteller_id, korrespondenz_sprachen)
);

ALTER TABLE gesuchsteller_korrespondenz_sprachen
	ADD CONSTRAINT FK_gesuchsteller_korrespondenz_sprachen_gesuchsteller_id
FOREIGN KEY (gesuchsteller_id)
REFERENCES gesuchsteller (id);

ALTER TABLE gesuchsteller_korrespondenz_sprachen_aud
	ADD CONSTRAINT FK_gesuchsteller_korrespondenz_sprachen_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);