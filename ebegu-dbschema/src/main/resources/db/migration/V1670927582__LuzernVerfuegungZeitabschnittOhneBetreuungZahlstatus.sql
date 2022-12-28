/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

update verfuegung_zeitabschnitt set zahlungsstatus_institution = 'VERRECHNET_KEINE_BETREUUNG' where id in (
	select verfuegung_zeitabschnitt.id
	from verfuegung_zeitabschnitt
		 join bgcalculation_result br ON verfuegung_zeitabschnitt.bg_calculation_result_asiv_id = br.id
		 join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
		 join betreuung on verfuegung.betreuung_id = betreuung.id
		 join kind_container kc ON betreuung.kind_id = kc.id
		 join gesuch on kc.gesuch_id = gesuch.id
		 join dossier on gesuch.dossier_id = dossier.id
		 join fall on dossier.fall_id = fall.id
		 join mandant on fall.mandant_id = mandant.id
	where mandant_identifier = 'LUZERN' and
		zahlungsstatus_institution != 'NEU'
		and zahlungsstatus_institution != 'VERRECHNET_KEINE_BETREUUNG' and zahlungsstatus_institution != 'VERRECHNEND'
		and br.betreuungspensum_prozent = 0);
