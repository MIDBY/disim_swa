//[custom Javascript]
//Project:	Aero - Responsive Bootstrap 4 Template
//Version:  1.0
//Last change:  15/12/2019
//Primary use:	Aero - Responsive Bootstrap 4 Template
//should be included in all pages. It controls some layout
if (document.getElementById("chartData1")) {
    $(function() {
        "use strict";
        initSparkline();
        initC3Chart();    
    });
}
function getMonthNames() {
    const currentMonth = new Date().getMonth();
    const monthNames = [];
    for (let i = currentMonth - 11; i <= currentMonth; i++) {
      const monthName = new Date(2000, i, 1).toLocaleString('default', { month: 'short' });
      monthNames.push(monthName.substring(0, 3));
    }
    return monthNames;
}

function getDataColumns(str, integers) {
    return [str, ...integers];
  }
function initSparkline() {
    $(".sparkline").each(function() {
        var $this = $(this);
        $this.sparkline('html', $this.data());
    });
}
if (document.getElementById("chartData1")) {
    function initC3Chart() {
        setTimeout(function(){ 
            $(document).ready(function(){
                var chart = c3.generate({
                    bindto: '#chart-area-spline-sracked', // id of chart wrapper
                    data: {
                        columns: [
                            // each columns data
                            getDataColumns('data1', document.getElementById("chartData1").value.split(',').map(Number)),
                            getDataColumns('data2', document.getElementById("chartData2").value.split(',').map(Number)),
                            getDataColumns('data3', document.getElementById("chartData3").value.split(',').map(Number)),
                        ],
                        type: 'area-spline', // default type of chart
                        groups: [
                            [ 'data1', 'data2', 'data3']
                        ],
                        colors: {
                            'data1': Aero.colors["#adff8c"],
                            'data2': Aero.colors["#ffc379"],
                            'data3': Aero.colors["#71c1fd"],
                        },
                        names: {
                            // name of each serie
                            'data1': 'New Requests',
                            'data2': 'Proposals',
                            'data3': 'New Orderings',
                        }
                    },
                    axis: {
                        x: {
                            type: 'category',
                            // name of each category
                            categories: getMonthNames()
                        },
                    },
                    legend: {
                        show: true, //hide legend
                    },
                    padding: {
                        bottom: 0,
                        top: 0
                    },
                });
            });    
            $(document).ready(function(){
                var chart = c3.generate({
                    bindto: '#chart-pie', // id of chart wrapper
                    data: {
                        columns: [
                            // each columns data
                            ['data1', document.getElementById("pieValue1").value],
                            ['data2', document.getElementById("pieValue2").value],
                            ['data3', document.getElementById("pieValue3").value],
                            ['data4', document.getElementById("pieValue4").value],
                        ],
                        type: 'pie', // default type of chart
                        colors: {
                            'data1': Aero.colors["#adff8c"],
                            'data2': Aero.colors["#ffc379"],
                            'data3': Aero.colors["#71c1fd"],
                            'data4': Aero.colors["#ff80ed"],
                        },
                        names: {
                            // name of each serie
                            'data1': document.getElementById("pieName1").value,
                            'data2': document.getElementById("pieName2").value,
                            'data3': document.getElementById("pieName3").value,
                            'data4': 'Others',
                        }
                    },
                    axis: {
                    },
                    legend: {
                        show: true, //hide legend
                    },
                    padding: {
                        bottom: 0,
                        top: 0
                    },
                });
            });
        }, 500);
    }
}
if(document.getElementById("mapData")) {
    setTimeout( function(){
        "use strict";
        const mapDataValue = document.getElementById("mapData").value;	
        const pairs = mapDataValue.split(',');
        const mapData = new Map();    
        const mapColors = [];
        pairs.forEach((pair) => {
            const [key, value] = pair.split(':');
            const r = Math.floor(Math.random() * 256).toString(16);
            const g = Math.floor(Math.random() * 256).toString(16);
            const b = Math.floor(Math.random() * 256).toString(16);
            mapData.set(key, parseInt(value));
            mapColors.push('#'+r+g+b);
        });
        if( $('#world-map-markers').length > 0 ){
            $('#world-map-markers').vectorMap({
                map: 'world_mill_en',
                backgroundColor: 'transparent',
                borderColor: '#fff',
                borderOpacity: 0.25,
                borderWidth: 0,
                //color: '#e6e6e6',
                regionStyle : {
                    initial : {
                        fill :  '#f4f4f4',
                    }
                },
                series: {
                    regions: [{
                        values: Object.fromEntries(mapData),
                        scale: mapColors,
                        normalizeFunction: 'none',
                    }]
                },
                onRegionTipShow: function(e, el, code){
                    if(mapData != null && mapData.get(code) > 0) {
                        el.html(el.html()+': '+mapData.get(code)+' clients');
                    } else {
                        el.html(el.html()+': 0 clients');
                    }
                },
                hoverOpacity: null,
                zoomOnScroll: false,
                scaleColors: ['#000000', '#000000'],
                selectedColor: '#000000',
                selectedRegions: [],
                enableZoom: false,
                hoverColor: '#fff',
            });
        }
    }, 800);
}