export default {
    randomString: function (len, radix) {
        var CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
        var chars = CHARS,
            uuid = [],
            i;
        radix = radix || chars.length;

        if (len) {
            // Compact form
            for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
        } else {
            // rfc4122, version 4 form
            var r;

            // rfc4122 requires these characters
            uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
            uuid[14] = '4';

            // Fill in random data.  At i==19 set the high bits of clock sequence as
            // per rfc4122, sec. 4.1.5
            for (i = 0; i < 36; i++) {
                if (!uuid[i]) {
                    r = 0 | Math.random() * 16;
                    uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                }
            }
        }

        return uuid.join('');
    },
    remember(form) {
        window.localStorage.setItem('autojs-account', JSON.stringify(form));
    },
    getRemember() {
        let account = window.localStorage.getItem("autojs-account");
        if (account) {
            return JSON.parse(account);
        }
        return null;
    },
    clearRemember() {
        window.localStorage.removeItem("autojs-account");
    },
    login({
        token,
        user
    }) {
        window.sessionStorage.setItem('autojs-token', token);
    },
    getToken() {
        let token = window.sessionStorage.getItem('autojs-token');
        return token;
    },
    logout(clearAcc, cb) {
        if (typeof clearAcc == "boolean" && clearAcc) {
            this.clearRemember();
        }
        window.localStorage.removeItem('autojs-token');
        if (cb) cb()
    }
}
