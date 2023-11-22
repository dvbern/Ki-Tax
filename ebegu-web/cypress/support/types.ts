export type Users = Parameters<typeof cy.login>[0];
export const getUser = (user: Users): Users => {
    return user;
}
