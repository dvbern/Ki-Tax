create table sozialdienst_fall_dokument_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	vorgaenger_id varchar(36),
	filename varchar(255),
	filepfad varchar(4000),
	filesize varchar(255),
	timestamp_upload datetime,
	sozialdienst_fall_id binary(16),
	primary key (id, rev)
);

create table sozialdienst_fall_dokument (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	vorgaenger_id varchar(36),
	filename varchar(255) not null,
	filepfad varchar(4000) not null,
	filesize varchar(255) not null,
	timestamp_upload datetime not null,
	sozialdienst_fall_id binary(16) not null,
	primary key (id)
);

ALTER TABLE sozialdienst_fall drop column vollmacht;
ALTER TABLE sozialdienst_fall_aud drop column vollmacht;

alter table sozialdienst_fall_dokument_aud
	add constraint FK_sozialdienst_fall_dokument_rev
		foreign key (rev)
			references revinfo (rev);

alter table sozialdienst_fall_dokument
	add constraint FK_sozialdienstFallDokument_sozialdienstFall_id
		foreign key (sozialdienst_fall_id)
			references sozialdienst_fall (id);