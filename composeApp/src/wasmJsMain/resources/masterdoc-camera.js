(function() {
    var activeSession = null;

    function ensureCameraDom() {
        if (!document.getElementById("masterdoc-camera-overlay")) {
            var overlay = document.createElement("div");
            overlay.id = "masterdoc-camera-overlay";
            overlay.innerHTML =
                '<video id="masterdoc-camera-preview" autoplay muted playsinline webkit-playsinline></video>' +
                '<div id="masterdoc-camera-status">Подключение камеры…</div>' +
                '<div id="masterdoc-camera-hint">Наведите на QR или шильдик</div>' +
                '<button id="masterdoc-camera-back" type="button" aria-label="Назад">‹</button>' +
                '<div id="masterdoc-camera-controls">' +
                '<button id="masterdoc-camera-capture" type="button" aria-label="Сделать снимок" disabled>' +
                '<span class="masterdoc-shutter-inner"></span>' +
                '</button>' +
                '</div>';
            document.body.appendChild(overlay);
        }
        ensureCameraFileInput();
    }

    function isMobileCaptureDevice() {
        var ua = navigator.userAgent || "";
        return /Android|iPhone|iPad|iPod|Mobile/i.test(ua);
    }

    function isLocalDevHost() {
        var host = location.hostname || "";
        return host === "localhost" || host === "127.0.0.1" || host === "[::1]";
    }

    function deliverDevTestPhoto(onSuccess) {
        var canvas = document.createElement("canvas");
        canvas.width = 800;
        canvas.height = 600;
        var ctx = canvas.getContext("2d");
        if (!ctx) return false;
        ctx.fillStyle = "#FBF8F3";
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.fillStyle = "#1A1814";
        ctx.font = "bold 36px system-ui, sans-serif";
        ctx.fillText("MASTERDOC · DEV", 48, 80);
        ctx.fillStyle = "#C2410C";
        ctx.fillRect(48, 120, 320, 320);
        ctx.fillStyle = "#FBF8F3";
        ctx.font = "20px monospace";
        ctx.fillText("QR-STUB-001", 72, 290);
        canvas.toBlob(function(blob) {
            if (!blob) return;
            blob.arrayBuffer().then(function(buffer) {
                console.info("[masterdoc camera] dev test photo", buffer.byteLength, "bytes");
                onSuccess(buffer);
            });
        }, "image/jpeg", 0.92);
        return true;
    }

    function formatCameraError(error) {
        if (!error) return "camera unavailable";
        if (typeof error === "string") return error;
        return error.name ? (error.name + (error.message ? ": " + error.message : "")) : String(error);
    }

    function ensureCameraFileInput() {
        var input = document.getElementById("masterdoc-camera-file-input");
        if (!input) {
            input = document.createElement("input");
            input.id = "masterdoc-camera-file-input";
            input.type = "file";
            input.accept = "image/*";
            input.setAttribute("capture", "environment");
            input.style.display = "none";
            document.body.appendChild(input);
        }
        return input;
    }

    function openNativeCameraFallback(onSuccess, onError) {
        var input = ensureCameraFileInput();
        input.value = "";
        input.onchange = function() {
            var file = input.files && input.files[0];
            input.onchange = null;
            if (!file) {
                onError("cancelled");
                return;
            }
            file.arrayBuffer().then(function(buffer) {
                onSuccess(buffer);
            }).catch(function(err) {
                onError(formatCameraError(err));
            });
        };
        input.click();
    }

    function tryGetUserMedia(constraints) {
        return navigator.mediaDevices.getUserMedia(constraints);
    }

    function requestCameraStream() {
        var attempts = [
            { video: { facingMode: { ideal: "environment" } }, audio: false },
            { video: { facingMode: "environment" }, audio: false },
            { video: { facingMode: "user" }, audio: false },
            { video: true, audio: false }
        ];
        var lastError = null;
        function tryAt(index) {
            if (index >= attempts.length) {
                return Promise.reject(lastError || new Error("camera unavailable"));
            }
            return tryGetUserMedia(attempts[index]).catch(function(error) {
                lastError = error;
                return tryAt(index + 1);
            });
        }
        return tryAt(0);
    }

    function waitForVideoFrame(video) {
        return new Promise(function(resolve, reject) {
            var settled = false;
            function finish(ok, err) {
                if (settled) return;
                settled = true;
                video.removeEventListener("loadeddata", onLoaded);
                video.removeEventListener("playing", onLoaded);
                clearTimeout(timer);
                if (ok) resolve();
                else reject(err || new Error("camera preview timeout"));
            }
            function onLoaded() {
                if (video.videoWidth > 0 && video.videoHeight > 0) finish(true);
            }
            var timer = setTimeout(function() {
                finish(false, new Error("camera preview timeout"));
            }, 12000);
            video.addEventListener("loadeddata", onLoaded);
            video.addEventListener("playing", onLoaded);
            if (video.readyState >= 2 && video.videoWidth > 0) finish(true);
        });
    }

    window.masterdocCaptureCamera = function(onSuccess, onError) {
        ensureCameraDom();
        if (activeSession) {
            activeSession.cleanup();
            activeSession = null;
        }

        if (!window.isSecureContext) {
            if (isMobileCaptureDevice()) {
                openNativeCameraFallback(onSuccess, onError);
            } else {
                onError("insecure-context");
            }
            return;
        }

        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            if (isMobileCaptureDevice()) {
                openNativeCameraFallback(onSuccess, onError);
            } else {
                onError("mediaDevices unavailable");
            }
            return;
        }

        var overlay = document.getElementById("masterdoc-camera-overlay");
        var video = document.getElementById("masterdoc-camera-preview");
        var captureButton = document.getElementById("masterdoc-camera-capture");
        var backButton = document.getElementById("masterdoc-camera-back");
        var stream = null;
        var ready = false;

        document.body.classList.add("masterdoc-camera-open");
        document.body.appendChild(overlay);

        function cleanup() {
            ready = false;
            captureButton.disabled = true;
            captureButton.classList.remove("ready");
            if (stream) {
                stream.getTracks().forEach(function(track) { track.stop(); });
                stream = null;
            }
            video.srcObject = null;
            overlay.classList.remove("active");
            overlay.classList.remove("waiting");
            document.body.classList.remove("masterdoc-camera-open");
            captureButton.onclick = null;
            backButton.onclick = null;
            video.onloadedmetadata = null;
        }

        function failWith(error) {
            cleanup();
            activeSession = null;
            var message = formatCameraError(error);
            console.warn("[masterdoc camera]", message);
            if (isLocalDevHost() && deliverDevTestPhoto(onSuccess)) {
                return;
            }
            openNativeCameraFallback(onSuccess, onError);
        }

        function captureFrame() {
            if (!ready || !stream) {
                onError("camera not ready");
                return;
            }
            var width = video.videoWidth;
            var height = video.videoHeight;
            if (!width || !height) {
                onError("camera has no frame size");
                return;
            }
            var canvas = document.createElement("canvas");
            canvas.width = width;
            canvas.height = height;
            var ctx = canvas.getContext("2d");
            if (!ctx) {
                onError("canvas unavailable");
                return;
            }
            ctx.drawImage(video, 0, 0, width, height);
            cleanup();
            activeSession = null;
            canvas.toBlob(function(blob) {
                if (!blob) {
                    onError("empty photo");
                    return;
                }
                blob.arrayBuffer().then(function(buffer) {
                    onSuccess(buffer);
                }).catch(function(err) {
                    onError(formatCameraError(err));
                });
            }, "image/jpeg", 0.92);
        }

        activeSession = { cleanup: cleanup };

        overlay.classList.add("waiting");
        overlay.classList.add("active");
        captureButton.disabled = true;

        requestCameraStream().then(function(activeStream) {
            stream = activeStream;
            video.srcObject = stream;
            video.setAttribute("playsinline", "true");
            video.setAttribute("webkit-playsinline", "true");
            video.muted = true;
            return video.play();
        }).then(function() {
            return waitForVideoFrame(video);
        }).then(function() {
            ready = true;
            overlay.classList.remove("waiting");
            captureButton.disabled = false;
            captureButton.classList.add("ready");
        }).catch(function(error) {
            if (isLocalDevHost()) {
                ready = true;
                overlay.classList.remove("waiting");
                var status = document.getElementById("masterdoc-camera-status");
                if (status) {
                    status.textContent = "Камера недоступна · тестовый снимок";
                    status.style.display = "flex";
                }
                captureButton.disabled = false;
                captureButton.classList.add("ready");
                captureButton.onclick = function() {
                    cleanup();
                    activeSession = null;
                    deliverDevTestPhoto(onSuccess);
                };
                return;
            }
            failWith(error);
        });

        captureButton.onclick = captureFrame;
        backButton.onclick = function() {
            cleanup();
            activeSession = null;
            onError("cancelled");
        };
    };
})();
