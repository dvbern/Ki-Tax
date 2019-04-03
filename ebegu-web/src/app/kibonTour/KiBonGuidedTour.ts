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
import {LogFactory} from '../core/logging/LogFactory';

const LOG = LogFactory.createLog('KiBonGuidedTour');

export class GemeindeGuidedTour implements GuidedTour {

    public tourId: string = 'GemeindeGuidedTour';
    public steps: TourStep[] = [

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_1_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_1_CONTENT'),
            '[class~="dv-helpmenu-question"]', Orientation.BottomRight, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_2_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_2_CONTENT'),
            'a[uisref="pendenzen.list-view"]', Orientation.BottomLeft, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_3_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_3_CONTENT'),
            'a[uisref="faelle.list"]', Orientation.BottomLeft, this.state, 'faelle.list'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_4_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_4_CONTENT'),
            'a[uisref="zahlungsauftrag.view"]', Orientation.BottomLeft, this.state, 'zahlungsauftrag.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_5_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_5_CONTENT'),
            'a[uisref="statistik.view"]', Orientation.BottomLeft, this.state, 'statistik.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_6_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_6_CONTENT'),
            'dv-posteingang[uisref="posteingang.view"]', Orientation.BottomLeft, this.state, 'posteingang.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_7_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_7_CONTENT'),
            '[class~="dv-ng-navbar-element-fall-eroeffnen"]', Orientation.Left),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_SEARCH_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_SEARCH_CONTENT'),
            'input[type="search"]', Orientation.BottomLeft),
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

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_HELP_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_HELP_CONTENT'),
            '[class~="dv-helpmenu-question"]', Orientation.BottomRight, this.state, 'pendenzenBetreuungen.list-view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_CONTENT'),
            'a[uisref="pendenzenBetreuungen.list-view"]', Orientation.BottomLeft, this.state, 'pendenzenBetreuungen.list-view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_3_CONTENT'),
            'a[uisref="faelle.list"]', Orientation.BottomLeft, this.state, 'faelle.list'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_CONTENT'),
            'a[uisref="zahlungsauftrag.view"]', Orientation.BottomLeft, this.state, 'zahlungsauftrag.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_CONTENT'),
            'a[uisref="statistik.view"]', Orientation.BottomLeft, this.state, 'statistik.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_CONTENT'),
            'input[type="search"]', Orientation.BottomLeft),

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

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_HELP_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_HELP_CONTENT'),
            '[class~="dv-helpmenu-question"]', Orientation.BottomRight, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_ADMIN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_ADMIN_CONTENT'),
            '[class~="user-menu"]', Orientation.BottomLeft, this.state, 'posteingang.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_PENDENZEN_CONTENT'),
            'a[uisref="pendenzenBetreuungen.list-view"]', Orientation.BottomLeft, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_ALLEFAELLE_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_3_CONTENT'),
            'a[uisref="faelle.list"]', Orientation.BottomLeft, this.state, 'faelle.list'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_ZAHLUNGEN_CONTENT'),
            'a[uisref="zahlungsauftrag.view"]', Orientation.BottomLeft, this.state, 'zahlungsauftrag.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_STATISTIKEN_CONTENT'),
            'a[uisref="statistik.view"]', Orientation.BottomLeft, this.state, 'statistik.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_SEARCH_CONTENT'),
            'input[type="search"]', Orientation.BottomLeft),

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

    public constructor(title: string, content: string)
    public constructor(title: string, content: string, selector: string, orientation: Orientation)
    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService,
                       navigateToOpen: string)
    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService,
                       navigateToOpen: string, navigateToClose: string)
    public constructor(title: string, content: string, selector?: string, orientation?: Orientation,
                       state?: StateService, navigateToOpen?: string, navigateToClose?: string) {
        this.title = title;
        this.content = content;
        this.selector = selector;
        this.orientation = orientation;
        // tslint:disable-next-line:early-exit
        if (state !== undefined && state !== null) {

            if (navigateToOpen !== undefined && navigateToOpen !== null) {
                this.action = () => {
                    LOG.info('Current state: ' + state.current.name);
                    try {
                        if (state.current.name !== navigateToOpen) {
                            LOG.info('Navigating to state: ' + navigateToOpen);
                            state.go(navigateToOpen);
                        } else {
                            // state.reload();
                        }
                    } catch (e) {
                        LOG.error(e);
                    }
                };
            }

            if (navigateToClose !== undefined && navigateToClose !== null) {
                this.closeAction = () => {
                    LOG.info('Current state: ' + state.current.name);
                    try {
                        if (state.current.name !== navigateToClose) {
                            LOG.info('Navigating to state: ' + navigateToClose);
                            state.go(navigateToClose);
                        } else {
                            // state.reload();
                        }
                    } catch (e) {
                        LOG.error(e);
                    }
                };
            }
        }
    }
}
