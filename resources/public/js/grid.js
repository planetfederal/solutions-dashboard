/*
 *
 *
 */
"use strict";

function Grid (options) {

  this.columns    = options.columns;
  this.element    = options.element;  
  this.collection = options.collection;

  this.element.addClass('table');
  this.element.addClass('table-bordered');

  this.build_header();
  this.render();

};


Grid.prototype.build_header = function() {

  var thead = $('<thead/>').appendTo(this.element),
      tr    = $('<tr/>').appendTo(thead);

  _.map(this.columns, function(column) {
    $('<th/>', {
      text: column.name,
      id: 'column_' + column.id
    }).appendTo(tr);

  });

}
var default_renderer = function(value, row) { return $('<p/>', {text: value}); };

/* how to handle ordering? */
Grid.prototype.render = function () {
  var tbody = $('<tbody/>', {}).appendTo(this.element);
  var columns = this.columns;

  this.collection.map(function(row) {
    var tr = $('<tr/>').appendTo(tbody);
    
    _.map(columns, function(column) {
      var value = row.get(column.field);

      if (column.renderer === undefined) {
        column.renderer = default_renderer;
      }

      var cell = $('<td>');
      column.renderer(value, row).appendTo(cell);
      cell.appendTo(tr);

    });

  });

}
