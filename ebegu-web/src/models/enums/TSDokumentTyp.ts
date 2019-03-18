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

export enum TSDokumentTyp {

    NACHWEIS_TRENNUNG = 'NACHWEIS_TRENNUNG',
    FACHSTELLENBESTAETIGUNG = 'FACHSTELLENBESTAETIGUNG',
    NACHWEIS_ERWERBSPENSUM = 'NACHWEIS_ERWERBSPENSUM',
    NACHWEIS_SELBSTAENDIGKEIT = 'NACHWEIS_SELBSTAENDIGKEIT',
    NACHWEIS_AUSBILDUNG = 'NACHWEIS_AUSBILDUNG',
    NACHWEIS_RAV = 'NACHWEIS_RAV',
    BESTAETIGUNG_ARZT = 'BESTAETIGUNG_ARZT',
    STEUERVERANLAGUNG = 'STEUERVERANLAGUNG',
    STEUERERKLAERUNG = 'STEUERERKLAERUNG',
    JAHRESLOHNAUSWEISE = 'JAHRESLOHNAUSWEISE',
    NACHWEIS_FAMILIENZULAGEN = 'NACHWEIS_FAMILIENZULAGEN',
    NACHWEIS_ERSATZEINKOMMEN = 'NACHWEIS_ERSATZEINKOMMEN',
    NACHWEIS_ERHALTENE_ALIMENTE = 'NACHWEIS_ERHALTENE_ALIMENTE',
    NACHWEIS_GELEISTETE_ALIMENTE = 'NACHWEIS_GELEISTETE_ALIMENTE',
    NACHWEIS_VERMOEGEN = 'NACHWEIS_VERMOEGEN',
    NACHWEIS_SCHULDEN = 'NACHWEIS_SCHULDEN',
    ERFOLGSRECHNUNGEN_JAHR = 'ERFOLGSRECHNUNGEN_JAHR',
    ERFOLGSRECHNUNGEN_JAHR_MINUS1 = 'ERFOLGSRECHNUNGEN_JAHR_MINUS1',
    ERFOLGSRECHNUNGEN_JAHR_MINUS2 = 'ERFOLGSRECHNUNGEN_JAHR_MINUS2',
    DIV = 'DIV',
    ORIGINAL_PAPIERGESUCH = 'ORIGINAL_PAPIERGESUCH',
    UNTERSTUETZUNGSBESTAETIGUNG = 'UNTERSTUETZUNGSBESTAETIGUNG'
}

export function getTSTSDokumentTypValues(): Array<TSDokumentTyp> {
    return [

        TSDokumentTyp.NACHWEIS_TRENNUNG,
        TSDokumentTyp.FACHSTELLENBESTAETIGUNG,
        TSDokumentTyp.NACHWEIS_ERWERBSPENSUM,
        TSDokumentTyp.NACHWEIS_SELBSTAENDIGKEIT,
        TSDokumentTyp.NACHWEIS_AUSBILDUNG,
        TSDokumentTyp.NACHWEIS_RAV,
        TSDokumentTyp.BESTAETIGUNG_ARZT,
        TSDokumentTyp.STEUERVERANLAGUNG,
        TSDokumentTyp.STEUERERKLAERUNG,
        TSDokumentTyp.JAHRESLOHNAUSWEISE,
        TSDokumentTyp.NACHWEIS_FAMILIENZULAGEN,
        TSDokumentTyp.NACHWEIS_ERSATZEINKOMMEN,
        TSDokumentTyp.NACHWEIS_ERHALTENE_ALIMENTE,
        TSDokumentTyp.NACHWEIS_GELEISTETE_ALIMENTE,
        TSDokumentTyp.NACHWEIS_VERMOEGEN,
        TSDokumentTyp.NACHWEIS_SCHULDEN,
        TSDokumentTyp.ERFOLGSRECHNUNGEN_JAHR,
        TSDokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1,
        TSDokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2,
        TSDokumentTyp.DIV,
        TSDokumentTyp.ORIGINAL_PAPIERGESUCH,
        TSDokumentTyp.UNTERSTUETZUNGSBESTAETIGUNG,
    ];
}
