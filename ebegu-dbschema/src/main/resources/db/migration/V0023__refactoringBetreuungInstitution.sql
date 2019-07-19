create table anmeldung_ferieninsel_aud (
	id                         binary(16) not null,
	rev                        integer    not null,
	revtype                    tinyint,
	timestamp_erstellt         datetime,
	timestamp_mutiert          datetime,
	user_erstellt              varchar(255),
	user_mutiert               varchar(255),
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer,
	gueltig                    bit,
	anmeldestatus              varchar(255),
	anmeldung_mutation_zustand varchar(255),
	fi_kind_id                    binary(16),
	belegung_ferieninsel_id    binary(16),
	institution_stammdaten_id  binary(16),
	primary key (id, rev)
);

create table anmeldung_tagesschule_aud (
	id                         binary(16) not null,
	rev                        integer    not null,
	revtype                    tinyint,
	timestamp_erstellt         datetime,
	timestamp_mutiert          datetime,
	user_erstellt              varchar(255),
	user_mutiert               varchar(255),
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer,
	gueltig                    bit,
	anmeldestatus              varchar(255),
	anmeldung_mutation_zustand varchar(255),
	keine_detailinformationen  bit,
	ts_kind_id                    binary(16),
	belegung_tagesschule_id    binary(16),
	institution_stammdaten_id  binary(16),
	primary key (id, rev)
);

create table anmeldung_ferieninsel (
	id                         binary(16)   not null,
	timestamp_erstellt         datetime     not null,
	timestamp_mutiert          datetime     not null,
	user_erstellt              varchar(255) not null,
	user_mutiert               varchar(255) not null,
	version                    bigint       not null,
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer      not null,
	gueltig                    bit          not null,
	anmeldestatus              varchar(255) not null,
	anmeldung_mutation_zustand varchar(255),
	fi_kind_id                    binary(16)   not null,
	belegung_ferieninsel_id    binary(16),
	institution_stammdaten_id  binary(16)   not null,
	primary key (id)
);

create table anmeldung_tagesschule (
	id                         binary(16)   not null,
	timestamp_erstellt         datetime     not null,
	timestamp_mutiert          datetime     not null,
	user_erstellt              varchar(255) not null,
	user_mutiert               varchar(255) not null,
	version                    bigint       not null,
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer      not null,
	gueltig                    bit          not null,
	anmeldestatus              varchar(255) not null,
	anmeldung_mutation_zustand varchar(255),
	keine_detailinformationen  bit          not null,
	ts_kind_id                    binary(16)   not null,
	belegung_tagesschule_id    binary(16),
	institution_stammdaten_id  binary(16)   not null,
	primary key (id)
);

create table ferieninsel (
	id                 binary(16)   not null,
	timestamp_erstellt datetime     not null,
	timestamp_mutiert  datetime     not null,
	user_erstellt      varchar(255) not null,
	user_mutiert       varchar(255) not null,
	version            bigint       not null,
	vorgaenger_id      varchar(36),
	name               varchar(255) not null,
	mandant_id         binary(16)   not null,
	traegerschaft_id   binary(16),
	primary key (id)
);

create table ferieninsel_aud (
	id                 binary(16) not null,
	rev                integer    not null,
	revtype            tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert  datetime,
	user_erstellt      varchar(255),
	user_mutiert       varchar(255),
	vorgaenger_id      varchar(36),
	name               varchar(255),
	mandant_id         binary(16),
	traegerschaft_id   binary(16),
	primary key (id, rev)
);

create table institution_stammdaten_ferieninsel1_aud (
	id                                binary(16) not null,
	rev                               integer    not null,
	revtype                           tinyint,
	timestamp_erstellt                datetime,
	timestamp_mutiert                 datetime,
	user_erstellt                     varchar(255),
	user_mutiert                      varchar(255),
	vorgaenger_id                     varchar(36),
	gueltig_ab                        date,
	gueltig_bis                       date,
	mail                              varchar(255),
	send_mail_wenn_offene_pendenzen   bit,
	ausweichstandort_fruehlingsferien varchar(255),
	ausweichstandort_herbstferien     varchar(255),
	ausweichstandort_sommerferien     varchar(255),
	ausweichstandort_sportferien      varchar(255),
	adresse_id                        binary(16),
	ferieninsel_id                    binary(16),
	primary key (id, rev)
);

create table institution_stammdaten_tagesschule1_aud (
	id                              binary(16) not null,
	rev                             integer    not null,
	revtype                         tinyint,
	timestamp_erstellt              datetime,
	timestamp_mutiert               datetime,
	user_erstellt                   varchar(255),
	user_mutiert                    varchar(255),
	vorgaenger_id                   varchar(36),
	gueltig_ab                      date,
	gueltig_bis                     date,
	mail                            varchar(255),
	send_mail_wenn_offene_pendenzen bit,
	adresse_id                      binary(16),
	tagesschule_id                  binary(16),
	primary key (id, rev)
);

create table institution_stammdaten_ferieninsel1 (
	id                                binary(16)   not null,
	timestamp_erstellt                datetime     not null,
	timestamp_mutiert                 datetime     not null,
	user_erstellt                     varchar(255) not null,
	user_mutiert                      varchar(255) not null,
	version                           bigint       not null,
	vorgaenger_id                     varchar(36),
	gueltig_ab                        date         not null,
	gueltig_bis                       date         not null,
	mail                              varchar(255) not null,
	send_mail_wenn_offene_pendenzen   bit          not null,
	ausweichstandort_fruehlingsferien varchar(255),
	ausweichstandort_herbstferien     varchar(255),
	ausweichstandort_sommerferien     varchar(255),
	ausweichstandort_sportferien      varchar(255),
	adresse_id                        binary(16)   not null,
	ferieninsel_id                    binary(16)   not null,
	primary key (id)
);

create table institution_stammdaten_tagesschule1 (
	id                              binary(16)   not null,
	timestamp_erstellt              datetime     not null,
	timestamp_mutiert               datetime     not null,
	user_erstellt                   varchar(255) not null,
	user_mutiert                    varchar(255) not null,
	version                         bigint       not null,
	vorgaenger_id                   varchar(36),
	gueltig_ab                      date         not null,
	gueltig_bis                     date         not null,
	mail                            varchar(255) not null,
	send_mail_wenn_offene_pendenzen bit          not null,
	adresse_id                      binary(16)   not null,
	tagesschule_id                  binary(16)   not null,
	primary key (id)
);

create table tagesschule (
	id                 binary(16)   not null,
	timestamp_erstellt datetime     not null,
	timestamp_mutiert  datetime     not null,
	user_erstellt      varchar(255) not null,
	user_mutiert       varchar(255) not null,
	version            bigint       not null,
	vorgaenger_id      varchar(36),
	name               varchar(255) not null,
	mandant_id         binary(16)   not null,
	traegerschaft_id   binary(16),
	primary key (id)
);

create table tagesschule_aud (
	id                 binary(16) not null,
	rev                integer    not null,
	revtype            tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert  datetime,
	user_erstellt      varchar(255),
	user_mutiert       varchar(255),
	vorgaenger_id      varchar(36),
	name               varchar(255),
	mandant_id         binary(16),
	traegerschaft_id   binary(16),
	primary key (id, rev)
);


alter table anmeldung_ferieninsel
	add constraint UK_anmeldung_ferieninsel_kind_betreuung_nummer unique (betreuung_nummer, fi_kind_id);

alter table anmeldung_tagesschule
	add constraint UK_anmeldung_tagesschule_kind_betreuung_nummer unique (betreuung_nummer, ts_kind_id);

create index IX_institution_stammdaten_fi_gueltig_ab
	on institution_stammdaten_ferieninsel1 (gueltig_ab);

create index IX_institution_stammdaten_fi_gueltig_bis
	on institution_stammdaten_ferieninsel1 (gueltig_bis);

alter table institution_stammdaten_ferieninsel1
	add constraint UK_institution_stammdaten_fi_adresse_id unique (adresse_id);

alter table institution_stammdaten_ferieninsel1
	add constraint UK_institution_stammdaten_fi_ferieninsel_id unique (ferieninsel_id);

create index IX_institution_stammdaten_ts_gueltig_ab
	on institution_stammdaten_tagesschule1 (gueltig_ab);

create index IX_institution_stammdaten_ts_gueltig_bis
	on institution_stammdaten_tagesschule1 (gueltig_bis);

alter table institution_stammdaten_tagesschule1
	add constraint UK_institution_stammdaten_ts_adresse_id unique (adresse_id);

alter table institution_stammdaten_tagesschule1
	add constraint UK_institution_stammdaten_ts_tagesschule_id unique (tagesschule_id);

alter table anmeldung_ferieninsel_aud
	add constraint FK_anmeldung_ferieninsel_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table anmeldung_tagesschule_aud
	add constraint FK_anmeldung_tagesschule_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_kind_id
foreign key (fi_kind_id)
references kind_container (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_belegung_ferieninsel_id
foreign key (belegung_ferieninsel_id)
references belegung_ferieninsel (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten_ferieninsel1 (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_kind_id
foreign key (ts_kind_id)
references kind_container (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_belegung_tagesschule_id
foreign key (belegung_tagesschule_id)
references belegung_tagesschule (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten_tagesschule1 (id);

alter table ferieninsel
	add constraint FK_ferieninsel_mandant_id
foreign key (mandant_id)
references mandant (id);

alter table ferieninsel
	add constraint FK_ferieninsel_traegerschaft_id
foreign key (traegerschaft_id)
references traegerschaft (id);

alter table ferieninsel_aud
	add constraint FK_ferieninsel_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table institution_stammdaten_ferieninsel1_aud
	add constraint FK_institution_stammdaten_fi_ferieninsel_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table institution_stammdaten_tagesschule1_aud
	add constraint FK_institution_stammdaten_ts_tagesschule_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table institution_stammdaten_ferieninsel1
	add constraint FK_institution_stammdaten_ferieninsel_adresse_id
foreign key (adresse_id)
references adresse (id);

alter table institution_stammdaten_ferieninsel1
	add constraint FK_institution_stammdaten_ferieninsel_id
foreign key (ferieninsel_id)
references ferieninsel (id);

alter table institution_stammdaten_tagesschule1
	add constraint FK_institution_stammdaten_tagesschule_adresse_id
foreign key (adresse_id)
references adresse (id);

alter table institution_stammdaten_tagesschule1
	add constraint FK_institution_stammdaten_tagesschule_id
foreign key (tagesschule_id)
references tagesschule (id);

alter table tagesschule
	add constraint FK_tagesschule_mandant_id
foreign key (mandant_id)
references mandant (id);

alter table tagesschule
	add constraint FK_tagesschule_traegerschaft_id
foreign key (traegerschaft_id)
references traegerschaft (id);

alter table tagesschule_aud
	add constraint FK_tagesschule_aud_revinfo
foreign key (rev)
references revinfo (rev);