/*
 * Copyright 2015 org.NLP4L
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

package org.nlp4l.ltr.support.controllers

import java.net.URLEncoder

import scala.concurrent.Await

import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper

import dispatch.Http
import dispatch.as
import dispatch.url
import dispatch._

import scala.util.{Success, Failure}

import scala.concurrent.ExecutionContext.Implicits.global

class SolrSearch(val searchUrl: String) {

  def search(queryStr: String): SolrSearchResponse = {
    val searchUrlQuery = searchUrl.replaceAll("\\$\\{query\\}", URLEncoder.encode(queryStr, "UTF-8"))
    val req = url(searchUrlQuery)
    val f = Http(req > as.String)
    val res = Await.result(f, scala.concurrent.duration.Duration.Inf)
    try {
      new SolrSearchResponse(res)
    }
    catch {
      case e: Exception => throw new Exception(res)
    }
  }
}

class SolrSearchResponse(val jsonString: String) {
  val json: JsValue = Json.parse(jsonString)

  val status = (json \ "responseHeader" \ "status").as[Int]

  def statusSuccess(): Boolean = {
    status == 0
  }
  def errorMsg(): String = {
    (json \ "error" \ "msg").as[String]
  }

  def docsList(): Seq[SolrSearchResultDocument] = {
    (json \ "response" \ "docs").as[Seq[JsObject]].map(
      new SolrSearchResultDocument(_)
    )
  }
}

class SolrSearchResultDocument(val doc: JsObject) {

  def getFirstValueAsString(name: String): String = {
    val v = doc.value.get(name)
    v match {
      case Some(x) =>
        if (x.isInstanceOf[JsArray]) {
          val seq = x.as[Seq[JsValue]]
          if (seq.nonEmpty) seq.head.as[String] else null
        } else {
          x.as[String]
        }
      case None => null
    }
  }
}
