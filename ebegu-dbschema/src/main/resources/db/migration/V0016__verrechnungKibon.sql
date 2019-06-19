create table verrechnung_kibon (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	primary key (id)
);

create table verrechnung_kibon_detail (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	total_kinder_verrechnet bigint not null,
	gemeinde_id binary(16) not null,
	gesuchsperiode_id binary(16) not null,
	verrechnung_kibon_id binary(16) not null,
	primary key (id)
);

alter table verrechnung_kibon_detail
	add constraint FK_verrechnungdetail_gemeinde_id
foreign key (gemeinde_id)
references gemeinde (id);

alter table verrechnung_kibon_detail
	add constraint FK_verrechnungdetail_gesuchsperiode_id
foreign key (gesuchsperiode_id)
references gesuchsperiode (id);

alter table verrechnung_kibon_detail
	add constraint FK_verrechnungdetail_verrechnung_id
foreign key (verrechnung_kibon_id)
references verrechnung_kibon (id);