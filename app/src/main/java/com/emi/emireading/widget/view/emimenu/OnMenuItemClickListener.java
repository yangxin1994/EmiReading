
/*
 * Copyright (C) 2017 skydoves
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

package com.emi.emireading.widget.view.emimenu;

/**
 * @author :zhoujian
 * @description : 菜单控件点击监听
 * @company :翼迈科技
 * @date 2018年05月21日上午 09:54
 * @Email: 971613168@qq.com
 */
public interface OnMenuItemClickListener<T> {
    /**
     * item点击事件
     *
     * @param position
     * @param item
     */
    void onItemClick(int position, T item);
}
