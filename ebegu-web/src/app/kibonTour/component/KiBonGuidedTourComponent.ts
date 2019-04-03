/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {ChangeDetectorRef, Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {GuidedTourComponent, GuidedTourService} from 'ngx-guided-tour';

@Component({
    selector: 'kibon-guided-tour',
    templateUrl: './kibon-guided-tour.component.html',
    styleUrls: ['./kibon-guided-tour.component.scss'],
})
export class KiBonGuidedTourComponent extends GuidedTourComponent {

    public tourStepWidth = 500;
    // tslint:disable
    public constructor(private readonly translate: TranslateService,
                       public readonly guidedTourService: GuidedTourService,
                       private readonly changeDetectorRef: ChangeDetectorRef) {
        super(guidedTourService);
    }

    // tslint:enable
    public updateStepLocation(): void {
        super.updateStepLocation();
        this.changeDetectorRef.markForCheck();
    }
}

/*
Copyright 2016 Google Inc. All Rights Reserved.
Use of this source code is governed by an MIT-style license that
can be found in the LICENSE file at http://angular.io/license
*/
