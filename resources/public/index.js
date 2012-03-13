/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore, backbone.js
 */


"use strict";

/* function to show an error to an end user */
var show_error = function (e) {

  // var error_div = $('<div>', {'class': 'alert'}),
  //     width = 400,
  //     error_msg = $('<p/>',  {text: e.responseText});

  // error_div.css('width', width)
  //   .css('height', 300)
  //   .css('margin', 'auto')


  // var close = $('<a/>', {'class': 'close', 'data-dismiss': 'alert', text: 'X'}).appendTo(error_div);
  // close.click(function () { error_div.remove() }); 

  // $('<h3/>',{'class': 'alert-heading',text: 'Something went wrong.'}).appendTo(error_div);
  // error_msg.appendTo(error_div);
  // error_div.appendTo($('body'));

 };


var progress = function () {
  
};

/* This function gets called once */
var progress_bar = function (options) { 
  
  var wrap = options.wrap; //  the wrapper for the progress bar
  
  var init =  setInterval(function () { progress(wrap) }, 1000);
  return init;
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
    $('<td/>', {text: p.budget}).appendTo(tr);
    $('<td/>', {text: p.hourly_rate}).appendTo(tr);
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
  var title = $('<h3/>', {text: 'Trello information'}).appendTo(app),
      table = build_table({
        id: 'trello_table',
        'classes': ['table','table-bordered'],
        headers: ['Project', '']
      });
  table.appendTo(app);

  _.map(trello_info.projects, function (p) { 
    var tr = $('<tr/>'),
        name = $('<td/>',{text: p.name}).appendTo(tr),
        tasks = $('<td/>').appendTo(tr);      
    tr.appendTo(table);

    var task_table = build_table({
      id: 'trello_tasks',
      'classes': ['table', 'table-bordered'],
      headers: ['Task name', 'Task due date']
    }).appendTo(tasks);

    _.map(p.tasks, function (t) {
      var ttr = $('<tr/>').appendTo(task_table);
      $('<td/>', {text: t.name}).appendTo(ttr);
      $('<td/>', {text: t.due}).appendTo(ttr);
    });

  });  

};

/* */
var show_harvest = function (app, projects) { 

  var title = $('<h3/>', {text: 'Harvest information'}).appendTo(app),
      table = build_table({
        id: 'harvest_table',
        'classes': ['table', 'table-bordered'],
        headers: ['Project name','Tasks']
      });
  table.appendTo(app);
  
  var round = function (n) { 
    return Math.round(n * 100)/100;
  };

  var sum_hours = function (t) { 
    return _.reduce(_.pluck(t.entries, 'hours'), function(res, n) { return res + +n; }, 0);
  };

  _.map(projects, function (p) {

    var tr = $('<tr/>');
    $('<td/>', {text: p.name}).appendTo(tr);
    var tasks = $('<td/>').appendTo(tr);

    var task_table = build_table({
      id: 'task-table',
      'classes': ['table', 'table-bordered'],
      headers: ['Task name', 'Task Hours']
    }).appendTo(tasks);

    
    _.map(p.tasks, function(t) { 
      var ttr = $('<tr/>');
      $('<td/>', {text: t.name}).appendTo(ttr);
      $('<td/>', {text: round(sum_hours(t))}).appendTo(ttr);
      ttr.appendTo(task_table);
      
    });

    tr.appendTo(table);

  });

};


/* */
var populate_user_info = function (model, user_info) {
  user_info.empty();
  var attrs = model.attributes;
  for (var key in attrs) { 
    var row = $('<div/>', {'class': 'row'});
    $('<p/>', {'class': 'span3',text: key}).appendTo(row);
    $('<p/>', {'class': 'span3', text: attrs[key]}).appendTo(row);
    row.appendTo(user_info);
  };      
};

var format_form_data = function (form) { 
  var data = {};
  _.each(form.serializeArray(), function(field) { 
    data[field.name] = field.value;
  });
  return data;
};

/* */
var ViewEmployee = Backbone.View.extend({

  render: function (app)  { 

    var model = this.model,
        wrap  = $('<div/>', {'class': 'well'}),
        tool_list = $('<div/>', {'class':'btn-group'}).appendTo(wrap),
        edit = $('<a/>', {text: 'Edit user', 'class': 'btn'}).appendTo(tool_list),
        email = $('<a/>', {text: 'Send priority email', 'class': 'btn'}).appendTo(tool_list),
        remove = $('<a/>', {text: 'Remove user', 'class': 'btn btn-danger'}).appendTo(tool_list);

    email.click(function () {
      $.ajax({
        url: '/employees/' + model.get('id') + '/send-email',
        type: 'POST',
        success: function () { alert('Your email has been sent'); } 
      });
    });

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

    var user_info = $('<div/>');    
    user_info.appendTo(wrap);

    populate_user_info(model, user_info); // populate the user info section

    edit.click(function () {
      user_info.empty();
      var form = $('<form/>');

      var inputs = _.filter(_.keys(model.attributes), function(attr) { 
        if (attr !== 'id') {
          return attr;
        }
      });

      _.map(inputs, function(input) {
        $('<label/>', {text: input}).appendTo(form);
        $('<input/>', {name: input, value: model.attributes[input]}).appendTo(form);

      });
            
      form.appendTo(user_info);
      var cancel = $('<a/>',{'class': 'btn', 'text': 'Cancel'}).appendTo(user_info);

      cancel.click(function () {
        populate_user_info(model, user_info);
      });

      var save   = $('<a/>', {'class': 'btn', 'text': 'Save'}).appendTo(user_info);
      save.click(function () {
        var data = format_form_data(form);
        model.save(data, {
          success: function () { 
            populate_user_info(model, user_info); // populate the user info section
          },
          error:   function (m, e) {
            alert(e);
          }
        });
      });

    });

    wrap.appendTo(app);

    $.ajax({
      url: '/employees/' + this.model.get('id') + '/get-trello-info',     
    }).done(function (trello_info) {
      show_trello(app, trello_info);
    }).fail(function (e) {show_error(e)});

    $.ajax({
      url: '/employees/' + this.model.get('id') + '/get-harvest-info',
    }).done(function (harvest) {
      show_harvest(app, harvest);
    }).fail(function (e) { show_error(e)});

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
