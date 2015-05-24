onmessage = function(e) {
    buffalo({
        printErr: function(s) { postMessage({ error: s }); },
        print: function(s) { postMessage(s); },
        arguments: e.data
    });
};
