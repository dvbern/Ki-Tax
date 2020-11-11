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

import { MatPaginatorIntl } from '@angular/material/paginator';
import { TranslateService } from '@ngx-translate/core';
import {LogFactory} from '../core/logging/LogFactory';

const LOG = LogFactory.createLog('PaginatorI18n');

/**
 * This class is used to translate the pagination items of a mattable. It extends MatPaginatorIntl what allows
 * us to change the translation every time we need it. This class must be provided in core.module so material
 * can use it
 *
 * Idea taken from
 * https://stackoverflow.com/questions/47593692/how-to-translate-mat-paginator-in-angular4
 * https://stackoverflow.com/questions/46869616/how-to-use-matpaginatorintl
 */
export class PaginatorI18n extends MatPaginatorIntl {

    public constructor(private readonly translate: TranslateService) {
        super();
        this.translate.onLangChange.subscribe(
            () => this.translateLabels(),
            (err: any) => LOG.error(err),
        );

        this.translateLabels();
    }

    private translateLabels(): void {
        this.itemsPerPageLabel = this.translate.instant('ITEMS_PER_PAGE_LABEL');
        this.nextPageLabel = this.translate.instant('NEXT_PAGE_LABEL');
        this.previousPageLabel = this.translate.instant('PREVIOUS_PAGE_LABEL');
        this.firstPageLabel = this.translate.instant('FIRST_PAGE_LABEL');
        this.lastPageLabel = this.translate.instant('LAST_PAGE_LABEL');
        this.getRangeLabel = this.getRangeLabelOverriden.bind(this);
        // forces a reload in all tables
        this.changes.next();
    }

    private getRangeLabelOverriden(page: number, pageSize: number, length: number): string {
        if (length === 0 || pageSize === 0) {
            return this.translate.instant('RANGE_PAGE_LABEL_1', { length });
        }
        const maxLength = Math.max(length, 0);
        const startIndex = page * pageSize;
        // If the start index exceeds the list length, do not try and fix the end index to the end.
        const endIndex = startIndex < maxLength ? Math.min(startIndex + pageSize, maxLength) : startIndex + pageSize;
        return this.translate.instant('RANGE_PAGE_LABEL_2', { startIndex: startIndex + 1, endIndex, maxLength });
    }
}
