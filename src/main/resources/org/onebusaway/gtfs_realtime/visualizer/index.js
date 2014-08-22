      function initialize() {
		var hostandport = window.location.hostname + ':' + window.location.port;
   	    var brisbaneLatLng = new google.maps.LatLng(-27.4775067,153.0281366);
        var mapOptions = {
          center: new google.maps.LatLng(-27.4775067,153.0281366),
          zoom: 15
        };
        var map = new google.maps.Map(document.getElementById('map-canvas'),
            mapOptions);
		var myLatLng = new google.maps.LatLng(-27.47351399999997,153.013136);	
		var marker = new google.maps.Marker({
			position: myLatLng, 
			map: map,
			title: 'Dialog is here - 35 Boundary St, South Brisbane Qld 4101.   (07) 3247 1000'
		});	
		var vehicleImage = 'Vehicle2.png';
		var williamJollyBridgeLatLng = new google.maps.LatLng(-27.47351399999997, 153.0204856);

		

		/**
		 * We want to assign a random color to each bus in our visualization. We
		 * pick from the HSV color-space since it gives more natural colors.
		 */
		var HsvToRgb = function(h, s, v) {
			h_int = parseInt(h * 6);
			f = h * 6 - h_int;
			var a = v * (1 - s);
			var b = v * (1 - f * s);
			var c = v * (1 - (1 - f) * s);
			switch (h_int) {
			case 0:
				return [ v, c, a ];
			case 1:
				return [ b, v, a ];
			case 2:
				return [ a, v, c ];
			case 3:
				return [ a, b, v ];
			case 4:
				return [ c, a, v ];
			case 5:
				return [ v, a, b ];
			}
		};

		var HsvToRgbString = function(h, s, v) {
			var rgb = HsvToRgb(h, s, v);
			for ( var i = 0; i < rgb.length; ++i) {
				rgb[i] = parseInt(rgb[i] * 256)
			}
			return 'rgb(' + rgb[0] + ',' + rgb[1] + ',' + rgb[2] + ')';
		};

		var h = Math.random();
		var golden_ratio_conjugate = 0.618033988749895;

		var NextRandomColor = function() {
			h = (h + golden_ratio_conjugate) % 1;
			return HsvToRgbString(h, 0.90, 0.90)
		};

		var icon = vehicleImage;   
		        //new google.maps.MarkerImage(
				//'http://' + hostandport + '/vehicle.png');
				//, null, null, new google.maps.Point(4, 4));

		var CreateVehicle = function(v_data) {
			var point = new google.maps.LatLng(v_data.lat, v_data.lon);
			var path = new google.maps.MVCArray();
			path.push(point);
			     
			var marker_opts = {
				clickable : true,
				draggable : false,
				flat : false,
				icon : icon,
				map : map,
				position : point,
				title : 'id=' + v_data.id + ',entity=' + v_data.entity + ',route=' + v_data.route + ',trip=' + v_data.trip
			};
			var polyline_opts = {
				clickable : false,
				editable : false,
				map : map,
				path : path,
				strokeColor : NextRandomColor(),
				strokeOpacity : 0.8,
				strokeWeight : 4
			};
			return {
				id : v_data.id,
				marker : new google.maps.Marker(marker_opts),
				polyline : new google.maps.Polyline(polyline_opts),
				path : path,
				lastUpdate : v_data.lastUpdate
			};
		};

		function CreateVehicleUpdateOperation(vehicle, lat, lon) {
			return function() {
				var point = new google.maps.LatLng(lat, lon);
				vehicle.marker.setPosition(point);
				var path = vehicle.path;
				var index = path.getLength() - 1;
				path.setAt(index, point);
			};
		};
		
		var vehicles_by_id = {};
		var animation_steps = 20;

		function UpdateVehicle(v_data, updates) {
			var id = v_data.id;
			console.log('UpdateVehicle: ' + id);
			if (!(id in vehicles_by_id)) {
				vehicles_by_id[id] = CreateVehicle(v_data);
			}
			var vehicle = vehicles_by_id[id];
			if (vehicle.lastUpdate >= v_data.lastUpdate) {
				return;
			}
			vehicle.lastUpdate = v_data.lastUpdate

			var path = vehicle.path;
			if (v_data.stationary != true) {
				var last = path.getAt(path.getLength() - 1);
				path.push(last);
				// erase track tails
				console.log('Vehicle:' + id + 'path.length=' + path.getLength());
				if (path.getLength() > 10) {
					path.removeAt(0);      // deletes entry 0, shifts the others down
					console.log('len now = ' + path.getLength());
				}
				
				var lat_delta = (v_data.lat - last.lat()) / animation_steps;
				var lon_delta = (v_data.lon - last.lng()) / animation_steps;
	
				if (lat_delta != 0 && lon_delta != 0) {
					for ( var i = 0; i < animation_steps; ++i) {
						var lat = last.lat() + lat_delta * (i + 1);
						var lon = last.lng() + lon_delta * (i + 1);
						var op = CreateVehicleUpdateOperation(vehicle, lat, lon);
						updates[i].push(op);
					}
				}
				vehicle.marker.setTitle('id=' + v_data.id + ',entity=' + v_data.entity + ',route=' + v_data.route + ',trip=' + v_data.trip);
			} else {
				// stationary - delete track trail
				console.log('Vehicle:' + id + 'stationary. path.length=' + path.getLength());
				vehicle.marker.setTitle('id=' + v_data.id + ',entity=' + v_data.entity + ',route=' + v_data.route + ',trip=' + v_data.trip + ' [stationary]');
				if (path.getLength() > 2) {
					path.removeAt(0);      // deletes entry 0, shifts the others down
					console.log('len now = ' + path.getLength());
				}
			}
			
		}
		
		var first_update = true;
	
		var ProcessVehicleData = function(data) {
			var vehicles = jQuery.parseJSON(data);
			var updates = [];
			console.log("ProcessVehicleData");
			console.log(data.toString());
			var bounds = new google.maps.LatLngBounds();
			for ( var i = 0; i < animation_steps; ++i) {
				updates.push(new Array());
			}
			jQuery.each(vehicles, function() {
				UpdateVehicle(this, updates);
				bounds.extend(new google.maps.LatLng(this.lat, this.lon));
			});
			if (first_update && ! bounds.isEmpty()) {
				map.fitBounds(bounds);
				first_update = false;
			}
			var applyUpdates = function() {
				if (updates.length == 0) {
					return;
				}
				var fs = updates.shift();
				for ( var i = 0; i < fs.length; i++) {
					fs[i]();
				}
				setTimeout(applyUpdates, 1);
			};
			setTimeout(applyUpdates, 1);	
		};
		
		/**
		 * We create a WebSocket to listen for vehicle position updates from our
		 * webserver.
		 */
		if ("WebSocket" in window) {
			var ws = new WebSocket("ws://" + hostandport + "/data.json");
			ws.onopen = function() {
				console.log("WebSockets connection opened");
			}
			ws.onmessage = function(e) {
				console.log("Got WebSockets message");
				ProcessVehicleData(e.data);
			}
			ws.onclose = function() {
				console.log("WebSockets connection closed");
			}
		} else {
			alert("No WebSockets support");
		}
	  }
      google.maps.event.addDomListener(window, 'load', initialize);
