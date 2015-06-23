package models

import com.novus.salat.annotations._


case class GeoJSONPoint(@Key("type")typ: String = "Point", coordinates: List[Double])

