if (document.getElementById("pie1")) {
    $(function() {
        "use strict";
        setTimeout(function(){
            $(document).ready(function(){
                var chart = c3.generate({
                    bindto: '#chart-donut', // id of chart wrapper
                    data: {
                        columns: [
                            // each columns data
                            ['data1', document.getElementById("pie1").value],
                            ['data2', document.getElementById("pie2").value],
                            ['data3', document.getElementById("pie3").value],
                        ],
                        type: 'donut', // default type of chart
                        colors: {
                            'data1': Aero.colors["teal"],
                            'data2': Aero.colors["lime"],
                            'data3': Aero.colors["orange"],
                        },
                        names: {
                            // name of each serie
                            'data1': 'Open',
                            'data2': 'Close',
                            'data3': 'Canceled',
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
    });
}