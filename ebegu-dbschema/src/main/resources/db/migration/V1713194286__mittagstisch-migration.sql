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

update betreuungspensum
set unit_for_display           = 'PERCENTAGE',
	monatliche_hauptmahlzeiten = round(pensum * 20.5 / 100, 0),
	tarif_pro_hauptmahlzeit    = round(monatliche_betreuungskosten / round(pensum * 20.5 / 100, 0), 2)
where unit_for_display = 'MAHLZEITEN';

update betreuungspensum_abweichung
set unit_for_display           = 'PERCENTAGE',
	monatliche_hauptmahlzeiten = round(pensum * 20.5 / 100, 0),
	tarif_pro_hauptmahlzeit    = round(monatliche_betreuungskosten / round(pensum * 20.5 / 100, 0), 2)
where unit_for_display = 'MAHLZEITEN';

update betreuungsmitteilung_pensum
set unit_for_display           = 'PERCENTAGE',
	monatliche_hauptmahlzeiten = round(pensum * 20.5 / 100, 0),
	tarif_pro_hauptmahlzeit    = round(monatliche_betreuungskosten / round(pensum * 20.5 / 100, 0), 2)
where unit_for_display = 'MAHLZEITEN';
