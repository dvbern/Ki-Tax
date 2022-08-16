UPDATE ebegu.application_property SET value = 'false' WHERE name = 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT' and value = '0';
UPDATE ebegu.application_property SET value = 'true' WHERE name = 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT' and value = '1';
UPDATE application_property SET name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' where name = 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT';

