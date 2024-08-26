function hasSalesRole(r) {
    const authHeader = r.headersIn['authorization'];
    if (!authHeader) {
        return false;
    }

    const obj = JSON.parse(Buffer.from(authHeader.split('.')[1], 'base64').toString());
    const roles = obj.realm_access.roles || [];
    return roles.includes("sales");
}

export default {hasSalesRole};