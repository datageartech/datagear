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
        module.exports = factory(require('./jquery.cometd'), require('cometd/AckExtension'));
    } else if (typeof define === 'function' && define.amd) {
        define(['jquery.cometd', 'cometd/AckExtension'], factory);
    } else {
        factory(jQuery.cometd, root.org.cometd.AckExtension);
    }
}(this, function(cometd, AckExtension) {
    var result = new AckExtension();
    cometd.registerExtension('ack', result);
    return result;
}));
