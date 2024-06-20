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

import * as moment from 'moment';
import {TSVeranlagungsstand} from './TSVeranlagungsstand';

export class TSSteuerdatenResponse {
    private _zpvNrAntragsteller: number;

    private _geburtsdatumAntragsteller: moment.Moment;

    private _kiBonAntragID: string;

    private _beginnGesuchsperiode: number;

    private _zpvNrDossiertraeger: number;

    private _geburtsdatumDossiertraeger: moment.Moment;

    private _zpvNrPartner: number;

    private _geburtsdatumPartner: moment.Moment;

    private _fallId: number;

    private _antwortdatum: moment.Moment;

    private _synchroneAntwort: boolean;

    private _veranlagungsstand: TSVeranlagungsstand;

    private _unterjaehrigerFall: boolean;

    private _erwerbseinkommenUnselbstaendigkeitDossiertraeger: number;

    private _erwerbseinkommenUnselbstaendigkeitPartner: number;

    private _steuerpflichtigesErsatzeinkommenDossiertraeger: number;

    private _steuerpflichtigesErsatzeinkommenPartner: number;

    private _erhalteneUnterhaltsbeitraegeDossiertraeger: number;

    private _erhalteneUnterhaltsbeitraegePartner: number;

    private _ausgewiesenerGeschaeftsertragDossiertraeger: number;

    private _ausgewiesenerGeschaeftsertragPartner: number;

    private _ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger: number;

    private _ausgewiesenerGeschaeftsertragVorperiodePartner: number;

    private _ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger: number;

    private _ausgewiesenerGeschaeftsertragVorperiode2Partner: number;

    private _weitereSteuerbareEinkuenfteDossiertraeger: number;

    private _weitereSteuerbareEinkuenftePartner: number;

    private _bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme: number;

    private _bruttoertraegeAusLiegenschaften: number;

    private _nettoertraegeAusEgmeDossiertraeger: number;

    private _nettoertraegeAusEgmePartner: number;

    private _geleisteteUnterhaltsbeitraege: number;

    private _schuldzinsen: number;

    private _gewinnungskostenBeweglichesVermoegen: number;

    private _liegenschaftsAbzuege: number;

    private _nettovermoegen: number;

    public get zpvNrAntragsteller(): number {
        return this._zpvNrAntragsteller;
    }

    public set zpvNrAntragsteller(value: number) {
        this._zpvNrAntragsteller = value;
    }

    public get geburtsdatumAntragsteller(): moment.Moment {
        return this._geburtsdatumAntragsteller;
    }

    public set geburtsdatumAntragsteller(value: moment.Moment) {
        this._geburtsdatumAntragsteller = value;
    }

    public get kiBonAntragID(): string {
        return this._kiBonAntragID;
    }

    public set kiBonAntragID(value: string) {
        this._kiBonAntragID = value;
    }

    public get beginnGesuchsperiode(): number {
        return this._beginnGesuchsperiode;
    }

    public set beginnGesuchsperiode(value: number) {
        this._beginnGesuchsperiode = value;
    }

    public get zpvNrDossiertraeger(): number {
        return this._zpvNrDossiertraeger;
    }

    public set zpvNrDossiertraeger(value: number) {
        this._zpvNrDossiertraeger = value;
    }

    public get geburtsdatumDossiertraeger(): moment.Moment {
        return this._geburtsdatumDossiertraeger;
    }

    public set geburtsdatumDossiertraeger(value: moment.Moment) {
        this._geburtsdatumDossiertraeger = value;
    }

    public get zpvNrPartner(): number {
        return this._zpvNrPartner;
    }

    public set zpvNrPartner(value: number) {
        this._zpvNrPartner = value;
    }

    public get geburtsdatumPartner(): moment.Moment {
        return this._geburtsdatumPartner;
    }

    public set geburtsdatumPartner(value: moment.Moment) {
        this._geburtsdatumPartner = value;
    }

    public get fallId(): number {
        return this._fallId;
    }

    public set fallId(value: number) {
        this._fallId = value;
    }

    public get antwortdatum(): moment.Moment {
        return this._antwortdatum;
    }

    public set antwortdatum(value: moment.Moment) {
        this._antwortdatum = value;
    }

    public get synchroneAntwort(): boolean {
        return this._synchroneAntwort;
    }

    public set synchroneAntwort(value: boolean) {
        this._synchroneAntwort = value;
    }

    public get veranlagungsstand(): TSVeranlagungsstand {
        return this._veranlagungsstand;
    }

    public set veranlagungsstand(value: TSVeranlagungsstand) {
        this._veranlagungsstand = value;
    }

    public get unterjaehrigerFall(): boolean {
        return this._unterjaehrigerFall;
    }

    public set unterjaehrigerFall(value: boolean) {
        this._unterjaehrigerFall = value;
    }

    public get erwerbseinkommenUnselbstaendigkeitDossiertraeger(): number {
        return this._erwerbseinkommenUnselbstaendigkeitDossiertraeger;
    }

    public set erwerbseinkommenUnselbstaendigkeitDossiertraeger(value: number) {
        this._erwerbseinkommenUnselbstaendigkeitDossiertraeger = value;
    }

    public get erwerbseinkommenUnselbstaendigkeitPartner(): number {
        return this._erwerbseinkommenUnselbstaendigkeitPartner;
    }

    public set erwerbseinkommenUnselbstaendigkeitPartner(value: number) {
        this._erwerbseinkommenUnselbstaendigkeitPartner = value;
    }

    public get steuerpflichtigesErsatzeinkommenDossiertraeger(): number {
        return this._steuerpflichtigesErsatzeinkommenDossiertraeger;
    }

    public set steuerpflichtigesErsatzeinkommenDossiertraeger(value: number) {
        this._steuerpflichtigesErsatzeinkommenDossiertraeger = value;
    }

    public get steuerpflichtigesErsatzeinkommenPartner(): number {
        return this._steuerpflichtigesErsatzeinkommenPartner;
    }

    public set steuerpflichtigesErsatzeinkommenPartner(value: number) {
        this._steuerpflichtigesErsatzeinkommenPartner = value;
    }

    public get erhalteneUnterhaltsbeitraegeDossiertraeger(): number {
        return this._erhalteneUnterhaltsbeitraegeDossiertraeger;
    }

    public set erhalteneUnterhaltsbeitraegeDossiertraeger(value: number) {
        this._erhalteneUnterhaltsbeitraegeDossiertraeger = value;
    }

    public get erhalteneUnterhaltsbeitraegePartner(): number {
        return this._erhalteneUnterhaltsbeitraegePartner;
    }

    public set erhalteneUnterhaltsbeitraegePartner(value: number) {
        this._erhalteneUnterhaltsbeitraegePartner = value;
    }

    public get ausgewiesenerGeschaeftsertragDossiertraeger(): number {
        return this._ausgewiesenerGeschaeftsertragDossiertraeger;
    }

    public set ausgewiesenerGeschaeftsertragDossiertraeger(value: number) {
        this._ausgewiesenerGeschaeftsertragDossiertraeger = value;
    }

    public get ausgewiesenerGeschaeftsertragPartner(): number {
        return this._ausgewiesenerGeschaeftsertragPartner;
    }

    public set ausgewiesenerGeschaeftsertragPartner(value: number) {
        this._ausgewiesenerGeschaeftsertragPartner = value;
    }

    public get ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger(): number {
        return this._ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
    }

    public set ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger(
        value: number
    ) {
        this._ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger = value;
    }

    public get ausgewiesenerGeschaeftsertragVorperiodePartner(): number {
        return this._ausgewiesenerGeschaeftsertragVorperiodePartner;
    }

    public set ausgewiesenerGeschaeftsertragVorperiodePartner(value: number) {
        this._ausgewiesenerGeschaeftsertragVorperiodePartner = value;
    }

    public get ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(): number {
        return this._ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
    }

    public set ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(
        value: number
    ) {
        this._ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger = value;
    }

    public get ausgewiesenerGeschaeftsertragVorperiode2Partner(): number {
        return this._ausgewiesenerGeschaeftsertragVorperiode2Partner;
    }

    public set ausgewiesenerGeschaeftsertragVorperiode2Partner(value: number) {
        this._ausgewiesenerGeschaeftsertragVorperiode2Partner = value;
    }

    public get weitereSteuerbareEinkuenfteDossiertraeger(): number {
        return this._weitereSteuerbareEinkuenfteDossiertraeger;
    }

    public set weitereSteuerbareEinkuenfteDossiertraeger(value: number) {
        this._weitereSteuerbareEinkuenfteDossiertraeger = value;
    }

    public get weitereSteuerbareEinkuenftePartner(): number {
        return this._weitereSteuerbareEinkuenftePartner;
    }

    public set weitereSteuerbareEinkuenftePartner(value: number) {
        this._weitereSteuerbareEinkuenftePartner = value;
    }

    public get bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme(): number {
        return this._bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme;
    }

    public set bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme(
        value: number
    ) {
        this._bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme = value;
    }

    public get bruttoertraegeAusLiegenschaften(): number {
        return this._bruttoertraegeAusLiegenschaften;
    }

    public set bruttoertraegeAusLiegenschaften(value: number) {
        this._bruttoertraegeAusLiegenschaften = value;
    }

    public get nettoertraegeAusEgmeDossiertraeger(): number {
        return this._nettoertraegeAusEgmeDossiertraeger;
    }

    public set nettoertraegeAusEgmeDossiertraeger(value: number) {
        this._nettoertraegeAusEgmeDossiertraeger = value;
    }

    public get nettoertraegeAusEgmePartner(): number {
        return this._nettoertraegeAusEgmePartner;
    }

    public set nettoertraegeAusEgmePartner(value: number) {
        this._nettoertraegeAusEgmePartner = value;
    }

    public get geleisteteUnterhaltsbeitraege(): number {
        return this._geleisteteUnterhaltsbeitraege;
    }

    public set geleisteteUnterhaltsbeitraege(value: number) {
        this._geleisteteUnterhaltsbeitraege = value;
    }

    public get schuldzinsen(): number {
        return this._schuldzinsen;
    }

    public set schuldzinsen(value: number) {
        this._schuldzinsen = value;
    }

    public get gewinnungskostenBeweglichesVermoegen(): number {
        return this._gewinnungskostenBeweglichesVermoegen;
    }

    public set gewinnungskostenBeweglichesVermoegen(value: number) {
        this._gewinnungskostenBeweglichesVermoegen = value;
    }

    public get liegenschaftsAbzuege(): number {
        return this._liegenschaftsAbzuege;
    }

    public set liegenschaftsAbzuege(value: number) {
        this._liegenschaftsAbzuege = value;
    }

    public get nettovermoegen(): number {
        return this._nettovermoegen;
    }

    public set nettovermoegen(value: number) {
        this._nettovermoegen = value;
    }
}
