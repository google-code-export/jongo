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

function getJongoQueries(){
    debug('Obtaining Jongo Queries');
    var queries = new Array();
    $.ajax({
        url: '/adminws/query',
        async: false,
        dataType: 'json',
        success: function(data){
            queries = data.response
        }
    })
    return queries;
}

function drawJongoQueries(component, queries){
    if(queries == null){
        queries = getJongoQueries();
    }
    // remove the first which is the jongoTest query and we don't want to see it'
    queries.shift();
    $(component).accordion("destroy");
    var items = new Array();
    $.each(queries, function(){
        items.push('<h3 id="h3_');
        items.push(this.id);
        items.push('"><a href="#">');
        items.push(this.database);
        items.push('.');
        items.push(this.name);
        items.push('</a></h3><div id="queryId_');
        items.push(this.id);
        items.push('"><p>');
        items.push(this.description);
        items.push('</p><p><form class="ui-form"><textarea class="jongo-text-area" rows="10" readonly="true">');
        items.push(this.query);
        items.push('</textarea></p>');
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
    items.push(this.database);
    items.push('.');
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

function addQuery(){
    var data = {}
    var database = $("#queryDatabase").val();
    data['database'] = database;
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

function addQueryDialog(componentName){
    $(componentName).html('');

    var output = new Array();
    
    output.push('<form class="ui-form"><table><tr>');
    output.push('<td><label for="queryDatabase">Database</label></td>');
    output.push('<td><label for="queryName">Name</label></td>');
    output.push('<td><label for="queryDescription">Description</label></td>');
    output.push('</tr><tr>');
    output.push('<td><input class="jongo-field" type="text" style="width: 90%;" id="queryDatabase" /></td>');
    output.push('<td><input class="jongo-field" type="text" style="width: 90%;" id="queryName" /></td>');
    output.push('<td><input class="jongo-field" type="text" style="width: 100%;" size="50" id="queryDescription" /></td>');
    output.push('</tr><tr><td colspan="3"><label for="queryText">Query</label></td>');
    output.push('</tr><tr><td colspan="3">');
    output.push('<textarea class="jongo-text-area" rows="10" id="queryText"></textarea>');
    output.push('</td></tr></table></form>');
    
    $(componentName).html(output.join(''));

    $(componentName).dialog({
        modal: true,
        width: 500,
        title: 'Add Query',
        buttons: {
            Test: function() {
                testQuery();
            },
            Save: function() {
                $( this ).dialog( "close" );
                addQuery();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
}

function testQuery(){
    var data = {};
    data['name'] = "jongoTest";
    var database = $("#queryDatabase").val();
    data['database'] = database;
    var queryText = $("#queryText").val();
    data['query'] = queryText.replace(/(\r\n|\n|\r)/gm,"\\n"); // chrome doesn't like line breaks in json
    
    var ret = $.ajax({
        type: 'PUT',
        contentType: "application/json",
        url: '/adminws/query/0',
        data: JSON.stringify(data),
        dataType: 'json',
        success: function(a,b,c) {
            debug("Alright! Updated the query")
            $.get('/' + database + '/query/jongoTest', function(data) {
                debug("Query executed correctly")
                var response = data.response
                var output = new Array();
                var first = response[0]
                output.push('<table class="meta-data-table ui-corner-all"><tr>')
                for( var k in first ){
                    output.push('<th>')
                    output.push(k)
                    output.push('</th>')
                }
                output.push('</tr>')
                $.each(response, function(){
                    output.push('<tr>');
                    for( var k in this){
                        var obj = this[k]
                        output.push('<td>');
                        output.push(obj);
                        output.push('</td>');
                    }
                    output.push('</tr>');
                })
                output.push('</table>');

                $("#errorDialogMessage").html(output.join(''));
                $("#errorDialog").dialog({
                    title: 'test data',
                    width: 600,
                    buttons: {
                        Ok: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                })
            }, 'json');
            
        },
        error: function(error){
            var jongoError = JSON.parse(error.responseText);
            showJQueryDialog("Error " + error.status, jongoError.response.message);
        }
    });
}
