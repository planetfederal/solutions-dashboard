/* Javascript code to handle user interaction on the main page
 * Requres jquery, underscore
 */

/* function to add an user to the employee table */
"use strict";
var format_error = function(e) { 
  return 'Error: ' + e.responseText;
}

var remove_user = function (e) { 
  var id = e.target.id.split('-')[1];
  var remove_p = confirm('Are you sure that you want to remove that user?');
  if (remove_p) { 
    $.ajax({
      url: '/employees',
      type: 'DELETE',
      data: {id: id},
      success: function() {
        var row = $('#row-' + id);
        row.remove();
      }
    })
  };
};

/* function to add an employee to the employee table */
var add_employee = function (employee, table) {
  var tr = $('<tr/>', {id: 'row-' + employee.id}).appendTo(table);

  $('<a/>', { text: employee.name, 
              href: '/employee/' + employee.id})
    .appendTo($('<td/>').appendTo(tr));

  $('<td/>',{text: employee.email}).appendTo(tr);

  var ahref = $('<a/>', { href: '#',
              'class': 'remove-links',
              id: 'ahref-' + employee.id,
              text: 'Remove employee'})
    .appendTo($('<td/>').appendTo(tr))

  ahref.click(function (e) { remove_user(e); });

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

var table;
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
