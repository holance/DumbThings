/*
 * Copyright (c) 2015 Lunci Hua
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.lunci.dumbthing.account;

import android.content.Intent;

/**
 * Created by Lunci on 2/6/2015.
 */
public interface ILinkAccount {
    public static interface LinkAccoutCallbacks{
        void onLinked(boolean succ, int toastId);
    }
    public void setButtonContainer(LinkButtonContainer container);
    public LinkButtonContainer getButtonContainer();
    public boolean isLinked();
    public void link(Object args);
    public void unlink();
    public void onLinkResult(int requestCode, int resultCode, Intent data);
}
