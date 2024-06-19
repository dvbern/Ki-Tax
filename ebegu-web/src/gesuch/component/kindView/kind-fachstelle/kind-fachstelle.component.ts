import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../../app/core/constants/CONSTANTS';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {TSFachstelleName} from '../../../../models/enums/TSFachstelleName';
import {TSFachstellenTyp} from '../../../../models/enums/TSFachstellenTyp';
import {TSGruendeZusatzleistung} from '../../../../models/enums/TSGruendeZusatzleistung';
import {TSIntegrationTyp} from '../../../../models/enums/TSIntegrationTyp';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSEinstellung} from '../../../../models/TSEinstellung';
import {TSFachstelle} from '../../../../models/TSFachstelle';
import {TSPensumFachstelle} from '../../../../models/TSPensumFachstelle';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {EnumEx} from '../../../../utils/EnumEx';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {HybridFormBridgeService} from '../../../service/hybrid-form-bridge.service';

const LOG = LogFactory.createLog('KindFachstelleComponennt');

@Component({
    selector: 'dv-kind-fachstelle',
    templateUrl: './kind-fachstelle.component.html',
    styleUrls: ['./kind-fachstelle.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KindFachstelleComponent
    implements OnInit, OnChanges, AfterViewInit, OnDestroy
{
    @Input()
    public pensumFachstelle: TSPensumFachstelle;

    // since we have a hybrid application, this component might be used in an angularjs parent,
    // therefore we need to provide this information for the form validation
    @Input()
    public submitted: boolean;

    @Input()
    public index: number = 0;

    @Input()
    public pensumFachstellenList: TSPensumFachstelle[];

    @Output()
    public readonly onPensumFachstellenOverlaps = new EventEmitter<string>();

    @ViewChild(NgForm) private readonly form: NgForm;

    public fachstellenTyp: TSFachstellenTyp;

    public minValueAllowed: number = 0;
    public maxValueAllowed: number = 100;
    public integrationTypes: TSIntegrationTyp[];
    public readonly allowedRoles: ReadonlyArray<TSRole> =
        TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    public readonly gruendeZusatzleistung = EnumEx.getNames(
        TSGruendeZusatzleistung
    );
    public readonly PATTERN_PERCENTAGE = CONSTANTS.PATTERN_PERCENTAGE;

    public constructor(
        private readonly einstellungRS: EinstellungRS,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly authService: AuthServiceRS,
        private readonly formBridgeService: HybridFormBridgeService
    ) {}

    public ngOnInit(): void {
        this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            )
            .subscribe(einstellungen => {
                this.loadEinstellungFachstellenTyp(einstellungen);
            });
        this.loadEinstellungenForIntegration();
    }

    public ngAfterViewInit(): void {
        this.formBridgeService.register(this.form);
    }

    public ngOnDestroy(): void {
        this.formBridgeService.unregister(this.form);
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (
            EbeguUtil.isNotNullOrUndefined(changes.submitted) &&
            changes.submitted.currentValue === true
        ) {
            this.form.onSubmit(null);
        }
    }

    public loadEinstellungenForIntegration(): void {
        if (EbeguUtil.isNullOrUndefined(this.pensumFachstelle)) {
            return;
        }
        if (this.isFachstellenTypLuzern()) {
            this.pensumFachstelle.pensum = 100;
        }
        if (
            this.pensumFachstelle.integrationTyp ===
            TSIntegrationTyp.SOZIALE_INTEGRATION
        ) {
            this.getEinstellungenFachstelle(
                TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
                TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION
            );
            this.resetGruendeZusatzleistung();
        } else if (
            this.pensumFachstelle.integrationTyp ===
            TSIntegrationTyp.SPRACHLICHE_INTEGRATION
        ) {
            this.getEinstellungenFachstelle(
                TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
                TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION
            );
            this.resetGruendeZusatzleistung();
            // eslint-disable-next-line max-len
        } else if (
            this.pensumFachstelle.integrationTyp ===
            TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION
        ) {
            this.pensumFachstelle.pensum = 100;
        }
    }

    public isFachstellenTypLuzern(): boolean {
        return this.fachstellenTyp === TSFachstellenTyp.LUZERN;
    }

    private getEinstellungenFachstelle(
        minValueEinstellungKey: TSEinstellungKey,
        maxValueEinstellungKey: TSEinstellungKey
    ): void {
        this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            )
            .subscribe(
                (response: TSEinstellung[]) => {
                    response
                        .filter(r => r.key === minValueEinstellungKey)
                        .forEach(value => {
                            this.minValueAllowed = Number(value.value);
                        });
                    response
                        .filter(r => r.key === maxValueEinstellungKey)
                        .forEach(value => {
                            this.maxValueAllowed = Number(value.value);
                        });

                    if (this.isOnlyOneValueAllowed()) {
                        this.pensumFachstelle.pensum = this.minValueAllowed;
                    }
                },
                error => LOG.error(error)
            );
    }

    private resetGruendeZusatzleistung(): void {
        this.pensumFachstelle.gruendeZusatzleistung = undefined;
    }

    private loadEinstellungFachstellenTyp(
        einstellungen: TSEinstellung[]
    ): void {
        const einstellung = einstellungen.find(
            e => e.key === TSEinstellungKey.FACHSTELLEN_TYP
        );
        this.fachstellenTyp = new EbeguRestUtil().parseFachstellenTyp(
            einstellung.value
        );

        this.integrationTypes =
            this.fachstellenTyp === TSFachstellenTyp.LUZERN
                ? [
                      TSIntegrationTyp.SPRACHLICHE_INTEGRATION,
                      TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION
                  ]
                : [
                      TSIntegrationTyp.SOZIALE_INTEGRATION,
                      TSIntegrationTyp.SPRACHLICHE_INTEGRATION
                  ];
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public gruendeZusatzleistungRequired(): boolean {
        return (
            this.pensumFachstelle.integrationTyp ===
                TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION &&
            this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles())
        );
    }

    public getFachstellenList$(): Observable<TSFachstelle[]> {
        return this.gesuchModelManager.getFachstellenAnspruchList().pipe(
            map(fachstellen => {
                if (
                    this.pensumFachstelle.fachstelle?.name ===
                    TSFachstelleName.KINDES_ERWACHSENEN_SCHUTZBEHOERDE
                ) {
                    fachstellen.concat(this.pensumFachstelle.fachstelle);
                }
                return fachstellen;
            })
        );
    }

    public isOnlyOneValueAllowed(): boolean {
        return this.minValueAllowed === this.maxValueAllowed;
    }

    public compareByID(fachstelle1: TSFachstelle, fachstelle2: TSFachstelle) {
        return fachstelle1?.id === fachstelle2?.id;
    }

    public validatePensumOverlaps(): void {
        if (!this.form.valid) {
            return;
        }

        if (this.pensumFachstellenList.length <= 1) {
            this.onPensumFachstellenOverlaps.emit(null);
            return;
        }

        this.onPensumFachstellenOverlaps.emit(
            this.getWarningIfFachstelleOverlaps()
        );
        return;
    }

    private getWarningIfFachstelleOverlaps(): string {
        const sortedFachstellenList = this.pensumFachstellenList
            .slice()
            .sort(
                (p1, p2) =>
                    p1.gueltigkeit.gueltigAb.valueOf() -
                    p2.gueltigkeit.gueltigAb.valueOf()
            );

        for (const [index, pensum] of sortedFachstellenList.entries()) {
            if (index === sortedFachstellenList.length - 1) {
                return null;
            }

            const nextPensum = sortedFachstellenList[index + 1];

            if (
                pensum.gueltigkeit.isInDateRange(
                    nextPensum.gueltigkeit.gueltigAb
                ) ||
                (EbeguUtil.isNotNullOrUndefined(
                    nextPensum.gueltigkeit.gueltigBis
                ) &&
                    pensum.gueltigkeit.isInDateRange(
                        nextPensum.gueltigkeit.gueltigBis
                    ))
            ) {
                return nextPensum.pensum > pensum.pensum
                    ? 'PENSUM_FACHSTELLE_WARN_OVERLAP_HOEHER'
                    : 'PENSUM_FACHSTELLE_WARN_OVERLAP_TIEFER';
            }
        }

        return null;
    }
}
