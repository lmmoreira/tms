function hasCompanySalesRole(r) {
    const obj = JSON.parse(Buffer.from(r.headersIn['authorization'].split('.')[1], 'base64').toString());
    const roles = obj.realm_access.roles || [];
    return roles.includes("company-sales");
}

export default {hasCompanySalesRole};