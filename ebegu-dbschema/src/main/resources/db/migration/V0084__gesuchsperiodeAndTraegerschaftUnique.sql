alter table gesuchsperiode
	add constraint UK_Gesuchsperiode_gueltigAb unique (gueltig_ab);

alter table traegerschaft
	add constraint UK_Traegerschaft_name unique (name);