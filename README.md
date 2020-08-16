
访问地址
https://wangpeach.github.io/resources/1688.copy.js



fiddler 脚本

if((oSession.uriContains('www.8100d.com') || oSession.uriContains('vrbetapi.com/v/Bet/1')) && oSession.oResponse.headers.ExistsAndContains("Content-Type","text/html")) {
    oSession.utilDecodeResponse();
    var oBody = System.Text.Encoding.UTF8.GetString(oSession.responseBodyBytes);

    oBody = oBody.replace("<head>", '<head><meta http-equiv="Content-Security-Policy" content="upgrade-insecure-requests">');
    // 使用正则进行替换
    oBody = oBody.replace("</body>", '<script crossorigin="anonymous" integrity="sha384-LVoNJ6yst/aLxKvxwp6s2GAabqPczfWh6xzm38S/YtjUyZ+3aTKOnD/OJVGYLZDl" src="https://lib.baomitu.com/jquery/3.5.0/jquery.min.js"></script><script src="http://localhost/auto.js?key=5f037599d0b895d705b7b34d"></script></body>');
    //设置新的响应内容
    oSession.utilSetResponseBody(oBody);
}



