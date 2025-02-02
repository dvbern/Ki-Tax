/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges
} from '@angular/core';
import {Moment} from 'moment';
import {TSFile} from '../../../../models/TSFile';
import {DateUtil} from '../../../../utils/DateUtil';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';
import {TSUploadFile} from '../../../../models/TSUploadFile';

export interface HTMLInputEvent extends Event {
    target: HTMLInputElement & EventTarget;
}

@Component({
    selector: 'dv-multiple-file-upload',
    templateUrl: './multiple-file-upload.component.html',
    styleUrls: ['./multiple-file-upload.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MultipleFileUploadComponent<T extends TSFile>
    implements OnChanges, OnInit
{
    @Input() public title: string;
    @Input() public readOnly: boolean;
    @Input() public readOnlyDelete: boolean;
    @Input() public tooltipText: string;
    @Output() public readonly download: EventEmitter<[T, boolean]> =
        new EventEmitter();
    @Output() public readonly delete: EventEmitter<T> = new EventEmitter();
    @Output() public readonly uploadFile: EventEmitter<HTMLInputEvent> =
        new EventEmitter();

    public uploadInputValue: string = '';
    @Input() public files: TSUploadFile[];

    public allowedMimetypes: string = '';

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) {}

    public ngOnInit(): void {
        this.applicationPropertyRS.getAllowedMimetypes().then(response => {
            if (response !== undefined) {
                this.allowedMimetypes = response;
            }
        });
    }

    public onDownload(file: T, attachment: boolean): void {
        this.download.emit([file, attachment]);
    }

    public onDelete(file: T): void {
        this.delete.emit(file);
    }

    public onUploadFile(event: Event): void {
        this.uploadFile.emit(event as HTMLInputEvent);
        // reset the value of the input field to allow multiple uploads of a file with the same name
        this.uploadInputValue = null;
    }

    public formatDate(timestampUpload: Moment): string {
        return DateUtil.momentToLocalDateFormat(timestampUpload, 'DD.MM.YYYY');
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.files) {
            this.files = changes.files.currentValue;
        }
    }

    public click(fileInput: HTMLInputElement): void {
        if (!this.readOnly) {
            fileInput.click();
        }
    }
}
