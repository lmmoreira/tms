function hasSalesRole(r) {
    const authHeader = r.headersIn['authorization'];
    if (!authHeader) {
        return false;
    }

    const obj = JSON.parse(Buffer.from(authHeader.split('.')[1], 'base64').toString());
    const roles = obj.realm_access.roles || [];
    return roles.includes("sales");
}

function headers_json(r) {
    return JSON.stringify(r.headersIn)
}

function body_json(r) {
    var body  = r.requestText;

    if (!body) {
        return '{}';
    }

    return body;
}

export default {hasSalesRole, headers_json, body_json};