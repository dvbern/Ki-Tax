import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {TSApplicationProperty} from '../../../models/TSApplicationProperty';
import {AbstractAdminViewX} from '../../abstractAdminViewX';
import {ReindexRS} from '../../service/reindexRS.rest';

const LOG = LogFactory.createLog('AdminViewXComponent');

@Component({
    selector: 'dv-admin-view-x',
    templateUrl: './admin-view-x.component.html',
    styleUrls: ['./admin-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminViewXComponent extends AbstractAdminViewX implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    public applicationProperty: TSApplicationProperty;
    public displayedCollection: MatTableDataSource<TSApplicationProperty>;
    public displayedColumns: string[] = ['name', 'value', 'timestampErstellt'];
    public filterColumns: string[] = ['filter'];
    public reindexInProgress: boolean = false;
    public recreateAlleFaelleInProgress: boolean = false;

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly reindexRS: ReindexRS,
        private readonly searchRS: SearchRS,
        private readonly dvDialog: MatDialog,
        private readonly cd: ChangeDetectorRef,
        authServiceRS: AuthServiceRS
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.resetForm();
    }

    public submit(): void {
        if (this.form.invalid) {
            return;
        }
        // Bei STADT_BERN_ASIV_CONFIGURED eine Sicherheitsabfrage machen
        // eslint-disable-next-line
        if (
            this.applicationProperty.name === 'STADT_BERN_ASIV_CONFIGURED' &&
            this.applicationProperty.value === 'true'
        ) {
            this.dvDialog
                .open(DvNgRemoveDialogComponent, {
                    data: {
                        title: 'CREATE_MASSENMUTATION_BERN_DIALOG_TITLE',
                        text: 'CREATE_MASSENMUTATION_BERN_DIALOG_TEXT'
                    }
                })
                .afterClosed()
                .subscribe(
                    () => {
                        this.doSave();
                    },
                    err => {
                        LOG.error(err);
                    }
                );
        } else {
            this.doSave();
        }
    }

    private doSave(): void {
        // testen ob aktuelles property schon gespeichert ist
        if (this.applicationProperty.isNew()) {
            this.applicationPropertyRS.update(
                this.applicationProperty.name,
                this.applicationProperty.value
            );
        } else {
            this.applicationPropertyRS.create(
                this.applicationProperty.name,
                this.applicationProperty.value
            );
        }
        this.applicationProperty = undefined;
    }

    public editRow(row: TSApplicationProperty): void {
        this.applicationProperty = row;
    }

    public getApplicationPropertyArray(): string[] {
        return this.applicationProperty.value.split(',');
    }

    public resetForm(): void {
        this.applicationProperty = undefined;
        this.applicationPropertyRS
            .getAllApplicationProperties()
            .then(response => {
                this.displayedCollection =
                    new MatTableDataSource<TSApplicationProperty>(response);
                this.displayedCollection.sort = this.sort;
                this.cd.markForCheck();
            });
    }

    public startReindex(): void {
        // avoid sending double by keeping it disabled until reload
        this.reindexInProgress = true;
        this.reindexRS.reindex().subscribe(response => {
            this.dvDialog.open(DvNgOkDialogComponent, {
                data: {title: response}
            });
        });
    }

    public startRecreateAlleFaelleView(): void {
        // avoid sending double by keeping it disabled until reload
        this.recreateAlleFaelleInProgress = true;
        this.searchRS.recreateAlleFaelleView().subscribe(response => {
            this.dvDialog.open(DvNgOkDialogComponent, {
                data: {title: response}
            });
        });
    }

    public doFilter(value: string): void {
        this.displayedCollection.filter = value;
    }
}
