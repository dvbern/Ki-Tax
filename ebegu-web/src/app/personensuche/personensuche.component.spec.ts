import * as moment from 'moment';
import {TSEWKAdresse} from '../../models/TSEWKAdresse';
import {TSEWKPerson} from '../../models/TSEWKPerson';

import {PersonensucheComponent} from './personensuche.component';

describe('PersonensucheComponent', () => {
  let component: PersonensucheComponent;

  beforeEach(async () => {
    component = new PersonensucheComponent(null, null, null);
  });

describe('getShortDescription', () => {
    it('must produce a short description of a person without address', () => {
        const personWithAdress = new TSEWKPerson('id', 'nachname', 'vorname', moment('2022-10-11'));

        expect(component.getShortDescription(personWithAdress)).toEqual('vorname, nachname, 11.10.2022');
    });

    it('must produce a short description of a person with an address', () => {
        const personWithAdress = new TSEWKPerson('id', 'nachname', 'vorname', moment('2022-10-11'));
        personWithAdress.adresse = new TSEWKAdresse(undefined, undefined, '23', undefined, 'Strasse', '3360', 'Ort');

        expect(component.getShortDescription(personWithAdress)).toEqual('vorname, nachname, 11.10.2022, Ort');
    });
});
});
