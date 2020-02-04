CREATE TABLE tscalculation_result_aud (
	id                       BINARY(16) NOT NULL,
	rev                      INTEGER    NOT NULL,
	revtype                  TINYINT,
	timestamp_erstellt       DATETIME,
	timestamp_mutiert        DATETIME,
	user_erstellt            VARCHAR(255),
	user_mutiert             VARCHAR(255),
	betreuungszeit_pro_woche INTEGER,
	gebuehr_pro_stunde       DECIMAL(19, 2),
	total_kosten_pro_woche   DECIMAL(19, 2),
	verpflegungskosten       DECIMAL(19, 2),
	PRIMARY KEY (id, rev)
);

CREATE TABLE tscalculation_result (
	id                       BINARY(16)     NOT NULL,
	timestamp_erstellt       DATETIME       NOT NULL,
	timestamp_mutiert        DATETIME       NOT NULL,
	user_erstellt            VARCHAR(255)   NOT NULL,
	user_mutiert             VARCHAR(255)   NOT NULL,
	version                  BIGINT         NOT NULL,
	betreuungszeit_pro_woche INTEGER        NOT NULL,
	gebuehr_pro_stunde       DECIMAL(19, 2) NOT NULL,
	total_kosten_pro_woche   DECIMAL(19, 2) NOT NULL,
	verpflegungskosten       DECIMAL(19, 2) NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE bgcalculation_result
	ADD ts_calculation_result_mit_paedagogischer_betreuung_id BINARY(16);
ALTER TABLE bgcalculation_result
	ADD ts_calculation_result_ohne_paedagogischer_betreuung_id BINARY(16);

ALTER TABLE bgcalculation_result_aud
	ADD ts_calculation_result_mit_paedagogischer_betreuung_id BINARY(16);
ALTER TABLE bgcalculation_result_aud
	ADD ts_calculation_result_ohne_paedagogischer_betreuung_id BINARY(16);

ALTER TABLE bgcalculation_result
	ADD CONSTRAINT FK_BGCalculationResult_tsCalculationResultMitBetreuung
		FOREIGN KEY (ts_calculation_result_mit_paedagogischer_betreuung_id)
			REFERENCES tscalculation_result(id);

ALTER TABLE bgcalculation_result
	ADD CONSTRAINT FK_BGCalculationResult_tsCalculationResultOhneBetreuung
		FOREIGN KEY (ts_calculation_result_ohne_paedagogischer_betreuung_id)
			REFERENCES tscalculation_result(id);

ALTER TABLE tscalculation_result_aud
	ADD CONSTRAINT FK_tscalculation_result_aud_revinfo
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);


