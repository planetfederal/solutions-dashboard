/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore, backbone.js
 */


"use strict";

/* function to show an error to an end user */
var show_error = function (e) {
  var error_div = $('<div>', {'class': 'alert'}),
      error_msg = $('<p/>',  {text: e.responseText});

  var width = 400;
  error_div.css('width', width)
    .css('height', 300)
    .css('margin', 'auto')


  var close = $('<a/>', {'class': 'close', 'data-dismiss': 'alert', text: 'X'}).appendTo(error_div);
  close.click(function () { error_div.remove() }); 

  $('<h3/>',{'class': 'alert-heading',text: 'Something went wrong.'}).appendTo(error_div);
  error_msg.appendTo(error_div);
  error_div.appendTo($('body'));

 };


/* function to populate a from an array of objects */
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

var show_harvest_projects = function (app, raw) {
  var projects = _.pluck(raw, 'project'),
      table    = $('<table/>'),
      thead    = $('<thead/>').appendTo(table),
      tr       = $('<tr/>').appendTo(thead),
      headers = _.first(projects);
  
  for (var header in headers) { 
    $('<th/>',{text: header}).appendTo(tr);
  };
    
  table.appendTo(app);

};

/* */
var AddEmployee = Backbone.View.extend({
  render: function () { 
    var el = $(this.el);
    el.empty();
  }
});

/* */
var show_trello  = function (app, trello_info) {

  var well = $('<div/>', {'class': 'well'}).appendTo(app);

  var title = $('<h3/>',{text: 'Trello projects associated with user'}).appendTo(well);
  var ul = $('<ul/>')

  var projects = trello_info.projects;

  _.map(projects, function(project) { 
    var li = $('<li>').appendTo(ul);
    $('<p/>', {text:project.name}).appendTo(li);

    var tasks = $('<ul/>').appendTo(li);

    _.map(project.tasks, function(task) { 

      var task_li = $('<li/>').appendTo(tasks);

      $('<p/>', {text: task.name}).appendTo(task_li);
      $('<p/>', {text: task.due}).appendTo(task_li);

    });


  });

  ul.appendTo(well);
  

}
var show_harvest = function (app, harvest_info) { 

}


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
      var rm = confirm('Are you sure you want to remove this user?');
      if (rm) { 
        model.destroy({
          success: function () { 
            window.location = '/';
          },
          error: function (m, e) { 
            alert(e);
          }
        });
      };
    });

    $('<hr/>').appendTo(wrap);

    var attrs = this.model.attributes;
    for (var key in attrs) { 

      var row = $('<div/>', {'class': 'row'})
      var column_name  = $('<p/>', {'class': 'span3',text: key}).appendTo(row);
      var column_value = $('<p/>', {'class': 'span3', text: attrs[key]}).appendTo(row);
      row.appendTo(wrap);
      
    }

    wrap.appendTo(app);

    $.ajax({
      url: '/get-trello-info/' + this.model.get('trello_username'),
      success: function (trello_info) {
        show_trello(app, trello_info);
      },
      error:   function (e) {show_error(e)}
    });

    $.ajax({
      url: '/get-harvest-info/' + this.model.get('harvest_id'),
      success: function (harvest_info) { 
        show_harvest(app, harvest_info);
      },
      error: function (e) {show_error(e);}
    });

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
    'harvest': 'harvest',
    'employee/:id': 'show_employee'
    
  },

  harvest: function () { 
    var app = $('#application').empty();
    reset_nav('#harvest');

    $.ajax({
      url: '/show-harvest-projects',
      success: function (projects) {
        show_harvest_projects(app, projects);
      },
      error: function (e) {show_error(e); }
    });
  },
 
  show_employee: function (id) { 
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

        {name: 'harvest_id',
         label: 'Employee Harvest id',
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
        error: function (m, e) {
          show_error(e);
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
