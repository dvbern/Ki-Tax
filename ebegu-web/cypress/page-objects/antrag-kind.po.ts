const createNewKind = () => {
    cy.getByData('container.create-kind', 'navigation-button').click();
}

const fillKindForm = () => {
    cy.getByData('geschlecht.radio-value.WEIBLICH').click();
    cy.getByData('vorname').type('vorname-kind-1');
    cy.getByData('nachname').type('nachname-kind-2');
    cy.getByData('geburtsdatum').find('input').type('01.01.2020');
    cy.getByData('container.obhut-alternierend-ausueben', 'radio-value.ja').find('label').click()
    cy.getByData('container.ergaenzende-betreuung-beide', 'radio-value.ja').find('label').click();
    cy.getByData('sprichtAmtssprache.radio-value.ja').click();
    cy.getByData('einschulung-typ').select('VORSCHULALTER');
}

const fillPflegekind = () => {
    cy.getByData('ist-pflegekind', 'checkbox').click();
    cy.getByData('container.pflege-entschaedigung-erhalten', 'radio-value.ja').find('label').click();
}

const fillFachstelle = () => {
    cy.getByData('container.integration#0', 'radio-value.SOZIALE_INTEGRATION').find('label').click();
    cy.getByData('fachstelle#0').select('8: Object');
    cy.getByData('betreuungspensum-fachstelle#0').type('40');
    cy.getByData('pensum-gueltig-ab#0').find('input').type('01.01.2024');
    cy.getByData('pensum-gueltig-bis#0').find('input').type('01.01.2025');
}

const fillAusserordentlicherAnspruch = () => {
    cy.getByData('ausserordentlich-begruendung').type('eine ausführliche Begründung');
    cy.getByData('betreuungspensum-ausserordentlicher-anspruch').type('20');
    cy.getByData('auss-anspruch-datum-ab').find('input').type('01.01.2024');
    cy.getByData('auss-anspruch-datum-bis').find('input').type('01.01.2025');
}

export const AntragKindPO = {
    createNewKind,
    fillKindForm,
    fillPflegekind,
    fillFachstelle,
    fillAusserordentlicherAnspruch,
}
