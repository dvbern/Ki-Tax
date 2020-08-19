alter table generated_dokument
	add constraint UK_generated_dokument_gesuch_filename unique (gesuch_id, filename);