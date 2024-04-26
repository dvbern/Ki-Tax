/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

# BetreuungAnfrageEventDTO, InstitutionEventDTO, VerfuegungEventDTO
UPDATE outbox_event
SET avro_schema = REPLACE(
	avro_schema,
	'{"type":"enum","name":"BetreuungsangebotTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["KITA","TAGESSCHULE","TAGESFAMILIEN","FERIENINSEL"]}',
	'{"type":"enum","name":"BetreuungsangebotTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["KITA","TAGESSCHULE","TAGESFAMILIEN","FERIENINSEL","MITTAGSTISCH","UNKNOWN"],"default":"UNKNOWN"}'
				  );

UPDATE outbox_event
SET avro_schema = REPLACE(
	avro_schema,
	'{"type":"enum","name":"EinschulungTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["VORSCHULALTER","KINDERGARTEN1","FREIWILLIGER_KINDERGARTEN","KINDERGARTEN2","OBLIGATORISCHER_KINDERGARTEN","PRIMAR_SEKUNDAR_STUFE","KLASSE1","KLASSE2","KLASSE3","KLASSE4","KLASSE5","KLASSE6","KLASSE7","KLASSE8","KLASSE9"]}',
	'{"type":"enum","name":"EinschulungTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["VORSCHULALTER","KINDERGARTEN1","FREIWILLIGER_KINDERGARTEN","KINDERGARTEN2","OBLIGATORISCHER_KINDERGARTEN","PRIMARSTUFE","PRIMAR_SEKUNDAR_STUFE","SEKUNDAR_UND_HOEHER_STUFE","KLASSE1","KLASSE2","KLASSE3","KLASSE4","KLASSE5","KLASSE6","KLASSE7","KLASSE8","KLASSE9","UNKNOWN"],"default":"UNKNOWN"}'
				  );

UPDATE outbox_event
SET avro_schema = REPLACE(
	avro_schema,
	'{"name":"einschulungTyp","type":["null",{"type":"enum","name":"EinschulungTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["VORSCHULALTER","KINDERGARTEN1","FREIWILLIGER_KINDERGARTEN","KINDERGARTEN2","OBLIGATORISCHER_KINDERGARTEN","PRIMAR_SEKUNDAR_STUFE","KLASSE1","KLASSE2","KLASSE3","KLASSE4","KLASSE5","KLASSE6","KLASSE7","KLASSE8","KLASSE9"]}],"default":null}',
	'{"type":"enum","name":"EinschulungTyp","namespace":"ch.dvbern.kibon.exchange.commons.types","symbols":["VORSCHULALTER","KINDERGARTEN1","FREIWILLIGER_KINDERGARTEN","KINDERGARTEN2","OBLIGATORISCHER_KINDERGARTEN","PRIMARSTUFE","PRIMAR_SEKUNDAR_STUFE","SEKUNDAR_UND_HOEHER_STUFE","KLASSE1","KLASSE2","KLASSE3","KLASSE4","KLASSE5","KLASSE6","KLASSE7","KLASSE8","KLASSE9","UNKNOWN"],"default":"UNKNOWN"}'
				  );

SELECT *
FROM outbox_event
WHERE avro_schema LIKE '{"type":"record","name":"BetreuungAnfrageEventDTO"%';
