/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {GuidedTour, Orientation, OrientationConfiguration, TourStep} from 'ngx-guided-tour';
import {TSRole} from '../../../models/enums/TSRole';

const SELECTOR_HELP_ICON = 'dv-helpmenu';
const SELECTOR_PENDENZEN_LIST = 'a[uisref="pendenzen.list-view"]';
const SELECTOR_PENDENZEN_BETREUUNGEN_LIST = 'a[uisref="pendenzenBetreuungen.list-view"]';
const SELECTOR_PENDENZEN_STEUERAMT_LIST = 'a[uisref="pendenzenSteueramt.list-view"]';
const SELECTOR_FAELLE_LIST = 'a[uisref="faelle.list"]';
const SELECTOR_ZAHLUNG = 'a[uisref="zahlungsauftrag.view"]';
const SELECTOR_STATISTIK = 'a[uisref="statistik.view"]';
const SELECTOR_POST = 'dv-posteingang';
const SELECTOR_SEARCH = 'dv-quicksearchbox';
const SELECTOR_USERMENU = '#tourTipUserMenu';

const ROUTE_PENDENZEN_LIST = 'pendenzen.list-view';
const ROUTE_PENDENZEN_STEUERAMT_LIST = 'pendenzenSteueramt.list-view';
const ROUTE_PENDENZEN_BETREUUNGEN_LIST = 'pendenzenBetreuungen.list-view';
const ROUTE_FAELLE_LIST = 'faelle.list';
const ROUTE_ZAHLUNG = 'zahlungsauftrag.view';
const ROUTE_STATISTIK = 'statistik.view';
const ROUTE_POST = 'posteingang.view';
export const GUIDED_TOUR_SUPPORTED_ROLES = new Set<TSRole>(
    [
        TSRole.ADMIN_TRAEGERSCHAFT,
        TSRole.ADMIN_INSTITUTION,
        TSRole.ADMIN_TS,
        TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
        TSRole.SACHBEARBEITER_INSTITUTION,
        TSRole.SUPER_ADMIN,
        TSRole.ADMIN_MANDANT,
        TSRole.SACHBEARBEITER_MANDANT,
        TSRole.ADMIN_GEMEINDE,
        TSRole.SACHBEARBEITER_GEMEINDE,
        TSRole.ADMIN_BG,
        TSRole.SACHBEARBEITER_BG,
        TSRole.JURIST,
        TSRole.REVISOR,
        TSRole.STEUERAMT,
    ]
);

export class GuidedTourByRole implements GuidedTour {

    public tourId: string = 'GemeindeGuidedTour';
    public steps: TourStep[] = [];

    public useOrb: boolean = false;
    public skipCallback: (stepSkippedOn: number) => void;
    public completeCallback: () => void;
    public minimumScreenSize: number = 0;
    public preventBackdropFromAdvancing: boolean = false;

    public constructor(private readonly state: StateService,
                       private readonly translate: TranslateService,
                       private readonly role: TSRole) {
        // Step 1: Help
        this.steps.push(new KiBonTourStep(
            this.translate.instant('TOUR_STEP_HELP_TITLE'),
            this.translate.instant('TOUR_STEP_HELP_CONTENT'),
            SELECTOR_HELP_ICON, Orientation.BottomRight, this.state, undefined));
        // Step 2: Admin
        switch (role) {
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ADMIN_INSTITUTION_TITLE'),
                    this.translate.instant('TOUR_STEP_ADMIN_INSTITUTION_CONTENT'),
                    SELECTOR_USERMENU, Orientation.BottomRight, this.state, undefined));
                break;
            case TSRole.SACHBEARBEITER_MANDANT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ADMIN_SACHBEARBEITER_MANDANT_TITLE'),
                    this.translate.instant('TOUR_STEP_ADMIN_SACHBEARBEITER_MANDANT_CONTENT'),
                    SELECTOR_USERMENU, Orientation.BottomRight, this.state, undefined));
                break;
            case TSRole.ADMIN_MANDANT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ADMIN_ADMIN_MANDANT_TITLE'),
                    this.translate.instant('TOUR_STEP_ADMIN_ADMIN_MANDANT_CONTENT'),
                    SELECTOR_USERMENU, Orientation.BottomRight, this.state, undefined));
                break;
            case TSRole.ADMIN_BG:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ADMIN_BG_TITLE'),
                    this.translate.instant('TOUR_STEP_ADMIN_BG_CONTENT'),
                    SELECTOR_USERMENU, Orientation.BottomRight, this.state, undefined));
                break;
            case TSRole.ADMIN_TS:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ADMIN_TS_TITLE'),
                    this.translate.instant('TOUR_STEP_ADMIN_TS_CONTENT'),
                    SELECTOR_USERMENU, Orientation.BottomRight, this.state, undefined));
                break;
            default:
        }
        // Step 3: Pendenzen
        switch (role) {
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_PENDENZEN_BG_TITLE'),
                    this.translate.instant('TOUR_STEP_PENDENZEN_BG_CONTENT'),
                    SELECTOR_PENDENZEN_LIST, Orientation.Bottom, this.state, ROUTE_PENDENZEN_LIST));
                break;
            case TSRole.ADMIN_TS:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_PENDENZEN_TS_TITLE'),
                    this.translate.instant('TOUR_STEP_PENDENZEN_TS_CONTENT'),
                    SELECTOR_PENDENZEN_LIST, Orientation.Bottom, this.state, ROUTE_PENDENZEN_LIST));
                break;
            case TSRole.STEUERAMT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_PENDENZEN_STEUERAMT_TITLE'),
                    this.translate.instant('TOUR_STEP_PENDENZEN_STEUERAMT_CONTENT'),
                    SELECTOR_PENDENZEN_STEUERAMT_LIST, Orientation.Bottom, this.state, ROUTE_PENDENZEN_STEUERAMT_LIST));
                break;
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_PENDENZEN_INTSTITUTION_TITLE'),
                    this.translate.instant('TOUR_STEP_PENDENZEN_INTSTITUTION_CONTENT'),
                    SELECTOR_PENDENZEN_BETREUUNGEN_LIST, Orientation.BottomLeft, this.state,
                    ROUTE_PENDENZEN_BETREUUNGEN_LIST));
                break;
            default:
        }
        // Step 3.5 Anmeldungen
        // eslint-disable-next-line
        switch (role) {
            case TSRole.ADMIN_TS:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ANMELDUNGEN_TS_TITLE'),
                    this.translate.instant('TOUR_STEP_ANMELDUNGEN_TS_CONTENT'),
                    SELECTOR_PENDENZEN_BETREUUNGEN_LIST,
                    Orientation.Bottom, this.state, ROUTE_PENDENZEN_BETREUUNGEN_LIST));
                break;
            default:
        }
        // Step 4: Alle Fälle
        switch (role) {
            case TSRole.ADMIN_BG:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.REVISOR:
            case TSRole.JURIST:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_GEMEINDEN_TITLE'),
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_GEMEINDEN_CONTENT'),
                    SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST));
                break;
            case TSRole.ADMIN_TS:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_TS_TITLE'),
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_TS_CONTENT'),
                    SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST));
                break;
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_INTSTITUTION_TITLE'),
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_INTSTITUTION_CONTENT'),
                    SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST));
                break;
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_MANDANT_TITLE'),
                    this.translate.instant('TOUR_STEP_ALLE_FAELLE_MANDANT_CONTENT'),
                    SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST));
                break;
            default:
        }
        // Step 5: Suche
        if (role !== TSRole.STEUERAMT) {
            this.steps.push(new KiBonTourStep(
                this.translate.instant('TOUR_STEP_SEARCH_TITLE'),
                this.translate.instant('TOUR_STEP_SEARCH_CONTENT'),
                SELECTOR_SEARCH, Orientation.BottomRight, this.state, undefined));
        }
        // Step 6: Zahlungen
        switch (role) {
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_MANDANT_TITLE'),
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_MANDANT_CONTENT'),
                    SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG));
                break;
            case TSRole.ADMIN_BG:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_GEMEINDE_ADMIN_TITLE'),
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_GEMEINDE_ADMIN_CONTENT'),
                    SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG));
                break;
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.REVISOR:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_GEMEINDE_TITLE'),
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_GEMEINDE_CONTENT'),
                    SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG));
                break;
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_INSTITUTION_TITLE'),
                    this.translate.instant('TOUR_STEP_ZAHLUNGEN_INTSTITUTION_CONTENT'),
                    SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG));
                break;
            default:
                break;
        }
        // Step 7: Statistiken
        switch (role) {
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_BG:
            case TSRole.ADMIN_TS:
            case TSRole.REVISOR:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_STATISTIKEN_TITLE'),
                    this.translate.instant('TOUR_STEP_STATISTIKEN_CONTENT'),
                    SELECTOR_STATISTIK, Orientation.BottomRight, this.state, ROUTE_STATISTIK));
                break;
            default:
                break;
        }
        // Step 8: Posteingang
        switch (role) {
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_BG:
            case TSRole.ADMIN_TS:
                this.steps.push(new KiBonTourStep(
                    this.translate.instant('TOUR_STEP_POSTEINGANG_TITLE'),
                    this.translate.instant('TOUR_STEP_POSTEINGANG_CONTENT'),
                    SELECTOR_POST, Orientation.Bottom, this.state, ROUTE_POST));
                break;
            default:
                break;
        }
    }
}

export class KiBonTourStep implements TourStep {
    public title: string;
    public content: string;
    public selector: string;
    public action: () => void;
    public closeAction: () => void;
    public highlightPadding: number;
    public orientation: Orientation | OrientationConfiguration[];
    public scrollAdjustment: number;
    public skipStep: boolean;
    public useHighlightPadding: boolean;

    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService,
                       navigateToOpen: string) {
        this.title = title;
        this.content = content;
        this.selector = selector;
        this.orientation = orientation;

        // eslint-disable-next-line
        if (state !== undefined && state !== null) {
            this.action = () => {
                if (navigateToOpen) {
                    state.go(navigateToOpen);
                }
            };
        }
    }
}
