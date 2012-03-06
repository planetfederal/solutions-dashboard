/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore
 */

/* function to add an user to the employee table */
"use strict";

var format_error = function (e) {
  return 'Error: ' + e.responseText;
 };


/* */
var populate_form = function (options) { 
  var form = options.form;
  var fields = options.fields;

  _.map(fields, function(field) { 

    $('<label/>', {text: field.label}).appendTo(form);
    $('<input/>', {
      name: field.name,
      'class': 'span3',
      type: field.type}).appendTo(form);                   
  });
  
  $('<hr/>').appendTo(form);
  
  options.submit.appendTo(form);
};

/* function to make building a table a little nicer */
var build_table = function(options) {

  var klasses = options.klasses.join(' ');
  var id = options.id;
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
    el.empty();
    var table = build_table({
      id: 'employee-table',
      klasses: ['table', 'table-bordered'],
      headers: [{text: 'Employee name'}, 
                {text: 'Employee email'},
                {text: 'Employee trello name'}]
    });

    table.appendTo(el);

    this.collection.each(function (e) { 

      var tr = $('<tr/>').appendTo(table);
      var title = $('<td/>').appendTo(tr);

      $('<a/>', {text: e.get('name'),
                 href: '#employee/' + e.get('id')}).appendTo(title);
      $('<td/>', {text: e.get('email')}).appendTo(tr);
      $('<td/>', {text: e.get('trello_username')}).appendTo(tr);

    });
    
  }

});


/* */
var AddEmployee = Backbone.View.extend({
  render: function () { 
    var el = $(this.el);
    el.empty();
  }
});

/* */
var ViewEmployee = Backbone.View.extend({

  render: function (app)  { 
    var model = this.model;
    var wrap  = $('<div/>', {'class': 'well'});

    var tool_list = $('<div/>', {'class':'btn-group'}).appendTo(wrap);
    var edit = $('<a/>', {text: 'Edit user',
               'class': 'btn'}).appendTo(tool_list);

    var remove = $('<a/>', {text: 'Remove user', 
                            'class': 'btn btn-danger'}).appendTo(tool_list);
    remove.click(function () {
      model.destroy({
        success: function () { 
          window.location = '/';
        },
        error: function (m, e) { 
          alert(e);
        }
      });
    });

    $('<h3/>', {text: this.model.get('name')}).appendTo(wrap);    
    $('<p/>', {text: this.model.get('trello_username')}).appendTo(wrap);
    $('<p/>', {text: this.model.get('email')}).appendTo(wrap);

    wrap.appendTo(app);
    
    return this;
  }

});

var reset_nav = function (_active, all) { 
// this is bull shit... but it seems to work
  $('#dash-nav').children().each(function() { $(this).removeClass('active'); });
  if (!all) {
    $('#dash-nav').find(_active).each(function () { $(this).addClass('active') ;});
  };

};

var Application = Backbone.Router.extend({
  routes: { 
    '' : 'index',
    'new': 'new',
    'employee/:id': 'show_employee'
  },

  show_employee: function(id) { 
    var app = $('#application').empty();
    reset_nav('', true);

    var employee = new Employee({id: id});
    employee.fetch({
      success: function () { 
        var view = new ViewEmployee({
          model: employee
        });
        view.render(app);
      }
    });

  },

  index: function () { 
    reset_nav('#index');
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
    });
  },
  
  new: function () { 

    reset_nav('#new');
    var app = $('#application').empty();

    var form = $('<form/>', {'class': 'form-horizontal'}).appendTo(app);
    var submit = $('<a />', 
                   {text: 'Add new employee',
                    'class': 'btn',
                    id: 'add-employee'});
    populate_form({
      form: form,
      submit: submit,
      fields: [
        {name: 'name', 
         label: 'Employee name', 
         type: 'text' },
        {name: 'trello_username',
         label: 'Employee trello username',
         type: 'text' },
        {name: 'email',
         label: 'Employee email',
         type: 'text'}]
    });

    submit.click(function () { 

      var data = {};
      _.each(form.serializeArray(), function(field) { 
        data[field.name] = field.value;
      });

      var employee = new Employee();

      employee.save(data, {
        success: function () { 
          window.location = '/'; // is there a better way of doing this.
        },
        error: function () {
          alert('something blew up');
        }
      });      

      return false;
    });
    
  }

});


/* main function to render the home page */
$(function () {
  new Application();
  Backbone.history.start();  
});
