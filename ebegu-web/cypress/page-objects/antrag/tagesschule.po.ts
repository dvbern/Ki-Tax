import {FixtureCreateTagesschule, FixtureTagesschule} from '@dv-e2e/fixtures';

const createTagesschule = (
    tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
    dataset: keyof typeof FixtureCreateTagesschule,
) => {
    FixtureCreateTagesschule[dataset](data => {
        cy.getByData('institution.create-tagesschule').click();
        cy.getByData('institution.create-tagesschule.gemeinde-auswaehlen').select(data[tagesschuleArt].gemeinde);
        cy.getByData('institution.create-tagesschule.institutionsname').type(data[tagesschuleArt].name);
        cy.getByData('institution.create-tagesschule.email-adresse').type(data[tagesschuleArt].email);
        cy.intercept('POST', '**/institutionen?**').as('goingToEdit');
        cy.getByData('institution.create-tagesschule.submit').click();
        cy.wait('@goingToEdit');
    });
}

export const CreateTagesschulePO = {
    createTagesschule,
};

const editTagesschuleForm = (
    tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
    dataset: keyof typeof FixtureTagesschule,
) => {
    FixtureTagesschule[dataset](data => {
        cy.getByData('institution.edit.anschrift').type(data[tagesschuleArt].anschrift);
        cy.getByData('institution.edit.strasse').type(data[tagesschuleArt].strasse);
        cy.getByData('institution.edit.hausnummer').type(data[tagesschuleArt].hausnummer);
        cy.getByData('institution.edit.plz').type(data[tagesschuleArt].plz);
        cy.getByData('institution.edit.ort').type(data[tagesschuleArt].ort);
        cy.getByData('institution.edit.gueltigAb').clear();
        cy.getByData('institution.edit.gueltigAb').type(data[tagesschuleArt].gueltigAb);
        cy.intercept('GET', '**/externalclients').as('loadingClientAfterSave');
        cy.getByData('institution.edit.submit').click();
        cy.wait('@loadingClientAfterSave');
    });

}
export const EditTagesschulePO = {
    editTagesschuleForm,
};
const controllEditTagesschuleForm = (
    tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
    dataset: keyof typeof FixtureTagesschule,
) => {
    FixtureTagesschule[dataset](data => {
        cy.getByData('institution.edit.submit').click();
        cy.getByData('institution.edit.anschrift').should('have.value', data[tagesschuleArt].anschrift);
        cy.getByData('institution.edit.strasse').should('have.value', data[tagesschuleArt].strasse);
        cy.getByData('institution.edit.hausnummer').should('have.value', data[tagesschuleArt].hausnummer);
        cy.getByData('institution.edit.plz').should('have.value', data[tagesschuleArt].plz);
        cy.getByData('institution.edit.ort').should('have.value', data[tagesschuleArt].ort);
        cy.getByData('institution.edit.gueltigAb').should('have.value', data[tagesschuleArt].gueltigAb);
        cy.getByData('institution.edit.submit').click();
    });
}
export const ControllEditTagesschulePO = {
    controllEditTagesschuleForm,
};
