
import {TSFile} from './TSFile';
import * as moment from 'moment';

export class TSUploadFile extends TSFile {

    private _timestampUpload: moment.Moment;

    public constructor(timestampUpload?: moment.Moment) {
        super();
        console.log(timestampUpload);
        this._timestampUpload = timestampUpload;
    }

    public get timestampUpload(): moment.Moment {
        return this._timestampUpload;
    }

    public set timestampUpload(value: moment.Moment) {
        this._timestampUpload = value;
    }

}
