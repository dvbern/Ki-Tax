/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Category, UIRouter} from '@uirouter/core';
import {visualizer} from '@uirouter/visualizer';
import * as Raven from 'raven-js';
import {Subject} from 'rxjs';
import {distinctUntilChanged, filter, map, takeUntil} from 'rxjs/operators';
import {LogFactory} from '../../../app/core/logging/LogFactory';

const LOG = LogFactory.createLog('DebuggingComponent');

@Component({
    selector: 'dv-debugging',
    templateUrl: './debugging.component.html',
    styleUrls: ['./debugging.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DebuggingComponent implements OnInit, OnDestroy {

    @ViewChild('traceForm', { static: true }) private readonly traceForm: NgForm;

    public readonly TRACE_CATEGORY = Category;
    public readonly TRACE_CATEGORY_KEYS = Object.keys(Category).filter(k => typeof Category[k as any] === 'number');

    public routerTraceCategories: Category[];
    public hasVisualizer: boolean;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(private readonly router: UIRouter) {
        this.routerTraceCategories = this.TRACE_CATEGORY_KEYS
            .map(k => Category[k as any] as any)
            .filter(c => router.trace.enabled(c));

        this.hasVisualizer = !!router.getPlugin('visualizer');
    }

    public ngOnInit(): void {
        this.initTracer();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public onActivateVisualizer(): void {
        if (!this.router.getPlugin('visualizer')) {
            this.hasVisualizer = true;
            this.router.plugin(visualizer);
        }
    }

    private initTracer(): void {
        this.traceForm.valueChanges
            .pipe(
                filter(values => !!values && values.hasOwnProperty('routerTraceCategories')),
                map(values => values.routerTraceCategories),
                distinctUntilChanged(),
                takeUntil(this.unsubscribe$),
            )
            .subscribe(
                (categories: Category[]) => categories.forEach(c => this.router.trace.enable(c)),
                err => LOG.error(err),
            );
    }

    public doUndefined(): void {
        const test = [1, 2];
        // tslint:disable-next-line:no-ignored-return
        test[2].toFixed(1);
    }

    public doThrowError(): void {
        throw new Error('This is a delibrate error thrown from an Angular controller');
    }

    public doShowDialog(): void {
        Raven.showReportDialog();
    }

    public findCategory(key: string): Category {
        switch (key) {
            case 'RESOLVE': {
                return Category.RESOLVE;
            }
            case 'TRANSITION': {
                return Category.TRANSITION;
            }
            case 'HOOK': {
                return Category.HOOK;
            }
            case 'UIVIEW': {
                return Category.UIVIEW;
            }
            default: {
                return Category.VIEWCONFIG;
            }
        }
    }

}
