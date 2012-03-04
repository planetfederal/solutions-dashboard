/* Javascript code to handle user interaction on the main page
 * Requres jquery
 *
 */


/* function to render employess in a html list */

var get_current_employees = function(div) {

  $.ajax({
    url: '/employees',
    success: function(employees){ 
      var list = "<% _.each(employees, function(name) { %> <li><%= name %></li> <% }); %>";
      div.html(function() { return _.template(list, {employees: employees}) });
    },
    error: function(error) {
      alert('Something went wrong' + error);
    }
  });

};

$(function(){
  var employee_list = $('#show-all-employees')
  get_current_employees(employee_list);
  
  $('#add-employee').submit(function() { 
    var form = $(this);
    
    $.ajax({
      url : '/employees/add',
      type: 'POST',
      dataType: 'json',
      data: form.serializeArray(),
      success: function(d) {},
      error: function(e) {alert(e)}
    });
    
    return false; // do not allow the normal form behavior
  });

});
