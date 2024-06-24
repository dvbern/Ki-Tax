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

import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSPensumAnzeigeTyp} from '../../../models/enums/TSPensumAnzeigeTyp';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import {TSBetreuungspensum} from '../../../models/TSBetreuungspensum';
import {TSInstitutionStammdatenSummary} from '../../../models/TSInstitutionStammdatenSummary';
import {TSDateRange} from '../../../models/types/TSDateRange';

export function createTSBetreuungspensum(params: {
    anzeigeEinstellung: TSPensumAnzeigeTyp;
    betreuungsangebotTyp: TSBetreuungsangebotTyp;
    instStammdaten: TSInstitutionStammdatenSummary;
    isTFOKostenBerechnungStuendlich: boolean;
    mahlzeitenverguenstigungActive: boolean;
}): TSBetreuungspensum {
    const tsBetreuungspensum = new TSBetreuungspensum();
    tsBetreuungspensum.unitForDisplay =
        params.anzeigeEinstellung === TSPensumAnzeigeTyp.NUR_STUNDEN
            ? TSPensumUnits.HOURS
            : TSPensumUnits.PERCENTAGE;
    tsBetreuungspensum.nichtEingetreten = false;
    tsBetreuungspensum.gueltigkeit = new TSDateRange();

    if (params.mahlzeitenverguenstigungActive) {
        if (params.instStammdaten.institutionStammdatenBetreuungsgutscheine) {
            // Wir setzen die Defaults der Institution, falls vorhanden (der else-Fall waere bei einer Unbekannten
            // Institution, dort werden die Mahlzeiten eh nicht angezeigt, oder im Fall einer Tagesschule, wo die
            // Tarife auf dem Modul hinterlegt sind)
            tsBetreuungspensum.tarifProHauptmahlzeit =
                params.instStammdaten.institutionStammdatenBetreuungsgutscheine.tarifProHauptmahlzeit;
            tsBetreuungspensum.tarifProNebenmahlzeit =
                params.instStammdaten.institutionStammdatenBetreuungsgutscheine.tarifProNebenmahlzeit;
        }
    } else {
        // die felder sind not null und müssen auf 0 gesetzt werden, damit die validierung nicht fehlschlägt falls
        // die gemeinde die vergünstigung deaktiviert hat
        tsBetreuungspensum.monatlicheNebenmahlzeiten = 0;
        tsBetreuungspensum.monatlicheHauptmahlzeiten = 0;
        tsBetreuungspensum.tarifProHauptmahlzeit = 0;
        tsBetreuungspensum.tarifProNebenmahlzeit = 0;
    }

    if (
        params.isTFOKostenBerechnungStuendlich &&
        params.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN
    ) {
        // die felder sind not null und müssen auf 0 gesetzt werden, damit die Validierung nicht fehlschlägt falls
        // die TFO Kosten stündlich eingegeben werden
        tsBetreuungspensum.monatlicheBetreuungskosten = 0;
        tsBetreuungspensum.pensum = 0;
    }

    if (params.betreuungsangebotTyp === TSBetreuungsangebotTyp.MITTAGSTISCH) {
        return initPensumMittagstisch();
    }

    return tsBetreuungspensum;
}

function initPensumMittagstisch(): TSBetreuungspensum {
    const pensum = new TSBetreuungspensum();
    pensum.nichtEingetreten = false;
    pensum.gueltigkeit = new TSDateRange();
    pensum.unitForDisplay = TSPensumUnits.PERCENTAGE;
    pensum.monatlicheNebenmahlzeiten = 0;
    pensum.tarifProNebenmahlzeit = 0;
    return pensum;
}
