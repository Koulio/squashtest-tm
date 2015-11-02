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
/*
* View derivated from abstractCutomReportChart  for representing PieChart in CustomReport context.
*/

define(["jquery", "underscore", "./abstractCustomReportChart", "jqplot-pie"], function($,_,AbstractCustomReportChart){



	return AbstractCustomReportChart.extend({

		// The color for '0%' charts
		EMPTY_COLOR : ["#EEEEEE"],


		// ************************* rendering  ***********************

		render : function(){

			var pieserie = this.getData();
			var conf = this.getConf(pieserie);

			var series = this.getSeries();
			var legends = this.getLegends();
			legends = _.flatten(legends);

			var jqplotSeries = [_.zip(legends,series)];

			console.log(series);
			console.log(legends);
			console.log(jqplotSeries);

			this.draw(jqplotSeries, conf);

		},

		// ************************** configuration *************************


		// returns data that works, eliminating corner cases.
		getData : function(){
			var serie = this.getSeries();
			return new PieSerie(serie);
		},


		getConf : function(pieserie){

			var colorsAndLabels;

			if (pieserie.isEmpty){
				colorsAndLabels = this._getEmptyConf(pieserie);
			}
			else if (pieserie.isFull){
				colorsAndLabels = this._getFullConf(pieserie);
			}
			else{
				colorsAndLabels = this._getNormalConf(pieserie);
			}

			return {
				seriesDefaults : {
					renderer : jQuery.jqplot.PieRenderer,
					rendererOptions : {
						showDataLabels : true,
						dataLabels : colorsAndLabels.labels,
						startAngle : -45,
						shadowOffset : 0,
						sliceMargin : 1.5
					},
					showHighlight : false
				},
				grid : {
					background : '#FFFFFF',
					drawBorder : false,
					borderColor : 'transparent',
					shadow : false,
					shadowColor : 'transparent'
				},
				height: 400,
				width: 600,
				legend:{
				            show:true,
				            placement: 'inside',
				            rendererOptions: {
				                numberRows: pieserie.length
				            },
				            location:'se',
				            marginTop: '15px',
                    fontSize : 16,
                    fontColor : "#000000"
				}
				//seriesColors : colorsAndLabels.colors
			};
		},

		_getEmptyConf : function(pieserie){
			return {
				labels : ["0% (0)"],
				colors :  this.EMPTY_COLOR
			};
		},

		_getFullConf : function(pieserie){
			return {
				labels : [ "100% ("+pieserie.total+")" ],
				//colors : [ this.colorscheme[pieserie.nonzeroindex]]
			};
		},

		_getNormalConf : function(pieserie){
			var labels = this._createLabels(pieserie);
			return {
				labels : labels,
				colors : this.colorscheme
			};
		},

		_createLabels : function(serie){

			var total=0,
				labels = [];

			for (var s=0, lens=serie.length; s<lens; s++){
				total += serie[s];
			}
			var coef = 100.0/total;

			var perc, dec;
			for (var i=0, leni=serie.length; i<leni ;i++){
				dec=serie[i];
				perc = (dec * coef).toFixed();
				labels.push(perc+"% ("+dec+")");
			}

			return labels;
		}
	});


	/*
	 * Some statistics on the serie to be plotted must be collected first, because there are some corner cases
	 * when the pie is only 1 slice (namely '0%' and '100%' charts).
	 *
	 * The View object will use those meta informations to make jqplot behave properly when those corner cases are met.
	 *
	 */
	function PieSerie(serie){

		var total=0,
			nonzeroindex=-1,
			nonzerocount=0,
			length = serie.length,
			_val;

		for (var i=0;i<length;i++){
			_val = serie[i];
			if (_val>0){
				total += _val;
				nonzerocount++;
				nonzeroindex = i;
			}
		}

		// collect the stats
		this.total = total;
		this.isEmpty = (total===0);
		this.isFull = (nonzerocount===1);
		this.nonzeroindex = (this.isFull) ? nonzeroindex : -1;

		// plot data : special data for special cases, normal data in normal cases.
		this.plotdata = (this.isEmpty || this.isFull) ? [[1]] : [ serie ];

		// push the data onto self
		Array.prototype.push.apply(this, serie);

	}

	PieSerie.prototype = [];
	PieSerie.prototype.constructor = PieSerie;



});
