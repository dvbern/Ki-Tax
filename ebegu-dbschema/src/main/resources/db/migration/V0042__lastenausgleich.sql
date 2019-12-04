create table lastenausgleich (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	jahr integer not null,
	total_alle_gemeinden decimal(19,2) not null,
	primary key (id)
);

create table lastenausgleich_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	jahr integer,
	total_alle_gemeinden decimal(19,2),
	primary key (id, rev)
);

create table lastenausgleich_detail_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	betrag_lastenausgleich decimal(19,2),
	jahr integer,
	korrektur bit,
	selbstbehalt_gemeinde decimal(19,2),
	total_belegungen decimal(19,2),
	total_betrag_gutscheine decimal(19,2),
	gemeinde_id binary(16),
	lastenausgleich_id binary(16),
	primary key (id, rev)
);

create table lastenausgleich_grundlagen_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	jahr integer,
	kosten_pro100prozent_platz decimal(19,2),
	primary key (id, rev)
);

create table lastenausgleich_detail (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	betrag_lastenausgleich decimal(19,2) not null,
	jahr integer not null,
	korrektur bit not null,
	selbstbehalt_gemeinde decimal(19,2) not null,
	total_belegungen decimal(19,2) not null,
	total_betrag_gutscheine decimal(19,2) not null,
	gemeinde_id binary(16) not null,
	lastenausgleich_id binary(16) not null,
	primary key (id)
);

create table lastenausgleich_grundlagen (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	jahr integer not null,
	kosten_pro100prozent_platz decimal(19,2) not null,
	primary key (id)
);

alter table lastenausgleich
	add constraint UK_Lastenausgleich_jahr unique (jahr);

alter table lastenausgleich_grundlagen
	add constraint UK_LastenausgleichGrundlagen_jahr unique (jahr);

alter table lastenausgleich_aud
	add constraint FK_lastenausgleich_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table lastenausgleich_detail_aud
	add constraint FK_lastenausgleich_detail_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table lastenausgleich_grundlagen_aud
	add constraint FK_lastenausgleich_grundlagen_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table lastenausgleich_detail
	add constraint FK_lastenausgleich_detail_gemeinde_id
foreign key (gemeinde_id)
references gemeinde (id);

alter table lastenausgleich_detail
	add constraint FK_Lastenausgleich_detail_lastenausgleich_id
foreign key (lastenausgleich_id)
references lastenausgleich (id);