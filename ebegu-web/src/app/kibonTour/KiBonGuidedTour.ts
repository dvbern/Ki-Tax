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
        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_START_TITLE'), this.translate.instant('GEMEINDE_TOUR_START_CONTENT'),
            '', Orientation.Center, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_1_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_1_CONTENT'),
            'a[uisref="pendenzen.list-view"]', Orientation.BottomLeft , this.state, 'faelle.list'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_2_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_2_CONTENT'),
            'a[uisref="faelle.list"]', Orientation.BottomLeft, this.state, 'zahlungsauftrag.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_3_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_3_CONTENT'),
            'a[uisref="zahlungsauftrag.view"]', Orientation.BottomLeft, this.state, 'statistik.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_4_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_4_CONTENT'),
            'a[uisref="statistik.view"]', Orientation.BottomLeft, this.state, 'posteingang.view'),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_5_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_5_CONTENT'),
            'dv-posteingang[uisref="posteingang.view"]', Orientation.BottomLeft),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_STEP_6_TITLE'), this.translate.instant('GEMEINDE_TOUR_STEP_6_CONTENT'),
            '[class~="dv-ng-navbar-element-fall-eroeffnen"]', Orientation.Left),

        new KiBonTourStep(this.translate.instant('GEMEINDE_TOUR_END_TITLE'), this.translate.instant('GEMEINDE_TOUR_END_CONTENT'),
            '[class~="dv-helpmenu-question"]', Orientation.BottomRight)];

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
        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_START_TITLE'), this.translate.instant('INSTITUTION_TOUR_START_CONTENT'),
            '', Orientation.Center, this.state, 'pendenzen.list-view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_1_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_1_CONTENT'),
            'a[uisref="pendenzen.list-view"]', Orientation.Bottom, this.state, 'faelle.list'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_2_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_2_CONTENT'),
            'a[uisref="faelle.list"]', Orientation.Bottom, this.state, 'zahlungsauftrag.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_3_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_3_CONTENT'),
            'a[uisref="zahlungsauftrag.view"]', Orientation.Bottom, this.state, 'statistik.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_4_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_4_CONTENT'),
            'a[uisref="statistik.view"]', Orientation.Bottom, this.state, 'posteingang.view'),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_5_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_5_CONTENT'),
            'dv-posteingang[uisref="posteingang.view"]', Orientation.Bottom),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_STEP_6_TITLE'), this.translate.instant('INSTITUTION_TOUR_STEP_6_CONTENT'),
            '[class~="dv-ng-navbar-element-fall-eroeffnen"]', Orientation.Left),

        new KiBonTourStep(this.translate.instant('INSTITUTION_TOUR_END_TITLE'), this.translate.instant('INSTITUTION_TOUR_END_CONTENT'),
            '[class~="dv-helpmenu-question"]', Orientation.Left)];

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
    public constructor(title: string, content: string, selector: string, orientation: Orientation, state: StateService, navigateTo: string)
    public constructor(title: string, content: string, selector?: string, orientation?: Orientation, state?: StateService, navigateTo?: string) {
        this.title = title;
        this.content = content;
        this.selector = selector;
        this.orientation = orientation;
        // tslint:disable-next-line:early-exit
        if (state !== undefined && state !== null) {
            this.closeAction = () => {
                LOG.info('Navigating to ' + navigateTo);
                try {
                    state.go(navigateTo);
                } catch (e) {
                    LOG.error(e);
                }
            };
        }
    }
}
