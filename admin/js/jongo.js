function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

debug = function (log) {
    if (window.console != undefined) {
        console.log(log);
    }
}

function getJongoTables(){
    debug('Obtaining Jongo Tables');
    var tables = new Array();
    $.ajax({
        url: '/adminws/table/all',
        async: false,
        dataType: 'json',
        success: function(data){
            tables = data.response
        }
    })
    return tables;
}

function getJongoQueries(){
    debug('Obtaining Jongo Queries');
    var queries = new Array();
    $.ajax({
        url: '/adminws/query/all',
        async: false,
        dataType: 'json',
        success: function(data){
            queries = data.response
        }
    })
    return queries;
}

function drawJongoTables(componentName, tables) {
    if(tables == null){
        tables = getJongoTables();
    }
    $(componentName).html('');
    var items = new Array();
    var ids = new Array();
    $.each(tables, function(){
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
        items.push('<input type="submit" class="jquery-button" value="Show" onclick="showTable(\'');
        items.push(this.name);
        items.push('\'); return false;"/>');
        items.push('<input type="submit" class="jquery-button" value="Update" onclick="editTable(');
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
}

function drawJongoQueries(component, queries){
    if(queries == null){
        queries = getJongoQueries();
    }
    $(component).accordion("destroy");
    var items = new Array();
    $.each(queries, function(){
        items.push('<h3 id="h3_');
        items.push(this.id);
        items.push('"><a href="#">');
        items.push(this.name);
        items.push('</a></h3><div id="queryId_');
        items.push(this.id);
        items.push('"><p>');
        items.push(this.description);
        items.push('</p><p><textarea class="jongo-text-area" rows="10" readonly="true">');
        items.push(this.query);
        items.push('</textarea></p><form>');
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
    $(component).accordion({active: false, autoHeight: false, navigation: true, collapsible: true});
    $('.jquery-button').button();
}

function addQueryToAccordion(component, id, name, description, query){
    if(component == null){
        component = '#jqueries';
    }
    
    var items = new Array();
    items.push('<h3 id="h3_');
    items.push(id);
    items.push('"><a href="#">');
    items.push(name);
    items.push('</a></h3><div id="queryId_');
    items.push(id);
    items.push('"><p>');
    items.push(description);
    items.push('</p><p><textarea class="jongo-text-area" rows="10" readonly="true">');
    items.push(query);
    items.push('</textarea></p><form>');
    items.push('<input type="hidden" value="');
    items.push(name);
    items.push('" id="query');
    items.push(id);
    items.push('">');
    items.push('<input type="submit" class="jquery-button" value="Delete" onclick="deleteQuery(');
    items.push(id);
    items.push('); return false;"/>');
    items.push('</form></div>');
    
    $(component).append(items.join(''));
    $(component).accordion("destroy");
    $(component).accordion({
                autoHeight: false,
                navigation: true,
                collapsible: true
    });
}

function drawJongoPlaygroundTablesCombo(component, tables){
    var selector = $(component);
    $.each(tables, function(){
        selector.append(
            $("<option></option>").attr("value", this.id).text(this.name)
        );
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
    
    var ret = $.post('/adminws/table', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new table", data['name']);
        drawJongoTables('#jtables');
    })
    
    ret.error(function(error){
        var jongoError = JSON.parse(error.responseText);
        showJQueryDialog("Error " + error.status, jongoError.response.message);
    });
}

function addTableDialog(componentName){
    $(componentName).html('');

    var output = new Array();
    output.push('<form><p>');
    output.push('<input class="jongo-field" id="tableName" value="" type="text"/>');
    output.push('<label class="low-contrast-label" for="tableName">Name</label>');
    output.push('</p><p>');
    output.push('<input class="jongo-field" id="tableCustomId" value="id" type="text"/>');
    output.push('<label class="low-contrast-label" for="tableCustomId">Custom ID</label>');
    output.push('</p><span id="jongo-permissions">');
    output.push('<input type="checkbox" id="read" name="radio" /><label for="read">Read</label>');
    output.push('<input type="checkbox" id="write" name="radio" /><label for="write">Write</label>');
    output.push('</span></form>');
    $(componentName).html(output.join(''));

    $('#jongo-permissions').buttonset();

    $(componentName).dialog({
        modal: true,
        title: 'Add Table',
        buttons: {
            Ok: function() {
                $( this ).dialog( "close" );
                addTable();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
}

function editTable(id){
    var data = new Array(3);
    var tableName = $("#tableName" + id).val()
    data.push("name=" + tableName);
    data.push("customId=" + $("#tableCustomId" + id).val());
    data.push("permits=" + getPermissionValue(id));
    
    var ret = $.ajax({
        type: 'PUT',
        url: '/adminws/table/' + id + '?' + data.join('&'),
        success: function() {},
        data: data,
        dataType: 'json'
    });
    
    ret.success(function(){
        showJQueryDialog("Successfully edited table", tableName);
        drawJongoTables('#jtables');
    });
    
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
                var ret = $.ajax({
                    type: 'DELETE',
                    url: 'http://localhost:8080/adminws/table/' + id,
                    success: function() {},
                    dataType: 'json'
                });

                ret.success(function(){
                    showJQueryDialog("Successfully deleted table", tableName);
                    drawJongoTables('#jtables');
                })

                ret.error(function(error){
                    var jongoError = JSON.parse(error.responseText);
                    showJQueryDialog("Error " + error.status, jongoError.response.message);
                });
                $( this ).dialog( "close" );
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
                var ret = $.ajax({
                    type: 'DELETE',
                    url: '/adminws/query/' + id,
                    success: function() {},
                    dataType: 'json'
                });

                ret.success(function(){
                    showJQueryDialog("Successfully deleted query", queryName);
                    drawJongoQueries('#jqueries');
                })

                ret.error(function(error){
                    var jongoError = JSON.parse(error.responseText);
                    showJQueryDialog("Error " + error.status, jongoError.response.message);
                });
                $( this ).dialog( "close" );
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
    var queryText = $("#queryText").val()
    data['query'] = queryText.replace(/(\r\n|\n|\r)/gm,"\\n"); // chrome doesn't like line breaks in json
    
    var ret = $.post('/adminws/query', data, function() {}, 'json');
    
    ret.success(function(){
        showJQueryDialog("Successfully added new query", data['name']);
        drawJongoQueries('#jqueries');
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

function loadTable(componentName, targetName){
    var tableName = $(componentName).find(":selected").text();
    var tableMetaData = null;
    $.ajax({
        url: '/jongo/' + tableName,
        async: false,
        dataType: 'json',
        success: function(data){
            tableMetaData = data.response
        },
        error: function(error){
            var jongoError = JSON.parse(error.responseText);
            showJQueryDialog("Error " + error.status, jongoError.response.message);
        }
    });
    
    if(tableMetaData != null){
        var output = new Array();
        output.push('<h3>' + tableName + '</h3><table class="meta-data-table ui-corner-all"><tr><th>Name</th><th>Size</th><th>Type</th></tr>')
        $.each(tableMetaData, function(){
            output.push('<tr><td>');
            output.push(this.columnname);
            output.push('</td><td>');
            output.push(this.columnsize);
            output.push('</td><td>');
            output.push(this.columntype);
            output.push('</td></tr>');
        })
        output.push('</table>');
        $(targetName).html(output.join(''));
    }
}

function showTable(tableName){
    var tableMetaData = null;
    $.ajax({
        url: '/jongo/' + tableName,
        async: false,
        dataType: 'json',
        success: function(data){
            tableMetaData = data.response
        },
        error: function(error){
            var jongoError = JSON.parse(error.responseText);
            showJQueryDialog("Error " + error.status, jongoError.response.message);
        }
    });
    
    if(tableMetaData != null){
        var output = new Array();
        output.push('<table class="meta-data-table ui-corner-all"><tr><th>Name</th><th>Size</th><th>Type</th></tr>')
        $.each(tableMetaData, function(){
            output.push('<tr><td>');
            output.push(this.columnname);
            output.push('</td><td>');
            output.push(this.columnsize);
            output.push('</td><td>');
            output.push(this.columntype);
            output.push('</td></tr>');
        })
        output.push('</table>');
        
        $("#errorDialogMessage").html(output.join(''));
        $("#errorDialog").dialog({
            title: tableName + ' data',
            buttons: {
                Ok: function() {
                    $( this ).dialog( "close" );
                }
            }
        })
    }
}