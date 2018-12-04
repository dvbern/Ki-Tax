ALTER TABLE dokument MODIFY filepfad VARCHAR(4000) NOT NULL;
ALTER TABLE dokument_aud MODIFY filepfad VARCHAR(4000);

ALTER TABLE download_file MODIFY filepfad VARCHAR(4000) NOT NULL;

ALTER TABLE generated_dokument MODIFY filepfad VARCHAR(4000) NOT NULL;
ALTER TABLE generated_dokument_aud MODIFY filepfad VARCHAR(4000);

ALTER TABLE vorlage MODIFY filepfad VARCHAR(4000) NOT NULL;
ALTER TABLE vorlage_aud MODIFY filepfad VARCHAR(4000);

ALTER TABLE pain001dokument MODIFY filepfad VARCHAR(4000) NOT NULL;
ALTER TABLE pain001dokument_aud MODIFY filepfad VARCHAR(4000);