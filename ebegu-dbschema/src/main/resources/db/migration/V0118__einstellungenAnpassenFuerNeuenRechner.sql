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