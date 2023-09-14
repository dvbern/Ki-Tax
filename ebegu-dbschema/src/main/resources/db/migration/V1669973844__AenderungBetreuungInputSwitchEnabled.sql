/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

update einstellung
set einstellung_key = 'PENSUM_ANZEIGE_TYP',
    version = version + 1,
    value = 'ZEITEINHEIT_UND_PROZENT',
	erklaerung = 'Das Pensum kann in verschiedenen Zeiteinheiten dargestellt werden. Die gewünschte Darstellung kann mit dieser Einstellung konfiguriert werden.
                 Mögliche Werte: NUR_PROZENT = Das Pensum wird immer in Prozent dargestellt, NUR_STUNDEN = Das Pensum wir immer in Stunden dargestellt,
                 ZEITEINHEIT_UND_PROZENT = Das Pensum kann sowohl in Zeiteinheiten (Stunden für TFOs und Tage für KITAs) als auch in Prozenten (TFOs und KITAS) dargestellt werden.'
where einstellung_key = 'BETREUUNG_INPUT_SWITCH_ENABLED'
and value = 'true';

update einstellung
set einstellung_key = 'PENSUM_ANZEIGE_TYP',
	version = version + 1,
	value = 'NUR_PROZENT',
	erklaerung = 'Das Pensum kann in verschiedenen Zeiteinheiten dargestellt werden. Die gewünschte Darstellung kann mit dieser Einstellung konfiguriert werden.
                 Mögliche Werte: NUR_PROZENT = Das Pensum wird immer in Prozent dargestellt, NUR_STUNDEN = Das Pensum wir immer in Stunden dargestellt,
                 ZEITEINHEIT_UND_PROZENT = Das Pensum kann sowohl in Zeiteinheiten (Stunden für TFOs und Tage für KITAs) als auch in Prozenten (TFOs und KITAS) dargestellt werden.'
where einstellung_key = 'BETREUUNG_INPUT_SWITCH_ENABLED'
and value = 'false';