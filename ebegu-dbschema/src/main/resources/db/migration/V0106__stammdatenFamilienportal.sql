alter table ebegu.institution_stammdaten_betreuungsgutscheine
		add column oeffnungs_abweichungen varchar(255),
        add column offen_bis time,
        add column offen_von time;

alter table ebegu.institution_stammdaten_betreuungsgutscheine_aud
		add column oeffnungs_abweichungen varchar(255),
        add column offen_bis time,
        add column offen_von time;

create table institution_stammdaten_betreuungsgutscheine_oeffnungstag (
        insitution_stammdaten_betreuungsgutscheine binary(16) not null,
        oeffnungstage varchar(255)
    );

create table institution_stammdaten_betreuungsgutscheine_oeffnungstag_aud (
        rev integer not null,
        insitution_stammdaten_betreuungsgutscheine binary(16) not null,
        oeffnungstage varchar(255) not null,
        revtype tinyint,
        primary key (rev, insitution_stammdaten_betreuungsgutscheine, oeffnungstage)
    );

create table betreuungsstandort_aud (
        id binary(16) not null,
        rev integer not null,
        revtype tinyint,
        timestamp_erstellt datetime,
        timestamp_mutiert datetime,
        user_erstellt varchar(255),
        user_mutiert varchar(255),
        vorgaenger_id varchar(36),
        mail varchar(255),
        telefon varchar(255),
        webseite varchar(255),
        adresse_id binary(16),
        institution_stammdaten_betreuungsgutscheine_id binary(16),
        primary key (id, rev)
    );

alter table ebegu.institution_stammdaten drop column oeffnungszeiten;

alter table institution_stammdaten_betreuungsgutscheine_oeffnungstag_aud
        add constraint FK_stammdaten_oeffnungstag_aud_rev_info
        foreign key (rev)
        references revinfo (rev);

alter table institution_stammdaten_betreuungsgutscheine_oeffnungstag
        add constraint FK_stammdaten_oeffnungstag_institution_stammdaten_bg
        foreign key (insitution_stammdaten_betreuungsgutscheine)
        references institution_stammdaten_betreuungsgutscheine (id);

create table betreuungsstandort (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	vorgaenger_id varchar(36),
	mail varchar(255),
	telefon varchar(255),
	webseite varchar(255),
	adresse_id binary(16) not null,
	institution_stammdaten_betreuungsgutscheine_id binary(16) not null,
	primary key (id)
);

alter table betreuungsstandort
	add constraint UK_betreuungsstandort_adresse unique (adresse_id);

alter table betreuungsstandort
	add constraint FK_betreuungsstandort_adresse_id
		foreign key (adresse_id)
			references adresse (id);

alter table betreuungsstandort
	add constraint FK_betreuungsstandort_betreuungsgutscheine_id
		foreign key (institution_stammdaten_betreuungsgutscheine_id)
			references institution_stammdaten_betreuungsgutscheine (id);
