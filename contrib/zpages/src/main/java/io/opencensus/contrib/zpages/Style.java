/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.zpages;

final class Style {
  private Style() {}

  static String style =
      "body{font-family: 'Roboto',sans-serif;"
          + "font-size: 14px;background-color: #F2F4EC;}"
          + "h1{color: #3D3D3D;text-align: center;margin-bottom: 20px;}"
          + "p{padding: 0 0.5em;color: #3D3D3D;}"
          + "h2{color: #3D3D3D;font-size: 1.5em;background-color: #FFF;"
          + "line-height: 2.0;margin-bottom: 0;padding: 0 0.5em;}"
          + "h3{font-size:16px;padding:0 0.5em;margin-top:6px;margin-bottom:25px;}"
          + "a{color:#A94442;}"
          + "p.header{font-family: 'Open Sans', sans-serif;top: 0;left: 0;width: 100%;"
          + "height: 60px;vertical-align: middle;color: #C1272D;font-size: 22pt;}"
          + "p.view{font-size: 20px;margin-bottom: 0;}"
          + ".header span{color: #3D3D3D;}"
          + "img.oc{vertical-align: middle;}"
          + "table{width: 100%;color: #FFF;background-color: #FFF;overflow: hidden;"
          + "margin-bottom: 30px;margin-top: 0;border-bottom: 1px solid #3D3D3D;"
          + "border-left: 1px solid #3D3D3D;border-right: 1px solid #3D3D3D;}"
          + "table.title{width:100%;color:#3D3D3D;background-color:#FFF;"
          + "border:none;line-height:2.0;margin-bottom:0;}"
          + "thead{color: #FFF;background-color: #A94442;"
          + "line-height:3.0;padding:0 0.5em;}"
          + "th{color: #FFF;background-color: #A94442;"
          + "line-height:3.0;padding:0 0.5em;}"
          + "th.borderL{border-left:1px solid #FFF; text-align:left;}"
          + "th.borderRL{border-right:1px solid #FFF; text-align:left;}"
          + "th.borderLB{border-left:1px solid #FFF;"
          + "border-bottom:1px solid #FFF;margin:0 10px;}"
          + "tr.direct{font-size:16px;padding:0 0.5em;background-color:#F2F4EC;}"
          + "tr:nth-child(even){background-color: #F2F2F2;}"
          + "td{color: #3D3D3D;line-height: 2.0;text-align: left;padding: 0 0.5em;}"
          + "td.borderLC{border-left:1px solid #3D3D3D;text-align:center;}"
          + "td.borderLL{border-left:1px solid #3D3D3D;text-align:left;}"
          + "td.borderRL{border-right:1px solid #3D3D3D;text-align:left;}"
          + "td.borderRW{border-right:1px solid #FFF}"
          + "td.borderLW{border-left:1px solid #FFF;}"
          + "td.centerW{text-align:center;color:#FFF;}"
          + "td.center{text-align:center;color:#3D3D3D;}"
          + "tr.bgcolor{background-color:#A94442;}"
          + "h1.left{text-align:left;margin-left:20px;}"
          + "table.small{width:40%;background-color:#FFF;"
          + "margin-left:20px;margin-bottom:30px;}"
          + "table.small{width:40%;background-color:#FFF;"
          + "margin-left:20px;margin-bottom:30px;}"
          + "td.col_headR{background-color:#A94442;"
          + "line-height:3.0;color:#FFF;border-right:1px solid #FFF;}"
          + "td.col_head{background-color:#A94442;"
          + "line-height:3.0;color:#FFF;}"
          + "b.title{margin-left:20px;font-weight:bold;line-height:2.0;}"
          + "input.button{margin-left:20px;margin-top:4px;"
          + "font-size:20px;width:80px;height:60px;}"
          + "td.head{text-align:center;color:#FFF;line-height:3.0;}";
}
