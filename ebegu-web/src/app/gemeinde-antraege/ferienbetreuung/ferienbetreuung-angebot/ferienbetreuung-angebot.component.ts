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
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAngebotComponent');

@Component({
    selector: 'dv-ferienbetreuung-angebot',
    templateUrl: './ferienbetreuung-angebot.component.html',
    styleUrls: ['./ferienbetreuung-angebot.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungAngebotComponent implements OnInit {

    public form: FormGroup;
    public formFreigebenTriggered = false;
    public bfsGemeinden: TSBfsGemeinde[];

    private angebot: TSFerienbetreuungAngabenAngebot;
    private container: TSFerienbetreuungAngabenContainer;
    private readonly WIZARD_TYPE: TSWizardStepXTyp.FERIENBETREUUNG;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly gemeindeRS: GemeindeRS,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly wizardRS: WizardStepXRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
                this.angebot = container.angabenDeklaration?.angebot;
                this.setupForm(this.angebot);
                this.cd.markForCheck();
            }, error => {
                LOG.error(error);
            });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
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

    private enableFormValidation(): void {
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

    public formularNotEditable(): boolean {
        return !(this.container?.status === FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE ||
            this.container?.status === FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON);
    }

    public async onAbschliessen(): Promise<void> {
        this.triggerFormValidation();

        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }
        if (!await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
            return;
        }

        this.ferienbetreuungService.angebotAbschliessen(this.container.id, this.formToObject())
            .subscribe(() => this.handleSaveSuccess(), () => this.handleSaveError());
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant(frageKey),
        };
        return this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }

    private handleSaveSuccess(): void {
        this.wizardRS.updateSteps(this.WIZARD_TYPE, this.uiRouterGlobals.params.id);
    }

    private handleSaveError(): void {
        this.errorService.addMesageAsError(this.translate.instant('SAVE_ERROR'));
    }

    private triggerFormValidation(): void {
        this.enableFormValidation();
        this.formFreigebenTriggered = true;
        for (const key in this.form.controls) {
            if (this.form.get(key) !== null) {
                this.form.get(key).markAsTouched();
                this.form.get(key).updateValueAndValidity();
            }
        }
        this.form.updateValueAndValidity();
    }
}
