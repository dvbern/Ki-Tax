update betreuung set institution_stammdaten_id = '6b7beb6e-6cf3-49d6-84c0-5818d9215ecd' where institution_stammdaten_id = '961b125d-6141-4f8a-8803-dbc8ba8b5d98';
UPDATE institution_stammdaten SET betreuungsangebot_typ = 'TAGESFAMILIEN' WHERE betreuungsangebot_typ = 'TAGESELTERN_KLEINKIND';
DELETE FROM institution_stammdaten WHERE betreuungsangebot_typ = 'TAGESELTERN_SCHULKIND';