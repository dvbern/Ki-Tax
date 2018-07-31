/*
 * Copyright 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {NgModule} from '@angular/core';
import {UIRouterModule} from '@uirouter/angular';
import {AppComponent} from './test/app.component';

@NgModule({
    imports: [
        // UIRouterUpgradeModule,
        UIRouterModule.forRoot({states: [{name: 'app', url: '/foo', component: AppComponent}]}),
    ],
    exports: [UIRouterModule],
})
export class AppRoutingModule {
}
