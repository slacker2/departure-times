Departure Times
================

Project Overview
---------
This project is a service that gives real-time departure time predictions for bus stops within 100 meters of the visitor's location.

You can see this project in action here: https://blooming-reef-6511.herokuapp.com

All bus, route, stop, and prediction data is provided by the [NextBus API](http://www.nextbus.com/xmlFeedDocs/NextBusXMLFeed.pdf). 

Focus
------
This project focuses on the back-end. My goal for this project was to provide a simple to use API that would provide only relevant departure predictions to the user, and cut back on displaying irrelevant data without requiring user input.

Technical Choices
------------------
### Functional Overview
This service has an API endpoint (see below for API documentation) that will populate the database with all of the transportation agencies, routes, and stops known by the NextBus API. It does so programatically and idempotently. Once the database is populated, queries can be made against the prediction endpoints. When provided with longitude and latitude coordinates, the service will find stops within a variable radius of the given location using the data in the database. Once the relevant stops are located, calls to the NextBus API will be made to get the predicted departure times of buses from those stops.

### Folders / Files of Interest
All of the actual code and work is done in the "app" directory. Everything in there was written by me.

### Stack
* [Play Framework](https://www.playframework.com/) (version 2.3.7)
  - I chose Play for 2 primary reasons:
    1. The simplicity in asynchronous, non-blocking I/O. Conceptually, if this were larger scale, since this API functions as a middleman to the NextBus API (and possibly others), it is important to not block on waiting for other APIs to return. It also enables several API calls to be made in parallel.
    2. I have been frequently developing on top of Play for the past 7 months; this is all the experience I have with this framework.
* [Scala](http://www.scala-lang.org/) (version 2.11.6)
  - I chose Scala for 2 primary reasons:
    1. It is more readable, expressive, and concise than the alternative, Java.
    2. I have been primarily developing in Scala for the past 7 months; this is all the experience I have with this language.
* [MongoDB](https://www.mongodb.org/) (version 2.6, via [MongoLab](https://mongolab.com/))
  - I primarily chose MongoDB to create a [geospatial index](http://docs.mongodb.org/manual/core/2dsphere/) to assist with finding stops near given coordinates. I have used MongoDB occasionally for the past two years.


### Libararies / Resources
* [NextBus API](http://www.nextbus.com/xmlFeedDocs/NextBusXMLFeed.pdf)
* [JQuery](https://jquery.com/)
* [Google Maps JavaScript API](https://developers.google.com/maps/documentation/javascript/)
* [Salat](https://github.com/leon/play-salat)


### Geolocating the visitor
The user will be geolocated via their browser. If the browser fails to geolocate, the API will attempt to geolocate the user by IP address, using [MaxMind](https://www.maxmind.com) data, via a server I set up in AWS EC2 for this demonstration.

### Data Modeling
This project uses [Salat](https://github.com/leon/play-salat) as an ORM plugin.

Highlights about the data models for this project:
  1. The id fields (the primary key for these objects as they are represented in the database) are designed to be idempotent when re-seeding the database programatically. That is to say: there won't be multiple objects representing the same agency for the same API.
  2. This project currently only uses the NextBus API. However, if there were multiple APIs to be used, then agencies, routes, and stops can be accounted for in multiple APIs (e.g. NextBus and 511.org both have an API for the Muni). The data models were designed with this behavior in mind, and could easily be adapted to include other APIs or for additional agencies or as backup APIs.
  3. Stops can be associated with multiple routes, agencies, and APIs. The Stop model is designed to account for this in order to make certain of getting all relevant predictions while minimizing redundant API calls.

### Testing
There is not any real testing being done here. One of the reasons for this is that since Scala is statically typed and a compiling language, there aren't many possible errors at runtime; issues typically occur at compile time. Another reason is that since the NextBus API is largely out of my control, testing against doesn't reflect so much of the functionality of this project. In the event of a failure or invalid parameters, requests will fail fast and hard, returning a BadRequest.

API Usage
----------
There are 3 primary endpoints of note.

1. > /predictions/query 
  * To query the API directly, submit a GET request to this endpoint using the following URL query string parameters:
    * lon - (Required) a double representing the longitude of the location to get predictions of nearby stops.
    * lat - (Required) a double representing the latitude of the location to get predictions of nearby stops.
    * rad - (Optional) an integer representing the radius, in meters, from the point specified by the longitude and latitude in which to include stops to show predictions for. If no distance parameter is specified, the default value of 100 will be used.

  Examples:
  ```
  http://blooming-reef-6511.herokuapp.com/predictions/query?lon=-122.43110589999999&lat=37.7757144

  http://blooming-reef-6511.herokuapp.com/predictions/query?lon=-122.43110589999999&lat=37.7757144&rad=150
  ```

  The return value will be JSON with the following format:
  ```
  { "query" :
      { "lon" : -122.43110589999999,
        "lat" : 37.7757144,
        "rad" : 100
      },
    "stops":
      [{ "name" : "Fillmore St & Hayes St",
         "loc" : { "lon" : -122.43121, "lat" : 37.7756 },
         "predictions":
           [{ "route" : "22-Fillmore",
             "direction" : "Inbound to the Marina District",
             "estimates" : [44,16,30,45,60,75]
           }]
       },
       { "name" : "Hayes St & Fillmore St",
         "loc" : { "lon" : -122.43083, "lat" : 37.7757999 },
         "predictions":
           [{ "route" : "21-Hayes",
             "direction" : "Inbound to Downtown",
             "estimates" : [4,33,63,93]
           }]
       },
       { "name" : "Hayes St & Fillmore St",
         "loc" : {"lon" : -122.4315599, "lat" : 37.77584 },
         "predictions" :
           [{ "route" : "21-Hayes",
             "direction" : "Outbound to Golden Gate Park",
             "estimates" : [2,30,60,90]
           }]
       }]
  }

  ```

  The "query" object represents the query made to generate these results (i.e. the parameters). The "stops" array as a list of all of the stops that were within the specified radius, the departure time predictions for that stop, and some other relevant information. If there is not a stop within the radius of the location specified by the longitude and latitude, the API will return an empty JSON body. Invalid parameters will result in a status code 400 (Bad Request).

2. > /predictions/geolocate
  * When making a GET request this endpoint, the request's IP will be geolocated, and those coordinates will be used to predicted departure times for stops near that location. The format will be identical to the format for the /predictions/query endpoint.

3. > /populate
  * When making a GET request to this endpoint, it will trigger a database repopulation. In the event that the agency / route / stop data available from the NextBus API changes, the database can be updated programmatically by using this endpoint. (NOTE: If any agency, route, or stop is removed from the NextBus API, it will not be removed from the database.) 

Moving Forward 
---------------
If I were to spend more time on this project, here are a few directions I would go with it:
* I realize that I left statically configured info in my project (e.g. geolocation server IP, MongoDB URI, etc.), and I would not do that in production or real-life, but I left them there for simplicity.
* The IP geolocation doesn't seem to work as well as I had hoped, so I'd probably defer to using another geolocation service, like Google's API.
* Add a few more freely available APIs to add more data for predictions. 
* Add in scheduled departure times; to display in lieu of unavailable predictions or in addition to the predictions. 
* Figure out how to allow a user to just click somewhere on the map, and predict departure times near that location. 
* Add some intelligent cacheing for stops that have very recently had predictions.
* Make stops appear on the map, and have some sort of UI response to listed predictions and the stop on the map.
