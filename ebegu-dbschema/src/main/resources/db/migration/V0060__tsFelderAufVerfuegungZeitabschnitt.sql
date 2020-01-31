create table tscalculation_result_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	betreuungszeit_pro_woche integer,
	gebuehr_pro_stunde decimal(19,2),
	total_kosten_pro_woche decimal(19,2),
	verpflegungskosten decimal(19,2),
	primary key (id, rev)
);

create table tscalculation_result (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	betreuungszeit_pro_woche integer not null,
	gebuehr_pro_stunde decimal(19,2) not null,
	total_kosten_pro_woche decimal(19,2) not null,
	verpflegungskosten decimal(19,2) not null,
	primary key (id)
);

alter table bgcalculation_result add ts_calculation_result_mit_paedagogischer_betreuung_id binary(16);
alter table bgcalculation_result add ts_calculation_result_ohne_paedagogischer_betreuung_id binary(16);

alter table bgcalculation_result_aud add ts_calculation_result_mit_paedagogischer_betreuung_id binary(16);
alter table bgcalculation_result_aud add ts_calculation_result_ohne_paedagogischer_betreuung_id binary(16);

alter table bgcalculation_result
	add constraint FK_BGCalculationResult_tsCalculationResultMitBetreuung
foreign key (ts_calculation_result_mit_paedagogischer_betreuung_id)
references tscalculation_result (id);

alter table bgcalculation_result
	add constraint FK_BGCalculationResult_tsCalculationResultOhneBetreuung
foreign key (ts_calculation_result_ohne_paedagogischer_betreuung_id)
references tscalculation_result (id);

ALTER TABLE tscalculation_result_aud
	ADD CONSTRAINT FK_tscalculation_result_aud_revinfo
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);


