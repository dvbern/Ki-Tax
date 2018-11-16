/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

import {RedirectToResult, TargetState, Transition} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSEinladungTyp} from '../../../models/enums/TSEinladungTyp';
import TSBenutzer from '../../../models/TSBenutzer';
import {getRoleBasedTargetState} from '../../../utils/AuthenticationUtil';

export function getEntityTargetState(transition: Transition, principal: TSBenutzer): TargetState {
    const stateService = transition.router.stateService;

    const params = transition.params();
    const entityId: string = params.entityid;
    const typ: TSEinladungTyp = params.typ;

    switch (typ) {
        case TSEinladungTyp.MITARBEITER:
            return getRoleBasedTargetState(principal.getCurrentRole(), stateService);
        case TSEinladungTyp.GEMEINDE:
            return stateService.target('gemeinde.edit', {gemeindeId: entityId});
        case TSEinladungTyp.TRAEGERSCHAFT:
            return stateService.target('pendenzenBetreuungen.list-view', {traegerschaftId: entityId});
        case TSEinladungTyp.INSTITUTION:
            return stateService.target('admin.institution', {institutionId: entityId});
        default:
            throw new Error(`unrecognised EinladungTyp ${typ}`);
    }
}

export function handleLoggedInUser(transition: Transition): Promise<RedirectToResult> {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');
    const stateService = transition.router.stateService;

    return authService.principal$
        .pipe(
            take(1),
            map(principal => {
                    if (!principal) {
                        return stateService.target('einladung.logininfo', transition.params(), transition.options());
                    }

                    // we are logged: redirect to the new entity
                    return getEntityTargetState(transition, principal);
                },
            ),
        )
        .toPromise();
}
