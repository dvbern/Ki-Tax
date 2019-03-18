ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_apr;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_aug;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_dez;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_feb;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_jan;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_jul;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_jun;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_mai;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_mrz;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_nov;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_okt;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_sep;
ALTER TABLE einkommensverschlechterung DROP COLUMN nettolohn_zus;

ALTER TABLE einkommensverschlechterung ADD nettolohn DECIMAL(19,2);

ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_apr;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_aug;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_dez;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_feb;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_jan;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_jul;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_jun;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_mai;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_mrz;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_nov;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_okt;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_sep;
ALTER TABLE einkommensverschlechterung_aud DROP COLUMN nettolohn_zus;

ALTER TABLE einkommensverschlechterung_aud ADD nettolohn DECIMAL(19,2);
