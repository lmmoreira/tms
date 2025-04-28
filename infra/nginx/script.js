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

function generateHex(bytesLength) {
    var bytes = crypto.getRandomValues(new Uint8Array(bytesLength));
    var hex = '';
    for (var i = 0; i < bytes.length; i++) {
        hex += ('0' + bytes[i].toString(16)).slice(-2);
    }
    return hex;
}

function traceparent(r) {
    var traceId = generateHex(16);
    var spanId = generateHex(8);
    var traceparent = "00-" + traceId + "-" + spanId + "-01";

    r.log('traceparent: ' + traceparent);

    return traceparent;
}

export default {hasSalesRole, jwt_details, headers_json, body_json, traceparent};