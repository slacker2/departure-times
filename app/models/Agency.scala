package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import customMongoContext._


object AgencyType extends Enumeration {
  val bus = "Bus"
  val rail = "Rail"
}

//<agency tag="actransit" title="AC Transit" regionTitle="California-Northern"/>
//<Agency Name="AC Transit" HasDirection="True" Mode="Bus"></Agency>
case class Agency(id: String,
                  name: String, // same as title
                  api: String,
                  mode: String = AgencyType.bus, // remove?
                  hasDirection: Option[Boolean],
                  regionTitle: Option[String],
                  tag: Option[String])


object Agency extends ModelCompanion[Agency, String] {
  val dao = new SalatDAO[Agency, String](collection = mongoCollection("agencies")) {}

  def getAllAgencies(): List[Agency] = {
    dao.find(MongoDBObject()).toList
  }

}
