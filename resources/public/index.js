/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore, backbone.js
 */


"use strict";

/* function to show an error to an end user */
var show_error = function (e) {
  var error_div = $('<div>', {'class': 'alert'}),
      width = 400,
      error_msg = $('<p/>',  {text: e.responseText});

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

  var classes = options.classes.join(' '),
      id = options.id,
      headers = options.headers,
      _table = $('<table/>', {id: id, 'class': classes}),
      _thead = $('<thead/>').appendTo(_table),
      _tr    = $('<tr/>').appendTo(_thead),
      _tbody = $('<tbody/>').appendTo(_table);

  _.each(headers, function (h) {
    
    $('<th/>',{text: h}).appendTo(_tr);

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
      classes: ['table', 'table-bordered'],
      headers: ['Employee name', 
                'Employee email',
                'Employee trello name']
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

var show_harvest_projects = function (app, projects) {
  
  var table = build_table({
    id: 'harvest-table',
    classes: ['table', 'table-bordered'],
    headers: ['Name','Budget','Hourly rate']
  });

  table.appendTo(app);

  var tbody = $('<tbody/>').appendTo(table);

  _.map(projects, function(p) { 
    var tr = $('<tr/>').appendTo(tbody);
    $('<td/>', {text: p.name}).appendTo(tr);
    $('<td/>', {text: '----'}).appendTo(tr);
    $('<td/>', {text: '----'}).appendTo(tr);
  });

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

  var well = $('<div/>', {}).appendTo(app),
      ul = $('<ul/>');

  _.map(trello_info.projects, function(project) { 
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
var show_harvest = function (app, harvest) { 
  console.log(app);
  console.log(harvest);
}


/* */
var ViewEmployee = Backbone.View.extend({

  render: function (app)  { 

    var model = this.model,
        wrap  = $('<div/>', {'class': 'well'}),
        tool_list = $('<div/>', {'class':'btn-group'}).appendTo(wrap),
        edit = $('<a/>', {text: 'Edit user', 'class': 'btn'}).appendTo(tool_list),
        remove = $('<a/>', {text: 'Remove user', 'class': 'btn btn-danger'}).appendTo(tool_list);

    remove.click(function () {
      var rm = confirm('Are you sure you want to remove this user?');
      if (rm) { 
        model.destroy({
          success: function () { 
            window.location = '/';
          },
          error: function (m, e) { 
            show_error(e);
          }
        });
      };
    });

    $('<hr/>').appendTo(wrap);

    var attrs = this.model.attributes;
    for (var key in attrs) { 
      var row = $('<div/>', {'class': 'row'});
      $('<p/>', {'class': 'span3',text: key}).appendTo(row);
      $('<p/>', {'class': 'span3', text: attrs[key]}).appendTo(row);
      row.appendTo(wrap);      
    };

    wrap.appendTo(app);

    $.ajax({
      url: '/employees/' + this.model.get('id') + '/get-trello-info',
      success: function (trello_info) {
        show_trello(app, trello_info);
      },
      error: function (e) {show_error(e)}
    });

    $ .ajax({
      url: '/employees/' + this.model.get('id') + '/get-harvest-info',
      success: function (harvest) { 
        show_harvest(app, harvest);
      },
      error:   function (e) {show_error(e);}
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
      async: false,
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
