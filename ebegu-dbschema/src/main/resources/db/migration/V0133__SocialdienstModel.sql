 create table sozialdienst_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
       	name VARCHAR(255),
		status VARCHAR(255),
		mandant_id BINARY(16),
        primary key (id, rev)
    );

    create table sozialdienst (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
       	name VARCHAR(255) NOT NULL,
		status VARCHAR(255) NOT NULL,
		mandant_id BINARY(16) NOT NULL,
        primary key (id)
    );

     create table sozialdienst_stammdaten_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
        mail VARCHAR(255),
		telefon VARCHAR(255),
		webseite VARCHAR(255),
		adresse_id BINARY(16),
		sozialdienst_id BINARY(16),
        primary key (id, rev)
    );

    create table sozialdienst_stammdaten (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
       	mail VARCHAR(255) NOT NULL,
       	telefon VARCHAR(255) NOT NULL,
		webseite VARCHAR(255),
		adresse_id BINARY(16)   NOT NULL,
		sozialdienst_id BINARY(16)   NOT NULL,
        primary key (id)
    );

ALTER TABLE sozialdienst
	ADD CONSTRAINT UK_sozialdienst_name UNIQUE (name);

ALTER TABLE sozialdienst_stammdaten
	ADD CONSTRAINT UK_sozialdienst_stammdaten_sozialdienst_id UNIQUE (sozialdienst_id);

ALTER TABLE sozialdienst_stammdaten
	ADD CONSTRAINT UK_sozialdienst_stammdaten_adresse_id UNIQUE (adresse_id);

ALTER TABLE sozialdienst_aud
	ADD CONSTRAINT FK_sozialdienst_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE sozialdienst_stammdaten_aud
	ADD CONSTRAINT FK_sozialdienst_stammdaten_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE sozialdienst
	ADD CONSTRAINT FK_sozialdienst_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE sozialdienst_stammdaten
	ADD CONSTRAINT FK_sozialdienststammdaten_adresse_id
FOREIGN KEY (adresse_id)
REFERENCES adresse(id);

ALTER TABLE sozialdienst_stammdaten
	ADD CONSTRAINT FK_sozialdienststammdaten_sozialdienst_id
FOREIGN KEY (sozialdienst_id)
REFERENCES sozialdienst(id);

CREATE TABLE sozialdienst_fall (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
		name VARCHAR(255) NOT NULL,
		status VARCHAR(255) NOT NULL,
		geburtsdatum  DATE NOT NULL,
		vollmacht longblob,
		adresse_id BINARY(16)   NOT NULL,
		sozialdienst_id BINARY(16)   NOT NULL,
        primary key (id)
    );

CREATE TABLE sozialdienst_fall_aud (
       id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
		name VARCHAR(255),
		status VARCHAR(255),
		geburtsdatum  DATE,
		vollmacht longblob,
		adresse_id BINARY(16),
		sozialdienst_id BINARY(16),
        primary key (id)
    );

ALTER TABLE sozialdienst_fall
	ADD CONSTRAINT UK_sozialdienst_fall_adresse_id UNIQUE (adresse_id);

ALTER TABLE sozialdienst_fall_aud
	ADD CONSTRAINT FK_sozialdienst_fall_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE sozialdienst_fall
	ADD CONSTRAINT FK_sozialdienst_fall_sozialdienst_id
FOREIGN KEY (sozialdienst_id)
REFERENCES sozialdienst(id);

ALTER TABLE sozialdienst_fall
	ADD CONSTRAINT FK_sozialdienst_fall_adresse_id
FOREIGN KEY (adresse_id)
REFERENCES adresse(id);

ALTER TABLE fall ADD COLUMN sozialdienst_fall_id BINARY(16);

ALTER TABLE fall_aud ADD COLUMN sozialdienst_fall_id BINARY(16);

ALTER TABLE fall
	ADD CONSTRAINT UK_fall_sozialdienst_fall_id UNIQUE (sozialdienst_fall_id);

ALTER TABLE fall
	ADD CONSTRAINT FK_fall_sozialdienst_fall_id
FOREIGN KEY (sozialdienst_fall_id)
REFERENCES sozialdienst_fall(id);

ALTER TABLE berechtigung ADD COLUMN sozialdienst_id BINARY(16);

ALTER TABLE berechtigung_aud ADD COLUMN sozialdienst_id BINARY(16);

ALTER TABLE berechtigung
	ADD CONSTRAINT FK_berechtigung_sozialdienst_id
FOREIGN KEY (sozialdienst_id)
REFERENCES sozialdienst(id);

ALTER TABLE berechtigung_history ADD COLUMN sozialdienst_id BINARY(16);

ALTER TABLE berechtigung_history_aud ADD COLUMN sozialdienst_id BINARY(16);

ALTER TABLE berechtigung_history
	ADD CONSTRAINT FK_berechtigung_history_sozialdienst_id
FOREIGN KEY (sozialdienst_id)
REFERENCES sozialdienst(id);