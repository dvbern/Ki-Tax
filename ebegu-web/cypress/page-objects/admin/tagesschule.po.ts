import {FixtureCreateTagesschule, FixtureTagesschule} from '@dv-e2e/fixtures';
import {EbeguUtil} from '../../../src/utils/EbeguUtil';


// !! -- PAGE OBJECTS -- !!

const getCreateTagesschuleButton = () => {
	return cy.getByData('institution.create-tagesschule');
};

const getGemeinde = () => {
	return cy.getByData('institution.create-tagesschule.gemeinde-auswaehlen');
};

const getName = () => {
	return cy.getByData('institution.create-tagesschule.institutionsname');
};

const getEmail = () => {
	return cy.getByData('institution.create-tagesschule.email-adresse');
};

const getSubmitButton = () => {
	return cy.getByData('institution.create-tagesschule.submit');
};


// !! -- PAGE ACTIONS -- !!
const createTagesschule = (
    tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
    dataset: keyof typeof FixtureCreateTagesschule,
) => {
    FixtureCreateTagesschule[dataset](data => {
        getCreateTagesschuleButton().click();
        getGemeinde().select(data[tagesschuleArt].gemeinde);
        getName().type(data[tagesschuleArt].name + '-' + EbeguUtil.generateRandomName(3));
        getEmail().type(data[tagesschuleArt].email);
        cy.waitForRequest('POST', '**/institutionen*', () => {
            getSubmitButton().click();
        });
    });
};

export const CreateTagesschulePO = {
    //page objects
    getName,
    getGemeinde,
    getEmail,
    getSubmitButton,
    //page actions
    createTagesschule,
};


// !! -- PAGE OBJECTS -- !!
const getAnschrift = () => {
    return cy.getByData('institution.edit.anschrift');
};

const getStrasse = () => {
    return cy.getByData('institution.edit.strasse');
};

const getHausnummer = () => {
    return cy.getByData('institution.edit.hausnummer');
};

const getPlz = () => {
    return cy.getByData('institution.edit.plz');
};

const getOrt = () => {
    return cy.getByData('institution.edit.ort');
};

const getGueltigAb = () => {
    return cy.getByData('institution.edit.gueltigAb');
};

const getEditSaveButton = () => {
    return cy.getByData('institution.edit.submit');
};

const getCancelButton = () => {
	return cy.getByData('institution.edit.cancel');
};

const getGesuchsperiodeTab = (gesuchsperiodeIndex: number) => {
	return cy.getByData('institution.gesuchsperiode-' + gesuchsperiodeIndex);
};

const getGesuchsperiodeModulTable = (gesuchsperiodeIndex: number) => {
	return cy.getByData('institution.gesuchsperiode.module.table-' + gesuchsperiodeIndex);
};

const getAddModuleButton = (moduleIndex: number) => {
	return cy.getByData('institution.gesuchsperiode.add.modul-' + moduleIndex);
};

const getAddImportModuleButton = (moduleIndex: number) => {
	return cy.getByData('institution.gesuchsperiode.import.modul-' + moduleIndex);
};

// !! -- PAGE ACTIONS -- !!
const editTagesschuleForm = (
tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
dataset: keyof typeof FixtureTagesschule,
) => {
    FixtureTagesschule[dataset](data => {
        getAnschrift().type(data[tagesschuleArt].anschrift);
        getStrasse().type(data[tagesschuleArt].strasse);
        getHausnummer().type(data[tagesschuleArt].hausnummer);
        getPlz().type(data[tagesschuleArt].plz);
        getOrt().type(data[tagesschuleArt].ort);
        getGueltigAb().clear().type(data[tagesschuleArt].gueltigAb);
        cy.waitForRequest('GET', '**/externalclients', () => {
            getEditSaveButton().click();
        });
    });
};

export const EditTagesschulePO = {
    // page objects
    getEditSaveButton: getEditSaveButton,
    getCancelButton,
    getAnschrift,
    getStrasse,
    getHausnummer,
    getOrt,
    getPlz,
    getGueltigAb,

    getGesuchsperiodeTab,
    getGesuchsperiodeModulTable,
    getAddModuleButton,
    getAddImportModuleButton,
    // page actions
    editTagesschuleForm,
};
