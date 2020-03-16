drop table ferieninsel_stammdaten_ferieninsel_zeitraum;
drop table ferieninsel_stammdaten_ferieninsel_zeitraum_aud;
drop table ferieninsel_zeitraum;
drop table ferieninsel_zeitraum_aud;
drop table ferieninsel_stammdaten;
drop table ferieninsel_stammdaten_aud;

create table gemeinde_stammdaten_gesuchsperiode_ferieninsel (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	vorgaenger_id varchar(36),
	anmeldeschluss date not null,
	ferienname varchar(255) not null,
	gemeinde_stammdaten_gesuchsperiode_id binary(16) not null,
	primary key (id)
);

create table gemeinde_stammdaten_gesuchsperiode_ferieninsel_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	vorgaenger_id varchar(36),
	anmeldeschluss date,
	ferienname varchar(255),
	gemeinde_stammdaten_gesuchsperiode_id binary(16) not null,
	primary key (id, rev)
);

create table gemeinde_stammdaten_gesuchsperiode_ferieninsel_zeitraum (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	vorgaenger_id varchar(36),
	gueltig_ab date not null,
	gueltig_bis date not null,
	primary key (id)
);

create table gemeinde_stammdaten_gesuchsperiode_ferieninsel_zeitraum_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	vorgaenger_id varchar(36),
	gueltig_ab date,
	gueltig_bis date,
	primary key (id, rev)
);

create table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum (
	ferieninsel_stammdaten_id binary(16) not null,
	zeitraum_id binary(16) not null
);

create table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum_aud (
	rev integer not null,
	ferieninsel_stammdaten_id binary(16) not null,
	zeitraum_id binary(16) not null,
	revtype tinyint,
	primary key (rev, ferieninsel_stammdaten_id, zeitraum_id)
);

create index IX_ferieninsel_stammdaten_ferieninsel_stammdaten_id on gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum (ferieninsel_stammdaten_id);
create index IX_ferieninsel_stammdaten_zeitraum_id on gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum (zeitraum_id);

alter table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum
	add constraint UK_ferieninsel_stammdaten_zeitraum_id unique (zeitraum_id);

alter table gemeinde_stammdaten_gesuchsperiode_ferieninsel_aud
	add constraint FK_gmde_stammdaten_gp_fi_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum_aud
	add constraint FK_gmde_gp_fi_gmde_gp_fi_zeitraum_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table gemeinde_stammdaten_gesuchsperiode_ferieninsel_zeitraum_aud
	add constraint FK_gmde_stammdaten_gp_fi_zeitraum_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table gemeinde_stammdaten_gesuchsperiode_ferieninsel
	add constraint FK_ferieninsel_stammdaten_gemeinde_stammdaten_gesuchsperiodeId
foreign key (gemeinde_stammdaten_gesuchsperiode_id)
references gemeinde_stammdaten_gesuchsperiode (id);

alter table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum
	add constraint FK_ferieninsel_stammdaten_ferieninsel_zeitraum_id
foreign key (zeitraum_id)
references gemeinde_stammdaten_gesuchsperiode_ferieninsel_zeitraum (id);

alter table gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum
	add constraint FK_ferieninsel_stammdaten_ferieninsel_stammdaten_id
foreign key (ferieninsel_stammdaten_id)
references gemeinde_stammdaten_gesuchsperiode_ferieninsel (id);

