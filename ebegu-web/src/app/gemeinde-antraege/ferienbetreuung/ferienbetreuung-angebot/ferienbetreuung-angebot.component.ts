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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Subject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAngebotComponent');

@Component({
    selector: 'dv-ferienbetreuung-angebot',
    templateUrl: './ferienbetreuung-angebot.component.html',
    styleUrls: ['./ferienbetreuung-angebot.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungAngebotComponent extends AbstractFerienbetreuungFormular implements OnInit {

    public formFreigebenTriggered = false;
    public bfsGemeinden: TSBfsGemeinde[];

    private angebot: TSFerienbetreuungAngabenAngebot;
    private container: TSFerienbetreuungAngabenContainer;
    private readonly WIZARD_TYPE: TSWizardStepXTyp.FERIENBETREUUNG;

    public readonly canSeeAbschliessen: Subject<boolean> = new Subject<boolean>();
    public readonly canSeeSave: Subject<boolean> = new Subject<boolean>();
    public readonly canSeeFalscheAngaben: Subject<boolean> = new Subject<boolean>();

    public constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly gemeindeRS: GemeindeRS,
        private readonly wizardRS: WizardStepXRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly authService: AuthServiceRS,
    ) {
        super(errorService, translate, dialog);
    }

    public ngOnInit(): void {
        combineLatest([
                this.ferienbetreuungService.getFerienbetreuungContainer(),
                this.authService.principal$.pipe(filter(principal => !!principal)),
            ],
        ).subscribe(([container, principal]) => {
            this.container = container;
            this.angebot = container.angabenDeklaration?.angebot;
            this.setupForm(this.angebot);

            if (this.angebot?.isGeprueft() ||
                this.angebot?.isAtLeastAbgeschlossenGemeinde() &&
                principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
                this.form.disable();
            }
            this.setupRoleBasedPropertiesForPrincipal(principal);

            this.cd.markForCheck();
        }, error => {
            LOG.error(error);
        });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
    }

    private setupRoleBasedPropertiesForPrincipal(principal: TSBenutzer): void {
        if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
            if (this.angebot.isAtLeastAbgeschlossenGemeinde()) {
                this.canSeeAbschliessen.next(false);
                this.canSeeSave.next(false);
                this.canSeeFalscheAngaben.next(true);
            } else {
                this.canSeeAbschliessen.next(true);
                this.canSeeSave.next(true);
                this.canSeeFalscheAngaben.next(false);
            }
        } else if (principal.hasOneOfRoles(TSRoleUtil.getMandantRoles())) {
            this.canSeeAbschliessen.next(false);
            if (this.angebot.isInPruefungKanton()) {
                this.canSeeSave.next(false);
            } else {
                this.canSeeSave.next(true);
            }
        }
    }

    private setupForm(angebot: TSFerienbetreuungAngabenAngebot): void {
        if (!angebot) {
            return;
        }
        this.form = this.fb.group({
            id: [
                angebot?.id,
            ],
            angebot: [
                angebot?.angebot,
            ],
            angebotKontaktpersonVorname: [
                angebot?.angebotKontaktpersonVorname,
            ],
            angebotKontaktpersonNachname: [
                angebot?.angebotKontaktpersonNachname,
            ],
            angebotAdresse: this.fb.group({
                strasse: [
                    angebot?.angebotAdresse?.strasse,
                ],
                hausnummer: [
                    angebot?.angebotAdresse?.hausnummer,
                ],
                plz: [
                    angebot?.angebotAdresse?.plz,
                ],
                ort: [
                    angebot?.angebotAdresse?.ort,
                ],
            }),
            anzahlFerienwochenHerbstferien: [
                angebot?.anzahlFerienwochenHerbstferien,
            ],
            anzahlFerienwochenWinterferien: [
                angebot?.anzahlFerienwochenWinterferien,
            ],
            anzahlFerienwochenFruehlingsferien: [
                angebot?.anzahlFerienwochenFruehlingsferien,
            ],
            anzahlFerienwochenSommerferien: [
                angebot?.anzahlFerienwochenSommerferien,
            ],
            anzahlTage: [
                angebot?.anzahlTage,
            ],
            bemerkungenAnzahlFerienwochen: [
                angebot?.bemerkungenAnzahlFerienwochen,
            ],
            anzahlStundenProBetreuungstag: [
                angebot?.anzahlStundenProBetreuungstag,
            ],
            betreuungErfolgtTagsueber: [
                angebot?.betreuungErfolgtTagsueber,
            ],
            bemerkungenOeffnungszeiten: [
                angebot?.bemerkungenOeffnungszeiten,
            ],
            finanziellBeteiligteGemeinden: [
                angebot?.finanziellBeteiligteGemeinden,
            ],
            gemeindeFuehrtAngebotSelber: [
                angebot?.gemeindeFuehrtAngebotSelber,
            ],
            gemeindeBeauftragtExterneAnbieter: [
                angebot?.gemeindeBeauftragtExterneAnbieter,
            ],
            angebotVereineUndPrivateIntegriert: [
                angebot?.angebotVereineUndPrivateIntegriert,
            ],
            bemerkungenKooperation: [
                angebot?.bemerkungenKooperation,
            ],
            leitungDurchPersonMitAusbildung: [
                angebot?.leitungDurchPersonMitAusbildung,
            ],
            betreuungDurchPersonenMitErfahrung: [
                angebot?.betreuungDurchPersonenMitErfahrung,
            ],
            anzahlKinderAngemessen: [
                angebot?.anzahlKinderAngemessen,
            ],
            betreuungsschluessel: [
                angebot?.betreuungsschluessel,
            ],
            bemerkungenPersonal: [
                angebot?.bemerkungenPersonal,
            ],
            fixerTarifKinderDerGemeinde: [
                angebot?.fixerTarifKinderDerGemeinde,
            ],
            einkommensabhaengigerTarifKinderDerGemeinde: [
                angebot?.einkommensabhaengigerTarifKinderDerGemeinde,
            ],
            tagesschuleTarifGiltFuerFerienbetreuung: [
                angebot?.tagesschuleTarifGiltFuerFerienbetreuung,
            ],
            ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet: [
                angebot?.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet,
            ],
            kinderAusAnderenGemeindenZahlenAnderenTarif: [
                angebot?.kinderAusAnderenGemeindenZahlenAnderenTarif,
            ],
            bemerkungenTarifsystem: [
                angebot?.bemerkungenTarifsystem,
            ],
        }, {
            updateOn: 'blur',
        });
    }

    // overwrite
    protected enableFormValidation(): void {
        this.form.get('angebot').setValidators(Validators.required);
        this.form.get('angebotKontaktpersonVorname').setValidators(Validators.required);
        this.form.get('angebotKontaktpersonNachname').setValidators(Validators.required);
        this.enableAdressFormValidation();

        this.form.get('anzahlFerienwochenHerbstferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenWinterferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenFruehlingsferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenSommerferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);

        this.form.get('anzahlTage').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlStundenProBetreuungstag')
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);

        this.form.get('betreuungErfolgtTagsueber').setValidators(Validators.required);

        this.form.get('leitungDurchPersonMitAusbildung').setValidators(Validators.required);
        this.form.get('betreuungDurchPersonenMitErfahrung').setValidators(Validators.required);
        this.form.get('anzahlKinderAngemessen').setValidators(Validators.required);
        this.form.get('betreuungsschluessel')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
    }

    private enableAdressFormValidation(): void {
        this.form.get('angebotAdresse').get('strasse').setValidators(Validators.required);
        this.form.get('angebotAdresse').get('plz').setValidators(Validators.required);
        this.form.get('angebotAdresse').get('ort').setValidators(Validators.required);
    }

    public save(): void {
        this.ferienbetreuungService.saveAngebot(this.container.id, this.formToObject())
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
    }

    private formToObject(): TSFerienbetreuungAngabenAngebot {
        this.angebot = this.form.value;
        const formAdresse = this.form.value.angebotAdresse;

        const adresse = new TSAdresse();
        Object.assign(adresse, formAdresse);
        // set only if adresse is valid
        this.angebot.angebotAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        return this.angebot;
    }

    public formularReadOnky(): boolean {
        return !(this.container?.status === FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE ||
            this.container?.status === FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON);
    }

    public async onAbschliessen(): Promise<void> {
        this.triggerFormValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        if (!await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
            return;
        }

        this.ferienbetreuungService.angebotAbschliessen(this.container.id, this.formToObject())
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
    }

    private handleSaveSuccess(): void {
        this.wizardRS.updateSteps(this.WIZARD_TYPE, this.uiRouterGlobals.params.id);
    }

    private handleSaveError(error: any): void {
        if (error.error?.includes('Not all required properties are set')) {
            this.triggerFormValidation();
            this.showValidierungFehlgeschlagenErrorMessage();
        } else {
            this.errorService.addMesageAsError(this.translate.instant('SAVE_ERROR'));
        }
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenAngebot(this.container.id, this.angebot)
            .subscribe(() => this.handleSaveSuccess(), (error: any) => this.handleSaveError(error));
    }
}
