'use strict';
angular.module('myApp', []).controller('RutasController', ['$scope', '$http', function($scope, $http) {

        /*
         * INICIALIZACION DE LA APP
         */

        // Inicializamos el layer de OpenStreetMaps
        var layer = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        });

        // Inicializamos el mapa centrado en Asuncion
        $scope.map = L.map('map', {
            center: [-25.3200, -57.5292],
            layers: [layer],
            zoom: 13
        });
        
        //Creamos el layerGroup para el tráfico
        $scope.trafico = L.layerGroup();

        // Obtenemos la lista de rutas
        $http.get('http://209.208.108.214:8080/autotracks/resources/rutas').success(function(data) {
            $scope.rutas = data;
        });

        /*
         * METODOS PRIVADOS
         */

        /**
         * Obtiene la lista de localizaciones de la ruta seleccionada por el usuario.
         * 
         * @returns {undefined}
         */
        var obtenerLocalizaciones = function() {
            var id = $scope.rutaSeleccionada.id
            $http.get('http://209.208.108.214:8080/autotracks/resources/rutas/' + id + '/localizaciones').success(function(data) {
                $scope.localizaciones = data;
                eliminarTrayectos();
                dibujarTrayectoOriginal();
                if (tieneTrayectoMatcheado()) {
                    dibujarTrayectoMatcheado();
                }
            });
        };
        
        /**
         * Dibuja el estado del trafico
         * 
         * @returns {undefined}
         */
        var dibujarTrafico = function () {
            $http.get('http://209.208.108.214:8080/autotracks/resources/rutas/trafico').success(function(data) {
                $scope.trafico.eachLayer(function (layer) {
                    $scope.map.removeLayer(layer);
                });

                $scope.trafico.clearLayers();
                var polyline;
                var color;
                for (var i = 0; i < data.length; i++) {
                    if (data[i].cantidad > 4) {
                        color = 'red';
                    } else if (data[i].cantidad > 3) {
                        color = 'yellow';
                    } else {
                        color = 'green';
                    }

                    polyline = L.polyline([L.latLng(data[i].y1,data[i].x1),
                        L.latLng(data[i].y2,data[i].x2)], {color: color});
                    
                    console.log('el color es: ' + color);
                    $scope.trafico.addLayer(polyline);
                    $scope.map.addLayer(polyline);
                }
                //$scope.trafico.addTo($scope.map);
            });
        };

        /**
         * Eliminar ambos trayectos del mapa, el original y el matcheado.
         * 
         * @returns {undefined}
         */
        var eliminarTrayectos = function() {
            if ($scope.trayecto1) {
                $scope.map.removeLayer($scope.trayecto1);
            }
            if ($scope.trayecto2) {
                $scope.map.removeLayer($scope.trayecto2);
            }
        };

        /**
         * Dibuja el trayecto original en el mapa. El trayecto original es el
         * que se obtiene de la aplicacion de Android de tracking.
         * 
         * @returns {undefined}
         */
        var dibujarTrayectoOriginal = function() {
            var latlngs = $scope.localizaciones.map(function(l) { 
                return L.latLng(l.latitud, l.longitud); 
            });
            $scope.trayecto1 = L.polyline(latlngs, {color: 'red'});
            $scope.map.addLayer($scope.trayecto1);
            $scope.map.fitBounds($scope.trayecto1.getBounds());
        };

        /**
         * Dibuja el trayecto matcheado en el mapa. El trayecto matcheado es el
         * que se obtiene luego de ejecutar el proceso de map matching sobre un
         * trayecto original.
         * 
         * @returns {undefined}
         */
        var dibujarTrayectoMatcheado = function() {
            var latlngs = $scope.localizaciones.map(function(l) {
                return L.latLng(l.latitudMatch, l.longitudMatch);
            });
            $scope.trayecto2 = L.polyline(latlngs, {color: 'green'});
            $scope.map.addLayer($scope.trayecto2);
        };

        /**
         * Retorna 'true' si el trayecto original tiene un correspondiente
         * trayecto matcheado.
         * 
         * @returns {Boolean} 'true' si existe un trayecto matcheado.
         */
        var tieneTrayectoMatcheado = function() {
            return $scope.localizaciones[0].latitudMatch !== null;
        };

        /*
         *  METODOS PUBLICOS
         */

        /**
         * Retorna 'true' si la ruta pasada como parametro es la ruta que fue 
         * selecionada por el usuario.
         *  
         * @param {Ruta} ruta
         * @returns {Boolean} si la ruta es la que fue seleccionada.
         */
        $scope.isActive = function(ruta) {
            return ruta === $scope.rutaSeleccionada;
        };

        /**
         * Guarda la ruta como ruta seleccionada. Dibuja el trayecto original y 
         * el trayecto generado por el map-matching.
         * @param {type} ruta
         * @returns {undefined}
         */
        $scope.seleccionar = function(ruta) {
            $scope.rutaSeleccionada = ruta;
            obtenerLocalizaciones();
        };

    }]);