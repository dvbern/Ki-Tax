/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../enums/TSRole';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSBenutzerNoDetails} from '../TSBenutzerNoDetails';
import {TSGemeinde} from '../TSGemeinde';
import {TSGesuchsperiode} from '../TSGesuchsperiode';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from './TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from './TSLastenausgleichTagesschuleAngabenInstitutionContainer';

export class TSLastenausgleichTagesschuleAngabenGemeindeContainer extends TSAbstractEntity {
    public status: TSLastenausgleichTagesschuleAngabenGemeindeStatus;
    public zurueckAnGemeinde: boolean;
    public gemeinde: TSGemeinde;
    public gesuchsperiode: TSGesuchsperiode;
    public alleAngabenInKibonErfasst: boolean;
    public internerKommentar: string;
    public angabenDeklaration: TSLastenausgleichTagesschuleAngabenGemeinde;
    public angabenKorrektur: TSLastenausgleichTagesschuleAngabenGemeinde;
    public angabenInstitutionContainers: Array<TSLastenausgleichTagesschuleAngabenInstitutionContainer>;
    public verantwortlicher: TSBenutzerNoDetails;
    private _betreuungsstundenPrognose: number;
    private _bemerkungenBetreuungsstundenPrognose: string;
    /**
     * Based on AngabenGemeindeStatus, we work with AngabenDeklaration or AngabenKorrektur
     */
    public getAngabenToWorkWith(): TSLastenausgleichTagesschuleAngabenGemeinde {
        if (this.isAtLeastInBearbeitungKantonOrZurueckgegeben()) {
            return this.angabenKorrektur;
        }
        return this.angabenDeklaration;
    }

    public isInBearbeitungGemeinde(): boolean {
        return [
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE
        ].includes(this.status);
    }

    public isInBearbeitungKanton(): boolean {
        return [
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG
        ].includes(this.status);
    }

    public isAtLeastInBearbeitungKanton(): boolean {
        return ![
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE
        ].includes(this.status);
    }

    public isAtLeastInBearbeitungKantonOrZurueckgegeben(): boolean {
        return this.isAtLeastInBearbeitungKanton()
            || this.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE;
    }

    public isGemeindeFormularInBearbeitungForRole(role: TSRole): boolean {
        switch (role) {
            case TSRole.SUPER_ADMIN:
                return (this.isInBearbeitungKanton() && this.getAngabenToWorkWith()?.isInBearbeitung()) ||
                    (this.isInBearbeitungGemeinde() && this.getAngabenToWorkWith()?.isInBearbeitung());
            case TSRole.SACHBEARBEITER_MANDANT:
            case TSRole.ADMIN_MANDANT:
                return this.isInBearbeitungKanton() && this.getAngabenToWorkWith()?.isInBearbeitung();
            case TSRole.ADMIN_TS:
            case TSRole.SACHBEARBEITER_TS:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
                return this.isInBearbeitungGemeinde() && this.getAngabenToWorkWith()?.isInBearbeitung();
            default:
                return false;
        }
    }

    public allAngabenInstitutionContainersGeprueft(): boolean {
        return this.angabenInstitutionContainers.reduce((
            prev: boolean,
            cur: TSLastenausgleichTagesschuleAngabenInstitutionContainer
        ) => prev && cur.isGeprueftGemeinde(), true);
    }

    public isAtLeastGeprueft(): boolean {
        return [
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.ABGESCHLOSSEN
        ].includes(this.status);
    }

    public isinPruefungKanton(): boolean {
        return [
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON,
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG
        ].includes(this.status);
    }

    public isInZweitPruefung(): boolean {
        return this.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG;
    }

    public isGeprueft(): boolean {
        return this.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT;
    }

    public get betreuungsstundenPrognose(): number {
        return this._betreuungsstundenPrognose;
    }

    public set betreuungsstundenPrognose(value: number) {
        this._betreuungsstundenPrognose = value;
    }

    public get bemerkungenBetreuungsstundenPrognose(): string {
        return this._bemerkungenBetreuungsstundenPrognose;
    }

    public set bemerkungenBetreuungsstundenPrognose(value: string) {
        this._bemerkungenBetreuungsstundenPrognose = value;
    }

    public isAbgeschlossen(): boolean {
        return this.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.ABGESCHLOSSEN;
    }
}
