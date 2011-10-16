function kk(){
    alert("Hola JS");
}

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function addTable(evt){
    var data = {}
    data['name'] = $("#tableName").val()
    data['customId'] = $("#tableCustomId").val()
    data['permits'] = $("#jongoPermissions").val()
    
    var ret = $.post('http://localhost:8080/jongo/JongoTable', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new table", data['name']);
    })
    
    ret.error(function(error){
        var jongoError = JSON.parse(error.responseText);
        showJQueryDialog("Error " + error.status, jongoError.response.message);
    });
    return false;
}

function editTable(id){
    var formName = "#fieldForm" + id;
    var values = $('#fieldForm0').serialize();
    return false;
}

function showJQueryDialog(title, message){
    $("#errorDialogMessage").text(message)
    $("#errorDialog").dialog({
        title: title,
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
            }
        }
    })
}