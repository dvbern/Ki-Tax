ALTER TABLE fachstelle ADD fachstelle_anspruch BIT;
ALTER TABLE fachstelle_aud ADD fachstelle_anspruch BIT;
ALTER TABLE fachstelle ADD fachstelle_erweiterte_betreuung BIT;
ALTER TABLE fachstelle_aud ADD fachstelle_erweiterte_betreuung BIT;

UPDATE fachstelle SET fachstelle.fachstelle_anspruch = 1;
UPDATE fachstelle SET fachstelle.fachstelle_erweiterte_betreuung = 0;
UPDATE fachstelle_aud SET fachstelle_aud.fachstelle_anspruch = 1;
UPDATE fachstelle_aud SET fachstelle_aud.fachstelle_erweiterte_betreuung = 0;

INSERT INTO fachstelle(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, behinderungsbestaetigung, beschreibung, name, fachstelle_anspruch, fachstelle_erweiterte_betreuung) VALUES
('5923ded4-d2ea-11e8-a8d5-f2801f1b9fd1' , '2018-10-18 17:30:04' , '2018-10-18 17:30:04' , 'anonymous' , 'anonymous' , 0 , null , 0 , 'Früherziehungsdienst des Kantons Bern' , 'Früherziehungsdienst des Kantons Bern' , 0 , 1),
('5923e226-d2ea-11e8-a8d5-f2801f1b9fd1' , '2018-10-18 17:30:04' , '2018-10-18 17:30:04' , 'anonymous' , 'anonymous' , 0 , null , 0 , 'Heilpädagogische Früherziehung für blinde und sehbehinderte Kinder der Blindenschule Zollikofen' , 'Heilpädagogische Früherziehung für blinde und sehbehinderte Kinder der Blindenschule Zollikofen' , 0 , 1),
('5923e3de-d2ea-11e8-a8d5-f2801f1b9fd1' , '2018-10-18 17:30:04' , '2018-10-18 17:30:04' , 'anonymous' , 'anonymous' , 0 , null , 0 , 'Audiopädagogischen Dienst des Pädagogischen Zentrums für Hören und Sprache HSM' , 'Audiopädagogischen Dienst des Pädagogischen Zentrums für Hören und Sprache HSM' , 0 , 1);

# add not null just before setting the value in all records to avoid problems with existing data
ALTER TABLE fachstelle MODIFY fachstelle_anspruch BIT NOT NULL;
ALTER TABLE fachstelle MODIFY fachstelle_erweiterte_betreuung BIT NOT NULL;

ALTER TABLE erweiterte_betreuung ADD fachstelle_id VARCHAR(36);
ALTER TABLE erweiterte_betreuung_aud ADD fachstelle_id VARCHAR(36);

ALTER TABLE erweiterte_betreuung
  ADD CONSTRAINT FK_erweiterte_betreuung_fachstelle_id
FOREIGN KEY (fachstelle_id)
REFERENCES fachstelle (id);