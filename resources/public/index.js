/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore
 */

/* function to add an user to the employee table */
"use strict";
var format_error = function(e) { 
  return 'Error: ' + e.responseText;
}

var add_employee = function (employee, table) {
  var tr = $('<tr/>').appendTo(table);
  $('<td><a href="/employee/' + employee.id + ' ">' + employee.name + '</a></td>').appendTo(tr);
  $('<td>' + employee.email + '</td>').appendTo(tr);
  $('<td><a href="#">Remove employee</a></td>').appendTo(tr);
};

/* function to render employess in a html list */
var get_employees = function (table) {

  $.ajax({
    url: '/employees',
    dataType: 'json',
    success: function (es) {
      _.each(es, function(e) { 
        add_employee(e, table);
      });
    },
    error: function (e) {
      alert(format_error(e));
    }
  });
  
};

/* main function to render the home page */
$(function () {

  var table = $('#show-all-employees');
  get_employees(table);


  $('#add-employee').submit(function () { 
    var form = $(this);
    $.ajax({
      url : '/employees/add',
      type: 'POST',
      dataType: 'json',
      data: form.serializeArray(),
      success: function (d) { 
        add_employee(d, table);
        form.find(':input').each(function() { this.value = '';}); 
      },
      error: function (e) {
        alert(format_error(e));
      }
    });
    
    return false; // do not allow the normal form behavior
  });

});
