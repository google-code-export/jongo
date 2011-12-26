/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

function getJongoTables(){
    debug('Obtaining Jongo Tables');
    var tables = new Array();
    $.ajax({
        url: '/adminws/table',
        async: false,
        dataType: 'json',
        success: function(data){
            tables = data.response
        }
    })
    return tables;
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
    var data = {}
    var tableName = $("#tableName" + id).val()
    data['name'] = tableName;
    data['customId'] = $("#tableCustomId" + id).val();
    data['permits'] = getPermissionValue(id)
    
    var ret = $.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: '/adminws/table/' + id,
        success: function() {},
        data: JSON.stringify(data),
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

function showTable(tableName){
    var tableMetaData = null;
    $.ajax({
        url: '/jongo/' + tableName + '/meta',
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