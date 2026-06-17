(function() {
    /** Resize/compress before POST /assistants/detect (keeps vision usable, avoids 500/timeouts). */
    window.fixaverseCompressDetectImage = function(input, onReady) {
        var maxDim = 1600;
        var maxBytes = 1800000;
        var quality = 0.82;
        if (!input) {
            onReady(null);
            return;
        }
        var bytes = input.byteLength != null ? input.byteLength : 0;
        if (input.buffer && input.buffer instanceof ArrayBuffer) {
            input = input.buffer;
            bytes = input.byteLength;
        }
        if (!bytes) {
            onReady(null);
            return;
        }
        if (bytes <= maxBytes) {
            onReady(input);
            return;
        }
        var blob = new Blob([input]);
        var url = URL.createObjectURL(blob);
        var img = new Image();
        img.onload = function() {
            var w = img.naturalWidth || img.width;
            var h = img.naturalHeight || img.height;
            if (!w || !h) {
                URL.revokeObjectURL(url);
                onReady(input);
                return;
            }
            var scale = Math.min(1, maxDim / Math.max(w, h));
            var cw = Math.max(1, Math.round(w * scale));
            var ch = Math.max(1, Math.round(h * scale));
            var canvas = document.createElement("canvas");
            canvas.width = cw;
            canvas.height = ch;
            canvas.getContext("2d").drawImage(img, 0, 0, cw, ch);
            URL.revokeObjectURL(url);
            function encode(q) {
                canvas.toBlob(function(out) {
                    if (!out) {
                        onReady(input);
                        return;
                    }
                    if (out.size <= maxBytes || q <= 0.45) {
                        out.arrayBuffer().then(function(buf) {
                            console.info("[fixaverse detect] compressed", input.byteLength, "->", buf.byteLength, "bytes");
                            onReady(buf);
                        });
                    } else {
                        encode(Math.max(0.45, q - 0.08));
                    }
                }, "image/jpeg", q);
            }
            encode(quality);
        };
        img.onerror = function() {
            URL.revokeObjectURL(url);
            console.warn("[fixaverse detect] compress failed, sending original");
            onReady(input);
        };
        img.src = url;
    };

    window.fixaverseReadImageFile = function(file, onReady) {
        if (!file) {
            onReady(null);
            return;
        }
        var reader = new FileReader();
        reader.onload = function() {
            var buffer = reader.result;
            if (!buffer || !buffer.byteLength) {
                onReady(null);
                return;
            }
            fixaverseCompressDetectImage(buffer, onReady);
        };
        reader.onerror = function() {
            console.warn("[fixaverse detect] FileReader error");
            onReady(null);
        };
        reader.readAsArrayBuffer(file);
    };
})();
