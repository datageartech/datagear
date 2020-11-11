/*
 * Copyright (c) 2008-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* CometD Version ${project.version} */

(function(root, factory) {
    if (typeof exports === 'object') {
        // CommonJS.
        module.exports = factory();
    } else if (typeof define === 'function' && define.amd) {
        // AMD.
        define([], factory);
    } else {
        // Globals.
        root.org = root.org || {};
        root.org.cometd = factory();
    }
}(this, function() {
    /**
     * Browsers may throttle the Window scheduler,
     * so we may replace it with a Worker scheduler.
     */
    var Scheduler = function() {
        var _ids = 0;
        var _tasks = {};
        this.register = function(funktion) {
            var id = ++_ids;
            _tasks[id] = funktion;
            return id;
        };
        this.unregister = function(id) {
            var funktion = _tasks[id];
            delete _tasks[id];
            return funktion;
        };
        this.setTimeout = function(funktion, delay) {
            return window.setTimeout(funktion, delay);
        };
        this.clearTimeout = function(id) {
            window.clearTimeout(id);
        };
    };

    /**
     * The scheduler code that will run in the Worker.
     * Workers have a built-in `self` variable similar to `window`.
     */
    function WorkerScheduler() {
        var _tasks = {};
        self.onmessage = function(e) {
            var cmd = e.data;
            var id = _tasks[cmd.id];
            switch (cmd.type) {
                case 'setTimeout':
                    _tasks[cmd.id] = self.setTimeout(function() {
                        delete _tasks[cmd.id];
                        self.postMessage({
                            id: cmd.id
                        });
                    }, cmd.delay);
                    break;
                case 'clearTimeout':
                    delete _tasks[cmd.id];
                    if (id) {
                        self.clearTimeout(id);
                    }
                    break;
                default:
                    throw 'Unknown command ' + cmd.type;
            }
        };
    }


    /**
     * Utility functions.
     */
    var Utils = {
        isString: function(value) {
            if (value === undefined || value === null) {
                return false;
            }
            return typeof value === 'string' || value instanceof String;
        },
        isArray: function(value) {
            if (value === undefined || value === null) {
                return false;
            }
            return value instanceof Array;
        },
        /**
         * Returns whether the given element is contained into the given array.
         * @param element the element to check presence for
         * @param array the array to check for the element presence
         * @return the index of the element, if present, or a negative index if the element is not present
         */
        inArray: function(element, array) {
            for (var i = 0; i < array.length; ++i) {
                if (element === array[i]) {
                    return i;
                }
            }
            return -1;
        }
    };


    /**
     * A registry for transports used by the CometD object.
     */
    var TransportRegistry = function() {
        var _types = [];
        var _transports = {};

        this.getTransportTypes = function() {
            return _types.slice(0);
        };

        this.findTransportTypes = function(version, crossDomain, url) {
            var result = [];
            for (var i = 0; i < _types.length; ++i) {
                var type = _types[i];
                if (_transports[type].accept(version, crossDomain, url) === true) {
                    result.push(type);
                }
            }
            return result;
        };

        this.negotiateTransport = function(types, version, crossDomain, url) {
            for (var i = 0; i < _types.length; ++i) {
                var type = _types[i];
                for (var j = 0; j < types.length; ++j) {
                    if (type === types[j]) {
                        var transport = _transports[type];
                        if (transport.accept(version, crossDomain, url) === true) {
                            return transport;
                        }
                    }
                }
            }
            return null;
        };

        this.add = function(type, transport, index) {
            var existing = false;
            for (var i = 0; i < _types.length; ++i) {
                if (_types[i] === type) {
                    existing = true;
                    break;
                }
            }

            if (!existing) {
                if (typeof index !== 'number') {
                    _types.push(type);
                } else {
                    _types.splice(index, 0, type);
                }
                _transports[type] = transport;
            }

            return !existing;
        };

        this.find = function(type) {
            for (var i = 0; i < _types.length; ++i) {
                if (_types[i] === type) {
                    return _transports[type];
                }
            }
            return null;
        };

        this.remove = function(type) {
            for (var i = 0; i < _types.length; ++i) {
                if (_types[i] === type) {
                    _types.splice(i, 1);
                    var transport = _transports[type];
                    delete _transports[type];
                    return transport;
                }
            }
            return null;
        };

        this.clear = function() {
            _types = [];
            _transports = {};
        };

        this.reset = function(init) {
            for (var i = 0; i < _types.length; ++i) {
                _transports[_types[i]].reset(init);
            }
        };
    };


    /**
     * Base object with the common functionality for transports.
     */
    var Transport = function() {
        var _type;
        var _cometd;
        var _url;

        /**
         * Function invoked just after a transport has been successfully registered.
         * @param type the type of transport (for example 'long-polling')
         * @param cometd the cometd object this transport has been registered to
         * @see #unregistered()
         */
        this.registered = function(type, cometd) {
            _type = type;
            _cometd = cometd;
        };

        /**
         * Function invoked just after a transport has been successfully unregistered.
         * @see #registered(type, cometd)
         */
        this.unregistered = function() {
            _type = null;
            _cometd = null;
        };

        this._debug = function() {
            _cometd._debug.apply(_cometd, arguments);
        };

        this._mixin = function() {
            return _cometd._mixin.apply(_cometd, arguments);
        };

        this.getConfiguration = function() {
            return _cometd.getConfiguration();
        };

        this.getAdvice = function() {
            return _cometd.getAdvice();
        };

        this.setTimeout = function(funktion, delay) {
            return _cometd.setTimeout(funktion, delay);
        };

        this.clearTimeout = function(id) {
            _cometd.clearTimeout(id);
        };

        /**
         * Converts the given response into an array of bayeux messages
         * @param response the response to convert
         * @return an array of bayeux messages obtained by converting the response
         */
        this.convertToMessages = function(response) {
            if (Utils.isString(response)) {
                try {
                    return JSON.parse(response);
                } catch (x) {
                    this._debug('Could not convert to JSON the following string', '"' + response + '"');
                    throw x;
                }
            }
            if (Utils.isArray(response)) {
                return response;
            }
            if (response === undefined || response === null) {
                return [];
            }
            if (response instanceof Object) {
                return [response];
            }
            throw 'Conversion Error ' + response + ', typeof ' + (typeof response);
        };

        /**
         * Returns whether this transport can work for the given version and cross domain communication case.
         * @param version a string indicating the transport version
         * @param crossDomain a boolean indicating whether the communication is cross domain
         * @param url the URL to connect to
         * @return true if this transport can work for the given version and cross domain communication case,
         * false otherwise
         */
        this.accept = function(version, crossDomain, url) {
            throw 'Abstract';
        };

        /**
         * Returns the type of this transport.
         * @see #registered(type, cometd)
         */
        this.getType = function() {
            return _type;
        };

        this.getURL = function() {
            return _url;
        };

        this.setURL = function(url) {
            _url = url;
        };

        this.send = function(envelope, metaConnect) {
            throw 'Abstract';
        };

        this.reset = function(init) {
            this._debug('Transport', _type, 'reset', init ? 'initial' : 'retry');
        };

        this.abort = function() {
            this._debug('Transport', _type, 'aborted');
        };

        this.toString = function() {
            return this.getType();
        };
    };

    Transport.derive = function(baseObject) {
        function F() {
        }

        F.prototype = baseObject;
        return new F();
    };


    /**
     * Base object with the common functionality for transports based on requests.
     * The key responsibility is to allow at most 2 outstanding requests to the server,
     * to avoid that requests are sent behind a long poll.
     * To achieve this, we have one reserved request for the long poll, and all other
     * requests are serialized one after the other.
     */
    var RequestTransport = function() {
        var _super = new Transport();
        var _self = Transport.derive(_super);
        var _requestIds = 0;
        var _metaConnectRequest = null;
        var _requests = [];
        var _envelopes = [];

        function _coalesceEnvelopes(envelope) {
            while (_envelopes.length > 0) {
                var envelopeAndRequest = _envelopes[0];
                var newEnvelope = envelopeAndRequest[0];
                var newRequest = envelopeAndRequest[1];
                if (newEnvelope.url === envelope.url &&
                    newEnvelope.sync === envelope.sync) {
                    _envelopes.shift();
                    envelope.messages = envelope.messages.concat(newEnvelope.messages);
                    this._debug('Coalesced', newEnvelope.messages.length, 'messages from request', newRequest.id);
                    continue;
                }
                break;
            }
        }

        function _transportSend(envelope, request) {
            this.transportSend(envelope, request);
            request.expired = false;

            if (!envelope.sync) {
                var maxDelay = this.getConfiguration().maxNetworkDelay;
                var delay = maxDelay;
                if (request.metaConnect === true) {
                    delay += this.getAdvice().timeout;
                }

                this._debug('Transport', this.getType(), 'waiting at most', delay, 'ms for the response, maxNetworkDelay', maxDelay);

                var self = this;
                request.timeout = this.setTimeout(function() {
                    request.expired = true;
                    var errorMessage = 'Request ' + request.id + ' of transport ' + self.getType() + ' exceeded ' + delay + ' ms max network delay';
                    var failure = {
                        reason: errorMessage
                    };
                    var xhr = request.xhr;
                    failure.httpCode = self.xhrStatus(xhr);
                    self.abortXHR(xhr);
                    self._debug(errorMessage);
                    self.complete(request, false, request.metaConnect);
                    envelope.onFailure(xhr, envelope.messages, failure);
                }, delay);
            }
        }

        function _queueSend(envelope) {
            var requestId = ++_requestIds;
            var request = {
                id: requestId,
                metaConnect: false,
                envelope: envelope
            };

            // Consider the /meta/connect requests which should always be present.
            if (_requests.length < this.getConfiguration().maxConnections - 1) {
                _requests.push(request);
                _transportSend.call(this, envelope, request);
            } else {
                this._debug('Transport', this.getType(), 'queueing request', requestId, 'envelope', envelope);
                _envelopes.push([envelope, request]);
            }
        }

        function _metaConnectComplete(request) {
            var requestId = request.id;
            this._debug('Transport', this.getType(), '/meta/connect complete, request', requestId);
            if (_metaConnectRequest !== null && _metaConnectRequest.id !== requestId) {
                throw '/meta/connect request mismatch, completing request ' + requestId;
            }
            _metaConnectRequest = null;
        }

        function _complete(request, success) {
            var index = Utils.inArray(request, _requests);
            // The index can be negative if the request has been aborted
            if (index >= 0) {
                _requests.splice(index, 1);
            }

            if (_envelopes.length > 0) {
                var envelopeAndRequest = _envelopes.shift();
                var nextEnvelope = envelopeAndRequest[0];
                var nextRequest = envelopeAndRequest[1];
                this._debug('Transport dequeued request', nextRequest.id);
                if (success) {
                    if (this.getConfiguration().autoBatch) {
                        _coalesceEnvelopes.call(this, nextEnvelope);
                    }
                    _queueSend.call(this, nextEnvelope);
                    this._debug('Transport completed request', request.id, nextEnvelope);
                } else {
                    // Keep the semantic of calling response callbacks asynchronously after the request
                    var self = this;
                    this.setTimeout(function() {
                        self.complete(nextRequest, false, nextRequest.metaConnect);
                        var failure = {
                            reason: 'Previous request failed'
                        };
                        var xhr = nextRequest.xhr;
                        failure.httpCode = self.xhrStatus(xhr);
                        nextEnvelope.onFailure(xhr, nextEnvelope.messages, failure);
                    }, 0);
                }
            }
        }

        _self.complete = function(request, success, metaConnect) {
            if (metaConnect) {
                _metaConnectComplete.call(this, request);
            } else {
                _complete.call(this, request, success);
            }
        };

        /**
         * Performs the actual send depending on the transport type details.
         * @param envelope the envelope to send
         * @param request the request information
         */
        _self.transportSend = function(envelope, request) {
            throw 'Abstract';
        };

        _self.transportSuccess = function(envelope, request, responses) {
            if (!request.expired) {
                this.clearTimeout(request.timeout);
                this.complete(request, true, request.metaConnect);
                if (responses && responses.length > 0) {
                    envelope.onSuccess(responses);
                } else {
                    envelope.onFailure(request.xhr, envelope.messages, {
                        httpCode: 204
                    });
                }
            }
        };

        _self.transportFailure = function(envelope, request, failure) {
            if (!request.expired) {
                this.clearTimeout(request.timeout);
                this.complete(request, false, request.metaConnect);
                envelope.onFailure(request.xhr, envelope.messages, failure);
            }
        };

        function _metaConnectSend(envelope) {
            if (_metaConnectRequest !== null) {
                throw 'Concurrent /meta/connect requests not allowed, request id=' + _metaConnectRequest.id + ' not yet completed';
            }

            var requestId = ++_requestIds;
            this._debug('Transport', this.getType(), '/meta/connect send, request', requestId, 'envelope', envelope);
            var request = {
                id: requestId,
                metaConnect: true,
                envelope: envelope
            };
            _transportSend.call(this, envelope, request);
            _metaConnectRequest = request;
        }

        _self.send = function(envelope, metaConnect) {
            if (metaConnect) {
                _metaConnectSend.call(this, envelope);
            } else {
                _queueSend.call(this, envelope);
            }
        };

        _self.abort = function() {
            _super.abort();
            for (var i = 0; i < _requests.length; ++i) {
                var request = _requests[i];
                if (request) {
                    this._debug('Aborting request', request);
                    if (!this.abortXHR(request.xhr)) {
                        this.transportFailure(request.envelope, request, {reason: 'abort'});
                    }
                }
            }
            var metaConnectRequest = _metaConnectRequest;
            if (metaConnectRequest) {
                this._debug('Aborting /meta/connect request', metaConnectRequest);
                if (!this.abortXHR(metaConnectRequest.xhr)) {
                    this.transportFailure(metaConnectRequest.envelope, metaConnectRequest, {reason: 'abort'});
                }
            }
            this.reset(true);
        };

        _self.reset = function(init) {
            _super.reset(init);
            _metaConnectRequest = null;
            _requests = [];
            _envelopes = [];
        };

        _self.abortXHR = function(xhr) {
            if (xhr) {
                try {
                    var state = xhr.readyState;
                    xhr.abort();
                    return state !== window.XMLHttpRequest.UNSENT;
                } catch (x) {
                    this._debug(x);
                }
            }
            return false;
        };

        _self.xhrStatus = function(xhr) {
            if (xhr) {
                try {
                    return xhr.status;
                } catch (x) {
                    this._debug(x);
                }
            }
            return -1;
        };

        return _self;
    };


    var LongPollingTransport = function() {
        var _super = new RequestTransport();
        var _self = Transport.derive(_super);
        // By default, support cross domain
        var _supportsCrossDomain = true;

        _self.accept = function(version, crossDomain, url) {
            return _supportsCrossDomain || !crossDomain;
        };

        _self.newXMLHttpRequest = function() {
            return new window.XMLHttpRequest();
        };

        function _copyContext(xhr) {
            try {
                // Copy external context, to be used in other environments.
                xhr.context = _self.context;
            } catch (e) {
                // May happen if XHR is wrapped by Object.seal(),
                // Object.freeze(), or Object.preventExtensions().
                this._debug('Could not copy transport context into XHR', e);
            }
        }

        _self.xhrSend = function(packet) {
            var xhr = _self.newXMLHttpRequest();
            _copyContext(xhr);
            xhr.withCredentials = true;
            xhr.open('POST', packet.url, packet.sync !== true);
            var headers = packet.headers;
            if (headers) {
                for (var headerName in headers) {
                    if (headers.hasOwnProperty(headerName)) {
                        xhr.setRequestHeader(headerName, headers[headerName]);
                    }
                }
            }
            xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
            xhr.onload = function() {
                if (xhr.status === 200) {
                    packet.onSuccess(xhr.responseText);
                } else {
                    packet.onError(xhr.statusText);
                }
            };
            xhr.onabort = xhr.onerror = function() {
                packet.onError(xhr.statusText);
            };
            xhr.send(packet.body);
            return xhr;
        };

        _self.transportSend = function(envelope, request) {
            this._debug('Transport', this.getType(), 'sending request', request.id, 'envelope', envelope);

            var self = this;
            try {
                var sameStack = true;
                request.xhr = this.xhrSend({
                    transport: this,
                    url: envelope.url,
                    sync: envelope.sync,
                    headers: this.getConfiguration().requestHeaders,
                    body: JSON.stringify(envelope.messages),
                    onSuccess: function(response) {
                        self._debug('Transport', self.getType(), 'received response', response);
                        var success = false;
                        try {
                            var received = self.convertToMessages(response);
                            if (received.length === 0) {
                                _supportsCrossDomain = false;
                                self.transportFailure(envelope, request, {
                                    httpCode: 204
                                });
                            } else {
                                success = true;
                                self.transportSuccess(envelope, request, received);
                            }
                        } catch (x) {
                            self._debug(x);
                            if (!success) {
                                _supportsCrossDomain = false;
                                var failure = {
                                    exception: x
                                };
                                failure.httpCode = self.xhrStatus(request.xhr);
                                self.transportFailure(envelope, request, failure);
                            }
                        }
                    },
                    onError: function(reason, exception) {
                        self._debug('Transport', self.getType(), 'received error', reason, exception);
                        _supportsCrossDomain = false;
                        var failure = {
                            reason: reason,
                            exception: exception
                        };
                        failure.httpCode = self.xhrStatus(request.xhr);
                        if (sameStack) {
                            // Keep the semantic of calling response callbacks asynchronously after the request
                            self.setTimeout(function() {
                                self.transportFailure(envelope, request, failure);
                            }, 0);
                        } else {
                            self.transportFailure(envelope, request, failure);
                        }
                    }
                });
                sameStack = false;
            } catch (x) {
                _supportsCrossDomain = false;
                // Keep the semantic of calling response callbacks asynchronously after the request
                this.setTimeout(function() {
                    self.transportFailure(envelope, request, {
                        exception: x
                    });
                }, 0);
            }
        };

        _self.reset = function(init) {
            _super.reset(init);
            _supportsCrossDomain = true;
        };

        return _self;
    };


    var CallbackPollingTransport = function() {
        var _super = new RequestTransport();
        var _self = Transport.derive(_super);
        var jsonp = 0;

        _self.accept = function(version, crossDomain, url) {
            return true;
        };

        _self.jsonpSend = function(packet) {
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');

            var callbackName = '_cometd_jsonp_' + jsonp++;
            window[callbackName] = function(responseText) {
                head.removeChild(script);
                delete window[callbackName];
                packet.onSuccess(responseText);
            };

            var url = packet.url;
            url += url.indexOf('?') < 0 ? '?' : '&';
            url += 'jsonp=' + callbackName;
            url += '&message=' + encodeURIComponent(packet.body);
            script.src = url;
            script.async = packet.sync !== true;
            script.type = 'application/javascript';
            script.onerror = function(e) {
                packet.onError('jsonp ' + e.type);
            };
            head.appendChild(script);
        };

        function _failTransportFn(envelope, request, x) {
            var self = this;
            return function() {
                self.transportFailure(envelope, request, 'error', x);
            };
        }

        _self.transportSend = function(envelope, request) {
            var self = this;

            // Microsoft Internet Explorer has a 2083 URL max length
            // We must ensure that we stay within that length
            var start = 0;
            var length = envelope.messages.length;
            var lengths = [];
            while (length > 0) {
                // Encode the messages because all brackets, quotes, commas, colons, etc
                // present in the JSON will be URL encoded, taking many more characters
                var json = JSON.stringify(envelope.messages.slice(start, start + length));
                var urlLength = envelope.url.length + encodeURI(json).length;

                var maxLength = this.getConfiguration().maxURILength;
                if (urlLength > maxLength) {
                    if (length === 1) {
                        var x = 'Bayeux message too big (' + urlLength + ' bytes, max is ' + maxLength + ') ' +
                            'for transport ' + this.getType();
                        // Keep the semantic of calling response callbacks asynchronously after the request
                        this.setTimeout(_failTransportFn.call(this, envelope, request, x), 0);
                        return;
                    }

                    --length;
                    continue;
                }

                lengths.push(length);
                start += length;
                length = envelope.messages.length - start;
            }

            // Here we are sure that the messages can be sent within the URL limit

            var envelopeToSend = envelope;
            if (lengths.length > 1) {
                var begin = 0;
                var end = lengths[0];
                this._debug('Transport', this.getType(), 'split', envelope.messages.length, 'messages into', lengths.join(' + '));
                envelopeToSend = this._mixin(false, {}, envelope);
                envelopeToSend.messages = envelope.messages.slice(begin, end);
                envelopeToSend.onSuccess = envelope.onSuccess;
                envelopeToSend.onFailure = envelope.onFailure;

                for (var i = 1; i < lengths.length; ++i) {
                    var nextEnvelope = this._mixin(false, {}, envelope);
                    begin = end;
                    end += lengths[i];
                    nextEnvelope.messages = envelope.messages.slice(begin, end);
                    nextEnvelope.onSuccess = envelope.onSuccess;
                    nextEnvelope.onFailure = envelope.onFailure;
                    this.send(nextEnvelope, request.metaConnect);
                }
            }

            this._debug('Transport', this.getType(), 'sending request', request.id, 'envelope', envelopeToSend);

            try {
                var sameStack = true;
                this.jsonpSend({
                    transport: this,
                    url: envelopeToSend.url,
                    sync: envelopeToSend.sync,
                    headers: this.getConfiguration().requestHeaders,
                    body: JSON.stringify(envelopeToSend.messages),
                    onSuccess: function(responses) {
                        var success = false;
                        try {
                            var received = self.convertToMessages(responses);
                            if (received.length === 0) {
                                self.transportFailure(envelopeToSend, request, {
                                    httpCode: 204
                                });
                            } else {
                                success = true;
                                self.transportSuccess(envelopeToSend, request, received);
                            }
                        } catch (x) {
                            self._debug(x);
                            if (!success) {
                                self.transportFailure(envelopeToSend, request, {
                                    exception: x
                                });
                            }
                        }
                    },
                    onError: function(reason, exception) {
                        var failure = {
                            reason: reason,
                            exception: exception
                        };
                        if (sameStack) {
                            // Keep the semantic of calling response callbacks asynchronously after the request
                            self.setTimeout(function() {
                                self.transportFailure(envelopeToSend, request, failure);
                            }, 0);
                        } else {
                            self.transportFailure(envelopeToSend, request, failure);
                        }
                    }
                });
                sameStack = false;
            } catch (xx) {
                // Keep the semantic of calling response callbacks asynchronously after the request
                this.setTimeout(function() {
                    self.transportFailure(envelopeToSend, request, {
                        exception: xx
                    });
                }, 0);
            }
        };

        return _self;
    };


    var WebSocketTransport = function() {
        var _super = new Transport();
        var _self = Transport.derive(_super);
        var _cometd;
        // By default WebSocket is supported
        var _webSocketSupported = true;
        // Whether we were able to establish a WebSocket connection
        var _webSocketConnected = false;
        var _stickyReconnect = true;
        // The context contains the envelopes that have been sent
        // and the timeouts for the messages that have been sent.
        var _context = null;
        var _connecting = null;
        var _connected = false;
        var _successCallback = null;

        _self.reset = function(init) {
            _super.reset(init);
            _webSocketSupported = true;
            if (init) {
                _webSocketConnected = false;
            }
            _stickyReconnect = true;
            _context = null;
            _connecting = null;
            _connected = false;
        };

        function _forceClose(context, event) {
            if (context) {
                this.webSocketClose(context, event.code, event.reason);
                // Force immediate failure of pending messages to trigger reconnect.
                // This is needed because the server may not reply to our close()
                // and therefore the onclose function is never called.
                this.onClose(context, event);
            }
        }

        function _sameContext(context) {
            return context === _connecting || context === _context;
        }

        function _storeEnvelope(context, envelope, metaConnect) {
            var messageIds = [];
            for (var i = 0; i < envelope.messages.length; ++i) {
                var message = envelope.messages[i];
                if (message.id) {
                    messageIds.push(message.id);
                }
            }
            context.envelopes[messageIds.join(',')] = [envelope, metaConnect];
            this._debug('Transport', this.getType(), 'stored envelope, envelopes', context.envelopes);
        }

        function _websocketConnect(context) {
            // We may have multiple attempts to open a WebSocket
            // connection, for example a /meta/connect request that
            // may take time, along with a user-triggered publish.
            // Early return if we are already connecting.
            if (_connecting) {
                return;
            }

            // Mangle the URL, changing the scheme from 'http' to 'ws'.
            var url = _cometd.getURL().replace(/^http/, 'ws');
            this._debug('Transport', this.getType(), 'connecting to URL', url);

            try {
                var protocol = _cometd.getConfiguration().protocol;
                context.webSocket = protocol ? new window.WebSocket(url, protocol) : new window.WebSocket(url);
                _connecting = context;
            } catch (x) {
                _webSocketSupported = false;
                this._debug('Exception while creating WebSocket object', x);
                throw x;
            }

            // By default use sticky reconnects.
            _stickyReconnect = _cometd.getConfiguration().stickyReconnect !== false;

            var self = this;
            var connectTimeout = _cometd.getConfiguration().connectTimeout;
            if (connectTimeout > 0) {
                context.connectTimer = this.setTimeout(function() {
                    _cometd._debug('Transport', self.getType(), 'timed out while connecting to URL', url, ':', connectTimeout, 'ms');
                    // The connection was not opened, close anyway.
                    _forceClose.call(self, context, {code: 1000, reason: 'Connect Timeout'});
                }, connectTimeout);
            }

            var onopen = function() {
                _cometd._debug('WebSocket onopen', context);
                if (context.connectTimer) {
                    self.clearTimeout(context.connectTimer);
                }

                if (_sameContext(context)) {
                    _connecting = null;
                    _context = context;
                    _webSocketConnected = true;
                    self.onOpen(context);
                } else {
                    // We have a valid connection already, close this one.
                    _cometd._warn('Closing extra WebSocket connection', this, 'active connection', _context);
                    _forceClose.call(self, context, {code: 1000, reason: 'Extra Connection'});
                }
            };

            // This callback is invoked when the server sends the close frame.
            // The close frame for a connection may arrive *after* another
            // connection has been opened, so we must make sure that actions
            // are performed only if it's the same connection.
            var onclose = function(event) {
                event = event || {code: 1000};
                _cometd._debug('WebSocket onclose', context, event, 'connecting', _connecting, 'current', _context);

                if (context.connectTimer) {
                    self.clearTimeout(context.connectTimer);
                }

                self.onClose(context, event);
            };

            var onmessage = function(wsMessage) {
                _cometd._debug('WebSocket onmessage', wsMessage, context);
                self.onMessage(context, wsMessage);
            };

            context.webSocket.onopen = onopen;
            context.webSocket.onclose = onclose;
            context.webSocket.onerror = function() {
                // Clients should call onclose(), but if they do not we do it here for safety.
                onclose({code: 1000, reason: 'Error'});
            };
            context.webSocket.onmessage = onmessage;

            this._debug('Transport', this.getType(), 'configured callbacks on', context);
        }

        function _webSocketSend(context, envelope, metaConnect) {
            var json = JSON.stringify(envelope.messages);
            context.webSocket.send(json);
            this._debug('Transport', this.getType(), 'sent', envelope, '/meta/connect =', metaConnect);

            // Manage the timeout waiting for the response.
            var maxDelay = this.getConfiguration().maxNetworkDelay;
            var delay = maxDelay;
            if (metaConnect) {
                delay += this.getAdvice().timeout;
                _connected = true;
            }

            var self = this;
            var messageIds = [];
            for (var i = 0; i < envelope.messages.length; ++i) {
                (function() {
                    var message = envelope.messages[i];
                    if (message.id) {
                        messageIds.push(message.id);
                        context.timeouts[message.id] = self.setTimeout(function() {
                            _cometd._debug('Transport', self.getType(), 'timing out message', message.id, 'after', delay, 'on', context);
                            _forceClose.call(self, context, {code: 1000, reason: 'Message Timeout'});
                        }, delay);
                    }
                })();
            }

            this._debug('Transport', this.getType(), 'waiting at most', delay, 'ms for messages', messageIds, 'maxNetworkDelay', maxDelay, ', timeouts:', context.timeouts);
        }

        _self._notifySuccess = function(fn, messages) {
            fn.call(this, messages);
        };

        _self._notifyFailure = function(fn, context, messages, failure) {
            fn.call(this, context, messages, failure);
        };

        function _send(context, envelope, metaConnect) {
            try {
                if (context === null) {
                    context = _connecting || {
                        envelopes: {},
                        timeouts: {}
                    };
                    _storeEnvelope.call(this, context, envelope, metaConnect);
                    _websocketConnect.call(this, context);
                } else {
                    _storeEnvelope.call(this, context, envelope, metaConnect);
                    _webSocketSend.call(this, context, envelope, metaConnect);
                }
            } catch (x) {
                // Keep the semantic of calling response callbacks asynchronously after the request.
                var self = this;
                this.setTimeout(function() {
                    _forceClose.call(self, context, {
                        code: 1000,
                        reason: 'Exception',
                        exception: x
                    });
                }, 0);
            }
        }

        _self.onOpen = function(context) {
            var envelopes = context.envelopes;
            this._debug('Transport', this.getType(), 'opened', context, 'pending messages', envelopes);
            for (var key in envelopes) {
                if (envelopes.hasOwnProperty(key)) {
                    var element = envelopes[key];
                    var envelope = element[0];
                    var metaConnect = element[1];
                    // Store the success callback, which is independent from the envelope,
                    // so that it can be used to notify arrival of messages.
                    _successCallback = envelope.onSuccess;
                    _webSocketSend.call(this, context, envelope, metaConnect);
                }
            }
        };

        _self.onMessage = function(context, wsMessage) {
            this._debug('Transport', this.getType(), 'received websocket message', wsMessage, context);

            var close = false;
            var messages = this.convertToMessages(wsMessage.data);
            var messageIds = [];
            for (var i = 0; i < messages.length; ++i) {
                var message = messages[i];

                // Detect if the message is a response to a request we made.
                // If it's a meta message, for sure it's a response; otherwise it's
                // a publish message and publish responses don't have the data field.
                if (/^\/meta\//.test(message.channel) || message.data === undefined) {
                    if (message.id) {
                        messageIds.push(message.id);

                        var timeout = context.timeouts[message.id];
                        if (timeout) {
                            this.clearTimeout(timeout);
                            delete context.timeouts[message.id];
                            this._debug('Transport', this.getType(), 'removed timeout for message', message.id, ', timeouts', context.timeouts);
                        }
                    }
                }

                if ('/meta/connect' === message.channel) {
                    _connected = false;
                }
                if ('/meta/disconnect' === message.channel && !_connected) {
                    close = true;
                }
            }

            // Remove the envelope corresponding to the messages.
            var removed = false;
            var envelopes = context.envelopes;
            for (var j = 0; j < messageIds.length; ++j) {
                var id = messageIds[j];
                for (var key in envelopes) {
                    if (envelopes.hasOwnProperty(key)) {
                        var ids = key.split(',');
                        var index = Utils.inArray(id, ids);
                        if (index >= 0) {
                            removed = true;
                            ids.splice(index, 1);
                            var envelope = envelopes[key][0];
                            var metaConnect = envelopes[key][1];
                            delete envelopes[key];
                            if (ids.length > 0) {
                                envelopes[ids.join(',')] = [envelope, metaConnect];
                            }
                            break;
                        }
                    }
                }
            }
            if (removed) {
                this._debug('Transport', this.getType(), 'removed envelope, envelopes', envelopes);
            }

            this._notifySuccess(_successCallback, messages);

            if (close) {
                this.webSocketClose(context, 1000, 'Disconnect');
            }
        };

        _self.onClose = function(context, event) {
            this._debug('Transport', this.getType(), 'closed', context, event);

            if (_sameContext(context)) {
                // Remember if we were able to connect.
                // This close event could be due to server shutdown,
                // and if it restarts we want to try websocket again.
                _webSocketSupported = _stickyReconnect && _webSocketConnected;
                _connecting = null;
                _context = null;
            }

            var timeouts = context.timeouts;
            context.timeouts = {};
            for (var id in timeouts) {
                if (timeouts.hasOwnProperty(id)) {
                    this.clearTimeout(timeouts[id]);
                }
            }

            var envelopes = context.envelopes;
            context.envelopes = {};
            for (var key in envelopes) {
                if (envelopes.hasOwnProperty(key)) {
                    var envelope = envelopes[key][0];
                    var metaConnect = envelopes[key][1];
                    if (metaConnect) {
                        _connected = false;
                    }
                    var failure = {
                        websocketCode: event.code,
                        reason: event.reason
                    };
                    if (event.exception) {
                        failure.exception = event.exception;
                    }
                    this._notifyFailure(envelope.onFailure, context, envelope.messages, failure);
                }
            }
        };

        _self.registered = function(type, cometd) {
            _super.registered(type, cometd);
            _cometd = cometd;
        };

        _self.accept = function(version, crossDomain, url) {
            this._debug('Transport', this.getType(), 'accept, supported:', _webSocketSupported);
            // Using !! to return a boolean (and not the WebSocket object).
            return _webSocketSupported && !!window.WebSocket && _cometd.websocketEnabled !== false;
        };

        _self.send = function(envelope, metaConnect) {
            this._debug('Transport', this.getType(), 'sending', envelope, '/meta/connect =', metaConnect);
            _send.call(this, _context, envelope, metaConnect);
        };

        _self.webSocketClose = function(context, code, reason) {
            try {
                if (context.webSocket) {
                    context.webSocket.close(code, reason);
                }
            } catch (x) {
                this._debug(x);
            }
        };

        _self.abort = function() {
            _super.abort();
            _forceClose.call(this, _context, {code: 1000, reason: 'Abort'});
            this.reset(true);
        };

        return _self;
    };


    /**
     * The constructor for a CometD object, identified by an optional name.
     * The default name is the string 'default'.
     * @param name the optional name of this cometd object
     */
    var CometD = function(name) {
        var _scheduler = new Scheduler();
        var _cometd = this;
        var _name = name || 'default';
        var _crossDomain = false;
        var _transports = new TransportRegistry();
        var _transport;
        var _status = 'disconnected';
        var _messageId = 0;
        var _clientId = null;
        var _batch = 0;
        var _messageQueue = [];
        var _internalBatch = false;
        var _listenerId = 0;
        var _listeners = {};
        var _backoff = 0;
        var _scheduledSend = null;
        var _extensions = [];
        var _advice = {};
        var _handshakeProps;
        var _handshakeCallback;
        var _callbacks = {};
        var _remoteCalls = {};
        var _reestablish = false;
        var _connected = false;
        var _unconnectTime = 0;
        var _handshakeMessages = 0;
        var _metaConnect = null;
        var _config = {
            useWorkerScheduler: true,
            protocol: null,
            stickyReconnect: true,
            connectTimeout: 0,
            maxConnections: 2,
            backoffIncrement: 1000,
            maxBackoff: 60000,
            logLevel: 'info',
            maxNetworkDelay: 10000,
            requestHeaders: {},
            appendMessageTypeToURL: true,
            autoBatch: false,
            urls: {},
            maxURILength: 2000,
            advice: {
                timeout: 60000,
                interval: 0,
                reconnect: undefined,
                maxInterval: 0
            }
        };

        function _fieldValue(object, name) {
            try {
                return object[name];
            } catch (x) {
                return undefined;
            }
        }

        /**
         * Mixes in the given objects into the target object by copying the properties.
         * @param deep if the copy must be deep
         * @param target the target object
         * @param objects the objects whose properties are copied into the target
         */
        this._mixin = function(deep, target, objects) {
            var result = target || {};

            // Skip first 2 parameters (deep and target), and loop over the others
            for (var i = 2; i < arguments.length; ++i) {
                var object = arguments[i];

                if (object === undefined || object === null) {
                    continue;
                }

                for (var propName in object) {
                    if (object.hasOwnProperty(propName)) {
                        var prop = _fieldValue(object, propName);
                        var targ = _fieldValue(result, propName);

                        // Avoid infinite loops
                        if (prop === target) {
                            continue;
                        }
                        // Do not mixin undefined values
                        if (prop === undefined) {
                            continue;
                        }

                        if (deep && typeof prop === 'object' && prop !== null) {
                            if (prop instanceof Array) {
                                result[propName] = this._mixin(deep, targ instanceof Array ? targ : [], prop);
                            } else {
                                var source = typeof targ === 'object' && !(targ instanceof Array) ? targ : {};
                                result[propName] = this._mixin(deep, source, prop);
                            }
                        } else {
                            result[propName] = prop;
                        }
                    }
                }
            }

            return result;
        };

        function _isString(value) {
            return Utils.isString(value);
        }

        function _isFunction(value) {
            if (value === undefined || value === null) {
                return false;
            }
            return typeof value === 'function';
        }

        function _zeroPad(value, length) {
            var result = '';
            while (--length > 0) {
                if (value >= Math.pow(10, length)) {
                    break;
                }
                result += '0';
            }
            result += value;
            return result;
        }

        function _log(level, args) {
            if (window.console) {
                var logger = window.console[level];
                if (_isFunction(logger)) {
                    var now = new Date();
                    [].splice.call(args, 0, 0, _zeroPad(now.getHours(), 2) + ':' + _zeroPad(now.getMinutes(), 2) + ':' +
                        _zeroPad(now.getSeconds(), 2) + '.' + _zeroPad(now.getMilliseconds(), 3));
                    logger.apply(window.console, args);
                }
            }
        }

        this._warn = function() {
            _log('warn', arguments);
        };

        this._info = function() {
            if (_config.logLevel !== 'warn') {
                _log('info', arguments);
            }
        };

        this._debug = function() {
            if (_config.logLevel === 'debug') {
                _log('debug', arguments);
            }
        };

        function _splitURL(url) {
            // [1] = protocol://,
            // [2] = host:port,
            // [3] = host,
            // [4] = IPv6_host,
            // [5] = IPv4_host,
            // [6] = :port,
            // [7] = port,
            // [8] = uri,
            // [9] = rest (query / fragment)
            return new RegExp('(^https?://)?(((\\[[^\\]]+])|([^:/?#]+))(:(\\d+))?)?([^?#]*)(.*)?').exec(url);
        }

        /**
         * Returns whether the given hostAndPort is cross domain.
         * The default implementation checks against window.location.host
         * but this function can be overridden to make it work in non-browser
         * environments.
         *
         * @param hostAndPort the host and port in format host:port
         * @return whether the given hostAndPort is cross domain
         */
        this._isCrossDomain = function(hostAndPort) {
            if (window.location && window.location.host) {
                if (hostAndPort) {
                    return hostAndPort !== window.location.host;
                }
            }
            return false;
        };

        function _configure(configuration) {
            _cometd._debug('Configuring cometd object with', configuration);
            // Support old style param, where only the Bayeux server URL was passed.
            if (_isString(configuration)) {
                configuration = {
                    url: configuration
                };
            }
            if (!configuration) {
                configuration = {};
            }

            _config = _cometd._mixin(false, _config, configuration);

            var url = _cometd.getURL();
            if (!url) {
                throw 'Missing required configuration parameter \'url\' specifying the Bayeux server URL';
            }

            // Check if we're cross domain.
            var urlParts = _splitURL(url);
            var hostAndPort = urlParts[2];
            var uri = urlParts[8];
            var afterURI = urlParts[9];
            _crossDomain = _cometd._isCrossDomain(hostAndPort);

            // Check if appending extra path is supported.
            if (_config.appendMessageTypeToURL) {
                if (afterURI !== undefined && afterURI.length > 0) {
                    _cometd._info('Appending message type to URI ' + uri + afterURI + ' is not supported, disabling \'appendMessageTypeToURL\' configuration');
                    _config.appendMessageTypeToURL = false;
                } else {
                    var uriSegments = uri.split('/');
                    var lastSegmentIndex = uriSegments.length - 1;
                    if (uri.match(/\/$/)) {
                        lastSegmentIndex -= 1;
                    }
                    if (uriSegments[lastSegmentIndex].indexOf('.') >= 0) {
                        // Very likely the CometD servlet's URL pattern is mapped to an extension, such as *.cometd
                        // It will be difficult to add the extra path in this case
                        _cometd._info('Appending message type to URI ' + uri + ' is not supported, disabling \'appendMessageTypeToURL\' configuration');
                        _config.appendMessageTypeToURL = false;
                    }
                }
            }

            if (window.Worker && window.Blob && window.URL && _config.useWorkerScheduler) {
                var code = WorkerScheduler.toString();
                // Remove the function declaration, the opening brace and the closing brace.
                code = code.substring(code.indexOf('{') + 1, code.lastIndexOf('}'));
                var blob = new window.Blob([code], {
                    type: 'application/json'
                });
                var blobURL = window.URL.createObjectURL(blob);
                var worker = new window.Worker(blobURL);
                _scheduler.setTimeout = function(funktion, delay) {
                    var id = _scheduler.register(funktion);
                    worker.postMessage({
                        id: id,
                        type: 'setTimeout',
                        delay: delay
                    });
                    return id;
                };
                _scheduler.clearTimeout = function(id) {
                    _scheduler.unregister(id);
                    worker.postMessage({
                        id: id,
                        type: 'clearTimeout'
                    });
                };
                worker.onmessage = function(e) {
                    var id = e.data.id;
                    var funktion = _scheduler.unregister(id);
                    if (funktion) {
                        funktion();
                    }
                };
            }
        }

        function _removeListener(subscription) {
            if (subscription) {
                var subscriptions = _listeners[subscription.channel];
                if (subscriptions && subscriptions[subscription.id]) {
                    delete subscriptions[subscription.id];
                    _cometd._debug('Removed', subscription.listener ? 'listener' : 'subscription', subscription);
                }
            }
        }

        function _removeSubscription(subscription) {
            if (subscription && !subscription.listener) {
                _removeListener(subscription);
            }
        }

        function _clearSubscriptions() {
            for (var channel in _listeners) {
                if (_listeners.hasOwnProperty(channel)) {
                    var subscriptions = _listeners[channel];
                    if (subscriptions) {
                        for (var id in subscriptions) {
                            if (subscriptions.hasOwnProperty(id)) {
                                _removeSubscription(subscriptions[id]);
                            }
                        }
                    }
                }
            }
        }

        function _setStatus(newStatus) {
            if (_status !== newStatus) {
                _cometd._debug('Status', _status, '->', newStatus);
                _status = newStatus;
            }
        }

        function _isDisconnected() {
            return _status === 'disconnecting' || _status === 'disconnected';
        }

        function _nextMessageId() {
            var result = ++_messageId;
            return '' + result;
        }

        function _applyExtension(scope, callback, name, message, outgoing) {
            try {
                return callback.call(scope, message);
            } catch (x) {
                var handler = _cometd.onExtensionException;
                if (_isFunction(handler)) {
                    _cometd._debug('Invoking extension exception handler', name, x);
                    try {
                        handler.call(_cometd, x, name, outgoing, message);
                    } catch (xx) {
                        _cometd._info('Exception during execution of extension exception handler', name, xx);
                    }
                } else {
                    _cometd._info('Exception during execution of extension', name, x);
                }
                return message;
            }
        }

        function _applyIncomingExtensions(message) {
            for (var i = 0; i < _extensions.length; ++i) {
                if (message === undefined || message === null) {
                    break;
                }

                var extension = _extensions[i];
                var callback = extension.extension.incoming;
                if (_isFunction(callback)) {
                    var result = _applyExtension(extension.extension, callback, extension.name, message, false);
                    message = result === undefined ? message : result;
                }
            }
            return message;
        }

        function _applyOutgoingExtensions(message) {
            for (var i = _extensions.length - 1; i >= 0; --i) {
                if (message === undefined || message === null) {
                    break;
                }

                var extension = _extensions[i];
                var callback = extension.extension.outgoing;
                if (_isFunction(callback)) {
                    var result = _applyExtension(extension.extension, callback, extension.name, message, true);
                    message = result === undefined ? message : result;
                }
            }
            return message;
        }

        function _notify(channel, message) {
            var subscriptions = _listeners[channel];
            if (subscriptions) {
                for (var id in subscriptions) {
                    if (subscriptions.hasOwnProperty(id)) {
                        var subscription = subscriptions[id];
                        // Subscriptions may come and go, so the array may have 'holes'
                        if (subscription) {
                            try {
                                subscription.callback.call(subscription.scope, message);
                            } catch (x) {
                                var handler = _cometd.onListenerException;
                                if (_isFunction(handler)) {
                                    _cometd._debug('Invoking listener exception handler', subscription, x);
                                    try {
                                        handler.call(_cometd, x, subscription, subscription.listener, message);
                                    } catch (xx) {
                                        _cometd._info('Exception during execution of listener exception handler', subscription, xx);
                                    }
                                } else {
                                    _cometd._info('Exception during execution of listener', subscription, message, x);
                                }
                            }
                        }
                    }
                }
            }
        }

        function _notifyListeners(channel, message) {
            // Notify direct listeners
            _notify(channel, message);

            // Notify the globbing listeners
            var channelParts = channel.split('/');
            var last = channelParts.length - 1;
            for (var i = last; i > 0; --i) {
                var channelPart = channelParts.slice(0, i).join('/') + '/*';
                // We don't want to notify /foo/* if the channel is /foo/bar/baz,
                // so we stop at the first non recursive globbing
                if (i === last) {
                    _notify(channelPart, message);
                }
                // Add the recursive globber and notify
                channelPart += '*';
                _notify(channelPart, message);
            }
        }

        function _cancelDelayedSend() {
            if (_scheduledSend !== null) {
                _cometd.clearTimeout(_scheduledSend);
            }
            _scheduledSend = null;
        }

        function _delayedSend(operation, delay) {
            _cancelDelayedSend();
            var time = _advice.interval + delay;
            _cometd._debug('Function scheduled in', time, 'ms, interval =', _advice.interval, 'backoff =', _backoff, operation);
            _scheduledSend = _cometd.setTimeout(operation, time);
        }

        // Needed to break cyclic dependencies between function definitions
        var _handleMessages;
        var _handleFailure;

        /**
         * Delivers the messages to the CometD server
         * @param messages the array of messages to send
         * @param metaConnect true if this send is on /meta/connect
         * @param extraPath an extra path to append to the Bayeux server URL
         */
        function _send(messages, metaConnect, extraPath) {
            // We must be sure that the messages have a clientId.
            // This is not guaranteed since the handshake may take time to return
            // (and hence the clientId is not known yet) and the application
            // may create other messages.
            for (var i = 0; i < messages.length; ++i) {
                var message = messages[i];
                var messageId = message.id;

                if (_clientId) {
                    message.clientId = _clientId;
                }

                message = _applyOutgoingExtensions(message);
                if (message !== undefined && message !== null) {
                    // Extensions may have modified the message id, but we need to own it.
                    message.id = messageId;
                    messages[i] = message;
                } else {
                    delete _callbacks[messageId];
                    messages.splice(i--, 1);
                }
            }

            if (messages.length === 0) {
                return;
            }

            if (metaConnect) {
                _metaConnect = messages[0];
            }

            var url = _cometd.getURL();
            if (_config.appendMessageTypeToURL) {
                // If url does not end with '/', then append it
                if (!url.match(/\/$/)) {
                    url = url + '/';
                }
                if (extraPath) {
                    url = url + extraPath;
                }
            }

            var envelope = {
                url: url,
                sync: false,
                messages: messages,
                onSuccess: function(rcvdMessages) {
                    try {
                        _handleMessages.call(_cometd, rcvdMessages);
                    } catch (x) {
                        _cometd._info('Exception during handling of messages', x);
                    }
                },
                onFailure: function(conduit, messages, failure) {
                    try {
                        var transport = _cometd.getTransport();
                        failure.connectionType = transport ? transport.getType() : "unknown";
                        _handleFailure.call(_cometd, conduit, messages, failure);
                    } catch (x) {
                        _cometd._info('Exception during handling of failure', x);
                    }
                }
            };
            _cometd._debug('Send', envelope);
            _transport.send(envelope, metaConnect);
        }

        function _queueSend(message) {
            if (_batch > 0 || _internalBatch === true) {
                _messageQueue.push(message);
            } else {
                _send([message], false);
            }
        }

        /**
         * Sends a complete bayeux message.
         * This method is exposed as a public so that extensions may use it
         * to send bayeux message directly, for example in case of re-sending
         * messages that have already been sent but that for some reason must
         * be resent.
         */
        this.send = _queueSend;

        function _resetBackoff() {
            _backoff = 0;
        }

        function _increaseBackoff() {
            if (_backoff < _config.maxBackoff) {
                _backoff += _config.backoffIncrement;
            }
            return _backoff;
        }

        /**
         * Starts a the batch of messages to be sent in a single request.
         * @see #_endBatch(sendMessages)
         */
        function _startBatch() {
            ++_batch;
            _cometd._debug('Starting batch, depth', _batch);
        }

        function _flushBatch() {
            var messages = _messageQueue;
            _messageQueue = [];
            if (messages.length > 0) {
                _send(messages, false);
            }
        }

        /**
         * Ends the batch of messages to be sent in a single request,
         * optionally sending messages present in the message queue depending
         * on the given argument.
         * @see #_startBatch()
         */
        function _endBatch() {
            --_batch;
            _cometd._debug('Ending batch, depth', _batch);
            if (_batch < 0) {
                throw 'Calls to startBatch() and endBatch() are not paired';
            }

            if (_batch === 0 && !_isDisconnected() && !_internalBatch) {
                _flushBatch();
            }
        }

        /**
         * Sends the connect message
         */
        function _connect() {
            if (!_isDisconnected()) {
                var bayeuxMessage = {
                    id: _nextMessageId(),
                    channel: '/meta/connect',
                    connectionType: _transport.getType()
                };

                // In case of reload or temporary loss of connection
                // we want the next successful connect to return immediately
                // instead of being held by the server, so that connect listeners
                // can be notified that the connection has been re-established
                if (!_connected) {
                    bayeuxMessage.advice = {
                        timeout: 0
                    };
                }

                _setStatus('connecting');
                _cometd._debug('Connect sent', bayeuxMessage);
                _send([bayeuxMessage], true, 'connect');
                _setStatus('connected');
            }
        }

        function _delayedConnect(delay) {
            _setStatus('connecting');
            _delayedSend(function() {
                _connect();
            }, delay);
        }

        function _updateAdvice(newAdvice) {
            if (newAdvice) {
                _advice = _cometd._mixin(false, {}, _config.advice, newAdvice);
                _cometd._debug('New advice', _advice);
            }
        }

        function _disconnect(abort) {
            _cancelDelayedSend();
            if (abort && _transport) {
                _transport.abort();
            }
            _crossDomain = false;
            _transport = null;
            _setStatus('disconnected');
            _clientId = null;
            _batch = 0;
            _resetBackoff();
            _reestablish = false;
            _connected = false;
            _unconnectTime = 0;
            _metaConnect = null;

            // Fail any existing queued message
            if (_messageQueue.length > 0) {
                var messages = _messageQueue;
                _messageQueue = [];
                _handleFailure.call(_cometd, undefined, messages, {
                    reason: 'Disconnected'
                });
            }
        }

        function _notifyTransportException(oldTransport, newTransport, failure) {
            var handler = _cometd.onTransportException;
            if (_isFunction(handler)) {
                _cometd._debug('Invoking transport exception handler', oldTransport, newTransport, failure);
                try {
                    handler.call(_cometd, failure, oldTransport, newTransport);
                } catch (x) {
                    _cometd._info('Exception during execution of transport exception handler', x);
                }
            }
        }

        /**
         * Sends the initial handshake message
         */
        function _handshake(handshakeProps, handshakeCallback) {
            if (_isFunction(handshakeProps)) {
                handshakeCallback = handshakeProps;
                handshakeProps = undefined;
            }

            _clientId = null;

            _clearSubscriptions();

            // Reset the transports if we're not retrying the handshake
            if (_isDisconnected()) {
                _transports.reset(true);
            }

            // Reset the advice.
            _updateAdvice({});

            _batch = 0;

            // Mark the start of an internal batch.
            // This is needed because handshake and connect are async.
            // It may happen that the application calls init() then subscribe()
            // and the subscribe message is sent before the connect message, if
            // the subscribe message is not held until the connect message is sent.
            // So here we start a batch to hold temporarily any message until
            // the connection is fully established.
            _internalBatch = true;

            // Save the properties provided by the user, so that
            // we can reuse them during automatic re-handshake
            _handshakeProps = handshakeProps;
            _handshakeCallback = handshakeCallback;

            var version = '1.0';

            // Figure out the transports to send to the server
            var url = _cometd.getURL();
            var transportTypes = _transports.findTransportTypes(version, _crossDomain, url);

            var bayeuxMessage = {
                id: _nextMessageId(),
                version: version,
                minimumVersion: version,
                channel: '/meta/handshake',
                supportedConnectionTypes: transportTypes,
                advice: {
                    timeout: _advice.timeout,
                    interval: _advice.interval
                }
            };
            // Do not allow the user to override important fields.
            var message = _cometd._mixin(false, {}, _handshakeProps, bayeuxMessage);

            // Save the callback.
            _cometd._putCallback(message.id, handshakeCallback);

            // Pick up the first available transport as initial transport
            // since we don't know if the server supports it
            if (!_transport) {
                _transport = _transports.negotiateTransport(transportTypes, version, _crossDomain, url);
                if (!_transport) {
                    var failure = 'Could not find initial transport among: ' + _transports.getTransportTypes();
                    _cometd._warn(failure);
                    throw failure;
                }
            }

            _cometd._debug('Initial transport is', _transport.getType());

            // We started a batch to hold the application messages,
            // so here we must bypass it and send immediately.
            _setStatus('handshaking');
            _cometd._debug('Handshake sent', message);
            _send([message], false, 'handshake');
        }

        function _delayedHandshake(delay) {
            _setStatus('handshaking');

            // We will call _handshake() which will reset _clientId, but we want to avoid
            // that between the end of this method and the call to _handshake() someone may
            // call publish() (or other methods that call _queueSend()).
            _internalBatch = true;

            _delayedSend(function() {
                _handshake(_handshakeProps, _handshakeCallback);
            }, delay);
        }

        function _notifyCallback(callback, message) {
            try {
                callback.call(_cometd, message);
            } catch (x) {
                var handler = _cometd.onCallbackException;
                if (_isFunction(handler)) {
                    _cometd._debug('Invoking callback exception handler', x);
                    try {
                        handler.call(_cometd, x, message);
                    } catch (xx) {
                        _cometd._info('Exception during execution of callback exception handler', xx);
                    }
                } else {
                    _cometd._info('Exception during execution of message callback', x);
                }
            }
        }

        this._getCallback = function(messageId) {
            return _callbacks[messageId];
        };

        this._putCallback = function(messageId, callback) {
            var result = this._getCallback(messageId);
            if (_isFunction(callback)) {
                _callbacks[messageId] = callback;
            }
            return result;
        };

        function _handleCallback(message) {
            var callback = _cometd._getCallback([message.id]);
            if (_isFunction(callback)) {
                delete _callbacks[message.id];
                _notifyCallback(callback, message);
            }
        }

        function _handleRemoteCall(message) {
            var context = _remoteCalls[message.id];
            delete _remoteCalls[message.id];
            if (context) {
                _cometd._debug('Handling remote call response for', message, 'with context', context);

                // Clear the timeout, if present.
                var timeout = context.timeout;
                if (timeout) {
                    _cometd.clearTimeout(timeout);
                }

                var callback = context.callback;
                if (_isFunction(callback)) {
                    _notifyCallback(callback, message);
                    return true;
                }
            }
            return false;
        }

        this.onTransportFailure = function(message, failureInfo, failureHandler) {
            this._debug('Transport failure', failureInfo, 'for', message);

            var transports = this.getTransportRegistry();
            var url = this.getURL();
            var crossDomain = this._isCrossDomain(_splitURL(url)[2]);
            var version = '1.0';
            var transportTypes = transports.findTransportTypes(version, crossDomain, url);

            if (failureInfo.action === 'none') {
                if (message.channel === '/meta/handshake') {
                    if (!failureInfo.transport) {
                        var failure = 'Could not negotiate transport, client=[' + transportTypes + '], server=[' + message.supportedConnectionTypes + ']';
                        this._warn(failure);
                        _notifyTransportException(_transport.getType(), null, {
                            reason: failure,
                            connectionType: _transport.getType(),
                            transport: _transport
                        });
                    }
                }
            } else {
                failureInfo.delay = this.getBackoffPeriod();
                // Different logic depending on whether we are handshaking or connecting.
                if (message.channel === '/meta/handshake') {
                    if (!failureInfo.transport) {
                        // The transport is invalid, try to negotiate again.
                        var oldTransportType = _transport ? _transport.getType() : null;
                        var newTransport = transports.negotiateTransport(transportTypes, version, crossDomain, url);
                        if (!newTransport) {
                            this._warn('Could not negotiate transport, client=[' + transportTypes + ']');
                            _notifyTransportException(oldTransportType, null, message.failure);
                            failureInfo.action = 'none';
                        } else {
                            var newTransportType = newTransport.getType();
                            this._debug('Transport', oldTransportType, '->', newTransportType);
                            _notifyTransportException(oldTransportType, newTransportType, message.failure);
                            failureInfo.action = 'handshake';
                            failureInfo.transport = newTransport;
                        }
                    }

                    if (failureInfo.action !== 'none') {
                        this.increaseBackoffPeriod();
                    }
                } else {
                    var now = new Date().getTime();

                    if (_unconnectTime === 0) {
                        _unconnectTime = now;
                    }

                    if (failureInfo.action === 'retry') {
                        failureInfo.delay = this.increaseBackoffPeriod();
                        // Check whether we may switch to handshaking.
                        var maxInterval = _advice.maxInterval;
                        if (maxInterval > 0) {
                            var expiration = _advice.timeout + _advice.interval + maxInterval;
                            var unconnected = now - _unconnectTime;
                            if (unconnected + _backoff > expiration) {
                                failureInfo.action = 'handshake';
                            }
                        }
                    }

                    if (failureInfo.action === 'handshake') {
                        failureInfo.delay = 0;
                        transports.reset(false);
                        this.resetBackoffPeriod();
                    }
                }
            }

            failureHandler.call(_cometd, failureInfo);
        };

        function _handleTransportFailure(failureInfo) {
            _cometd._debug('Transport failure handling', failureInfo);

            if (failureInfo.transport) {
                _transport = failureInfo.transport;
            }

            if (failureInfo.url) {
                _transport.setURL(failureInfo.url);
            }

            var action = failureInfo.action;
            var delay = failureInfo.delay || 0;
            switch (action) {
                case 'handshake':
                    _delayedHandshake(delay);
                    break;
                case 'retry':
                    _delayedConnect(delay);
                    break;
                case 'none':
                    _disconnect(true);
                    break;
                default:
                    throw 'Unknown action ' + action;
            }
        }

        function _failHandshake(message, failureInfo) {
            _handleCallback(message);
            _notifyListeners('/meta/handshake', message);
            _notifyListeners('/meta/unsuccessful', message);

            // The listeners may have disconnected.
            if (_isDisconnected()) {
                failureInfo.action = 'none';
            }

            _cometd.onTransportFailure.call(_cometd, message, failureInfo, _handleTransportFailure);
        }

        function _handshakeResponse(message) {
            var url = _cometd.getURL();
            if (message.successful) {
                var crossDomain = _cometd._isCrossDomain(_splitURL(url)[2]);
                var newTransport = _transports.negotiateTransport(message.supportedConnectionTypes, message.version, crossDomain, url);
                if (newTransport === null) {
                    message.successful = false;
                    _failHandshake(message, {
                        cause: 'negotiation',
                        action: 'none',
                        transport: null
                    });
                    return;
                } else if (_transport !== newTransport) {
                    _cometd._debug('Transport', _transport.getType(), '->', newTransport.getType());
                    _transport = newTransport;
                }

                _clientId = message.clientId;

                // End the internal batch and allow held messages from the application
                // to go to the server (see _handshake() where we start the internal batch).
                _internalBatch = false;
                _flushBatch();

                // Here the new transport is in place, as well as the clientId, so
                // the listeners can perform a publish() if they want.
                // Notify the listeners before the connect below.
                message.reestablish = _reestablish;
                _reestablish = true;

                _handleCallback(message);
                _notifyListeners('/meta/handshake', message);

                _handshakeMessages = message['x-messages'] || 0;

                var action = _isDisconnected() ? 'none' : _advice.reconnect || 'retry';
                switch (action) {
                    case 'retry':
                        _resetBackoff();
                        if (_handshakeMessages === 0) {
                            _delayedConnect(0);
                        } else {
                            _cometd._debug('Processing', _handshakeMessages, 'handshake-delivered messages');
                        }
                        break;
                    case 'none':
                        _disconnect(true);
                        break;
                    default:
                        throw 'Unrecognized advice action ' + action;
                }
            } else {
                _failHandshake(message, {
                    cause: 'unsuccessful',
                    action: _advice.reconnect || 'handshake',
                    transport: _transport
                });
            }
        }

        function _handshakeFailure(message) {
            _failHandshake(message, {
                cause: 'failure',
                action: 'handshake',
                transport: null
            });
        }

        function _matchMetaConnect(connect) {
            if (_status === 'disconnected') {
                return true;
            }
            if (_metaConnect && _metaConnect.id === connect.id) {
                _metaConnect = null;
                return true;
            }
            return false;
        }

        function _failConnect(message, failureInfo) {
            // Notify the listeners after the status change but before the next action.
            _notifyListeners('/meta/connect', message);
            _notifyListeners('/meta/unsuccessful', message);

            // The listeners may have disconnected.
            if (_isDisconnected()) {
                failureInfo.action = 'none';
            }

            _cometd.onTransportFailure.call(_cometd, message, failureInfo, _handleTransportFailure);
        }

        function _connectResponse(message) {
            if (_matchMetaConnect(message)) {
                _connected = message.successful;
                if (_connected) {
                    _notifyListeners('/meta/connect', message);

                    // Normally, the advice will say "reconnect: 'retry', interval: 0"
                    // and the server will hold the request, so when a response returns
                    // we immediately call the server again (long polling).
                    // Listeners can call disconnect(), so check the state after they run.
                    var action = _isDisconnected() ? 'none' : _advice.reconnect || 'retry';
                    switch (action) {
                        case 'retry':
                            _resetBackoff();
                            _delayedConnect(_backoff);
                            break;
                        case 'none':
                            _disconnect(false);
                            break;
                        default:
                            throw 'Unrecognized advice action ' + action;
                    }
                } else {
                    _failConnect(message, {
                        cause: 'unsuccessful',
                        action: _advice.reconnect || 'retry',
                        transport: _transport
                    });
                }
            } else {
                _cometd._debug('Mismatched /meta/connect reply', message);
            }
        }

        function _connectFailure(message) {
            if (_matchMetaConnect(message)) {
                _connected = false;
                _failConnect(message, {
                    cause: 'failure',
                    action: 'retry',
                    transport: null
                });
            } else {
                _cometd._debug('Mismatched /meta/connect failure', message);
            }
        }

        function _failDisconnect(message) {
            _disconnect(true);
            _handleCallback(message);
            _notifyListeners('/meta/disconnect', message);
            _notifyListeners('/meta/unsuccessful', message);
        }

        function _disconnectResponse(message) {
            if (message.successful) {
                // Wait for the /meta/connect to arrive.
                _disconnect(false);
                _handleCallback(message);
                _notifyListeners('/meta/disconnect', message);
            } else {
                _failDisconnect(message);
            }
        }

        function _disconnectFailure(message) {
            _failDisconnect(message);
        }

        function _failSubscribe(message) {
            var subscriptions = _listeners[message.subscription];
            if (subscriptions) {
                for (var id in subscriptions) {
                    if (subscriptions.hasOwnProperty(id)) {
                        var subscription = subscriptions[id];
                        if (subscription && !subscription.listener) {
                            delete subscriptions[id];
                            _cometd._debug('Removed failed subscription', subscription);
                        }
                    }
                }
            }
            _handleCallback(message);
            _notifyListeners('/meta/subscribe', message);
            _notifyListeners('/meta/unsuccessful', message);
        }

        function _subscribeResponse(message) {
            if (message.successful) {
                _handleCallback(message);
                _notifyListeners('/meta/subscribe', message);
            } else {
                _failSubscribe(message);
            }
        }

        function _subscribeFailure(message) {
            _failSubscribe(message);
        }

        function _failUnsubscribe(message) {
            _handleCallback(message);
            _notifyListeners('/meta/unsubscribe', message);
            _notifyListeners('/meta/unsuccessful', message);
        }

        function _unsubscribeResponse(message) {
            if (message.successful) {
                _handleCallback(message);
                _notifyListeners('/meta/unsubscribe', message);
            } else {
                _failUnsubscribe(message);
            }
        }

        function _unsubscribeFailure(message) {
            _failUnsubscribe(message);
        }

        function _failMessage(message) {
            if (!_handleRemoteCall(message)) {
                _handleCallback(message);
                _notifyListeners('/meta/publish', message);
                _notifyListeners('/meta/unsuccessful', message);
            }
        }

        function _messageResponse(message) {
            if (message.data !== undefined) {
                if (!_handleRemoteCall(message)) {
                    _notifyListeners(message.channel, message);
                    if (_handshakeMessages > 0) {
                        --_handshakeMessages;
                        if (_handshakeMessages === 0) {
                            _cometd._debug('Processed last handshake-delivered message');
                            _delayedConnect(0);
                        }
                    }
                }
            } else {
                if (message.successful === undefined) {
                    _cometd._warn('Unknown Bayeux Message', message);
                } else {
                    if (message.successful) {
                        _handleCallback(message);
                        _notifyListeners('/meta/publish', message);
                    } else {
                        _failMessage(message);
                    }
                }
            }
        }

        function _messageFailure(failure) {
            _failMessage(failure);
        }

        function _receive(message) {
            _unconnectTime = 0;

            message = _applyIncomingExtensions(message);
            if (message === undefined || message === null) {
                return;
            }

            _updateAdvice(message.advice);

            var channel = message.channel;
            switch (channel) {
                case '/meta/handshake':
                    _handshakeResponse(message);
                    break;
                case '/meta/connect':
                    _connectResponse(message);
                    break;
                case '/meta/disconnect':
                    _disconnectResponse(message);
                    break;
                case '/meta/subscribe':
                    _subscribeResponse(message);
                    break;
                case '/meta/unsubscribe':
                    _unsubscribeResponse(message);
                    break;
                default:
                    _messageResponse(message);
                    break;
            }
        }

        /**
         * Receives a message.
         * This method is exposed as a public so that extensions may inject
         * messages simulating that they had been received.
         */
        this.receive = _receive;

        _handleMessages = function(rcvdMessages) {
            _cometd._debug('Received', rcvdMessages);

            for (var i = 0; i < rcvdMessages.length; ++i) {
                var message = rcvdMessages[i];
                _receive(message);
            }
        };

        _handleFailure = function(conduit, messages, failure) {
            _cometd._debug('handleFailure', conduit, messages, failure);

            failure.transport = conduit;
            for (var i = 0; i < messages.length; ++i) {
                var message = messages[i];
                var failureMessage = {
                    id: message.id,
                    successful: false,
                    channel: message.channel,
                    failure: failure
                };
                failure.message = message;
                switch (message.channel) {
                    case '/meta/handshake':
                        _handshakeFailure(failureMessage);
                        break;
                    case '/meta/connect':
                        _connectFailure(failureMessage);
                        break;
                    case '/meta/disconnect':
                        _disconnectFailure(failureMessage);
                        break;
                    case '/meta/subscribe':
                        failureMessage.subscription = message.subscription;
                        _subscribeFailure(failureMessage);
                        break;
                    case '/meta/unsubscribe':
                        failureMessage.subscription = message.subscription;
                        _unsubscribeFailure(failureMessage);
                        break;
                    default:
                        _messageFailure(failureMessage);
                        break;
                }
            }
        };

        function _hasSubscriptions(channel) {
            var subscriptions = _listeners[channel];
            if (subscriptions) {
                for (var id in subscriptions) {
                    if (subscriptions.hasOwnProperty(id)) {
                        if (subscriptions[id]) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        function _resolveScopedCallback(scope, callback) {
            var delegate = {
                scope: scope,
                method: callback
            };
            if (_isFunction(scope)) {
                delegate.scope = undefined;
                delegate.method = scope;
            } else {
                if (_isString(callback)) {
                    if (!scope) {
                        throw 'Invalid scope ' + scope;
                    }
                    delegate.method = scope[callback];
                    if (!_isFunction(delegate.method)) {
                        throw 'Invalid callback ' + callback + ' for scope ' + scope;
                    }
                } else if (!_isFunction(callback)) {
                    throw 'Invalid callback ' + callback;
                }
            }
            return delegate;
        }

        function _addListener(channel, scope, callback, isListener) {
            // The data structure is a map<channel, subscription[]>, where each subscription
            // holds the callback to be called and its scope.

            var delegate = _resolveScopedCallback(scope, callback);
            _cometd._debug('Adding', isListener ? 'listener' : 'subscription', 'on', channel, 'with scope', delegate.scope, 'and callback', delegate.method);

            var id = ++_listenerId;
            var subscription = {
                id: id,
                channel: channel,
                scope: delegate.scope,
                callback: delegate.method,
                listener: isListener
            };

            var subscriptions = _listeners[channel];
            if (!subscriptions) {
                subscriptions = {};
                _listeners[channel] = subscriptions;
            }

            subscriptions[id] = subscription;

            _cometd._debug('Added', isListener ? 'listener' : 'subscription', subscription);

            return subscription;
        }

        //
        // PUBLIC API
        //

        /**
         * Registers the given transport under the given transport type.
         * The optional index parameter specifies the "priority" at which the
         * transport is registered (where 0 is the max priority).
         * If a transport with the same type is already registered, this function
         * does nothing and returns false.
         * @param type the transport type
         * @param transport the transport object
         * @param index the index at which this transport is to be registered
         * @return true if the transport has been registered, false otherwise
         * @see #unregisterTransport(type)
         */
        this.registerTransport = function(type, transport, index) {
            var result = _transports.add(type, transport, index);
            if (result) {
                this._debug('Registered transport', type);

                if (_isFunction(transport.registered)) {
                    transport.registered(type, this);
                }
            }
            return result;
        };

        /**
         * Unregisters the transport with the given transport type.
         * @param type the transport type to unregister
         * @return the transport that has been unregistered,
         * or null if no transport was previously registered under the given transport type
         */
        this.unregisterTransport = function(type) {
            var transport = _transports.remove(type);
            if (transport !== null) {
                this._debug('Unregistered transport', type);

                if (_isFunction(transport.unregistered)) {
                    transport.unregistered();
                }
            }
            return transport;
        };

        this.unregisterTransports = function() {
            _transports.clear();
        };

        /**
         * @return an array of all registered transport types
         */
        this.getTransportTypes = function() {
            return _transports.getTransportTypes();
        };

        this.findTransport = function(name) {
            return _transports.find(name);
        };

        /**
         * @returns the TransportRegistry object
         */
        this.getTransportRegistry = function() {
            return _transports;
        };

        /**
         * Configures the initial Bayeux communication with the Bayeux server.
         * Configuration is passed via an object that must contain a mandatory field <code>url</code>
         * of type string containing the URL of the Bayeux server.
         * @param configuration the configuration object
         */
        this.configure = function(configuration) {
            _configure.call(this, configuration);
        };

        /**
         * Configures and establishes the Bayeux communication with the Bayeux server
         * via a handshake and a subsequent connect.
         * @param configuration the configuration object
         * @param handshakeProps an object to be merged with the handshake message
         * @see #configure(configuration)
         * @see #handshake(handshakeProps)
         */
        this.init = function(configuration, handshakeProps) {
            this.configure(configuration);
            this.handshake(handshakeProps);
        };

        /**
         * Establishes the Bayeux communication with the Bayeux server
         * via a handshake and a subsequent connect.
         * @param handshakeProps an object to be merged with the handshake message
         * @param handshakeCallback a function to be invoked when the handshake is acknowledged
         */
        this.handshake = function(handshakeProps, handshakeCallback) {
            if (_status !== 'disconnected') {
                throw 'Illegal state: handshaken';
            }
            _handshake(handshakeProps, handshakeCallback);
        };

        /**
         * Disconnects from the Bayeux server.
         * @param disconnectProps an object to be merged with the disconnect message
         * @param disconnectCallback a function to be invoked when the disconnect is acknowledged
         */
        this.disconnect = function(disconnectProps, disconnectCallback) {
            if (_isDisconnected()) {
                return;
            }

            if (_isFunction(disconnectProps)) {
                disconnectCallback = disconnectProps;
                disconnectProps = undefined;
            }

            var bayeuxMessage = {
                id: _nextMessageId(),
                channel: '/meta/disconnect'
            };
            // Do not allow the user to override important fields.
            var message = this._mixin(false, {}, disconnectProps, bayeuxMessage);

            // Save the callback.
            _cometd._putCallback(message.id, disconnectCallback);

            _setStatus('disconnecting');
            _send([message], false, 'disconnect');
        };

        /**
         * Marks the start of a batch of application messages to be sent to the server
         * in a single request, obtaining a single response containing (possibly) many
         * application reply messages.
         * Messages are held in a queue and not sent until {@link #endBatch()} is called.
         * If startBatch() is called multiple times, then an equal number of endBatch()
         * calls must be made to close and send the batch of messages.
         * @see #endBatch()
         */
        this.startBatch = function() {
            _startBatch();
        };

        /**
         * Marks the end of a batch of application messages to be sent to the server
         * in a single request.
         * @see #startBatch()
         */
        this.endBatch = function() {
            _endBatch();
        };

        /**
         * Executes the given callback in the given scope, surrounded by a {@link #startBatch()}
         * and {@link #endBatch()} calls.
         * @param scope the scope of the callback, may be omitted
         * @param callback the callback to be executed within {@link #startBatch()} and {@link #endBatch()} calls
         */
        this.batch = function(scope, callback) {
            var delegate = _resolveScopedCallback(scope, callback);
            this.startBatch();
            try {
                delegate.method.call(delegate.scope);
                this.endBatch();
            } catch (x) {
                this._info('Exception during execution of batch', x);
                this.endBatch();
                throw x;
            }
        };

        /**
         * Adds a listener for bayeux messages, performing the given callback in the given scope
         * when a message for the given channel arrives.
         * @param channel the channel the listener is interested to
         * @param scope the scope of the callback, may be omitted
         * @param callback the callback to call when a message is sent to the channel
         * @returns the subscription handle to be passed to {@link #removeListener(object)}
         * @see #removeListener(subscription)
         */
        this.addListener = function(channel, scope, callback) {
            if (arguments.length < 2) {
                throw 'Illegal arguments number: required 2, got ' + arguments.length;
            }
            if (!_isString(channel)) {
                throw 'Illegal argument type: channel must be a string';
            }

            return _addListener(channel, scope, callback, true);
        };

        /**
         * Removes the subscription obtained with a call to {@link #addListener(string, object, function)}.
         * @param subscription the subscription to unsubscribe.
         * @see #addListener(channel, scope, callback)
         */
        this.removeListener = function(subscription) {
            // Beware of subscription.id == 0, which is falsy => cannot use !subscription.id
            if (!subscription || !subscription.channel || !("id" in subscription)) {
                throw 'Invalid argument: expected subscription, not ' + subscription;
            }

            _removeListener(subscription);
        };

        /**
         * Removes all listeners registered with {@link #addListener(channel, scope, callback)} or
         * {@link #subscribe(channel, scope, callback)}.
         */
        this.clearListeners = function() {
            _listeners = {};
        };

        /**
         * Subscribes to the given channel, performing the given callback in the given scope
         * when a message for the channel arrives.
         * @param channel the channel to subscribe to
         * @param scope the scope of the callback, may be omitted
         * @param callback the callback to call when a message is sent to the channel
         * @param subscribeProps an object to be merged with the subscribe message
         * @param subscribeCallback a function to be invoked when the subscription is acknowledged
         * @return the subscription handle to be passed to {@link #unsubscribe(object)}
         */
        this.subscribe = function(channel, scope, callback, subscribeProps, subscribeCallback) {
            if (arguments.length < 2) {
                throw 'Illegal arguments number: required 2, got ' + arguments.length;
            }
            if (!_isString(channel)) {
                throw 'Illegal argument type: channel must be a string';
            }
            if (_isDisconnected()) {
                throw 'Illegal state: disconnected';
            }

            // Normalize arguments
            if (_isFunction(scope)) {
                subscribeCallback = subscribeProps;
                subscribeProps = callback;
                callback = scope;
                scope = undefined;
            }
            if (_isFunction(subscribeProps)) {
                subscribeCallback = subscribeProps;
                subscribeProps = undefined;
            }

            // Only send the message to the server if this client has not yet subscribed to the channel
            var send = !_hasSubscriptions(channel);

            var subscription = _addListener(channel, scope, callback, false);

            if (send) {
                // Send the subscription message after the subscription registration to avoid
                // races where the server would send a message to the subscribers, but here
                // on the client the subscription has not been added yet to the data structures
                var bayeuxMessage = {
                    id: _nextMessageId(),
                    channel: '/meta/subscribe',
                    subscription: channel
                };
                // Do not allow the user to override important fields.
                var message = this._mixin(false, {}, subscribeProps, bayeuxMessage);

                // Save the callback.
                _cometd._putCallback(message.id, subscribeCallback);

                _queueSend(message);
            }

            return subscription;
        };

        /**
         * Unsubscribes the subscription obtained with a call to {@link #subscribe(string, object, function)}.
         * @param subscription the subscription to unsubscribe.
         * @param unsubscribeProps an object to be merged with the unsubscribe message
         * @param unsubscribeCallback a function to be invoked when the unsubscription is acknowledged
         */
        this.unsubscribe = function(subscription, unsubscribeProps, unsubscribeCallback) {
            if (arguments.length < 1) {
                throw 'Illegal arguments number: required 1, got ' + arguments.length;
            }
            if (_isDisconnected()) {
                throw 'Illegal state: disconnected';
            }

            if (_isFunction(unsubscribeProps)) {
                unsubscribeCallback = unsubscribeProps;
                unsubscribeProps = undefined;
            }

            // Remove the local listener before sending the message
            // This ensures that if the server fails, this client does not get notifications
            this.removeListener(subscription);

            var channel = subscription.channel;
            // Only send the message to the server if this client unsubscribes the last subscription
            if (!_hasSubscriptions(channel)) {
                var bayeuxMessage = {
                    id: _nextMessageId(),
                    channel: '/meta/unsubscribe',
                    subscription: channel
                };
                // Do not allow the user to override important fields.
                var message = this._mixin(false, {}, unsubscribeProps, bayeuxMessage);

                // Save the callback.
                _cometd._putCallback(message.id, unsubscribeCallback);

                _queueSend(message);
            }
        };

        this.resubscribe = function(subscription, subscribeProps) {
            _removeSubscription(subscription);
            if (subscription) {
                return this.subscribe(subscription.channel, subscription.scope, subscription.callback, subscribeProps);
            }
            return undefined;
        };

        /**
         * Removes all subscriptions added via {@link #subscribe(channel, scope, callback, subscribeProps)},
         * but does not remove the listeners added via {@link addListener(channel, scope, callback)}.
         */
        this.clearSubscriptions = function() {
            _clearSubscriptions();
        };

        /**
         * Publishes a message on the given channel, containing the given content.
         * @param channel the channel to publish the message to
         * @param content the content of the message
         * @param publishProps an object to be merged with the publish message
         * @param publishCallback a function to be invoked when the publish is acknowledged by the server
         */
        this.publish = function(channel, content, publishProps, publishCallback) {
            if (arguments.length < 1) {
                throw 'Illegal arguments number: required 1, got ' + arguments.length;
            }
            if (!_isString(channel)) {
                throw 'Illegal argument type: channel must be a string';
            }
            if (/^\/meta\//.test(channel)) {
                throw 'Illegal argument: cannot publish to meta channels';
            }
            if (_isDisconnected()) {
                throw 'Illegal state: disconnected';
            }

            if (_isFunction(content)) {
                publishCallback = content;
                content = {};
                publishProps = undefined;
            } else if (_isFunction(publishProps)) {
                publishCallback = publishProps;
                publishProps = undefined;
            }

            var bayeuxMessage = {
                id: _nextMessageId(),
                channel: channel,
                data: content
            };
            // Do not allow the user to override important fields.
            var message = this._mixin(false, {}, publishProps, bayeuxMessage);

            // Save the callback.
            _cometd._putCallback(message.id, publishCallback);

            _queueSend(message);
        };

        /**
         * Publishes a message with binary data on the given channel.
         * The binary data chunk may be an ArrayBuffer, a DataView, a TypedArray
         * (such as Uint8Array) or a plain integer array.
         * The meta data object may contain additional application data such as
         * a file name, a mime type, etc.
         * @param channel the channel to publish the message to
         * @param data the binary data to publish
         * @param last whether the binary data chunk is the last
         * @param meta an object containing meta data associated to the binary chunk
         * @param callback a function to be invoked when the publish is acknowledged by the server
         */
        this.publishBinary = function(channel, data, last, meta, callback) {
            if (_isFunction(data)) {
                callback = data;
                data = new ArrayBuffer(0);
                last = true;
                meta = undefined;
            } else if (_isFunction(last)) {
                callback = last;
                last = true;
                meta = undefined;
            } else if (_isFunction(meta)) {
                callback = meta;
                meta = undefined;
            }
            var content = {
                meta: meta,
                data: data,
                last: last
            };
            var ext = {
                ext: {
                    binary: {}
                }
            };
            this.publish(channel, content, ext, callback);
        };

        this.remoteCall = function(target, content, timeout, callProps, callback) {
            if (arguments.length < 1) {
                throw 'Illegal arguments number: required 1, got ' + arguments.length;
            }
            if (!_isString(target)) {
                throw 'Illegal argument type: target must be a string';
            }
            if (_isDisconnected()) {
                throw 'Illegal state: disconnected';
            }

            if (_isFunction(content)) {
                callback = content;
                content = {};
                timeout = _config.maxNetworkDelay;
                callProps = undefined;
            } else if (_isFunction(timeout)) {
                callback = timeout;
                timeout = _config.maxNetworkDelay;
                callProps = undefined;
            } else if (_isFunction(callProps)) {
                callback = callProps;
                callProps = undefined;
            }

            if (typeof timeout !== 'number') {
                throw 'Illegal argument type: timeout must be a number';
            }

            if (!target.match(/^\//)) {
                target = '/' + target;
            }
            var channel = '/service' + target;

            var bayeuxMessage = {
                id: _nextMessageId(),
                channel: channel,
                data: content
            };
            var message = this._mixin(false, {}, callProps, bayeuxMessage);

            var context = {
                callback: callback
            };
            if (timeout > 0) {
                context.timeout = _cometd.setTimeout(function() {
                    _cometd._debug('Timing out remote call', message, 'after', timeout, 'ms');
                    _failMessage({
                        id: message.id,
                        error: '406::timeout',
                        successful: false,
                        failure: {
                            message: message,
                            reason: 'Remote Call Timeout'
                        }
                    });
                }, timeout);
                _cometd._debug('Scheduled remote call timeout', message, 'in', timeout, 'ms');
            }
            _remoteCalls[message.id] = context;

            _queueSend(message);
        };

        this.remoteCallBinary = function(target, data, last, meta, timeout, callback) {
            if (_isFunction(data)) {
                callback = data;
                data = new ArrayBuffer(0);
                last = true;
                meta = undefined;
                timeout = _config.maxNetworkDelay;
            } else if (_isFunction(last)) {
                callback = last;
                last = true;
                meta = undefined;
                timeout = _config.maxNetworkDelay;
            } else if (_isFunction(meta)) {
                callback = meta;
                meta = undefined;
                timeout = _config.maxNetworkDelay;
            } else if (_isFunction(timeout)) {
                callback = timeout;
                timeout = _config.maxNetworkDelay;
            }

            var content = {
                meta: meta,
                data: data,
                last: last
            };
            var ext = {
                ext: {
                    binary: {}
                }
            };

            this.remoteCall(target, content, timeout, ext, callback);
        };

        /**
         * Returns a string representing the status of the bayeux communication with the Bayeux server.
         */
        this.getStatus = function() {
            return _status;
        };

        /**
         * Returns whether this instance has been disconnected.
         */
        this.isDisconnected = _isDisconnected;

        /**
         * Sets the backoff period used to increase the backoff time when retrying an unsuccessful or failed message.
         * Default value is 1 second, which means if there is a persistent failure the retries will happen
         * after 1 second, then after 2 seconds, then after 3 seconds, etc. So for example with 15 seconds of
         * elapsed time, there will be 5 retries (at 1, 3, 6, 10 and 15 seconds elapsed).
         * @param period the backoff period to set
         * @see #getBackoffIncrement()
         */
        this.setBackoffIncrement = function(period) {
            _config.backoffIncrement = period;
        };

        /**
         * Returns the backoff period used to increase the backoff time when retrying an unsuccessful or failed message.
         * @see #setBackoffIncrement(period)
         */
        this.getBackoffIncrement = function() {
            return _config.backoffIncrement;
        };

        /**
         * Returns the backoff period to wait before retrying an unsuccessful or failed message.
         */
        this.getBackoffPeriod = function() {
            return _backoff;
        };

        /**
         * Increases the backoff period up to the maximum value configured.
         * @returns the backoff period after increment
         * @see getBackoffIncrement
         */
        this.increaseBackoffPeriod = function() {
            return _increaseBackoff();
        };

        /**
         * Resets the backoff period to zero.
         */
        this.resetBackoffPeriod = function() {
            _resetBackoff();
        };

        /**
         * Sets the log level for console logging.
         * Valid values are the strings 'error', 'warn', 'info' and 'debug', from
         * less verbose to more verbose.
         * @param level the log level string
         */
        this.setLogLevel = function(level) {
            _config.logLevel = level;
        };

        /**
         * Registers an extension whose callbacks are called for every incoming message
         * (that comes from the server to this client implementation) and for every
         * outgoing message (that originates from this client implementation for the
         * server).
         * The format of the extension object is the following:
         * <pre>
         * {
         *     incoming: function(message) { ... },
         *     outgoing: function(message) { ... }
         * }
         * </pre>
         * Both properties are optional, but if they are present they will be called
         * respectively for each incoming message and for each outgoing message.
         * @param name the name of the extension
         * @param extension the extension to register
         * @return true if the extension was registered, false otherwise
         * @see #unregisterExtension(name)
         */
        this.registerExtension = function(name, extension) {
            if (arguments.length < 2) {
                throw 'Illegal arguments number: required 2, got ' + arguments.length;
            }
            if (!_isString(name)) {
                throw 'Illegal argument type: extension name must be a string';
            }

            var existing = false;
            for (var i = 0; i < _extensions.length; ++i) {
                var existingExtension = _extensions[i];
                if (existingExtension.name === name) {
                    existing = true;
                    break;
                }
            }
            if (!existing) {
                _extensions.push({
                    name: name,
                    extension: extension
                });
                this._debug('Registered extension', name);

                // Callback for extensions
                if (_isFunction(extension.registered)) {
                    extension.registered(name, this);
                }

                return true;
            } else {
                this._info('Could not register extension with name', name, 'since another extension with the same name already exists');
                return false;
            }
        };

        /**
         * Unregister an extension previously registered with
         * {@link #registerExtension(name, extension)}.
         * @param name the name of the extension to unregister.
         * @return true if the extension was unregistered, false otherwise
         */
        this.unregisterExtension = function(name) {
            if (!_isString(name)) {
                throw 'Illegal argument type: extension name must be a string';
            }

            var unregistered = false;
            for (var i = 0; i < _extensions.length; ++i) {
                var extension = _extensions[i];
                if (extension.name === name) {
                    _extensions.splice(i, 1);
                    unregistered = true;
                    this._debug('Unregistered extension', name);

                    // Callback for extensions
                    var ext = extension.extension;
                    if (_isFunction(ext.unregistered)) {
                        ext.unregistered();
                    }

                    break;
                }
            }
            return unregistered;
        };

        /**
         * Find the extension registered with the given name.
         * @param name the name of the extension to find
         * @return the extension found or null if no extension with the given name has been registered
         */
        this.getExtension = function(name) {
            for (var i = 0; i < _extensions.length; ++i) {
                var extension = _extensions[i];
                if (extension.name === name) {
                    return extension.extension;
                }
            }
            return null;
        };

        /**
         * Returns the name assigned to this CometD object, or the string 'default'
         * if no name has been explicitly passed as parameter to the constructor.
         */
        this.getName = function() {
            return _name;
        };

        /**
         * Returns the clientId assigned by the Bayeux server during handshake.
         */
        this.getClientId = function() {
            return _clientId;
        };

        /**
         * Returns the URL of the Bayeux server.
         */
        this.getURL = function() {
            if (_transport) {
                var url = _transport.getURL();
                if (url) {
                    return url;
                }
                url = _config.urls[_transport.getType()];
                if (url) {
                    return url;
                }
            }
            return _config.url;
        };

        this.getTransport = function() {
            return _transport;
        };

        this.getConfiguration = function() {
            return this._mixin(true, {}, _config);
        };

        this.getAdvice = function() {
            return this._mixin(true, {}, _advice);
        };

        this.setTimeout = function(funktion, delay) {
            return _scheduler.setTimeout(function() {
                try {
                    _cometd._debug('Invoking timed function', funktion);
                    funktion();
                } catch (x) {
                    _cometd._debug('Exception invoking timed function', funktion, x);
                }
            }, delay);
        };

        this.clearTimeout = function(id) {
            _scheduler.clearTimeout(id);
        };

        // Initialize transports.
        if (window.WebSocket) {
            this.registerTransport('websocket', new WebSocketTransport());
        }
        this.registerTransport('long-polling', new LongPollingTransport());
        this.registerTransport('callback-polling', new CallbackPollingTransport());
    };

    var _z85EncodeTable = [
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', '.', '-', ':', '+', '=', '^', '!', '/',
        '*', '?', '&', '<', '>', '(', ')', '[', ']', '{',
        '}', '@', '%', '$', '#'
    ];
    var _z85DecodeTable = [
        0x00, 0x44, 0x00, 0x54, 0x53, 0x52, 0x48, 0x00,
        0x4B, 0x4C, 0x46, 0x41, 0x00, 0x3F, 0x3E, 0x45,
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x40, 0x00, 0x49, 0x42, 0x4A, 0x47,
        0x51, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A,
        0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32,
        0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A,
        0x3B, 0x3C, 0x3D, 0x4D, 0x00, 0x4E, 0x43, 0x00,
        0x00, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
        0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
        0x21, 0x22, 0x23, 0x4F, 0x00, 0x50, 0x00, 0x00
    ];
    var Z85 = {
        encode: function(bytes) {
            var buffer = null;
            if (bytes instanceof ArrayBuffer) {
                buffer = bytes;
            } else if (bytes.buffer instanceof ArrayBuffer) {
                buffer = bytes.buffer;
            } else if (Array.isArray(bytes)) {
                buffer = new Uint8Array(bytes).buffer;
            }
            if (buffer == null) {
                throw 'Cannot Z85 encode ' + bytes;
            }

            var length = buffer.byteLength;
            var remainder = length % 4;
            var padding = 4 - (remainder === 0 ? 4 : remainder);
            var view = new DataView(buffer);
            var result = '';
            var value = 0;
            for (var i = 0; i < length + padding; ++i) {
                var isPadding = i >= length;
                value = value * 256 + (isPadding ? 0 : view.getUint8(i));
                if ((i + 1) % 4 === 0) {
                    var divisor = 85 * 85 * 85 * 85;
                    for (var j = 5; j > 0; --j) {
                        if (!isPadding || j > padding) {
                            var code = Math.floor(value / divisor) % 85;
                            result += _z85EncodeTable[code];
                        }
                        divisor /= 85;
                    }
                    value = 0;
                }
            }

            return result;
        },
        decode: function(string) {
            var remainder = string.length % 5;
            var padding = 5 - (remainder === 0 ? 5 : remainder);
            for (var p = 0; p < padding; ++p) {
                string += _z85EncodeTable[_z85EncodeTable.length - 1];
            }
            var length = string.length;

            var buffer = new ArrayBuffer((length * 4 / 5) - padding);
            var view = new DataView(buffer);
            var value = 0;
            var charIdx = 0;
            var byteIdx = 0;
            for (var i = 0; i < length; ++i) {
                var code = string.charCodeAt(charIdx++) - 32;
                value = value * 85 + _z85DecodeTable[code];
                if (charIdx % 5 === 0) {
                    var divisor = 256 * 256 * 256;
                    while (divisor >= 1) {
                        if (byteIdx < view.byteLength) {
                            view.setUint8(byteIdx++, Math.floor(value / divisor) % 256);
                        }
                        divisor /= 256;
                    }
                    value = 0;
                }
            }

            return buffer;
        }
    };

    return {
        CometD: CometD,
        Transport: Transport,
        RequestTransport: RequestTransport,
        LongPollingTransport: LongPollingTransport,
        CallbackPollingTransport: CallbackPollingTransport,
        WebSocketTransport: WebSocketTransport,
        Utils: Utils,
        Z85: Z85
    };
}));
