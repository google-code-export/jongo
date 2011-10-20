function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

debug = function (log) {
    if (window.console != undefined) {
        console.log(log);
    }
}

function drawJongoTables(componentName) {
    $.getJSON('http://localhost:8080/jongo/JongoTable?query=findAllBy.Id.GreaterThanEquals&values=0', function(data) {
        var items = new Array();
            $.each(data.response, function(){
                items.push('<form id="fieldForm')
                items.push(this.id);
                items.push('">');
                items.push('<input type="text" readonly="readonly" id="tableId')
                items.push(this.id);
                items.push('" value="');
                items.push(this.id);
                items.push('"/>');
                items.push('<input type="text" id="tableName');
                items.push(this.id);
                items.push('" value="');
                items.push(this.name);
                items.push('"/><input type="text" id="tableCustomId');
                items.push(this.id);
                items.push('" value="');
                items.push(this.customid);
                items.push('"/>');
                items.push(getSelectComponent(this.id, this.permits));
                items.push('<input type="submit" value="Edit" onclick="editTable(');
                items.push(this.id);
                items.push('); return false;"/>');
                items.push('</form>');
            })
        $(componentName).html(items.join(''));
    });
}

function drawJongoQueries(component){
    $.getJSON('http://localhost:8080/jongo/JongoQuery?query=findAllBy.Id.GreaterThanEquals&values=0', function(data) {
        var items = new Array();
            $.each(data.response.rows, function(){
                //
            });
    });
}

function getSelectComponent(id, arg){
    var select = new Array();
    select.push('<select id="jongoPermissions' + id + '">');
    if(arg == 0){
        select.push('<option value="0" selected>None</option>');
    }else{
        select.push('<option value="0">None</option>');
    }
    
    if(arg == 1){
        select.push('<option value="1" selected>Read</option>');
    }else{
        select.push('<option value="1">Read</option>');
    }
    
    if(arg == 2){
        select.push('<option value="2" selected>Write</option>');
    }else{
        select.push('<option value="2">Write</option>');
    }
    
    if(arg == 3){
        select.push('<option value="3" selected>Read & Write</option>');
    }else{
        select.push('<option value="3">Read & Write</option>');
    }
    select.push('</select>');
    return select.join('');
}

function addTable(){
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
}

function editTable(id){
    var data = new Array(3);
    var tableName = $("#tableName" + id).val()
    data.push("name=" + tableName);
    data.push("customId=" + $("#tableCustomId" + id).val());
    data.push("permits=" + $("#jongoPermissions" + id).val());
    
    var ret = $.ajax({
        type: 'PUT',
        url: 'http://localhost:8080/jongo/JongoTable/' + id + '?' + data.join('&'),
        success: function() {},
        data: data,
        dataType: 'json'
    });
    
    ret.success(function(){
        showJQueryDialog("Successfully edited table", tableName);
        
    })
    
    ret.error(function(error){
        var jongoError = JSON.parse(error.responseText);
        showJQueryDialog("Error " + error.status, jongoError.response.message);
    });
}

function addQuery(){
    var data = {}
    data['name'] = $("#queryName").val()
    data['description'] = $("#queryDescription").val()
    data['query'] = $("#queryText").val()
    
    var ret = $.post('http://localhost:8080/jongo/JongoQuery', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new query", data['name']);
    })
    
    ret.error(function(error){
        var jongoError = JSON.parse(error.responseText);
        showJQueryDialog("Error " + error.status, jongoError.response.message);
    });
}

function showJQueryDialog(title, message){
    $("#errorDialogMessage").text(message)
    $("#errorDialog").dialog({
        title: title,
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
                window.location.reload();
            }
        }
    })
}