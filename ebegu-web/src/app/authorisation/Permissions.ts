import {TSRole} from '../../models/enums/TSRole';
import {Permission} from './Permission';

export const PERMISSIONS: { [k in Permission]: TSRole[] } = {
        [Permission.BENUTZER_EINLADEN]: [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT
        ]
    }
;
