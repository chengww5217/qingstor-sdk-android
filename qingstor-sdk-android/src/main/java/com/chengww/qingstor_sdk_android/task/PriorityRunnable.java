/*
 * Copyright 2018 chengww
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chengww.qingstor_sdk_android.task;

/**
 * Created by chengww on 2019/3/4.
 */
public class PriorityRunnable extends PriorityObject<Runnable> implements Runnable, Comparable {

    public PriorityRunnable(Runnable obj) {
        super(0, obj);
    }

    @Override
    public void run() {
        this.obj.run();
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
