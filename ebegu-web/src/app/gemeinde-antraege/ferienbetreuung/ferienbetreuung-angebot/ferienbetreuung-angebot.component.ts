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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, of, Observable, Subject} from 'rxjs';
import {filter, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungFormularStatus} from '../../../../models/enums/TSFerienbetreuungFormularStatus';
import {TSFerienbetreuungAngaben} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAngebotComponent');

@Component({
    selector: 'dv-ferienbetreuung-angebot',
    templateUrl: './ferienbetreuung-angebot.component.html',
    styleUrls: ['./ferienbetreuung-angebot.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungAngebotComponent extends AbstractFerienbetreuungFormular implements OnInit, OnDestroy {

    public formValidationTriggered = false;
    public bfsGemeinden: TSBfsGemeinde[];

    public form = this.fb.group({
        angebot: [<null | string> null],
        angebotAdresse: this.fb.group({
            kontaktpersonVorname: [<null | string> null],
            kontaktpersonNachname: [<null | string> null],
            strasse: [<null | string> null],
            hausnummer: [<null | string> null],
            plz: [<null | string> null],
            ort: [<null | string> null],
            zusatzzeile: [<null | string> null],
        }),
        anzahlFerienwochenHerbstferien: [<null | number> null],
        anzahlFerienwochenWinterferien: [<null | number> null],
        anzahlFerienwochenSportferien: [<null | number> null],
        anzahlFerienwochenFruehlingsferien: [<null | number> null],
        anzahlFerienwochenSommerferien: [<null | number> null],
        anzahlTage: [<null | number> null],
        bemerkungenAnzahlFerienwochen: [<null | string> null],
        anzahlStundenProBetreuungstag: [<null | number> null],
        betreuungErfolgtTagsueber: [<null | boolean> null],
        bemerkungenOeffnungszeiten: [<null | string> null],
        finanziellBeteiligteGemeinden: [<null | string[]> null],
        gemeindeFuehrtAngebotSelber: [<null | boolean> null],
        gemeindeFuehrtAngebotInKooperation: [<null | boolean> null],
        gemeindeBeauftragtExterneAnbieter: [<null | boolean> null],
        angebotVereineUndPrivateIntegriert: [<null | boolean> null],
        bemerkungenKooperation: [<null | string> null],
        leitungDurchPersonMitAusbildung: [<null | boolean> null],
        betreuungDurchPersonenMitErfahrung: [<null | boolean> null],
        anzahlKinderAngemessen: [<null | boolean> null],
        betreuungsschluessel: [<null | string> null],
        bemerkungenPersonal: [<null | string> null],
        fixerTarifKinderDerGemeinde: [<null | boolean> null],
        einkommensabhaengigerTarifKinderDerGemeinde: [<null | boolean> null],
        tagesschuleTarifGiltFuerFerienbetreuung: [<null | boolean> null],
        ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet: [<null | boolean> null],
        kinderAusAnderenGemeindenZahlenAnderenTarif: [<null | string> null],
        bemerkungenTarifsystem: [<null | string> null]
    });

    private angebot: TSFerienbetreuungAngabenAngebot;
    public vorgaenger$: Observable<TSFerienbetreuungAngabenContainer>;
    private readonly unsubscribe$ = new Subject();

    public constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        protected readonly cd: ChangeDetectorRef,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        private readonly fb: FormBuilder,
        private readonly gemeindeRS: GemeindeRS,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly authService: AuthServiceRS,
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        combineLatest([
                this.ferienbetreuungService.getFerienbetreuungContainer(),
                this.authService.principal$.pipe(filter(principal => !!principal))
            ]
        ).pipe(takeUntil(this.unsubscribe$))
            .subscribe(([container, principal]) => {
            this.container = container;
            this.angebot = container.isAtLeastInPruefungKantonOrZurueckgegeben() ?
                container.angabenKorrektur?.angebot : container.angabenDeklaration?.angebot;
            this.setupFormAndPermissions(container, this.angebot, principal);
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            LOG.error(error);
        });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.bfsGemeinden.sort((a, b) => a.name.localeCompare(b.name));
            this.cd.markForCheck();
        });
        this.vorgaenger$ = this.ferienbetreuungService.getFerienbetreuungVorgaengerContainer()
            .pipe(takeUntil(this.unsubscribe$));
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    protected setupForm(angebot: TSFerienbetreuungAngabenAngebot): void {
        if (!angebot) {
            return;
        }
        this.form.patchValue({
            anzahlFerienwochenSommerferien: angebot.anzahlFerienwochenSommerferien,
            anzahlFerienwochenHerbstferien: angebot.anzahlFerienwochenHerbstferien,
            anzahlFerienwochenWinterferien: angebot.anzahlFerienwochenWinterferien,
            anzahlFerienwochenSportferien: angebot.anzahlFerienwochenSportferien,
            anzahlFerienwochenFruehlingsferien: angebot.anzahlFerienwochenFruehlingsferien,
            angebot: angebot.angebot,
            angebotAdresse: {
                kontaktpersonVorname: angebot.angebotKontaktpersonVorname,
                kontaktpersonNachname: angebot.angebotKontaktpersonNachname,
                strasse: angebot.angebotAdresse?.strasse,
                plz: angebot.angebotAdresse?.plz,
                hausnummer: angebot.angebotAdresse?.hausnummer,
                ort: angebot.angebotAdresse?.ort,
                zusatzzeile: angebot.angebotAdresse?.zusatzzeile
            },
            anzahlTage: angebot.anzahlTage,
            angebotVereineUndPrivateIntegriert: angebot.angebotVereineUndPrivateIntegriert,
            anzahlKinderAngemessen: angebot.anzahlKinderAngemessen,
            anzahlStundenProBetreuungstag: angebot.anzahlStundenProBetreuungstag,
            bemerkungenAnzahlFerienwochen: angebot.bemerkungenAnzahlFerienwochen,
            gemeindeFuehrtAngebotInKooperation: angebot.gemeindeFuehrtAngebotInKooperation,
            gemeindeFuehrtAngebotSelber: angebot.gemeindeFuehrtAngebotInKooperation,
            kinderAusAnderenGemeindenZahlenAnderenTarif: angebot.kinderAusAnderenGemeindenZahlenAnderenTarif,
            leitungDurchPersonMitAusbildung: angebot.leitungDurchPersonMitAusbildung,
            bemerkungenTarifsystem: angebot.bemerkungenTarifsystem,
            bemerkungenKooperation: angebot.bemerkungenKooperation,
            bemerkungenPersonal: angebot.bemerkungenPersonal,
            betreuungDurchPersonenMitErfahrung: angebot.betreuungDurchPersonenMitErfahrung,
            betreuungErfolgtTagsueber: angebot.betreuungErfolgtTagsueber,
            einkommensabhaengigerTarifKinderDerGemeinde: angebot.einkommensabhaengigerTarifKinderDerGemeinde,
            ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet:
                angebot.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet,
            finanziellBeteiligteGemeinden: angebot.finanziellBeteiligteGemeinden,
            fixerTarifKinderDerGemeinde: angebot.fixerTarifKinderDerGemeinde,
            gemeindeBeauftragtExterneAnbieter: angebot.gemeindeBeauftragtExterneAnbieter,
            tagesschuleTarifGiltFuerFerienbetreuung: angebot.tagesschuleTarifGiltFuerFerienbetreuung,
            bemerkungenOeffnungszeiten: angebot.bemerkungenOeffnungszeiten,
            betreuungsschluessel: angebot.betreuungsschluessel,
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.controls.anzahlFerienwochenHerbstferien.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlFerienwochenWinterferien.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlFerienwochenSportferien.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlFerienwochenFruehlingsferien.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlFerienwochenSommerferien.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlTage.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlStundenProBetreuungstag.setValidators(
            numberValidator(ValidationType.HALF)
        );

        this.enableAdressValidation();
        this.triggerFormValidation();
    }

    // overwrite
    protected enableFormValidation(): void {
        this.form.controls.angebot.setValidators(Validators.required);
        this.enableAdressValidation();

        this.form.controls.anzahlFerienwochenHerbstferien
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlFerienwochenWinterferien
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlFerienwochenSportferien
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlFerienwochenFruehlingsferien
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlFerienwochenSommerferien
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);

        this.form.controls.anzahlTage.setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlStundenProBetreuungstag
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);

        this.form.controls.betreuungErfolgtTagsueber.setValidators(Validators.required);

        this.form.controls.leitungDurchPersonMitAusbildung.setValidators(Validators.required);
        this.form.controls.betreuungDurchPersonenMitErfahrung.setValidators(Validators.required);
        this.form.controls.anzahlKinderAngemessen.setValidators(Validators.required);
        this.form.controls.betreuungsschluessel.setValidators([Validators.required]);

        this.form.controls.gemeindeFuehrtAngebotSelber.setValidators([Validators.required]);
        this.form.controls.gemeindeBeauftragtExterneAnbieter.setValidators([Validators.required]);
    }

    public async save(): Promise<void> {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return of<void>().toPromise();
        }
        try {
            await this.modifyValuesDelegationsModellIfNecessary();
        } catch (e) {
            if (e instanceof UserDidNotAcceptError) {
                // user did not accept saving.
                return of<void>().toPromise();
            }
            throw e;
        }
        this.ferienbetreuungService.saveAngebot(this.container.id, this.formToObject())
            .subscribe(() => {
                this.formValidationTriggered = false;
                this.ferienbetreuungService.updateFerienbetreuungContainerStores(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveErrors(err));
    }

    // falls es sich nicht mehr um das Delegationsmodell handelt müssen in diesem Schritt
    // alle Werte gelöscht werden, die spezifisch für das Delegationsmodell angegeben wurden.
    private modifyValuesDelegationsModellIfNecessary(): Promise<any> {
        if (!this.container) {
            return of().toPromise();
        }
        const angaben = this.container.isAtLeastInPruefungKantonOrZurueckgegeben()
            ? this.container.angabenKorrektur
            : this.container.angabenDeklaration;
        if (!angaben.kostenEinnahmen) {
            return of().toPromise();
        }
        // falls es sich NICHT um das Delegationsmodell handelt und einige Werte, die nur für das Delegationsmodell
        // notwendig sind, schon ausgefüllt sind, dann werden diese gelöscht
        if (!this.isDelegationsmodell()) {
            return this.deleteAngabenDelegationsmodellIfNecessary(angaben);
        }
        // falls es sich um das Delegationsmodell handelt wird der Step KostenEinnahmen invalidiert, falls dieser
        // schon valid ist
        return this.invalidateKostenEinnahmenBecauseOfDelegationsmodell(angaben);
    }

    private async deleteAngabenDelegationsmodellIfNecessary(angaben: TSFerienbetreuungAngaben): Promise<any> {
        if (angaben.kostenEinnahmen.sockelbeitrag
            || angaben.kostenEinnahmen.beitraegeNachAnmeldungen
            || angaben.kostenEinnahmen.vorfinanzierteKantonsbeitraege
            || angaben.kostenEinnahmen.eigenleistungenGemeinde) {
            const confirmed = await this.confirmDialog('FERIENBETREUUNG_DELETE_KOSTEN_NUTZEN_DELEGATIONSMODELL');
            if (!confirmed) {
                throw new UserDidNotAcceptError();
            }
            angaben.kostenEinnahmen.sockelbeitrag = null;
            angaben.kostenEinnahmen.beitraegeNachAnmeldungen = null;
            angaben.kostenEinnahmen.vorfinanzierteKantonsbeitraege = null;
            angaben.kostenEinnahmen.eigenleistungenGemeinde = null;
            return this.ferienbetreuungService.saveKostenEinnahmen(this.container.id, angaben.kostenEinnahmen)
                .toPromise();
        }
        return of().toPromise();
    }

    private async invalidateKostenEinnahmenBecauseOfDelegationsmodell(angaben: TSFerienbetreuungAngaben): Promise<any> {
        // step muss nur updated werden, wenn schon abgeschlossen
        if (angaben.kostenEinnahmen.status !== TSFerienbetreuungFormularStatus.ABGESCHLOSSEN) {
            return of().toPromise();
        }
        if (!angaben.kostenEinnahmen.sockelbeitrag
            || !angaben.kostenEinnahmen.beitraegeNachAnmeldungen
            || !angaben.kostenEinnahmen.vorfinanzierteKantonsbeitraege
            || !angaben.kostenEinnahmen.eigenleistungenGemeinde) {
            const confirmed = await this.confirmDialog('FERIENBETREUUNG_INVALIDATE_KOSTEN_NUTZEN_DELEGATIONSMODELL');
            if (!confirmed) {
                throw new UserDidNotAcceptError();
            }
            return this.ferienbetreuungService.falscheAngabenKostenEinnahmen(
                this.container.id,
                angaben.kostenEinnahmen
            ).toPromise();
        }
        return of().toPromise();
    }

    private formToObject(): TSFerienbetreuungAngabenAngebot {
        const values = this.form.getRawValue();
        this.angebot.angebot = values.angebot;
        this.angebot.angebotKontaktpersonVorname = values.angebotAdresse.kontaktpersonVorname;
        this.angebot.angebotKontaktpersonNachname = values.angebotAdresse.kontaktpersonNachname;
        if (EbeguUtil.isNullOrUndefined(this.angebot.angebotAdresse)) {
            this.angebot.angebotAdresse = new TSAdresse();
        }
        this.angebot.angebotAdresse.strasse = values.angebotAdresse.strasse;
        this.angebot.angebotAdresse.hausnummer = values.angebotAdresse.hausnummer;
        this.angebot.angebotAdresse.zusatzzeile = values.angebotAdresse.zusatzzeile;
        this.angebot.angebotAdresse.plz = values.angebotAdresse.plz;
        this.angebot.angebotAdresse.ort = values.angebotAdresse.ort;

        this.angebot.anzahlFerienwochenHerbstferien = values.anzahlFerienwochenHerbstferien;
        this.angebot.anzahlFerienwochenWinterferien = values.anzahlFerienwochenWinterferien;
        this.angebot.anzahlFerienwochenSportferien = values.anzahlFerienwochenSportferien;
        this.angebot.anzahlFerienwochenFruehlingsferien = values.anzahlFerienwochenFruehlingsferien;
        this.angebot.anzahlFerienwochenSommerferien = values.anzahlFerienwochenSommerferien;
        this.angebot.anzahlTage = values.anzahlTage;
        this.angebot.bemerkungenAnzahlFerienwochen = values.bemerkungenAnzahlFerienwochen;
        this.angebot.anzahlStundenProBetreuungstag = values.anzahlStundenProBetreuungstag;
        this.angebot.betreuungErfolgtTagsueber = values.betreuungErfolgtTagsueber;
        this.angebot.bemerkungenOeffnungszeiten = values.bemerkungenOeffnungszeiten;
        this.angebot.finanziellBeteiligteGemeinden = values.finanziellBeteiligteGemeinden;
        this.angebot.gemeindeFuehrtAngebotSelber = values.gemeindeFuehrtAngebotSelber;
        this.angebot.gemeindeFuehrtAngebotInKooperation = values.gemeindeFuehrtAngebotInKooperation;
        this.angebot.gemeindeBeauftragtExterneAnbieter = values.gemeindeBeauftragtExterneAnbieter;
        this.angebot.angebotVereineUndPrivateIntegriert = values.angebotVereineUndPrivateIntegriert;
        this.angebot.bemerkungenKooperation = values.bemerkungenKooperation;
        this.angebot.leitungDurchPersonMitAusbildung = values.leitungDurchPersonMitAusbildung;
        this.angebot.betreuungDurchPersonenMitErfahrung = values.betreuungDurchPersonenMitErfahrung;
        this.angebot.anzahlKinderAngemessen = values.anzahlKinderAngemessen;
        this.angebot.betreuungsschluessel = values.betreuungsschluessel;
        this.angebot.bemerkungenPersonal = values.bemerkungenPersonal;
        this.angebot.fixerTarifKinderDerGemeinde = values.fixerTarifKinderDerGemeinde;
        this.angebot.einkommensabhaengigerTarifKinderDerGemeinde =
            values.einkommensabhaengigerTarifKinderDerGemeinde;
        this.angebot.tagesschuleTarifGiltFuerFerienbetreuung =
            values.tagesschuleTarifGiltFuerFerienbetreuung;
        this.angebot.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet =
            values.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
        this.angebot.kinderAusAnderenGemeindenZahlenAnderenTarif =
            values.kinderAusAnderenGemeindenZahlenAnderenTarif;
        this.angebot.bemerkungenTarifsystem = values.bemerkungenTarifsystem;

        return this.angebot;
    }

    public async onAbschliessen(): Promise<void> {
        if (!await this.checkReadyForAbschliessen()) {
            return of<void>().toPromise();
        }
        try {
            await this.modifyValuesDelegationsModellIfNecessary();
        } catch (e) {
            if (e instanceof UserDidNotAcceptError) {
                // user did not accept saving.
                return of<void>().toPromise();
            }
            throw e;
        }
        this.ferienbetreuungService.angebotAbschliessen(this.container.id, this.formToObject())
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenAngebot(this.container.id, this.angebot)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
    }

    // eslint-disable-next-line
    private adressValidValidator(): ValidatorFn {
        return control => {
            const strasse = control.get('strasse');
            const ort = control.get('ort');
            const plz = control.get('plz');
            const vorname = control.get('kontaktpersonVorname');
            const nachname = control.get('kontaktpersonNachname');

            let formErroneous = false;

            if (this.formAbschliessenTriggered ||
                (strasse.value || ort.value || plz.value || vorname.value || nachname.value)) {
                if (!strasse.value) {
                    strasse.setErrors({required: true});
                    formErroneous = true;
                }
                if (!ort.value) {
                    ort.setErrors({required: true});
                    formErroneous = true;
                }
                if (!plz.value) {
                    plz.setErrors({required: true});
                    formErroneous = true;
                }
                if (!vorname.value) {
                    vorname.setErrors({required: true});
                    formErroneous = true;
                }
                if (!nachname.value) {
                    nachname.setErrors({required: true});
                    formErroneous = true;
                }
            } else {
                strasse.setErrors(null);
                ort.setErrors(null);
                plz.setErrors(null);
                vorname.setErrors(null);
                nachname.setErrors(null);
            }
            return formErroneous ? {adressInvalid: true} : null;
        };
    }

    protected enableAdressValidation(): void {
        this.form.controls.angebotAdresse.setValidators(this.adressValidValidator());
        this.form.controls.angebotAdresse.markAllAsTouched();

        this.triggerFormValidation();
    }

    private isDelegationsmodell(): boolean {
        return EbeguUtil.isNotNullAndFalse(this.form.value.gemeindeFuehrtAngebotSelber)
            && this.form.value.gemeindeBeauftragtExterneAnbieter;
    }
}

class UserDidNotAcceptError extends Error {}
