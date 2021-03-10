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

import {TSAbstractEntity} from '../TSAbstractEntity';

export class TSFerienbetreuungAngabenNutzung extends TSAbstractEntity {

    private _anzahlBetreuungstageKinderBern: number;
    private _betreuungstageKinderDieserGemeinde: number;
    private _betreuungstageKinderDieserGemeindeSonderschueler: number;
    private _davonBetreuungstageKinderAndererGemeinden: number;
    private _davonBetreuungstageKinderAndererGemeindenSonderschueler: number;
    private _anzahlBetreuteKinder: number;
    private _anzahlBetreuteKinderSonderschueler: number;
    private _anzahlBetreuteKinder1Zyklus: number;
    private _anzahlBetreuteKinder2Zyklus: number;
    private _anzahlBetreuteKinder3Zyklus: number;

    public get anzahlBetreuungstageKinderBern(): number {
        return this._anzahlBetreuungstageKinderBern;
    }

    public set anzahlBetreuungstageKinderBern(value: number) {
        this._anzahlBetreuungstageKinderBern = value;
    }

    public get betreuungstageKinderDieserGemeinde(): number {
        return this._betreuungstageKinderDieserGemeinde;
    }

    public set betreuungstageKinderDieserGemeinde(value: number) {
        this._betreuungstageKinderDieserGemeinde = value;
    }

    public get betreuungstageKinderDieserGemeindeSonderschueler(): number {
        return this._betreuungstageKinderDieserGemeindeSonderschueler;
    }

    public set betreuungstageKinderDieserGemeindeSonderschueler(value: number) {
        this._betreuungstageKinderDieserGemeindeSonderschueler = value;
    }

    public get davonBetreuungstageKinderAndererGemeinden(): number {
        return this._davonBetreuungstageKinderAndererGemeinden;
    }

    public set davonBetreuungstageKinderAndererGemeinden(value: number) {
        this._davonBetreuungstageKinderAndererGemeinden = value;
    }

    public get davonBetreuungstageKinderAndererGemeindenSonderschueler(): number {
        return this._davonBetreuungstageKinderAndererGemeindenSonderschueler;
    }

    public set davonBetreuungstageKinderAndererGemeindenSonderschueler(value: number) {
        this._davonBetreuungstageKinderAndererGemeindenSonderschueler = value;
    }

    public get anzahlBetreuteKinder(): number {
        return this._anzahlBetreuteKinder;
    }

    public set anzahlBetreuteKinder(value: number) {
        this._anzahlBetreuteKinder = value;
    }

    public get anzahlBetreuteKinderSonderschueler(): number {
        return this._anzahlBetreuteKinderSonderschueler;
    }

    public set anzahlBetreuteKinderSonderschueler(value: number) {
        this._anzahlBetreuteKinderSonderschueler = value;
    }

    public get anzahlBetreuteKinder1Zyklus(): number {
        return this._anzahlBetreuteKinder1Zyklus;
    }

    public set anzahlBetreuteKinder1Zyklus(value: number) {
        this._anzahlBetreuteKinder1Zyklus = value;
    }

    public get anzahlBetreuteKinder2Zyklus(): number {
        return this._anzahlBetreuteKinder2Zyklus;
    }

    public set anzahlBetreuteKinder2Zyklus(value: number) {
        this._anzahlBetreuteKinder2Zyklus = value;
    }

    public get anzahlBetreuteKinder3Zyklus(): number {
        return this._anzahlBetreuteKinder3Zyklus;
    }

    public set anzahlBetreuteKinder3Zyklus(value: number) {
        this._anzahlBetreuteKinder3Zyklus = value;
    }
}
