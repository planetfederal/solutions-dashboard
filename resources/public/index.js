/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore
 */

/* function to add an user to the employee table */
"use strict";

var format_error = function(e) { 
  return 'Error: ' + e.responseText;
};

var make_table = function(options) {

  var klasses = options.klasses.join(' ');
  var id = options.id
  var headers = options.headers;

  var _table = $('<table/>', {id: id, 'class': klasses});
  var _thead = $('<thead/>').appendTo(_table);
  var _tr    = $('<tr/>').appendTo(_thead);
  var _tbody = $('<tbody/>').appendTo(_table);

  _.each(headers, function (h) {
    
    $('<th/>',{text: h.text}).appendTo(_tr);

  });
  return _table;
};

var Employee = Backbone.Model.extend({
  url: function () { 
    var base_url = 'employees';

    if (this.isNew()) { 
      return base_url; 
    } else { 
      return base_url + '/' + this.id;
    };
  }
});


var Employees = Backbone.Collection.extend({
    model: Employee,
    url: '/employees'
});


var Index = Backbone.View.extend({

  render: function () { 

    var el = $(this.el);

    var table = make_table({
      id: 'employee-table',
      klasses: ['table', 'table-bordered',],
      headers: [{text: 'Employee name'}, 
                {text: 'Employee email'},
                {text: 'Employee trello'}]
    });

    table.appendTo(el);

    this.collection.each(function (e) { 

      var tr = $('<tr/>').appendTo(table);
      $('<td/>', {text: e.get('name')}).appendTo(tr);
      $('<td/>', {text: e.get('email')}).appendTo(tr);
      $('<td/>', {text: e.get('trello_username')}).appendTo(tr);

    });
    
  }

});

var Application = Backbone.Router.extend({

  routes: { 
    '' : 'index',
    'new': 'new',    
  },

  index: function () { 
    var employees = new Employees();
    employees.fetch({
      success: function () {
        var view = new Index({
          collection: employees,
          el: $('#application')
        });
        view.render();
      },
      error: function () { 
        console.log('error');
      }
    })
  },
  
  new: function () { 
  }

})


var App = {

  Views: {},
  Controllers: {},
  init: function() { 
    new Application();
    Backbone.history.start();
  }
};


/* main function to render the home page */
$(function () {
  App.init();
  var table = $('#show-all-employees');
  
});
