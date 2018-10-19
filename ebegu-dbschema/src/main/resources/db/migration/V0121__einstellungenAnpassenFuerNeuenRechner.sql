# Fuer jede Gesuchsperiode einfuegen: System Default = KINDERGARTEN2
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG', '140', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG', '100', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_SCHULE_PRO_TG', '75', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD', '11.90', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD', '8.50', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD', '8.50', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MAX_MASSGEBENDES_EINKOMMEN', '160000', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MIN_MASSGEBENDES_EINKOMMEN', '43000', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'OEFFNUNGSTAGE_KITA', '240', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'OEFFNUNGSTAGE_TFO', '240', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'OEFFNUNGSSTUNDEN_TFO', '11', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'ZUSCHLAG_BEHINDERUNG_PRO_TG', '50', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'ZUSCHLAG_BEHINDERUNG_PRO_STD', '4.25', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MIN_VERGUENSTIGUNG_PRO_TG', '7', NULL, p.id, NULL
	FROM gesuchsperiode p
);
INSERT INTO einstellung (
	SELECT
		UUID(), p.timestamp_erstellt, p.timestamp_mutiert, p.user_erstellt, p.user_mutiert, p.version,
		'MIN_VERGUENSTIGUNG_PRO_STD', '0.70', NULL, p.id, NULL
	FROM gesuchsperiode p
);

DELETE FROM einstellung WHERE einstellung_key = 'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_ANZAL_TAGE_MAX_KITA';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_STUNDEN_PRO_TAG_MAX_KITA';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_KOSTEN_PRO_STUNDE_MAX';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_KOSTEN_PRO_STUNDE_MIN';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_ANZAHL_TAGE_KANTON';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_STUNDEN_PRO_TAG_TAGI';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_PENSUM_TAGI_MIN';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_BABY_FAKTOR';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_BABY_ALTER_IN_MONATEN';
DELETE FROM einstellung WHERE einstellung_key = 'PARAM_ABGELTUNG_PRO_TAG_KANTON';