alter table ebegu.institution_stammdaten
	add column mehrere_betreuungsstandorte bit not null,
	add column offen_bis time not null,
	add column offen_von time not null,
	change column oeffnungszeiten oeffnungs_abweichungen varchar (255);

alter table ebegu.institution_stammdaten_aud
	add column mehrere_betreuungsstandorte bit not null,
	add column offen_bis time not null,
	add column offen_von time not null,
	change column oeffnungszeiten oeffnungs_abweichungen varchar (255);

create table betreuungsstandort (
        id binary(16) not null,
        timestamp_erstellt datetime not null,
        timestamp_mutiert datetime not null,
        user_erstellt varchar(255) not null,
        user_mutiert varchar(255) not null,
        version bigint not null,
        vorgaenger_id varchar(36),
        mail varchar(255) not null,
        telefon varchar(255),
        webseite varchar(255),
        adresse_id binary(16) not null,
        institution_stammdaten_id binary(16) not null,
        primary key (id)
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
        institution_stammdaten_id binary(16),
        primary key (id, rev)
    );

alter table betreuungsstandort
        add constraint UK_adresse unique (adresse_id);

alter table betreuungsstandort
        add constraint FK_institution_stammdaten_adresse_id
        foreign key (adresse_id)
        references adresse (id);

alter table betreuungsstandort
        add constraint FK_betreuungsstandort_institution_stammdaten_id
        foreign key (institution_stammdaten_id)
        references institution_stammdaten (id);

alter table betreuungsstandort_aud
        add constraint FK_betreuungsstandort_aud_revinfo
        foreign key (rev)
        references revinfo (rev);

create table institution_stammdaten_oeffnungszeit (
        insitution_stammdaten binary(16) not null,
        oeffnungszeiten varchar(255) not null
    );

alter table institution_stammdaten_oeffnungszeit_aud
        add constraint FK_institution_stammdaten_oeffnungszeit_aud_revinfo
        foreign key (rev)
        references revinfo (rev);
