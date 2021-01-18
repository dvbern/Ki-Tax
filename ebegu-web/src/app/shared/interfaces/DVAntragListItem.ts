import * as moment from 'moment';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';

export interface DVAntragListItem {
    fallNummer?: number;
    dossierId?: string;
    antragId?: string;
    gemeinde?: string;
    status?: string;
    familienName?: string;
    kinder?: string[];
    antragTyp?: string;
    periode?: string;
    aenderungsdatum?: moment.Moment;
    dokumenteHochgeladen?: boolean;
    angebote?: TSBetreuungsangebotTyp[];
    institutionen?: string[];
    verantwortlicheTS?: string;
    verantwortlicheBG?: string;

    hasBesitzer?(): boolean;
}
