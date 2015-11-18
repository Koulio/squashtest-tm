/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

define(["backbone","./chart-render-utils","./customReportPieView","./customReportBarView",
  "./customReportLineView","./customReportCumulativeView","./customReportComparativeView"],
		function(Backbone,renderUtils,PieView,BarView,LineView,CumulativeView,ComparativeView){


	function generateBarChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});

    var axis = jsonChart.axes;

    var title = jsonChart.name;

		var Bar = BarView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			}

		});


		return new Bar({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : series,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}

	function generateComparativeChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

    ticks = _.uniq(ticks);//abscissa are combinaison axis1Value/axis2Value

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});

    //First we regroup each value in serie with it's abscisse.
    //the server give us an object like :
    //abscisse : {[axis1Value1,axis2Value1],[axis1Value2,axis2Value2],...},
    //serie : {"serie1" : [Number1, Number2, Number3,...],"serie2":[Number1, Number2, Number3,...]}
    //and we need something like  [[[axis1Value1,axis2Value1,Number1],[axis1Value2,axis2Value2,Number2]],[serie2...]]
    var comparativeSeries = _.map(series, function(serie){
        return _.map(serie, function(value,index){
            var result = [jsonChart.abscissa[index],value];
            return _.flatten(result);
        });
    })[0];//for V1.13 we have only one serie in each json chart instance

    var computedSeries = _.chain(comparativeSeries)
      .groupBy(function (memo) {//group by on second axis ie series on comparative chart
        return memo[1];
      })
        //we have now an object :
        //{
        //axis2Value1 : [[axis1Value1,Value],[axis1Value2,Value]],
        //axis2Value2 : [[axis1Value1,Value],[axis1Value2,Value]]
        //}
        //ie we have an object were axis2 values are keys and values are series
      .map(function (serie) {
        var result = [];
        _.each(ticks,function(tick,index) {
          var valueForOneTick = _.find( serie, function (value) {//inside each serie retrive the value for a given tick
            return value[0]===tick;
          });
          //if the series has no value for given tick, _.find() return undefined so we affect 0 instead,
          //else we affect value[2] wich is the numeric value of the double groupby axis1Value/axis2Value
          valueForOneTick = valueForOneTick === undefined ? 0 : valueForOneTick[2];
          //index +1 because for HORIZONTAL stacked bar chart jqplot wait series like :
          //[[[x,1],[y,2],[z,3]],
          //[[x1,1],[y1,2],[z1,3]],
          //[[x2,1],[y2,2],[z2,3]],
          //]
          result.push([valueForOneTick,index+1]);
        });
        return result;
      })
    .value();


    console.log("comparativeSeries");
    console.log(comparativeSeries);
    console.log("computedSeries");
    console.log(computedSeries);

    var axis = jsonChart.axes;

    var title = jsonChart.name;

    var seriesLegend = _.chain(comparativeSeries)
      .groupBy(function (memo) {//group by on second axis ie series on comparative chart
        return memo[1];
      })
      .keys()
      .value();

    console.log("seriesLegend");
    console.log(seriesLegend);

		var Comparative = ComparativeView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			},

      getSeriesLegends : function () {
        return this.model.get('seriesLegend');
      }

		});


		return new Comparative({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : computedSeries,
        title : title,
        axis : axis,
        seriesLegend : seriesLegend
			},{
				url : "whatever"
			})
		});
	}

  function generateLineChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});

    var axis = jsonChart.axes;

    var title = jsonChart.name;

		var Line = LineView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			}

		});


		return new Line({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : series,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}

  function generateCumulativeChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});
    //Now transforming series -> cumulative series
    //ex : [[1,2,3,4],[1,3,5]] -> [[1,3,6,10],[1,4,9]]
    var cumulativeSeries =_.map( series, function( serie,indox ){
        var resultSerie =  _.map( serie, function( value,index ){
            var memoSerie = serie.slice(0,index+1);
            var result =  _.reduce( memoSerie, function (memo, num) {
              return memo + num;
            }, 0);
            return result;
        });
        return resultSerie;
    });

    var axis = jsonChart.axes;

    var title = jsonChart.name;

		var Cumulative = CumulativeView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			}

		});


		return new Cumulative({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : cumulativeSeries,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}


	function generatePieChart(viewID, jsonChart){

		var Pie = PieView.extend({

			getSeries : function(){
				return this.model.get('series');
			},

      getLegends : function(){
				return this.model.get('legends');
			}

		});

		var series = jsonChart.getSerie(0);
    var legends = jsonChart.abscissa;
    var title = jsonChart.name;
    var axis = jsonChart.axes;

		return new Pie({
			el : $(viewID),
			model : new Backbone.Model({
				series : series,
        legends : legends,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}

	function generateTableChart(viewID, jsonChart){
		// NOOP : the DOM has it all already
		// TODO : use dashboard/basic-objects/table-view for
		// the sake of consistency
	}

  function buildChart (viewID, jsonChart) {
    console.log("FACTORY BUILD CHART");
    console.log(viewID);
    jsonChart = renderUtils.toChartInstance(jsonChart);
    switch(jsonChart.type){
      case 'PIE' : return generatePieChart(viewID, jsonChart);
      case 'BAR' : return generateBarChart(viewID, jsonChart);
      case 'LINE' : return generateLineChart(viewID, jsonChart);
      case 'CUMULATIVE' : return generateCumulativeChart(viewID, jsonChart);
      case 'COMPARATIVE' : return generateComparativeChart(viewID, jsonChart);
      default : throw jsonChart.chartType+" not supported yet";
    }
  }

	return  {
		buildChart : buildChart
	};
});