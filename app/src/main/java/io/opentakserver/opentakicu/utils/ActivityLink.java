/*
 * Copyright (C) 2023 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentakserver.opentakicu.utils;

import android.content.Intent;

public class ActivityLink {
  private final int minSdk;
  private final String label;
  private final Intent intent;

  public ActivityLink(Intent intent, String label, int minSdk) {
    this.intent = intent;
    this.label = label;
    this.minSdk = minSdk;
  }

  public String getLabel() {
    return label;
  }

  public Intent getIntent() {
    return intent;
  }

  public int getMinSdk() {
    return minSdk;
  }
}
