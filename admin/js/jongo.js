function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

debug = function (log) {
    if (window.console != undefined) {
        console.log(log);
    }
}

function drawJongoTables(componentName) {
    $.getJSON('http://localhost:8080/adminws/table/all', function(data) {
        var items = new Array();
        var ids = new Array();
            $.each(data.response, function(){
                ids.push(this.id);
                items.push('<div class="jongo-form-row" id="tableRow_')
                items.push(this.id);
                items.push('"><form><input class="jongo-field" type="text" readonly="readonly" id="tableId');
                items.push(this.id);
                items.push('" value="');
                items.push(this.id);
                items.push('"/>');
                items.push('<input class="jongo-field" type="text" id="tableName');
                items.push(this.id);
                items.push('" value="');
                items.push(this.name);
                items.push('"/><input class="jongo-field" type="text" id="tableCustomId');
                items.push(this.id);
                items.push('" value="');
                items.push(this.customid);
                items.push('"/>');
                items.push(getPermissionsComboBox(this.id, this.permits));
                items.push('<input type="submit" class="jquery-button" value="Edit" onclick="editTable(');
                items.push(this.id);
                items.push('); return false;"/>');
                items.push('<input type="submit" class="jquery-button" value="Delete" onclick="deleteTable(');
                items.push(this.id);
                items.push('); return false;"/>');
                items.push('</form></div>');
            })
        $(componentName).html(items.join(''));
        $('.jquery-button').button();
        $.each(ids, function(){
            var name = "#jongoPermissions" + this;
            $(name).buttonset();
        });
    });
}

function drawJongoQueries(component){
    $.getJSON('http://localhost:8080/adminws/query/all', function(data) {
        var items = new Array();
            $.each(data.response, function(){
                items.push('<h3 id="h3_');
                items.push(this.id);
                items.push('"><a href="#">');
                items.push(this.name);
                items.push('</a></h3><div id="queryId_');
                items.push(this.id);
                items.push('"><p>');
                items.push(this.description);
                items.push('</p><p>');
                items.push(this.query);
                items.push('</p><form>');
                items.push('<input type="hidden" value="');
                items.push(this.name);
                items.push('" id="query');
                items.push(this.id);
                items.push('">');
                items.push('<input type="submit" class="jquery-button" value="Delete" onclick="deleteQuery(');
                items.push(this.id);
                items.push('); return false;"/>');
                items.push('</form></div>');
            });
        $(component).html(items.join(''));
        $(component).accordion({
                    autoHeight: false,
                    navigation: true,
                    collapsible: true
        });
        $('.jquery-button').button();
    });
}

function getPermissionsComboBox(id, arg){
    var componentName = 'jongoPermissions' + id;
    var combo = new Array();
    combo.push('<span id="' + componentName + '">');
    
    if(arg == 1){
        combo.push('<input type="checkbox" id="read_' + id + '" name="radio"  checked="checked"/><label for="read_' + id + '">Read</label>');
        combo.push('<input type="checkbox" id="write_' + id + '" name="radio" /><label for="write_' + id + '">Write</label>');
    }else if(arg == 2){
        combo.push('<input type="checkbox" id="read_' + id + '" name="radio" /><label for="read_' + id + '">Read</label>');
        combo.push('<input type="checkbox" id="write_' + id + '" name="radio"  checked="checked"/><label for="write_' + id + '">Write</label>');
    }else if(arg == 3){
        combo.push('<input type="checkbox" id="read_' + id + '" name="radio"  checked="checked"/><label for="read_' + id + '">Read</label>');
        combo.push('<input type="checkbox" id="write_' + id + '" name="radio"  checked="checked"/><label for="write_' + id + '">Write</label>');
    }else{
        combo.push('<input type="checkbox" id="read_' + id + '" name="radio" /><label for="read_' + id + '">Read</label>');
        combo.push('<input type="checkbox" id="write_' + id + '" name="radio" /><label for="write_' + id + '">Write</label>');
    }
    combo.push('</span>');
    return combo.join('');
}

function addTable(){
    var data = {}
    data['name'] = $("#tableName").val()
    data['customId'] = $("#tableCustomId").val()
    data['permits'] = getPermissionValue(null);
    
    var ret = $.post('http://localhost:8080/admin/table', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new table", data['name']);
        window.location.reload();
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
        url: 'http://localhost:8080/admin/table/' + id + '?' + data.join('&'),
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

function deleteTable(id){
    var tableName = $("#tableName" + id).val()
    
    $("#confirmDialogMessage").text('Are you sure you wish to continue and delete the table?')
    $( "#confirmDialog" ).dialog({
        resizable: false,
        height:200,
        width: 300,
        modal: true,
        title: 'Delete Table ' + tableName,
        buttons: {
            Ok: function() {
                $('#tableRow_'+id).remove();
                var ret = $.ajax({
                    type: 'DELETE',
                    url: 'http://localhost:8080/admin/table/' + id,
                    success: function() {},
                    dataType: 'json'
                });

                ret.success(function(){
                    showJQueryDialog("Successfully deleted table", tableName);

                })

                ret.error(function(error){
                    var jongoError = JSON.parse(error.responseText);
                    showJQueryDialog("Error " + error.status, jongoError.response.message);
                });
                $( this ).dialog( "close" );
//                window.location.reload();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
}

function deleteQuery(id){
    var queryName = $("#query" + id).val();
    
    $("#confirmDialogMessage").text('Are you sure you wish to continue and delete the query?')
    $( "#confirmDialog" ).dialog({
        resizable: false,
        height:200,
        width: 300,
        modal: true,
        title: 'Delete Query ' + queryName,
        buttons: {
            Ok: function() {
                $('#h3_'+id).remove();
                $('#queryId_'+id).remove();
                var ret = $.ajax({
                    type: 'DELETE',
                    url: 'http://localhost:8080/admin/query/' + id,
                    success: function() {},
                    dataType: 'json'
                });

                ret.success(function(){
                    showJQueryDialog("Successfully deleted query", queryName);

                })

                ret.error(function(error){
                    var jongoError = JSON.parse(error.responseText);
                    showJQueryDialog("Error " + error.status, jongoError.response.message);
                });
                $( this ).dialog( "close" );
//                window.location.reload();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
    

}

function addQuery(){
    var data = {}
    data['name'] = $("#queryName").val();
    data['description'] = $("#queryDescription").val();
    data['query'] = $("#queryText").val();
    
    var ret = $.post('http://localhost:8080/admin/query', data, function() {}, 'json');
    
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
            }
        }
    })
}

function showConfirmationDialog(title, message){
    $("#confirmDialogMessage").text(message)
    $( "#confirmDialog" ).dialog({
        resizable: false,
        height:140,
        modal: true,
        title: title,
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
}

function getPermissionValue(id){
    var r = null;
    var w = null;
    if(id == null){
        r = $("#read");
        w = $("#write");
    }else{
        r = $("#read_"+id);
        w = $("#write_"+id);
    }
    
    var readIsSelected = (r.attr("checked") != "undefined" && r.attr("checked") == "checked");
    var writeIsSelected = (w.attr("checked") != "undefined" && w.attr("checked") == "checked");
    var ret = 0;
    if(!readIsSelected && !writeIsSelected){
        ret = 0;
    }else if(readIsSelected && !writeIsSelected){
        ret = 1;
    }else if(!readIsSelected && writeIsSelected){
        ret = 2;
    }else if(readIsSelected && writeIsSelected){
        ret = 3;
    }else{
        ret = 0;
    }
    return ret;
    
}