/*
 * Copyright 2010 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.i18n;

import org.jetbrains.annotations.NonNls;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Aug 23, 2010
* Time: 10:52:04 PM
* To change this template use File | Settings | File Templates.
*/
public class MessageBuffer {
    
    private final String msgKey;
    private final Object[] msgArgs;

    MessageBuffer(String msgKey, Object... msgArgs) {
        this.msgKey = msgKey;
        this.msgArgs = msgArgs;
    }

    @Override
    public String toString() {
        return I18N.msg(msgKey, msgArgs);
    }

    public static MessageBuffer msg(@NonNls String msgKey, Object... msgArgs) {
        return new MessageBuffer(msgKey, msgArgs);
    }
}
