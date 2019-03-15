/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {StateService} from '@uirouter/core';
import {GuidedTour, Orientation, OrientationConfiguration, TourStep} from 'ngx-guided-tour';
import {LogFactory} from '../core/logging/LogFactory';

const LOG = LogFactory.createLog('KiBonGuidedTour');

export class KiBonGuidedTour implements GuidedTour {
    public tourId: string;
    public steps: TourStep[];
    public useOrb: boolean;
    public skipCallback: (stepSkippedOn: number) => void;
    public completeCallback: () => void;
    public minimumScreenSize: number;
    public preventBackdropFromAdvancing: boolean;

    public constructor(tourId: string, steps: TourStep[], useOrb: boolean) {
        this.tourId = tourId;
        this.steps = steps;
        this.useOrb = useOrb;
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
                state.go(navigateTo);
            };
        }
    }
}
