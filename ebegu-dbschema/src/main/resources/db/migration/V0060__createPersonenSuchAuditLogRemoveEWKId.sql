CREATE TABLE personensuche_audit_log (
	id BIGINT NOT NULL AUTO_INCREMENT,
	called_method VARCHAR(255) NOT NULL,
	validity_date DATE NOT NULL,
	resident_info_parameters LONGTEXT NOT NULL,
	timestamp_result DATETIME NOT NULL,
	timestamp_searchstart DATETIME NOT NULL,
	fault_received VARCHAR(255),
	num_results_received BIGINT,
	total_number_of_results BIGINT,
	username VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

alter table gesuchsteller drop column ewk_person_id;
alter table gesuchsteller drop column ewk_abfrage_datum;

alter table gesuchsteller_aud drop column ewk_abfrage_datum;
alter table gesuchsteller_aud drop column ewk_person_id;
