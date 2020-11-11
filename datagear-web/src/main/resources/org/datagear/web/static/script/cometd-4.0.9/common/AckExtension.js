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

(function(root, factory){
    if (typeof exports === 'object') {
        module.exports = factory(require('./cometd'));
    } else if (typeof define === 'function' && define.amd) {
        define(['./cometd'], factory);
    } else {
        factory(root.org.cometd);
    }
}(this, function(cometdModule) {
    /**
     * This client-side extension enables the client to acknowledge to the server
     * the messages that the client has received.
     * For the acknowledgement to work, the server must be configured with the
     * correspondent server-side ack extension. If both client and server support
     * the ack extension, then the ack functionality will take place automatically.
     * By enabling this extension, all messages arriving from the server will arrive
     * via /meta/connect, so the comet communication will be slightly chattier.
     * The fact that all messages will return via /meta/connect means also that the
     * messages will arrive with total order, which is not guaranteed if messages
     * can arrive via both /meta/connect and normal response.
     * Messages are not acknowledged one by one, but instead a batch of messages is
     * acknowledged when the /meta/connect returns.
     */
    return cometdModule.AckExtension = function() {
        var _cometd;
        var _serverSupportsAcks = false;
        var _batch;

        function _debug(text, args) {
            _cometd._debug(text, args);
        }

        this.registered = function(name, cometd) {
            _cometd = cometd;
            _debug('AckExtension: executing registration callback');
        };

        this.unregistered = function() {
            _debug('AckExtension: executing unregistration callback');
            _cometd = null;
        };

        this.incoming = function(message) {
            var channel = message.channel;
            var ext = message.ext;
            if (channel === '/meta/handshake') {
                if (ext) {
                    var ackField = ext.ack;
                    if (typeof ackField === 'object') {
                        // New format.
                        _serverSupportsAcks = ackField.enabled === true;
                        var batch = ackField.batch;
                        if (typeof batch === 'number') {
                            _batch = batch;
                        }
                    } else {
                        // Old format.
                        _serverSupportsAcks = ackField === true;
                    }
                }
                _debug('AckExtension: server supports acknowledgements', _serverSupportsAcks);
            } else if (channel === '/meta/connect' && message.successful && _serverSupportsAcks) {
                if (ext && typeof ext.ack === 'number') {
                    _batch = ext.ack;
                    _debug('AckExtension: server sent batch', _batch);
                }
            }
            return message;
        };

        this.outgoing = function(message) {
            var channel = message.channel;
            if (!message.ext) {
                message.ext = {};
            }
            if (channel === '/meta/handshake') {
                message.ext.ack = _cometd && _cometd.ackEnabled !== false;
                _serverSupportsAcks = false;
                _batch = 0;
            } else if (channel === '/meta/connect') {
                if (_serverSupportsAcks) {
                    message.ext.ack = _batch;
                    _debug('AckExtension: client sending batch', _batch);
                }
            }
            return message;
        };
    };
}));
