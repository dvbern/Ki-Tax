/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {TSPensumUnits} from './enums/TSPensumUnits';
import {TSVerfuegungZeitabschnittZahlungsstatus} from './enums/TSVerfuegungZeitabschnittZahlungsstatus';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';

export class TSVerfuegungZeitabschnitt extends TSAbstractDateRangedEntity {
    public abzugFamGroesse: number;
    public anspruchberechtigtesPensum: number;
    public anspruchsberechtigteAnzahlZeiteinheiten: number;
    public anspruchspensumRest: number;
    public bemerkungen: string;
    public betreuungspensum: number;
    public betreuungsstunden: number;
    public bgPensum: number;
    public einkommensjahr: number;
    public elternbeitrag: number;
    public erwerbspensumGS1: number;
    public erwerbspensumGS2: number;
    public fachstellenpensum: number;
    public famGroesse: number;
    public kategorieKeinPensum: boolean;
    public kategorieMaxEinkommen: boolean;
    public massgebendesEinkommenVorAbzugFamgr: number;
    public minimalerElternbeitrag: number;
    public minimalesEwpUnterschritten: boolean;
    public sameAusbezahlteVerguenstigung: boolean;
    public sameVerfuegteVerfuegungsrelevanteDaten: boolean;
    public verfuegteAnzahlZeiteinheiten: number;
    public verguenstigung: number;
    public verguenstigungOhneBeruecksichtigungMinimalbeitrag: number;
    public verguenstigungOhneBeruecksichtigungVollkosten: number;
    public vollkosten: number;
    public zahlungsstatus: TSVerfuegungZeitabschnittZahlungsstatus;
    public zeiteinheit: TSPensumUnits;
    public zuSpaetEingereicht: boolean;

    public getMinimalerElternbeitragGekuerzt(): number {
        const vollkostenMinusVerguenstigung = this.vollkosten - this.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
        if (vollkostenMinusVerguenstigung > this.minimalerElternbeitrag) {
            return 0;
        }
        return this.minimalerElternbeitrag - vollkostenMinusVerguenstigung;
    }
}
