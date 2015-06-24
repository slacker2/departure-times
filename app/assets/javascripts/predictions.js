
window.onload = loadGoogleMapsAPI;

function loadGoogleMapsAPI() {
  var script = document.createElement('script');
  script.type = 'text/javascript';
  // Will automagically callback to getPredictions
  script.src = 'https://maps.googleapis.com/maps/api/js?v=3.exp&callback=getPredictions';
  document.body.appendChild(script);
}

var predictionResults;
var map;

function getPredictions() {

  map = new google.maps.Map(document.getElementById('map'), { zoom: 15 });

  var geoLocationOptions = {
    enableHighAccuracy: true,
    timeout: 10000,
    maximumAge: 0
  };

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(predictDeparturesWithCoords, predictDeparturesWithoutCoords, geoLocationOptions);
  } else {
    predictDeparturesWithoutCoords(null);
  }

  function predictDeparturesWithCoords(pos) {
    var predictionsURL = "/predictions/query?lon=" + pos.coords.longitude + "&lat=" + pos.coords.latitude;
    $.get(predictionsURL, function(data, s) {
      predictionResults = data;
      console.log(predictionResults);
      drawMap(predictionResults.query.lon, predictionResults.query.lat);
      displayPredictionResults();
    });

  }
  function predictDeparturesWithoutCoords(err) {
    $.get("/predictions/geolocate", function(data, s) {
      predictionResults = data;
      console.log(predictionResults);
      drawMap(predictionResults.query.lon, predictionResults.query.lat);
      displayPredictionResults();
    });
  }

  function drawMap(lon, lat) {
    var pos = new google.maps.LatLng(lat, lon);
    var infoWindow = new google.maps.InfoWindow({
      map: map,
      position: pos,
      content: 'Here you are!'
    });
    map.setCenter(pos);
  }

  function displayPredictionResults() {
    predictionResults.stops.forEach( function(currStop) {
      currStop.predictions.forEach( function(currPrediction) {
        $("#predictions").append( "<tr>" + 
          "<td>" + currStop.name + "</td>" +
          "<td>" + currPrediction.route + "</td>" +
          "<td>" + currPrediction.direction + "</td>" +
          "<td>" + currPrediction.estimates + "</td></tr>");
      });
    });
  }

}

