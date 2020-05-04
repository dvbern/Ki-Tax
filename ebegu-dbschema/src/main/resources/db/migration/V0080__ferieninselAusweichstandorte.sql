create table einstellungen_ferieninsel_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	gesuchsperiode_id binary(16),
	institution_stammdaten_ferieninsel_id binary(16),
	ausweichstandort_sommerferien varchar(255),
	ausweichstandort_herbstferien varchar(255),
	ausweichstandort_sportferien varchar(255),
	ausweichstandort_fruehlingsferien varchar(255),
	primary key (id, rev)
);

create table einstellungen_ferieninsel (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	gesuchsperiode_id binary(16) not null,
	institution_stammdaten_ferieninsel_id binary(16) not null,
	ausweichstandort_sommerferien varchar(255),
	ausweichstandort_herbstferien varchar(255),
	ausweichstandort_sportferien varchar(255),
	ausweichstandort_fruehlingsferien varchar(255),
	primary key (id)
);

ALTER TABLE institution_stammdaten_ferieninsel DROP ausweichstandort_sommerferien;
ALTER TABLE institution_stammdaten_ferieninsel_aud DROP ausweichstandort_sommerferien;

ALTER TABLE institution_stammdaten_ferieninsel DROP ausweichstandort_herbstferien;
ALTER TABLE institution_stammdaten_ferieninsel_aud DROP ausweichstandort_herbstferien;

ALTER TABLE institution_stammdaten_ferieninsel DROP ausweichstandort_sportferien;
ALTER TABLE institution_stammdaten_ferieninsel_aud DROP ausweichstandort_sportferien;

ALTER TABLE institution_stammdaten_ferieninsel DROP ausweichstandort_fruehlingsferien;
ALTER TABLE institution_stammdaten_ferieninsel_aud DROP ausweichstandort_fruehlingsferien;

alter table einstellungen_ferieninsel_aud
	add constraint FK_einstellungen_ferieninsel_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table einstellungen_ferieninsel
	add constraint FK_einstellungen_fi_gesuchsperiode_id
foreign key (gesuchsperiode_id)
references gesuchsperiode (id);

alter table einstellungen_ferieninsel
	add constraint FK_einstellungen_fi_inst_stammdaten_ferieninsel_id
foreign key (institution_stammdaten_ferieninsel_id)
references institution_stammdaten_ferieninsel (id);
