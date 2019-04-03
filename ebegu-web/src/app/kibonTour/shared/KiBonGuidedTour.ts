/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {GuidedTour, Orientation, OrientationConfiguration, TourStep} from 'ngx-guided-tour';

const SELECTOR_HELP_ICON = '[class~="dv-helpmenu-question"]';
const SELECTOR_PENDENZEN_LIST = 'a[uisref="pendenzen.list-view"]';
const SELECTOR_PENDENZEN_BETREUUNGEN_LIST = 'a[uisref="pendenzenBetreuungen.list-view"]';
const SELECTOR_FAELLE_LIST = 'a[uisref="faelle.list"]';
const SELECTOR_ZAHLUNG = 'a[uisref="zahlungsauftrag.view"]';
const SELECTOR_STATISTIK = 'a[uisref="statistik.view"]';
const SELECTOR_POST = 'dv-posteingang[uisref="posteingang.view"]';
const SELECTOR_CREATE_FALL = '[class~="dv-ng-navbar-element-fall-eroeffnen"]';
const SELECTOR_SEARCH = 'input[type="search"]';
const SELECTOR_USERMENU = '[class~="md-menu"]';

const ROUTE_PENDENZEN_LIST = 'pendenzen.list-view';
const ROUTE_PENDENZEN_BETREUUNGEN_LIST = 'pendenzenBetreuungen.list-view';
const ROUTE_FAELLE_LIST = 'faelle.list';
const ROUTE_ZAHLUNG = 'zahlungsauftrag.view';
const ROUTE_STATISTIK = 'statistik.view';
const ROUTE_POST = 'posteingang.view';

export class GemeindeGuidedTour implements GuidedTour {

    public tourId: string = 'GemeindeGuidedTour';
    public steps: TourStep[] = [

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_1_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_1_CONTENT'),
            SELECTOR_HELP_ICON, Orientation.BottomRight),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_2_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_2_CONTENT'),
            SELECTOR_PENDENZEN_LIST, Orientation.Bottom, this.state, ROUTE_PENDENZEN_LIST),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_3_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_3_CONTENT'),
            SELECTOR_FAELLE_LIST, Orientation.Bottom, this.state, ROUTE_FAELLE_LIST),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_4_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_4_CONTENT'),
            SELECTOR_ZAHLUNG, Orientation.Bottom, this.state, ROUTE_ZAHLUNG),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_5_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_5_CONTENT'),
            SELECTOR_STATISTIK, Orientation.Bottom, this.state, ROUTE_STATISTIK),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_6_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_6_CONTENT'),
            SELECTOR_POST, Orientation.Bottom, this.state, ROUTE_POST),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_7_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_7_CONTENT'),
            SELECTOR_CREATE_FALL, Orientation.Left),

        new KiBonTourStep(
            this.translate.instant('GEMEINDE_TOUR_STEP_SEARCH_TITLE'),
            this.translate.instant('GEMEINDE_TOUR_STEP_SEARCH_CONTENT'),
            SELECTOR_SEARCH, Orientation.BottomLeft),
    ];

    public useOrb: boolean = false;
    public skipCallback: (stepSkippedOn: number) => void;
    public completeCallback: () => void;
    public minimumScreenSize: number = 0;
    public preventBackdropFromAdvancing: boolean = false;

    public constructor(private readonly state: StateService,
                       private readonly translate: TranslateService) {
    }

}

export class InstitutionGuidedTour implements GuidedTour {

    public tourId: string = 'InstitutionGuidedTour';
    public steps: TourStep[] = [

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_HELP_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_HELP_CONTENT'),
            SELECTOR_HELP_ICON, Orientation.BottomRight),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_CONTENT'),
            SELECTOR_PENDENZEN_BETREUUNGEN_LIST, Orientation.BottomLeft, this.state, ROUTE_PENDENZEN_BETREUUNGEN_LIST),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_CONTENT'),
            SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_CONTENT'),
            SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_CONTENT'),
            SELECTOR_STATISTIK, Orientation.BottomLeft, this.state, ROUTE_STATISTIK),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_CONTENT'),
            SELECTOR_SEARCH, Orientation.BottomLeft),

    ];

    public useOrb: boolean = false;
    public skipCallback: (stepSkippedOn: number) => void;
    public completeCallback: () => void;
    public minimumScreenSize: number = 0;
    public preventBackdropFromAdvancing: boolean = false;

    public constructor(private readonly state: StateService,
                       private readonly translate: TranslateService) {
    }

}

export class AdminInstitutionGuidedTour implements GuidedTour {

    public tourId: string = 'InstitutionGuidedTour';
    public steps: TourStep[] = [

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_HELP_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_HELP_CONTENT'),
            SELECTOR_HELP_ICON, Orientation.BottomRight),

        new KiBonTourStep(
             this.translate.instant('INSTITUTION_TOUR_STEP_ADMIN_TITLE'),
             this.translate.instant('INSTITUTION_TOUR_STEP_ADMIN_CONTENT'),
             SELECTOR_USERMENU, Orientation.BottomRight),

        // todo medu check, i think admin_institution should see   ROUTE_PENDENZEN_BETREUUNGEN_LIST instead of ROUTE_PENDENZEN_LIST
        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_CONTENT'),
            SELECTOR_PENDENZEN_BETREUUNGEN_LIST, Orientation.BottomLeft, this.state, ROUTE_PENDENZEN_BETREUUNGEN_LIST),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_CONTENT'),
            SELECTOR_FAELLE_LIST, Orientation.BottomLeft, this.state, ROUTE_FAELLE_LIST),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_CONTENT'),
            SELECTOR_ZAHLUNG, Orientation.BottomLeft, this.state, ROUTE_ZAHLUNG),

        new KiBonTourStep(
            this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_TITLE'),
            this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_CONTENT'),
            SELECTOR_STATISTIK, Orientation.BottomLeft, this.state, ROUTE_STATISTIK)

    ];

    public useOrb: boolean = false;
    public skipCallback: (stepSkippedOn: number) => void;
    public completeCallback: () => void;
    public minimumScreenSize: number = 0;
    public preventBackdropFromAdvancing: boolean = false;

    public constructor(private readonly state: StateService,
                       private readonly translate: TranslateService) {
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

    // @ts-ignore
    public constructor(title: string, content: string)
    public constructor(title: string, content: string, selector: string, orientation: Orientation)
    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService,
                       navigateToOpen: string)
    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService,
                       navigateToOpen: string) {
        this.title = title;
        this.content = content;
        this.selector = selector;
        this.orientation = orientation;
        // tslint:disable-next-line:early-exit
        if (state !== undefined && state !== null) {
            this.closeAction = () => {
                console.log('closeaction: ' + new Date().toLocaleString());
            };
            this.action = () => {
                console.log('action: ' + new Date().toLocaleString());
                console.log(this.selector);
                console.log(navigateToOpen);
                if (navigateToOpen) {
                    state.go(navigateToOpen);
                }
            };

        }
    }
}
