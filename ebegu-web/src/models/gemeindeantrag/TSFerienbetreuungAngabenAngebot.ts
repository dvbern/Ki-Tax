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

import {TSFerienbetreuungFormularStatus} from '../enums/TSFerienbetreuungFormularStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSAdresse} from '../TSAdresse';

export class TSFerienbetreuungAngabenAngebot extends TSAbstractEntity {

    private _angebot: string;
    private _angebotKontaktpersonVorname: string;
    private _angebotKontaktpersonNachname: string;
    private _angebotAdresse: TSAdresse;
    private _anzahlFerienwochenHerbstferien: number;
    private _anzahlFerienwochenWinterferien: number;
    private _anzahlFerienwochenFruehlingsferien: number;
    private _anzahlFerienwochenSommerferien: number;
    private _anzahlTage: number;
    private _bemerkungenAnzahlFerienwochen: string;
    private _anzahlStundenProBetreuungstag: number;
    private _betreuungErfolgtTagsueber: boolean;
    private _bemerkungenOeffnungszeiten: string;
    private _finanziellBeteiligteGemeinden: string[];
    private _gemeindeFuehrtAngebotSelber: boolean;
    private _gemeindeBeauftragtExterneAnbieter: boolean;
    private _angebotVereineUndPrivateIntegriert: boolean;
    private _bemerkungenKooperation: boolean;
    private _leitungDurchPersonMitAusbildung: string;
    private _betreuungDurchPersonenMitErfahrung: boolean;
    private _anzahlKinderAngemessen: boolean;
    private _betreuungsschluessel: number;
    private _bemerkungenPersonal: string;
    private _fixerTarifKinderDerGemeinde: boolean;
    private _einkommensabhaengigerTarifKinderDerGemeinde: boolean;
    private _tagesschuleTarifGiltFuerFerienbetreuung: boolean;
    private _ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet: boolean;
    private _kinderAusAnderenGemeindenZahlenAnderenTarif: boolean;
    private _bemerkungenTarifsystem: string;
    private _status: TSFerienbetreuungFormularStatus;

    public get angebot(): string {
        return this._angebot;
    }

    public set angebot(value: string) {
        this._angebot = value;
    }

    public get angebotKontaktpersonVorname(): string {
        return this._angebotKontaktpersonVorname;
    }

    public set angebotKontaktpersonVorname(value: string) {
        this._angebotKontaktpersonVorname = value;
    }

    public get angebotKontaktpersonNachname(): string {
        return this._angebotKontaktpersonNachname;
    }

    public set angebotKontaktpersonNachname(value: string) {
        this._angebotKontaktpersonNachname = value;
    }

    public get angebotAdresse(): TSAdresse {
        return this._angebotAdresse;
    }

    public set angebotAdresse(value: TSAdresse) {
        this._angebotAdresse = value;
    }

    public get anzahlFerienwochenHerbstferien(): number {
        return this._anzahlFerienwochenHerbstferien;
    }

    public set anzahlFerienwochenHerbstferien(value: number) {
        this._anzahlFerienwochenHerbstferien = value;
    }

    public get anzahlFerienwochenWinterferien(): number {
        return this._anzahlFerienwochenWinterferien;
    }

    public set anzahlFerienwochenWinterferien(value: number) {
        this._anzahlFerienwochenWinterferien = value;
    }

    public get anzahlFerienwochenFruehlingsferien(): number {
        return this._anzahlFerienwochenFruehlingsferien;
    }

    public set anzahlFerienwochenFruehlingsferien(value: number) {
        this._anzahlFerienwochenFruehlingsferien = value;
    }

    public get anzahlFerienwochenSommerferien(): number {
        return this._anzahlFerienwochenSommerferien;
    }

    public set anzahlFerienwochenSommerferien(value: number) {
        this._anzahlFerienwochenSommerferien = value;
    }

    public get anzahlTage(): number {
        return this._anzahlTage;
    }

    public set anzahlTage(value: number) {
        this._anzahlTage = value;
    }

    public get bemerkungenAnzahlFerienwochen(): string {
        return this._bemerkungenAnzahlFerienwochen;
    }

    public set bemerkungenAnzahlFerienwochen(value: string) {
        this._bemerkungenAnzahlFerienwochen = value;
    }

    public get anzahlStundenProBetreuungstag(): number {
        return this._anzahlStundenProBetreuungstag;
    }

    public set anzahlStundenProBetreuungstag(value: number) {
        this._anzahlStundenProBetreuungstag = value;
    }

    public get betreuungErfolgtTagsueber(): boolean {
        return this._betreuungErfolgtTagsueber;
    }

    public set betreuungErfolgtTagsueber(value: boolean) {
        this._betreuungErfolgtTagsueber = value;
    }

    public get bemerkungenOeffnungszeiten(): string {
        return this._bemerkungenOeffnungszeiten;
    }

    public set bemerkungenOeffnungszeiten(value: string) {
        this._bemerkungenOeffnungszeiten = value;
    }

    public get finanziellBeteiligteGemeinden(): string[] {
        return this._finanziellBeteiligteGemeinden;
    }

    public set finanziellBeteiligteGemeinden(value: string[]) {
        this._finanziellBeteiligteGemeinden = value;
    }

    public get gemeindeFuehrtAngebotSelber(): boolean {
        return this._gemeindeFuehrtAngebotSelber;
    }

    public set gemeindeFuehrtAngebotSelber(value: boolean) {
        this._gemeindeFuehrtAngebotSelber = value;
    }

    public get gemeindeBeauftragtExterneAnbieter(): boolean {
        return this._gemeindeBeauftragtExterneAnbieter;
    }

    public set gemeindeBeauftragtExterneAnbieter(value: boolean) {
        this._gemeindeBeauftragtExterneAnbieter = value;
    }

    public get angebotVereineUndPrivateIntegriert(): boolean {
        return this._angebotVereineUndPrivateIntegriert;
    }

    public set angebotVereineUndPrivateIntegriert(value: boolean) {
        this._angebotVereineUndPrivateIntegriert = value;
    }

    public get bemerkungenKooperation(): boolean {
        return this._bemerkungenKooperation;
    }

    public set bemerkungenKooperation(value: boolean) {
        this._bemerkungenKooperation = value;
    }

    public get leitungDurchPersonMitAusbildung(): string {
        return this._leitungDurchPersonMitAusbildung;
    }

    public set leitungDurchPersonMitAusbildung(value: string) {
        this._leitungDurchPersonMitAusbildung = value;
    }

    public get betreuungDurchPersonenMitErfahrung(): boolean {
        return this._betreuungDurchPersonenMitErfahrung;
    }

    public set betreuungDurchPersonenMitErfahrung(value: boolean) {
        this._betreuungDurchPersonenMitErfahrung = value;
    }

    public get anzahlKinderAngemessen(): boolean {
        return this._anzahlKinderAngemessen;
    }

    public set anzahlKinderAngemessen(value: boolean) {
        this._anzahlKinderAngemessen = value;
    }

    public get betreuungsschluessel(): number {
        return this._betreuungsschluessel;
    }

    public set betreuungsschluessel(value: number) {
        this._betreuungsschluessel = value;
    }

    public get bemerkungenPersonal(): string {
        return this._bemerkungenPersonal;
    }

    public set bemerkungenPersonal(value: string) {
        this._bemerkungenPersonal = value;
    }

    public get fixerTarifKinderDerGemeinde(): boolean {
        return this._fixerTarifKinderDerGemeinde;
    }

    public set fixerTarifKinderDerGemeinde(value: boolean) {
        this._fixerTarifKinderDerGemeinde = value;
    }

    public get einkommensabhaengigerTarifKinderDerGemeinde(): boolean {
        return this._einkommensabhaengigerTarifKinderDerGemeinde;
    }

    public set einkommensabhaengigerTarifKinderDerGemeinde(value: boolean) {
        this._einkommensabhaengigerTarifKinderDerGemeinde = value;
    }

    public get tagesschuleTarifGiltFuerFerienbetreuung(): boolean {
        return this._tagesschuleTarifGiltFuerFerienbetreuung;
    }

    public set tagesschuleTarifGiltFuerFerienbetreuung(value: boolean) {
        this._tagesschuleTarifGiltFuerFerienbetreuung = value;
    }

    public get ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(): boolean {
        return this._ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
    }

    public set ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(value: boolean) {
        this._ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = value;
    }

    public get kinderAusAnderenGemeindenZahlenAnderenTarif(): boolean {
        return this._kinderAusAnderenGemeindenZahlenAnderenTarif;
    }

    public set kinderAusAnderenGemeindenZahlenAnderenTarif(value: boolean) {
        this._kinderAusAnderenGemeindenZahlenAnderenTarif = value;
    }

    public get bemerkungenTarifsystem(): string {
        return this._bemerkungenTarifsystem;
    }

    public set bemerkungenTarifsystem(value: string) {
        this._bemerkungenTarifsystem = value;
    }

    public get status(): TSFerienbetreuungFormularStatus {
        return this._status;
    }

    public set status(value: TSFerienbetreuungFormularStatus) {
        this._status = value;
    }
}
