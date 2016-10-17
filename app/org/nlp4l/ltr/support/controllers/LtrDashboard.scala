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

import org.nlp4l.ltr.support.dao.DocFeatureDAO
import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.Action
import play.api.mvc.Controller
import org.nlp4l.ltr.support.dao.FeatureDAO
import org.nlp4l.ltr.support.dao.LtrconfigDAO
import org.nlp4l.ltr.support.dao.LtrmodelDAO
import org.nlp4l.ltr.support.dao.LtrannotationDAO
import org.nlp4l.ltr.support.dao.LtrqueryDAO
import org.nlp4l.ltr.support.models.Ltrconfig
import org.nlp4l.ltr.support.models.Ltrquery
import org.nlp4l.ltr.support.models.Ltrmodel
import org.nlp4l.ltr.support.models.Feature
import scala.concurrent.Await
import org.nlp4l.ltr.support.models.Menubar
import scala.util.Failure
import scala.util.Success

@Singleton
class LtrDashboard @Inject()(docFeatureDAO: DocFeatureDAO, 
                             featureDAO: FeatureDAO, 
                             ltrconfigDAO: LtrconfigDAO,
                             ltrmodelDAO: LtrmodelDAO,
                             ltrqueryDAO: LtrqueryDAO,
                             ltrannotationDAO: LtrannotationDAO) extends Controller {

  def index(ltrid: Int) = Action { request =>
    docFeatureDAO.init
    featureDAO.init
    ltrconfigDAO.init
    ltrmodelDAO.init
    ltrqueryDAO.init
    ltrannotationDAO.init
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.dashboard(menubars))
  }

  
  def config(ltrid: Int) = Action {
    val f = ltrconfigDAO.fetchAll()
    val ltrconfigs: Seq[Ltrconfig] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.config(ltrid,menubars,ltrconfigs))
  }
  
  def editConfig(ltrid: Int, target: Int) = Action {
    val f = ltrconfigDAO.fetchAll()
    val ltrconfigs: Seq[Ltrconfig] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    val ltr = getLtr(target)
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.editConfig(ltrid,menubars,ltrconfigs,ltr,"",""))
  }
  
  def newConfig(ltrid: Int) = Action {
    val f = ltrconfigDAO.fetchAll()
    val ltrconfigs: Seq[Ltrconfig] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    val ltr = None
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.editConfig(ltrid,menubars,ltrconfigs,ltr,"",""))
  }
  
  
  def query(ltrid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.query(ltrid,menubars,ltr,"",""))
  }
  
  
  def annotation(ltrid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    val ltrquery =ltrqueryDAO.fetchNext(ltrid, 0) match {
      case Some(x) => Some(x)
      case _ => Some(Ltrquery(Some(0), "", ltrid, false))
    }
    Ok(org.nlp4l.ltr.support.views.html.annotation(ltrid,menubars,ltr,ltrquery,"",""))
  }

  def editAnnotation(ltrid: Int, qid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    val f = ltrqueryDAO.get(qid)
    Await.ready(f, scala.concurrent.duration.Duration.Inf)
    val ltrquery = f.value.get match {
      case Success(x) => Some(x)
      case Failure(ex) => Some(Ltrquery(Some(0), "", ltrid, false))
    }
    Ok(org.nlp4l.ltr.support.views.html.annotation(ltrid,menubars,ltr,ltrquery,"",""))
  }

  def feature(ltrid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    Ok(org.nlp4l.ltr.support.views.html.feature(ltrid,menubars,ltr,"",""))
  }

  def model(ltrid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    val f = ltrmodelDAO.fetchByLtrid(ltrid)
    val ltrmodels: Seq[Ltrmodel] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    Ok(org.nlp4l.ltr.support.views.html.model(ltrid,menubars,ltr,ltrmodels,"",""))
  }

  def newModel(ltrid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    val f = ltrmodelDAO.fetchByLtrid(ltrid)
    val ltrmodels: Seq[Ltrmodel] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    val ff = featureDAO.fetchByLtrid(ltrid)
    val features: Seq[Feature] = Await.result(ff, scala.concurrent.duration.Duration.Inf)
    Ok(org.nlp4l.ltr.support.views.html.newModel(ltrid,menubars,ltr,ltrmodels,features,"",""))
  }

  def modelStatus(ltrid: Int, mid: Int) = Action {
    val ltr = getLtr(ltrid)
    val menubars = buildMenubars(ltrid)
    val f = ltrmodelDAO.fetchByLtrid(ltrid)
    val ltrmodels: Seq[Ltrmodel] = Await.result(f, scala.concurrent.duration.Duration.Inf)
    val ff = featureDAO.fetchByLtrid(ltrid)
    val features: Seq[Feature] = Await.result(ff, scala.concurrent.duration.Duration.Inf)
    Ok(org.nlp4l.ltr.support.views.html.modelStatus(ltrid,menubars,ltr,ltrmodels,features,"",""))
  }

  private def buildMenubars(ltrid: Int): Seq[Menubar] = {
    if(ltrid <= 0) {
      Seq(Menubar("Config","/ltrdashboard/0/config"))
    } else {
      val ltr = getLtr(ltrid)
      val ltrname: String = ltr match {
        case Some(ltr) => "[ " + ltr.name + " ]"
        case None => ""
      }
      Seq(
          Menubar("Config"+ltrname,"/ltrdashboard/" + ltrid + "/config"),
          Menubar("Query","/ltrdashboard/" + ltrid + "/query"),
          Menubar("Annotation","/ltrdashboard/" + ltrid + "/annotation"),
          Menubar("Feature","/ltrdashboard/" + ltrid + "/feature"),
          Menubar("Model","/ltrdashboard/" + ltrid + "/model")
      )
    }
  }
  
  private def getLtr(ltrid: Int): Option[Ltrconfig] = {
      val f = ltrconfigDAO.get(ltrid)
      Await.ready(f, scala.concurrent.duration.Duration.Inf)
      f.value.get match {
        case Success(ltr) => Some(ltr)
        case Failure(ex) => None
      }
  }
  
}
