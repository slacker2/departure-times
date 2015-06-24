
window.onload = loadGoogleMapsAPI;

function loadGoogleMapsAPI() {
  var script = document.createElement('script');
  script.type = 'text/javascript';
  // Will automagically callback to getPredictions
  script.src = 'https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&callback=getPredictions';
  document.body.appendChild(script);
}

var predictionResults;
var map;

function getPredictions() {

  var mapOptions = {
    zoom: 15
  };

  map = new google.maps.Map(document.getElementById('map'), mapOptions);

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
    console.log(predictionsURL);
    $.get(predictionsURL, function(data, s) {
      predictionResults = data;
      console.log(predictionResults);
      drawMap(predictionResults.query.lon, predictionResults.query.lat);
    });

  }
  function predictDeparturesWithoutCoords(err) {
    $.get("/predictions/geolocate", function(data, s) {
      predictionResults = data;
      console.log(predictionResults);
      drawMap(predictionResults.query.lon, predictionResults.query.lat);
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
}

