/* Javascript code to handle user interaction on the main page
 * Requres jquery
 *
 */


/* function to render employess in a html list */

var get_employees = function() { 
  var ol = $('#show-all-employees');
  ol.empty();
  $.ajax({
    url: '/employees',
    dataType: 'json',
    success: function(es) { 
      _.each(es, function(e) { 
        $('<li>' + e.name +'</li>').appendTo(ol);
      });
    },
    error: function(error){ 
      alert('error');
    }
  });

};

$(function(){
  get_employees();
  
  $('#add-employee').submit(function() { 
    var form = $(this);
    
    $.ajax({
      url : '/employees/add',
      type: 'POST',
      dataType: 'json',
      data: form.serializeArray(),
      success: function(d) { 
        // reload the employee list
        // ideally this should only append the list 
        // not reload the whole thing
        get_employees();
        // clear the html form
        form.find(':input').each(function() { this.value = ''}); 
      },
      error: function(e) {alert(e)}
    });
    
    return false; // do not allow the normal form behavior
  });

});
