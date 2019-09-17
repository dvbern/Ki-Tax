/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {getTSModulTagesschuleIntervallValues, TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import EbeguUtil from '../../../utils/EbeguUtil';

@Component({
    selector: 'dv-edit-modul-tagesschule',
    templateUrl: './edit-modul-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditModulTagesschuleComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    @Input() public modulTagesschuleGroup: TSModulTagesschuleGroup;
    @Output() callback = new EventEmitter<TSModulTagesschuleGroup>();

    public modulMontag: TSModulTagesschule;
    public modulDienstag: TSModulTagesschule;
    public modulMittwoch: TSModulTagesschule;
    public modulDonnerstag: TSModulTagesschule;
    public modulFreitag: TSModulTagesschule;

    public constructor(
    ) {
    }



    public ngOnInit(): void {
        // Alle die aktuell gesetzt sind, werden als angeboten initialisiert
        for (const modul of this.modulTagesschuleGroup.module) {
            EditModulTagesschuleComponent.initializeModulIfAngeboten(TSDayOfWeek.MONDAY, modul, this.modulMontag);
            EditModulTagesschuleComponent.initializeModulIfAngeboten(TSDayOfWeek.TUESDAY, modul, this.modulDienstag);
            EditModulTagesschuleComponent.initializeModulIfAngeboten(TSDayOfWeek.WEDNESDAY, modul, this.modulMittwoch);
            EditModulTagesschuleComponent.initializeModulIfAngeboten(TSDayOfWeek.THURSDAY, modul, this.modulDonnerstag);
            EditModulTagesschuleComponent.initializeModulIfAngeboten(TSDayOfWeek.FRIDAY, modul, this.modulFreitag);
        }
        // Alle die jetzt noch nicht gesetzt sind, müssen neu erstellt werden (nicht angeboten)
        EditModulTagesschuleComponent.initalizeModulIfNichtAngeboten(TSDayOfWeek.MONDAY, this.modulMontag);
        EditModulTagesschuleComponent.initalizeModulIfNichtAngeboten(TSDayOfWeek.TUESDAY, this.modulDienstag);
        EditModulTagesschuleComponent.initalizeModulIfNichtAngeboten(TSDayOfWeek.WEDNESDAY, this.modulMittwoch);
        EditModulTagesschuleComponent.initalizeModulIfNichtAngeboten(TSDayOfWeek.THURSDAY, this.modulDonnerstag);
        EditModulTagesschuleComponent.initalizeModulIfNichtAngeboten(TSDayOfWeek.FRIDAY, this.modulFreitag);
    }

    private static initializeModulIfAngeboten(day: TSDayOfWeek, modulToEvaluate: TSModulTagesschule, modulToInitialize: TSModulTagesschule) {
        if (modulToEvaluate.wochentag === day) {
            modulToInitialize = modulToEvaluate;
            modulToInitialize.angeboten = true;
        }
    }

    private static initalizeModulIfNichtAngeboten(day: TSDayOfWeek, modulToInitialize: TSModulTagesschule) {
        if (EbeguUtil.isNullOrUndefined(modulToInitialize)) {
            modulToInitialize = new TSModulTagesschule();
            modulToInitialize.wochentag = day;
            modulToInitialize.angeboten = false;
        }
    }

    public getModulTagesschuleIntervallOptions(): Array<TSModulTagesschuleIntervall> {
        return getTSModulTagesschuleIntervallValues();
    }

    public apply(): void {
        this.modulTagesschuleGroup.module = [];
        this.applyModulIfAngeboten(this.modulMontag);
        this.applyModulIfAngeboten(this.modulDienstag);
        this.applyModulIfAngeboten(this.modulMittwoch);
        this.applyModulIfAngeboten(this.modulDonnerstag);
        this.applyModulIfAngeboten(this.modulFreitag);
        if (this.isValid()) {
            this.callback.emit(this.modulTagesschuleGroup);
        } else {
            this.ngOnInit();
        }
    }

    private applyModulIfAngeboten(modul: TSModulTagesschule): void {
        if (modul.angeboten) {
            this.modulTagesschuleGroup.module.push(modul);
        }
    }

    private isValid(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleGroup.modulTagesschuleName)
            && EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleGroup.bezeichnung)
            && EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleGroup.zeitVon)
            && EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleGroup.zeitBis)
            && EbeguUtil.isNotNullOrUndefined(this.modulTagesschuleGroup.intervall)
            && this.modulTagesschuleGroup.module.length > 0;
    }
}

