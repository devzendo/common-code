/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
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


package org.devzendo.commoncode.collection;

import java.util.ArrayList;
import java.util.List;

public class Lists {

    private Lists() { } // can't be instantiated

    public static <T> List<T> join(List<T>... lists) {
        final List<T> result = new ArrayList<T>();
        for(List<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

}