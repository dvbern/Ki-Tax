CREATE TABLE abwesenheit (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE abwesenheit_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE abwesenheit_container_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	abwesenheitgs_id   BINARY(16),
	abwesenheitja_id   BINARY(16),
	betreuung_id       BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE abwesenheit_container (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	abwesenheitgs_id   BINARY(16),
	abwesenheitja_id   BINARY(16),
	betreuung_id       BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE adresse (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	gemeinde           VARCHAR(255),
	hausnummer         VARCHAR(100),
	land               VARCHAR(255) NOT NULL,
	organisation       VARCHAR(255),
	ort                VARCHAR(255) NOT NULL,
	plz                VARCHAR(100) NOT NULL,
	strasse            VARCHAR(255) NOT NULL,
	zusatzzeile        VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE adresse_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	gemeinde           VARCHAR(255),
	hausnummer         VARCHAR(100),
	land               VARCHAR(255),
	organisation       VARCHAR(255),
	ort                VARCHAR(255),
	plz                VARCHAR(100),
	strasse            VARCHAR(255),
	zusatzzeile        VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE antrag_status_history_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	status             VARCHAR(255),
	timestamp_bis      DATETIME(6),
	timestamp_von      DATETIME(6),
	benutzer_id        BINARY(16),
	gesuch_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE antrag_status_history (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	status             VARCHAR(255) NOT NULL,
	timestamp_bis      DATETIME(6),
	timestamp_von      DATETIME(6)  NOT NULL,
	benutzer_id        BINARY(16)   NOT NULL,
	gesuch_id          BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE application_property_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255),
	value              VARCHAR(4000),
	PRIMARY KEY (id, rev)
);

CREATE TABLE application_property (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255)  NOT NULL,
	value              VARCHAR(4000) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE authorisierter_benutzer (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	auth_token         VARCHAR(255),
	first_login        DATETIME     NOT NULL,
	last_login         DATETIME     NOT NULL,
	role               VARCHAR(255) NOT NULL,
	samlidpentityid    VARCHAR(255),
	saml_name_id       VARCHAR(255),
	samlspentityid     VARCHAR(255),
	session_index      VARCHAR(255),
	username           VARCHAR(255) NOT NULL,
	benutzer_id        BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE belegung_ferieninsel_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	ferienname         VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE belegung_ferieninsel_belegung_ferieninsel_tag_aud (
	rev                     INTEGER    NOT NULL,
	belegung_ferieninsel_id BINARY(16) NOT NULL,
	tage_id                 BINARY(16) NOT NULL,
	revtype                 TINYINT,
	PRIMARY KEY (rev, belegung_ferieninsel_id, tage_id)
);

CREATE TABLE belegung_ferieninsel_tag_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	tag                DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE belegung_tagesschule_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	eintrittsdatum     DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE belegung_tagesschule_modul_tagesschule_aud (
	rev                     INTEGER    NOT NULL,
	belegung_tagesschule_id BINARY(16) NOT NULL,
	module_tagesschule_id   BINARY(16) NOT NULL,
	revtype                 TINYINT,
	PRIMARY KEY (rev, belegung_tagesschule_id, module_tagesschule_id)
);

CREATE TABLE belegung_ferieninsel (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	ferienname         VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE belegung_ferieninsel_belegung_ferieninsel_tag (
	belegung_ferieninsel_id BINARY(16) NOT NULL,
	tage_id                 BINARY(16) NOT NULL
);

CREATE TABLE belegung_ferieninsel_tag (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	tag                DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE belegung_tagesschule (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	eintrittsdatum     DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE belegung_tagesschule_modul_tagesschule (
	belegung_tagesschule_id BINARY(16) NOT NULL,
	module_tagesschule_id   BINARY(16) NOT NULL,
	PRIMARY KEY (belegung_tagesschule_id, module_tagesschule_id)
);

CREATE TABLE benutzer (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	email              VARCHAR(255) NOT NULL,
	externaluuid       VARCHAR(36),
	nachname           VARCHAR(255) NOT NULL,
	status             VARCHAR(255) NOT NULL,
	username           VARCHAR(255) NOT NULL,
	vorname            VARCHAR(255) NOT NULL,
	mandant_id         BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE benutzer_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	email              VARCHAR(255),
	externaluuid       VARCHAR(36),
	nachname           VARCHAR(255),
	status             VARCHAR(255),
	username           VARCHAR(255),
	vorname            VARCHAR(255),
	mandant_id         BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE berechtigung (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	role               VARCHAR(255) NOT NULL,
	benutzer_id        BINARY(16)   NOT NULL,
	institution_id     BINARY(16),
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE berechtigung_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	role               VARCHAR(255),
	benutzer_id        BINARY(16),
	institution_id     BINARY(16),
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE berechtigung_gemeinde (
	berechtigung_id BINARY(16) NOT NULL,
	gemeinde_id     BINARY(16) NOT NULL,
	PRIMARY KEY (berechtigung_id, gemeinde_id)
);

CREATE TABLE berechtigung_gemeinde_aud (
	rev             INTEGER    NOT NULL,
	berechtigung_id BINARY(16) NOT NULL,
	gemeinde_id     BINARY(16) NOT NULL,
	revtype         TINYINT,
	PRIMARY KEY (rev, berechtigung_id, gemeinde_id)
);

CREATE TABLE berechtigung_history_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	geloescht          BIT,
	gemeinden          VARCHAR(255),
	role               VARCHAR(255),
	status             VARCHAR(255),
	username           VARCHAR(255),
	institution_id     BINARY(16),
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE berechtigung_history (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	geloescht          BIT          NOT NULL,
	gemeinden          VARCHAR(255),
	role               VARCHAR(255) NOT NULL,
	status             VARCHAR(255) NOT NULL,
	username           VARCHAR(255) NOT NULL,
	institution_id     BINARY(16),
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE betreuung (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	abwesenheit_mutiert        BIT,
	anmeldung_mutation_zustand VARCHAR(255),
	betreuung_mutiert          BIT,
	betreuung_nummer           INTEGER      NOT NULL,
	betreuungsstatus           VARCHAR(255) NOT NULL,
	datum_ablehnung            DATE,
	datum_bestaetigung         DATE,
	grund_ablehnung            VARCHAR(4000),
	gueltig                    BIT          NOT NULL,
	keine_detailinformationen  BIT          NOT NULL,
	keine_kesb_platzierung     BIT          NOT NULL,
	vertrag                    BIT          NOT NULL,
	belegung_ferieninsel_id    BINARY(16),
	belegung_tagesschule_id    BINARY(16),
	institution_stammdaten_id  BINARY(16)   NOT NULL,
	kind_id                    BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE betreuung_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	abwesenheit_mutiert        BIT,
	anmeldung_mutation_zustand VARCHAR(255),
	betreuung_mutiert          BIT,
	betreuung_nummer           INTEGER,
	betreuungsstatus           VARCHAR(255),
	datum_ablehnung            DATE,
	datum_bestaetigung         DATE,
	grund_ablehnung            VARCHAR(4000),
	gueltig                    BIT,
	keine_detailinformationen  BIT,
	keine_kesb_platzierung     BIT,
	vertrag                    BIT,
	belegung_ferieninsel_id    BINARY(16),
	belegung_tagesschule_id    BINARY(16),
	institution_stammdaten_id  BINARY(16),
	kind_id                    BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE betreuungsmitteilung_pensum_aud (
	id                          BINARY(16) NOT NULL,
	rev                         INTEGER    NOT NULL,
	revtype                     TINYINT,
	timestamp_erstellt          DATETIME,
	timestamp_mutiert           DATETIME,
	user_erstellt               VARCHAR(255),
	user_mutiert                VARCHAR(255),
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE,
	gueltig_bis                 DATE,
	monatliche_betreuungskosten DECIMAL(19, 2),
	pensum                      DECIMAL(19, 2),
	unit_for_display            VARCHAR(255),
	betreuungsmitteilung_id     BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE betreuungsmitteilung_pensum (
	id                          BINARY(16)     NOT NULL,
	timestamp_erstellt          DATETIME       NOT NULL,
	timestamp_mutiert           DATETIME       NOT NULL,
	user_erstellt               VARCHAR(255)   NOT NULL,
	user_mutiert                VARCHAR(255)   NOT NULL,
	version                     BIGINT         NOT NULL,
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE           NOT NULL,
	gueltig_bis                 DATE           NOT NULL,
	monatliche_betreuungskosten DECIMAL(19, 2) NOT NULL,
	pensum                      DECIMAL(19, 2) NOT NULL,
	unit_for_display            VARCHAR(255)   NOT NULL,
	betreuungsmitteilung_id     BINARY(16)     NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE betreuungspensum (
	id                          BINARY(16)     NOT NULL,
	timestamp_erstellt          DATETIME       NOT NULL,
	timestamp_mutiert           DATETIME       NOT NULL,
	user_erstellt               VARCHAR(255)   NOT NULL,
	user_mutiert                VARCHAR(255)   NOT NULL,
	version                     BIGINT         NOT NULL,
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE           NOT NULL,
	gueltig_bis                 DATE           NOT NULL,
	monatliche_betreuungskosten DECIMAL(19, 2) NOT NULL,
	pensum                      DECIMAL(19, 2) NOT NULL,
	unit_for_display            VARCHAR(255)   NOT NULL,
	nicht_eingetreten           BIT            NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE betreuungspensum_aud (
	id                          BINARY(16) NOT NULL,
	rev                         INTEGER    NOT NULL,
	revtype                     TINYINT,
	timestamp_erstellt          DATETIME,
	timestamp_mutiert           DATETIME,
	user_erstellt               VARCHAR(255),
	user_mutiert                VARCHAR(255),
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE,
	gueltig_bis                 DATE,
	monatliche_betreuungskosten DECIMAL(19, 2),
	pensum                      DECIMAL(19, 2),
	unit_for_display            VARCHAR(255),
	nicht_eingetreten           BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE betreuungspensum_container_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	betreuung_id          BINARY(16),
	betreuungspensumgs_id BINARY(16),
	betreuungspensumja_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE betreuungspensum_container (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	betreuung_id          BINARY(16)   NOT NULL,
	betreuungspensumgs_id BINARY(16),
	betreuungspensumja_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE bfs_gemeinde (
	id            VARCHAR(36)  NOT NULL,
	bezirk        VARCHAR(255) NOT NULL,
	bezirk_nummer BIGINT       NOT NULL,
	bfs_nummer    BIGINT       NOT NULL,
	gueltig_ab    DATE         NOT NULL,
	hist_nummer   BIGINT       NOT NULL,
	kanton        VARCHAR(255) NOT NULL,
	name          VARCHAR(255) NOT NULL,
	mandant_id    BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE dokument (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	timestamp_upload   DATETIME      NOT NULL,
	dokument_grund_id  BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE dokument_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255),
	filepfad           VARCHAR(4000),
	filesize           VARCHAR(255),
	timestamp_upload   DATETIME,
	dokument_grund_id  BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE dokument_grund_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	dokument_grund_typ VARCHAR(255),
	dokument_typ       VARCHAR(255),
	person_number      INTEGER,
	person_type        VARCHAR(255),
	tag                VARCHAR(255),
	gesuch_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE dokument_grund (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	dokument_grund_typ VARCHAR(255),
	dokument_typ       VARCHAR(255),
	person_number      INTEGER,
	person_type        VARCHAR(255),
	tag                VARCHAR(255),
	gesuch_id          BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE dossier (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	fall_id               BINARY(16)   NOT NULL,
	gemeinde_id           BINARY(16)   NOT NULL,
	verantwortlicherbg_id BINARY(16),
	verantwortlicherts_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE dossier_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	fall_id               BINARY(16),
	gemeinde_id           BINARY(16),
	verantwortlicherbg_id BINARY(16),
	verantwortlicherts_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE download_file (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	access_token       VARCHAR(36)   NOT NULL,
	ip                 VARCHAR(45)   NOT NULL,
	lifespan           VARCHAR(255)  NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE ebegu_vorlage_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	name               VARCHAR(255),
	vorlage_id         BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE ebegu_vorlage (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	name               VARCHAR(255) NOT NULL,
	vorlage_id         BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE einkommensverschlechterung (
	id                                BINARY(16)   NOT NULL,
	timestamp_erstellt                DATETIME     NOT NULL,
	timestamp_mutiert                 DATETIME     NOT NULL,
	user_erstellt                     VARCHAR(255) NOT NULL,
	user_mutiert                      VARCHAR(255) NOT NULL,
	version                           BIGINT       NOT NULL,
	vorgaenger_id                     VARCHAR(36),
	bruttovermoegen                   DECIMAL(19, 2),
	erhaltene_alimente                DECIMAL(19, 2),
	ersatzeinkommen                   DECIMAL(19, 2),
	familienzulage                    DECIMAL(19, 2),
	geleistete_alimente               DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr        DECIMAL(19, 2),
	nettolohn                         DECIMAL(19, 2),
	schulden                          DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus1 DECIMAL(19, 2),
	PRIMARY KEY (id)
);

CREATE TABLE einkommensverschlechterung_aud (
	id                                BINARY(16) NOT NULL,
	rev                               INTEGER    NOT NULL,
	revtype                           TINYINT,
	timestamp_erstellt                DATETIME,
	timestamp_mutiert                 DATETIME,
	user_erstellt                     VARCHAR(255),
	user_mutiert                      VARCHAR(255),
	vorgaenger_id                     VARCHAR(36),
	bruttovermoegen                   DECIMAL(19, 2),
	erhaltene_alimente                DECIMAL(19, 2),
	ersatzeinkommen                   DECIMAL(19, 2),
	familienzulage                    DECIMAL(19, 2),
	geleistete_alimente               DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr        DECIMAL(19, 2),
	nettolohn                         DECIMAL(19, 2),
	schulden                          DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus1 DECIMAL(19, 2),
	PRIMARY KEY (id, rev)
);

CREATE TABLE einkommensverschlechterung_container_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	ekvgsbasis_jahr_plus1_id   BINARY(16),
	ekvgsbasis_jahr_plus2_id   BINARY(16),
	ekvjabasis_jahr_plus1_id   BINARY(16),
	ekvjabasis_jahr_plus2_id   BINARY(16),
	gesuchsteller_container_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE einkommensverschlechterung_info_aud (
	id                               BINARY(16) NOT NULL,
	rev                              INTEGER    NOT NULL,
	revtype                          TINYINT,
	timestamp_erstellt               DATETIME,
	timestamp_mutiert                DATETIME,
	user_erstellt                    VARCHAR(255),
	user_mutiert                     VARCHAR(255),
	vorgaenger_id                    VARCHAR(36),
	einkommensverschlechterung       BIT,
	ekv_basis_jahr_plus1annulliert   BIT,
	ekv_basis_jahr_plus2annulliert   BIT,
	ekv_fuer_basis_jahr_plus1        BIT,
	ekv_fuer_basis_jahr_plus2        BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE einkommensverschlechterung_info_container_aud (
	id                                   BINARY(16) NOT NULL,
	rev                                  INTEGER    NOT NULL,
	revtype                              TINYINT,
	timestamp_erstellt                   DATETIME,
	timestamp_mutiert                    DATETIME,
	user_erstellt                        VARCHAR(255),
	user_mutiert                         VARCHAR(255),
	vorgaenger_id                        VARCHAR(36),
	einkommensverschlechterung_infogs_id BINARY(16),
	einkommensverschlechterung_infoja_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE einkommensverschlechterung_container (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	ekvgsbasis_jahr_plus1_id   BINARY(16),
	ekvgsbasis_jahr_plus2_id   BINARY(16),
	ekvjabasis_jahr_plus1_id   BINARY(16),
	ekvjabasis_jahr_plus2_id   BINARY(16),
	gesuchsteller_container_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE einkommensverschlechterung_info (
	id                               BINARY(16)   NOT NULL,
	timestamp_erstellt               DATETIME     NOT NULL,
	timestamp_mutiert                DATETIME     NOT NULL,
	user_erstellt                    VARCHAR(255) NOT NULL,
	user_mutiert                     VARCHAR(255) NOT NULL,
	version                          BIGINT       NOT NULL,
	vorgaenger_id                    VARCHAR(36),
	einkommensverschlechterung       BIT          NOT NULL,
	ekv_basis_jahr_plus1annulliert   BIT          NOT NULL,
	ekv_basis_jahr_plus2annulliert   BIT          NOT NULL,
	ekv_fuer_basis_jahr_plus1        BIT          NOT NULL,
	ekv_fuer_basis_jahr_plus2        BIT          NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE einkommensverschlechterung_info_container (
	id                                   BINARY(16)   NOT NULL,
	timestamp_erstellt                   DATETIME     NOT NULL,
	timestamp_mutiert                    DATETIME     NOT NULL,
	user_erstellt                        VARCHAR(255) NOT NULL,
	user_mutiert                         VARCHAR(255) NOT NULL,
	version                              BIGINT       NOT NULL,
	vorgaenger_id                        VARCHAR(36),
	einkommensverschlechterung_infogs_id BINARY(16),
	einkommensverschlechterung_infoja_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE einstellung (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	einstellung_key    VARCHAR(255) NOT NULL,
	value              VARCHAR(255) NOT NULL,
	gemeinde_id        BINARY(16),
	gesuchsperiode_id  BINARY(16)   NOT NULL,
	mandant_id         BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE einstellung_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	einstellung_key    VARCHAR(255),
	value              VARCHAR(255),
	gemeinde_id        BINARY(16),
	gesuchsperiode_id  BINARY(16),
	mandant_id         BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE erweiterte_betreuung_aud (
	id                      BINARY(16) NOT NULL,
	rev                     INTEGER    NOT NULL,
	revtype                 TINYINT,
	timestamp_erstellt      DATETIME,
	timestamp_mutiert       DATETIME,
	user_erstellt           VARCHAR(255),
	user_mutiert            VARCHAR(255),
	vorgaenger_id           VARCHAR(36),
	erweiterte_beduerfnisse BIT,
	fachstelle_id           BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE erweiterte_betreuung_container_aud (
	betreuung_id              BINARY(16) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(255),
	user_mutiert              VARCHAR(255),
	vorgaenger_id             VARCHAR(36),
	erweiterte_betreuunggs_id BINARY(16),
	erweiterte_betreuungja_id BINARY(16),
	PRIMARY KEY (betreuung_id, rev)
);

CREATE TABLE erweiterte_betreuung (
	id                      BINARY(16)   NOT NULL,
	timestamp_erstellt      DATETIME     NOT NULL,
	timestamp_mutiert       DATETIME     NOT NULL,
	user_erstellt           VARCHAR(255) NOT NULL,
	user_mutiert            VARCHAR(255) NOT NULL,
	version                 BIGINT       NOT NULL,
	vorgaenger_id           VARCHAR(36),
	erweiterte_beduerfnisse BIT          NOT NULL,
	fachstelle_id           BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE erweiterte_betreuung_container (
	timestamp_erstellt        DATETIME     NOT NULL,
	timestamp_mutiert         DATETIME     NOT NULL,
	user_erstellt             VARCHAR(255) NOT NULL,
	user_mutiert              VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,
	vorgaenger_id             VARCHAR(36),
	betreuung_id              BINARY(16)   NOT NULL,
	erweiterte_betreuunggs_id BINARY(16),
	erweiterte_betreuungja_id BINARY(16),
	PRIMARY KEY (betreuung_id)
);

CREATE TABLE erwerbspensum (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	gueltig_ab            DATE         NOT NULL,
	gueltig_bis           DATE         NOT NULL,
	pensum                INTEGER      NOT NULL,
	bezeichnung           VARCHAR(255),
	taetigkeit            VARCHAR(255) NOT NULL,
	unbezahlter_urlaub_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE erwerbspensum_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	gueltig_ab            DATE,
	gueltig_bis           DATE,
	pensum                INTEGER,
	bezeichnung           VARCHAR(255),
	taetigkeit            VARCHAR(255),
	unbezahlter_urlaub_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE erwerbspensum_container_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	erwerbspensumgs_id         BINARY(16),
	erwerbspensumja_id         BINARY(16),
	gesuchsteller_container_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE erwerbspensum_container (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	erwerbspensumgs_id         BINARY(16),
	erwerbspensumja_id         BINARY(16),
	gesuchsteller_container_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE fachstelle (
	id                              BINARY(16)   NOT NULL,
	timestamp_erstellt              DATETIME     NOT NULL,
	timestamp_mutiert               DATETIME     NOT NULL,
	user_erstellt                   VARCHAR(255) NOT NULL,
	user_mutiert                    VARCHAR(255) NOT NULL,
	version                         BIGINT       NOT NULL,
	vorgaenger_id                   VARCHAR(36),
	fachstelle_anspruch             BIT          NOT NULL,
	fachstelle_erweiterte_betreuung BIT          NOT NULL,
	name                            VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE fachstelle_aud (
	id                              BINARY(16) NOT NULL,
	rev                             INTEGER    NOT NULL,
	revtype                         TINYINT,
	timestamp_erstellt              DATETIME,
	timestamp_mutiert               DATETIME,
	user_erstellt                   VARCHAR(255),
	user_mutiert                    VARCHAR(255),
	vorgaenger_id                   VARCHAR(36),
	fachstelle_anspruch             BIT,
	fachstelle_erweiterte_betreuung BIT,
	name                            VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE fall (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	fall_nummer        BIGINT       NOT NULL,
	next_number_kind   INTEGER      NOT NULL,
	besitzer_id        BINARY(16),
	mandant_id         BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE fall_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	fall_nummer        BIGINT,
	next_number_kind   INTEGER,
	besitzer_id        BINARY(16),
	mandant_id         BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE familiensituation (
	id                                   BINARY(16)   NOT NULL,
	timestamp_erstellt                   DATETIME     NOT NULL,
	timestamp_mutiert                    DATETIME     NOT NULL,
	user_erstellt                        VARCHAR(255) NOT NULL,
	user_mutiert                         VARCHAR(255) NOT NULL,
	version                              BIGINT       NOT NULL,
	vorgaenger_id                        VARCHAR(36),
	aenderung_per                        DATE,
	familienstatus                       VARCHAR(255) NOT NULL,
	gemeinsame_steuererklaerung          BIT,
	sozialhilfe_bezueger                 BIT,
	antrag_nur_fuer_behinderungszuschlag BIT,
	start_konkubinat                     DATE,
	verguenstigung_gewuenscht            BIT,
	PRIMARY KEY (id)
);

CREATE TABLE familiensituation_aud (
	id                                   BINARY(16) NOT NULL,
	rev                                  INTEGER    NOT NULL,
	revtype                              TINYINT,
	timestamp_erstellt                   DATETIME,
	timestamp_mutiert                    DATETIME,
	user_erstellt                        VARCHAR(255),
	user_mutiert                         VARCHAR(255),
	vorgaenger_id                        VARCHAR(36),
	aenderung_per                        DATE,
	familienstatus                       VARCHAR(255),
	gemeinsame_steuererklaerung          BIT,
	sozialhilfe_bezueger                 BIT,
	antrag_nur_fuer_behinderungszuschlag BIT,
	start_konkubinat                     DATE,
	verguenstigung_gewuenscht            BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE familiensituation_container_aud (
	id                              BINARY(16) NOT NULL,
	rev                             INTEGER    NOT NULL,
	revtype                         TINYINT,
	timestamp_erstellt              DATETIME,
	timestamp_mutiert               DATETIME,
	user_erstellt                   VARCHAR(255),
	user_mutiert                    VARCHAR(255),
	vorgaenger_id                   VARCHAR(36),
	familiensituation_erstgesuch_id BINARY(16),
	familiensituationgs_id          BINARY(16),
	familiensituationja_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE familiensituation_container (
	id                              BINARY(16)   NOT NULL,
	timestamp_erstellt              DATETIME     NOT NULL,
	timestamp_mutiert               DATETIME     NOT NULL,
	user_erstellt                   VARCHAR(255) NOT NULL,
	user_mutiert                    VARCHAR(255) NOT NULL,
	version                         BIGINT       NOT NULL,
	vorgaenger_id                   VARCHAR(36),
	familiensituation_erstgesuch_id BINARY(16),
	familiensituationgs_id          BINARY(16),
	familiensituationja_id          BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE ferieninsel_stammdaten_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	anmeldeschluss     DATE,
	ferienname         VARCHAR(255),
	gesuchsperiode_id  BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE ferieninsel_stammdaten_ferieninsel_zeitraum_aud (
	rev                       INTEGER    NOT NULL,
	ferieninsel_stammdaten_id BINARY(16) NOT NULL,
	zeitraum_id               BINARY(16) NOT NULL,
	revtype                   TINYINT,
	PRIMARY KEY (rev, ferieninsel_stammdaten_id, zeitraum_id)
);

CREATE TABLE ferieninsel_zeitraum_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE ferieninsel_stammdaten (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	anmeldeschluss     DATE         NOT NULL,
	ferienname         VARCHAR(255) NOT NULL,
	gesuchsperiode_id  BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE ferieninsel_stammdaten_ferieninsel_zeitraum (
	ferieninsel_stammdaten_id BINARY(16) NOT NULL,
	zeitraum_id               BINARY(16) NOT NULL
);

CREATE TABLE ferieninsel_zeitraum (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE finanzielle_situation_aud (
	id                                BINARY(16) NOT NULL,
	rev                               INTEGER    NOT NULL,
	revtype                           TINYINT,
	timestamp_erstellt                DATETIME,
	timestamp_mutiert                 DATETIME,
	user_erstellt                     VARCHAR(255),
	user_mutiert                      VARCHAR(255),
	vorgaenger_id                     VARCHAR(36),
	bruttovermoegen                   DECIMAL(19, 2),
	erhaltene_alimente                DECIMAL(19, 2),
	ersatzeinkommen                   DECIMAL(19, 2),
	familienzulage                    DECIMAL(19, 2),
	geleistete_alimente               DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr        DECIMAL(19, 2),
	nettolohn                         DECIMAL(19, 2),
	schulden                          DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus1 DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus2 DECIMAL(19, 2),
	steuererklaerung_ausgefuellt      BIT,
	steuerveranlagung_erhalten        BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE finanzielle_situation_container_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	jahr                       INTEGER,
	finanzielle_situationgs_id BINARY(16),
	finanzielle_situationja_id BINARY(16),
	gesuchsteller_container_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE finanzielle_situation (
	id                                BINARY(16)   NOT NULL,
	timestamp_erstellt                DATETIME     NOT NULL,
	timestamp_mutiert                 DATETIME     NOT NULL,
	user_erstellt                     VARCHAR(255) NOT NULL,
	user_mutiert                      VARCHAR(255) NOT NULL,
	version                           BIGINT       NOT NULL,
	vorgaenger_id                     VARCHAR(36),
	bruttovermoegen                   DECIMAL(19, 2),
	erhaltene_alimente                DECIMAL(19, 2),
	ersatzeinkommen                   DECIMAL(19, 2),
	familienzulage                    DECIMAL(19, 2),
	geleistete_alimente               DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr        DECIMAL(19, 2),
	nettolohn                         DECIMAL(19, 2),
	schulden                          DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus1 DECIMAL(19, 2),
	geschaeftsgewinn_basisjahr_minus2 DECIMAL(19, 2),
	steuererklaerung_ausgefuellt      BIT          NOT NULL,
	steuerveranlagung_erhalten        BIT          NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE finanzielle_situation_container (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	jahr                       INTEGER      NOT NULL,
	finanzielle_situationgs_id BINARY(16),
	finanzielle_situationja_id BINARY(16),
	gesuchsteller_container_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gemeinde (
	id                              BINARY(16)   NOT NULL,
	timestamp_erstellt              DATETIME     NOT NULL,
	timestamp_mutiert               DATETIME     NOT NULL,
	user_erstellt                   VARCHAR(255) NOT NULL,
	user_mutiert                    VARCHAR(255) NOT NULL,
	version                         BIGINT       NOT NULL,
	betreuungsgutscheine_startdatum DATE         NOT NULL,
	bfs_nummer                      BIGINT       NOT NULL,
	gemeinde_nummer                 BIGINT       NOT NULL,
	name                            VARCHAR(255) NOT NULL,
	status                          VARCHAR(255) NOT NULL,
	mandant_id                      BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gemeinde_aud (
	id                              BINARY(16) NOT NULL,
	rev                             INTEGER    NOT NULL,
	revtype                         TINYINT,
	timestamp_erstellt              DATETIME,
	timestamp_mutiert               DATETIME,
	user_erstellt                   VARCHAR(255),
	user_mutiert                    VARCHAR(255),
	betreuungsgutscheine_startdatum DATE,
	bfs_nummer                      BIGINT,
	gemeinde_nummer                 BIGINT,
	name                            VARCHAR(255),
	status                          VARCHAR(255),
	mandant_id                      BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gemeinde_stammdaten_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	bic                   VARCHAR(255),
	iban                  VARCHAR(34),
	kontoinhaber          VARCHAR(255),
	korrespondenzsprache  VARCHAR(255),
	logo_content          LONGBLOB,
	mail                  VARCHAR(255),
	standard_rechtsmittelbelehrung BIT,
	telefon               VARCHAR(255),
	webseite              VARCHAR(255),
	adresse_id            BINARY(16),
	beschwerde_adresse_id BINARY(16),
	default_benutzerbg_id BINARY(16),
	default_benutzerts_id BINARY(16),
	gemeinde_id           BINARY(16),
	rechtsmittelbelehrung_id binary(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gemeinde_stammdaten (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	bic                   VARCHAR(255) NOT NULL,
	iban                  VARCHAR(34),
	kontoinhaber          VARCHAR(255) NOT NULL,
	korrespondenzsprache  VARCHAR(255) NOT NULL,
	logo_content          LONGBLOB,
	mail                  VARCHAR(255) NOT NULL,
	standard_rechtsmittelbelehrung bit not null,
	telefon               VARCHAR(255),
	webseite              VARCHAR(255),
	adresse_id            BINARY(16)   NOT NULL,
	beschwerde_adresse_id BINARY(16),
	default_benutzerbg_id BINARY(16),
	default_benutzerts_id BINARY(16),
	gemeinde_id           BINARY(16)   NOT NULL,
	rechtsmittelbelehrung_id binary(16),
	PRIMARY KEY (id)
);

CREATE TABLE generated_dokument_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255),
	filepfad           VARCHAR(4000),
	filesize           VARCHAR(255),
	typ                VARCHAR(255),
	write_protected    BIT,
	gesuch_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE generated_dokument (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	typ                VARCHAR(255)  NOT NULL,
	write_protected    BIT           NOT NULL,
	gesuch_id          BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gesuch (
	id                                           BINARY(16)   NOT NULL,
	timestamp_erstellt                           DATETIME     NOT NULL,
	timestamp_mutiert                            DATETIME     NOT NULL,
	user_erstellt                                VARCHAR(255) NOT NULL,
	user_mutiert                                 VARCHAR(255) NOT NULL,
	version                                      BIGINT       NOT NULL,
	vorgaenger_id                                VARCHAR(36),
	bemerkungen                                  VARCHAR(4000),
	bemerkungen_pruefungstv                      VARCHAR(4000),
	bemerkungenstv                               VARCHAR(4000),
	datum_gewarnt_fehlende_quittung              DATE,
	datum_gewarnt_nicht_freigegeben              DATE,
	dokumente_hochgeladen                        BIT          NOT NULL,
	eingangsart                                  VARCHAR(255) NOT NULL,
	eingangsdatum                                DATE,
	eingangsdatumstv                             DATE,
	fin_sit_status                               VARCHAR(255),
	freigabe_datum                               DATE,
	geprueftstv                                  BIT          NOT NULL,
	gesperrt_wegen_beschwerde                    BIT          NOT NULL,
	gesuch_betreuungen_status                    VARCHAR(255) NOT NULL,
	gueltig                                      BIT,
	laufnummer                                   INTEGER      NOT NULL,
	regeln_gueltig_ab                            DATE,
	status                                       VARCHAR(255) NOT NULL,
	timestamp_verfuegt                           DATETIME,
	typ                                          VARCHAR(255) NOT NULL,
	verfuegung_eingeschrieben                    BIT          NOT NULL,
	dossier_id                                   BINARY(16)   NOT NULL,
	einkommensverschlechterung_info_container_id BINARY(16),
	familiensituation_container_id               BINARY(16),
	gesuchsperiode_id                            BINARY(16)   NOT NULL,
	gesuchsteller1_id                            BINARY(16),
	gesuchsteller2_id                            BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE gesuch_aud (
	id                                           BINARY(16) NOT NULL,
	rev                                          INTEGER    NOT NULL,
	revtype                                      TINYINT,
	timestamp_erstellt                           DATETIME,
	timestamp_mutiert                            DATETIME,
	user_erstellt                                VARCHAR(255),
	user_mutiert                                 VARCHAR(255),
	vorgaenger_id                                VARCHAR(36),
	bemerkungen                                  VARCHAR(4000),
	bemerkungen_pruefungstv                      VARCHAR(4000),
	bemerkungenstv                               VARCHAR(4000),
	datum_gewarnt_fehlende_quittung              DATE,
	datum_gewarnt_nicht_freigegeben              DATE,
	dokumente_hochgeladen                        BIT,
	eingangsart                                  VARCHAR(255),
	eingangsdatum                                DATE,
	eingangsdatumstv                             DATE,
	fin_sit_status                               VARCHAR(255),
	freigabe_datum                               DATE,
	geprueftstv                                  BIT,
	gesperrt_wegen_beschwerde                    BIT,
	gesuch_betreuungen_status                    VARCHAR(255),
	gueltig                                      BIT,
	laufnummer                                   INTEGER,
	regeln_gueltig_ab                            DATE,
	status                                       VARCHAR(255),
	timestamp_verfuegt                           DATETIME,
	typ                                          VARCHAR(255),
	verfuegung_eingeschrieben                    BIT,
	dossier_id                                   BINARY(16),
	einkommensverschlechterung_info_container_id BINARY(16),
	familiensituation_container_id               BINARY(16),
	gesuchsperiode_id                            BINARY(16),
	gesuchsteller1_id                            BINARY(16),
	gesuchsteller2_id                            BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuch_deletion_log (
	id                VARCHAR(36)  NOT NULL,
	version           BIGINT       NOT NULL,
	cause             VARCHAR(255) NOT NULL,
	fall_nummer       BIGINT       NOT NULL,
	geburtsdatum      DATE,
	gesuch_id         VARCHAR(36)  NOT NULL,
	nachname          VARCHAR(255),
	timestamp_deleted DATETIME     NOT NULL,
	user_deleted      VARCHAR(36)  NOT NULL,
	vorname           VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE gesuchsperiode (
	id                              BINARY(16)   NOT NULL,
	timestamp_erstellt              DATETIME     NOT NULL,
	timestamp_mutiert               DATETIME     NOT NULL,
	user_erstellt                   VARCHAR(255) NOT NULL,
	user_mutiert                    VARCHAR(255) NOT NULL,
	version                         BIGINT       NOT NULL,
	vorgaenger_id                   VARCHAR(36),
	gueltig_ab                      DATE         NOT NULL,
	gueltig_bis                     DATE         NOT NULL,
	datum_aktiviert                 DATE,
	datum_erster_schultag           DATE,
	datum_freischaltung_tagesschule DATE,
	status                          VARCHAR(255) NOT NULL,
	verfuegung_erlaeuterungen_de    longblob,
	verfuegung_erlaeuterungen_fr    longblob,
	PRIMARY KEY (id)
);

CREATE TABLE gesuchsperiode_aud (
	id                              BINARY(16) NOT NULL,
	rev                             INTEGER    NOT NULL,
	revtype                         TINYINT,
	timestamp_erstellt              DATETIME,
	timestamp_mutiert               DATETIME,
	user_erstellt                   VARCHAR(255),
	user_mutiert                    VARCHAR(255),
	vorgaenger_id                   VARCHAR(36),
	gueltig_ab                      DATE,
	gueltig_bis                     DATE,
	datum_aktiviert                 DATE,
	datum_erster_schultag           DATE,
	datum_freischaltung_tagesschule DATE,
	status                          VARCHAR(255),
	verfuegung_erlaeuterungen_de    longblob,
	verfuegung_erlaeuterungen_fr    longblob,
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuchsteller (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	geburtsdatum          DATE         NOT NULL,
	geschlecht            VARCHAR(255) NOT NULL,
	nachname              VARCHAR(255) NOT NULL,
	vorname               VARCHAR(255) NOT NULL,
	diplomatenstatus      BIT          NOT NULL,
	ewk_abfrage_datum     DATE,
	ewk_person_id         VARCHAR(255),
	korrespondenz_sprache VARCHAR(255),
	mail                  VARCHAR(255) NOT NULL,
	mobile                VARCHAR(255),
	telefon               VARCHAR(255),
	telefon_ausland       VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE gesuchsteller_adresse_aud (
	id                BINARY(16) NOT NULL,
	rev               INTEGER    NOT NULL,
	adresse_typ       VARCHAR(255),
	nicht_in_gemeinde BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuchsteller_adresse_container_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	gesuchsteller_adressegs_id BINARY(16),
	gesuchsteller_adresseja_id BINARY(16),
	gesuchsteller_container_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuchsteller_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	geburtsdatum          DATE,
	geschlecht            VARCHAR(255),
	nachname              VARCHAR(255),
	vorname               VARCHAR(255),
	diplomatenstatus      BIT,
	ewk_abfrage_datum     DATE,
	ewk_person_id         VARCHAR(255),
	korrespondenz_sprache VARCHAR(255),
	mail                  VARCHAR(255),
	mobile                VARCHAR(255),
	telefon               VARCHAR(255),
	telefon_ausland       VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuchsteller_container_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gesuchstellergs_id BINARY(16),
	gesuchstellerja_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gesuchsteller_adresse (
	adresse_typ       VARCHAR(255),
	nicht_in_gemeinde BIT        NOT NULL,
	id                BINARY(16) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gesuchsteller_adresse_container (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	gesuchsteller_adressegs_id BINARY(16),
	gesuchsteller_adresseja_id BINARY(16),
	gesuchsteller_container_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gesuchsteller_container (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gesuchstellergs_id BINARY(16),
	gesuchstellerja_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE institution (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255) NOT NULL,
	status             VARCHAR(255) NOT NULL,
	mandant_id         BINARY(16)   NOT NULL,
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE institution_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255),
	status             VARCHAR(255),
	mandant_id         BINARY(16),
	traegerschaft_id   BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE institution_stammdaten_aud (
	id                                    BINARY(16) NOT NULL,
	rev                                   INTEGER    NOT NULL,
	revtype                               TINYINT,
	timestamp_erstellt                    DATETIME,
	timestamp_mutiert                     DATETIME,
	user_erstellt                         VARCHAR(255),
	user_mutiert                          VARCHAR(255),
	vorgaenger_id                         VARCHAR(36),
	gueltig_ab                            DATE,
	gueltig_bis                           DATE,
	alterskategorie_baby                  BIT,
	alterskategorie_kindergarten          BIT,
	alterskategorie_schule                BIT,
	alterskategorie_vorschule             BIT,
	anzahl_plaetze                        DECIMAL(19, 2),
	anzahl_plaetze_firmen                 DECIMAL(19, 2),
	betreuungsangebot_typ                 VARCHAR(255),
	iban                                  VARCHAR(34),
	kontoinhaber                          VARCHAR(255),
	mail                                  VARCHAR(255),
	oeffnungszeiten                       VARCHAR(255),
	subventionierte_plaetze               BIT,
	telefon                               VARCHAR(255),
	webseite                              VARCHAR(255),
	adresse_id                            BINARY(16),
	adresse_kontoinhaber_id               BINARY(16),
	institution_id                        BINARY(16),
	institution_stammdaten_ferieninsel_id BINARY(16),
	institution_stammdaten_tagesschule_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE institution_stammdaten_ferieninsel_aud (
	id                                BINARY(16) NOT NULL,
	rev                               INTEGER    NOT NULL,
	revtype                           TINYINT,
	timestamp_erstellt                DATETIME,
	timestamp_mutiert                 DATETIME,
	user_erstellt                     VARCHAR(255),
	user_mutiert                      VARCHAR(255),
	vorgaenger_id                     VARCHAR(36),
	gueltig_ab                        DATE,
	gueltig_bis                       DATE,
	ausweichstandort_fruehlingsferien VARCHAR(255),
	ausweichstandort_herbstferien     VARCHAR(255),
	ausweichstandort_sommerferien     VARCHAR(255),
	ausweichstandort_sportferien      VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE institution_stammdaten_tagesschule_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE institution_stammdaten (
	id                                    BINARY(16)   NOT NULL,
	timestamp_erstellt                    DATETIME     NOT NULL,
	timestamp_mutiert                     DATETIME     NOT NULL,
	user_erstellt                         VARCHAR(255) NOT NULL,
	user_mutiert                          VARCHAR(255) NOT NULL,
	version                               BIGINT       NOT NULL,
	vorgaenger_id                         VARCHAR(36),
	gueltig_ab                            DATE         NOT NULL,
	gueltig_bis                           DATE         NOT NULL,
	alterskategorie_baby                  BIT            NOT NULL,
	alterskategorie_kindergarten          BIT            NOT NULL,
	alterskategorie_schule                BIT            NOT NULL,
	alterskategorie_vorschule             BIT            NOT NULL,
	anzahl_plaetze                        DECIMAL(19, 2) NOT NULL,
	anzahl_plaetze_firmen                 DECIMAL(19, 2),
	betreuungsangebot_typ                 VARCHAR(255) NOT NULL,
	iban                                  VARCHAR(34),
	kontoinhaber                          VARCHAR(255),
	mail                                  VARCHAR(255) NOT NULL,
	oeffnungszeiten                       VARCHAR(255),
	subventionierte_plaetze               BIT            NOT NULL,
	telefon                               VARCHAR(255),
	webseite                              VARCHAR(255),
	adresse_id                            BINARY(16)   NOT NULL,
	adresse_kontoinhaber_id               BINARY(16),
	institution_id                        BINARY(16)   NOT NULL,
	institution_stammdaten_ferieninsel_id BINARY(16),
	institution_stammdaten_tagesschule_id BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE institution_stammdaten_ferieninsel (
	id                                BINARY(16)   NOT NULL,
	timestamp_erstellt                DATETIME     NOT NULL,
	timestamp_mutiert                 DATETIME     NOT NULL,
	user_erstellt                     VARCHAR(255) NOT NULL,
	user_mutiert                      VARCHAR(255) NOT NULL,
	version                           BIGINT       NOT NULL,
	vorgaenger_id                     VARCHAR(36),
	gueltig_ab                        DATE         NOT NULL,
	gueltig_bis                       DATE         NOT NULL,
	ausweichstandort_fruehlingsferien VARCHAR(255),
	ausweichstandort_herbstferien     VARCHAR(255),
	ausweichstandort_sommerferien     VARCHAR(255),
	ausweichstandort_sportferien      VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE institution_stammdaten_tagesschule (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE kind (
	id                                    BINARY(16)   NOT NULL,
	timestamp_erstellt                    DATETIME     NOT NULL,
	timestamp_mutiert                     DATETIME     NOT NULL,
	user_erstellt                         VARCHAR(255) NOT NULL,
	user_mutiert                          VARCHAR(255) NOT NULL,
	version                               BIGINT       NOT NULL,
	vorgaenger_id                         VARCHAR(36),
	geburtsdatum                          DATE         NOT NULL,
	geschlecht                            VARCHAR(255) NOT NULL,
	nachname                              VARCHAR(255) NOT NULL,
	vorname                               VARCHAR(255) NOT NULL,
	einschulung_typ                       VARCHAR(255),
	familien_ergaenzende_betreuung        BIT          NOT NULL,
	kinderabzug_erstes_halbjahr           VARCHAR(255) NOT NULL,
	kinderabzug_zweites_halbjahr          VARCHAR(255) NOT NULL,
	spricht_amtssprache                   BIT,
	pensum_ausserordentlicher_anspruch_id BINARY(16),
	pensum_fachstelle_id                  BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE kind_aud (
	id                                    BINARY(16) NOT NULL,
	rev                                   INTEGER    NOT NULL,
	revtype                               TINYINT,
	timestamp_erstellt                    DATETIME,
	timestamp_mutiert                     DATETIME,
	user_erstellt                         VARCHAR(255),
	user_mutiert                          VARCHAR(255),
	vorgaenger_id                         VARCHAR(36),
	geburtsdatum                          DATE,
	geschlecht                            VARCHAR(255),
	nachname                              VARCHAR(255),
	vorname                               VARCHAR(255),
	einschulung_typ                       VARCHAR(255),
	familien_ergaenzende_betreuung        BIT,
	kinderabzug_erstes_halbjahr           VARCHAR(255),
	kinderabzug_zweites_halbjahr          VARCHAR(255),
	spricht_amtssprache                   BIT,
	pensum_ausserordentlicher_anspruch_id BINARY(16),
	pensum_fachstelle_id                  BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE kind_container_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	kind_mutiert          BIT,
	kind_nummer           INTEGER,
	next_number_betreuung INTEGER,
	gesuch_id             BINARY(16),
	kindgs_id             BINARY(16),
	kindja_id             BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE kind_container (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	kind_mutiert          BIT,
	kind_nummer           INTEGER      NOT NULL,
	next_number_betreuung INTEGER      NOT NULL,
	gesuch_id             BINARY(16)   NOT NULL,
	kindgs_id             BINARY(16),
	kindja_id             BINARY(16),
	PRIMARY KEY (id)
);

CREATE TABLE mahnung (
	id                      BINARY(16)    NOT NULL,
	timestamp_erstellt      DATETIME      NOT NULL,
	timestamp_mutiert       DATETIME      NOT NULL,
	user_erstellt           VARCHAR(255)  NOT NULL,
	user_mutiert            VARCHAR(255)  NOT NULL,
	version                 BIGINT        NOT NULL,
	vorgaenger_id           VARCHAR(36),
	abgelaufen              BIT           NOT NULL,
	bemerkungen             VARCHAR(4000) NOT NULL,
	datum_fristablauf       DATE          NOT NULL,
	mahnung_typ             VARCHAR(255)  NOT NULL,
	timestamp_abgeschlossen DATETIME(6),
	gesuch_id               BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE mahnung_aud (
	id                      BINARY(16) NOT NULL,
	rev                     INTEGER    NOT NULL,
	revtype                 TINYINT,
	timestamp_erstellt      DATETIME,
	timestamp_mutiert       DATETIME,
	user_erstellt           VARCHAR(255),
	user_mutiert            VARCHAR(255),
	vorgaenger_id           VARCHAR(36),
	abgelaufen              BIT,
	bemerkungen             VARCHAR(4000),
	datum_fristablauf       DATE,
	mahnung_typ             VARCHAR(255),
	timestamp_abgeschlossen DATETIME(6),
	gesuch_id               BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE mandant (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE mandant_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	name               VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE massenversand (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	einstellungen      VARCHAR(255) NOT NULL,
	text               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE massenversand_gesuch (
	massenversand_id BINARY(16) NOT NULL,
	gesuch_id        BINARY(16) NOT NULL
);

CREATE TABLE mitteilung (
	dtype              VARCHAR(31)  NOT NULL,
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	empfaenger_typ     VARCHAR(255) NOT NULL,
	message            VARCHAR(4000),
	mitteilung_status  VARCHAR(255) NOT NULL,
	sender_typ         VARCHAR(255) NOT NULL,
	sent_datum         DATETIME,
	subject            VARCHAR(255),
	applied            BIT,
	betreuung_id       BINARY(16),
	dossier_id         BINARY(16)   NOT NULL,
	empfaenger_id      BINARY(16),
	sender_id          BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE mitteilung_aud (
	id                 BINARY(16)  NOT NULL,
	rev                INTEGER     NOT NULL,
	dtype              VARCHAR(31) NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	empfaenger_typ     VARCHAR(255),
	message            VARCHAR(4000),
	mitteilung_status  VARCHAR(255),
	sender_typ         VARCHAR(255),
	sent_datum         DATETIME,
	subject            VARCHAR(255),
	betreuung_id       BINARY(16),
	dossier_id         BINARY(16),
	empfaenger_id      BINARY(16),
	sender_id          BINARY(16),
	applied            BIT,
	PRIMARY KEY (id, rev)
);

CREATE TABLE modul_tagesschule_aud (
	id                                    BINARY(16) NOT NULL,
	rev                                   INTEGER    NOT NULL,
	revtype                               TINYINT,
	timestamp_erstellt                    DATETIME,
	timestamp_mutiert                     DATETIME,
	user_erstellt                         VARCHAR(255),
	user_mutiert                          VARCHAR(255),
	vorgaenger_id                         VARCHAR(36),
	modul_tagesschule_name                VARCHAR(255),
	wochentag                             VARCHAR(255),
	zeit_bis                              TIME,
	zeit_von                              TIME,
	institution_stammdaten_tagesschule_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE modul_tagesschule (
	id                                    BINARY(16)   NOT NULL,
	timestamp_erstellt                    DATETIME     NOT NULL,
	timestamp_mutiert                     DATETIME     NOT NULL,
	user_erstellt                         VARCHAR(255) NOT NULL,
	user_mutiert                          VARCHAR(255) NOT NULL,
	version                               BIGINT       NOT NULL,
	vorgaenger_id                         VARCHAR(36),
	modul_tagesschule_name                VARCHAR(255) NOT NULL,
	wochentag                             VARCHAR(255) NOT NULL,
	zeit_bis                              TIME         NOT NULL,
	zeit_von                              TIME         NOT NULL,
	institution_stammdaten_tagesschule_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE pain001dokument (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	typ                VARCHAR(255)  NOT NULL,
	write_protected    BIT           NOT NULL,
	zahlungsauftrag_id BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE pain001dokument_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255),
	filepfad           VARCHAR(4000),
	filesize           VARCHAR(255),
	typ                VARCHAR(255),
	write_protected    BIT,
	zahlungsauftrag_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE pensum_ausserordentlicher_anspruch_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	pensum             INTEGER,
	begruendung        VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE pensum_fachstelle_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	pensum             INTEGER,
	integration_typ    VARCHAR(255),
	fachstelle_id      BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE pensum_ausserordentlicher_anspruch (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	pensum             INTEGER      NOT NULL,
	begruendung        VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE pensum_fachstelle (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	pensum             INTEGER      NOT NULL,
	integration_typ    VARCHAR(255) NOT NULL,
	fachstelle_id      BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE revinfo (
	rev      INTEGER NOT NULL AUTO_INCREMENT,
	revtstmp BIGINT,
	PRIMARY KEY (rev)
);

CREATE TABLE sequence (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	current_value      BIGINT       NOT NULL,
	sequence_type      VARCHAR(100) NOT NULL,
	mandant_id         BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE text_ressource_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	text_deutsch       VARCHAR(4000),
	text_franzoesisch  VARCHAR(4000),
	primary key (id, rev)
);

CREATE TABLE text_ressource (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	text_deutsch       VARCHAR(4000),
	text_franzoesisch  VARCHAR(4000),
	PRIMARY KEY (id)
);

CREATE TABLE traegerschaft (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	active             BIT          NOT NULL,
	name               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE traegerschaft_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	active             BIT,
	name               VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE unbezahlter_urlaub_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE unbezahlter_urlaub (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE verfuegung (
	timestamp_erstellt        DATETIME     NOT NULL,
	timestamp_mutiert         DATETIME     NOT NULL,
	user_erstellt             VARCHAR(255) NOT NULL,
	user_mutiert              VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,
	vorgaenger_id             VARCHAR(36),
	generated_bemerkungen     VARCHAR(4000),
	kategorie_kein_pensum     BIT          NOT NULL,
	kategorie_max_einkommen   BIT          NOT NULL,
	kategorie_nicht_eintreten BIT          NOT NULL,
	kategorie_normal          BIT          NOT NULL,
	manuelle_bemerkungen      VARCHAR(4000),
	betreuung_id              BINARY(16)  NOT NULL,
	PRIMARY KEY (betreuung_id)
);

CREATE TABLE verfuegung_aud (
	betreuung_id              BINARY(16) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(255),
	user_mutiert              VARCHAR(255),
	vorgaenger_id             VARCHAR(36),
	generated_bemerkungen     VARCHAR(4000),
	kategorie_kein_pensum     BIT,
	kategorie_max_einkommen   BIT,
	kategorie_nicht_eintreten BIT,
	kategorie_normal          BIT,
	manuelle_bemerkungen      VARCHAR(4000),
	PRIMARY KEY (betreuung_id, rev)
);

CREATE TABLE verfuegung_zeitabschnitt_aud (
	id                                                   BINARY(16) NOT NULL,
	rev                                                  INTEGER     NOT NULL,
	revtype                                              TINYINT,
	timestamp_erstellt                                   DATETIME,
	timestamp_mutiert                                    DATETIME,
	user_erstellt                                        VARCHAR(255),
	user_mutiert                                         VARCHAR(255),
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE,
	gueltig_bis                                          DATE,
	abzug_fam_groesse                                    DECIMAL(19, 2),
	anspruchberechtigtes_pensum                          INTEGER,
	bemerkungen                                          VARCHAR(4000),
	betreuungspensum                                     DECIMAL(19, 2),
	betreuungsstunden                                    DECIMAL(19, 2),
	einkommensjahr                                       INTEGER,
	elternbeitrag                                        DECIMAL(19, 2),
	fam_groesse                                          DECIMAL(19, 2),
	massgebendes_einkommen_vor_abzug_famgr               DECIMAL(19, 2),
	minimaler_elternbeitrag                              DECIMAL(19, 2),
	minimales_ewp_unterschritten                         BIT,
	verguenstigung                                       DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_vollkosten     DECIMAL(19, 2),
	vollkosten                                           DECIMAL(19, 2),
	zahlungsstatus                                       VARCHAR(255),
	zu_spaet_eingereicht                                 BIT,
	verfuegung_betreuung_id                              BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE verfuegung_zeitabschnitt (
	id                                                   BINARY(16)    NOT NULL,
	timestamp_erstellt                                   DATETIME       NOT NULL,
	timestamp_mutiert                                    DATETIME       NOT NULL,
	user_erstellt                                        VARCHAR(255)   NOT NULL,
	user_mutiert                                         VARCHAR(255)   NOT NULL,
	version                                              BIGINT         NOT NULL,
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE           NOT NULL,
	gueltig_bis                                          DATE           NOT NULL,
	abzug_fam_groesse                                    DECIMAL(19, 2),
	anspruchberechtigtes_pensum                          INTEGER        NOT NULL,
	bemerkungen                                          VARCHAR(4000),
	betreuungspensum                                     DECIMAL(19, 2) NOT NULL,
	betreuungsstunden                                    DECIMAL(19, 2),
	einkommensjahr                                       INTEGER        NOT NULL,
	elternbeitrag                                        DECIMAL(19, 2),
	fam_groesse                                          DECIMAL(19, 2),
	massgebendes_einkommen_vor_abzug_famgr               DECIMAL(19, 2),
	minimaler_elternbeitrag                              DECIMAL(19, 2),
	minimales_ewp_unterschritten                         BIT            NOT NULL,
	verguenstigung                                       DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_vollkosten     DECIMAL(19, 2),
	vollkosten                                           DECIMAL(19, 2),
	zahlungsstatus                                       VARCHAR(255)   NOT NULL,
	zu_spaet_eingereicht                                 BIT            NOT NULL,
	verfuegung_betreuung_id                              BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE vorlage (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE vorlage_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255),
	filepfad           VARCHAR(4000),
	filesize           VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE wizard_step_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	bemerkungen        VARCHAR(4000),
	verfuegbar         BIT,
	wizard_step_name   VARCHAR(255),
	wizard_step_status VARCHAR(255),
	gesuch_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE wizard_step (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	bemerkungen        VARCHAR(4000),
	verfuegbar         BIT          NOT NULL,
	wizard_step_name   VARCHAR(255) NOT NULL,
	wizard_step_status VARCHAR(255) NOT NULL,
	gesuch_id          BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE workjob (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	execution_id       BIGINT,
	metadata           LONGTEXT,
	params             VARCHAR(255) NOT NULL,
	requesturi         VARCHAR(255) NOT NULL,
	result_data        VARCHAR(255),
	startinguser       VARCHAR(255) NOT NULL,
	status             VARCHAR(255) NOT NULL,
	triggering_ip      VARCHAR(45),
	work_job_type      VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE zahlung (
	id                        BINARY(16)   NOT NULL,
	timestamp_erstellt        DATETIME     NOT NULL,
	timestamp_mutiert         DATETIME     NOT NULL,
	user_erstellt             VARCHAR(255) NOT NULL,
	user_mutiert              VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,
	vorgaenger_id             VARCHAR(36),
	betrag_total_zahlung      DECIMAL(19, 2),
	status                    VARCHAR(255) NOT NULL,
	institution_stammdaten_id BINARY(16)   NOT NULL,
	zahlungsauftrag_id        BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE zahlung_aud (
	id                        BINARY(16) NOT NULL,
	rev                       INTEGER    NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(255),
	user_mutiert              VARCHAR(255),
	vorgaenger_id             VARCHAR(36),
	betrag_total_zahlung      DECIMAL(19, 2),
	status                    VARCHAR(255),
	institution_stammdaten_id BINARY(16),
	zahlungsauftrag_id        BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE zahlungsauftrag (
	id                   BINARY(16)   NOT NULL,
	timestamp_erstellt   DATETIME     NOT NULL,
	timestamp_mutiert    DATETIME     NOT NULL,
	user_erstellt        VARCHAR(255) NOT NULL,
	user_mutiert         VARCHAR(255) NOT NULL,
	version              BIGINT       NOT NULL,
	vorgaenger_id        VARCHAR(36),
	gueltig_ab           DATE         NOT NULL,
	gueltig_bis          DATE         NOT NULL,
	beschrieb            VARCHAR(255) NOT NULL,
	betrag_total_auftrag DECIMAL(19, 2),
	datum_faellig        DATE         NOT NULL,
	datum_generiert      DATETIME     NOT NULL,
	status               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE zahlungsauftrag_aud (
	id                   BINARY(16) NOT NULL,
	rev                  INTEGER    NOT NULL,
	revtype              TINYINT,
	timestamp_erstellt   DATETIME,
	timestamp_mutiert    DATETIME,
	user_erstellt        VARCHAR(255),
	user_mutiert         VARCHAR(255),
	vorgaenger_id        VARCHAR(36),
	gueltig_ab           DATE,
	gueltig_bis          DATE,
	beschrieb            VARCHAR(255),
	betrag_total_auftrag DECIMAL(19, 2),
	datum_faellig        DATE,
	datum_generiert      DATETIME,
	status               VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE zahlungsposition (
	id                          BINARY(16)     NOT NULL,
	timestamp_erstellt          DATETIME       NOT NULL,
	timestamp_mutiert           DATETIME       NOT NULL,
	user_erstellt               VARCHAR(255)   NOT NULL,
	user_mutiert                VARCHAR(255)   NOT NULL,
	version                     BIGINT         NOT NULL,
	vorgaenger_id               VARCHAR(36),
	betrag                      DECIMAL(19, 2) NOT NULL,
	ignoriert                   BIT            NOT NULL,
	status                      VARCHAR(255)   NOT NULL,
	verfuegung_zeitabschnitt_id BINARY(16)     NOT NULL,
	zahlung_id                  BINARY(16)     NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE zahlungsposition_aud (
	id                          BINARY(16) NOT NULL,
	rev                         INTEGER    NOT NULL,
	revtype                     TINYINT,
	timestamp_erstellt          DATETIME,
	timestamp_mutiert           DATETIME,
	user_erstellt               VARCHAR(255),
	user_mutiert                VARCHAR(255),
	vorgaenger_id               VARCHAR(36),
	betrag                      DECIMAL(19, 2),
	ignoriert                   BIT,
	status                      VARCHAR(255),
	verfuegung_zeitabschnitt_id BINARY(16),
	zahlung_id                  BINARY(16),
	PRIMARY KEY (id, rev)
);

ALTER TABLE application_property
	ADD CONSTRAINT UK_application_property_name UNIQUE (name);
CREATE INDEX IX_authorisierter_benutzer ON authorisierter_benutzer(benutzer_id);
CREATE INDEX IX_authorisierter_benutzer_token ON authorisierter_benutzer(auth_token, benutzer_id);
CREATE INDEX IX_benutzer_username ON benutzer(username);
CREATE INDEX IX_benutzer_externalUUID ON benutzer(externaluuid);

ALTER TABLE benutzer
	ADD CONSTRAINT UK_username UNIQUE (username);

ALTER TABLE benutzer
	ADD CONSTRAINT UK_externalUUID UNIQUE (externaluuid);
CREATE INDEX IX_berechtigung_gemeinde_berechtigung_id ON berechtigung_gemeinde(berechtigung_id);
CREATE INDEX IX_berechtigung_gemeinde_gemeinde_id ON berechtigung_gemeinde(gemeinde_id);

ALTER TABLE betreuung
	ADD CONSTRAINT UK_betreuung_kind_betreuung_nummer UNIQUE (betreuung_nummer, kind_id);

CREATE INDEX IX_dossier_verantwortlicher_bg ON dossier(verantwortlicherbg_id);
CREATE INDEX IX_dossier_verantwortlicher_ts ON dossier(verantwortlicherts_id);

ALTER TABLE dossier
	ADD CONSTRAINT UK_dossier_fall_gemeinde UNIQUE (fall_id, gemeinde_id);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT UK_einkommensverschlechterungcontainer_gesuchsteller UNIQUE (gesuchsteller_container_id);

CREATE INDEX IX_fall_fall_nummer ON fall(fall_nummer);
CREATE INDEX IX_fall_besitzer ON fall(besitzer_id);
CREATE INDEX IX_fall_mandant ON fall(mandant_id);

ALTER TABLE fall
	ADD CONSTRAINT UK_fall_nummer UNIQUE (fall_nummer);

ALTER TABLE fall
	ADD CONSTRAINT UK_fall_besitzer UNIQUE (besitzer_id);

ALTER TABLE ferieninsel_stammdaten_ferieninsel_zeitraum
	ADD CONSTRAINT UK_ferieninsel_stammdaten_zeitraum_id UNIQUE (zeitraum_id);

ALTER TABLE finanzielle_situation_container
	ADD CONSTRAINT UK_finanzielle_situation_container_gesuchsteller UNIQUE (gesuchsteller_container_id);

ALTER TABLE gemeinde
	ADD CONSTRAINT UK_gemeinde_name UNIQUE (name);

ALTER TABLE gemeinde
	ADD CONSTRAINT UK_gemeinde_bfsnummer UNIQUE (bfs_nummer);

ALTER TABLE gemeinde
	ADD CONSTRAINT UK_gemeinde_gemeindeNummer_mandant UNIQUE (gemeinde_nummer, mandant_id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT UK_gemeinde_stammdaten_gemeinde_id UNIQUE (gemeinde_id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT UK_gemeinde_stammdaten_adresse_id UNIQUE (adresse_id);
CREATE INDEX IX_gesuch_timestamp_erstellt ON gesuch(timestamp_erstellt);

ALTER TABLE gesuch
	ADD CONSTRAINT UK_gueltiges_gesuch UNIQUE (dossier_id, gesuchsperiode_id, gueltig);
CREATE INDEX IX_institution_stammdaten_gueltig_ab ON institution_stammdaten(gueltig_ab);
CREATE INDEX IX_institution_stammdaten_gueltig_bis ON institution_stammdaten(gueltig_bis);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT UK_institution_stammdaten_adresse_id UNIQUE (adresse_id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT UK_institution_stammdaten_adressekontoinhaber_id UNIQUE (adresse_kontoinhaber_id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT UK_institution_stammdaten_institution_id UNIQUE (institution_id);
CREATE INDEX IX_kind_geburtsdatum ON kind(geburtsdatum);

ALTER TABLE kind_container
	ADD CONSTRAINT UK_kindcontainer_gesuch_kind_nummer UNIQUE (kind_nummer, gesuch_id);
CREATE INDEX sequence_ix1 ON sequence(mandant_id);

ALTER TABLE sequence
	ADD CONSTRAINT UK_sequence UNIQUE (sequence_type, mandant_id);

ALTER TABLE wizard_step
	ADD CONSTRAINT UK_wizardstep_gesuch_stepname UNIQUE (wizard_step_name, gesuch_id);

ALTER TABLE abwesenheit_aud
	ADD CONSTRAINT FK_abwesenheit_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE abwesenheit_container_aud
	ADD CONSTRAINT FK_abwesenheit_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE abwesenheit_container
	ADD CONSTRAINT FK_abwesenheit_container_abwesenheit_gs
FOREIGN KEY (abwesenheitgs_id)
REFERENCES abwesenheit(id);

ALTER TABLE abwesenheit_container
	ADD CONSTRAINT FK_abwesenheit_container_abwesenheit_ja
FOREIGN KEY (abwesenheitja_id)
REFERENCES abwesenheit(id);

ALTER TABLE abwesenheit_container
	ADD CONSTRAINT FK_abwesenheit_container_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE adresse_aud
	ADD CONSTRAINT FK_adresse_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE antrag_status_history_aud
	ADD CONSTRAINT FK_antrag_status_history_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE antrag_status_history
	ADD CONSTRAINT FK_antragstatus_history_benutzer_id
FOREIGN KEY (benutzer_id)
REFERENCES benutzer(id);

ALTER TABLE antrag_status_history
	ADD CONSTRAINT FK_antragstatus_history_antrag_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE application_property_aud
	ADD CONSTRAINT FK_application_property_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE authorisierter_benutzer
	ADD CONSTRAINT FK_authorisierter_benutzer_benutzer_id
FOREIGN KEY (benutzer_id)
REFERENCES benutzer(id);

ALTER TABLE belegung_ferieninsel_aud
	ADD CONSTRAINT FK_belegung_ferieninsel_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE belegung_ferieninsel_belegung_ferieninsel_tag_aud
	ADD CONSTRAINT FK_belegung_ferieninsel_belegung_ferieninsel_tag_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE belegung_ferieninsel_tag_aud
	ADD CONSTRAINT FK_belegung_ferieninsel_tag_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE belegung_tagesschule_aud
	ADD CONSTRAINT FK_belegung_tagesschule_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE belegung_tagesschule_modul_tagesschule_aud
	ADD CONSTRAINT FK_belegung_tagesschule_modul_tagesschule_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE belegung_ferieninsel_belegung_ferieninsel_tag
	ADD CONSTRAINT FK_belegung_ferieninsel_tage_id
FOREIGN KEY (tage_id)
REFERENCES belegung_ferieninsel_tag(id);

ALTER TABLE belegung_ferieninsel_belegung_ferieninsel_tag
	ADD CONSTRAINT FK_belegung_ferieninsel_belegung_ferieninsel_id
FOREIGN KEY (belegung_ferieninsel_id)
REFERENCES belegung_ferieninsel(id);

ALTER TABLE belegung_tagesschule_modul_tagesschule
	ADD CONSTRAINT FK_belegung_tagesschule_module_tagesschule_id
FOREIGN KEY (module_tagesschule_id)
REFERENCES modul_tagesschule(id);

ALTER TABLE belegung_tagesschule_modul_tagesschule
	ADD CONSTRAINT FK_belegung_tagesschule_belegung_tagesschule_id
FOREIGN KEY (belegung_tagesschule_id)
REFERENCES belegung_tagesschule(id);

ALTER TABLE benutzer
	ADD CONSTRAINT FK_benutzer_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE benutzer_aud
	ADD CONSTRAINT FK_benutzer_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE berechtigung
	ADD CONSTRAINT FK_Berechtigung_benutzer_id
FOREIGN KEY (benutzer_id)
REFERENCES benutzer(id);

ALTER TABLE berechtigung
	ADD CONSTRAINT FK_Berechtigung_institution_id
FOREIGN KEY (institution_id)
REFERENCES institution(id);

ALTER TABLE berechtigung
	ADD CONSTRAINT FK_Berechtigung_traegerschaft_id
FOREIGN KEY (traegerschaft_id)
REFERENCES traegerschaft(id);

ALTER TABLE berechtigung_aud
	ADD CONSTRAINT FK_berechtigung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_berechtigung_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde(id);

ALTER TABLE berechtigung_gemeinde
	ADD CONSTRAINT FK_berechtigung_gemeinde_gemeinde_id
FOREIGN KEY (berechtigung_id)
REFERENCES berechtigung(id);

ALTER TABLE berechtigung_gemeinde_aud
	ADD CONSTRAINT FK_berechtigung_gemeinde_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE berechtigung_history_aud
	ADD CONSTRAINT FK_berechtigung_history_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE berechtigung_history
	ADD CONSTRAINT FK_berechtigung_history_institution_id
FOREIGN KEY (institution_id)
REFERENCES institution(id);

ALTER TABLE berechtigung_history
	ADD CONSTRAINT FK_berechtigung_history_traegerschaft_id
FOREIGN KEY (traegerschaft_id)
REFERENCES traegerschaft(id);

ALTER TABLE betreuung
	ADD CONSTRAINT FK_betreuung_belegung_ferieninsel_id
FOREIGN KEY (belegung_ferieninsel_id)
REFERENCES belegung_ferieninsel(id);

ALTER TABLE betreuung
	ADD CONSTRAINT FK_betreuung_belegung_tagesschule_id
FOREIGN KEY (belegung_tagesschule_id)
REFERENCES belegung_tagesschule(id);

ALTER TABLE betreuung
	ADD CONSTRAINT FK_betreuung_institution_stammdaten_id
FOREIGN KEY (institution_stammdaten_id)
REFERENCES institution_stammdaten(id);

ALTER TABLE betreuung
	ADD CONSTRAINT FK_betreuung_kind_id
FOREIGN KEY (kind_id)
REFERENCES kind_container(id);

ALTER TABLE betreuung_aud
	ADD CONSTRAINT FK_betreuung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE betreuungsmitteilung_pensum_aud
	ADD CONSTRAINT FK_betreuungsmitteilung_pensum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE betreuungsmitteilung_pensum
	ADD CONSTRAINT FK_betreuungsmitteilung_pens_betreuungsmitteilung_id
FOREIGN KEY (betreuungsmitteilung_id)
REFERENCES mitteilung(id);

ALTER TABLE betreuungspensum_aud
	ADD CONSTRAINT FK_betreuungspensum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE betreuungspensum_container_aud
	ADD CONSTRAINT FK_betreuungspensum_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE betreuungspensum_container
	ADD CONSTRAINT FK_betreuungspensum_container_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE betreuungspensum_container
	ADD CONSTRAINT FK_betreuungspensum_container_betreuungspensum_gs
FOREIGN KEY (betreuungspensumgs_id)
REFERENCES betreuungspensum(id);

ALTER TABLE betreuungspensum_container
	ADD CONSTRAINT FK_betreuungspensum_container_betreuungspensum_ja
FOREIGN KEY (betreuungspensumja_id)
REFERENCES betreuungspensum(id);

ALTER TABLE bfs_gemeinde
	ADD CONSTRAINT FK_bfs_gemeinde_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE dokument
	ADD CONSTRAINT FK_dokument_dokumentgrund_id
FOREIGN KEY (dokument_grund_id)
REFERENCES dokument_grund(id);

ALTER TABLE dokument_aud
	ADD CONSTRAINT FK_dokument_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE dokument_grund_aud
	ADD CONSTRAINT FK_dokument_grund_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE dokument_grund
	ADD CONSTRAINT FK_dokumentGrund_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_fall_id
FOREIGN KEY (fall_id)
REFERENCES fall(id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde(id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_verantwortlicher_bg_id
FOREIGN KEY (verantwortlicherbg_id)
REFERENCES benutzer(id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_verantwortlicher_ts_id
FOREIGN KEY (verantwortlicherts_id)
REFERENCES benutzer(id);

ALTER TABLE dossier_aud
	ADD CONSTRAINT FK_dossier_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE ebegu_vorlage_aud
	ADD CONSTRAINT FK_ebegu_vorlage_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE ebegu_vorlage
	ADD CONSTRAINT FK_ebeguvorlage_vorlage_id
FOREIGN KEY (vorlage_id)
REFERENCES vorlage(id);

ALTER TABLE einkommensverschlechterung_aud
	ADD CONSTRAINT FK_einkommensverschlechterung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE einkommensverschlechterung_container_aud
	ADD CONSTRAINT FK_einkommensverschlechterung_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE einkommensverschlechterung_info_aud
	ADD CONSTRAINT FK_einkommensverschlechterung_info_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE einkommensverschlechterung_info_container_aud
	ADD CONSTRAINT FK_einkommensverschlechterung_info_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus1_id
FOREIGN KEY (ekvgsbasis_jahr_plus1_id)
REFERENCES einkommensverschlechterung(id);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus2_id
FOREIGN KEY (ekvgsbasis_jahr_plus2_id)
REFERENCES einkommensverschlechterung(id);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus1_id
FOREIGN KEY (ekvjabasis_jahr_plus1_id)
REFERENCES einkommensverschlechterung(id);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus2_id
FOREIGN KEY (ekvjabasis_jahr_plus2_id)
REFERENCES einkommensverschlechterung(id);

ALTER TABLE einkommensverschlechterung_container
	ADD CONSTRAINT FK_einkommensverschlechterungcontainer_gesuchstellerContainer_id
FOREIGN KEY (gesuchsteller_container_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE einkommensverschlechterung_info_container
	ADD CONSTRAINT FK_ekvinfocontainer_einkommensverschlechterunginfogs_id
FOREIGN KEY (einkommensverschlechterung_infogs_id)
REFERENCES einkommensverschlechterung_info(id);

ALTER TABLE einkommensverschlechterung_info_container
	ADD CONSTRAINT FK_ekvinfocontainer_einkommensverschlechterunginfoja_id
FOREIGN KEY (einkommensverschlechterung_infoja_id)
REFERENCES einkommensverschlechterung_info(id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde(id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode(id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE einstellung_aud
	ADD CONSTRAINT FK_einstellung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE erweiterte_betreuung_aud
	ADD CONSTRAINT FK_erweiterte_betreuung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE erweiterte_betreuung_container_aud
	ADD CONSTRAINT FK_erweiterte_betreuung_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE erweiterte_betreuung
	ADD CONSTRAINT FK_erweiterte_betreuung_fachstelle_id
FOREIGN KEY (fachstelle_id)
REFERENCES fachstelle(id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_erweiterte_betreuung_gs
FOREIGN KEY (erweiterte_betreuunggs_id)
REFERENCES erweiterte_betreuung(id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_erweiterte_betreuung_ja
FOREIGN KEY (erweiterte_betreuungja_id)
REFERENCES erweiterte_betreuung(id);

ALTER TABLE erwerbspensum
	ADD CONSTRAINT FK_erwerbspensum_urlaub_id
FOREIGN KEY (unbezahlter_urlaub_id)
REFERENCES unbezahlter_urlaub(id);

ALTER TABLE erwerbspensum_aud
	ADD CONSTRAINT FK_erwerbspensum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE erwerbspensum_container_aud
	ADD CONSTRAINT FK_erwerbspensum_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE erwerbspensum_container
	ADD CONSTRAINT FK_erwerbspensum_container_erwerbspensumgs_id
FOREIGN KEY (erwerbspensumgs_id)
REFERENCES erwerbspensum(id);

ALTER TABLE erwerbspensum_container
	ADD CONSTRAINT FK_erwerbspensum_container_erwerbspensumja_id
FOREIGN KEY (erwerbspensumja_id)
REFERENCES erwerbspensum(id);

ALTER TABLE erwerbspensum_container
	ADD CONSTRAINT FK_erwerbspensum_container_gesuchstellerContainer_id
FOREIGN KEY (gesuchsteller_container_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE fachstelle_aud
	ADD CONSTRAINT FK_fachstelle_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE fall
	ADD CONSTRAINT FK_fall_besitzer_id
FOREIGN KEY (besitzer_id)
REFERENCES benutzer(id);

ALTER TABLE fall
	ADD CONSTRAINT FK_fall_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE fall_aud
	ADD CONSTRAINT FK_fall_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE familiensituation_aud
	ADD CONSTRAINT FK_familiensituation_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE familiensituation_container_aud
	ADD CONSTRAINT FK_familiensituation_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE familiensituation_container
	ADD CONSTRAINT FK_familiensituation_container_familiensituation_erstgesuch_id
FOREIGN KEY (familiensituation_erstgesuch_id)
REFERENCES familiensituation(id);

ALTER TABLE familiensituation_container
	ADD CONSTRAINT FK_familiensituation_container_familiensituation_GS_id
FOREIGN KEY (familiensituationgs_id)
REFERENCES familiensituation(id);

ALTER TABLE familiensituation_container
	ADD CONSTRAINT FK_familiensituation_container_familiensituation_JA_id
FOREIGN KEY (familiensituationja_id)
REFERENCES familiensituation(id);

ALTER TABLE ferieninsel_stammdaten_aud
	ADD CONSTRAINT FK_ferieninsel_stammdaten_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE ferieninsel_stammdaten_ferieninsel_zeitraum_aud
	ADD CONSTRAINT FK_ferieninsel_stammdaten_ferieninsel_zeitraum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE ferieninsel_zeitraum_aud
	ADD CONSTRAINT FK_ferieninsel_zeitraum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE ferieninsel_stammdaten
	ADD CONSTRAINT FK_ferieninsel_stammdaten_gesuchsperiodeId
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode(id);

ALTER TABLE ferieninsel_stammdaten_ferieninsel_zeitraum
	ADD CONSTRAINT FK_ferieninsel_stammdaten_ferieninsel_zeitraum_id
FOREIGN KEY (zeitraum_id)
REFERENCES ferieninsel_zeitraum(id);

ALTER TABLE ferieninsel_stammdaten_ferieninsel_zeitraum
	ADD CONSTRAINT FK_ferieninsel_stammdaten_ferieninsel_stammdaten_id
FOREIGN KEY (ferieninsel_stammdaten_id)
REFERENCES ferieninsel_stammdaten(id);

ALTER TABLE finanzielle_situation_aud
	ADD CONSTRAINT FK_finanzielle_situation_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE finanzielle_situation_container_aud
	ADD CONSTRAINT FK_finanzielle_situation_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE finanzielle_situation_container
	ADD CONSTRAINT FK_finanzielleSituationContainer_finanzielleSituationGS_id
FOREIGN KEY (finanzielle_situationgs_id)
REFERENCES finanzielle_situation(id);

ALTER TABLE finanzielle_situation_container
	ADD CONSTRAINT FK_finanzielleSituationContainer_finanzielleSituationJA_id
FOREIGN KEY (finanzielle_situationja_id)
REFERENCES finanzielle_situation(id);

ALTER TABLE finanzielle_situation_container
	ADD CONSTRAINT FK_finanzielleSituationContainer_gesuchstellerContainer_id
FOREIGN KEY (gesuchsteller_container_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE gemeinde
	ADD CONSTRAINT FK_gemeinde_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE gemeinde_aud
	ADD CONSTRAINT FK_gemeinde_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gemeinde_stammdaten_aud
	ADD CONSTRAINT FK_gemeinde_stammdaten_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_adresse_id
FOREIGN KEY (adresse_id)
REFERENCES adresse(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_beschwerdeadresse_id
FOREIGN KEY (beschwerde_adresse_id)
REFERENCES adresse(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_defaultbenutzerbg_id
FOREIGN KEY (default_benutzerbg_id)
REFERENCES benutzer(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_defaultbenutzerts_id
FOREIGN KEY (default_benutzerts_id)
REFERENCES benutzer(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_rechtsmittelbelehrung_id
FOREIGN KEY (rechtsmittelbelehrung_id)
REFERENCES text_ressource (id);

ALTER TABLE generated_dokument_aud
	ADD CONSTRAINT FK_generated_dokument_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE generated_dokument
	ADD CONSTRAINT FK_generated_dokument_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_dossier_id
FOREIGN KEY (dossier_id)
REFERENCES dossier(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_einkommensverschlechterungInfoContainer_id
FOREIGN KEY (einkommensverschlechterung_info_container_id)
REFERENCES einkommensverschlechterung_info_container(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_familiensituation_container_id
FOREIGN KEY (familiensituation_container_id)
REFERENCES familiensituation_container(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_antrag_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_gesuchsteller_container1_id
FOREIGN KEY (gesuchsteller1_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_gesuchsteller_container2_id
FOREIGN KEY (gesuchsteller2_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE gesuch_aud
	ADD CONSTRAINT FK_gesuch_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gesuchsperiode_aud
	ADD CONSTRAINT FK_gesuchsperiode_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gesuchsteller_adresse_aud
	ADD CONSTRAINT FK_gesuchsteller_adresse_aud_adresse_aud
FOREIGN KEY (id, rev)
REFERENCES adresse_aud(id, rev);

ALTER TABLE gesuchsteller_adresse_container_aud
	ADD CONSTRAINT FK_gesuchsteller_adresse_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gesuchsteller_aud
	ADD CONSTRAINT FK_gesuchsteller_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gesuchsteller_container_aud
	ADD CONSTRAINT FK_gesuchsteller_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE gesuchsteller_adresse
	ADD CONSTRAINT FK_gesuchsteller_adresse_adresse_id # TODO Dieser FK wird als einziger nicht generiert (https://hibernate.atlassian.net/browse/HHH-10352)
FOREIGN KEY (id)
REFERENCES adresse(id);

ALTER TABLE gesuchsteller_adresse_container
	ADD CONSTRAINT FK_gesuchstelleradresse_container_gesuchstellergs_id
FOREIGN KEY (gesuchsteller_adressegs_id)
REFERENCES gesuchsteller_adresse(id);

ALTER TABLE gesuchsteller_adresse_container
	ADD CONSTRAINT FK_gesuchstelleradresse_container_gesuchstellerja_id
FOREIGN KEY (gesuchsteller_adresseja_id)
REFERENCES gesuchsteller_adresse(id);

ALTER TABLE gesuchsteller_adresse_container
	ADD CONSTRAINT FK_gesuchstelleradresse_container_gesuchstellerContainer_id
FOREIGN KEY (gesuchsteller_container_id)
REFERENCES gesuchsteller_container(id);

ALTER TABLE gesuchsteller_container
	ADD CONSTRAINT FK_gesuchsteller_container_gesuchstellergs_id
FOREIGN KEY (gesuchstellergs_id)
REFERENCES gesuchsteller(id);

ALTER TABLE gesuchsteller_container
	ADD CONSTRAINT FK_gesuchsteller_container_gesuchstellerja_id
FOREIGN KEY (gesuchstellerja_id)
REFERENCES gesuchsteller(id);

ALTER TABLE institution
	ADD CONSTRAINT FK_institution_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE institution
	ADD CONSTRAINT FK_institution_traegerschaft_id
FOREIGN KEY (traegerschaft_id)
REFERENCES traegerschaft(id);

ALTER TABLE institution_aud
	ADD CONSTRAINT FK_institution_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE institution_stammdaten_aud
	ADD CONSTRAINT FK_institution_stammdaten_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE institution_stammdaten_ferieninsel_aud
	ADD CONSTRAINT FK_institution_stammdaten_ferieninsel_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE institution_stammdaten_tagesschule_aud
	ADD CONSTRAINT FK_institution_stammdaten_tagesschule_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT FK_institution_stammdaten_adresse_id
FOREIGN KEY (adresse_id)
REFERENCES adresse(id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT FK_institution_stammdaten_adressekontoinhaber_id
FOREIGN KEY (adresse_kontoinhaber_id)
REFERENCES adresse(id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT FK_institution_stammdaten_institution_id
FOREIGN KEY (institution_id)
REFERENCES institution(id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT FK_inst_stammdaten_inst_stammdaten_ferieninsel_id
FOREIGN KEY (institution_stammdaten_ferieninsel_id)
REFERENCES institution_stammdaten_ferieninsel(id);

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT FK_inst_stammdaten_inst_stammdaten_tagesschule_id
FOREIGN KEY (institution_stammdaten_tagesschule_id)
REFERENCES institution_stammdaten_tagesschule(id);

ALTER TABLE kind
	ADD CONSTRAINT FK_kind_pensum_ausserordentlicheranspruch_id
FOREIGN KEY (pensum_ausserordentlicher_anspruch_id)
REFERENCES pensum_ausserordentlicher_anspruch(id);

ALTER TABLE kind
	ADD CONSTRAINT FK_kind_pensum_fachstelle_id
FOREIGN KEY (pensum_fachstelle_id)
REFERENCES pensum_fachstelle(id);

ALTER TABLE kind_aud
	ADD CONSTRAINT FK_kind_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE kind_container_aud
	ADD CONSTRAINT FK_kind_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE kind_container
	ADD CONSTRAINT FK_kind_container_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE kind_container
	ADD CONSTRAINT FK_kind_container_kindgs_id
FOREIGN KEY (kindgs_id)
REFERENCES kind(id);

ALTER TABLE kind_container
	ADD CONSTRAINT FK_kind_container_kindja_id
FOREIGN KEY (kindja_id)
REFERENCES kind(id);

ALTER TABLE mahnung
	ADD CONSTRAINT FK_mahnung_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE mahnung_aud
	ADD CONSTRAINT FK_mahnung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE mandant_aud
	ADD CONSTRAINT FK_mandant_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE massenversand_gesuch
	ADD CONSTRAINT FK_massenversand_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE massenversand_gesuch
	ADD CONSTRAINT FK_massenversand_massenversand_id
FOREIGN KEY (massenversand_id)
REFERENCES massenversand(id);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_mitteilung_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_mitteilung_dossier_id
FOREIGN KEY (dossier_id)
REFERENCES dossier(id);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_Mitteilung_empfaenger
FOREIGN KEY (empfaenger_id)
REFERENCES benutzer(id);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_Mitteilung_sender
FOREIGN KEY (sender_id)
REFERENCES benutzer(id);

ALTER TABLE mitteilung_aud
	ADD CONSTRAINT FK_mitteilung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE modul_tagesschule_aud
	ADD CONSTRAINT FK_modul_tagesschule_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE modul_tagesschule
	ADD CONSTRAINT FK_modul_tagesschule_inst_stammdaten_tagesschule_id
FOREIGN KEY (institution_stammdaten_tagesschule_id)
REFERENCES institution_stammdaten_tagesschule(id);

ALTER TABLE pain001dokument
	ADD CONSTRAINT FK_pain001dokument_zahlungsauftrag_id
FOREIGN KEY (zahlungsauftrag_id)
REFERENCES zahlungsauftrag(id);

ALTER TABLE pain001dokument_aud
	ADD CONSTRAINT FK_pain001dokument_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE pensum_ausserordentlicher_anspruch_aud
	ADD CONSTRAINT FK_pensum_ausserordentlicher_anspruch_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE pensum_fachstelle_aud
	ADD CONSTRAINT FK_pensum_fachstelle_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE pensum_fachstelle
	ADD CONSTRAINT FK_pensum_fachstelle_fachstelle_id
FOREIGN KEY (fachstelle_id)
REFERENCES fachstelle(id);

ALTER TABLE sequence
	ADD CONSTRAINT FK_sequence_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant(id);

ALTER TABLE text_ressource_aud
	ADD CONSTRAINT FK_text_ressource_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE traegerschaft_aud
	ADD CONSTRAINT FK_traegerschaft_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE unbezahlter_urlaub_aud
	ADD CONSTRAINT FK_unbezahlter_urlaub_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE verfuegung_aud
	ADD CONSTRAINT FK_verfuegung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE verfuegung_zeitabschnitt_aud
	ADD CONSTRAINT FK_verfuegung_zeitabschnitt_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE verfuegung_zeitabschnitt
	ADD CONSTRAINT FK_verfuegung_zeitabschnitt_verfuegung_id
FOREIGN KEY (verfuegung_betreuung_id)
REFERENCES verfuegung (betreuung_id);

ALTER TABLE vorlage_aud
	ADD CONSTRAINT FK_vorlage_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE wizard_step_aud
	ADD CONSTRAINT FK_wizard_step_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE wizard_step
	ADD CONSTRAINT FK_wizardstep_gesuch_id
FOREIGN KEY (gesuch_id)
REFERENCES gesuch(id);

ALTER TABLE zahlung
	ADD CONSTRAINT FK_Zahlung_institutionstammdaten_id
FOREIGN KEY (institution_stammdaten_id)
REFERENCES institution_stammdaten(id);

ALTER TABLE zahlung
	ADD CONSTRAINT FK_Zahlung_zahlungsauftrag_id
FOREIGN KEY (zahlungsauftrag_id)
REFERENCES zahlungsauftrag(id);

ALTER TABLE zahlung_aud
	ADD CONSTRAINT FK_zahlung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE zahlungsauftrag_aud
	ADD CONSTRAINT FK_zahlungsauftrag_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE zahlungsposition
	ADD CONSTRAINT FK_Zahlungsposition_verfuegungZeitabschnitt_id
FOREIGN KEY (verfuegung_zeitabschnitt_id)
REFERENCES verfuegung_zeitabschnitt (id);

ALTER TABLE zahlungsposition
	ADD CONSTRAINT FK_Zahlungsposition_zahlung_id
FOREIGN KEY (zahlung_id)
REFERENCES zahlung(id);

ALTER TABLE zahlungsposition_aud
	ADD CONSTRAINT FK_zahlungsposition_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);
