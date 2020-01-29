create table tscalculation_result_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	betreuungszeit_pro_woche decimal(19,2),
	gebuehr_pro_stunde decimal(19,2),
	verpflegungskosten decimal(19,2),
	bg_calculation_result_id binary(16),
	primary key (id, rev)
);

create table tscalculation_result (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	betreuungszeit_pro_woche decimal(19,2) not null,
	gebuehr_pro_stunde decimal(19,2) not null,
	verpflegungskosten decimal(19,2) not null,
	bg_calculation_result_id binary(16) not null,
	primary key (id)
);

alter table tscalculation_result
	add constraint UK_tscalculation_result_bg_calculation_result_id unique (bg_calculation_result_id);