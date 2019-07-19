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
	kind_id                    binary(16),
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
	kind_id                    binary(16),
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
	kind_id                    binary(16)   not null,
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
	kind_id                    binary(16)   not null,
	belegung_tagesschule_id    binary(16),
	institution_stammdaten_id  binary(16)   not null,
	primary key (id)
);

alter table anmeldung_ferieninsel
	add constraint UK_anmeldung_ferieninsel_kind_betreuung_nummer unique (betreuung_nummer, kind_id);

alter table anmeldung_tagesschule
	add constraint UK_anmeldung_tagesschule_kind_betreuung_nummer unique (betreuung_nummer, kind_id);

alter table anmeldung_ferieninsel_aud
	add constraint FKpm4j5x0erqjuuunut7x8fiu8o
foreign key (rev)
references revinfo (rev);

alter table anmeldung_tagesschule_aud
	add constraint FK99mrsiimout7npkunoq0jdsq3
foreign key (rev)
references revinfo (rev);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_kind_id
foreign key (kind_id)
references kind_container (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_belegung_ferieninsel_id
foreign key (belegung_ferieninsel_id)
references belegung_ferieninsel (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_kind_id
foreign key (kind_id)
references kind_container (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_belegung_tagesschule_id
foreign key (belegung_tagesschule_id)
references belegung_tagesschule (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten (id);