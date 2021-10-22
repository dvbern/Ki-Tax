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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {TSFerienbetreuungBerechnung} from '../../app/gemeinde-antraege/ferienbetreuung/ferienbetreuung-kosten-einnahmen/TSFerienbetreuungBerechnung';
import {FerienbetreuungAngabenStatus} from '../enums/FerienbetreuungAngabenStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSGemeinde} from '../TSGemeinde';
import {TSGesuchsperiode} from '../TSGesuchsperiode';
import {TSFerienbetreuungAngaben} from './TSFerienbetreuungAngaben';

export class TSFerienbetreuungAngabenContainer extends TSAbstractEntity {

    private _status: FerienbetreuungAngabenStatus;
    private _gemeinde: TSGemeinde;
    private _gesuchsperiode: TSGesuchsperiode;
    private _angabenDeklaration: TSFerienbetreuungAngaben;
    private _angabenKorrektur: TSFerienbetreuungAngaben;
    private _internerKommentar: string;

    public get status(): FerienbetreuungAngabenStatus {
        return this._status;
    }

    public set status(value: FerienbetreuungAngabenStatus) {
        this._status = value;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(value: TSGesuchsperiode) {
        this._gesuchsperiode = value;
    }

    public get angabenDeklaration(): TSFerienbetreuungAngaben {
        return this._angabenDeklaration;
    }

    public set angabenDeklaration(value: TSFerienbetreuungAngaben) {
        this._angabenDeklaration = value;
    }

    public get angabenKorrektur(): TSFerienbetreuungAngaben {
        return this._angabenKorrektur;
    }

    public set angabenKorrektur(value: TSFerienbetreuungAngaben) {
        this._angabenKorrektur = value;
    }

    public get internerKommentar(): string {
        return this._internerKommentar;
    }

    public set internerKommentar(value: string) {
        this._internerKommentar = value;
    }

    public isAtLeastInPruefungKanton(): boolean {
        return [
            FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON,
            FerienbetreuungAngabenStatus.GEPRUEFT,
            FerienbetreuungAngabenStatus.ABGESCHLOSSEN,
            FerienbetreuungAngabenStatus.ABGELEHNT
        ].includes(this.status);
    }

    public isInPruefungKanton(): boolean {
        return this.status === FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON;
    }

    public isInBearbeitungGemeinde(): boolean {
        return this.status === FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE;
    }

    public isGeprueft(): boolean {
        return [
            FerienbetreuungAngabenStatus.GEPRUEFT,
            FerienbetreuungAngabenStatus.ABGESCHLOSSEN,
            FerienbetreuungAngabenStatus.ABGELEHNT
        ].includes(this.status);
    }

    public isAbgeschlossen(): boolean {
        return [
            FerienbetreuungAngabenStatus.ABGESCHLOSSEN,
            FerienbetreuungAngabenStatus.ABGELEHNT
        ].includes(this.status);
    }

    public calculateBerechnungen(): void {
        if (this.angabenKorrektur === null) {
            throw new Error('Angaben Korrektur must not be null to complete the calculations');
        }
        const berechnungen = new TSFerienbetreuungBerechnung();
        if (!this.angabenKorrektur.kostenEinnahmen.isAbgeschlossen()) {
            throw new Error('Kosten Einnahmen müssen abgeschlossen sein um die Berchnungen durchzuführen');
        }
        if (!this.angabenKorrektur.nutzung.isAbgeschlossen()) {
            throw new Error('Nutzung muss abgeschlossen sein um die Berchnungen durchzuführen');
        }
        const kostenEinnahmen = this.angabenKorrektur.kostenEinnahmen;
        const nutzung = this.angabenKorrektur.nutzung;

        berechnungen.personalkosten = kostenEinnahmen.personalkosten;
        berechnungen.sachkosten = kostenEinnahmen.sachkosten;
        berechnungen.verpflegungskosten = kostenEinnahmen.verpflegungskosten;
        berechnungen.weitereKosten = kostenEinnahmen.weitereKosten;

        berechnungen.anzahlBetreuungstageKinderBern = nutzung.anzahlBetreuungstageKinderBern;
        berechnungen.betreuungstageKinderDieserGemeinde = nutzung.betreuungstageKinderDieserGemeinde;
        berechnungen.betreuungstageKinderDieserGemeindeSonderschueler =
                nutzung.betreuungstageKinderDieserGemeindeSonderschueler;
        berechnungen.betreuungstageKinderAndererGemeinde = nutzung.davonBetreuungstageKinderAndererGemeinden;
        berechnungen.betreuungstageKinderAndererGemeindenSonderschueler =
                nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler;

        berechnungen.einnahmenElterngebuehren = kostenEinnahmen.elterngebuehren;
        berechnungen.weitereEinnahmen = kostenEinnahmen.weitereEinnahmen;

        berechnungen.calculate();

        this.angabenKorrektur.berechnungen = berechnungen;
    }
}
