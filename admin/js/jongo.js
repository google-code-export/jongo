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

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

debug = function (log) {
    if (window.console != undefined) {
        console.log(log);
    }
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

function drawJongoPlaygroundTablesCombo(component, tables){
    var selector = $(component);
    $.each(tables, function(){
        selector.append(
            $("<option></option>").attr("value", this.id).text(this.name)
        );
    });
}