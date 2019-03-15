update fachstelle set name = 'MUTTER_VATER_BERATUNG_BERN' where name = 'Mütter- und Väterberatung Bern';
update fachstelle set name = 'SOZIALDIENST' where name = 'Sozialdienst';
update fachstelle set name = 'KINDES_ERWACHSENEN_SCHUTZBEHOERDE' where name = 'Kindes- und Erwachsenenschutzbehörde';
update fachstelle set name = 'ERZIEHUNGSBERATUNG' where name = 'Erziehungsberatung';
update fachstelle set name = 'FRUEHERZIEHUNGSDIENST_KANTON_BERN' where name = 'Früherziehungsdienst des Kantons Bern';
update fachstelle set name = 'FRUEHERZIEHUNG_BLINDENSCHULE_ZOLLIKOFEN' where name = 'Heilpädagogische Früherziehung für blinde und sehbehinderte Kinder der Blindenschule Zollikofen';
update fachstelle set name = 'DIENST_ZENTRUM_HOEREN_SPRACHE' where name = 'Audiopädagogischen Dienst des Pädagogischen Zentrums für Hören und Sprache HSM';

update fachstelle_aud set name = 'MUTTER_VATER_BERATUNG_BERN' where name = 'Mütter- und Väterberatung Bern';
update fachstelle_aud set name = 'SOZIALDIENST' where name = 'Sozialdienst';
update fachstelle_aud set name = 'KINDES_ERWACHSENEN_SCHUTZBEHOERDE' where name = 'Kindes- und Erwachsenenschutzbehörde';
update fachstelle_aud set name = 'ERZIEHUNGSBERATUNG' where name = 'Erziehungsberatung';
update fachstelle_aud set name = 'FRUEHERZIEHUNGSDIENST_KANTON_BERN' where name = 'Früherziehungsdienst des Kantons Bern';
update fachstelle_aud set name = 'FRUEHERZIEHUNG_BLINDENSCHULE_ZOLLIKOFEN' where name = 'Heilpädagogische Früherziehung für blinde und sehbehinderte Kinder der Blindenschule Zollikofen';
update fachstelle_aud set name = 'DIENST_ZENTRUM_HOEREN_SPRACHE' where name = 'Audiopädagogischen Dienst des Pädagogischen Zentrums für Hören und Sprache HSM';

# column Beschreibung is not needed any more
ALTER TABLE fachstelle DROP COLUMN beschreibung;
ALTER TABLE fachstelle_aud DROP COLUMN beschreibung;