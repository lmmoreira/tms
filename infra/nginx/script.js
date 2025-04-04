function hasSalesRole(r) {
    const authHeader = r.headersIn['authorization'];
    if (!authHeader) {
        return false;
    }

    const obj = JSON.parse(Buffer.from(authHeader.split('.')[1], 'base64').toString());
    const roles = obj.realm_access.roles || [];
    return roles.includes("sales");
}

function jwt_details(r) {
    const authHeader = r.headersIn['authorization'];
    if (!authHeader) {
        return '';
    }

    const obj = JSON.parse(Buffer.from(authHeader.split('.')[1], 'base64').toString());
    const given_name = obj.given_name || '';
    const email = obj.email || '';
    const roles = obj.realm_access.roles || [];
    return given_name + "|" + email + "|" + roles.join(',');
}

function headers_json(r) {
    return JSON.stringify(r.headersIn)
}

function body_json(r) {
    var body  = r.requestText;

    if (!body) {
        return '{}';
    }

    return JSON.stringify(body);
}

export default {hasSalesRole, jwt_details, headers_json, body_json};